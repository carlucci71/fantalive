# Roadmap sistemazione FantaLive

> Ultimo aggiornamento: 2026-10-01 — ?mobile=1: UI portrait + gabbia 390px; overlay ruota solo UA reale (ui182).
> **Iniziativa attiva:** S14 UI/UX FantaAsta — rifinitura mobile verticale  
> **Prossimo passo:** conferma ?mobile=1 vs PC senza param

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

### Note recenti (2026-10-01)

- **S14 home ui182:** `?mobile=1` — `isMobilePortraitUi()` sempre true + gabbia body 390px (debug telefono su PC); overlay ruota solo UA mobile reale.
- **S14 home ui181:** `?mobile=1` → `isMobilePortraitUi()` sempre true (layout mobile/tab bar anche su finestra landscape); overlay ruota resta solo UA telefono.
- **S14 home ui180:** overlay «Solo verticale» solo se UA telefono/tablet; `?mobile=1` forza layout mobile ma non più l’overlay (restava attivo in Cursor via localStorage da test precedenti).
- **S14 home ui179:** overlay «Solo verticale» / `fa-ui-mobile-land` solo se dispositivo mobile (UA/touch) o `?mobile=1`; rimosso trigger CSS `orientation+max-height:560` che scattava ridimensionando il browser PC.
- **S14 home ui178:** admin — click su giocatore seleziona sempre (anche fuori turno); non va più in preferito. `puoSelezionareGiocatoreIdle` torna `true` per admin; Opera come resta solo per abilitare Avvia.
- **S14 home ui177:** admin su PC/web — in IDLE lo split a 3 (giocatore | live | Opera come) è sempre visibile, anche senza giocatore selezionato (prima solo su mobile). Codice remoto era già allineato: differenza di viewport.
- **S14 home ui176:** login — un solo `connessioneKO` (niente doppio banner globale+card); dopo fine sessione riapre WS anonimo e azzera il messaggio a socket pronto (`$applyAsync`); PC + telefono.

### Note recenti (2026-09-28)

