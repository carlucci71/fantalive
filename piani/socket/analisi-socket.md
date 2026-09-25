# Analisi problema WebSocket — FantaLive

> Documento di analisi focalizzato esclusivamente sul canale real-time.  
> Collegato a: [piano-sviluppo-socket.md](./piano-sviluppo-socket.md) · [roadmap-sistemazione.md](../roadmap-sistemazione.md)

---

## 1. Contesto

Il progetto espone **due endpoint WebSocket indipendenti**:

| Endpoint | Handler | Direzione | Uso |
|----------|---------|-----------|-----|
| `/messaggi-websocket` | `SocketHandler` | Bidirezionale | Asta FantaAsta (critico) |
| `/fantalive/fantalive-websocket` | `SocketHandlerFantalive` | Solo push server→client | Live score FantaLive |

Il problema segnalato (**blocco gestione sessioni sincrone**) riguarda principalmente **FantaAsta**, dove socket, sessione HTTP e stato applicativo sono fortemente accoppiati.

### Scenario reale: un solo client, molte WebSocket

Nei test locali l'admin usa **un unico browser** che gestisce tutti gli allenatori via "opera come". Non sono più utenti/dispositivi, ma **più connessioni WS dallo stesso client**:

| Osservazione log (sessione test) | Valore |
|----------------------------------|--------|
| Utenti in `utentiLoggati` | 1 |
| WebSocket attive contemporaneamente | **4** (poi 5) |
| Sessione usata per l'asta | 1 sola (`:50885`) |
| Tutte le altre WS | zombie da `ricaricaIndex()` / `connectWS()` |

Effetto: ogni `invia()` manda lo stesso payload a **tutte** le WS del client, moltiplicando traffico e lock inutilmente. Esempio dai log:

- `caricaFile` → **64 KB × 4 sessioni**
- `confermaAsta` → **63 KB × 4 sessioni**
- ogni `inviaOfferta` → **2 broadcast** (reset timer + aggiornamento offerta) × 4 sessioni

Il problema non è "session stealing" tra utenti diversi, ma **accumulo di connessioni sullo stesso browser** senza cleanup.

---

## 2. Architettura attuale (FantaAsta)

```
Browser (app.js)
    │  WS: operazioni JSON (connetti, ping, inviaOfferta, confermaAsta…)
    ▼
SocketHandler.handleTextMessage()     ← thread WebSocket
    ├── muta stato in-memory (utentiLoggati, offertaVincente, messaggi…)
    ├── scrive HttpSession (nomeGiocatoreLoggato, idLoggato)
    ├── chiama MyControllerAsta (query DB)
    └── invia() → broadcast a TUTTI i client

@Scheduled aggiorna() ogni 1s          ← thread scheduler
    └── invia() → full-state broadcast

MyControllerAsta (HTTP REST)
    └── invoca socketHandler.notifica*() → invia()
```

**File coinvolti:**

| Ruolo | File |
|-------|------|
| Handler WS asta | `src/main/java/com/daniele/asta/SocketHandler.java` |
| Config endpoint | `src/main/java/com/daniele/asta/WebSocketConfig2.java` |
| Handshake sessione | `src/main/java/com/daniele/asta/HttpSessionIdHandshakeInterceptor.java` |
| Registry HTTP session | `src/main/java/com/daniele/asta/HttpSessionConfig.java` |
| Controller REST+WS bridge | `src/main/java/com/daniele/asta/MyControllerAsta.java` |
| Client WS | `src/main/webapp/fantaasta/app.js` |
| Config refresh | `src/main/resources/application.properties` (`frequenza.refresh=1000`) |

---

## 3. Sintomi osservabili

