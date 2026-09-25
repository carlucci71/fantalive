# Analisi gestione connessioni WebSocket — FantaAsta

> Analisi sistematica (non fix puntuali) di connessioni, riconnessioni, login e sessioni.  
> Collegato a: [analisi-socket.md](./analisi-socket.md) · [piano-sviluppo-socket.md](./piano-sviluppo-socket.md)

**Data:** 2026-09-25

---

## 1. Modello corretto (target)

Un sistema asta multi-client deve garantire invarianti chiare:

| Invariante | Significato |
|------------|-------------|
| **I1** | Un utente loggato (`nomegiocatore`) = **una sola** WS attiva nel registry |
| **I2** | Una tab/pagina browser = **una sola** WS client (singleton) |
| **I3** | HTTP session e WS registry **concordano** dopo ogni transizione stabile |
| **I4** | Riconnessione trasparente: drop di rete → stesso utente ripristinato senza intervento |
| **I5** | Secondo dispositivo stesso utente → primo dispositivo riceve segnale esplicito e si ferma |
| **I6** | Messaggi operativi (offerta, conferma) non persi durante brevi disconnessioni |
| **I7** | Nessun loop connect/disconnect sulla stessa tab |

### Stati logici (mancanti oggi nel client)

```
[PAGE_LOAD] → HTTP_INIT → WS_CONNECTING → WS_OPEN → WS_LOGIN(connetti) → READY
                ↑                              |
                └──────── RECONNECTING ←───────┘ (onclose non volontario)
```

Oggi questi stati sono impliciti e sparsi tra `ws`, `$rootScope.nomegiocatore`, cookie HTTP e `tokenUtente`.

---

## 2. Architettura attuale — tre store disallineati

```
┌─────────────────┐     connetti      ┌──────────────────────┐
│  HttpSession    │◄──────────────────│  AstaSessionRegistry │
│  nomeGiocatore  │                   │  user ↔ WS ↔ ping    │
│  idLoggato      │                   └──────────┬───────────┘
└────────┬────────┘                              │
         │                                        │
         │  /init legge HTTP                      │  getUtentiLoggati()
         ▼                                        ▼
┌─────────────────────────────────────────────────────────────┐
│  Client Angular ($rootScope.nomegiocatore, tokenUtente, ws) │
└─────────────────────────────────────────────────────────────┘
```

| Store | Scritto da | Letto da | Cleanup su WS drop |
|-------|------------|----------|-------------------|
| `HttpSession` | `connetti`, `disconnetti` | `/init`, REST | **No** (resta fino a timeout/logout) |
| `AstaSessionRegistry` | `bind`, `unbind`, `onWsClosed` | broadcast `utenti`, ping | **Sì** (`afterConnectionClosed`) |
| Client `ws` + scope | `doConnect`, `ricaricaIndex` | UI, `sendMsg` | Parziale (`scheduleWsReconnect`) |

**Problema P1 — HTTP sopravvive alla WS:** alla chiusura improvvisa della socket, il registry rimuove l’utente ma `HttpSession` conserva `nomeGiocatoreLoggato`. `/init` lo rimanda in login automatico. È voluto per il reconnect, ma crea finestre in cui `onlineWs=false` e `giocatoreLoggato!=null`, e il client non usa `onlineWs` per decidere nulla.

---

## 3. Flusso client — problemi strutturali

### 3.1 Troppi entry point verso `connectWS()`

| Trigger | File:riga | Comportamento |
|---------|-----------|---------------|
| Avvio pagina | `app.js:1085` | `ricaricaIndex(false)` |
| Login utente | `doConnect` → `connectWS` | + `connetti` |
| Reconnect | `scheduleWsReconnect` → `ricaricaIndex` | init HTTP + connect + doConnect |
| `sendMsg` senza ws | `app.js:1091` | `connectWS()` fire-and-forget, **messaggio perso** |
| Config admin | `confermaConfigIniziale` | `ricaricaIndex` + `doConnect` |
| Modal password | `ModalInstanceCtrl` | `doConnect()` senza attendere |

**Problema P2 — Nessuna macchina a stati:** più chiamate concorrenti a `connectWS` sono mitigate dal debounce (150 ms) e da `wsConnectPromise`, ma `sendMsg` può aprire una connessione parallela senza coordinamento.

**Problema P3 — `ricaricaIndex` annida connect:**  
`ricaricaIndex` → `connectWS()` → se loggato → `doConnect()` → **`connectWS()` di nuovo** → `connetti`. Ridondante e aumenta la finestra di race.

### 3.2 `sendMsg` non affidabile

```javascript
// app.js — comportamento attuale
if (!ws) { connectWS(); return; }           // messaggio perso
if (ws.readyState === 1) { ws.send(s); }    // OK
else if (ws.readyState === 0) { /* attende? no */ }
else if (ws.readyState === 3) { scheduleWsReconnect(); }  // messaggio perso
```

