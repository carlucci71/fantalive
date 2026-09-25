# Analisi struttura asta — FantaAsta

> Modello corretto, stato attuale, gap funzionali.  
> Fuori scope: auth, login, sicurezza.  
> Collegato: [analisi-socket.md](./analisi-socket.md) · [analisi-connessioni.md](./analisi-connessioni.md)

**Data:** 2026-09-25

---

## 1. Modello corretto (target)

L’asta è una **macchina a stati** con una sola fonte di verità (server). Il client **mostra** lo stato, non lo decide.

### Stati logici

```
                    start
         ┌──────────────────────────────┐
         ▼                              │
      ┌──────┐   bid / timer reset   ┌─────────┐   timer scaduto   ┌──────────────┐
      │ IDLE │ ────────────────────► │ BIDDING │ ─────────────────► │ DA_CONFERMARE │
      └──────┘                       └─────────┘                     └──────┬───────┘
         ▲                              │  ▲                               │
         │                              │  │ pausa/riprendi                 │ conferma / annulla
         │         clearOfferta         │  └──────── PAUSA                   │
         └──────────────────────────────┴──────────────────────────────────┘
```

| Stato | Significato UI | Chi può fare cosa |
|-------|----------------|-------------------|
| **IDLE** | Lista calciatori, semaforo verde | Turno (o admin) avvia asta su calciatore selezionato |
| **BIDDING** | Barra timer, tasti rilancio | Tutti i partecipanti con budget/ruolo ok possono rilanciare |
| **DA_CONFERMARE** | `timeStart==3`, admin conferma/annulla/forza | Solo admin conferma o annulla |
| **PAUSA** | Timeout, timer fermo | Admin riprende |

### Invarianti da garantire

| ID | Invariante |
|----|------------|
| **A1** | Un solo `offertaVincente` attiva sul server |
| **A2** | Dopo conferma: DB aggiornato **e** stato asta resettato **e** tutti i client allineati |
| **A3** | Reconnect mid-asta: client riceve snapshot completo (non solo poll 1 Hz) |
| **A4** | `avviabili` / max rilancio coerenti con `mapSpesoTotale` server |
| **A5** | Timer UI (`contaTempo`, `timeStart`) derivato solo dal server durante BIDDING |
| **A6** | Nessun campo “vuoto” o default (es. `timeStart=0`) sovrascrive stato IDLE |

### Canale corretto per operazioni

| Operazione | Canale | Fonte di verità |
|------------|--------|-----------------|
| Avvia asta | WS `start` | Server |
| Rilancio | WS `inviaOfferta` | Server |
| Conferma acquisto | **Un solo** handler server (DB + reset stato + broadcast) | Server |
| Annulla | WS `annullaAsta` | Server |
| Timer tick | WS evento dedicato o `aggiorna` ridotto | Server |

---

## 2. Architettura attuale

### Server (`SocketHandler`)

Stato asta in campi in-memory:

- `offertaVincente`, `calInizioOfferta`, `sSemaforoAttivo`, `selCalciatoreMacroRuolo`
- `timeOut`, `millisFromPausa`, `giocatoreTimeout`

Broadcast:

- **Eventi** (start, inviaOfferta, confermaAsta, annullaAsta, …) → `invia()` immediato
- **Poll 1 Hz** `aggiorna()` → manda `contaTempo`, `timeStart`, `utenti`, `messaggi`, `offertaVincente` (se attiva), `sSemaforoAttivo`, …

### Client (`app.js` + `index.html`)

Stato duplicato su `$rootScope`:

- `offertaVincente`, `bSemaforoAttivo`, `timeStart`, `contaTempo`, `avviabili` (derivato), `timeout`

UI guidata da **combinazioni** di flag:

- Selezione calciatore: `bSemaforoAttivo && calciatori.length`
- Rilanci: `offertaVincente.nomegiocatore && timeStart < 3`
- Conferma admin: `timeStart == 3 && isAdmin`

### Flusso conferma (oggi) — problema strutturale

```
Admin click conferma
    │
    ├─► HTTP POST /confermaAsta  ──► salva Fantarose in DB
    │
    └─► WS confermaAsta (solo se HTTP OK) ──► server resetta offertaVincente + broadcast clearOfferta
```

Due step separati: se il WS fallisce o è in coda, **DB aggiornato ma asta ancora “aperta” sul server**.

### `/init` (HTTP)

