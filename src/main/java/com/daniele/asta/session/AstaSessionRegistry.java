package com.daniele.asta.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Component
public class AstaSessionRegistry {

    private static final Logger log = LoggerFactory.getLogger(AstaSessionRegistry.class);
    public static final long PING_EXPIRE_MS = 20_000L;
    private static final String ATTR_SUPERSEDED = "WS_SUPERSEDED";

    private final ConcurrentHashMap<String, AstaSessionBinding> byName = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> wsSessionIdToName = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<String> loginOrder = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<String> expiredUsers = new CopyOnWriteArrayList<>();

    public List<String> getLoggedUserNames() {
        pruneStaleBindings();
        List<String> active = new ArrayList<>();
        for (String nome : loginOrder) {
            AstaSessionBinding binding = byName.get(nome);
            if (binding != null && binding.getSession().isOpen()) {
                active.add(nome);
            }
        }
        return Collections.unmodifiableList(active);
    }

    public boolean isLoggedIn(String nomegiocatore) {
        if (nomegiocatore == null) {
            return false;
        }
        AstaSessionBinding binding = byName.get(nomegiocatore);
        return binding != null && binding.getSession().isOpen();
    }

    public void pruneStaleBindings() {
        for (String nome : new ArrayList<>(loginOrder)) {
            AstaSessionBinding binding = byName.get(nome);
            if (binding == null || !binding.getSession().isOpen()) {
                if (binding != null) {
                    wsSessionIdToName.remove(binding.getSession().getId());
                }
                byName.remove(nome);
                loginOrder.remove(nome);
                expiredUsers.remove(nome);
                log.info("[WS] registry prune stale user={} utenti={}", nome, loginOrder.size());
            }
        }
    }

    public AstaSessionBinding getBinding(String nomegiocatore) {
        return byName.get(nomegiocatore);
    }

    public String getUserForWsSession(WebSocketSession session) {
        if (session == null) {
            return null;
        }
        return wsSessionIdToName.get(session.getId());
    }

    public boolean verifySessionUser(WebSocketSession session, String nomegiocatore) {
        if (nomegiocatore == null || nomegiocatore.isEmpty() || session == null) {
            return false;
        }
        String bound = wsSessionIdToName.get(session.getId());
        return nomegiocatore.equals(bound);
    }

    public List<String> getExpiredUsers() {
        return Collections.unmodifiableList(new ArrayList<>(expiredUsers));
    }

    public Map<String, Map<String, Object>> getPingUtentiSnapshot() {
        Map<String, Map<String, Object>> snapshot = new HashMap<>();
        long now = System.currentTimeMillis();
        for (String nome : loginOrder) {
            AstaSessionBinding binding = byName.get(nome);
            if (binding == null) {
                continue;
            }
            Map<String, Object> mp = new HashMap<>();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(binding.getLastPingMillis());
            mp.put("lastPing", c);
            mp.put("checkPing", now - binding.getLastPingMillis());
            snapshot.put(nome, mp);
        }
        return snapshot;
    }

    public AstaSessionBinding bind(WebSocketSession session, String nomegiocatore, String idgiocatore,
            Long tokenUtente, String httpSessionId, Consumer<String> onStealMessage) {
        AstaSessionBinding previous = byName.get(nomegiocatore);
        if (previous != null && !previous.getSession().isOpen()) {
            removeBinding(previous.getSession(), false);
            previous = null;
        }
        if (previous != null && previous.getSession().isOpen()
                && !previous.getSession().getId().equals(session.getId())) {
            boolean sameHttpSession = httpSessionId != null
                    && httpSessionId.equals(previous.getHttpSessionId());
            if (!sameHttpSession && onStealMessage != null) {
                onStealMessage.accept("Sessione RUBATA da " + nomegiocatore);
            }
            closeSession(previous.getSession(), sameHttpSession ? null : previous.getTokenUtente(), true);
            removeBinding(previous.getSession(), false);
        }

        removeBinding(session, false);

        if (expiredUsers.contains(nomegiocatore)) {
            expiredUsers.remove(nomegiocatore);
        }

        AstaSessionBinding binding = new AstaSessionBinding(session, nomegiocatore, idgiocatore, tokenUtente,
                httpSessionId);
        byName.put(nomegiocatore, binding);
        wsSessionIdToName.put(session.getId(), nomegiocatore);
        loginOrder.remove(nomegiocatore);
        loginOrder.add(nomegiocatore);

        log.info("[WS] registry bind user={} wsSession={} httpSession={} utenti={}",
                nomegiocatore, session.getId(), httpSessionId, loginOrder.size());
        return binding;
    }

    public void unbindUser(String nomegiocatore) {
        AstaSessionBinding binding = byName.remove(nomegiocatore);
        if (binding != null) {
            wsSessionIdToName.remove(binding.getSession().getId());
            loginOrder.remove(nomegiocatore);
            expiredUsers.remove(nomegiocatore);
            log.info("[WS] registry unbind user={} utenti={}", nomegiocatore, loginOrder.size());
        }
    }

    public void onWsClosed(WebSocketSession session) {
        if (Boolean.TRUE.equals(session.getAttributes().get(ATTR_SUPERSEDED))) {
            wsSessionIdToName.remove(session.getId());
            return;
        }
        removeBinding(session, true);
    }

