package com.daniele.fantalive.configurazione;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.daniele.fantalive.bl.Main;

@Component
public class SocketHandlerFantalive extends TextWebSocketHandler implements WebSocketHandler {

	public void invia(Map map) throws IOException {
		invia(Main.toJson(map));
	}
	
	public void invia(String payload) throws IOException {
		for (WebSocketSession webSocketSession : getSessions()) {
			if (webSocketSession.isOpen()) {
				synchronized (webSocketSession) {
					webSocketSession.sendMessage(new TextMessage(payload));
				}
			}
		}
	}
	private List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
	public List<WebSocketSession> getSessions() {
		return sessions;
	}

	public void setSessions(List<WebSocketSession> sessions) {
		this.sessions = sessions;
	}
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//		HttpSession httpSession = (HttpSession) session.getAttributes().get("HTTPSESSIONID");
		getSessions().add(session);
		Iterator<WebSocketSession> iterator = sessions.iterator();
		List<WebSocketSession> rimuovibili = new ArrayList<>();
		while (iterator.hasNext()) {
			WebSocketSession webSocketSession = (WebSocketSession) iterator.next();
			if (!webSocketSession.isOpen()) {
				rimuovibili.add(webSocketSession);
			}
		}
		for (WebSocketSession webSocketSession : rimuovibili) {
			sessions.remove(webSocketSession);
		}

	}

}
