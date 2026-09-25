# Roadmap sistemazione FantaLive

> Ultimo aggiornamento: 2026-09-25 — Suite test completa: 14 Java + 8 Playwright (`./scripts/run-s6-tests.sh`).  
> **Iniziativa attiva:** [Fix WebSocket](./socket/piano-sviluppo-socket.md) — fasi S0–S6 chiuse  
> **Prossimo passo:** #14 UI soft

---

## Iniziativa corrente: Fix WebSocket

| Fase | Nome | Stato | Piano |
|------|------|-------|-------|
| S0 | Baseline e verifica | Completata (test manuali) | [S0](./socket/piano-sviluppo-socket.md#fase-s0--baseline-e-verifica-prerequisito) |
| S1 | Quick win: sblocco send e lifecycle | Completata | [S1](./socket/piano-sviluppo-socket.md#fase-s1--quick-win-sblocco-send-e-lifecycle-1-sprint) |
| S2 | Sessioni unificate | Completata (con S2.7) | [S2](./socket/piano-sviluppo-socket.md#fase-s2--sessioni-unificate-1-sprint) |
| S2.7 | Connection manager client + policy server | Completata | [S2.7](./socket/piano-sviluppo-socket.md#fase-s27--connection-manager-client--policy-server-implementata-2026-09-25) |
| S3 | Decoupling thread WebSocket | Completata (test log 14:23–14:26) | [S3](./socket/piano-sviluppo-socket.md#fase-s3--decoupling-thread-websocket-12-sprint) |
| S4 | Broadcast event-driven | Completata (test log ~395 byte rilanci) | [S4](./socket/piano-sviluppo-socket.md#fase-s4--broadcast-event-driven-12-sprint) |
| S5 | Allineamento FantaLive | Completata (test browser 14:46) | [S5](./socket/piano-sviluppo-socket.md#fase-s5--allineamento-fantalive-05-sprint) |
| S6 | Test e hardening | Completata (Java + Playwright) | [S6](./socket/piano-sviluppo-socket.md#fase-s6--test-e-hardening-parallelo-05-sprint) |

### Debito residuo post-audit (opzionale)

| ID | Cosa | Priorità |
|----|------|----------|
| — | `$apply()` → `$applyAsync` in `getMessaggio` | Bassa |

### Note recenti (2026-09-25)

- **Test E2E browser flow:** `tests/e2e/asta-browser-flow.spec.js` — admin setup, opera come, 2 browser, conferme, flusso 3 aste. Helper `tests/e2e/helpers/asta-helpers.js`. Comando: `./scripts/run-s6-tests.sh`.
- **C3+C6:** timeout login 15s su `connettiOk`; `disconnectAll` invia `DISCONNECT_ALL`, pulisce sessioni HTTP, blocca reconnect client; fallback in `syncSessionFromServer` se HTTP senza utente. Cache `?v=20250925s`.
- **C1+C5:** `requireTargetSelfOrAdmin` su `start`/`inviaOfferta`; `liberaSemaforo` solo admin; test integrazione.
- **Audit S6:** S2 chiusa con S2.7. Dettaglio: [audit-s2.7.md](./socket/audit-s2.7.md).
- **S6 completata:** `AstaWebSocketIntegrationTest` (8 scenari), `AstaSessionRegistryTest`, `FantaLiveWebSocketIntegrationTest`; Playwright 4 test (2 browser, rilancio, conferma, FantaLive); `POST /fantaasta/test/seed` per E2E; script `./scripts/run-s6-tests.sh`.
- **S5 verificata (14:46):** restart PID 28564; `/fantalive/index.html` → connect `sessions=1`; refresh → disconnect `1001 removed=true sessions=0` + reconnect ~20ms; leave verso fantaasta → `sessions=0`; nessun errore scheduler.
- **S5 implementata:** `SocketHandlerFantalive` con `afterConnectionClosed`; `chckNotifica` non invia se zero client WS; payload meta completo solo se cambia stato, altrimenti solo `timeRefresh`; `Main.toSocket` rimosso → `buildFantaliveStatusPayload()`; interceptor unificato con `com.daniele.configurazione`.
- **S4 completata:** rilanci ~395 byte, conferma ~63k evento; timer delta non sovrascrive stato UI asta.
- **S3 completata:** servizi broadcast/log/data/turni; `inviaOfferta` 1–7ms; asta PC+mobile con opera come, conferma e 2ª asta OK (14:23–14:26).
- **Rilanci post-conferma:** `clearOfferta` + allinea `offertaPriv`; cache `?v=20250925p`. Piano: S4→S5→S6 poi #14 UI soft.
- **Sessioni fantasma:** `onWsClosed` rimuoveva `wsSessionIdToName` prima di `removeBinding` → utente restava nel registry (`onlineWs=true`) e reconnect bloccato fino a cestino admin; fix registry + `isLoggedIn` solo WS aperti + recovery mobile (visibility, last-user tab, asta visibile da `/init`). Cache `?v=20250925o`.
- **cancellaUtente (cestino):** payload senza `nomegiocatoreOperaCome` → auth falliva come GIOC1, reconnect loop e sessione admin persa; fix client+server (solo admin può espellere altri).
- **Admin:** login automatico legge l'utente con `isAdmin` da `elencoAllenatori` (`trovaAllenatoreAdmin`); branch admin prima del check `onlineWs`.
- **Test browser E2E:** 2 tab stesso browser (GIOC0 admin + GIOC1), rilanci multipli, vincitore corretto su entrambi i client, conferma OK asta 1 (GIOC1→Ronaldo 12) e asta 2 (GIOC0→Pedro 20). UI: auto → tasti +1/+5/+10; non-auto → banconota (con importo > offerta corrente).
- **Multi-utente stessa sessione HTTP:** `connetti` chiude solo WS non associati (`closeUnboundDuplicateHttpSessions`), non disconnette altri utenti già loggati sulla stessa sessione browser.
- **Offerta vincente:** race scheduler/bid poteva ripristinare vincitore obsoleto; fix `synchronized` + `astaEpoch` + broadcast rilancio unico. Cache `?v=20250925j`.
- **confermaAsta:** `ClassCastException` Long→BigInteger in `avanzaTurnoDopoConferma`; fix `toInt(Number)`.
- **Multi-tab:** pagine secondarie solo HTTP `/init`, non rubano WS alla home (`?v=20250925h`).
- **S2.7:** connection manager + policy server. Audit: [audit-s2.7.md](./socket/audit-s2.7.md) — fix B1–B7.
- **Asta:** F1–F5 + A1–A3 fixati e verificati. Checklist §8: asta multi-client, pausa/resume, termina, conferma, azzera (con reconnect WS), seconda asta, snapshot `/init`, riapri, azzeraTempo — tutti OK (`target/asta-check.py`). Nota: `azzera` invalida sessioni WS (comportamento atteso).
- **S0/S1:** test asta 1+2 client OK (pre-S2). Rieseguire test post-S2 per confermare `sessions ≈ utenti`.
- **S0.1** (checklist formale): rinviata a S6.

---

## Macro attività (roadmap generale)

| # | Macro attività | Area | Priorità | Piano socket | Stato |
|---|----------------|------|----------|--------------|-------|
| 1 | Rimuovere lock globale su broadcast WebSocket | Socket FantaAsta | Alta | S1.1 | Fatto |
| 2 | Unificare gestione sessioni (HTTP + WS + utenti loggati) | Sessioni | Alta | S2, S2.7 | Fatto (test) |
| 3 | Cleanup lifecycle WebSocket (`afterConnectionClosed`, reconnect client) | Socket FantaAsta | Alta | S1.2–S1.5, S5.1 | Fatto |
| 4 | Spostare lavoro DB fuori dal thread WebSocket | Socket FantaAsta | Alta | S3 | Fatto (log @Async) |
| 5 | Passare da broadcast full-state a eventi/delta | Socket FantaAsta | Alta | S4 | Fatto |
| 6 | Rendere thread-safe le collection condivise | Concorrenza | Alta | S1.6, S2.4 | Fatto |
| 7 | Ottimizzare scheduler FantaLive (`go()`, `toSocket`, push 5s) | Socket FantaLive | Media | S5 | Fatto (push condizionale) |
| 8 | Riattivare sicurezza base (auth, validazione operazioni, WSS) | Sicurezza | Alta | — | Backlog |
| 9 | Esternalizzare credenziali e segreti | Sicurezza | Alta | — | Backlog |
| 10 | Refactoring god class (`Main.java`, `SocketHandler`, controller) | Architettura | Media | S3 (parziale) | Backlog |
| 11 | Eliminare duplicazioni tecniche (interceptor sessione, config WS) | Architettura | Media | S5.4 | Parziale (fantalive→configurazione) |
| 12 | Introdurre test su sessioni, WS e flussi asta critici | Qualità | Media | S0, S6 | Fatto (Java + Playwright) |
| 13 | Hardening operativo (limiti messaggi, heartbeat, logging errori) | Affidabilità | Media | S2.5–S2.6, S4.4, S6.4 | Fatto (log + cap messaggi) |
| 14 | Piano evoluzione frontend (build moderna / migrazione graduale) | Frontend | Bassa | — | Backlog |

---

## Ordine consigliato

### Ora (post-socket)
**#14** UI soft

### Dopo socket
1. **Sicurezza:** 8 → 9  
2. **Architettura:** 10 → 11  
3. **Evoluzione:** 14

---

## Documenti correlati

| Documento | Contenuto |
|-----------|-----------|
| [analisi-socket.md](./socket/analisi-socket.md) | Analisi tecnica cause radice |
| [analisi-connessioni.md](./socket/analisi-connessioni.md) | Connessioni, reconnect, multi-tab, sendMsg |
| [piano-sviluppo-socket.md](./socket/piano-sviluppo-socket.md) | Piano operativo per fasi S0–S6 |
