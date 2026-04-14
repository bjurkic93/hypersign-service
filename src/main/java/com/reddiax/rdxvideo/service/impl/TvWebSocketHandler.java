package com.reddiax.rdxvideo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reddiax.rdxvideo.model.entity.TvAuthSessionEntity;
import com.reddiax.rdxvideo.repository.TvAuthSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for TV devices.
 * Manages connections and broadcasts messages to connected devices.
 * 
 * Connection URL: ws://host/ws/tv?token={deviceToken}
 * 
 * Messages sent to clients:
 * - {"action": "REFRESH_CONTENT"} - reload content from API
 * - {"action": "RESTART_APP"} - restart the application
 * - {"action": "PING"} - keep-alive ping
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TvWebSocketHandler extends TextWebSocketHandler {

    private final TvAuthSessionRepository tvAuthSessionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Map: organizationId -> Map<sessionId, WebSocketSession>
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, WebSocketSession>> organizationSessions = new ConcurrentHashMap<>();
    
    // Map: sessionId -> deviceToken (for reverse lookup)
    private final ConcurrentHashMap<String, String> sessionToToken = new ConcurrentHashMap<>();
    
    // Map: sessionId -> organizationId (for reverse lookup)
    private final ConcurrentHashMap<String, Long> sessionToOrg = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractToken(session);
        if (token == null) {
            log.warn("WebSocket connection without token, closing");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        Optional<TvAuthSessionEntity> authSession = tvAuthSessionRepository.findByAccessToken(token);
        if (authSession.isEmpty() || authSession.get().getOrganization() == null) {
            log.warn("Invalid device token for WebSocket: {}", token.substring(0, Math.min(20, token.length())));
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        Long orgId = authSession.get().getOrganization().getId();
        String sessionId = session.getId();

        organizationSessions.computeIfAbsent(orgId, k -> new ConcurrentHashMap<>()).put(sessionId, session);
        sessionToToken.put(sessionId, token);
        sessionToOrg.put(sessionId, orgId);

        log.info("TV WebSocket connected: org={}, sessionId={}, total connections for org={}", 
                orgId, sessionId, organizationSessions.get(orgId).size());

        // Send welcome message
        sendMessage(session, Map.of("action", "CONNECTED", "message", "WebSocket connected successfully"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        Long orgId = sessionToOrg.remove(sessionId);
        sessionToToken.remove(sessionId);

        if (orgId != null) {
            ConcurrentHashMap<String, WebSocketSession> orgSessions = organizationSessions.get(orgId);
            if (orgSessions != null) {
                orgSessions.remove(sessionId);
                log.info("TV WebSocket disconnected: org={}, sessionId={}, remaining={}", 
                        orgId, sessionId, orgSessions.size());
                if (orgSessions.isEmpty()) {
                    organizationSessions.remove(orgId);
                }
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received WebSocket message: {}", payload);

        // Handle PONG responses from clients
        if (payload.contains("PONG")) {
            return;
        }

        // Echo back for testing
        sendMessage(session, Map.of("action", "ACK", "received", payload));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
    }

    /**
     * Send content refresh notification to all devices in an organization.
     */
    public void sendContentRefresh(Long organizationId) {
        broadcastToOrganization(organizationId, Map.of("action", "REFRESH_CONTENT"));
    }

    /**
     * Send restart command to all devices in an organization.
     */
    public void sendRestartCommand(Long organizationId) {
        broadcastToOrganization(organizationId, Map.of("action", "RESTART_APP"));
    }

    /**
     * Send a custom message to all devices in an organization.
     */
    public void broadcastToOrganization(Long organizationId, Map<String, Object> message) {
        ConcurrentHashMap<String, WebSocketSession> orgSessions = organizationSessions.get(organizationId);
        if (orgSessions == null || orgSessions.isEmpty()) {
            log.debug("No WebSocket connections for org {}", organizationId);
            return;
        }

        int sent = 0;
        int failed = 0;
        for (WebSocketSession session : orgSessions.values()) {
            if (session.isOpen()) {
                if (sendMessage(session, message)) {
                    sent++;
                } else {
                    failed++;
                }
            }
        }
        log.info("WebSocket broadcast to org {}: sent={}, failed={}, message={}", 
                organizationId, sent, failed, message.get("action"));
    }

    /**
     * Get count of connected devices for an organization.
     */
    public int getConnectionCount(Long organizationId) {
        ConcurrentHashMap<String, WebSocketSession> orgSessions = organizationSessions.get(organizationId);
        return orgSessions != null ? orgSessions.size() : 0;
    }

    /**
     * Get total connection count across all organizations.
     */
    public int getTotalConnectionCount() {
        return organizationSessions.values().stream()
                .mapToInt(ConcurrentHashMap::size)
                .sum();
    }

    private boolean sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
            return true;
        } catch (IOException e) {
            log.error("Failed to send WebSocket message: {}", e.getMessage());
            return false;
        }
    }

    private String extractToken(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;

        String query = uri.getQuery();
        if (query == null) return null;

        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && "token".equals(pair[0])) {
                return pair[1];
            }
        }
        return null;
    }
}