- **S14 home ui166:** tab Giocatori — lista sempre attiva; fuori turno click riga = toggle preferito (come stella), non selezione asta; tolto `is-locked`.
- **S14 home ui165:** tab Giocatori — tasto **Reset** filtri a sinistra della ★ (nome/ruolo/squadra/preferiti/sort); attivo solo se filtri modificati; PC+mobile.
- **S14 home ui164:** fix scroll tab Giocatori — catena flex `pane → panel → body → .fa-player-list` con `min-height:0` + `overflow-y:auto` (mobile + force-mobile).
- **S14 home ui163:** Giocatori — fuori turno niente selezione in asta (PC+mobile, eccezione Assegna); icona martello a sinistra di prezzo/stella in riga unica; riga `is-locked` attenuata.
- **S14 home ui162:** fix ricerca Squadre — `ng-model` su `\$root.teamsSearchQ` (lo scope di `ng-include` non scriveva su root → 0 risultati).
- **S14 home ui161:** tab Squadre — searchbar fissa (non scrolla) per nome giocatore acquistato o squadra; next/prev + highlight + scroll al match; Enter/Shift+Enter/Esc.
- **S14 home ui160:** tab Squadre mobile — tutte le tabelle rosa complete (niente clip interno); griglia **2 / 3 / 4** colonne (`<520` / `520–719` / `≥720`); desktop invariato.
- **S14 home ui159:** badge tab — solo **Squadre** (`has-new`) dopo conferma asta o Assegna; Giocatori senza badge; consuma aprendo Squadre.
- **S14 home ui158:** Assegna senza giocatore — niente alert: apre tab Giocatori (`pendingAssegna`); dopo la scelta torna ad Asta e apre il form Assegna.
- **S14 home ui157:** fix Assegna — i poll/snapshot IDLE non azzerano più `abilitaForza` se già in IDLE (il tasto sembrava non fare nulla).
- **S14 home ui156:** admin Assegna IDLE — click Assegna = navigazione a pieno riquadro (spariscono tasti Opera come); Indietro torna alla griglia utenti.
- **S14 home ui155:** admin Assegna — input/select a tutta larghezza e altezza della colonna campi (come Conferma/Indietro).
- **S14 home ui154:** admin Assegna (IDLE e fine asta) — stesso layout a 3 blocchi orizzontali: campi (cifre+squadra) | Conferma | Indietro; riempie lo slot admin.
- **S14 home ui153:** admin DA_CONFERMARE — Conferma / Annulla / Assegna / Riavvia asta in griglia 2×2 che riempie lo slot admin (stessa logica celle uguali dei tasti utente).
- **S14 home ui152:** admin — pannello centrale asta (`.fa-auction-split__live`) centrato in altezza come i non-admin (`center` / `safe center`), IDLE e live-phase; media query + `fa-force-mobile`.
- **S14 home ui151:** fine asta (qualsiasi esito) / cambio turno / cambio Opera come — `pulisciSelezioneSeNonDiTurno`: se chi opera non può fare l'asta, toglie il nome giocatore (niente sticky dopo la prima asta).
- **S14 home ui150:** admin mobile — click Opera come su utente di turno mostra il tasto Seleziona nel pannello Asta (niente salto al tab Giocatori); se Opera come ≠ turno resta Attesa.
- **S14 home ui149:** admin mobile — come su PC può sempre selezionare un giocatore; click su utente di turno (Opera come) apre tab Giocatori se IDLE senza selezione; hint «puoi scegliere al suo posto».
- **S14 home ui148:** non-admin — pannello sotto (`.fa-auction-split__live`) di nuovo centrato in altezza (`justify-content: center` / `safe center`); admin resta `flex-start`. Media query + `fa-force-mobile`.
- **S14 home ui147:** mobile non-admin — Asta live sempre **2 metà uguali** (`flex: 1 1 0`, admin con `ng-if` fuori dal DOM); divider più visibile; admin resta 1:1:1. Verificato browser `?mobile=1` come GIOC2 (IDLE + giocatore selezionato).
- **S14 home ui126/127:** fix telefono reale — `isMobilePortraitUi` e CSS mobile usano solo `max-width: 899px` (niente `orientation: portrait`, inaffidabile); tasto giallo “scegli giocatore” anche nello empty; ritorno ad Asta dopo selezione allineato.
- **S14 home ui118:** mobile admin — Asta live sempre 3 sezioni 1:1:1 (anche senza giocatore); larghezza piena (tolto cage 390px che restringeva il pannello a ~200px); desktop invariato.
- **S14 home ui117:** mobile portrait Asta live — altezza piena viewport, 3 sezioni `flex: 1 1 0` (1:1:1), niente scroll pagina; desktop invariato.
- **S14 home ui116:** mobile portrait — pannello Asta live con le stesse sezioni (giocatore → live/CTA → admin) in **colonna**; desktop row invariato; anche su `?mobile=1`.
- **Vincolo UI:** prossime modifiche **solo mobile portrait** (`max-width: 899px` / `orientation: portrait`); landscape dopo; **non rompere layout PC** (≥900px). Test con `?mobile=1`.
- **S14 home ui112:** force mobile persistente via `?mobile=1` (localStorage + colonna 390px) perché l’emulazione CDP del browser Cursor si resetta a fine turno; `?mobile=0` per tornare desktop.
- **S14 home ui110:** fix `$rootScope:infdig` — cache `rosaSlotPerSquadra` / `adminElencoImpersonazione` (niente array nuovi in `ng-repeat`); `teamStatusBadgeClass` e `settingsSteps` stabili; riga wizard admin (`.is-admin`) allineata a card “mia squadra” (`--me-surface`).
- **Fix Salva squadre:** `Configurazione.toString()` con `nomeLega` faceva sforare `logger_messaggi.messaggio` (255) → 500 su `aggiornaConfigLega`; truncamento in `AstaMessageLogService` + log config corto.
- **S14 home ui109:** `nomeLega` salvato in `Configurazione` (+ localStorage), inviato in `inizializzaLega`/`aggiornaConfigLega`, ripristinato da `/init`; brand toolbar allineato; cache `?v=20250925ui109`.
- **S14 home ui106:** tab Offerte — header tabella in cima (niente Aggiorna); refresh a ogni click Offerte; popola da `giocatoriPerSquadra` + `/elencoCronologiaOfferte`; cache `?v=20250925ui106`.
- **S14 home ui104:** tab Offerte nel drawer ops allineato a cronologia legacy (ruolo/id/giocatore/squadra/costo/allenatore/ora + Cancella admin); cache `?v=20250925ui104`.
- **S14 home ui103:** wizard Nome lega legato a `$root.nomeLega` → brand toolbar aggiornato in live (fix scope `ng-include`); cache `?v=20250925ui103`.
- **S14 home ui102:** menu utente admin — **Esporta** sotto Opzioni → Classic `./esporta`, Mantra `./esportaMantra`; cache `?v=20250925ui102`.
- **S14 home ui101:** refresh con creazione incompleta (squadre senza quotazioni) azzera la lega, cancella sessione e torna al **login** (riparte da capo); cache `?v=20250925ui101`.
- **S14 home ui100:** creazione/salvataggio squadre non forza più uppercase («Admin» resta Admin); unicità case-insensitive; cache `?v=20250925ui100`.
- **S14 home ui99:** Admin IDLE — **Assegna** (stesso icona/flusso Conferma+Indietro) assegna giocatore+cifra senza asta via `/assegnaGiocatore`; cache `?v=20250925ui99`.
- **S14 home ui98:** Admin BIDDING — Pausa|Termina fissi in footer orizzontale; scroll solo su Opera come; cache `?v=20250925ui98`.
- **S14 home ui97:** Admin fine asta — Conferma / Annulla / Assegna / Riapri asta; in Assegna solo Conferma + Indietro; cache `?v=20250925ui97`.
- **S14 home ui96:** Admin fine asta — Conferma / Annulla / **Assegna**; in Assegna: cifre+squadra + Conferma (applica e chiude) / Annulla / Riapri asta; rimossi Forza e «Forza e conferma»; cache `?v=20250925ui96`.
- **S14 home ui95:** pannello Admin — un solo `overflow-y` sulla colonna (niente doppio scroll body/foot); cache `?v=20250925ui95`.
- **S14 home ui94:** Admin Forza — form in body (non taglia i tasti); fix 2° click (`chiudiForza` su `$root`, niente `abilitaForza=false` in template); «+ Conf» → **Forza e conferma**; cache `?v=20250925ui94`.
- **S14 home ui93:** «Stai vincendo» / «Ti sei aggiudicato» solo se vince la propria squadra admin; con Opera come su altro utente resta «X sta vincendo»; cache `?v=20250925ui93`.
- **S14 home ui92:** banner asta personalizzato — se sei in testa: «NOME Stai vincendo l'asta!»; se hai vinto: «NOME Ti sei aggiudicato il giocatore X!»; gli altri restano in terza persona; cache `?v=20250925ui92`.
- **S14 home ui91:** preferiti — click stella in lista toggla preferito (`addFav`, stopPropagation, evidenzia `is-on`); filtro ★ in header invariato; cast robusto server; cache `?v=20250925ui91`.
- **S14 home ui90:** fix modale Taglia invisibile — overlay era `position:static` sotto la board; ora `position:fixed` a schermo intero con backdrop; cache `?v=20250925ui90`.
- **S14 home ui89:** fix «Vai in home» — dopo upload il wizard restava a schermo (`ng-if` leave appeso: ngAnimate aspettava `transition` su `.fa-btn` figli); `$animateProvider.classNameFilter` + CSS leave istantaneo; cache `?v=20250925ui89`.
- **S14 home ui88:** taglio acquisto da Squadre — click admin su giocatore in rosa → modale dettaglio + **Taglia** (API legacy `cancellaOfferta`, restituisce crediti); `idGiocatore`/`idAllenatore` in `giocatoriPerSquadra`; slot rosa un po’ più grandi; cache `?v=20250925ui88`.
- **S14 home ui84:** card Squadre — slot rosa placeholder (P×maxP, D×maxD…) al posto di «Nessun acquisto»; slot vuoti attenuati, pieni con nome+costo; stesso su mobile; cache `?v=20250925ui84`.
- **S14 home ui83:** “mia squadra” = sfondo sobrio `--me-surface` (Opera come, Squadre, avatar) senza bordo dorato; turno resta bordo teal; cache `?v=20250925ui83`.
- **S14 home ui80:** bordi pulsanti Admin «Opera come» allineati alle team card — `is-me` (arancio) e `is-turn` (teal) come `.fa-team-card`; cache `?v=20250925ui80`.
- **S14 home ui79:** pannello Admin IDLE+BIDDING unificato — selettore **Opera come** (tutte le squadre, admin per primo); sticky tra aste (non reset in `resetStatoAstaLocale`); default se stesso al login/refresh; centro mostra «Avvia per NOME» solo se la selezione può partire, altrimenti attesa turno come gli altri; cache `?v=20250925ui79`.
- **S14 home ui78:** fix login/refresh coerente — admin senza quotazioni → upload (non home); hard refresh senza sessione → login; con sessione+quotazioni → home; `calcolaIsAdmin` dopo login + watch `idgiocatore` (preserva `isAdminBootstrap`); `needsWizard` usa DB come fonte di verità; E2E wizard 6/6; browser verificato; cache `?v=20250925ui78`.
- **S14 home ui77:** fix hard refresh — non si ripristina più il login da `localStorage` senza sessione HTTP (`giocatoreLoggato`); flag setup completato azzerato se DB senza quotazioni; `canSkipToUploadStep` non usa più `setup-complete` obsoleto; cache `?v=20250925ui77`.
- **S14 home ui76:** footer Opzioni — «Vai in home»/«Chiudi» a sinistra, «Indietro» + «Avanti»/«Salva» a destra; creazione lega invariata; cache `?v=20250925ui76`.
- **S14 home ui75:** fix step upload creazione — «Vai in home» disabilitato finché non c'è upload (`uploadStepAllowsHome`: richiede `wizardUploadCount` durante `setupInProgress`); stili CSS `:disabled` sui pulsanti; E2E dedicato; cache `?v=20250925ui75`.
- **S14 home ui74:** footer wizard su una riga (nav sinistra + azioni destra); step Quotazioni senza import → «Vai in home» disabilitato + «Chiudi»; pannello Admin visibile in IDLE con giocatore selezionato (avvio asta per squadra, come legacy); cache `?v=20250925ui74`.