| Sintomo | Probabile causa |
|---------|-----------------|
| UI che si “congela” durante offerte/login | Thread WS bloccato da DB + lock globale `invia()` |
| Utente disconnesso ma ancora in lista | Nessun `afterConnectionClosed`, leak client |
| Sessione HTTP valida ma utente “non loggato” | `utentiLoggati` e `HttpSession` disallineati |
| Latenza crescente con più utenti | Broadcast full-state ogni 1s a tutti |
| Ping che resetta stato altri utenti | `utentiScaduti = new ArrayList<>()` ad ogni ping |
| Connessioni duplicate stesso utente | `connectWS()` non chiude WS precedente |

---

## 4. Cause radice (ordinate per impatto)

### 4.1 Mutex globale su broadcast — CRITICO

`invia()` è `synchronized` a livello di istanza handler. Tutti i flussi (handler messaggi, scheduler, notifiche HTTP) competono per lo stesso lock.

Effetto: durante un broadcast lento (payload grande, N client), **nessun altro messaggio può essere inviato** e gli handler che attendono `invia()` restano bloccati.

Nota: esiste `inviaOrig()` con lock per-sessione, ma **non è usato**.

### 4.2 Broadcast full-state ogni secondo — CRITICO

`@Scheduled aggiorna()` invia ogni 1s l’intero stato: utenti, offerta, ping, messaggi, timer, turno… anche senza cambiamenti.

Effetto: carico costante su CPU, rete e lock; payload che cresce con `messaggi` (lista illimitata).

### 4.3 Lavoro sincrono sul thread WebSocket — ALTO

Operazioni come `connetti` e `confermaAsta` eseguono query DB e logica business **prima** di `invia()`, sullo stesso thread che gestisce tutti i messaggi in ingresso.

Effetto: un’operazione lenta blocca la ricezione/gestione di tutti gli altri messaggi WS.

### 4.4 Tre store di sessione disconnessi — ALTO

| Store | Contenuto | Cleanup |
|-------|-----------|---------|
| `HttpSession` | `nomeGiocatoreLoggato`, `idLoggato` | Solo su `disconnetti` / timeout container |
| `utentiLoggati` | Nomi utenti “online” | Manuale, parziale |
| `sessions` | `WebSocketSession` attive | Solo su nuova connessione o `disconnectAll` |

Non esiste mapping `WebSocketSession ↔ utente`. Il server non può chiudere la WS di un utente specifico né fare cleanup automatico alla chiusura.

### 4.5 Collection non thread-safe — ALTO

`utentiLoggati`, `utentiScaduti`, `pingUtenti`, `messaggi` sono `ArrayList`/`HashMap` standard, accessibili da:
- thread WebSocket (N connessioni)
- thread scheduler (`aggiorna()`)
- thread HTTP (notifiche da `MyControllerAsta`)

Solo `sessions` usa `CopyOnWriteArrayList`.

### 4.6 Lifecycle WebSocket incompleto — MEDIO

- `afterConnectionEstablished`: aggiunge sessione, pulisce solo WS già chiuse
- **Manca `afterConnectionClosed`**: nessuna rimozione utente da `utentiLoggati` alla chiusura improvvisa
- Client: `connectWS()` crea nuova WS senza chiudere la precedente

### 4.7 Protocollo ibrido HTTP + WS — MEDIO

Flusso tipico conferma asta:
1. Client → HTTP `POST /confermaAsta` (salva DB)
2. Client → WS `confermaAsta` (aggiorna stato e broadcast)

Due canali per una singola operazione, senza transazione condivisa né idempotenza.

### 4.8 Gestione errori silenziosa — BASSO

`handleTextMessage` cattura tutte le eccezioni e stampa solo `e.getMessage()`. NPE su `httpSession` null (handshake senza sessione HTTP) fallisce in silenzio.

---

## 5. Flussi critici analizzati

### 5.1 Login (`connetti`)

1. Client invia WS `connetti` con nome/id
2. Server scrive su `HttpSession` (può essere null)
3. Server aggiorna `utentiLoggati`
4. Server carica calciatori, preferiti, cronologia da DB
5. Server broadcast a tutti via `invia()`

**Punti deboli:** DB sync su thread WS, session stealing solo loggato, nessuna chiusura WS precedente dello stesso utente.

