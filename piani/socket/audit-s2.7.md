# Audit S2.7 — Connection manager (2026-09-25)

Check pre-proseguimento (S3/S4/test). Collegato a [analisi-connessioni.md](./analisi-connessioni.md).

---

## Cosa è stato implementato

| Area | File | Contenuto |
|------|------|-----------|
| Client WS | `ws-connection.js` | Stati, coda send, reconnect, tab leader, login ack |
| Client app | `app.js` | `syncSessionFromServer`, `applyInitData`, delega a FantaWsConnection |
| HTML | `index/admin/...html` | Script `ws-connection.js` prima di `app.js` |
| Server registry | `AstaSessionRegistry` | bind/unbind, verify session, duplicate HTTP cleanup |
| Server handler | `SocketHandler` | `connettiOk` diretto, auth operazioni, ping senza broadcast, `aggiorna` senza offerta vuota |
| Persistenza asta | `MyControllerAsta` | `confermaAsta` usa offerta server |

---

## Bug già trovati e corretti

| # | Problema | Sintomo | Fix |
|---|----------|---------|-----|
| B1 | `ricaricaIndex(false)` chiamato prima di `calcolaIsAdmin` | JS error, pagina bianca/congelata | Spostato a fine `app.run` |
| B2 | `DA_CONFIGURARE` non apriva WS | "Connessione al backend in corso..." infinito | `syncSessionFromServer` chiama `connect()` anche se non configurato |
| B3 | `doConnect` risolveva prima di `connettiOk` | Redirect admin senza login HTTP/WS | `login()` attende ack `connettiOk` |
| B4 | `erroreConnetti` via `invia()` broadcast | Errore a tutti i client | `sendToSession` solo al mittente |
| B5 | `onReconnect` senza guard | Crash se reconnect durante init | Guard su `syncSessionFromServer` |
| B6 | `ricaricaIndex(false)` dentro `ModalDemoCtrl` | Admin (e pagine senza quel controller) non si avviava mai | Spostato in `app.run` |
| B7 | `admin.html` con `ng-show` su `bSemaforoAttivo` | Admin vuota durante asta attiva | Solo `nomegiocatore` su admin |

---

## Audit strutturale aggiuntivo (2026-09-25)

### Critici — da risolvere prima di considerare “stabile”

| ID | Problema | Impatto | Dove |
|----|----------|---------|------|
| **C1** | **Auth WS “opera come” senza check admin** | `requireBoundOperator` verifica solo `nomegiocatoreOperaCome` (chi è loggato), ma `inviaOfferta`/`start` applicano `nomegiocatore` dal JSON. Un utente può offrire o avviare asta **a nome di un altro** | `SocketHandler.requireBoundOperator`, `inviaOfferta`, `start` |
| **C2** | **`erroreOperazione` → reconnect loop** | Auth fallita chiama `onReconnect()` → `syncSessionFromServer` → nuovo login → nuovo errore | `ws-connection.js` `handleServerMessage` |
| **C3** | **Login ack senza timeout** | Se `connettiOk` non arriva, `loginPromise` e coda messaggi restano bloccati | `ws-connection.js` `login()` |
| **C4** | **Link Admin su index nascosto in asta** | `ng-show="nomegiocatore && bSemaforoAttivo"` — stesso bug di B7 su admin, ma su index | `index.html` ~109 |
| **C5** | **`liberaSemaforo` senza ruolo** | Qualsiasi utente bindato può sbloccare il semaforo (payload senza nome; auth solo via session bind) | `SocketHandler`, `app.js` |
| **C6** | **`disconnectAll` inefficace** | Server chiude WS ma client fa auto-reconnect; HTTP session non pulita → tutti tornano online | `SocketHandler.disconnectAll`, `ws-connection.js` |

### Strutturali — instabilità / debito architetturale

