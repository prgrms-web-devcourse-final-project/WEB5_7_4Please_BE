package com.deal4u.fourplease.config;

import com.deal4u.fourplease.domain.bid.dto.BidMessageResponse;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.entity.BidMessageStatus;
import com.deal4u.fourplease.domain.bid.mapper.BidMapper;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class BidWebSocketHandler extends TextWebSocketHandler {

    // 해당하는 경매ID에 대해 session1, session2 ..의 형태로 경매방을 관리합니다.
    // ConcurrentHashMap : Read에 대해서는 Multi-Thread가 동시에 접근이 가능
    // Write에 대해서는 Lock이 걸립니다.
    private final Map<Long, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public BidWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Client Connection
        Long auctionId = getAuctionId(session);
        rooms.computeIfAbsent(auctionId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
            throws Exception {
        // Client Connection Lost
        Long auctionId = getAuctionId(session);
        Set<WebSocketSession> roomSessions = rooms.get(auctionId);
        if (roomSessions != null) {
            roomSessions.remove(session);
        }
    }

    // BidService에서 입찰 시에 호출할 Method
    public void broadcastBid(Bid newBid, BidMessageStatus type) {
        Long auctionId = newBid.getAuction().getAuctionId();
        Set<WebSocketSession> roomSessions = rooms.get(auctionId);

        if (roomSessions == null) {
            return;
        }

        try {
            BidMessageResponse messageResponse = BidMapper.toMessageResponse(newBid, type);
            String json = objectMapper.writeValueAsString(messageResponse);
            TextMessage message = new TextMessage(json);

            // 해당 경매의 입찰 내역의 모든 세션에 새 입찰 정보 전송
            for (WebSocketSession session : roomSessions) {
                session.sendMessage(message);
            }

        } catch (IOException e) {
            throw ErrorCode.WEBSOCKET_SEND_ERROR.toException();
        }
    }

    // URL에서 auctionId를 추출
    private Long getAuctionId(WebSocketSession session) {
        String path = session.getUri().getPath();
        return Long.parseLong(path.substring(path.lastIndexOf("/") + 1));
    }
}
