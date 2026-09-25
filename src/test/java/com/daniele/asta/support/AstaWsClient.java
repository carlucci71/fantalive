package com.daniele.asta.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class AstaWsClient implements AutoCloseable {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final List<String> rawMessages = new CopyOnWriteArrayList<>();
    private final StandardWebSocketClient client = new StandardWebSocketClient();
    private WebSocketSession session;
    private final CountDownLatch openLatch = new CountDownLatch(1);

    public void connect(String baseUrl, String jsessionId) throws Exception {
        String wsUrl = baseUrl.replace("http://", "ws://").replace("https://", "wss://") + "/messaggi-websocket";
        org.springframework.web.socket.WebSocketHttpHeaders headers = new org.springframework.web.socket.WebSocketHttpHeaders();
        headers.add("Cookie", "JSESSIONID=" + jsessionId);

        TextWebSocketHandler handler = new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession webSocketSession) {
                session = webSocketSession;
                openLatch.countDown();
            }

            @Override
            protected void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) {
                rawMessages.add(message.getPayload());
            }
        };

        session = client.doHandshake(handler, headers, URI.create(wsUrl)).get(10, TimeUnit.SECONDS);
        if (!openLatch.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("WebSocket non aperto in tempo");
        }
    }

    public void sendJson(Map<String, Object> payload) throws Exception {
        ensureOpen();
        session.sendMessage(new TextMessage(MAPPER.writeValueAsString(payload)));
    }

    public void sendOperazione(String operazione, Map<String, Object> fields) throws Exception {
        Map<String, Object> payload = new java.util.LinkedHashMap<>(fields);
        payload.put("operazione", operazione);
        sendJson(payload);
    }

    public void connetti(String nome, int id) throws Exception {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("operazione", "connetti");
        payload.put("nomegiocatore", nome);
        payload.put("idgiocatore", Integer.toString(id));
        payload.put("tokenUtente", System.currentTimeMillis());
        sendJson(payload);
    }

    public Map<String, Object> waitFor(Predicate<Map<String, Object>> predicate, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            for (String raw : new ArrayList<>(rawMessages)) {
                Map<String, Object> msg = parse(raw);
                if (predicate.test(msg)) {
                    return msg;
                }
            }
            Thread.sleep(40);
        }
        throw new AssertionError("Timeout attesa messaggio WS. Ultimi: " + lastMessages(5));
    }

    public Map<String, Object> waitForConnettiOk(long timeoutMs) throws Exception {
        return waitFor(msg -> Boolean.TRUE.equals(msg.get("connettiOk")), timeoutMs);
    }

    public Map<String, Object> waitForOffertaVincente(String nome, int offerta, long timeoutMs) throws Exception {
        return waitFor(msg -> {
            Object ov = msg.get("offertaVincente");
            if (!(ov instanceof Map)) {
                return false;
            }
            Map<?, ?> map = (Map<?, ?>) ov;
            return nome.equals(map.get("nomegiocatore")) && offerta == toInt(map.get("offerta"));
        }, timeoutMs);
    }

    public List<String> getRawMessages() {
        return List.copyOf(rawMessages);
    }

    public void clearMessages() {
        rawMessages.clear();
    }

    private void ensureOpen() {
        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("WebSocket chiuso");
        }
    }

    private static Map<String, Object> parse(String raw) throws Exception {
        return MAPPER.readValue(raw, new TypeReference<Map<String, Object>>() {});
    }

    private static int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private List<String> lastMessages(int n) {
        List<String> all = rawMessages;
        int from = Math.max(0, all.size() - n);
        return all.subList(from, all.size());
    }

    @Override
    public void close() throws Exception {
        if (session != null && session.isOpen()) {
            session.close(CloseStatus.NORMAL);
        }
    }
}