| ID | Problema | Dettaglio |
|----|----------|-----------|
| **S1** | `aggiorna()` invia `timeStart=0` sempre | Anche senza asta attiva (`calInizioOfferta==null`); client sovrascrive `timeStart=-1` da `clearOfferta` |
| **S2** | `syncSessionFromServer` senza mutex | `ricaricaIndex` + `onReconnect` possono sovrapporsi |
| **S3** | `$rootScope.$apply()` cieco | Ogni messaggio WS; rischio digest error sotto carico |
| **S4** | Tab leader solo `BroadcastChannel` | Safari vecchio: index+admin competono ancora |
| **S5** | Tutte le pagine aprono WS | `liberi.html`, `logger.html`, ecc. caricano `app.js` → `ricaricaIndex` → WS anche su pagine secondarie |
| **S6** | Modal password non attende login | `ModalInstanceCtrl` chiama `doConnect()` e chiude senza aspettare `connettiOk` |
| **S7** | Admin GIOC0 auto-login senza password | Accesso diretto `admin.html` = admin se lega configurata (OK dev, rischio prod) |
| **S8** | Cache-bust inconsistente | Solo index/admin hanno `?v=20250925b`; altre pagine possono caricare JS vecchio |
| **S9** | HTTP session sopravvive a drop WS | `/init` rimanda in login automatico; registry già unbind (P1 analisi-connessioni) |
| **S10** | `connetti` + DB su thread WS | Latenza login; piano S3 |

### Priorità fix consigliata (prima di S3)

1. **C1** — server: se `nomegiocatore != boundUser` richiedere admin (o rifiutare)
2. **C2 + C3** — timeout login + no reconnect su `erroreOperazione` (o max retry)
3. **C4** — una riga su `index.html`
4. **C6** — `disconnectAll`: flag client o messaggio WS “stop reconnect” + clear HTTP
5. Poi test checklist §9

---

## Rischi residui (da tenere d’occhio)

### Alta priorità

| ID | Rischio | Dettaglio | Mitigazione suggerita |
|----|---------|-----------|----------------------|
| R1 | **H2 in-memory** | Ogni restart server = DB vuoto → schermata CONFIGURAZIONE | Usare H2 file o profilo con dati; documentare per dev |
| R2 | **`erroreOperazione` → reconnect loop** | Auth fallita scatena `onReconnect` ricorsivo | → vedi **C2** |
| R3 | **Tab leader solo `BroadcastChannel`** | Safari/iOS vecchi: fight index+admin torna possibile | Fallback `sessionStorage` lock |
| R4 | **`$rootScope.$apply()` in getMessaggio** | Possibile "digest already in progress" | `$scope.$applyAsync()` o verifica `$$phase` |

### Media priorità

| ID | Rischio | Dettaglio |
|----|---------|-----------|
| R5 | `send()` con WS OPEN ma non READY | Messaggio in coda fino a `connettiOk`; se ack non arriva, coda bloccata | Timeout login + reject coda |
| R6 | `aggiorna()` 1 Hz ancora attivo | Sovrascrive parzialmente UI (mitigato: no offerta vuota) | S4 event-driven |
| R7 | `connetti` fa DB sync su thread WS | Latenza sotto carico | S3 async |
| R8 | `liberaSemaforo` senza `nomegiocatore` nel JSON | Auth via session bind only — OK se loggati |
| R9 | Cache browser `app.js` | Fix non visibili senza hard refresh | Cache-bust query string in dev |

### Bassa priorità

| ID | Rischio |
|----|---------|
| R10 | `login.html` non usa `ws-connection.js` (solo form HTTP — OK) |
| R11 | `RICHIESTA` in getMessaggio ancora gestito ma server non lo invia più |
| R12 | Doppio `cronologiaOfferte` in payload `connetti` (pre-esistente) |

---

## Ordine di inizializzazione client (verificato)

```
FantaWsConnection.init()          → riga ~34  (callback lazy OK)
applyInitData / syncSession...    → riga ~749
aggiornaTimePing / ping interval  → riga ~1199
calcolaIsAdmin                    → riga ~1367
ricaricaIndex(false)              → riga ~1561  ✅ ultimo
```

**Regola:** qualsiasi nuova init che chiama funzioni `$rootScope` deve stare **dopo** le loro definizioni o usare callback lazy.

---

## Checklist test prima di S3

- [ ] Fresh start: config iniziale → admin → caricamento giocatori/allenatori
- [ ] Login GIOC0 + GIOC1 (PC + mobile)
- [ ] Asta: offerte, conferma, seconda asta con tasti
- [ ] Kill rete 10s: reconnect automatico, un solo `connetti` in log
- [ ] index + admin aperti: una tab attiva, altra sospesa
- [ ] Hard refresh dopo deploy JS
- [ ] Log: `sessions ≈ utenti`, no raffiche `close duplicate`

---

## Prossimo passo consigliato

1. Completare checklist test sopra  
2. S3 (DB off thread WS)  
3. S4 (`aggiorna` → eventi)