Restituisce config, calciatori, `mapSpesoTotale`, turno — **non** include asta in corso (`offertaVincente`, timer, semaforo).

---

## 3. Problemi strutturali (solo asta)

### Critici per il funzionamento

| ID | Problema | Sintomo | Dove |
|----|----------|---------|------|
| **F1** | **Conferma HTTP + WS non atomica** | Giocatore in rosa ma UI ancora in asta; seconda conferma / doppio insert | `app.js` `conferma()`, `MyControllerAsta.confermaAsta`, `SocketHandler.confermaAsta` |
| **F2** | **`/init` senza snapshot asta** | Refresh/reconnect: niente offerta/timer fino al prossimo `aggiorna` o evento | `MyControllerAsta.init()` |
| **F3** | **`aggiorna()` sovrascrive stato client** | `timeStart=0` ogni secondo anche senza asta; conflitti con `clearOfferta` (`timeStart=-1`) | `SocketHandler.aggiorna()`, `app.js` `getMessaggio` |
| **F4** | **Client muta stato asta in locale** | `inizia()` / `conferma()` / `annulla()` impostano `bSemaforoAttivo` prima dell’ack server | `app.js` |
| **F5** | **Logica duplicata client/server** | Mobile senza tasti; rilanci rifiutati lato client ma accettati server (o viceversa) | `testRilancia`, `avviabili`, `inviaOfferta` |

### Medi (instabilità / confusione)

| ID | Problema | Dettaglio |
|----|----------|-----------|
| **F6** | `bSemaforoAttivo` usato per troppe cose | Selezione calciatore, link Admin, visibilità sezioni — un flag, più significati |
| **F7** | `timeStart` è fase timer **e** switch UI | 0–2 = rilancia, 3 = conferma; inviato anche a idle |
| **F8** | `liberaSemaforo` senza broadcast evento | Server aggiorna `sSemaforoAttivo` ma non `invia()`; UI admin disabilitata (`ng-show="false"`) |
| **F9** | `inviaOfferta` può fare doppio broadcast | Reset timer + offerta in due messaggi ravvicinati |
| **F10** | `messaggi` + `utenti` nel poll 1 Hz | Traffico e patch parziali non necessari per l’asta |

---

## 4. Design target (fase S4 — asta)

### Server: `AstaState` (concetto)

Un servizio con:

```java
enum FaseAsta { IDLE, BIDDING, DA_CONFERMARE, PAUSA }

class AstaSnapshot {
  FaseAsta fase;
  Map offertaVincente;      // null se IDLE
  long contaTempoMs;
  int timePhase;            // 0-3, solo se BIDDING o DA_CONFERMARE
  boolean semaforoVerde;     // può avviare nuova asta
  String selCalciatoreMacroRuolo;
  // ...
}
```

Ogni operazione:

1. Valida sullo snapshot
2. Aggiorna stato
3. Emette **un** evento WS con snapshot completo asta (o delta versionato)

### Conferma unificata

```
POST /confermaAsta  (o WS unico)
  → valida offerta server
  → salva DB
  → reset AstaState → IDLE
  → broadcast AstaConfirmed { snapshot, calciatori, mapSpesoTotale, turno, ... }
```

Niente secondo messaggio WS dal client.

### `aggiorna()` ridotto

Solo durante `BIDDING`:

- `contaTempo`, `timePhase` (ex timeStart)
- Niente `messaggi`, `utenti`, `offertaVincente` (già inviati sugli eventi)

A 1 Hz o solo se `calInizioOfferta != null`.

### Client: rendering da `faseAsta`

```javascript
// invece di: offertaVincente && timeStart < 3 && bSemaforoAttivo && ...
ng-show="faseAsta === 'BIDDING'"
ng-show="faseAsta === 'IDLE'"
ng-show="faseAsta === 'DA_CONFERMARE' && isAdmin"
```

Niente scritture locali su `bSemaforoAttivo` / `timeStart` salvo ottimistic UI esplicita con rollback.

### `/init` arricchito

Se asta in corso, includere `astaSnapshot` uguale al payload evento WS.

---

## 5. Interventi consigliati (ordine, solo asta)

