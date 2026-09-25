package com.daniele.asta;

import com.daniele.asta.service.AstaBroadcastService;
import com.daniele.asta.service.AstaDataService;
import com.daniele.asta.service.AstaMessageLogService;
import com.daniele.asta.service.AstaTurnoService;
import com.daniele.asta.session.AstaSessionBinding;
import com.daniele.asta.session.AstaSessionRegistry;
import com.daniele.fantalive.entity.Allenatori;
import com.daniele.fantalive.entity.Configurazione;
import com.daniele.fantalive.entity.EnumCategoria;
import com.daniele.fantalive.entity.Giocatori;
import com.daniele.fantalive.repository.GiocatoriRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SocketHandler extends TextWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SocketHandler.class);
    private static final long HANDLE_SLOW_MS = 200;

    private List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    Map<String, Object> offertaVincente = new HashMap<>();
    Calendar calInizioOfferta;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ssZ");
    String selCalciatoreMacroRuolo = "";
    String idCalciatore;
    String timeOut = "N";
    String nomeCalciatore;
    Long millisFromPausa = 0l;
    String giocatoreTimeout;
    String sSemaforoAttivo;
    private Integer tokenVerifica = -1;
    /** Incrementato ad ogni cambio stato asta/offerta; i client ignorano broadcast obsoleti. */
    private long astaEpoch = 0;

    @Autowired
    MyControllerAsta myController;
    @Autowired
    GiocatoriRepository giocatoriRepository;
    @Autowired
    AstaSessionRegistry sessionRegistry;
    @Autowired
    AstaMessageLogService messageLogService;
    @Autowired
    AstaBroadcastService broadcastService;
    @Autowired
    AstaDataService astaDataService;
    @Autowired
    AstaTurnoService astaTurnoService;
    @Autowired
    HttpSessionConfig httpSessionConfig;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {
        long handleStart = System.nanoTime();
        String operazione = null;
        String indirizzo = null;
        try {
            indirizzo = session.getRemoteAddress().toString();
            HttpSession httpSession = (HttpSession) session.getAttributes().get("HTTPSESSIONID");
            String payload = message.getPayload();
            Map<String, Object> jsonToMap = jsonToMap(payload);
            operazione = (String) jsonToMap.get("operazione");

            if (!"ping".equals(operazione)) {
                log.info("[WS] recv op={} session={} remote={} httpSession={} payloadBytes={}",
                        operazione, session.getId(), indirizzo, httpSession != null, payload.length());
            } else if (log.isDebugEnabled()) {
                log.debug("[WS] recv ping session={} remote={}", session.getId(), indirizzo);
            }

            synchronized (this) {

            if (operazione != null && needsAuth(operazione)
                    && !requireBoundOperator(session, jsonToMap, operazione)) {
                return;
            }

            if (operazione != null && operazione.equals("cancellaUtente")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                String operator = sessionRegistry.getUserForWsSession(session);
                Integer iIdgiocatore = Integer.parseInt(jsonToMap.get("idgiocatore").toString());
                if (operator != null && !operator.equals(nomegiocatore) && !isAllenatoreAdmin(operator)) {
                    log.warn("[WS] cancellaUtente rifiutata: {} non admin, target={}", operator, nomegiocatore);
                    Map<String, Object> err = new HashMap<>();
                    err.put("erroreOperazione", "Solo l'admin può disconnettere altri utenti.");
                    sessionRegistry.sendToSession(session, toJson(err));
                    return;
                }
                AstaSessionBinding targetBinding = sessionRegistry.getBinding(nomegiocatore);
                if (targetBinding != null) {
                    sessionRegistry.closeSession(targetBinding.getSession(), targetBinding.getTokenUtente(), false);
                }
                sessionRegistry.unbindUser(nomegiocatore);
                Map<String, Object> m = new HashMap<>();
                m.put("utenti", getUtentiLoggati());
                messageLogService.creaMessaggio(indirizzo, "Utente cancellato: " + nomegiocatore, EnumCategoria.Alert);
                m.put("azzera", String.valueOf(iIdgiocatore));
                m.put("messaggi", messageLogService.getMessaggiForBroadcast());
                inviaEvento(m);
            }
            if (operazione != null && operazione.equals("azzera")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                messageLogService.creaMessaggio(indirizzo, "AZZERATO DA: " + nomegiocatore, EnumCategoria.Alert);
                resetStatoAstaInIdle();
                messageLogService.clearMessaggi();
                sessionRegistry.clearAll();
                Map<String, Object> m = buildIdleBroadcastPayload();
                m.put("calciatori", astaDataService.getGiocatoriLiberi());
                m.put("utenti", getUtentiLoggati());
                m.put("messaggi", messageLogService.getMessaggiForBroadcast());
                m.put("azzera", "x");
                inviaEvento(m);
            }
            if (operazione != null && operazione.equals("connetti")) {
//			messaggi = new ArrayList<>();
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                String idgiocatore = jsonToMap.get("idgiocatore").toString();
                Long tokenUtente = (Long) jsonToMap.get("tokenUtente");
                Map<String, Object> m = new HashMap<>();
                sessionRegistry.clearExpiredAndRejoin(nomegiocatore);
                if (httpSession == null) {
                    log.warn("[WS] connetti rifiutato: sessione HTTP assente session={}", session.getId());
                    Map<String, Object> err = new HashMap<>();
                    err.put("erroreConnetti", "Sessione HTTP non disponibile. Ricarica la pagina.");
                    sessionRegistry.sendToSession(session, toJson(err));
                    return;
                }
                httpSession.setAttribute("nomeGiocatoreLoggato", nomegiocatore);
                httpSession.setAttribute("idLoggato", idgiocatore);
                String httpSessionId = httpSession.getId();
                final String remoteAddr = indirizzo;
                sessionRegistry.bind(session, nomegiocatore, idgiocatore, tokenUtente, httpSessionId,
                        msg -> messageLogService.creaMessaggio(remoteAddr, msg, EnumCategoria.Alert));
                sessionRegistry.closeUnboundDuplicateHttpSessions(getSessions(), session);
                m.putAll(astaDataService.buildConnettiPayload(idgiocatore));
                m.put("utenti", getUtentiLoggati());
                messageLogService.creaMessaggio(indirizzo, "Connesso: " + nomegiocatore, EnumCategoria.Connessione);
                m.put("messaggi", messageLogService.getMessaggiForBroadcast());
                m.put("connettiOk", true);
                m.put("broadcastTipo", "evento");
                sessionRegistry.sendToSession(session, toJson(m));
                Map<String, Object> presence = new HashMap<>();
                presence.put("broadcastTipo", "evento");
                presence.put("utenti", getUtentiLoggati());
                presence.put("messaggi", messageLogService.getMessaggiForBroadcast());
                inviaBroadcastExcept(session, toJson(presence));
            } else if (operazione != null && operazione.equals("forzaTurno")) {
                String turno = jsonToMap.get("turno").toString();
                Iterable<Allenatori> allAllenatori = myController.getAllAllenatori();
                for (Allenatori allenatori : allAllenatori) {
                    if (allenatori.getOrdine() == Integer.parseInt(turno)) {
                        myController.setNomeGiocatoreTurno(allenatori.getNome());
                    }
                }
                myController.setTurno(turno);
                Map<String, Object> m = new HashMap<>();
                m.put("turno", myController.getTurno());
                m.put("nomeGiocatoreTurno", myController.getNomeGiocatoreTurno());
                inviaEvento(m);
            } else if (operazione != null && operazione.equals("azzeraTempo")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                Map<String, Object> m = new HashMap<>();
                if (timeOut.equalsIgnoreCase("S")) {
                    millisFromPausa = 0l;
                } else {
                    calInizioOfferta = Calendar.getInstance();
                    bumpAstaEpoch();
                }
                messageLogService.creaMessaggio(indirizzo,
                        "Tempo azzerato da " + nomegiocatore + " per " + offertaVincente.get("nomeCalciatore") + "("
                                + ((Giocatori) offertaVincente.get("giocatore")).getRuolo() + ") "
                                + ((Giocatori) offertaVincente.get("giocatore")).getSquadra(),
                        EnumCategoria.Asta);
                m.put("faseAsta", "BIDDING");
                m.put("contaTempo", 0L);
                m.put("timeStart", -1);
                putAstaEpoch(m);
                m.put("timeout", "N");
                m.put("millisFromPausa", Long.toString(millisFromPausa));
                m.put("messaggi", messageLogService.getMessaggiForBroadcast());
                inviaEvento(m);
            } else if (operazione != null && operazione.equals("confermaAsta")) {
                confirmAstaAndBroadcast(indirizzo);
            } else if (operazione != null && operazione.equals("annullaAsta")) {
                annullaAstaAndBroadcast(indirizzo);
            } else if (operazione != null && operazione.equals("resumeAsta")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                Calendar now = Calendar.getInstance();
                calInizioOfferta.setTimeInMillis(now.getTimeInMillis() - millisFromPausa);
                Map<String, Object> m = new HashMap<>();
                messageLogService.creaMessaggio(indirizzo, "Offerta tolta dalla pausa da " + nomegiocatore + " per "
                                + offertaVincente.get("nomeCalciatore") + ". Riparte dopo " + millisFromPausa + " millisecondi",
                        EnumCategoria.Asta);
                timeOut = "N";
                m.put("faseAsta", "BIDDING");
                m.put("timeout", timeOut);
                m.put("contaTempo", now.getTimeInMillis() - calInizioOfferta.getTimeInMillis());
                m.put("messaggi", messageLogService.getMessaggiForBroadcast());
                inviaEvento(m);
            } else if (operazione != null && operazione.equals("pausaAsta")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                Calendar now = Calendar.getInstance();
                millisFromPausa = now.getTimeInMillis() - calInizioOfferta.getTimeInMillis();
                calInizioOfferta.set(Calendar.YEAR, 2971);
                Map<String, Object> m = new HashMap<>();
                messageLogService.creaMessaggio(
                        indirizzo, "Offerta messa in pausa da da " + nomegiocatore + " per "
                                + offertaVincente.get("nomeCalciatore") + " dopo " + millisFromPausa + " millisecondi",
                        EnumCategoria.Asta);
                timeOut = "S";
                m.put("faseAsta", "PAUSA");
                m.put("millisFromPausa", Long.toString(millisFromPausa));
                giocatoreTimeout = nomegiocatore;
                m.put("giocatoreTimeout", giocatoreTimeout);
                m.put("timeout", timeOut);
                m.put("messaggi", messageLogService.getMessaggiForBroadcast());
                inviaEvento(m);
            } else if (operazione != null && operazione.equals("terminaAsta")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                calInizioOfferta.set(Calendar.YEAR, 1971);
                timeOut = "N";
                Map<String, Object> m = new HashMap<>();
                messageLogService.creaMessaggio(indirizzo, "Offerta terminata in anticipo da " + nomegiocatore + " per "
                        + offertaVincente.get("nomeCalciatore"), EnumCategoria.Asta);
                bumpAstaEpoch();
                m.put("faseAsta", "DA_CONFERMARE");
                m.put("timeStart", 3);
                m.put("offertaVincente", offertaVincente);
                putAstaEpoch(m);
                m.put("messaggi", messageLogService.getMessaggiForBroadcast());
                inviaEvento(m);
            } else if (operazione != null && operazione.equals("liberaSemaforo")) {
                String operator = sessionRegistry.getUserForWsSession(session);
                if (!isAllenatoreAdmin(operator)) {
                    log.warn("[WS] liberaSemaforo rifiutata: {} non admin", operator);
                    Map<String, Object> err = new HashMap<>();
                    err.put("erroreOperazione", "Solo l'admin può sbloccare il semaforo.");
                    sessionRegistry.sendToSession(session, toJson(err));
                    return;
                }
                sSemaforoAttivo = "S";
                Map<String, Object> m = new HashMap<>();
                m.put("sSemaforoAttivo", sSemaforoAttivo);
                m.put("faseAsta", resolveFaseAsta(Calendar.getInstance()));
                inviaEvento(m);
            } else if (operazione != null && operazione.equals("start")) {
                selCalciatoreMacroRuolo = (String) jsonToMap.get("selCalciatoreMacroRuolo");
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                if (!requireTargetSelfOrAdmin(session, nomegiocatore, operazione)) {
                    return;
                }
                String idgiocatore = jsonToMap.get("idgiocatore").toString();
                String nomegiocatoreOperaCome = (String) jsonToMap.get("nomegiocatoreOperaCome");
                if (Boolean.TRUE.equals(myController.getIsATurni())) {
                    String turnoAttuale = myController.getNomeGiocatoreTurno();
                    String operator = sessionRegistry.getUserForWsSession(session);
                    if (turnoAttuale == null || turnoAttuale.isEmpty()) {
                        Map<String, Object> err = new HashMap<>();
                        err.put("erroreOperazione", "Turno asta non definito.");
                        sessionRegistry.sendToSession(session, toJson(err));
                        return;
                    }
                    if (!turnoAttuale.equals(nomegiocatore)) {
                        Map<String, Object> err = new HashMap<>();
                        err.put("erroreOperazione", "Puoi avviare l'asta solo per " + turnoAttuale + " (turno corrente).");
                        sessionRegistry.sendToSession(session, toJson(err));
                        return;
                    }
                    if (!turnoAttuale.equals(operator) && !isAllenatoreAdmin(operator)) {
                        Map<String, Object> err = new HashMap<>();
                        err.put("erroreOperazione", "Solo " + turnoAttuale + " o l'admin possono avviare l'asta in questo turno.");
                        sessionRegistry.sendToSession(session, toJson(err));
                        return;
                    }
                }
                String selCalciatore = (String) jsonToMap.get("selCalciatore");
                String[] split = selCalciatore.split("@");
                idCalciatore = split[0];
                nomeCalciatore = split[1];
                sSemaforoAttivo = "N";
                timeOut = "N";
                giocatoreTimeout = null;
                millisFromPausa = 0L;
                calInizioOfferta = Calendar.getInstance();
                offertaVincente = new HashMap<>();
                offertaVincente.put("giocatore", giocatoriRepository.findById(Integer.parseInt(idCalciatore)).get());
                offertaVincente.put("nomegiocatore", nomegiocatore);
                offertaVincente.put("idgiocatore", idgiocatore);
                offertaVincente.put("offerta", 1);
                offertaVincente.put("nomeCalciatore", nomeCalciatore);
                offertaVincente.put("idCalciatore", idCalciatore);
                bumpAstaEpoch();

                Map<String, Object> m = new HashMap<>();
                m.put("faseAsta", "BIDDING");
                m.put("avviaAsta", "S");
                m.put("offertaVincente", offertaVincente);
                putAstaEpoch(m);
                m.put("selCalciatoreMacroRuolo", selCalciatoreMacroRuolo);
                m.put("sSemaforoAttivo", sSemaforoAttivo);
                m.put("timeStart", -1);
                m.put("contaTempo", 0);
                m.put("mapSpesoTotale", myController.getMapSpesoTotale());
                String str = "Asta avviata da " + nomegiocatore + " per " + offertaVincente.get("nomeCalciatore") + "("
                        + ((Giocatori) offertaVincente.get("giocatore")).getRuolo() + ") "
                        + ((Giocatori) offertaVincente.get("giocatore")).getSquadra();
                if (!nomegiocatoreOperaCome.equalsIgnoreCase(nomegiocatore)) {
                    str = str + "(" + nomegiocatoreOperaCome + ")";
                }
                m.put("loggerMessaggi", myController.elencoLoggerMessaggi());
                messageLogService.clearMessaggi();
                messageLogService.creaMessaggio(indirizzo, str, EnumCategoria.Asta);
                inviaEvento(m);
            } else if (operazione != null && operazione.equals("disconnetti")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                String idgiocatore = jsonToMap.get("idgiocatore").toString();
                sessionRegistry.unbindUser(nomegiocatore);
                if (httpSession != null) {
                    httpSession.removeAttribute("nomeGiocatoreLoggato");
                    httpSession.removeAttribute("idLoggato");
                }
                Map<String, Object> m = new HashMap<>();
                messageLogService.creaMessaggio(indirizzo, "Utente disconnesso: " + nomegiocatore, EnumCategoria.Connessione);
                m.put("calciatori", astaDataService.getGiocatoriLiberi());
                m.put("utenti", getUtentiLoggati());
                inviaEvento(m);
            } else if (operazione != null && operazione.equals("inviaOfferta")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                if (!requireTargetSelfOrAdmin(session, nomegiocatore, operazione)) {
                    return;
                }
                boolean azzera = false;
                if (jsonToMap.get("azzera") != null) azzera = (Boolean) jsonToMap.get("azzera");
                String idgiocatore = jsonToMap.get("idgiocatore").toString();
                String nomegiocatoreOperaCome = (String) jsonToMap.get("nomegiocatoreOperaCome");
                Integer offerta = toInt(jsonToMap.get("offerta"));
                Integer maxRilancio = toInt(jsonToMap.get("maxRilancio"));
                Integer attOfferta = toInt(offertaVincente.get("offerta"));
                Calendar now = Calendar.getInstance();
                Calendar scadenzaAsta = Calendar.getInstance();
                scadenzaAsta.setTimeInMillis(calInizioOfferta.getTimeInMillis());
                scadenzaAsta.add(Calendar.SECOND, myController.getDurataAsta());
                Map<String, Object> m = new HashMap<>();
                boolean autoRilancio = false;
                if (!azzera && nomegiocatore.equals(offertaVincente.get("nomegiocatore").toString())) {
                    String str = "Autorilancio di " + nomegiocatore + " annullato. ";
                    if (!nomegiocatoreOperaCome.equalsIgnoreCase(nomegiocatore)) {
                        str = str + "(" + nomegiocatoreOperaCome + ")";
                    }
                    messageLogService.creaMessaggio(indirizzo, str, EnumCategoria.Asta);
                    autoRilancio = true;
                }
                if (!autoRilancio) {
                    if (offerta > maxRilancio) {
                        String str = "Rilancio da " + offerta + " di " + nomegiocatore + " per "
                                + offertaVincente.get("nomeCalciatore") + "("
                                + ((Giocatori) offertaVincente.get("giocatore")).getRuolo() + ") "
                                + ((Giocatori) offertaVincente.get("giocatore")).getSquadra() + " abbassato a " + maxRilancio
                                + " perchè oltre il massimo rilancio.";
                        if (!nomegiocatoreOperaCome.equalsIgnoreCase(nomegiocatore)) {
                            str = str + "(" + nomegiocatoreOperaCome + ")";
                        }
                        messageLogService.creaMessaggio(indirizzo, str, EnumCategoria.Asta);
                        offerta = maxRilancio;
                    }
                    if (now.after(scadenzaAsta)) {
                        String str = "Rilancio di " + nomegiocatore + " per " + offertaVincente.get("nomeCalciatore") + "("
                                + ((Giocatori) offertaVincente.get("giocatore")).getRuolo() + ") "
                                + ((Giocatori) offertaVincente.get("giocatore")).getSquadra() + " arrivato dopo : "
                                + (now.getTimeInMillis() - scadenzaAsta.getTimeInMillis()) + "millisecondi da scadenza asta";
                        if (!nomegiocatoreOperaCome.equalsIgnoreCase(nomegiocatore)) {
                            str = str + "(" + nomegiocatoreOperaCome + ")";
                        }
                        messageLogService.creaMessaggio(indirizzo, str, EnumCategoria.Asta);
                    } else {
                        String str = "Rilancio di " + offerta + " fatto da " + nomegiocatore;
                        if (!nomegiocatoreOperaCome.equalsIgnoreCase(nomegiocatore)) {
                            str = str + "(" + nomegiocatoreOperaCome + ")";
                        }
                        str = str + " per " + offertaVincente.get("nomeCalciatore") + "("
                                + ((Giocatori) offertaVincente.get("giocatore")).getRuolo() + ") "
                                + ((Giocatori) offertaVincente.get("giocatore")).getSquadra();
                        if (attOfferta != null && offerta <= attOfferta && azzera == false) {
                            messageLogService.creaMessaggio(indirizzo, str + " non superiore all'offerta vincente di " + attOfferta + " fatta da "
                                    + offertaVincente.get("nomegiocatore"), EnumCategoria.Asta);
                        } else {
                            calInizioOfferta = Calendar.getInstance();
                            offertaVincente.put("nomegiocatore", nomegiocatore);
                            offertaVincente.put("idgiocatore", idgiocatore);
                            offertaVincente.put("offerta", offerta);
                            bumpAstaEpoch();
                            log.info("[WS] offerta vincente {}={} (id={}) epoch={}",
                                    nomegiocatore, offerta, idgiocatore, astaEpoch);
                            m.put("faseAsta", "BIDDING");
                            m.put("contaTempo", 0);
                            m.put("timeStart", -1);
                            m.put("offertaVincente", offertaVincente);
                            m.put("selCalciatoreMacroRuolo", selCalciatoreMacroRuolo);
                            putAstaEpoch(m);
                            messageLogService.creaMessaggio(indirizzo, str, EnumCategoria.Asta);
                        }
                    }
                }
                inviaEvento(m);
            } else if (operazione != null && operazione.equals("forza")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                String idgiocatore = jsonToMap.get("idgiocatore").toString();
                String forzaAllenatore = (String) jsonToMap.get("forzaAllenatore");
                Integer forzaOfferta = toInt(jsonToMap.get("forzaOfferta"));
                String nomeForzaAllenatore = "";
                Iterable<Allenatori> allAllenatori = myController.getAllAllenatori();
                for (Allenatori allenatori : allAllenatori) {
                    if (allenatori.getId() == Integer.parseInt(forzaAllenatore)) {
                        nomeForzaAllenatore = allenatori.getNome();
                    }
                }
                Map<String, Object> m = new HashMap<>();
                offertaVincente.put("offerta", forzaOfferta);
                offertaVincente.put("nomegiocatore", nomeForzaAllenatore);
                offertaVincente.put("idgiocatore", forzaAllenatore);
                bumpAstaEpoch();

                Map<String, Object> offertaVincenteClone = new HashMap<>();
                for (String key : offertaVincente.keySet()) {
                    offertaVincenteClone.put(key, offertaVincente.get(key));
                }
                offertaVincenteClone.put("confermaForza", jsonToMap.get("conferma"));
                offertaVincenteClone.put("tokenCasuale", jsonToMap.get("tokenCasuale"));

                m.put("faseAsta", "DA_CONFERMARE");
                m.put("timeStart", 3);
                m.put("offertaVincente", offertaVincenteClone);
                putAstaEpoch(m);
                messageLogService.creaMessaggio(indirizzo, "Offerta forzata da " + nomegiocatore + " per "
                                + offertaVincente.get("nomeCalciatore") + ": " + nomeForzaAllenatore + " per " + forzaOfferta,
                        EnumCategoria.Asta);
                inviaEvento(m);
            } else if (operazione != null && operazione.equals("riapri")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                timeOut = "N";
                giocatoreTimeout = null;
                millisFromPausa = 0L;
                sSemaforoAttivo = "N";
                calInizioOfferta = Calendar.getInstance();
                bumpAstaEpoch();
                Map<String, Object> m = new HashMap<>();
                m.put("faseAsta", "BIDDING");
                m.put("sSemaforoAttivo", sSemaforoAttivo);
                m.put("timeStart", -1);
                m.put("contaTempo", 0);
                m.put("timeout", "N");
                m.put("messaggi", messageLogService.getMessaggiForBroadcast());
                m.put("offertaVincente", offertaVincente);
                putAstaEpoch(m);
                messageLogService.creaMessaggio(indirizzo, "Offerta riaperta da " + nomegiocatore + " per " + offertaVincente.get("nomeCalciatore"), EnumCategoria.Asta);
                inviaEvento(m);
            } else if (operazione != null && operazione.equals("verificaDispositiva")) {
                Integer tokenDispositiva = (Integer) jsonToMap.get("tokenDispositiva");
                String idgiocatore = jsonToMap.get("idgiocatore").toString();
                String idLoggato = (String) httpSession.getAttribute("idLoggato");
                if (idgiocatore.equalsIgnoreCase(idLoggato)) {
                    setTokenVerifica(tokenDispositiva);
                }

            } else if (operazione != null && operazione.equals("ping")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                if (nomegiocatore != null && !nomegiocatore.isEmpty()
                        && sessionRegistry.verifySessionUser(session, nomegiocatore)) {
                    sessionRegistry.recordPing(nomegiocatore);
                }
            } else {
                invia(payload);
            }

            } // synchronized (this)

        } catch (Exception e) {
            log.warn("[WS] errore op={} session={} remote={}: {}",
                    operazione, session.getId(), indirizzo, e.getMessage(), e);
        } finally {
            long handleMs = (System.nanoTime() - handleStart) / 1_000_000;
            if ("ping".equals(operazione)) {
                if (handleMs >= HANDLE_SLOW_MS && log.isDebugEnabled()) {
                    log.debug("[WS] ping lento ms={} session={}", handleMs, session.getId());
                }
            } else if (handleMs >= HANDLE_SLOW_MS) {
                log.warn("[WS] handle LENTO op={} ms={} session={} remote={}",
                        operazione, handleMs, session.getId(), indirizzo);
            } else if (operazione != null) {
                log.info("[WS] handle ok op={} ms={} session={}", operazione, handleMs, session.getId());
            }
        }
    }

    public void notificaInizializzaLega(String indirizzo) throws IOException {
        Map<String, Object> m = new HashMap<>();
        messageLogService.creaMessaggio(indirizzo, "Lega inizializzata", EnumCategoria.Alert);
        m.put("messaggi", messageLogService.getMessaggiForBroadcast());
        m.put("elencoAllenatori", myController.getAllAllenatori());
        inviaEvento(m);
    }

    public void verificaTokenDispositiva(String idgiocatore) {
        try {
            Map<String, Object> m = new HashMap<>();
            m.put("verificaDispositiva", idgiocatore);
            inviaEvento(m);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void notificaCancellaOfferta(Map<String, Object> mapOfferta, String indirizzo, String idgiocatore)
            throws IOException {
        Map<String, Object> m = new HashMap<>();
        messageLogService.creaMessaggio(indirizzo,
                "Offerta registrata CANCELLATA: " + mapOfferta.get("allenatore") + " per " + mapOfferta.get("giocatore")
                        + "(" + mapOfferta.get("ruolo") + ") " + mapOfferta.get("squadra") + " vinto a "
                        + mapOfferta.get("costo"),
                EnumCategoria.Alert);
        m.put("messaggi", messageLogService.getMessaggiForBroadcast());
        m.put("giocatoriPerSquadra", myController.giocatoriPerSquadra());
        m.put("mapSpesoTotale", myController.getMapSpesoTotale());
        m.put("calciatori", astaDataService.getGiocatoriLiberi());
        inviaEvento(m);
    }

    public void aggiornaConfigLega(Map<String, String> utentiRinominati, Iterable<Allenatori> allAllenatori,
                                   Configurazione configurazione, String indirizzo) throws IOException {

        Iterator<String> iterator = utentiRinominati.keySet().iterator();
        while (iterator.hasNext()) {
            String vecchioNome = (String) iterator.next();
            String nuovoNome = utentiRinominati.get(vecchioNome);
            sessionRegistry.renameUser(vecchioNome, nuovoNome);
            if (myController.getNomeGiocatoreTurno().equalsIgnoreCase(vecchioNome)) {
                myController.setNomeGiocatoreTurno(nuovoNome);
            }
        }
        Map<String, Object> m = new HashMap<>();
        messageLogService.creaMessaggio(indirizzo, "Aggiornata configurazione: " + configurazione, EnumCategoria.Alert);
        if (!utentiRinominati.isEmpty())
            messageLogService.creaMessaggio(indirizzo, "Utenti rinominati: " + utentiRinominati, EnumCategoria.Alert);
        if (myController.getIsATurni()) {
            m.put("isATurni", "S");
        } else {
            m.put("isATurni", "N");
        }
        if (myController.getIsSingle()) {
            m.put("isSingle", "S");
        } else {
            m.put("isSingle", "N");
        }
        if (myController.getIsMantra()) {
            m.put("isMantra", "S");
        } else {
            m.put("isMantra", "N");
        }
        m.put("numAcquisti", myController.getNumAcquisti());
        m.put("numMinAcquisti", myController.getNumMinAcquisti());
        m.put("maxP", myController.getMaxP());
        m.put("maxD", myController.getMaxD());
        m.put("maxC", myController.getMaxC());
        m.put("maxA", myController.getMaxA());
        m.put("minP", myController.getMinP());
        m.put("minD", myController.getMinD());
        m.put("minC", myController.getMinC());
        m.put("minA", myController.getMinA());
        m.put("budget", myController.getBudget());
        m.put("durataAsta", myController.getDurataAsta());
        m.put("messaggi", messageLogService.getMessaggiForBroadcast());
        m.put("utentiRinominati", utentiRinominati);
        m.put("elencoAllenatori", allAllenatori);
        inviaEvento(m);
    }

    public void notificaPreferiti(Map<Integer, List<Integer>> fav) throws IOException {
        Map<String, Object> m = new HashMap<>();
        m.put("preferiti", fav);
        inviaEvento(m);
    }

    public void visFmv() throws IOException {
        Map<String, Object> m = new HashMap<>();
        m.put("visFmv", "X");
        inviaEvento(m);
    }


    public void notificaCaricaFile(String indirizzo) throws IOException {
        Map<String, Object> m = new HashMap<>();
        messageLogService.creaMessaggio(indirizzo, "Giocatori caricati", EnumCategoria.Alert);
        m.put("calciatori", astaDataService.getGiocatoriLiberi());
        m.put("messaggi", messageLogService.getMessaggiForBroadcast());
        inviaEvento(m);
    }

    private boolean needsAuth(String operazione) {
        return !"connetti".equals(operazione)
                && !"ping".equals(operazione)
                && !"verificaDispositiva".equals(operazione);
    }

    private String resolveOperator(Map<String, Object> jsonToMap) {
        String operator = (String) jsonToMap.get("nomegiocatoreOperaCome");
        if (operator == null || operator.isEmpty()) {
            operator = (String) jsonToMap.get("nomegiocatore");
        }
        return operator;
    }

    private boolean isAllenatoreAdmin(String nomegiocatore) {
        if (nomegiocatore == null) {
            return false;
        }
        for (Allenatori allenatore : myController.getAllAllenatori()) {
            if (nomegiocatore.equals(allenatore.getNome()) && Boolean.TRUE.equals(allenatore.getIsAdmin())) {
                return true;
            }
        }
        return false;
    }

    private boolean requireTargetSelfOrAdmin(WebSocketSession session, String target, String operazione)
            throws IOException {
        String operator = sessionRegistry.getUserForWsSession(session);
        if (target == null || operator == null) {
            return true;
        }
        if (!target.equalsIgnoreCase(operator) && !isAllenatoreAdmin(operator)) {
            log.warn("[WS] {} rifiutata: {} non admin, target={}", operazione, operator, target);
            Map<String, Object> err = new HashMap<>();
            err.put("erroreOperazione", "Solo l'admin può operare per altri utenti.");
            sessionRegistry.sendToSession(session, toJson(err));
            return false;
        }
        return true;
    }

    private boolean requireBoundOperator(WebSocketSession session, Map<String, Object> jsonToMap,
            String operazione) throws IOException {
        String operator = resolveOperator(jsonToMap);
        if (operator == null || operator.isEmpty()) {
            operator = sessionRegistry.getUserForWsSession(session);
        }
        if (operator == null || !sessionRegistry.verifySessionUser(session, operator)) {
            log.warn("[WS] op {} rifiutata: utente {} non associato a session={}",
                    operazione, operator, session.getId());
            Map<String, Object> err = new HashMap<>();
            err.put("erroreOperazione", "Operazione non autorizzata per la sessione corrente.");
            sessionRegistry.sendToSession(session, toJson(err));
            return false;
        }
        return true;
    }

    private void inviaBroadcastExcept(WebSocketSession except, String payload) throws IOException {
        broadcastService.broadcastExcept(getSessions(), except, payload);
    }

    private void invia(String payload) throws IOException {
        broadcastService.broadcastAll(getSessions(), payload, () -> getUtentiLoggati().size());
    }

    public void disconnectAll() throws IOException {
        Map<String, Object> stop = new HashMap<>();
        stop.put("DISCONNECT_ALL", true);
        String payload = toJson(stop);
        List<WebSocketSession> toClose = new ArrayList<>(sessions);
        for (WebSocketSession session : toClose) {
            try {
                if (session.isOpen()) {
                    sessionRegistry.sendToSession(session, payload);
                }
            } catch (IOException e) {
                log.warn("[WS] disconnectAll notify failed session={}: {}", session.getId(), e.getMessage());
            }
        }
        clearHttpLoginSessions();
        sessionRegistry.clearAll();
        for (WebSocketSession session : toClose) {
            try {
                session.close(CloseStatus.NORMAL);
            } catch (IOException e) {
                log.warn("[WS] disconnectAll close failed session={}: {}", session.getId(), e.getMessage());
            }
        }
        sessions.clear();
        log.info("[WS] disconnectAll completato, sessioni chiuse={}", toClose.size());
    }

    private void clearHttpLoginSessions() {
        for (HttpSession hs : httpSessionConfig.getActiveSessions()) {
            try {
                hs.removeAttribute("nomeGiocatoreLoggato");
                hs.removeAttribute("idLoggato");
            } catch (IllegalStateException e) {
                log.debug("[WS] HTTP session già invalidata id={}", hs.getId());
            }
        }
    }

    @Scheduled(fixedRateString = "${frequenza.refresh}", initialDelay = 1000)
    private void aggiorna() throws IOException {
        Map<String, Object> m;
        synchronized (this) {
            sessionRegistry.evaluateExpiredUsers();
            if (!hasOffertaAttiva() && getSessions().isEmpty()) {
                return;
            }
            m = buildTimerBroadcastPayload();
        }
        invia(toJson(m));
    }

    private void inviaEvento(Map<String, Object> m) throws IOException {
        m.put("broadcastTipo", "evento");
        invia(toJson(m));
    }

    /** Tick scheduler: solo timer + presenza (S4.1/S4.2). */
    private Map<String, Object> buildTimerBroadcastPayload() {
        Map<String, Object> m = new HashMap<>();
        m.put("broadcastTipo", "timer");
        Calendar now = Calendar.getInstance();
        boolean astaAttiva = hasOffertaAttiva();
        boolean timerAttivo = isTimerAttivo();
        if (timerAttivo) {
            m.put("contaTempo", now.getTimeInMillis() - calInizioOfferta.getTimeInMillis());
            int phase = computeTimeStartPhase(now);
            m.put("timeStart", phase);
            m.put("faseAsta", phase >= 3 ? "DA_CONFERMARE" : "BIDDING");
        } else if (astaAttiva && "S".equals(timeOut)) {
            m.put("faseAsta", "PAUSA");
        }
        if (astaAttiva) {
            m.put("timeout", timeOut);
            m.put("giocatoreTimeout", giocatoreTimeout);
            m.put("millisFromPausa", Long.toString(millisFromPausa));
            putAstaEpoch(m);
        }
        m.put("utentiScaduti", sessionRegistry.getExpiredUsers());
        m.put("utenti", getUtentiLoggati());
        m.put("pingUtenti", sessionRegistry.getPingUtentiSnapshot());
        return m;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        HttpSession httpSession = (HttpSession) session.getAttributes().get("HTTPSESSIONID");
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
            log.info("[WS] cleanup sessione chiusa id={} remote={}",
                    webSocketSession.getId(), webSocketSession.getRemoteAddress());
        }
        sessionRegistry.closeUnboundDuplicateHttpSessions(getSessions(), session);
        log.info("[WS] connect session={} remote={} httpSession={} sessions={} utenti={}",
                session.getId(), session.getRemoteAddress(), httpSession != null,
                getSessions().size(), getUtentiLoggati().size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionRegistry.onWsClosed(session);
        boolean removed = getSessions().remove(session);
        log.info("[WS] disconnect session={} remote={} status={} removed={} sessions={} utenti={}",
                session.getId(), session.getRemoteAddress(), status, removed,
                getSessions().size(), getUtentiLoggati().size());
    }

    private ObjectMapper mapper = new ObjectMapper();

    private Map<String, Object> jsonToMap(String json) {
        try {
            return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String toJson(Object o) {
        if (o == null)
            return null;
        try {
            byte[] data = mapper.writeValueAsBytes(o);
            return new String(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Integer getTokenVerifica() {
        return tokenVerifica;
    }

    public void setTokenVerifica(Integer tokenVerifica) {
        this.tokenVerifica = tokenVerifica;
    }

    public List<String> getUtentiLoggati() {
        return sessionRegistry.getLoggedUserNames();
    }

    public void setUtentiLoggati(List<String> utentiLoggati) {
        sessionRegistry.clearAll();
    }

    public List<WebSocketSession> getSessions() {
        return sessions;
    }

    public void setSessions(List<WebSocketSession> sessions) {
        this.sessions = sessions;
    }

    public Map<String, Object> getOffertaVincente() {
        return offertaVincente;
    }

    public Map<String, Object> buildAstaSnapshot() {
        Map<String, Object> snap = new HashMap<>();
        Calendar now = Calendar.getInstance();
        String fase = resolveFaseAsta(now);
        snap.put("faseAsta", fase);
        if (sSemaforoAttivo != null) {
            snap.put("sSemaforoAttivo", sSemaforoAttivo);
        }
        snap.put("selCalciatoreMacroRuolo", selCalciatoreMacroRuolo);
        if (hasOffertaAttiva()) {
            snap.put("offertaVincente", offertaVincente);
            if (isTimerAttivo()) {
                snap.put("contaTempo", now.getTimeInMillis() - calInizioOfferta.getTimeInMillis());
                snap.put("timeStart", computeTimeStartPhase(now));
            }
            if ("S".equals(timeOut)) {
                snap.put("timeout", timeOut);
                snap.put("millisFromPausa", Long.toString(millisFromPausa));
                snap.put("giocatoreTimeout", giocatoreTimeout);
            }
        } else {
            snap.put("timeStart", -1);
        }
        snap.put("astaEpoch", astaEpoch);
        return snap;
    }

    public synchronized void confirmAstaAndBroadcast(String indirizzo) throws IOException {
        if (!hasOffertaAttiva()) {
            log.info("[WS] confermaAsta skip: nessuna offerta attiva (indirizzo={})", indirizzo);
            return;
        }
        sSemaforoAttivo = "S";
        messageLogService.clearMessaggi();
        messageLogService.creaMessaggio(indirizzo,
                "Asta confermata per " + offertaVincente.get("nomeCalciatore") + "("
                        + ((Giocatori) offertaVincente.get("giocatore")).getRuolo() + ") "
                        + ((Giocatori) offertaVincente.get("giocatore")).getSquadra() + ". Assegnato a "
                        + offertaVincente.get("nomegiocatore") + " per " + offertaVincente.get("offerta"),
                EnumCategoria.Asta);
        resetStatoAstaInIdle();
        Map<String, Object> m = buildIdleBroadcastPayload();
        m.putAll(astaDataService.buildPostConfirmPayload());
        m.put("messaggi", messageLogService.getMessaggiForBroadcast());
        astaTurnoService.avanzaTurnoDopoConferma(m);
        inviaEvento(m);
    }

    public synchronized void annullaAstaAndBroadcast(String indirizzo) throws IOException {
        if (!hasOffertaAttiva()) {
            log.info("[WS] annullaAsta skip: nessuna offerta attiva");
            return;
        }
        messageLogService.clearMessaggi();
        messageLogService.creaMessaggio(indirizzo, "Asta annullata per:" + offertaVincente.get("nomeCalciatore"), EnumCategoria.Asta);
        resetStatoAstaInIdle();
        Map<String, Object> m = buildIdleBroadcastPayload();
        m.put("messaggi", messageLogService.getMessaggiForBroadcast());
        inviaEvento(m);
    }

    private void resetStatoAstaInIdle() {
        offertaVincente = new HashMap<>();
        selCalciatoreMacroRuolo = "";
        timeOut = "N";
        giocatoreTimeout = null;
        millisFromPausa = 0L;
        calInizioOfferta = null;
        sSemaforoAttivo = "S";
        bumpAstaEpoch();
    }

    /** Reset stato asta in-memory (solo dev/test E2E). */
    public synchronized void resetForDevTest() {
        resetStatoAstaInIdle();
        messageLogService.clearMessaggi();
    }

    private Map<String, Object> buildIdleBroadcastPayload() {
        Map<String, Object> m = new HashMap<>();
        m.put("faseAsta", "IDLE");
        m.put("clearOfferta", "x");
        m.put("timeStart", -1);
        m.put("sSemaforoAttivo", sSemaforoAttivo);
        m.put("selCalciatoreMacroRuolo", selCalciatoreMacroRuolo);
        m.put("timeout", "N");
        putAstaEpoch(m);
        return m;
    }

    private static int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private void bumpAstaEpoch() {
        astaEpoch++;
    }

    private void putAstaEpoch(Map<String, Object> m) {
        if (m != null) {
            m.put("astaEpoch", astaEpoch);
        }
    }

    private boolean hasOffertaAttiva() {
        return offertaVincente != null && offertaVincente.get("nomegiocatore") != null;
    }

    private boolean isTimerAttivo() {
        if (calInizioOfferta == null || !hasOffertaAttiva() || "S".equals(timeOut)) {
            return false;
        }
        int year = calInizioOfferta.get(Calendar.YEAR);
        return year != 1971 && year != 2971;
    }

    private int computeTimeStartPhase(Calendar now) {
        if (calInizioOfferta == null || myController.getDurataAsta() <= 0) {
            return -1;
        }
        long l = (now.getTimeInMillis() - calInizioOfferta.getTimeInMillis()) / 1000;
        l = 100 * l / myController.getDurataAsta();
        if (l < 33) {
            return -1;
        }
        if (l < 66) {
            return 1;
        }
        if (l < 99) {
            return 2;
        }
        return 3;
    }

    private String resolveFaseAsta(Calendar now) {
        if (!hasOffertaAttiva()) {
            return "IDLE";
        }
        if ("S".equals(timeOut)) {
            return "PAUSA";
        }
        if (calInizioOfferta != null) {
            int year = calInizioOfferta.get(Calendar.YEAR);
            if (year == 2971) {
                return "PAUSA";
            }
            if (year == 1971) {
                return "DA_CONFERMARE";
            }
            if (isTimerAttivo() && computeTimeStartPhase(now) >= 3) {
                return "DA_CONFERMARE";
            }
        }
        return "BIDDING";
    }

}