### 5.2 Heartbeat (`ping`)

1. Ogni client invia ping ogni N ms (`$interval` in app.js)
2. Server resetta `utentiScaduti` globalmente
3. Server ricalcola latenza per tutti gli utenti
4. Server broadcast `RICHIESTA` a tutti

**Punti deboli:** race con `aggiorna()`, ogni ping compete per lock `invia()`, utenti scaduti non rimossi da `utentiLoggati`.

### 5.3 Timer asta (`aggiorna` schedulato)

1. Ogni 1s costruisce payload completo
2. Chiama `invia()` con lock globale

**Punti deboli:** traffico ridondante, contesa costante col lock, serializzazione `messaggi` completa.

### 5.4 Offerta (`inviaOfferta`)

1. Validazione offerta in-memory
2. Possibile doppio `invia()` (reset timer + aggiornamento offerta)
3. `creaMessaggio()` → write DB sincrona

**Punti deboli:** due broadcast ravvicinati, lock doppio, latenza su rilanci rapidi.

---

## 6. FantaLive (impatto secondario)

`SocketHandlerFantalive` è più semplice (solo push), ma presenta pattern simili:

| Problema | Dettaglio |
|----------|-----------|
| Push periodico | `MyController.chckNotifica()` ogni 5s |
| Stato statico | `Main.toSocket` HashMap mutata senza sync |
| Lock globale indiretto | `Main.go()` è `synchronized static` |
| Nessun handler inbound | Client non invia messaggi (OK) |
| Lifecycle | Stesso pattern: no `afterConnectionClosed` |

Priorità inferiore rispetto a FantaAsta, ma da allineare nella stessa iniziativa architetturale.

---

## 7. Metriche di successo (target post-intervento)

| Metrica | Stato attuale (stimato) | Target |
|---------|-------------------------|--------|
| Lock globale su send | Sì (`synchronized invia`) | No (lock per-sessione o executor) |
| Broadcast full-state | 1 Hz sempre | Solo su evento o timer ridotto (solo timer UI) |
| Cleanup WS chiusa | Manuale/parziale | Automatico in `afterConnectionClosed` |
| Mapping sessione↔utente | Assente | Presente e univoco |
| Thread-safety stato shared | Parziale | Completa (`ConcurrentHashMap` / servizio dedicato) |
| DB su thread WS | Sì | No (`@Async` o service layer) |

---

## 8. Rischi dell’intervento

| Rischio | Mitigazione |
|---------|-------------|
| Regressioni su flussi asta | Test manuali su scenari offerta/conferma/turno |
| Client legacy non compatibile con delta | Fase 1 retrocompatibile; delta in fase 2 |
| Refactor troppo ampio | Piano a sprint incrementali (vedi piano sviluppo) |
| Downtime in produzione | Deploy per fasi, feature flag su nuovo broadcast |

---

## 9. Conclusione

Il blocco delle sessioni non è un singolo bug ma l’effetto combinato di:

1. **Lock globale** su tutti i send
2. **Polling aggressivo** (1 Hz full-state)
3. **Stato condiviso non thread-safe** senza lifecycle WS completo
4. **Accoppiamento** tra socket, HTTP session e logica DB nello stesso thread

L’intervento va strutturato in fasi incrementali, partendo da quick win a basso rischio (lock, lifecycle, client reconnect) e proseguendo con separazione responsabilità e broadcast event-driven.

---

## 10. Analisi connessioni (2026-09-25)

Analisi dedicata a connessioni, riconnessioni, multi-tab e affidabilità `sendMsg`:  
→ **[analisi-connessioni.md](./analisi-connessioni.md)**

Sintesi: i sintomi recenti (RESET loop, tasti mobile, assegnazione errata) derivano da **mancanza di macchina a stati client**, **sendMsg non affidabile**, **conflitto tab index/admin**, e **polling `aggiorna()` che sovrascrive lo stato UI** — non da singoli bug isolati.