| Priorità | Intervento | Stato |
|----------|------------|-------|
| **1** | Conferma atomica server (HTTP + broadcast) | ✅ 2026-09-25 |
| **2** | `aggiorna`: `timeStart`/`contaTempo` solo con asta attiva | ✅ 2026-09-25 |
| **3** | `/init` con `astaSnapshot` | ✅ 2026-09-25 |
| **4** | Client: no mutazioni locali semaforo/timer in inizia/conferma/annulla | ✅ 2026-09-25 |
| **5** | `faseAsta` su eventi server + `applyAstaSnapshot` | ✅ 2026-09-25 |
| **6** | Poll solo timer; eventi per messaggi/utenti | Parziale (timer ok, utenti ancora in poll) | S4 |
| **7** | UI `ng-show` da `faseAsta` invece di combo flag | Da fare | S4 + HTML |

---

## 6. Checklist test asta (funzionale)

1. Avvia asta → tutti vedono calciatore e timer
2. Rilanci PC + mobile → offerta e timer allineati
3. Timer scade → `timeStart==3`, tasti rilancio spariscono, admin vede conferma
4. Conferma → giocatore in rosa, lista liberi aggiornata, **subito** nuova selezione calciatore (IDLE)
5. Seconda asta senza refresh → tasti rilancio visibili su mobile
6. Refresh mid-asta → stato ripristinato da `/init` o primo evento
7. Annulla → torna IDLE senza insert DB
8. Pausa / riprendi → timer coerente su tutti i client

---

## 7. Verifica post-implementazione (2026-09-25)

### Corretto ✅

| Area | Verifica |
|------|----------|
| Conferma atomica | HTTP salva DB + `confirmAstaAndBroadcast`; client non invia più WS |
| `/init` | `astaSnapshot` con fase, offerta, timer, semaforo |
| Poll timer | `timeStart`/`contaTempo` solo con asta attiva e timer valido |
| `faseAsta` | Su start, bid, pausa, resume, conferma, annulla |
| Client | Niente mutazioni locali semaforo/timer in inizia/conferma/annulla |

### Bug trovati in review e corretti

| Bug | Fix |
|-----|-----|
| `if (msg.contaTempo)` falsy su `0` → timer non resettato al rilancio | `!= null` |
| `start` non resettava `timeOut`/pausa | Reset esplicito |
| `pausa`/`resume` senza `faseAsta` | Aggiunto su broadcast |
| `applyAstaSnapshot` IDLE senza `ricalcolaAvviabili` | Aggiunto |
| Broadcast fallito dopo save DB | HTTP ritorna KO con messaggio |

### Residui noti (non bloccanti)

| ID | Problema |
|----|----------|
| R1 | `aggiorna` manda ancora `utenti`/`messaggi` ogni 1s (S4) |
| R2 | UI usa ancora `timeStart`+`bSemaforoAttivo` oltre a `faseAsta` |
| R3 | Race teorica `aggiorna` vs `confirmAsta` (non sincronizzati) |
| R4 | Sezione rilancio visibile in pausa (`timeStart<3` senza check `timeout`) — pre-esistente |

## 8. Secondo audit struttura/funzionalità (2026-09-25)

### Valutazione complessiva

| Area | Voto | Note |
|------|------|------|
| Modello server (faseAsta + snapshot) | ✅ Buono | Fonte di verità chiara |
| Conferma atomica | ✅ Buono | HTTP → DB + broadcast |
| Reconnect `/init` | ✅ Buono | `astaSnapshot` ripristina asta |
| Poll timer | ✅ Buono | Solo con asta attiva |
| Client `applyAstaSnapshot` | ⚠️ Parziale | `getMessaggio` duplica logica, non delega |
| UI HTML | ⚠️ Parziale | Usa ancora `timeStart`/`bSemaforoAttivo`, ignora `faseAsta` |
| Operazioni admin secondarie | ❌ Lacune | `azzera`, `terminaAsta`, `riapri`, `azzeraTempo` |

### Flusso funzionale per fase

```
IDLE ──start──► BIDDING ──timer──► DA_CONFERMARE ──conferma HTTP──► IDLE
  ▲                │                      │
  │                ├──pausa──► PAUSA      ├──annulla──► IDLE
  │                │                      └──forza──► (poi conferma)
  └────────────────┴── resume ─────────────┘
```

**Copertura `faseAsta` su server:**

