# Checklist test scenari — FantaAsta / FantaLive

> S0.1 + S6 — test riproducibili automatici dove possibile.

## Esecuzione rapida

```bash
# Solo Java (avvia contesto Spring in-memory, nessun server esterno)
mvn test -Dtest=AstaSessionRegistryTest,AstaWebSocketIntegrationTest,FantaLiveWebSocketIntegrationTest

# Suite S6 completa (Java + Playwright, server su :8080 richiesto per E2E)
./scripts/run-s6-tests.sh

# Solo Java, salta browser
SKIP_E2E=1 ./scripts/run-s6-tests.sh
```

## Test automatici Java

| Test | Scenario |
|------|----------|
| `AstaSessionRegistryTest` | bind, close, rebind stesso utente |
| `AstaWebSocketIntegrationTest.connectSingleUser` | 1 WS = 1 utente, `/init` coerente |
| `AstaWebSocketIntegrationTest.twoUsersParallel` | 2 sessioni HTTP, 2 utenti, sessions=2 |
| `AstaWebSocketIntegrationTest.reconnectSameUser` | reconnect non duplica utente |
| `AstaWebSocketIntegrationTest.secondaryPagesHttpOnly` | liberi/riepilogo/logger/cronologia non aprono WS |
| `AstaWebSocketIntegrationTest.auctionWinner` | rilancio GIOC1 visibile su entrambi i client |
| `AstaWebSocketIntegrationTest.confirmAuction` | termina + conferma aggiorna `mapSpesoTotale` |
| `AstaWebSocketIntegrationTest.secondAuction` | seconda asta dopo conferma |
| `AstaWebSocketIntegrationTest.ping` | ping mantiene sessione |
| `FantaLiveWebSocketIntegrationTest` | connect/disconnect + push `timeRefresh` |

## Test browser Playwright

### Core (`tests/e2e/asta.spec.js`)

| Test | Scenario |
|------|----------|
| due browser paralleli | GIOC0 + GIOC1, init coerente, pagine secondarie |
| rilancio GIOC1 | offerta visibile su entrambi i client |
| asta via WS | start → offerta → termina → conferma |
| FantaLive refresh | `timeRefresh` ricevuto su index |

### Browser flow (`tests/e2e/asta-browser-flow.spec.js`)

Test “manuali” automatizzati — richiamabili con `npm run test:flow` o `./scripts/run-s6-tests.sh`.

| Test | Scenario |
|------|----------|
| setup admin | login admin.html, AGGIORNA lega, giocatori seed presenti |
| asta 1 opera come | admin avvia, GIOC1 rilancia, admin opera-come → vince GIOC1, conferma |
| asta 2 admin vince | tasti Auto visibili, conferma coerente su 2 browser |
| flusso completo | setup → 3 aste (opera come + dirette) → spesa finale coerente |

Helper condivisi: `tests/e2e/helpers/asta-helpers.js`

**Prerequisito E2E:** server `DEVTEMPLATE` su `:8080` + `POST /fantaasta/test/seed`.

## Checklist manuale residua

- [ ] Mobile reconnect dopo background
- [ ] Pausa / resume asta
- [ ] Azzera asta + reconnect
- [ ] Cestino admin espelle altro utente
- [ ] Import file .xls reale da admin (i flow usano seed giocatori)