### Note recenti (2026-09-27)

- **S14 home ui73:** Opzioni con quotazioni già caricate — step 5 mostra Reset import + stato «Quotazioni già caricate»; «Vai in home» al posto di «Chiudi»; footer wizard con stato unico (`wizardFooter`) e transizioni CSS senza flash Salva/Avanti; cache `?v=20250925ui73`.
- **S14 home ui72:** fix durata asta dopo creazione lega — `syncWizardConfigInputsFromDom()` prima di `inizializzaLega`/`aggiornaConfigLega` (valori step 2 letti dal DOM anche se nascosti); Opzioni non sovrascrive più lo scope con input DOM obsoleti (`captureSettingsSnapshot(true)` + `syncSettingsDomFromScope` dopo reload); E2E `wizard-league-flow` durata 13→Opzioni; cache `?v=20250925ui72`.
- **S14 home ui71:** fix wizard creazione — se admin cambia giocatore (es. GIOC0→GIOC3) su step 4, dopo «Salva squadre» la sessione HTTP/WS si riallinea al nuovo admin (`sessioneAllenatore` da `aggiornaConfigLega`, `applicaCambioSessioneAdmin` + `riconnettiComeAllenatore`); wizard prosegue a step 5 upload; E2E `wizard-league-flow` test dedicato; cache `?v=20250925ui71`.
- **S14 home ui70:** UX pulsanti Opzioni — senza modifiche: **Avanti** + **Chiudi**; con modifiche: **Salva** + **Annulla** (ripristina step); navigazione step/indietro bloccata finché dirty; Salva resta sullo step corrente (non chiude).
- **S14 home ui69:** fix salvataggio Opzioni — `syncSettingsInputsFromDom()` prima del save (valori input sempre letti); reload da `/init` all'apertura/chiusura; **Salva** su ogni step; step **Quotazioni** (upload/reset) visibile anche in modifica lega; **Indietro** nascosto sul primo step Opzioni; E2E `opzioni-settings-flow.spec.js` + browser verificato (durata 21 persiste).
- **S14 home ui68:** pannello Opzioni rivisto — step 2-4 (Regole/Rosa/Squadre) navigabili con indicatori cliccabili; salvataggio su cambio step se dirty (best practice: non ad ogni keystroke); ultimo step con **Salva** + **Annulla** (conferma se modifiche); rimossi Chiudi/Fine e step upload in modalità impostazioni; fix `wizardNext` step 4 che chiamava `wizardSaveUsers` anche in Opzioni; E2E 7/7 (`config-persistenza` + `durata-asta-settings` incluso durata 21).
- **S14 home ui67:** audit persistenza configurazione — tutti i campi numerici/flag (`budget`, `durataAsta`, `isATurni`, `isSingle`, `isMantra`, max/min P/D/C/A, `numAcquisti`, nomi squadra, admin, ordine) salvati su DB via `aggiornaConfigLega`; fix `isMantra` (client inviava flag, server non chiamava `setIsMantra`/`configurazione.setMantra`); `syncConfigFromDb()` su save e avvio asta; E2E `config-persistenza.spec.js` (4 test) + `durata-asta-settings.spec.js` (2 test) = 6/6 verdi; cache `?v=20250925ui67`. **Non persistito:** `nomeLega` (solo UI), `numeroUtenti` (solo creazione lega).
- **S14 home ui66:** durata asta — verificato con E2E `durata-asta-settings.spec.js` (Opzioni Chiudi + admin AGGIORNA → timer 20s); server `syncDurataAstaFromDb()` su avvio asta; log `[CONFIG]`/`[WS] start asta durataAsta`; admin.html cache ui66; alert se non-admin tenta salvataggio.
- **S14 home ui65:** fix durata asta — Opzioni salvava solo con «Fine» step 5; ora Chiudi/Avanti/Fine chiamano `aggiornaConfigLega`; server aveva ancora 15 (init confermato); rimosso sync client-only in settings che mascherava il valore reale.
- **S14 home ui64:** fix wizard dopo refresh — se lega/squadre già su server (o setup completato in localStorage) salta step 4 “Salva squadre” → step 5 upload o home se quotazioni presenti; `applyInitData` con calciatori va sempre in home.
- **S14 home ui63:** fix durata asta — `aggiornaConfigLega` usa `toInt` (JSON non-Integer); server include `durataAsta` in avvio/timer WS; client `applyDurataAstaFromServer` + watch impostazioni; post-conferma deseleziona giocatore venduto e mostra “Prossimo turno”; banner vincitore in BIDDING/DA_CONFERMARE; E2E asta 6/6 verdi.
- **S14 home ui52:** audit wizard — feedback “Creazione squadre…”/“Salvataggio…” su step 3-4; pulizia post-upload; DB resettato per test pulito.
- **S14 home ui51:** fix salto wizard step 4→5 — `wizardSoloUpload` solo dopo “Salva squadre”; `setupInProgress` non sovrascritto da `syncWizardStepAfterInit`; carosello nomi squadra step 4.
- **S14 home ui50:** fix typo Java `setIsMantra(toBool(...))` in `inizializzaLega` — wizard bloccato su “Crea squadre” (500); client mostra errore se fallisce.
- **S14 home ui49:** fix `POST /azzera` — NPE se configurazione assente (stato DA_CONFIGURARE); client con gestione errore e reload pulito dopo cancellazione.
- **S14 home ui48:** admin con lega già creata ma senza quotazioni → wizard solo step 5 (“Completa configurazione”); pulsante **Cancella lega** in alto a destra con conferma; fix `caricaFile` (digest Angular + reload post-upload); `setupCompletato` corretto dopo import giocatori.
- **S14 home ui47:** fix critico `applyAstaSnapshot` — i broadcast parziali (timer/pausa/resume) non azzerano più `offertaVincente`; `isAstaInPausa()` per UI pausa; E2E pausa/riprendi + annulla→nuova asta verdi (6 test).
- **S14 home ui46:** re-login pulito — `resetStatoAstaLocale` su logout/scadenza; `ricalcolaStatoSessione` dopo `doConnect` applica `astaSnapshot` server (timer, fase, offerta); toolbar status solo con sessione attiva.
- **S14 home ui45:** fix limbo sessione — alla scadenza HTTP/WS (`giocatoreLoggato` assente, `utentiScaduti`, errore sessione) `terminaSessioneCliente` pulisce stato e mostra login; toolbar sempre visibile; `showLoginScreen` non bypassa più con asta live; board solo con `nomegiocatore` valido.