**Problema P4:** offerte, ping e `confermaAsta` WS possono essere silenziosamente scartati. Nessuna coda né retry.

### 3.3 Reconnect

```javascript
scheduleWsReconnect → ricaricaIndex(false)  // backoff OK
```

Migliorato (niente doppio connectWS esplicito), ma:
- **P5:** ogni reconnect rigenera `tokenUtente` (`doConnect`) senza che il server validi il token precedente
- **P6:** `RESET_UTENTE` gestito con flag `wsConnecting` — fragile; dipende dal timing, non dall’identità della socket

### 3.4 Ping

- `$interval` ogni 5 s invia ping **anche con `nomegiocatore` vuoto**
- Server risponde con broadcast `RICHIESTA` a **tutti** i client per ogni ping ricevuto
- **P7:** traffico O(N²) su ping; latenza misurata in modo indiretto e rumoroso

---

## 4. Flusso server — problemi strutturali

### 4.1 Lifecycle WS

| Evento | Comportamento attuale | Gap |
|--------|----------------------|-----|
| `afterConnectionEstablished` | Aggiunge sessione; chiude solo WS **non bindate** stesso HTTP | OK per zombie |
| `connetti` | `bind` + chiude altre WS stesso HTTP | OK post-fix |
| `afterConnectionClosed` | `onWsClosed` → unbind se non superseded | OK |
| `disconnetti` | unbind + pulisce HTTP | OK solo se client cooperativo |

**Problema P8 — Tab multiple stesso utente:** `index.html` e `admin.html` sono **pagine separate** che caricano entrambe `app.js` e chiamano `ricaricaIndex` all’avvio. Due tab = stesso `JSESSIONID` = due WS che competono per `bind(GIOC0)`. La seconda ruba la prima → la prima fa reconnect → loop di steal silenziosi. **Scenario reale admin + index aperti insieme.**

### 4.2 Session stealing / RESET_UTENTE

| Caso | Comportamento atteso | Attuale |
|------|---------------------|---------|
| Reconnect stesso browser | Chiudi vecchia WS, nessun alert | OK (post-fix: no RESET se same httpSession) |
| Secondo dispositivo stesso utente | RESET su primo | OK (`bind` con httpSession diversa) |
| Due tab stesso browser | Una tab “vince” | **Conflitto continuo** — non modellato |
| WS duplicate prima di `connetti` | Ignorare o chiudere zombie | OK (`closeUnboundDuplicateHttpSessions`) |

**Problema P9:** non c’è policy esplicita per multi-tab. Serve: una sola tab attiva, oppure tab secondaria in sola lettura senza `connetti`.

### 4.3 `connetti` — side effect pesanti sul thread WS

1. Scrittura HTTP session  
2. Query DB (calciatori, preferiti, cronologia)  
3. Broadcast full payload (~60 KB) a **tutte** le sessioni  

**Problema P10:** latenza login e contesa con `aggiorna()` 1 Hz (già in analisi-socket §4.3).

### 4.4 `erroreConnetti` non gestito lato client

Server invia `erroreConnetti` se HTTP session assente; **il client non lo legge** in `getMessaggio`. L’utente resta in stato indefinito.

### 4.5 Validazione operazioni

- `ping` / `inviaOfferta` / `confermaAsta` WS: **nessun controllo** che la WS mittente sia quella bindata per `nomegiocatore`
- **P11:** un client con WS aperta ma non loggato potrebbe inviare operazioni con nome altrui (sicurezza + correttezza)

---

## 5. Broadcast `aggiorna()` vs stato client

Ogni 1 s il server invia l’intero stato, incluso:
- `offertaVincente` (anche `{}` vuoto)
- `timeStart`, `contaTempo`, `messaggi`, `pingUtenti`

**Problema P12:** il client applica patch parziali con `if (msg.campo)` — un oggetto vuoto o un valore “falsy” può sovrascrivere stato locale (già visto con tasti offerta mobile dopo conferma). È un anti-pattern: **polling server sovrascrive UI client** senza versioning/event id.

---

## 6. Mappa sintomi → cause

| Sintomo osservato | Cause probabili |
|-------------------|-----------------|
| "Utente esistente. Riconnettiti!" | RESET_UTENTE su WS vecchia durante reconnect; loop duplicate httpSession (mitigato parzialmente) |
| Instabilità / reconnect continui | Tab multiple; `sendMsg`→`connectWS` parallelo; fight tra `ricaricaIndex` e reconnect |
| Mobile senza tasti offerta | `aggiorna()` + `start`/`conferma` non allineati; `timeStart`/`avviabili` non resettati |
| Assegnazione giocatore sbagliata | HTTP `confermaAsta` usava offerta client admin, non server (fix applicato) |
| Utente “offline” ma ancora in lista HTTP | Registry unbind su drop, HTTP session ancora valorizzata |
| Traffico/lentezza con pochi utenti | `aggiorna` 1 Hz × N sessioni × payload grande; ping→RICHIESTA broadcast |
| 4–5 WS stesso browser | `ricaricaIndex` + navigazione pagine + reconnect senza policy tab |

