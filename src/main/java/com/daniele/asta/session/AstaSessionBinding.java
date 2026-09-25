package com.daniele.asta.session;

import org.springframework.web.socket.WebSocketSession;

public class AstaSessionBinding {

    private final WebSocketSession session;
    private final String nomegiocatore;
    private final String idgiocatore;
    private final Long tokenUtente;
    private final String httpSessionId;
    private volatile long lastPingMillis;

    public AstaSessionBinding(WebSocketSession session, String nomegiocatore, String idgiocatore,
            Long tokenUtente, String httpSessionId) {
        this.session = session;
        this.nomegiocatore = nomegiocatore;
        this.idgiocatore = idgiocatore;
        this.tokenUtente = tokenUtente;
        this.httpSessionId = httpSessionId;
        this.lastPingMillis = System.currentTimeMillis();
    }

    public WebSocketSession getSession() {
        return session;
    }

    public String getNomegiocatore() {
        return nomegiocatore;
    }

    public String getIdgiocatore() {
        return idgiocatore;
    }

    public Long getTokenUtente() {
        return tokenUtente;
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }

    public long getLastPingMillis() {
        return lastPingMillis;
    }

    public void touchPing() {
        lastPingMillis = System.currentTimeMillis();
    }

    public long getCheckPingMillis() {
        return System.currentTimeMillis() - lastPingMillis;
    }
}
