package com.daniele.asta;

import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.daniele.fantalive.entity.Allenatori;
import com.daniele.fantalive.entity.Configurazione;
import com.daniele.fantalive.entity.EnumCategoria;
import com.daniele.fantalive.entity.Giocatori;
import com.daniele.fantalive.entity.LoggerMessaggi;
import com.daniele.fantalive.repository.GiocatoriRepository;
import com.daniele.fantalive.repository.LoggerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SocketHandler extends TextWebSocketHandler implements WebSocketHandler {

    private void creaMessaggio(String indirizzo, String messaggio, EnumCategoria categoria) {
        Long now = System.currentTimeMillis();
        UUID uuid = UUID.randomUUID();
        Map<String, Object> msg = new HashMap<>();
        msg.put("key", uuid.toString());
        msg.put("data", now);
        msg.put("testo", messaggio);
        msg.put("indirizzo", indirizzo);
        msg.put("categoria", categoria);
        messaggi.add(msg);
        LoggerMessaggi loggerMessaggi = new LoggerMessaggi();
        loggerMessaggi.setId(now);
        loggerMessaggi.setMessaggio(messaggio);
        loggerMessaggi.setCategoria(categoria.name());
        loggerMessaggi.setIndirizzo(indirizzo);
        loggerRepository.save(loggerMessaggi);
    }

    private List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private List<String> utentiLoggati = new ArrayList<>();
    List<String> utentiScaduti = new ArrayList<>();
    Map<String, Map<String, Object>> pingUtenti = new HashMap<>();
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

    @Autowired
    LoggerRepository loggerRepository;
    List<Map<String, Object>> messaggi = new ArrayList<>();
    @Autowired
    MyControllerAsta myController;
    @Autowired
    GiocatoriRepository giocatoriRepository;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {
        try {
            String indirizzo = session.getRemoteAddress().toString();
            HttpSession httpSession = (HttpSession) session.getAttributes().get("HTTPSESSIONID");
            String payload = message.getPayload();
            Map<String, Object> jsonToMap = jsonToMap(payload);
            String operazione = (String) jsonToMap.get("operazione");

            if (operazione != null && operazione.equals("ping")) {
//			System.out.println("Ping ricevuto" + payload);
            } else {
//			System.err.println("Messaggio ricevuto" + payload);
            }

            if (operazione != null && operazione.equals("cancellaUtente")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                Integer iIdgiocatore = Integer.parseInt(jsonToMap.get("idgiocatore").toString());
                getUtentiLoggati().remove(nomegiocatore);
                utentiScaduti.remove(nomegiocatore);
                pingUtenti.remove(nomegiocatore);
                Map<String, Object> m = new HashMap<>();
                m.put("utenti", getUtentiLoggati());
                creaMessaggio(indirizzo, "Utente cancellato: " + nomegiocatore, EnumCategoria.Alert);
                m.put("azzera", String.valueOf(iIdgiocatore));
                m.put("messaggi", messaggi);
                invia(toJson(m));
            }
            if (operazione != null && operazione.equals("azzera")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                String idgiocatore = jsonToMap.get("idgiocatore").toString();
                creaMessaggio(indirizzo, "AZZERATO DA: " + nomegiocatore, EnumCategoria.Alert);
                Map<String, Object> m = new HashMap<>();
                m.put("calciatori", myController.getGiocatoriLiberi());
                setUtentiLoggati(new ArrayList<>());
                utentiScaduti = new ArrayList<>();
                pingUtenti = new HashMap<>();
                m.put("utenti", getUtentiLoggati());
                m.put("azzera", "x");
                invia(toJson(m));
            }
            if (operazione != null && operazione.equals("connetti")) {
//			messaggi = new ArrayList<>();
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                String idgiocatore = jsonToMap.get("idgiocatore").toString();
                Long tokenUtente = (Long) jsonToMap.get("tokenUtente");
                Map<String, Object> m = new HashMap<>();
                if (utentiScaduti.contains(nomegiocatore)) {
                    getUtentiLoggati().remove(nomegiocatore);
                    utentiScaduti.remove(nomegiocatore);
                    pingUtenti.remove(nomegiocatore);
                }
                if (getUtentiLoggati() != null && getUtentiLoggati().contains(nomegiocatore)) {
                    creaMessaggio(indirizzo, "Sessione RUBATA da " + nomegiocatore, EnumCategoria.Alert);
                }
                httpSession.setAttribute("nomeGiocatoreLoggato", nomegiocatore);
                httpSession.setAttribute("idLoggato", idgiocatore);
                getUtentiLoggati().remove(nomegiocatore);
                getUtentiLoggati().add(nomegiocatore);
                m.put("calciatori", myController.getGiocatoriLiberi());
                myController.aggiornaFavoriti(idgiocatore);
                m.put("preferiti", myController.getFavoriti());
                m.put("cronologiaOfferte", myController.elencoCronologiaOfferte());
                m.put("utenti", getUtentiLoggati());
                creaMessaggio(indirizzo, "Connesso: " + nomegiocatore, EnumCategoria.Connessione);
                m.put("messaggi", messaggi);
                m.put("cronologiaOfferte", myController.elencoCronologiaOfferte());
                invia(toJson(m));
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
                invia(toJson(m));
            } else if (operazione != null && operazione.equals("azzeraTempo")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                Map<String, Object> m = new HashMap<>();
                if (timeOut.equalsIgnoreCase("S")) {
                    millisFromPausa = 0l;
                } else {
                    calInizioOfferta = Calendar.getInstance();
                }
                creaMessaggio(indirizzo,
                        "Tempo azzerato da " + nomegiocatore + " per " + offertaVincente.get("nomeCalciatore") + "("
                                + ((Giocatori) offertaVincente.get("giocatore")).getRuolo() + ") "
                                + ((Giocatori) offertaVincente.get("giocatore")).getSquadra(),
                        EnumCategoria.Asta);
                m.put("millisFromPausa", Long.toString(millisFromPausa));
                m.put("messaggi", messaggi);
                invia(toJson(m));
            } else if (operazione != null && operazione.equals("confermaAsta")) {
                sSemaforoAttivo = "S";
                messaggi = new ArrayList<>();
                creaMessaggio(indirizzo,
                        "Asta confermata per " + offertaVincente.get("nomeCalciatore") + "("
                                + ((Giocatori) offertaVincente.get("giocatore")).getRuolo() + ") "
                                + ((Giocatori) offertaVincente.get("giocatore")).getSquadra() + ". Assegnato a "
                                + offertaVincente.get("nomegiocatore") + " per " + offertaVincente.get("offerta"),
                        EnumCategoria.Asta);
                offertaVincente = new HashMap<>();
                selCalciatoreMacroRuolo = "";
                Map<String, Object> m = new HashMap<>();
                String idgiocatore = jsonToMap.get("idgiocatore").toString();
                m.put("calciatori", myController.getGiocatoriLiberi());
                m.put("cronologiaOfferte", myController.elencoCronologiaOfferte());
                m.put("clearOfferta", "x");
                m.put("messaggi", messaggi);
                Integer iTurno = Integer.parseInt(myController.getTurno());
                Iterable<Allenatori> allAllenatori = myController.getAllAllenatori();
                List<Map<String, Object>> riepilogoAllenatori = myController.riepilogoAllenatori();
                Configurazione configurazione = myController.getConfigurazione();
                boolean okTurno = false;
                int contaPassaggi=0;
                while (!okTurno) {
                    iTurno++;
                    String nomeFirst = null;
                    Integer conta = 0;
                    for (Allenatori allenatori : allAllenatori) {
                        if (nomeFirst == null) {
                            nomeFirst = allenatori.getNome();
                        }
                        if (allenatori.getOrdine() == iTurno) {
                            myController.setNomeGiocatoreTurno(allenatori.getNome());
                        }
                        conta++;
                    }
                    if (iTurno > conta - 1) {
                        iTurno = 0;
                        myController.setNomeGiocatoreTurno(nomeFirst);
                    }
                    List<Map<String, Object>> attAllenatore = riepilogoAllenatori.stream()
                            .filter((kk) -> kk.get("nome").equals(myController.getNomeGiocatoreTurno()))
                            .collect(Collectors.toList());

                    Map<String, Object> attP = attAllenatore.stream()
                            .filter((kk) -> kk.get("ruolo").equals("P"))
                            .findFirst()
                            .orElse(new HashMap<>());
                    Map<String, Object> attD = attAllenatore.stream()
                            .filter((kk) -> kk.get("ruolo").equals("D"))
                            .findFirst()
                            .orElse(new HashMap<>());
                    Map<String, Object> attC = attAllenatore.stream()
                            .filter((kk) -> kk.get("ruolo").equals("C"))
                            .findFirst()
                            .orElse(new HashMap<>());
                    Map<String, Object> attA = attAllenatore.stream()
                            .filter((kk) -> kk.get("ruolo").equals("A"))
                            .findFirst()
                            .orElse(new HashMap<>());

                    int contaP = 0;
                    if (attP.get("conta") != null) {
                        contaP=((BigInteger) attP.get("conta")).intValue();
                    }
                    int contaD = 0;
                    if (attD.get("conta") != null) {
                        contaD=((BigInteger) attD.get("conta")).intValue();
                    }
                    int contaC = 0;
                    if (attC.get("conta") != null) {
                        contaC=((BigInteger) attC.get("conta")).intValue();
                    }
                    int contaA = 0;
                    if (attA.get("conta") != null) {
                        contaA=((BigInteger) attA.get("conta")).intValue();
                    }

                    if (
                            contaP !=configurazione.getMaxP().intValue() ||
                            contaD !=configurazione.getMaxD().intValue() ||
                            contaC !=configurazione.getMaxC().intValue() ||
                            contaA !=configurazione.getMaxA().intValue()
                    ) {
                        okTurno = true;
                    }
                    contaPassaggi++;
                    if (configurazione.getNumeroGiocatori()<contaPassaggi){
                        okTurno=true;
                    }
                }
                myController.setTurno(Integer.toString(iTurno));
                m.put("turno", myController.getTurno());
                m.put("giocatoriPerSquadra", myController.giocatoriPerSquadra());
                m.put("mapSpesoTotale", myController.getMapSpesoTotale());
                m.put("nomeGiocatoreTurno", myController.getNomeGiocatoreTurno());
                invia(toJson(m));

            } else if (operazione != null && operazione.equals("annullaAsta")) {
                sSemaforoAttivo = "S";
                messaggi = new ArrayList<>();
                creaMessaggio(indirizzo, "Asta annullata per:" + offertaVincente.get("nomeCalciatore"), EnumCategoria.Asta);
                offertaVincente = new HashMap<>();
                selCalciatoreMacroRuolo = "";
                Map<String, Object> m = new HashMap<>();
                m.put("clearOfferta", "x");
                m.put("messaggi", messaggi);
                invia(toJson(m));
            } else if (operazione != null && operazione.equals("resumeAsta")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                Calendar now = Calendar.getInstance();
                calInizioOfferta.setTimeInMillis(now.getTimeInMillis() - millisFromPausa);
                Map<String, Object> m = new HashMap<>();
                creaMessaggio(indirizzo, "Offerta tolta dalla pausa da " + nomegiocatore + " per "
                                + offertaVincente.get("nomeCalciatore") + ". Riparte dopo " + millisFromPausa + " millisecondi",
                        EnumCategoria.Asta);
                timeOut = "N";
                m.put("timeout", timeOut);
                m.put("contaTempo", now.getTimeInMillis() - calInizioOfferta.getTimeInMillis());
                m.put("messaggi", messaggi);
                invia(toJson(m));
            } else if (operazione != null && operazione.equals("pausaAsta")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                Calendar now = Calendar.getInstance();
                millisFromPausa = now.getTimeInMillis() - calInizioOfferta.getTimeInMillis();
                calInizioOfferta.set(Calendar.YEAR, 2971);
                Map<String, Object> m = new HashMap<>();
                creaMessaggio(
                        indirizzo, "Offerta messa in pausa da da " + nomegiocatore + " per "
                                + offertaVincente.get("nomeCalciatore") + " dopo " + millisFromPausa + " millisecondi",
                        EnumCategoria.Asta);
                timeOut = "S";
                m.put("millisFromPausa", Long.toString(millisFromPausa));
                giocatoreTimeout = nomegiocatore;
                m.put("giocatoreTimeout", giocatoreTimeout);
                m.put("timeout", timeOut);
                m.put("messaggi", messaggi);
                invia(toJson(m));
            } else if (operazione != null && operazione.equals("terminaAsta")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                String idgiocatore = jsonToMap.get("idgiocatore").toString();
                calInizioOfferta.set(Calendar.YEAR, 1971);
                Map<String, Object> m = new HashMap<>();
                creaMessaggio(indirizzo, "Offerta terminata in anticipo da " + nomegiocatore + " per "
                        + offertaVincente.get("nomeCalciatore"), EnumCategoria.Asta);
                m.put("messaggi", messaggi);
                invia(toJson(m));
            } else if (operazione != null && operazione.equals("liberaSemaforo")) {
                sSemaforoAttivo = "S";
            } else if (operazione != null && operazione.equals("start")) {
                selCalciatoreMacroRuolo = (String) jsonToMap.get("selCalciatoreMacroRuolo");
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                String idgiocatore = jsonToMap.get("idgiocatore").toString();
                String nomegiocatoreOperaCome = (String) jsonToMap.get("nomegiocatoreOperaCome");
                String selCalciatore = (String) jsonToMap.get("selCalciatore");
                String[] split = selCalciatore.split("@");
                idCalciatore = split[0];
                nomeCalciatore = split[1];
                sSemaforoAttivo = "N";
                calInizioOfferta = Calendar.getInstance();
                offertaVincente = new HashMap<>();
                offertaVincente.put("giocatore", giocatoriRepository.findById(Integer.parseInt(idCalciatore)).get());
                offertaVincente.put("nomegiocatore", nomegiocatore);
                offertaVincente.put("idgiocatore", idgiocatore);
                offertaVincente.put("offerta", 1);
                offertaVincente.put("nomeCalciatore", nomeCalciatore);
                offertaVincente.put("idCalciatore", idCalciatore);

                Map<String, Object> m = new HashMap<>();
                m.put("avviaAsta", "S");
                m.put("offertaVincente", offertaVincente);
                String str = "Asta avviata da " + nomegiocatore + " per " + offertaVincente.get("nomeCalciatore") + "("
                        + ((Giocatori) offertaVincente.get("giocatore")).getRuolo() + ") "
                        + ((Giocatori) offertaVincente.get("giocatore")).getSquadra();
                if (!nomegiocatoreOperaCome.equalsIgnoreCase(nomegiocatore)) {
                    str = str + "(" + nomegiocatoreOperaCome + ")";
                }
                m.put("loggerMessaggi", myController.elencoLoggerMessaggi());
                messaggi = new ArrayList<>();
                creaMessaggio(indirizzo, str, EnumCategoria.Asta);
                invia(toJson(m));
            } else if (operazione != null && operazione.equals("disconnetti")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                String idgiocatore = jsonToMap.get("idgiocatore").toString();
                getUtentiLoggati().remove(nomegiocatore);
                utentiScaduti.remove(nomegiocatore);
                pingUtenti.remove(nomegiocatore);
                httpSession.removeAttribute("nomeGiocatoreLoggato");
                httpSession.removeAttribute("idLoggato");
                Map<String, Object> m = new HashMap<>();
                creaMessaggio(indirizzo, "Utente disconnesso: " + nomegiocatore, EnumCategoria.Connessione);
                m.put("calciatori", myController.getGiocatoriLiberi());
                m.put("utenti", getUtentiLoggati());
                invia(toJson(m));
            } else if (operazione != null && operazione.equals("inviaOfferta")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                Boolean azzera = null;
                if (jsonToMap.get("azzera") != null) azzera = (Boolean) jsonToMap.get("azzera");
                String idgiocatore = jsonToMap.get("idgiocatore").toString();
                String nomegiocatoreOperaCome = (String) jsonToMap.get("nomegiocatoreOperaCome");
                Integer offerta = (Integer) jsonToMap.get("offerta");
                Integer maxRilancio = (Integer) jsonToMap.get("maxRilancio");
                Integer attOfferta = (Integer) offertaVincente.get("offerta");
                Calendar now = Calendar.getInstance();
                Calendar scadenzaAsta = Calendar.getInstance();
                scadenzaAsta.setTimeInMillis(calInizioOfferta.getTimeInMillis());
                scadenzaAsta.add(Calendar.SECOND, myController.getDurataAsta());
                Map<String, Object> m = new HashMap<>();
//			Long maxRilancio = myController.getMapSpesoTotale().get(nomegiocatore).get("maxRilancio");
                if (offerta > maxRilancio) {
                    String str = "Rilancio da " + offerta + " di " + nomegiocatore + " per "
                            + offertaVincente.get("nomeCalciatore") + "("
                            + ((Giocatori) offertaVincente.get("giocatore")).getRuolo() + ") "
                            + ((Giocatori) offertaVincente.get("giocatore")).getSquadra() + " abbassato a " + maxRilancio
                            + " perchè oltre il massimo rilancio.";
                    if (!nomegiocatoreOperaCome.equalsIgnoreCase(nomegiocatore)) {
                        str = str + "(" + nomegiocatoreOperaCome + ")";
                    }
                    creaMessaggio(indirizzo, str, EnumCategoria.Asta);
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
                    creaMessaggio(indirizzo, str, EnumCategoria.Asta);
                } else {
                    String str = "Rilancio di " + offerta + " fatto da " + nomegiocatore;
                    if (!nomegiocatoreOperaCome.equalsIgnoreCase(nomegiocatore)) {
                        str = str + "(" + nomegiocatoreOperaCome + ")";
                    }
                    str = str + " per " + offertaVincente.get("nomeCalciatore") + "("
                            + ((Giocatori) offertaVincente.get("giocatore")).getRuolo() + ") "
                            + ((Giocatori) offertaVincente.get("giocatore")).getSquadra();
                    if (attOfferta != null && offerta <= attOfferta && azzera == null) {
                        creaMessaggio(indirizzo, str + " non superiore all'offerta vincente di " + attOfferta + " fatta da "
                                + offertaVincente.get("nomegiocatore"), EnumCategoria.Asta);
                    } else {
                        calInizioOfferta = Calendar.getInstance();
                        offertaVincente.put("nomegiocatore", nomegiocatore);
                        offertaVincente.put("idgiocatore", idgiocatore);
                        offertaVincente.put("offerta", offerta);
                        m.put("offertaVincente", offertaVincente);
                        m.put("selCalciatoreMacroRuolo", selCalciatoreMacroRuolo);
                        creaMessaggio(indirizzo, str, EnumCategoria.Asta);
                    }
                }
                invia(toJson(m));
            } else if (operazione != null && operazione.equals("forza")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                String idgiocatore = jsonToMap.get("idgiocatore").toString();
                String forzaAllenatore = (String) jsonToMap.get("forzaAllenatore");
                Integer forzaOfferta = (Integer) jsonToMap.get("forzaOfferta");
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

                Map<String, Object> offertaVincenteClone = new HashMap<>();
                for (String key : offertaVincente.keySet()) {
                    offertaVincenteClone.put(key, offertaVincente.get(key));
                }
                offertaVincenteClone.put("confermaForza", jsonToMap.get("conferma"));
                offertaVincenteClone.put("tokenCasuale", jsonToMap.get("tokenCasuale"));

                m.put("offertaVincente", offertaVincenteClone);
                creaMessaggio(indirizzo, "Offerta forzata da " + nomegiocatore + " per "
                                + offertaVincente.get("nomeCalciatore") + ": " + nomeForzaAllenatore + " per " + forzaOfferta,
                        EnumCategoria.Asta);
                invia(toJson(m));
            } else if (operazione != null && operazione.equals("verificaDispositiva")) {
                Integer tokenDispositiva = (Integer) jsonToMap.get("tokenDispositiva");
                String idgiocatore = jsonToMap.get("idgiocatore").toString();
                String idLoggato = (String) httpSession.getAttribute("idLoggato");
                if (idgiocatore.equalsIgnoreCase(idLoggato)) {
                    setTokenVerifica(tokenDispositiva);
                }

            } else if (operazione != null && operazione.equals("ping")) {
                String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
                utentiScaduti = new ArrayList<>();
                Calendar now = Calendar.getInstance();
                if (nomegiocatore != null) {
                    Map<String, Object> mp = new HashMap<>();
                    mp.put("lastPing", now);
                    mp.put("checkPing", 0);
                    pingUtenti.put(nomegiocatore, mp);
                }
                Map<String, Object> m = new HashMap<>();
                for (String utente : getUtentiLoggati()) {
                    Map<String, Object> map = pingUtenti.get(utente);
                    if (map != null) {
                        Calendar c = (Calendar) map.get("lastPing");
                        long checkPing = now.getTimeInMillis() - c.getTimeInMillis();
                        map.put("checkPing", checkPing);
                        if (checkPing > 20000) {
                            utentiScaduti.add(utente);
                        }
                    }
                }
                m.put("RICHIESTA", nomegiocatore);
                invia(toJson(m));
            } else {
                invia(payload);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void notificaInizializzaLega(String indirizzo) throws IOException {
        Map<String, Object> m = new HashMap<>();
        creaMessaggio(indirizzo, "Lega inizializzata", EnumCategoria.Alert);
        m.put("messaggi", messaggi);
        m.put("elencoAllenatori", myController.getAllAllenatori());
        invia(toJson(m));
    }

    public void verificaTokenDispositiva(String idgiocatore) {
        try {
            Map<String, Object> m = new HashMap<>();
            m.put("verificaDispositiva", idgiocatore);
            invia(toJson(m));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void notificaCancellaOfferta(Map<String, Object> mapOfferta, String indirizzo, String idgiocatore)
            throws IOException {
        Map<String, Object> m = new HashMap<>();
        creaMessaggio(indirizzo,
                "Offerta registrata CANCELLATA: " + mapOfferta.get("allenatore") + " per " + mapOfferta.get("giocatore")
                        + "(" + mapOfferta.get("ruolo") + ") " + mapOfferta.get("squadra") + " vinto a "
                        + mapOfferta.get("costo"),
                EnumCategoria.Alert);
        m.put("messaggi", messaggi);
        m.put("giocatoriPerSquadra", myController.giocatoriPerSquadra());
        m.put("mapSpesoTotale", myController.getMapSpesoTotale());
        m.put("calciatori", myController.getGiocatoriLiberi());
        invia(toJson(m));
    }

    public void aggiornaConfigLega(Map<String, String> utentiRinominati, Iterable<Allenatori> allAllenatori,
                                   Configurazione configurazione, String indirizzo) throws IOException {

        Iterator<String> iterator = utentiRinominati.keySet().iterator();
        while (iterator.hasNext()) {
            String vecchioNome = (String) iterator.next();
            String nuovoNome = utentiRinominati.get(vecchioNome);
            if (utentiScaduti.contains(vecchioNome)) {
                utentiScaduti.remove(vecchioNome);
                utentiScaduti.add(nuovoNome);
            }
            if (getUtentiLoggati().contains(vecchioNome)) {
                getUtentiLoggati().remove(vecchioNome);
                getUtentiLoggati().add(nuovoNome);
            }
            Map<String, Object> map = pingUtenti.get(vecchioNome);
            if (map != null) {
                pingUtenti.remove(vecchioNome);
                pingUtenti.put(nuovoNome, map);
            }
            if (myController.getNomeGiocatoreTurno().equalsIgnoreCase(vecchioNome)) {
                myController.setNomeGiocatoreTurno(nuovoNome);
            }
        }
        Map<String, Object> m = new HashMap<>();
        creaMessaggio(indirizzo, "Aggiornata configurazione: " + configurazione, EnumCategoria.Alert);
        if (!utentiRinominati.isEmpty())
            creaMessaggio(indirizzo, "Utenti rinominati: " + utentiRinominati, EnumCategoria.Alert);
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
        m.put("messaggi", messaggi);
        m.put("utentiRinominati", utentiRinominati);
        m.put("elencoAllenatori", allAllenatori);
        invia(toJson(m));
    }

    public void notificaPreferiti(Map<Integer, List<Integer>> fav) throws IOException {
        Map<String, Object> m = new HashMap<>();
        m.put("preferiti", fav);
        invia(toJson(m));
    }

    public void visFmv() throws IOException {
        Map<String, Object> m = new HashMap<>();
        m.put("visFmv", "X");
        invia(toJson(m));
    }


    public void notificaCaricaFile(String indirizzo) throws IOException {
        Map<String, Object> m = new HashMap<>();
        creaMessaggio(indirizzo, "Giocatori caricati", EnumCategoria.Alert);
        m.put("calciatori", myController.getGiocatoriLiberi());
        m.put("messaggi", messaggi);
        invia(toJson(m));
    }

    private void inviaOrig(String payload) throws IOException {
        for (WebSocketSession webSocketSession : getSessions()) {
            if (webSocketSession.isOpen()) {
                synchronized (webSocketSession) {
                    webSocketSession.sendMessage(new TextMessage(payload));
                }
            }
        }
    }

    private synchronized void invia(String payload) throws IOException {
        for (WebSocketSession webSocketSession : getSessions()) {
            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(new TextMessage(payload));
            }
        }
    }

    public void disconnectAll() {
        synchronized (sessions) {
            for (WebSocketSession session : sessions) {
                try {
                    session.close(CloseStatus.NORMAL);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            sessions.clear();
        }
    }

    @Scheduled(fixedRateString = "${frequenza.refresh}", initialDelay = 1000)
    private void aggiorna() throws IOException {
        Map<String, Object> m = new HashMap<>();
        Calendar now = Calendar.getInstance();
        if (calInizioOfferta != null)
            m.put("contaTempo", now.getTimeInMillis() - calInizioOfferta.getTimeInMillis());
        m.put("timeout", timeOut);
        m.put("utentiScaduti", utentiScaduti);
//		m.put("elencoAllenatori", myController.getAllAllenatori());
        m.put("utenti", getUtentiLoggati());
        m.put("durataAsta", myController.getDurataAsta());
        m.put("sSemaforoAttivo", sSemaforoAttivo);
        m.put("offertaVincente", offertaVincente);
        m.put("selCalciatoreMacroRuolo", selCalciatoreMacroRuolo);
        m.put("pingUtenti", pingUtenti);
        m.put("messaggi", messaggi);
        m.put("giocatoreTimeout", giocatoreTimeout);
        m.put("turno", myController.getTurno());
        m.put("nomeGiocatoreTurno", myController.getNomeGiocatoreTurno());
        m.put("millisFromPausa", Long.toString(millisFromPausa));
        long l = 0;
        int conta = 0;
        if (calInizioOfferta != null) {
            l = (now.getTimeInMillis() - calInizioOfferta.getTimeInMillis()) / 1000;
            l = 100 * l / myController.getDurataAsta();
            if (l < 33)
                conta = -1;
            else if (l < 66)
                conta = 1;
            else if (l < 99)
                conta = 2;
            else
                conta = 3;
        }
        m.put("timeStart", conta);
        invia(toJson(m));
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
        return utentiLoggati;
    }

    public void setUtentiLoggati(List<String> utentiLoggati) {
        this.utentiLoggati = utentiLoggati;
    }

    public List<WebSocketSession> getSessions() {
        return sessions;
    }

    public void setSessions(List<WebSocketSession> sessions) {
        this.sessions = sessions;
    }

}
