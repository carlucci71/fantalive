package com.daniele.fantalive;

import com.daniele.MainClass;
import com.daniele.fantalive.configurazione.SocketHandlerFantalive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainClass.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FantaLiveWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private SocketHandlerFantalive socketHandlerFantalive;

    @Test
    public void connectAndDisconnect_updatesActiveSessions() throws Exception {
        assertFalse(socketHandlerFantalive.hasActiveSessions());

        StandardWebSocketClient client = new StandardWebSocketClient();
        CountDownLatch open = new CountDownLatch(1);
        WebSocketSession session = client.doHandshake(new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession webSocketSession) {
                open.countDown();
            }
        }, "ws://localhost:" + port + "/fantalive/fantalive-websocket").get(10, TimeUnit.SECONDS);

        assertTrue(open.await(5, TimeUnit.SECONDS));
        assertTrue(socketHandlerFantalive.hasActiveSessions());
        assertEquals(1, socketHandlerFantalive.getSessions().size());

        session.close(CloseStatus.NORMAL);
        Thread.sleep(200);
        assertFalse(socketHandlerFantalive.hasActiveSessions());
    }

    @Test
    public void receivesScheduledTimeRefreshWhenConnected() throws Exception {
        StandardWebSocketClient client = new StandardWebSocketClient();
        CountDownLatch messageLatch = new CountDownLatch(1);
        WebSocketSession session = client.doHandshake(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) {
                if (message.getPayload().contains("timeRefresh")) {
                    messageLatch.countDown();
                }
            }
        }, "ws://localhost:" + port + "/fantalive/fantalive-websocket").get(10, TimeUnit.SECONDS);

        assertTrue(messageLatch.await(12, TimeUnit.SECONDS));
        session.close(CloseStatus.NORMAL);
    }
}
