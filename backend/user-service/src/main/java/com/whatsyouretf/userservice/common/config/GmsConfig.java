package com.whatsyouretf.userservice.common.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * LLM API 설정 (Anthropic 직접 호출 우선, GMS 대체)
 */
@Configuration
@Slf4j
public class GmsConfig {

    @Value("${gms.api.base-url}")
    private String gmsBaseUrl;

    @Value("${gms.api.key:}")
    private String gmsApiKey;

    @Value("${anthropic.api.base-url:https://api.anthropic.com}")
    private String anthropicBaseUrl;

    @Value("${anthropic.api.key:}")
    private String anthropicApiKey;

    @Bean
    public WebClient gmsWebClient() {
        // Anthropic API 키가 있으면 직접 호출, 없으면 GMS 사용
        String baseUrl;
        String apiKey;

        if (anthropicApiKey != null && !anthropicApiKey.isEmpty()) {
            baseUrl = anthropicBaseUrl;
            apiKey = anthropicApiKey;
            log.info("LLM API: Anthropic 직접 호출 모드");
        } else {
            baseUrl = gmsBaseUrl;
            apiKey = gmsApiKey;
            log.info("LLM API: GMS 경유 모드");
        }

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(Duration.ofSeconds(60))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("Content-Type", "application/json")
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }
}
