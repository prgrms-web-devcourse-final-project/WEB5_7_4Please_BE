package com.deal4u.fourplease.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final BidWebSocketHandler bidWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                // addHandler : WebSocket 핸들러를 지정된 URL 경로에 등록합니다.
                .addHandler(bidWebSocketHandler, "/ws/auction/{auctionId}")
                // 접속을 시도하는 모든 도메인 or IP에서 WebSocket 연결을 허용합니다.
                .setAllowedOrigins("*");
    }
}
