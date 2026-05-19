"""RabbitMQ 이벤트 발행자"""
import json
import logging
from typing import List, Optional

import aio_pika
from aio_pika import Message, DeliveryMode

from app.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()

# 큐/익스체인지 이름
EXCHANGE_NAME = "wye.events"
QUEUE_NEWS_ALERT = "wye.news.alert"
QUEUE_PORTFOLIO_ALERT = "wye.portfolio.alert"


class EventPublisher:
    """RabbitMQ 이벤트 발행자"""

    def __init__(self):
        self.connection: Optional[aio_pika.RobustConnection] = None
        self.channel: Optional[aio_pika.Channel] = None

    async def connect(self):
        """RabbitMQ 연결"""
        if self.connection and not self.connection.is_closed:
            return

        try:
            self.connection = await aio_pika.connect_robust(
                host=settings.rabbitmq_host,
                port=settings.rabbitmq_port,
                login=settings.rabbitmq_user,
                password=settings.rabbitmq_password,
            )
            self.channel = await self.connection.channel()

            # Exchange 선언
            await self.channel.declare_exchange(
                EXCHANGE_NAME,
                aio_pika.ExchangeType.DIRECT,
                durable=True
            )

            logger.info("RabbitMQ 연결 완료")
        except Exception as e:
            logger.error(f"RabbitMQ 연결 실패: {e}")
            self.connection = None
            self.channel = None

    async def close(self):
        """연결 종료"""
        if self.connection and not self.connection.is_closed:
            await self.connection.close()
            logger.info("RabbitMQ 연결 종료")

    async def publish_news_alert(
        self,
        news_id: int,
        news_title: str,
        news_summary: Optional[str],
        etf_ids: List[int],
        influence_type: Optional[str] = None
    ) -> bool:
        """
        뉴스 알림 이벤트 발행

        Args:
            news_id: 뉴스 ID
            news_title: 뉴스 제목
            news_summary: 뉴스 요약
            etf_ids: 영향받는 ETF ID 목록
            influence_type: 영향 유형

        Returns:
            발행 성공 여부
        """
        if not etf_ids:
            return False

        await self.connect()

        if not self.channel:
            logger.warning("RabbitMQ 채널이 없어 이벤트 발행 스킵")
            return False

        try:
            event = {
                "eventType": "NEWS_ANALYZED",
                "newsId": news_id,
                "newsTitle": news_title,
                "newsSummary": news_summary or "",
                "etfIds": etf_ids,
                "influenceType": influence_type or "NEUTRAL"
            }

            exchange = await self.channel.get_exchange(EXCHANGE_NAME)
            await exchange.publish(
                Message(
                    body=json.dumps(event).encode(),
                    delivery_mode=DeliveryMode.PERSISTENT,
                    content_type="application/json"
                ),
                routing_key=QUEUE_NEWS_ALERT
            )

            logger.info(f"뉴스 알림 이벤트 발행: newsId={news_id}, etfCount={len(etf_ids)}")
            return True

        except Exception as e:
            logger.error(f"이벤트 발행 실패: {e}")
            return False

    async def publish_portfolio_alert(
        self,
        portfolio_id: int,
        user_id: int,
        portfolio_name: str,
        change_rate: float,
        threshold: int,
        direction: str
    ) -> bool:
        """
        포트폴리오 가치 변동 알림 이벤트 발행

        Args:
            portfolio_id: 포트폴리오 ID
            user_id: 사용자 ID
            portfolio_name: 포트폴리오 이름
            change_rate: 변동률 (%, 음수 = 하락)
            threshold: 트리거 임계값 (5 또는 10)
            direction: "상승" 또는 "하락"

        Returns:
            발행 성공 여부
        """
        await self.connect()

        if not self.channel:
            logger.warning("RabbitMQ 채널이 없어 포트폴리오 알림 이벤트 발행 스킵")
            return False

        try:
            event = {
                "eventType": "PORTFOLIO_ALERT",
                "portfolioId": portfolio_id,
                "userId": user_id,
                "portfolioName": portfolio_name,
                "changeRate": change_rate,
                "threshold": threshold,
                "direction": direction,
            }

            exchange = await self.channel.get_exchange(EXCHANGE_NAME)
            await exchange.publish(
                Message(
                    body=json.dumps(event, ensure_ascii=False).encode(),
                    delivery_mode=DeliveryMode.PERSISTENT,
                    content_type="application/json"
                ),
                routing_key=QUEUE_PORTFOLIO_ALERT
            )

            logger.info(
                f"포트폴리오 알림 이벤트 발행: portfolioId={portfolio_id}, "
                f"userId={user_id}, change={change_rate}%, threshold={threshold}%"
            )
            return True

        except Exception as e:
            logger.error(f"포트폴리오 알림 이벤트 발행 실패: {e}")
            return False


# 싱글톤 인스턴스
event_publisher = EventPublisher()
