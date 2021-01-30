# Descrizione
Fantaasta è una applicazione per gestire, in modalità online, le asta del fantacalcio. Funziona sia in modalità singolo utente ("amministratore") che con accessi multipli. Sono previsti due stile di presentazioni una per schermi fino a 980px ed una per schermi maggiori.

# Configurazione iniziale
Funziona sia con database MySql/Postgres che H2 in memory (in questo caso tutte le informazioni si perdono al riavvio del programma, anche se esiste una modalità di recupero da file che verrà spiegata in seguito). 

Per creare un database ed un utente su mysql si può usare lo script seguente:
 	
~~~~
CREATE USER 'asta'@'%' IDENTIFIED by 'asta';
GRANT ALL PRIVILEGES ON *.* TO 'asta'@'%' REQUIRE NONE WITH GRANT OPTION MAX_QUERIES_PER_HOUR 0 MAX_CONNECTIONS_PER_HOUR 0 MAX_UPDATES_PER_HOUR 0 MAX_USER_CONNECTIONS 0;GRANT ALL PRIVILEGES ON asta.* TO 'asta'@'%';
CREATE DATABASE asta;
~~~~

Nel file `application.properties` configurare opportunamente le seguenti chiavi (ci sono già dei valori di esempio da scommentare):
~~~~
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
~~~~

Se il database è h2 o la prima esecuzione di mysql/postgres configurare le chiavi:
`application.properties`
~~~~
spring.jpa.hibernate.ddl-auto=create
~~~~
`spy.properties`
~~~~
append = false
~~~~

Se il database è mysql/postgres, dalla seconda esecuzione cambiare i valori in (se si vuole partire dai dati salvati in precedenza senza perdere tutto ad ogni esecuzione):
~~~~
spring.jpa.hibernate.ddl-auto=update
~~~~
`spy.properties`
~~~~
append = true
~~~~
E' possibile configurare la porta modificando la chiave:
`application.properties`
~~~~
server.port=8081
~~~~
E' possibile **proteggere** l'accesso alla pagina principale abilitando la chiave:
`application.properties`
~~~~
spring.profiles.active=protetto
~~~~
In tal caso, verrà richiesto di digitare il nome della lega e si accederà alle pagine successive solo se corrisponde alla chiave:
`application.properties`
~~~~
security.user.name
~~~~
 il cui valore di default è `mialega`



# Esecuzione dell'applicazione
L'applicazione si può avviare con il seguente comando, dalla root del progetto:
`mvn spring-boot:run`

# Attività iniziali dopo il primo accesso
Al primo accesso verrà chiesto di inserire i seguenti valori:
* Modalità Mantra o classica
* Numero utenti
* Budget
* Secondi durata asta
* Se le chiamate verranno fatte a turno o chiunque può chiamare senza ordine
* La composizione delle rose, indicando il numero minimo/massimo di Portieri, Difensori, Centrocampisti e Attaccanti per la modalità classica oppure il numero minimo di portieri ed il totale minimo/massimo di giocatori totali acquistabili

Confermando questi dati verrà caricata la pagina di admin in cui sarà possibile:
* Cambiare i nomi degli allenatori
* Impostare le password
* Attribuire il ruolo di admin agli allenatori
* Cambiare l'ordine degli allenatori (per le chiamate a turni)
* Modificare altre informazioni già presentate in precedenza

Se si modifica qualche informazione, andrà confermata con il bottone `aggiorna configurazione`.

In questa pagina è presente anche il bottone `AZZERA TUTTO` per cancellare il db e ricominciare.

L'ultima attività di configurazione da effettuare consiste nel caricare la lista dei calciatori. Produrre il file da caricare nelle seguenti modalità:
* fantaservice (se deselezionata la scelta Mantra):
  recuperando l'elenco da https://www.fanta.soccer/it/archivioquotazioni/A/2020-2021/
* leghefantacalcio  (se selezionata la scelta Mantra)
scarica lista svincolati da https://leghe.fantacalcio.it/fanta-viva/lista-svincolati, aprilo con excel, rimuovi le prime 4 righe, -esporta - cambia tipo file - testo delimitato da tabulazione e salva con nome

Sono presenti due file di esempio.

# Funzionalità di gioco
La pagina è divisa in accordion, in modo da collassare le sezioni che non si vogliono vedere. Sono presenti dei suggerimenti, in rosso, contestualizzati per suggerire l'operatività da effettuare. In modalità mantra, oltre ai ruoli, è previsto un macro ruolo per aiutare i filtri.

