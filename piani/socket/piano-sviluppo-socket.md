# Piano di sviluppo — Fix WebSocket

> Piano operativo per risolvere i problemi di sincronizzazione socket/sessioni.  
> Basato su: [analisi-socket.md](./analisi-socket.md) · Collegato a: [roadmap-sistemazione.md](../roadmap-sistemazione.md)

---

## Obiettivo

Stabilizzare il canale real-time di **FantaAsta** eliminando blocchi, sessioni zombie e race condition, senza interrompere il funzionamento dell’asta in produzione.

**Scope:** FantaAsta (priorità 1) + allineamento minimo FantaLive (priorità 2).  
**Fuori scope:** sicurezza, refactoring `Main.java`, migrazione frontend.

---

## Fasi di sviluppo

### Fase S0 — Baseline e verifica (prerequisito)

| ID | Attività | Output | Roadmap |
|----|----------|--------|---------|
| S0.1 | Documentare scenari di test manuali (login, offerta, conferma, disconnect, reconnect) | Checklist test in `piani/socket/test-scenari.md` | #12 |
| S0.2 | Misurare comportamento attuale (latenza ping, freeze su N utenti) | Note baseline per confronto post-fix | — |
| S0.3 | Inventario operazioni WS (`connetti`, `ping`, `inviaOfferta`…) e payload | Matrice operazione → side-effect | — |

**Criterio uscita:** scenari critici documentati e riproducibili.

---

### Fase S1 — Quick win: sblocco send e lifecycle (1 sprint)

| ID | Attività | File principali | Roadmap |
|----|----------|-----------------|---------|
| S1.1 | Rimuovere `synchronized` globale da `invia()`, usare lock per-sessione | `SocketHandler.java` | #1 |
| S1.2 | Implementare `afterConnectionClosed`: rimuovi sessione + cleanup utente associato | `SocketHandler.java` | #3 |
| S1.3 | Introdurre registry `WebSocketSession → UserInfo` (nome, id, lastPing) | Nuovo servizio o inner in handler | #2 |
| S1.4 | Client: chiudere WS esistente prima di `new WebSocket()` | `fantaasta/app.js` | #3 |
| S1.5 | Client: reconnect con backoff su `onclose`/`readyState==3` | `fantaasta/app.js` | #3 |
| S1.6 | Rendere thread-safe `utentiLoggati`, `pingUtenti`, `utentiScaduti` | `SocketHandler.java` | #6 |

**Criterio uscita:**
- Nessun lock globale su send
- Chiusura tab/browser rimuove utente entro 1 ciclo ping
- Nessuna WS duplicata lato client dopo `ricaricaIndex`

**Rischio:** basso. Retrocompatibile con protocollo attuale.

---

### Fase S2 — Sessioni unificate (1 sprint)

| ID | Attività | File principali | Roadmap |
|----|----------|-----------------|---------|
| S2.1 | Creare `AstaSessionRegistry`: unica fonte di verità utente online | Nuovo package `asta/session` | #2 |
| S2.2 | Allineare `/init` HTTP: validazione contro registry, non solo `utentiLoggati` | `MyControllerAsta.java` | #2 |
| S2.3 | Session stealing: chiudere WS precedente dello stesso utente su nuovo `connetti` | `SocketHandler` + registry | #2 |
| S2.4 | Fix ping: non resettare `utentiScaduti` globalmente; calcolo per-utente atomico | `SocketHandler.java` | #6 |
| S2.5 | Null-safe su `httpSession`: rifiutare `connetti` se sessione HTTP assente | `SocketHandler.java` | #13 |
| S2.6 | Logging strutturato errori WS (sostituire `System.out.println`) | `SocketHandler.java` | #13 |

**Criterio uscita:**
- Un utente = una WS attiva
- `/init` e WS concordano sempre sullo stato login
- Nessuna NPE silenziosa su handshake senza HTTP session

**Rischio:** medio. Richiede test su login multi-dispositivo.

---

### Fase S2.7 — Connection manager client + policy server (implementata 2026-09-25)

| ID | Attività | File | Stato |
|----|----------|------|-------|
| S2.7.1 | `FantaWsConnection`: stati, coda send, reconnect unico | `ws-connection.js` | Fatto |
| S2.7.2 | Integrazione Angular: `syncSessionFromServer`, `applyInitData` | `app.js` | Fatto |
| S2.7.3 | Tab leader via `BroadcastChannel` (no fight index/admin) | `ws-connection.js` | Fatto |
| S2.7.4 | Server: `connettiOk` solo al client login; presence leggera agli altri | `SocketHandler.java` | Fatto |
| S2.7.5 | Server: verify WS bind su operazioni; ping senza broadcast | `SocketHandler` + registry | Fatto |
| S2.7.6 | `aggiorna()` non invia `offertaVincente` vuota | `SocketHandler.java` | Fatto |

**Criterio uscita:** checklist in [analisi-connessioni.md](./analisi-connessioni.md) §9.

---

### Fase S3 — Decoupling thread WebSocket (1–2 sprint)