| Operazione | faseAsta | Completo |
|------------|----------|----------|
| start | BIDDING | ✅ |
| inviaOfferta (ok) | BIDDING | ✅ |
| aggiorna (timer) | BIDDING / DA_CONFERMARE | ✅ |
| pausaAsta | PAUSA | ✅ |
| resumeAsta | BIDDING | ✅ |
| conferma HTTP/WS | IDLE | ✅ |
| annullaAsta | IDLE | ✅ |
| terminaAsta | — | ❌ manca |
| riapri | — | ❌ manca |
| forza | — | ❌ manca |
| azzeraTempo | — | ❌ manca |
| azzera (WS) | — | ❌ manca reset stato asta |

### Problemi per gravità

#### Alta (funzionalità asta compromessa)

| ID | Problema | Sintomo | Dove |
|----|----------|---------|------|
| **A1** | `azzera` WS non resetta stato asta server | Dopo "AZZERA TUTTO" il poll continua a mandare `offertaVincente` fantasma | `SocketHandler` op `azzera` |
| **A2** | `terminaAsta` senza `faseAsta`/`timeStart=3` | Stop anticipato: timer fermo ma fase client incoerente, rilanci ancora possibili | `SocketHandler` `terminaAsta` |
| **A3** | UI rilanci in pausa | `timeStart<3` senza `!timeout` → tasti visibili con asta in pausa | `index.html` ~254 |

#### Media (incoerenze / debito)

| ID | Problema | Dettaglio |
|----|----------|-----------|
| **M1** | Client doppio modello | `faseAsta` aggiornato ma UI legge `timeStart` + `bSemaforoAttivo` |
| **M2** | `getMessaggio` non usa `applyAstaSnapshot` | ~150 righe patch ordine-dipendenti; rischio drift |
| **M3** | `riapri` incompleto | Reset timer server ma broadcast senza `faseAsta`, `timeStart`, `contaTempo` |
| **M4** | `azzeraTempo` senza broadcast timer | Client non resetta barra finché non arriva poll |
| **M5** | `inviaOfferta` doppio `invia()` | m2 (timer) + m (offerta) = 2 broadcast/rilancio |
| **M6** | Race `aggiorna` vs `confirmAsta` | Thread scheduler vs WS; `confirm` synchronized, `aggiorna` no |
| **M7** | `testRilancia` 1500ms client | Server accetta/rifiuta con regole diverse → disallineamento mobile |
| **M8** | Conferma HTTP: DB ok, broadcast KO | Ritorna errore ma giocatore già in rosa |

#### Bassa (S4 / cleanup)

| ID | Problema |
|----|----------|
| **B1** | Poll manda `utenti`/`messaggi`/`offertaVincente` ogni 1s |
| **B2** | `$rootScope.$apply()` su ogni WS message |
| **B3** | `liberaSemaforo` UI disabilitata (`ng-show="false"`) |
| **B4** | `sSemaforoAttivo` null a cold start server fino al primo evento |

### Cosa funziona (evidenze codice)

1. **Conferma**: `MyControllerAsta.confermaAsta` → save → `confirmAstaAndBroadcast` — un solo percorso effettivo dal client admin.
2. **Reconnect**: `/init` include `astaSnapshot` con offerta, timer, pausa.
3. **Timer rilancio**: `contaTempo != null` (fix 0 falsy); reset timer su bid via m2.
4. **Seconda asta**: `clearOfferta` + `faseAsta IDLE` + no `timeStart` a idle nel poll.
5. **Start pulito**: reset pausa/timeout su nuova asta.

### Priorità interventi consigliati

1. **A1** — reset stato asta in `azzera` WS (5 righe)
2. **A2** — `terminaAsta` → `faseAsta=DA_CONFERMARE`, `timeStart=3`
3. **A3** — `ng-show` rilanci: aggiungere `&& !timeout` o `faseAsta==='BIDDING'`
4. **M2** — `getMessaggio` delega campi asta a `applyAstaSnapshot` (refactor)
5. **M1** — UI da `faseAsta` (refactor HTML, S4)

### Checklist test aggiornata

- [ ] Asta normale PC + mobile
- [ ] Seconda asta senza refresh
- [ ] Refresh mid-asta
- [ ] Pausa / resume (rilanci **non** visibili in pausa)
- [ ] Termina asta anticipo → solo conferma admin
- [ ] AZZERA TUTTO durante asta → stato IDLE su tutti
- [ ] Conferma → rosa + IDLE immediato

## 9. Prossimo passo

Fix A1–A3 → test checklist §8 → refactor M1/M2 (UI `faseAsta`) → S4.