    public void closeUnboundDuplicateHttpSessions(java.util.List<WebSocketSession> allSessions,
            WebSocketSession keepSession) {
        closeOtherHttpSessionSockets(allSessions, keepSession, true);
    }

    public void closeOtherHttpSessionSockets(java.util.List<WebSocketSession> allSessions,
            WebSocketSession keepSession) {
        closeOtherHttpSessionSockets(allSessions, keepSession, false);
    }

    private void closeOtherHttpSessionSockets(java.util.List<WebSocketSession> allSessions,
            WebSocketSession keepSession, boolean onlyUnbound) {
        if (keepSession == null || allSessions == null) {
            return;
        }
        jakarta.servlet.http.HttpSession keepHttp = (jakarta.servlet.http.HttpSession) keepSession.getAttributes()
                .get("HTTPSESSIONID");
        if (keepHttp == null) {
            return;
        }
        String httpId = keepHttp.getId();
        for (WebSocketSession other : allSessions) {
            if (!other.isOpen() || other.getId().equals(keepSession.getId())) {
                continue;
            }
            jakarta.servlet.http.HttpSession otherHttp = (jakarta.servlet.http.HttpSession) other.getAttributes()
                    .get("HTTPSESSIONID");
            if (otherHttp == null || !httpId.equals(otherHttp.getId())) {
                continue;
            }
            String nome = getUserForWsSession(other);
            if (onlyUnbound && nome != null) {
                continue;
            }
            log.info("[WS] registry close duplicate httpSession={} oldWs={} newWs={} user={} onlyUnbound={}",
                    httpId, other.getId(), keepSession.getId(), nome, onlyUnbound);
            closeSession(other, null, true);
            removeBinding(other, false);
        }
    }

    public void recordPing(String nomegiocatore) {
        if (nomegiocatore == null || nomegiocatore.isEmpty()) {
            return;
        }
        AstaSessionBinding binding = byName.get(nomegiocatore);
        if (binding != null) {
            binding.touchPing();
            expiredUsers.remove(nomegiocatore);
        }
        evaluateExpiredUsers();
    }

    public void evaluateExpiredUsers() {
        long now = System.currentTimeMillis();
        for (String nome : loginOrder) {
            AstaSessionBinding binding = byName.get(nome);
            if (binding == null) {
                continue;
            }
            long checkPing = now - binding.getLastPingMillis();
            if (checkPing > PING_EXPIRE_MS) {
                if (!expiredUsers.contains(nome)) {
                    expiredUsers.add(nome);
                }
            }
        }
    }

    public void clearExpiredAndRejoin(String nomegiocatore) {
        if (nomegiocatore == null) {
            return;
        }
        if (expiredUsers.contains(nomegiocatore)) {
            unbindUser(nomegiocatore);
            expiredUsers.remove(nomegiocatore);
        }
    }

    public void renameUser(String vecchioNome, String nuovoNome) {
        if (vecchioNome == null || nuovoNome == null || vecchioNome.equals(nuovoNome)) {
            return;
        }
        if (expiredUsers.contains(vecchioNome)) {
            expiredUsers.remove(vecchioNome);
            expiredUsers.add(nuovoNome);
        }
        AstaSessionBinding binding = byName.remove(vecchioNome);
        if (binding != null) {
            byName.put(nuovoNome, binding);
            wsSessionIdToName.put(binding.getSession().getId(), nuovoNome);
        }
        int idx = loginOrder.indexOf(vecchioNome);
        if (idx >= 0) {
            loginOrder.set(idx, nuovoNome);
        }
    }

    public void clearAll() {
        byName.clear();
        wsSessionIdToName.clear();
        loginOrder.clear();
        expiredUsers.clear();
        log.info("[WS] registry cleared");
    }

    public void sendToSession(WebSocketSession session, String payload) throws IOException {
        if (session != null && session.isOpen()) {
            synchronized (session) {
                session.sendMessage(new org.springframework.web.socket.TextMessage(payload));
            }
        }
    }

    public void closeSession(WebSocketSession session, Long tokenUtente, boolean superseded) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            if (tokenUtente != null) {
                Map<String, Object> reset = new HashMap<>();
                reset.put("RESET_UTENTE", tokenUtente);
                sendToSession(session, new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(reset));
            }
        } catch (Exception e) {
            log.warn("[WS] registry RESET_UTENTE failed wsSession={}: {}", session.getId(), e.getMessage());
        }
        if (superseded) {
            session.getAttributes().put(ATTR_SUPERSEDED, true);
        }
        try {
            session.close(CloseStatus.NORMAL);
        } catch (IOException e) {
            log.warn("[WS] registry close failed wsSession={}: {}", session.getId(), e.getMessage());
        }
    }

    private void removeBinding(WebSocketSession session, boolean logUnbind) {
        String nome = wsSessionIdToName.remove(session.getId());
        if (nome == null) {
            return;
        }
        AstaSessionBinding binding = byName.get(nome);
        if (binding != null && binding.getSession().getId().equals(session.getId())) {
            byName.remove(nome);
            loginOrder.remove(nome);
            expiredUsers.remove(nome);
            if (logUnbind) {
                log.info("[WS] registry ws closed user={} wsSession={} utenti={}",
                        nome, session.getId(), loginOrder.size());
            }
        }
    }
}
