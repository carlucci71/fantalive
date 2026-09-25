package com.daniele.asta.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.function.IntSupplier;

@Service
public class AstaBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(AstaBroadcastService.class);
    private static final long INVIA_SLOW_MS = 100;

    public void broadcastAll(List<WebSocketSession> sessions, String payload, IntSupplier utentiCount)
            throws IOException {
        long inviaStart = System.nanoTime();
        int totalSessions = sessions.size();
        int openSessions = 0;
        int sent = 0;
        for (WebSocketSession webSocketSession : sessions) {
            if (webSocketSession.isOpen()) {
                openSessions++;
                synchronized (webSocketSession) {
                    webSocketSession.sendMessage(new TextMessage(payload));
                }
                sent++;
            }
        }
        long inviaMs = (System.nanoTime() - inviaStart) / 1_000_000;
        int payloadBytes = payload != null ? payload.length() : 0;
        boolean scheduled = Thread.currentThread().getName().contains("scheduling");
        int utenti = utentiCount.getAsInt();
        if (inviaMs >= INVIA_SLOW_MS) {
            log.warn("[WS] invia LENTO ms={} sessions={}/{} sent={} bytes={} scheduled={} utenti={}",
                    inviaMs, openSessions, totalSessions, sent, payloadBytes, scheduled, utenti);
        } else if (!scheduled) {
            log.info("[WS] invia ms={} sessions={}/{} sent={} bytes={} utenti={}",
                    inviaMs, openSessions, totalSessions, sent, payloadBytes, utenti);
        } else if (log.isDebugEnabled()) {
            log.debug("[WS] invia scheduled ms={} sessions={}/{} bytes={}",
                    inviaMs, openSessions, totalSessions, payloadBytes);
        }
    }

    public void broadcastExcept(List<WebSocketSession> sessions, WebSocketSession except, String payload)
            throws IOException {
        for (WebSocketSession webSocketSession : sessions) {
            if (!webSocketSession.isOpen()) {
                continue;
            }
            if (except != null && webSocketSession.getId().equals(except.getId())) {
                continue;
            }
            synchronized (webSocketSession) {
                webSocketSession.sendMessage(new TextMessage(payload));
            }
        }
    }
}
