package com.daniele.fantalive.configurazione;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.daniele.fantalive.bl.Main;

@Component
public class SocketHandlerFantalive extends TextWebSocketHandler implements WebSocketHandler {

	private static final Logger log = LoggerFactory.getLogger(SocketHandlerFantalive.class);

	private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

	public void invia(Map<?, ?> map) throws IOException {
		invia(Main.toJson(map));
	}

	public void invia(String payload) throws IOException {
		if (sessions.isEmpty()) {
			return;
		}
		for (WebSocketSession webSocketSession : sessions) {
			if (webSocketSession.isOpen()) {
				synchronized (webSocketSession) {
					webSocketSession.sendMessage(new TextMessage(payload));
				}
			}
		}
	}

	public List<WebSocketSession> getSessions() {
		return sessions;
	}

	public boolean hasActiveSessions() {
		for (WebSocketSession session : sessions) {
			if (session.isOpen()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessions.add(session);
		pruneClosedSessions();
		log.info("[FantaLive WS] connect session={} remote={} sessions={}",
				session.getId(), session.getRemoteAddress(), sessions.size());
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		boolean removed = sessions.remove(session);
		log.info("[FantaLive WS] disconnect session={} remote={} status={} removed={} sessions={}",
				session.getId(), session.getRemoteAddress(), status, removed, sessions.size());
	}

	private void pruneClosedSessions() {
		sessions.removeIf(session -> !session.isOpen());
	}

}