---

## 7. Design corretto proposto

### 7.1 Client — `WsConnectionManager` (concetto)

Responsabilità uniche:
1. **Una** WS per pagina, stati espliciti
2. Coda messaggi fino a `READY` (WS open + `connetti` ack)
3. Reconnect: `HTTP /init` → se `giocatoreLoggato` → connect → `connetti` (un solo percorso)
4. `sendMsg` sempre async: attende READY o accoda
5. Ignorare messaggi WS da socket `!== currentSocket`
6. Policy tab: `sessionStorage` lock o BroadcastChannel “sono tab primaria”

### 7.2 Server — policy connessioni

1. **Registry come unica fonte** per `utenti` online e ping
2. `connetti` idempotente: stessa WS + stesso utente → refresh, non steal
3. `connetti` stesso utente, WS diversa, **stesso httpSession** → supersede silenzioso (già fatto)
4. `connetti` stesso utente, **httpSession diversa** → RESET + steal
5. Validare che `operazione` critiche arrivino dalla WS bindata per quel `nomegiocatore`
6. Rispondere `connetti` solo al client che ha fatto login (non broadcast 60 KB a tutti) — o almeno evento `loginAck` dedicato
7. `aggiorna()`: non inviare campi vuoti che azzerano stato; o passare a delta/eventi (S4)

### 7.3 Separazione canali

| Dato | Canale corretto |
|------|-----------------|
| Login persistente | HTTP session |
| Presenza real-time | WS registry + ping |
| Stato asta | WS eventi (non poll 1 Hz full-state) |
| Persistenza acquisto | HTTP `confermaAsta` (autoritativo) + WS notifica |

Il flusso conferma deve essere **HTTP-first**: WS `confermaAsta` solo notifica, mai fonte di verità per l’assegnazione (fix in corso).

---

## 8. Elenco problemi prioritizzati

| ID | Problema | Gravità | Fase piano |
|----|----------|---------|------------|
| P1 | HTTP session vs registry disallineati su drop WS | Alta | S2+ |
| P2 | Nessuna macchina a stati client | Alta | **Nuova S2.7** |
| P3 | `ricaricaIndex` → doppio `connectWS` | Media | S2.7 |
| P4 | `sendMsg` perde messaggi | **Critica** (offerte) | S2.7 |
| P5 | `tokenUtente` senza validazione server | Media | S2 |
| P6 | RESET_UTENTE fragile lato client | Alta | S2.7 |
| P7 | Ping → broadcast RICHIESTA a tutti | Media | S4 |
| P8 | Tab index + admin competono | **Alta** | S2.7 |
| P9 | Policy multi-tab assente | Alta | S2.7 |
| P10 | DB + broadcast su thread WS in `connetti` | Alta | S3 |
| P11 | Operazioni WS senza auth bind | Alta | Sicurezza / S2 |
| P12 | `aggiorna()` sovrascrive stato client | Alta | S4 |
| P13 | `erroreConnetti` ignorato dal client | Media | S2.7 |
| P14 | `connetti` broadcast full-state a tutti | Media | S4 |

---

## 9. Criteri di accettazione (test obbligatori)

Prima di considerare “connessioni OK”:

1. **1 client** — login, offerta, conferma, reconnect Wi‑Fi kill: stato coerente
2. **2 client** (PC + mobile) — offerte alternate, conferma, seconda asta con tasti visibili
3. **Reconnect** — disabilitare rete 10 s: un solo `connetti`, nessun alert, nessun loop in log
4. **Tab doppia** — index + admin: comportamento definito (una attiva o entrambe stabili)
5. **Steal** — stesso utente su 2 dispositivi: secondo connette, primo riceve RESET e si ferma
6. **Log** — `sessions` ≈ `utenti` loggati; niente raffiche `close duplicate` < 1/min
7. **Messaggi** — offerta inviata durante reconnect breve non persa (dopo coda)

---

## 10. Prossimo passo consigliato

**Non altri fix puntuali.** Implementare **S2.7 — Connection manager client + policy tab + coda send** e completare test checklist §9. Poi S3 (DB off thread) e S4 (eventi al posto di `aggiorna` full-state).

I fix già applicati (registry, no RESET su same httpSession, conferma server-side) sono passi verso il modello §7, ma **non sostituiscono** macchina a stati client e coda messaggi.
