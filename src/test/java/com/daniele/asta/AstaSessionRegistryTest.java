package com.daniele.asta;

import com.daniele.asta.session.AstaSessionRegistry;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AstaSessionRegistryTest {

    private AstaSessionRegistry registry;

    @Before
    public void setUp() {
        registry = new AstaSessionRegistry();
    }

    @Test
    public void bindAndClose_removesUserFromOnlineList() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("ws-1");
        when(session.isOpen()).thenReturn(true);

        registry.bind(session, "GIOC0", "0", 1L, "http-1", msg -> {});
        assertTrue(registry.isLoggedIn("GIOC0"));
        assertEquals(1, registry.getLoggedUserNames().size());

        when(session.isOpen()).thenReturn(false);
        registry.onWsClosed(session);
        assertFalse(registry.isLoggedIn("GIOC0"));
        assertEquals(0, registry.getLoggedUserNames().size());
    }

    @Test
    public void rebindSameUser_replacesPreviousSession() throws Exception {
        WebSocketSession oldSession = mock(WebSocketSession.class);
        when(oldSession.getId()).thenReturn("ws-old");
        when(oldSession.isOpen()).thenReturn(true);

        WebSocketSession newSession = mock(WebSocketSession.class);
        when(newSession.getId()).thenReturn("ws-new");
        when(newSession.isOpen()).thenReturn(true);

        AtomicInteger steals = new AtomicInteger();
        registry.bind(oldSession, "GIOC0", "0", 1L, "http-1", msg -> steals.incrementAndGet());
        registry.bind(newSession, "GIOC0", "0", 2L, "http-1", msg -> steals.incrementAndGet());

        assertEquals("GIOC0", registry.getUserForWsSession(newSession));
        assertEquals(1, registry.getLoggedUserNames().size());
        assertTrue(steals.get() >= 1);
    }
}
