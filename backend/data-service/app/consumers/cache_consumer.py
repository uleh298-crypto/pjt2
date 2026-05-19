"""RabbitMQ consumer: ETF 캐시 갱신 요청 처리"""
import logging

import aio_pika

from app.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()


async def handle_cache_sync(message: aio_pika.abc.AbstractIncomingMessage):
    async with message.process():
        try:
            logger.info("[MQ] 전체 ETF 캐시 갱신 요청 수신")
            from app.schedulers.scheduler import fire_cache_sync
            fire_cache_sync()  # 별도 스레드+이벤트루프에서 실행 (_sync_running 중복 방지 내장)
            logger.info("[MQ] 전체 ETF 캐시 갱신 요청 스레드에 위임")
        except Exception as e:
            logger.error(f"[MQ] ETF 캐시 갱신 처리 실패: {e}")


async def start_cache_consumer():
    try:
        connection = await aio_pika.connect_robust(
            host=getattr(settings, "rabbitmq_host", "localhost"),
            port=getattr(settings, "rabbitmq_port", 5672),
            login=getattr(settings, "rabbitmq_user", "guest"),
            password=getattr(settings, "rabbitmq_password", "guest"),
        )
        channel = await connection.channel()
        await channel.set_qos(prefetch_count=5)

        exchange = await channel.declare_exchange("wye.events", aio_pika.ExchangeType.DIRECT, durable=True)
        queue = await channel.declare_queue("wye.cache.etf.sync", durable=True)
        await queue.bind(exchange, routing_key="wye.cache.etf.sync")

        await queue.consume(handle_cache_sync)
        logger.info("[MQ] ETF 캐시 갱신 consumer 시작")
        return connection
    except Exception as e:
        logger.warning(f"[MQ] RabbitMQ 연결 실패 (consumer 비활성화): {e}")
        return None