### Note recenti (2026-09-26)

- **S14 home ui38:** asta live — card unificata (player header, metriche 3 colonne Offerta/In testa/Timer, bid strip segmentata, action row conferma griglia); empty state con icona; CTA “Avvia asta” con label.
- **S14 home ui37:** pannello asta — layout centrato (nome+martello, riga orizzontale offerta+vincitore+timer anello), pillole compatte conferma/admin; squadre — martelletto turno 36px con pulse, nomi ellipsis; fix `fa-btn--compact width:100%` che sballava i tasti asta.
- **S14 home ui36:** filtri giocatori su `$root`, martello su riga, asta orizzontale, fix toolbar `nomeUtenteAttivo()`.

### Note recenti (2026-09-25)

- **S14 home ui29:** grid asta/giocatori 48% vs squadre 52%; card squadre con badge qualità ping (Ottima/Buona/Lenta/Scaduta) al posto avatar, footer budget fisso; `entraCome` aggiorna `utenti` post-connect.
- **S14 home ui28:** fix `fa-desktop-only` che bloccava flex su pannello squadre → card full-height.
- **Login squadre (fix ui18):** `login-screen.html` è in `ng-include` → scope figlio: il select aggiornava `loginUtenteSelezionatoId` locale ma `entraCome` leggeva `$rootScope` (sempre id 0 / WWWW). Fix: `ng-model="$root.loginUtenteSelezionatoId"`. Test browser: PIPPO admin, GIOC3SS id 3.
- **Login squadre (fix ui17):** `ng-options` su id primitivo invece di oggetto allenatore (prerequisito al fix ui18).
- **Login squadre (fix precedente):** `nomeSquadra` preferiva `nome` (GIOC*) invece di `nuovoNome` dopo rename wizard; `wizardSaveUsers` ora ricarica da server; `wizardCreateLega` verifica count richiesti vs creati; `aggiornaConfigLega` NPE su pwd null; `/init` espone `numeroGiocatori` + alert login se mismatch. Se restano 3 GIOC*: DB ha 3 record (seed E2E o lega non ricreata) — `curl …/init`.
- **S14 flusso navigazione:** `index.html` → login (admin bootstrap se `DA_CONFIGURARE`) → wizard (step 3 crea N squadre, step 4 salva nomi, step 5 upload → home); niente redirect obbligatorio a `admin.html`.
- **Reset dev:** `POST /fantaasta/test/reset` (profilo DEVTEMPLATE) → DB vuoto `DA_CONFIGURARE`, disconnect WS; alternativa restart server (H2 in-memory).
- **Fix wizard post-login:** `applyInitData` non azzera più `setupInProgress` se ci sono calciatori (seed/E2E); wizard resta visibile fino a `wizardCompleteSetup`.
- **Fix infdig:** cache `acquistiPerSquadra` al posto di `getAcquistiSquadra()` in `ng-repeat`.
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
| 14 | UI/UX FantaAsta (single-page, wizard, asta mobile) | Frontend | Media | — | Completata (core) → [piano-s14-ui.md](./piano-s14-ui.md) |

---

## Ordine consigliato

### Ora (post-socket)
**#14 / S14** UI FantaAsta — implementazione core completata; [piano-s14-ui.md](./piano-s14-ui.md)

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