#### Accordion Link
E' possibile accedere a:
* **pagina di amministrazione** oltre alle funzionalità descritte in precedenza per l'amministratore chiunque potrà personalizzare il proprio nome e la propria password.
* **elenco offerte** oltre alla cronologia delle offerte l'amministratore potrà cancellare una offerta salvata
* **log** cronologia di tutte le operazioni dispositive effettuate con indicazione oraria e indirizzo IP
* **giocatori liberi** elenco dei giocatori ancora disponibili, filtrabili per nome/ruolo/squadra/quotazione ("maggiore di" oppure mettendo il - "minore di"). E' possibile persolizzare un proprio elenco di giocatori preferiti, per filtrarli nella pagina delle offerte.
E' inoltre possibile aprire una scheda di dettaglio del giocatore.
* **riepilogo** situazione riassuntiva per allenatore dei giocatori presi. In questa pagina è possibile **esportare** i dati per il sito, se tipo lega mantra o in formato csv generico

#### Accordion allenatori
Una volta caricato l'url dell'applicazione è possibile connettersi cliccando sull'apposita icona, se si è settata una passowrd verrà richiesta altrimenti l'accesso sarà diretto.
Una volta connessi si potrà uscire scollegarsi tramite l'icona vicino al proprio nome oppure dal cestino in alto a destra. L'amministratore può escludere qualunque altro allenatore tramite l'icona del cestino. Se un allenatore non contatta il backend per più di 20 secondi (conteggiato da latenza) potrà essere cacciato da chiunque.
In ciascuna riga sarà presente una icona, per segnalare l'effettivo collegamento degli altri allenatori. L'indicazione del giocatore di turno (forzabile dall'amministratore) e un riepilogo dei giocatori presi (dettagliato per ruolo).
E' presente anche la possibilità di configurare la frequenza di **refresh** del client verso il backend. Si **sconsigliano** valori sotto i 1000 ms.
Solo l'amministratore, avrà anche la possibilità di avviare un'asta per un altro allenatore, tramite l'icona che appare una volta selezionato un giocatore.

#### Accordion offerte
Se è il proprio turno (o se si è amministratori) la prima attività da fare sarà **selezionare** il calciatore da offrire ed **avviare** l'asta (la durata è personalizzabile dall'amministratore nella pagina di admin). Dopo che sarà avviata, tutti gli allenatori vedranno il **progressivo del tempo rimanente** per effettuare un rilancio, la **situazione aggiornata** ed avranno la possibilità di **rilanciare** quanto indicato nell'apposito campo. Esiste la possibilità di **allineare in automatico** con i rilanci degli altri allenatori o **manualmente** con l'apposita icona. Inoltre esistono le scorciatoie per puntare **+1, +5 o +10**. 
E' possibile filtrare i giocatori per ruolo, nome, squadra, quotazione (mettendo il numero negativo si filtra per "minore di" altrimenti per "maggiore di") e per preferiti.
Non è prevista la possibilità di aggiungere i preferiti in questa pagina, ma solo in quella dei giocatori liberi.
E' inoltre possibile aprire una scheda di dettaglio del giocatore.
Chiunque potrà sospendere il conto alla rovescia con il bottone **pausa**.
Sono presenti i controlli per **bloccare** l'avvio ed il rilancio in caso di credito insufficiente o slot ruolo già completo.

L'**amministratore** avrà le seguenti possibilità aggiuntive:
* operare per qualunque altro allenatore.
* azzerare il tempo
* terminare l'asta in anticipo

Quando il tempo finisce, o l'amministratore termina l'asta, questo potrà **confermare** o **annullare** l'asta appena conclusa.

#### Accordion log sessione corrente
Verranno elencate tutte le attività effettuate, fino all'avvio di una nuova asta.

# Ripristino database
Sia con il database H2 che con MySql (avendo l'accortezza di settare il parametro APPEND come indicato in precedenza sul file `spy.properties`) è possibile ripristinare un database ad un salvataggio precedente.

Tutte le operazioni sul db sono tracciate nel file indicato dalla chiave logfile del `spy.properties` (il valore di default è spyAsta.log che verrà generato nella cartella di avvio dell'applicazione).
Se la chiave APPEND sarà indicata a true, verranno accodate le istruzioni per ogni riavvio dell'applicazione.

In caso di crash dell'applicazione è possibile recuperare questo file, salvarlo in un altro path, ad es. c:\restoreAs.txt e, una volta riavviata l'applicazione, ripristinare questa fotografia di database con il comando:
`curl -X POST "http://localhost:8081/restore" -H "accept: application/json" -H "Content-Type: application/json" -d "{ \"PATH\": \"C:\\restoreAs.txt\"}"`