| ID | Attività | File principali | Roadmap |
|----|----------|-----------------|---------|
| S3.1 | Estrarre `AstaBroadcastService`: unico punto di invio messaggi | Nuovo servizio | #10 |
| S3.2 | Spostare query DB (`getGiocatoriLiberi`, `elencoCronologiaOfferte`…) fuori da `handleTextMessage` | `SocketHandler` → service `@Async` | #4 |
| S3.3 | `creaMessaggio`: write DB asincrona o batch | `SocketHandler` / `LoggerRepository` | #4 |
| S3.4 | Estrarre logica `confermaAsta` (while turni) in `AstaTurnoService` | `SocketHandler` → service | #10 |
| S3.5 | Handler WS diventa thin: parse → delegate → schedule broadcast | `SocketHandler.java` | #10 |

**Criterio uscita:**
- `handleTextMessage` ritorna in < 50ms (no DB sync)
- Offerte concorrenti non si bloccano a vicenda

**Rischio:** medio-alto. Ordine messaggi da verificare su conferma asta.

---

### Fase S4 — Broadcast event-driven (1–2 sprint)

| ID | Attività | File principali | Roadmap |
|----|----------|-----------------|---------|
| S4.1 | Separare payload: **eventi** (offerta, login, messaggio) vs **timer** (contaTempo, timeStart) | `SocketHandler` / broadcast service | #5 |
| S4.2 | Scheduler invia solo campi timer (contaTempo, timeout, timeStart) — non full-state | `aggiorna()` | #5 |
| S4.3 | Eventi asta inviano solo delta (es. `{operazione:'offerta', offertaVincente:…}`) | Handler + client | #5 |
| S4.4 | Limitare `messaggi` a ultimi N (es. 100) nel broadcast | `SocketHandler` | #13 |
| S4.5 | Valutare riduzione `frequenza.refresh` o invio timer solo se asta attiva | `application.properties` | #5 |
| S4.6 | Aggiornare client `getMessaggio()` per gestire delta senza sovrascrivere tutto | `fantaasta/app.js` | #5 |

**Criterio uscita:**
- Payload medio broadcast < 30% attuale
- Timer UI fluido senza freeze su offerte

**Rischio:** medio. Richiede coordinamento client/server.

---

### Fase S5 — Allineamento FantaLive (0.5 sprint)

| ID | Attività | File principali | Roadmap |
|----|----------|-----------------|---------|
| S5.1 | `afterConnectionClosed` su `SocketHandlerFantalive` | `SocketHandlerFantalive.java` | #3 |
| S5.2 | Rendere thread-safe `Main.toSocket` o sostituire con metodo che ritorna snapshot | `Main.java`, `MyController.java` | #7 |
| S5.3 | Push solo se dati cambiati (hash/equals su snapshot) | `MyController.chckNotifica()` | #7 |
| S5.4 | Unificare interceptor sessione (eliminare 3 copie) | `configurazione/` | #11 |

**Criterio uscita:** nessun push ridondante ogni 5s se stato invariato.

**Rischio:** basso. Modulo meno critico.

---

### Fase S6 — Test e hardening (parallelo, 0.5 sprint)

| ID | Attività | Output | Roadmap |
|----|----------|--------|---------|
| S6.1 | Test integrazione WS: connect → ping → disconnect | `src/test/java/...` | #12 |
| S6.2 | Test concorrenza: N client ping simultaneo | Test JUnit + WebSocket client | #12 |
| S6.3 | Test session stealing e reconnect | Test manuale + automatizzato | #12 |
| S6.4 | Monitoraggio: log durata `invia()`, conteggio sessioni attive | Metriche/log | #13 |

**Criterio uscita:** test verdi su flussi S1–S4, checklist manuale completata.

---

## Timeline indicativa

| Fase | Durata stimata | Dipendenze |
|------|----------------|------------|
| S0 Baseline | 2–3 giorni | — |
| S1 Quick win | 3–5 giorni | S0 |
| S2 Sessioni | 3–5 giorni | S1 |
| S3 Decoupling | 5–8 giorni | S2 |
| S4 Event-driven | 5–8 giorni | S3 |
| S5 FantaLive | 2–3 giorni | S1 (parallelo possibile) |
| S6 Test | 3–5 giorni | S1+ (incrementale) |

**Totale stimato:** 4–6 settimane (1 dev, incrementale e deployabile per fase).

---

## Ordine di deploy consigliato

```
S0 → S1 (deploy) → S2 (deploy) → S3 (deploy) → S4 (deploy)
                      ↘ S5 (deploy parallelo dopo S1)
                      ↘ S6 (continuo)
```

Ogni fase è **deployabile indipendentemente** e porta valore misurabile.

---

## Mapping attività piano ↔ roadmap

| Piano | Roadmap # | Descrizione |
|-------|-----------|-------------|
| S1.1 | 1 | Rimuovere lock globale broadcast |
| S1.2, S1.4, S1.5, S5.1 | 3 | Lifecycle WS e reconnect client |
| S2.1–S2.3 | 2 | Sessioni unificate |
| S3.2–S3.3 | 4 | DB fuori thread WS |
| S4.1–S4.6 | 5 | Broadcast event-driven |
| S1.6, S2.4 | 6 | Thread-safety collection |
| S5.2–S5.3 | 7 | Ottimizzazione FantaLive |
| S2.5, S2.6, S4.4, S6.4 | 13 | Hardening operativo |
| S3.1, S3.4–S3.5 | 10 | Refactoring parziale handler |
| S5.4 | 11 | Eliminazione duplicazioni |
| S0, S6 | 12 | Test |

---

## Prossimo passo

**S6** completata (2026-09-25). Iniziativa socket S0–S6 chiusa. Prossimo: **#14** UI/UX soft. #8/#9 esclusi (uso tra amici).
