var app = angular.module('app', [ 'ngResource','ngAnimate', 'ngSanitize', 'ui.bootstrap' ]);
app.run(
		function($rootScope, $resource, $interval,$q){
	    	$rootScope.connessioneKO="Connessione al backend in corso...";
			$rootScope.sezLinkVisible=true;
			$rootScope.sezUtentiVisible=true;
			$rootScope.sezOperaComeVisible=true;
			$rootScope.sezOfferte=true;
			$rootScope.sezLog=true;
			$rootScope.config=false;
			$rootScope.nomegiocatore="";
			$rootScope.offerta=1;
			$rootScope.offertaPriv=1;
			$rootScope.offertaPrivOC=1;
			$rootScope.offertaOC=1;
			$rootScope.bSemaforoAttivo=true;
			$rootScope.messaggi=[];
			$rootScope.tokenUtente;
			$rootScope.isAdmin=false;
			$rootScope.calciatori=[];
			$rootScope.utenti=[];
			$rootScope.turno=0;
			$rootScope.tokenDispositiva=-1;
			$rootScope.isATurni=true;
			$rootScope.isSingle=false;
			$rootScope.autoAllinea=false;
			$rootScope.autoAllineaOC=false;
			$rootScope.isMantra=true;
			$rootScope.caricamentoInCorso=false;
			$rootScope.timePing=5000;
			$rootScope.budget=500;
			$rootScope.durataAstaDefault=15;
			$rootScope.idgiocatoreOperaCome=-1;
			$rootScope.nomegiocatoreOperaCome="";
			$rootScope.abilitaForza=false;
			$rootScope.firstAbilitaForza=false;
			$rootScope.forzaLogout= function(){
				$rootScope.sendMsg(JSON.stringify({'operazione':'cancellaUtente', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
				$rootScope.nomegiocatore='';
			};
			$rootScope.scegliMantra=function(){
				$rootScope.isMantra=!$rootScope.isMantra;
				if($rootScope.isMantra){
					$rootScope.numeroUtenti=10;
					$rootScope.minP=2;
					$rootScope.maxP=27;
					$rootScope.minD=0;
					$rootScope.maxD=0;
					$rootScope.minC=0;
					$rootScope.maxC=0;
					$rootScope.minA=23;
					$rootScope.maxA=27;
				}
				else {
					$rootScope.numeroUtenti=8;
					$rootScope.minP=3;
					$rootScope.maxP=3;
					$rootScope.minD=8;
					$rootScope.maxD=8;
					$rootScope.minC=8;
					$rootScope.maxC=8;
					$rootScope.minA=6;
					$rootScope.maxA=6;
				}
			}
			$rootScope.callDoConnect = function(nome,id, pwd) {
				var esci=false;
				if (pwd != '') {
					$rootScope.origPwd=pwd;
					$rootScope.tmpNpme=nome;
					$rootScope.tmpId=id;
					$rootScope.open(pwd);
				}
				else {
					$rootScope.nomegiocatore=nome;
					$rootScope.idgiocatore=id;
					$rootScope.doConnect();
				}
			}
			$rootScope.doConnect = function() {
//		        console.log('Connected');
		        if ($rootScope.nomegiocatore!=''){
		        	$rootScope.tokenUtente=new Date().getTime();
					$rootScope.sendMsg(JSON.stringify({'operazione':'connetti', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore, 'tokenUtente':$rootScope.tokenUtente}));
		        }
				$rootScope.calcolaIsAdmin();
			}
			$rootScope.urlDettaglio=function(cNome,cId, old){
				var url;
				if ($rootScope.isMantra){
					url= "https://www.fantacalcio.it/squadre/giocatore/" + cNome + "/" + cId;
					if (old) url=url+"/3/2020-21";
				}
				else {
					var id=(""+cId).substring(2,10);
					var nome="" + cNome;
					nome=nome.substring(0,nome.indexOf(" ")).toLowerCase();
					url = "https://www.fanta.soccer/it/seriea/" + id + "/calciatore/" + nome + "/";
				}
				window.open(url,'_blank' );
			}
			
			let myMap = new Map();
//			myMap.set(785,"Top assoluto di reparto, in assenza di Ronaldo e Lukaku sarà il giocatore più pagato allìasta.");
//			myMap.set(507,"Sta andando sotto costo in tante leghe, è una delle poche certezze in circolazione. L'infortunio, molto probabilmente, è fake.");
//			myMap.set(2841,"Resterà a Firenze. Italiano chiede molto lavoro di sacrificio alla punta, quindi potrebbe rendere meno dell'anno scorso.");
//			myMap.set(608,"E' un big di ruolo, dovrebbe garantire la cifra tra i 10 ed i 15 gol. Ritorna dopo la sosta.");
//			myMap.set(2764,"Senza Lukaku e con i rigori diventa un giocatore di prima fascia, ma bisogna stare attenti all'incostanza.");
//			myMap.set(2530,"Sarà molto difficile comprarlo con Giroud, quindi si può prendere singolarmente sperando che si infortuni il meno possibile.");
//			myMap.set(2529,"TOP di reparto, ma appetibilità diminuita causa ruolo (esterno sinistro, perde centralità nel sistema).");
//			myMap.set(409,"Bisogna prestare molta attenzione al prezzo, si rischia di strapagare le prestazioni passate (senza considerare il problema del rinnovo).");
//			myMap.set(4661,"Salterà diverse partite tra Coppa D'Africa e squalifica, ma a fine anno avrà garantito lo stesso un buon bottino di gol.");
//			myMap.set(647,"Farà i suoi gol, ma è un giocatore in declino: attenti al prezzo.");
//			myMap.set(531,"E' rimasto al Sassuolo, ma non sappiamo in realtà con quale morale e motivazioni. I suoi gol li farà, ma attenti al prezzo.");
//			myMap.set(441,"Alla fine resterà al Torino, ma in una situazione molto precaria. Torna dopo la sosta, ma attenti al prezzo.");
//			myMap.set(2544,"Finalizzatore di grandissima esperienza, probabile rendimento sopra doppia cifra.");
//			myMap.set(2002,"TOP di reparto.");
//			myMap.set(1874,"Uomo immagine del Cagliari e rigorista, classico secondo di buon affidamento.");
//			myMap.set(2085,"TOP di reparto, può portare tanti assist.");
//			myMap.set(1850,"Andrà in Coppa D'Africa: non costruitegli la squadra attorno. La situazione per il rinnovo è poco chiara.");
//			myMap.set(2215,"Rigorista, garantisce sempre diversi gol stagionali: state attenti, però, ne ha già fatti 3 nelle prime due giornate, quindi il prezzo ne risentirà.");
//			myMap.set(4427,"TOP di reparto, può essere snobbato dopo un inizio campionato non proprio entusiasmante.");
//			myMap.set(645,"TOP di reparto, tra punizioni e capacità d'inserimento è più interessante di Luis Alberto.");
//			myMap.set(309,"Senza Ronaldo la sua appetibilità sale enormemente, ma non svenatevi: ci sono giocatori che offrono più certezze.");
//			myMap.set(2819,"Alla Sampdoria, in assenza di alternative, avrà molto più minutaggio, ma è lecito aspettarsi che i gol vengano divisi con Quagliarella.");
//			myMap.set(2160,"TOP assoluto di ruolo. Per aggiudicarselo ci vorrà almeno il 10-12%.");
//			myMap.set(530,"Il ruolo da trequartista può rilanciarlo in ottica bonus. Dopo queste giornate verrà pagato quasi come un top.");
//			myMap.set(410,"Talento sulla via del tramonto, offre i suoi gol stagionali ma non è facile beccarli.");
//			myMap.set(4268,"Punta che ha dimostrato sempre di poter arrivare alla doppia cifra, ma attenti ad un carattere piuttosto complicato. Buon terzo slot.");
//			myMap.set(152,"E' il prototipo perfetto di trequartista per Spalletti, mi aspetto tanti bonus.");
//			myMap.set(186,"Il ballottaggio con Leao è serrato, quindi vanno presi entrambi. La doppia cifra è sicuramente alla portata.");
//			myMap.set(4517,"Dopo l'anno scorso sembra essersi ambientato, la doppia cifra è assolutamente alla portata.");
//			myMap.set(313,"In assenza di Ronaldo è virtualmente titolare, ma Kean è molto più forte di lui. Non spendetegli troppi crediti sopra.");
//			myMap.set(495,"E' un acquisto evitabile: sulla carta non è un titolare ed inevitabilmente verrà pagato parecchio a causa della doppietta contro il Verona.");
//			myMap.set(4200,"Dopo la doppietta con il Cagliari verrà pagato quanto un secondo slot: raggiungerà probabilmente la doppia cifra, ma state attenti al prezzo.");
//			myMap.set(4292,"TOP di ruolo, inferiore solo a Gosens.");
//			myMap.set(2475,"E' difficile beccare i suoi gol, soprattutto con la giornata spezzettata: evitabile.");
//			myMap.set(568,"La convivenza con Caputo gli leverà alla lunga diversi gol, non è più il caposaldo dell'attacco: c'è gente che offre maggiori certezze.");
//			myMap.set(2097,"Parte indietro nelle gerarchie, ma le qualità non si discutono: alla lunga può levare il posto a Morata, compratelo e non vi deluderà.");
//			myMap.set(2194,"Lo conoscete: qualche partita da top assoluto, poi scompare. Oltre il 6-7% è follia.");
//			myMap.set(632,"Alla Lazio si alternerà come esterno sinistro con Pedro, meno appetibile di quando era a Verona, ma ugualmente un buon acquisto.");
//			myMap.set(177,"Un buon acquisto per gli slot di riserva. Alla lunga Gasperini roterà tutti i trequartisti, quindi il suo spazio è destinato a diminuire.");
//			myMap.set(472,"E' stato confermato in rosa e sarà il titolare insieme a Caicedo, può essere un buon acquisto come riserva della squadra titolare.");
//			myMap.set(5233,"Scala a terzo attaccante con l'acquisto di Shomurodov, da evitare.");
//			myMap.set(697,"E' certamente un primo slot, ma con Allegri non ha mai veramente brillato: attenti al prezzo:");
//			myMap.set(1870,"Rischiate di pagare più il nome che il valore fantacalcistico, ci sono migliore alternative.");
//			myMap.set(2833,"Salto nel vuoto: quanto costa? Come gioca? Quando gioca? Io non ne ho idea.");
//			myMap.set(536,"Da attaccante perde tantissima appetibilità, c'è di meglio.");
//			myMap.set(5336,"Non è stato venduto ed è destinato a rimanere allo Spezia come separato in casa, si può comprare nella speranza che venga venduto a Gennaio.");
//			myMap.set(2077,"Garantisce diversi gol stagionali, ma è sempre meno al centro del progetto: attenti.");
//			myMap.set(2167,"Il passaggio a centrocampo più interessante: MV negativa, ma 7-8 gol tranquillamente a portata. E' rigorista.");
//			myMap.set(2625,"Certezza della squadra bolognese, può essere snobbato a causa dell'espulsione.");
//			myMap.set(335,"In queste prime giornate ha sorpreso: il suo prezzo sarà più alto, ma attenti ad una condizione fisica non propriamente eccezionale.");
//			myMap.set(2766,"Grande talento, ma non costruitegli la squadra attorno, ricordatevi che viene da una doppia operazione al legamento crociato.");
//			myMap.set(332,"Buon acquisto, pure rigorista in assenza di Quagliarella.");
//			myMap.set(302,"Uno dei migliori acquisti per rapporto qualità/prezzo. In assenza di De Paul dovrà caricarsi il team sulle spalle. E' rigorista.");
//			myMap.set(1997,"La sua fortuna è stata dovuta a fantasisti come Ounas e Messias, a Salerno sarà più difficile replicare i numeri dello scorso anno.");
//			myMap.set(250,"TOP di reparto. La MV può risentire della recente perdita di riflessi, ma la squadra ha mantenuto la solidità difensiva dell'anno scorso.");
//			myMap.set(2775,"Pagate più il nome che altro, di fatto gioca mediano: guardate altrove.");
//			myMap.set(2172,"In assenza di Zaccagni diventa il giocatore più importante della squadra: va comprato.");
//			myMap.set(2719,"Nononstante la stagione negativa, ha comunque segnato 8 gol: può essere una sorpresa.");
//			myMap.set(247,"Sarà la punta titolare, ha caratteristiche uniche in rosa. Va pagato poco, non è il giocatore di una volta.");
//			myMap.set(4179,"Giocatore dal talento immenso: Italiano ci punta tanto, ma non ha una grandissima propensione al gol.");
//			myMap.set(2832,"Non sono riusciti a cederlo, ma vi è una buona possibilità che venga ceduto a Gennaio: evitabile.");
//			myMap.set(4371,"La cessione di Caputo lo rende molto appetibile, è un buon acquisto: può giocare ovunque, giocatori così versatili sono oro per gli allenatori.");
//			myMap.set(2792,"Miglior portiere da modificatore di prima fascia, amato dai pagellisti per la spettacolarizzazione delle parate.");
//			myMap.set(453,"TOP di reparto, la FM gioverà del ritorno di Allegri. Non fatevi ingannare dalle prime giornate.");
//			myMap.set(798,"Profilo che ha dimostrato una crescita esponenziale, può meritare ad occhi chiusi il 4-5%.");
//			myMap.set(1995,"Le prime uscite sono state ottime, ad un prezzo compreso tra il 4 ed il 5% è un affare: trequartista titolare senza troppe discussioni.");
//			myMap.set(827,"Rischiate di pagare più il nome che altro, alla Juventus non ha molta appetibilità.");
//			myMap.set(5311,"Secondo attaccante Roma, potenziale sorpresa.");
//			myMap.set(387,"E' la porta più debole di prima fascia, quindi va accoppiata con un'altra delle seconde linee. Il vantaggio è che potrebbe essere snobbato.");
//			myMap.set(4312,"Il Milan è una squadra che subisce tanti tiri in porta, quindi la FM (a volte) e la MV dipenderanno fondamentalmente dalle sue capacità.");
//			myMap.set(572,"Spalletti nelle ultime annate di A ha avuto difese solide, quindi Meret rimane una delle scelte di prima fascia. Va comprato con Ospina.");
//			myMap.set(4270,"Buon portiere voluto specificamente da Josè, tuttavia la solidità difensiva della Roma è tutta da testare, quindi costerà di meno.");
//			myMap.set(554,"In attesa di Hateboer si alternerà con Maehle sulla fascia destra. E' un ottimo profilo, ma debole fisicamente e non certo della titolarità.");
//			myMap.set(788,"Ottima costanza di rendimento, il classico quarto slot che chiunque vorrebbe.");
//			myMap.set(5001,"Con un minutaggio scadente è già riuscito a fare ben 4 gol: lo comprate e lo mettete sempre, sperando che faccia qualcosa da subentrato.");
//			myMap.set(4973,"Potenziale sorpresa a centrocampo, potrebbe segnare più del previsto. Con Allegri avrà molto spazio.");
//			myMap.set(4825,"Sarà il titolare indiscusso della trequarti, in assenza di acquisti importanti la sua appetibilità sale incredibilmente: buon acquisto.");
//			myMap.set(2857,"La società ci crede parecchio ed il minutaggio sarà ampio, il talento c'è.");
//			myMap.set(236,"Caposaldo della squadra insieme a Faraoni: utile per schierarlo nelle partite più semplici, ma è difficile vederlo oltre un quarto/quinto slot.");
//			myMap.set(4510,"E' stato titolare nelle prime uscite, va comprato con Rebic.");
//			myMap.set(407,"E' stato tra i migliori marcatori della Sampdoria l'anno, avrà il suo spazio. Ritorna dopo la sosta.");
//			myMap.set(215,"Ottimo per gli ultimi slot, giocatore molto interessante.");
//			myMap.set(479,"Troverà diverso spazio come trequartista o punta, ma non sono propriamente i suoi ruoli: c'è di meglio.");
//			myMap.set(322,"Ha vissuto le migliori stagioni in carriera con Inzaghi, può far bene.");
//			myMap.set(2296,"Certezza del ruolo, diversi bonus in canna.");
//			myMap.set(4998,"La MV non è entusiasmante, non sa difendere, ma in attacco è dannatamente efficace. I bonus verranno da lui e Pereyra.");
//			myMap.set(581,"Unico difensore del Verona con appetibilità, la MV non è ottimale ma porta ogni anno diversi bonus distribuiti durante la stagione.");
//			myMap.set(2741,"Ha una folta concorrenza e, dopo la scorsa stagione, verrà pagato più del dovuto: forse, a parità di prezzo, troverete di meglio.");
//			myMap.set(4377,"Italiano punta molto sulle incursioni dei centrocampisti: o quest'anno o mai più!");
//			myMap.set(265,"Un acquisto sempre valido, da schierare nelle partite più semplici.");
//			myMap.set(5454,"Potenziale sorpresa e miglior giocatore che sale dalla serie B. Non è un gran rigorista, ma può sorprendere.");
//			myMap.set(2826,"Tra i più positivi nel precampionato e nelle prime giornate, può essere acquistato per gli ultimi slot del ruolo.");
//			myMap.set(4237,"Avrà spazio, ma non parte titolare e verrà pagato tanto da chi guarda il listone, può essere sfruttato per far spendere crediti agli avversari.");
//			myMap.set(513,"Deve adattarsi alla difesa a 4 e solitamente viene rialzato parecchio, oltre il 4% è un pò rischioso. Buono per il modificatore.");
//			myMap.set(392,"Nell'ultima annata è sembrato un pò distratto e dovrebbe pure saltare diverse partite per la Coppa D'Africa: attenti al prezzo.");
//			myMap.set(704,"Parte avanti a Dimarco, non verrà pagato molto.");
//			myMap.set(4978,"Il vero gioiello della Sampdoria, può essere l'anno della consacrazione: non andrà basso, ma è un bel colpo.");
//			myMap.set(4988,"Ottimo rincalzo da schierare nelle partite più semplici, ma Motta non stimola gli inserimenti dei centrocampisti come Italiano.");
//			myMap.set(4359,"E' rimasto a Bergamo come terza punta, poco appetibile.");
//			myMap.set(2284,"Al Genoa può fare molto bene, è una punta d'esperienza e può trovare la continuità non avuta a Roma.");
//			myMap.set(2137,"La cessione di Caputo lo rilancia enormemente, è meno appetibile di Raspadori ma rimane ugualmente interessante.");
//			myMap.set(4992,"Ha segnato alla prima, ma non fatevi ingannare: c'è di meglio.");
//			myMap.set(2119,"Ci si può puntare per gli ultimi slot, niente di più.");
//			myMap.set(2155,"Avrà spazio come seconda punta, però niente di particolare.");
//			myMap.set(2489,"L'acquisto di Zaccagni gli leverà minutaggio, avrà comunque un buon minutaggio e può regalare diversi assist.");
//			myMap.set(345,"Lo vedo più come riserva da sfruttare a partita in corso che come un titolare, può essere preso per gli ultimi slot.");
//			myMap.set(761,"Tra i migliori portieri della scorsa stagione per rendimento, tuttavia il passaggio da Ranieri a D'Aversa ne riduce leggermente l'appetibilità.");
//			myMap.set(509,"Nelle intenzioni Dionisi vuole cavalcare il gioco di De Zerbi, quindi bisogna attendersi una buona MV condita da qualche gol subito di troppo.");
//			myMap.set(2130,"Può essere preso come scommessa aspettando il recupero, per il resto evitabile.");
//			myMap.set(2120,"Non è mai veramente premiato dai pagellisti a causa dei suoi ben più importanti compagni di reparto, ma sotto il 3% è un ottimo acquisto.");
//			myMap.set(286,"Con Allegri non ha un bel rapporto, potreste anche farne a meno.");
//			myMap.set(4428,"Nonostante una stagione negativa della Juventus, i suoi numeri sono stati molto positivi: può solamente migliorarli.");
//			myMap.set(357,"Una delle sorprese dell'anno scorso, vorrà conquistarsi l'imminente Mondiale. Può confermare i numeri passati.");
//			myMap.set(2633,"Caposaldo della difesa milanista, macchina da 6,5. Bisogna stare attenti agli infortuni, conviene comprare anche Romagnoli.");
//			myMap.set(4751,"Difende con energia e decisione senza prendere cartellini: deve confermarsi, ma ha tutte le carte in regola per farlo.");
//			myMap.set(2180,"Ha realizzato una stagione dignitosa e nelle prime uscite si è spinto molto in attacco: entro il 3% è un affare.");
//			myMap.set(1852,"Può essere preso come scommessa in coppia con Vina.");
//			myMap.set(2788,"Uno dei migliori difensori del nostro campionato, con Juric può solo migliorarsi. Gli infortuni delle prime giornate ne possono abbassare il prezzo.");
//			myMap.set(4965,"Nonostante il ruolo ha diversi bonus in canna e tira perfino le punizioni. Miglior centrocampista fantacalcistico del Cagliari.");
//			myMap.set(4514,"E' rimasto al Cagliari e le prime uscite sono state positive, può essere comprato per gli ultimi slot.");
//			myMap.set(376,"Giocherà? Non giocherà? Farà pena per l'ennesima volta? A voi la scelta.");
//			myMap.set(2061,"Sarà probabilmente titolare al Verona, ha qualche gol in canna (condito da voti terribili).");
//			myMap.set(383,"E' rimasto a Napoli come seconda/terza punta, non è molto appetibile.");
//			myMap.set(643,"Indietro nelle gerarchie, evitabile.");
//			myMap.set(4324,"Ci sono giocatori più interessanti, evitabile.");
//			myMap.set(245,"Profilo evergreen del Grifone, avrà spazio ma probabilmente non sarà il titolare.");
//			myMap.set(5529,"Attaccante molto bravo, ottimi numeri e qualità non indifferenti: Forte ha i giorni contati.");
//			myMap.set(2211,"Il valore del portiere non si discute e l'assetto difensivo dell'Udinese rimarrà pressochè intatto: sicuramente un buon acquisto.");
//			myMap.set(787,"In assenza di Romero, per questioni di esperienza e presenza sarà lui l'uomo clou della difesa. Buon profilo, sperando nella clemenza dei voti.");
//			myMap.set(2164,"Ha rinnovato, può essere un buon acquisto per la rosa titolare.");
//			myMap.set(662,"Storicamente ha avuto un buon feeling con Allegri, ma è lontano dall'essere una certezza.");
//			myMap.set(2263,"Non è un terzino e la MV può risentirne. ma può trovare parecchi spazi in attacco. Dovrebbe tornare dopo la sosta.");
//			myMap.set(2816,"E' in rampa di crescita, attenti però al prezzo inflazionato dall'Europeo.");
//			myMap.set(1895,"Classico profilo interessante in ottica modificatore.");
//			myMap.set(5513,"Da lui ci si aspetta molto, sopratutto dopo l'Europeo: attenti all'hype, ma vi sono diversi bonus a portata. E' obbligatorio l'acquisto con Darmian.");
//			myMap.set(2744,"Gioca in mediana, lo sappiamo, ma si inserisce più del dovuto: ottimo nome a costi irrisori.");
//			myMap.set(5451,"Giocatore molto appetibile per la posizione all'interno dell'assetto toscano, può essere un onesto quarto slot.");
//			myMap.set(367,"E' il 12esimo uomo della Fiorentina, anche lui può giovare dello stile aggressivo richiesto ai centrocampisti (mezzali) da parte dell'allenatore.");
//			myMap.set(4892,"Ottimo rendimento, ma bonus scadenti: da prendere come rincalzo da schierare in partite più semplici.");
//			myMap.set(4970,"Nonostante l'assenza di titolarità, da centrocampista stuzzica parecchio: scommessa molto intrigante.");
//			myMap.set(795,"Mourinho punta sulle seconde linee, buon acquisto per chiudere gli ultimi slot.");
//			myMap.set(90,"Avrà la concorrenza di Simeone, si può prendere in coppia.");
//			myMap.set(4184,"Da attaccante perde inevitabilmente valore, visto che dovrebbe giocare trequartista: scommessa, non più di tanto");
//			myMap.set(133,"Il Bologna subisce davvero tanto e potrebbe cedere Tomiyasu: se potete, evitatelo.");
//			myMap.set(1917,"Italiano prende diverse gol, ma è difficile far peggio dell'anno scorso. Da prendere con Terracciano.");
//			myMap.set(487,"Ormai è molto debole fisicamente, i migliori tempi sono passati. Guardate altrove. Non fatevi ingannare da quella doppietta.");
//			myMap.set(2525,"Da titolare o subentrato troverà sempre spazio, è un buon pick anche in leghe molto numerose.");
//			myMap.set(2784,"Si distrae spesso e gli errori passano maggiormente dai suoi piedi, va trattato come un titolare e non di più.");
//			myMap.set(706,"La domanda è: gioca lui od Aina? Dovrebbero esser presi insieme, attenti al prezzo. Juric punta molto sui propri esterni.");
//			myMap.set(2280,"Profilo low-cost per eccellenza in ottica modificatore, l'affidabilità è pressochè totale.");
//			myMap.set(288,"Giocherà più del previsto: se integro, Allegri lo schiererà sempre. L'età è quella, ma le qualità non si discutono.");
//			myMap.set(5509,"Venuto per sostituire Spinazzola, è un giocatore talentuoso tutto da scoprire. Va comprato con Spinazzola.");
//			myMap.set(22,"Nome poco interessante al di fuori della titolarità. Salta le prime 4.");
//			myMap.set(2154,"Mihajovic crede molto in lui e sicuramente avrà spazio, ottimo per chiudere il ruolo.");
//			myMap.set(470,"Ha perso lo smalto di un tempo, non aspettatevi il colpaccio della vita.");
//			myMap.set(2414,"Scommessa per cuori forti.");
//			myMap.set(2009,"Può essere preso per chiudere il ruolo, ha preso la 10 e cercherà di responsabilizzarsi.");
//			myMap.set(1933,"Regista del Torino, può portare diversi assist tra punizioni e calci d'angolo.");
//			myMap.set(5007,"Ragazzo talentuoso e molto interessante, potrà sorprendere parecchio.");
//			myMap.set(2065,"La sua flessibilità gli garantisce una titolarità pressochè indiscussa: buoni voti e bonus saltuari sono assolutamente alla portata.");
//			myMap.set(315,"Mi rifiuto di commentare.");
//			myMap.set(5694,"Colpo a sorpresa dell'Udinese, di questo ragazzo si parla benissimo e dietro ha un investimento importante: va comprato, sperando che esploda.");
//			myMap.set(2178,"Uno dei migliori portieri in ottica modificatore, ma la pessima difesa lo condanna virtualmente ad una FM disastrosa.");
//			myMap.set(2170,"Savic non è così forte, ma la struttura difensiva assunta da Juric può sorprendere in ottica FM. Prendete pure Berisha.");
//			myMap.set(5375,"In assenza di Hateboer si giocherà il posto con Zappacosta. Ottimo Europeo, ma attenti al prezzo.");
//			myMap.set(2181,"Avrà tantissimo spazio, ma quando gioca non è entusiasmante. Evitabile.");
//			myMap.set(695,"Ha perso lo smalto fantacalcistico di un tempo, non lo pagherei oltre il 2%.");
//			myMap.set(252,"Ogni anno delude sempre tutti in ogni modo. Potete prenderlo sperando nella cura Italiano, ma non vi garantisco nulla.");
//			myMap.set(459,"Può arrivare qualche bonus, ma ha pericolosi momenti di blackout. Spalletti proverà a rilanciarlo, bisogna però testarne le motivazioni sul campo.");
//			myMap.set(464,"Riserva di Calabria e Saelemaekers, troverà spazio prevalentemente da subentrato.");
//			myMap.set(4245,"Profilo altamente valido. Grosse incognite fisiche, va preso con una riserva.");
//			myMap.set(4421,"Profilo low-cost da alternare nelle partite più semplici.");
//			myMap.set(4895,"Numeri importanti per un difensore low-cost come lui, profilo discetamente da modificatore.");
//			myMap.set(4415,"Si giocherà un posto con Orsolini, ma fino ad adesso si è rivelato eccessivamente fumoso. Acquistare con cautela.");
//			myMap.set(644,"L'arrivo di Torreira lo relega in panchina.");
//			myMap.set(2166,"Nell'ultimo anno è calato vistosamente, da maneggiare con cautela. Allegri proverà a rilanciarlo.");
//			myMap.set(4725,"Infortunato, al limite da comprare con Anguissa.");
//			myMap.set(4404,"Giocatore importantissimo nella mediana doriana, porterà ottimi voti..");
//			myMap.set(4681,"Prende troppi cartellini, ma ormai offre minutaggio abbondante ed ha la possibilità di giocare trequartista.");
//			myMap.set(1980,"Rigorista del Venezia e cuore pulsante della squadra, le azioni più pericolose passeranno dai suoi piedi. Se potete, acquistatelo.");
//			myMap.set(5501,"Colpo di esperienza del Venezia, può anche giocare nel tridente e merita sicuramente un acquisto di scommessa.");
//			myMap.set(1972,"Un ritorno inaspettato: buoni voti e qualche gol in canna, profilo interessante.");
//			myMap.set(537,"Avrà spazio da titolare o subentrato, si può prendere come ultimo slot.");
//			myMap.set(1958,"A Verona può trovare più spazio, ma rimane un profilo ugualmente poco appetibile.");
//			myMap.set(505,"Alla Salernitana sarà titolare, ma non aspettatevi chissà che cosa.");
//			myMap.set(4957,"Il ragazzo a Benevento ha fatto diversi errori e la fase difensiva di Di Francesco è piuttosto discutibile: io lo eviterei.");
//			myMap.set(2653,"Visto l'arrivo di Dalbert, può giocare nel terzetto difensivo o finire direttamente in panchina. Al momento non offre certezze.");
//			myMap.set(254,"Alternativa importante a Perisic, avrà diverso spazio e può essere un crack. Se la gioca, ma difficile andare oltre 1-2%.");
//			myMap.set(140,"Sarri lo conosce bene, con lui era una macchina da 6,5: buon acquisto a poco.");
//			myMap.set(4331,"Troverà spazio a causa delle condizioni non eccezionali di Smalling.");
//			myMap.set(5332,"I numeri sono interessanti ed il giocatore è capace, si giocherà però un posto con il neoarrivato Reca.");
//			myMap.set(2315,"E' rimasto e sarà probabilmente il titolare della fascia sinistra, però gli altri giocatori possono insediarlo a lungo termine.");
//			myMap.set(4886,"E' un'incognita: ha le qualità per incidere, ma rimane pressochè una scommessa.");
//			myMap.set(2161,"E' titolare e può portare qualche bonus, verrà pagato meno di Bajrami e può essere interessante per gli ultimi slot.");
//			myMap.set(2839,"In assenza di nuovi arrivi si gioca il posto con Callejon, va comprato con lui.");
//			myMap.set(170,"Titolare affidabile da ultimi slot.");
//			myMap.set(2379,"Possibile sorpresa dello scacchiere juventino, può essere il nuovo Khedira (con tutte le differenze qualitative del caso).");
//			myMap.set(2209,"Da prendere come titolare, non di più.");
//			myMap.set(4479,"Spalletti lo vede molto e nelle prime uscite è stato molto positivo, si può tentare come scommessa.");
//			myMap.set(600,"E' indietro nelle gerarchie, c'è di meglio.");
//			myMap.set(2848,"Nelle prime uscite è stato positivo, si può prendere come titolare momentaneo.");
//			myMap.set(5287,"Niente di particolare, potete evitarlo. Offre solo il voto, quindi consigliabile da leghe da 14 partecipanti in su.");
//			myMap.set(5674,"Acquisto di qualità, potrà far rifiutare i centrocampisti laziali.");
//			myMap.set(5685,"Non lasciatevi ingannare dai suoi numeri: sarà più forte di De Roon, ma non tirerà nè rigori nè punizioni. Fantacalcisticamente c'è di meglio.");
//			myMap.set(4884,"Da ultimo slot, solo per cuori forti.");
//			myMap.set(1943,"Evitabile.");
//			myMap.set(5495,"Henry gli leverà il posto.");
//			myMap.set(652,"Evitabile.");
//			myMap.set(5506,"Uno dei tanti giocatori arrivati a Genova per contendersi un posto, ma parte indietro nelle gerarchie.");
//			myMap.set(2179,"La fase difensiva del Genoa non è così terribile, ma lui ha perso lo smalto di un tempo, quindi la MV potrebbe risentire a lungo termine.");
//			myMap.set(2192,"I numeri che abbiamo visto ad Udine non dovrebbero cambiare più di tanto, non aspettatevi molto di più della titolarità.");
//			myMap.set(2572,"Ha deciso di rimanere, un buon acquisto da inserire nelle partite più facili.");
//			myMap.set(4461,"Con la permanenza di Nandez rischia di rimanere indietro nelle gerarchie, avrà sicuramente spazio ma non è una certezza.");
//			myMap.set(2759,"L'anno scorso è stato un pò sfortunato, quest'anno i numeri potrebbero migliorare. Unico rigorista tra i difensori.");
//			myMap.set(2335,"Incognita infortuni, non merita una spesa superiore all'1%.");
//			myMap.set(640,"Titolare low-cost, niente di più.");
//			myMap.set(2318,"Lo spazio lo avrà, ma i voti non sono entusiasmanti.");
//			myMap.set(4982,"Il miglior difensore per distacco dello Spezia, ottima soluzione low-cost.");
//			myMap.set(226,"Le prime uscite non sono andate bene, ma alla lunga la sua qualità uscirà: vale la pena comprarlo come slot scommessa.");
//			myMap.set(4899,"Profilo molto interessante in ottica bonus, da prendere con Vojvoda. Rischio Coppa d'Africa.");
//			myMap.set(790,"Per ora discreto titolare low-cost da girare in base alle partite, ma alla lunga potrebbe essere Perez il titolare.");
//			myMap.set(770,"Riserva di Gosens, conosce bene il campionato e potrebbe giocare più partite di quanto ci si possa aspettare.");
//			myMap.set(5675,"Interessante giocatore polivalente, i titolari sono sinceramente più scarsi di lui e non avrà problemi ad emergere.");
//			myMap.set(5677,"E' un colpo interessante, avrà sicuramente spazio e vi ci può investire un credito.");
//			myMap.set(2614,"Titolare della fascia destra, può regalare qualche bonus, sopratutto in ottica assist.");
//			myMap.set(550,"Al Genoa sarà titolare fisso senza discussioni, buon acquisto low cost.");
//			myMap.set(4719,"Colpo interessante del Venezia, secondo me leverà il posto a Caldara. Può portare diversi voti buoni.");
//			myMap.set(4869,"Troppo fumoso, guardate altrove.");
//			myMap.set(4393,"Offre buoni voti, ma bonus essenzialmente inesistenti.");
//			myMap.set(406,"Si alternerà con Sottil come esterno destro, rimane comunque una scommessa.");
//			myMap.set(150,"Evitabile.");
//			myMap.set(1978,"Forse andrà altrove, ma ormai l'ospedale è casa sua.");
//			myMap.set(2472,"Allegri lo sta testando come regista davanti la difesa, è sicuramente evitabile.");
//			myMap.set(2818,"Evitabile: tanti infortuni, tanti cartellini ed una Coppa d'Africa che lo aspetta a gennaio.");
//			myMap.set(4449,"Ha sorpreso, alla lunga può regalare qualche gioia ai propri fantallenatori. Ricordatevi, però, che è un mediano.");
//			myMap.set(779,"E' una riserva, ma troverà diverso spazio durante l'anno.");
//			myMap.set(2391,"Fantacalcisticamente lascia a desiderare, guardate altrove.");
//			myMap.set(5487,"Viene da un campionato inferiore e può soffrire il passaggio, ma ha una naturale propensione al gol. Trattatelo come scommessa, niente di più.");
//			myMap.set(2011,"Rigorista della squadra, acquisto sempre valido.");
//			myMap.set(4890,"Ha la concorrenza di Hongla, forse c'è di meglio.");
//			myMap.set(5511,"Sostituto formale di Locatelli, garantirà presenza ma fantacalcisticamente poco interessante.");
//			myMap.set(4220,"Acquisto eccezionale del Napoli, il ragazzo è davvero forte, ma al fantacalcio il suo ruolo è poco appetibile.");
//			myMap.set(2038,"All'Empoli si contende un posto con Cutrone nel ruolo di seconda punta.");
//			myMap.set(2195,"Al Napoli parte come riserva, avrà spazio da subentrato ma non è appetibile.");
//			myMap.set(5515,"Possibile scommessa, avrà spazio probabilmente come esterno durante la stagione, ma c'è di meglio.");
//			myMap.set(5676,"Fantacalcisticamente poco appetibile.");
//			myMap.set(4964,"E' il miglior portiere tra le piccole in ottica modificatore.");
//			myMap.set(5005,"Lo Spezia subisce tanto ed il ballottaggio con Provedel non è chiaro: state attenti. Per ora sembra essere avanti nelle gerarchie.");
//			myMap.set(2746,"Spinge parecchio, ma gli infortuni sono dietro l'angolo. Profilo non sicuro.");
//			myMap.set(4891,"E' stato scavalcato nelle gerarchie da Medel, con l'arrivo di Theate rischia di giocare ancora meno.");
//			myMap.set(1866,"Sarà protagonista della coppia horror con Carboni, direi di guardare altrove.");
//			myMap.set(5450,"Grande esperienza e qualche bonus in canna per questa scommessa. Gioca terzino.");
//			myMap.set(791,"Interessante acquisto di esperienza del Grifone, può essere una sorpresa.");
//			myMap.set(2328,"Qualitativamente è il miglior difensore del Genoa, ma attenti agli infortuni: non andate oltre l'1%.");
//			myMap.set(2188,"Parte indietro nelle gerarchie.");
//			myMap.set(460,"Riserva di lusso, può essere comprato con uno dei titolari.");
//			myMap.set(142,"Attuale titolare in assenza di acquisti, non offre molto.");
//			myMap.set(4495,"Avrebbe spazio, se non fosse per i costanti e ricorrenti infortuni. Non dategli troppa importanza.");
//			myMap.set(2104,"Titolare, niente di più.");
//			myMap.set(4530,"Parte indietro nelle gerarchie.");
//			myMap.set(4521,"Il ballottaggio con Toljan non è chiaro, spendere poco.");
//			myMap.set(5475,"Ha un bagaglio importante d'esperienza, i voti possono giovarne.");
//			myMap.set(5010,"Scambio di titolari con Nikolaou, dovrebbe portare titolarità.");
//			myMap.set(4994,"Parte indietro nelle gerarchie, ma avrà spazio.");
//			myMap.set(4412,"Titolare low-cost da incrociare, rischia di finire in panchina per Perez.");
//			myMap.set(4323,"Parte indietro nelle gerarchie.");
//			myMap.set(5483,"Scommessa tutta da provare, ma la titolarità non è sicura.");
//			myMap.set(2758,"Titolare low-cost. Attenti ai numerosi infortuni.");
//			myMap.set(2769,"Troppa incertezza sulla titolarità.");
//			myMap.set(5514,"Uno dei tanti difensori centrali del Genoa, consigliato come coppia solo in leghe molto numerose.");
//			myMap.set(2453,"Probabile riserva del duo di difesa.");
//			myMap.set(2076,"Stagione finita.");
//			myMap.set(5453,"Niente di particolare.");
//			myMap.set(4522,"Viene da una stagione negativa, al momento non avrà spazio da titolare.");
//			myMap.set(4515,"Al momento una scommessa, niente di più.");
//			myMap.set(801,"Quando gioca è in grado di timbrare il cartellino, ma dovete essere bravi a capire dove e quando schierarlo. C'è sicuramente di meglio.");
//			myMap.set(2692,"Evitabile.");
//			myMap.set(148,"Evitabile.");
//			myMap.set(5298,"Al Torino avrà molto spazio, è appetibile dal punto di vista dei bonus. Merita uno slot scommessa.");
//			myMap.set(4287,"Evitabile.");
//			myMap.set(2703,"Ha deluso molto. E' un profilo di esperienza, ma fantacalcisticamente inutile.");
//			myMap.set(5312,"Fantacalcisticamente inutile, perde anche la titolarità con Kovalenko.");
//			myMap.set(2008,"Sembra indietro nelle gerarchie, forse Juric proverà a rilanciarlo ma è diventato troppo discontinuo.");
//			myMap.set(238,"Evitabile.");
//			myMap.set(2144,"Sorpresa dei granata, da prendere come ultimo slot.");
//			myMap.set(2392,"Niente di più della titolarità.");
//			myMap.set(5488,"I nuovi acquisti probabilmente lo relegheranno in panchina, evitabile.");
//			myMap.set(5490,"Interessantissimo giocatore del Venezia, bravo sopratutto dal punto di vista degli inserimenti. Si può prendere per chiudere il ruolo.");
//			myMap.set(4423,"Acquisto molto interessante del Grifone, ha una buona tendenza al bonus ed può essere un buon rincalzo del centrocampo titolare.");
//			myMap.set(1879,"Evitabile.");
//			myMap.set(4477,"Relegato in panchina.");
//			myMap.set(5471,"Con i nuovi arrivi è, di fatto, relegato in panchina.");
//			myMap.set(4453,"Riserva di Caputo e Quagliarella.");
//			myMap.set(158,"Sta recuperando da una rottura del crociato e del menisco del ginocchio destro, è il titolare ma ritornerà a campionato già iniziato.");
//			myMap.set(4925,"Titolare, niente di più.");
//			myMap.set(2289,"Al momento titolare di fascia, buona scommessa low-cost.");
//			myMap.set(4374,"E' rimasto, ma non è molto affidabile.");
//			myMap.set(4329,"Titolare low-cost da incrociare, giocherà allo Spezia.");
//			myMap.set(5323,"Qualitativamente il miglior difensore della Fiorentina, con la vendita di Pezzella è verosimilmente titolare.");
//			myMap.set(2083,"Si gioca un posto, ma offre pressochè nulla. Guardate altrove.");
//			myMap.set(244,"Parte indietro nelle gerarchie, il posto dovrebbe essere di Sabelli. C'è di meglio, anche se in passato ha fatto bene.");
//			myMap.set(4332,"Gasperini proverà a rilanciarlo, ma non ci sono garanzie di successo.");
//			myMap.set(2208,"Che giochi esterno o terzo di difesa, il suo spazio lo avrà. Buono per chiudere il ruolo.");
//			myMap.set(2252,"L'infortunio di Parisi lo lancia titolare, ma anche lui non scherza con la condizione fisica. Meglio Stojanovic.");
//			myMap.set(4426,"In ballottaggio con Muldur, profilo evitare.");
//			myMap.set(2865,"Il ballottaggio con Ansaldi è più serrato del previsto, avrà diverso spazio. Scommessa da provare. Rischio Coppa d'Africa.");
//			myMap.set(2724,"Al momento Juric non lo vede tantissimo.");
//			myMap.set(2169,"Riserva, evitabile.");
//			myMap.set(5479,"Solamente 12 cartellini in 114 partite di serie B, può essere un interessante colpo low-cost.");
//			myMap.set(4493,"Possibile titolare low-cost.");
//			myMap.set(73,"Acquisto di esperienza, avrà diverso spazio. Storicamente fa almeno 1-2 gol l'anno.");
//			myMap.set(287,"Difensore d'esperienza, avrà diverso spazio nelle rotazioni, se in condizione è un titolare.");
//			myMap.set(2253,"Scommessa da ultimi slot, ha sorpreso nelle prime uscite.");
//			myMap.set(2778,"Nelle prime uscite è andato in panchina, probabilmente ha perso il posto.");
//			myMap.set(181,"Evitabile.");
//			myMap.set(184,"NO COMMENT.");
//			myMap.set(5492,"Titolarità dubbia.");
//			myMap.set(5507,"Scommessa da ultimi slot.");
//			myMap.set(5684,"Può trovare spazio nel centrocampo titolare, ha un'esperienza alle spalle non indifferente.");
//			myMap.set(2639,"Avrà spazio, ma è una riserva: c'è di meglio.");
//			myMap.set(5460,"Interessante scommessa per chi gioca con slot primavera.");
//			myMap.set(4938,"Scommessa da ultimo slot.");
//			myMap.set(5496,"Evitabile.");
//			myMap.set(1857,"Da attaccante ha poca appetibilità, guardate altrove.");
//			myMap.set(720,"La Salernitana fa della chiusura difensiva uno dei suoi aspetti principali, quindi può sorprendere se schierato nelle occasioni giuste.");
//			myMap.set(2739,"Si gioca un posto con Bastoni allo Spezia.");
//			myMap.set(5300,"Riserva.");
//			myMap.set(50,"Avrà spazio, ma parte indietro. C'è di meglio.");
//			myMap.set(5449,"Viene da un infortunio non leggero, avrà la concorrenza di Marchizza: c'è di meglio.");
//			myMap.set(76,"Attualmente l'allenatore gli preferisce Luperto, è evitabile.");
//			myMap.set(4407,"Riserva sia come terzino che come difensore centrale, evitabile.");
//			myMap.set(2285,"Evitabile.");
//			myMap.set(15,"Indietro nelle gerarchie, evitabile.");
//			myMap.set(118,"Al centro dei numerosi ballottaggi della difesa genovese, c'è di meglio. Solo per leghe molto numerose.");
//			myMap.set(253,"Buona riserva, avrà sicuramente il suo spazio. Da prendere al limite con uno dei titolari.");
//			myMap.set(358,"Riserva, niente di più.");
//			myMap.set(2728,"Tecnicamente prima riserva di Alex Sandro.");
//			myMap.set(294,"Riserva, niente di più.");
//			myMap.set(633,"Al Genoa sarà verosimilmente il titolare della fascia sinistra a posto di Cambiaso.");
//			myMap.set(329,"Con gli infortuni ricorrenti di Luiz Felipe avrà spazio senza troppi problemi.");
//			myMap.set(5017,"Riserva di Theo Hernandez.");
//			myMap.set(4409,"Avrà spazio quando KK andrà in Coppa D'Africa, quindi va comprato con lui. Buoni numeri.");
//			myMap.set(5464,"In ballottaggio, c'è di meglio.");
//			myMap.set(660,"Difensore di maggior affidamento della Salernitana, buono per chiudere il ruolo.");
//			myMap.set(2064,"C'è la possibilità che non giochi, quindi sarebbe meglio guardare altrove.");
//			myMap.set(4433,"Scommessina moltro intigrante per il ragazzo scuola Atalanta, da esterno intriga parecchio. Ballottaggio con Kechrida.");
//			myMap.set(4403,"Può partire altrove, al momento sarebbe meglio evitarlo.");
//			myMap.set(144,"Parte inizialmente indietro nelle gerarchie, ma avrà spazio.");
//			myMap.set(418,"Parte indietro nelle gerarchie, c'è di meglio.");
//			myMap.set(5355,"Se parte Larsen è una scommessa da fare, altrimenti ci penserei due volte.");
//			myMap.set(5480,"In ballottaggio con Mazzocchi, la titolarità non è certa.");
//			myMap.set(5481,"In ballottaggio con Ebuehi, la titolarità non è certa.");
//			myMap.set(5482,"Andrà probabilmente in panchina a causa dei nuovi acquisti.");
//			myMap.set(5498,"Possibile sorpresa della difesa Veronese.");
//			myMap.set(1891,"In ballottaggio come tanti altri in squadra, c'è di meglio.");
//			myMap.set(4934,"Scommessa, niente di più.");
//			myMap.set(5520,"Piccola scommessa low-cost di casa Genoa, ma l'acquisto di Fares ne mina fortemente la titolarità.");
//			myMap.set(5397,"Allo Spezia è molto interessante, profilo low-cost da tenere d'occhio.");
//			myMap.set(1871,"Niente di particolare.");
//			myMap.set(4376,"Scommessa da ultimi slot.");
//			myMap.set(557,"Scommessa rischiosissima.");
//			myMap.set(526,"Evitabile.");
//			myMap.set(5457,"Scommessa.");
//			myMap.set(2302,"Evitabile.");
//			myMap.set(4459,"Niente di particolare.");
//			myMap.set(305,"Evitabile.");
//			myMap.set(4285,"Deve recuperare da un infortunio importante, ci sono scelte più interessanti.");
//			myMap.set(58,"Evitabile.");
//			myMap.set(4885,"Evitabile.");
//			myMap.set(173,"Evitabile.");
//			myMap.set(5469,"Evitabile.");
//			myMap.set(560,"Momentaneo titolare.");
//			myMap.set(2855,"Evitabile.");
//			myMap.set(2825,"Evitabile.");
//			myMap.set(4501,"Evitabile.");
//			myMap.set(556,"Scommessa da ultimo slot.");
//			myMap.set(264,"Evitabile.");
//			myMap.set(5499,"Se la gioca con Tameze, probabilmente gli è superiore.");
//			myMap.set(2117,"Fantacalcisticamente poco interessante.");
//			myMap.set(2107,"Classico slot scommessa, non aspettatevi molto, ma è uno dei principali candidati per sorprendere nelle piccole.");
//			myMap.set(5689,"E' una scommessa, ma di quelle di grande qualità: si può prendere, sperando che esploda durante la stagione.");
//			myMap.set(4435,"Giocatore talentuoso, avrà spazio da esterno o punta.");
//			myMap.set(2743,"Riserva.");
//			myMap.set(5391,"Evitabile.");
//			myMap.set(2304,"Riserva.");
//			myMap.set(1998,"Probabilmente giocherà trequartista, acquistabile solamente in leghe molto numerose.");
//			myMap.set(5012,"Riserva di Immobile.");
//			myMap.set(5505,"Evitabile.");
//			myMap.set(2103,"Terza punta nella gerarchia del Milan, scommessa per cuori forti.");
//			myMap.set(5673,"Giocatore di gran talento, probabilmente sarà sopra Mraz nelle gerarchie.");
//			myMap.set(649,"E' una punta fisica, quello di cui Motta aveva bisogno: per me potrebbe essere il titolare come prima punta, ma a parlare sarà il campo.");
//			myMap.set(2116,"Riserva.");
//			myMap.set(805,"Riserva sulla carta.");
//			myMap.set(2174,"Riserva.");
//			myMap.set(2214,"Riserva.");
//			myMap.set(327,"Riserva.");
//			myMap.set(393,"Ha giocato la prima subito da titolare, potrebbe avere più spazio del previsto.");
//			myMap.set(2860,"Riserva.");
//			myMap.set(5398,"Riserva, ma si può prendere con Karsdorp.");
//			myMap.set(2141,"Tecnicamente riserva.");
//			myMap.set(45,"Indietro nelle gerarchie.");
//			myMap.set(1868,"Indietro nelle gerarchie.");
//			myMap.set(4979,"Riserva.");
//			myMap.set(521,"Riserva.");
//			myMap.set(224,"Riserva.");
//			myMap.set(256,"Riserva.");
//			myMap.set(5691,"Acquisto di prospettiva del Toro, ha diversa gente avanti a lui.");
//			myMap.set(4378,"Difensore di prospettiva, non di più.");
//			myMap.set(5695,"Può insediare i terzini sinistri in rosa, è un giocatore con buone qualità, ma parte indietro nelle gerarchie.");
//			myMap.set(272,"Al momento è il titolare in difesa, ma visto il ruolo è totalmente evitabile.");
//			myMap.set(5286,"Avrà spazio da subentrato, rimane valido solo per leghe molto numerose.");
//			myMap.set(5329,"Al momento offre titolarità.");
//			myMap.set(5486,"Momentaneo titolare?");
//			myMap.set(5119,"Possibile sorpresa low-cost, può regalare qualche assist se dovesse acquisire titolarità.");
//			myMap.set(2815,"Secondo della Fiorentina, da prendere con Dragowski.");
//			myMap.set(5354,"Possibile scommessa da ultimo slot.");
//			myMap.set(4888,"Entra neli ballottaggi di difesa, ma niente di particolare.");
//			myMap.set(4946,"Al Verona potrà giocare di più, ma non aspettatevi faville.");
//			myMap.set(4397,"Riserva.");
//			myMap.set(1847,"La sua posizione può essere messa in discussione da Ampadu, c'è di meglio.");
//			myMap.set(11,"Riserva. Interessante solo se va via.");
//			myMap.set(4976,"Riserva.");
//			myMap.set(390,"Riserva, cercherà di ritagliarsi uno spazio ma le condizioni fisiche sono precarie.");
//			myMap.set(4945,"Riserva di Vina/Spinazzola.");
//			myMap.set(5466,"In ballottaggio per un posto da titolare, ma si può evitare.");
//			myMap.set(2869,"Momentaneo titolare del terzetto, Juric gli pone molta fiducia.");
//			myMap.set(5503,"Si alternerà con Zortea.");
//			myMap.set(5439,"Scommessa da ultimo slot.");
//			myMap.set(5500,"Troverà spazio da titolare o subentrato, piace molto a Di Francesco.");
//			myMap.set(4,"Gasperini tende a far saltuariamente turnover dei propri portieri, quindi va comprato assolutamente con Musso.");
//			myMap.set(799,"Secondo portiere Bologna.");
//			myMap.set(1946,"Secondo del Cagliari.");
//			myMap.set(318,"Secondo Genoa.");
//			myMap.set(1889,"Terzo portiere Inter.");
//			myMap.set(1843,"Secondo portiere Inter.");
//			myMap.set(218,"Secondo Juventus.");
//			myMap.set(1934,"Secondo Lazio, da prendere assolutamente con Reina.");
//			myMap.set(160,"Secondo Milan.");
//			myMap.set(2468,"Secondo Napoli, da prendere assolutamente con Meret (seppur Spalletti tenda ad usare solo un portiere).");
//			myMap.set(2781,"Secondo portiere Roma.");
//			myMap.set(4977,"Secondo portiere Sampdoria.");
//			myMap.set(510,"Secondo Sassuolo.");
//			myMap.set(2814,"In ballottaggio con Zoet.");
//			myMap.set(316,"Da prendere assolutamente con Savic, può sostituirlo in caso di sue prestazioni negative.");
//			myMap.set(5478,"Portiere attualmente titolare al Venezia, in attesa del ritorno di Lezzerini.");
//			myMap.set(4996,"Secondo portiere Verona.");
//			myMap.set(5422,"Interessante per chi gioca con le riconferme.");
//			myMap.set(5459,"Da prendere come ultimo slot scommessa.");
			
			
			$rootScope.dettaglio=function(cId){
				return myMap.get(cId);
			}
			$rootScope.caricaFile = function(tipoFile){
				$rootScope.caricamentoInCorso=true;
				var f = document.getElementById('file').files[0], r = new FileReader();
                r.onloadend = function(e) {
			    var data = e.target.result;
				$rootScope.tokenDispositiva=Math.floor(Math.random()*(10000)+1);
				$resource('./caricaFile',{}).save({'file':btoa(data), 'tipo' : tipoFile,'idgiocatore':$rootScope.idgiocatore,'tokenDispositiva':$rootScope.tokenDispositiva}).$promise.then(function(data) {
					if(data.esitoDispositiva == 'OK'){
						$rootScope.caricamentoInCorso=false;
					}
					else {
						alert('Carica file. Errore!')
					}
						
					});
			    }
			    r.readAsBinaryString(f);
			}
			$rootScope.ordinaUtente= function(u,verso) {
				angular.forEach($rootScope.elencoAllenatori, function(value,chiave) {
					if (verso == 'D'){
						if(u.ordine==value.ordine){
							value.nuovoOrdine=u.ordine+1;
						} else  if(u.ordine+1==value.ordine){
							value.nuovoOrdine=u.ordine;
						} else {
							value.nuovoOrdine=value.ordine;
						}
					}
					if (verso == 'U'){
						if(u.ordine==value.ordine){
							value.nuovoOrdine=u.ordine-1;
						} else  if(u.ordine-1==value.ordine){
							value.nuovoOrdine=u.ordine;
						} else {
							value.nuovoOrdine=value.ordine;
						}
							
					}
				});
				angular.forEach($rootScope.elencoAllenatori, function(value,chiave) {
					value.ordine=value.nuovoOrdine;
				});
			}
			$rootScope.calcolaClassePresiPerRuolo=function(tipo,nome){
				var x=$rootScope.getFromMapSpesoTotale('CONTA'+tipo,nome);
				if(tipo=='ALL') tipo='A';
				var minimo  =eval('$rootScope.min' + tipo);
				var massimo =eval('$rootScope.max' + tipo);
				if($rootScope.isMantra && tipo=='P' && $rootScope.getFromMapSpesoTotale('CONTAALL',nome)>=$rootScope.maxA){
					return 'tuttiPresi';
				}
				if (x>=massimo)
					return 'tuttiPresi';
				else if (x>=minimo)
					return 'minPresi';				
				else
					return 'nonTuttiPresi';
				
			}
			$rootScope.calcolaClassePresi=function(tipo,nome){
					var esitoP=$rootScope.calcolaClassePresiPerRuolo('P',nome);
					var esitoD='tuttiPresi';
					if (!$rootScope.isMantra) esitoD=$rootScope.calcolaClassePresiPerRuolo('D',nome);
					var esitoC='tuttiPresi';
					if (!$rootScope.isMantra) esitoC=$rootScope.calcolaClassePresiPerRuolo('C',nome);
					var esitoA='tuttiPresi';
					if (!$rootScope.isMantra) esitoA=$rootScope.calcolaClassePresiPerRuolo('A',nome);
					var esitoAll='tuttiPresi';
					if ($rootScope.isMantra) esitoAll=$rootScope.calcolaClassePresiPerRuolo('ALL',nome);
					if(esitoP=='tuttiPresi' && esitoD=='tuttiPresi' && esitoC=='tuttiPresi' && esitoA=='tuttiPresi' && esitoAll=='tuttiPresi') return 'tuttiPresi';
					if((esitoAll=='tuttiPresi' || esitoAll=='minPresi') && (esitoP=='tuttiPresi' || esitoP=='minPresi') && (esitoD=='tuttiPresi' || esitoD=='minPresi')
							&& (esitoC=='tuttiPresi' || esitoC=='minPresi') && (esitoA=='tuttiPresi' || esitoA=='minPresi')) return 'minPresi';
					return 'nonTuttiPresi';
			}
			$rootScope.aggiornaConfigLega= function(amministratore) {
				var checkNome=[];
				var ok=true;
				angular.forEach($rootScope.elencoAllenatori, function(value,chiave) {
					var nuovoNome=value.nuovoNome.toUpperCase();
					if(checkNome.indexOf(nuovoNome) !== -1) {
						ok=false;
					}					
					checkNome.push(nuovoNome);
				});
				if (!ok) {
					alert("Errore!! Nomi non univoci");
				} if($rootScope.durataAstaDefault<=0) {
					alert("La durata asta deve essere maggiore di 0");
				}
				else {
					$rootScope.tokenDispositiva=Math.floor(Math.random()*(10000)+1);
					if($rootScope.isMantra) {
						$rootScope.maxP=$rootScope.maxA;
						$rootScope.numAcquisti=$rootScope.maxA;
					}
					else {
						$rootScope.numAcquisti=$rootScope.maxP+$rootScope.maxD+$rootScope.maxC+$rootScope.maxA;
					}
					$rootScope.numMinAcquisti=$rootScope.minP+$rootScope.minD+$rootScope.minC+$rootScope.minA;
					$resource('./aggiornaConfigLega',{}).save({'durataAsta':$rootScope.durataAstaDefault,'isSingle':$rootScope.isSingle,'isATurni':$rootScope.isATurni,
						'elencoAllenatori':$rootScope.elencoAllenatori,'admin':amministratore,'idgiocatore':$rootScope.idgiocatore,
						'tokenDispositiva':$rootScope.tokenDispositiva,'budget':$rootScope.budget,
						'maxP':$rootScope.maxP,'minP':$rootScope.minP,'maxD':$rootScope.maxD,'minD':$rootScope.minD,
						'maxC':$rootScope.maxC,'minC':$rootScope.minC,'maxA':$rootScope.maxA,'minA':$rootScope.minA,
						'numAcquisti':$rootScope.numAcquisti,'numMinAcquisti':$rootScope.numMinAcquisti
					}).$promise.then(function(data) {
						if(data.esitoDispositiva == 'OK'){
							if (data.nuovoNomeLoggato){
								$rootScope.nomegiocatore=data.nuovoNomeLoggato;
							}
							if(data.isATurni=="S")
								$rootScope.isATurni=true;
							else
								$rootScope.isATurni=false;
							if(data.isSingle=="S")
								$rootScope.isSingle=true;
							else
								$rootScope.isSingle=false;
							if(data.isMantra=="S")
								$rootScope.isMantra=true;
							else
								$rootScope.isMantra=false;
							window.location.href = './index.html';
						}
						else {
							alert('Aggiorna config lega. Errore!')
						}
					});
				}
			}
			$rootScope.addFav = function(calciatore, aggiungi) {
				$resource('./addFav',{}).save({'calciatoreId':calciatore.id,'idgiocatore':$rootScope.idgiocatore,'aggiungi':aggiungi}).$promise.then(function(data) {
				});
				
			}
			$rootScope.doDisconnect = function() {
				$rootScope.sendMsg(JSON.stringify({'operazione':'disconnetti', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
				$rootScope.nomegiocatore="";
				$rootScope.isAdmin=false;
			}
			$rootScope.connectWS = function() {
				var alreadyConnected;
				var deferred = $q.defer();
				deferred.resolve("Hi");
				var loc = window.location, new_uri;
				if (loc.protocol === "https:") {
					new_uri = "wss:";
				} else {
					new_uri = "ws:";
				}
				new_uri += "//" + loc.host;
				new_uri += '/' + "messaggi-websocket";
				document.cookie = 'PAGINA=' + window.location.href + '; path=/';				
				ws = new WebSocket(new_uri);
				ws.onmessage = function(data){
					$rootScope.getMessaggio(data.data);
				}
				ws.onclose = function(){
					console.log("connessione chiusa");
				}
				return deferred.promise;			
			}

			$rootScope.forzaTurno= function(turno) {
				$rootScope.sendMsg(JSON.stringify({'operazione':'forzaTurno', 'turno':turno,'idgiocatore':$rootScope.idgiocatore}));
			}
//			$resource('./giocatoriLiberi',{}).query({'idgiocatore':$rootScope.idgiocatore}).$promise.then(function(data) {
//				$rootScope.calciatori=data;
//			});
			$rootScope.aggiornaLoggerMessaggi=function(){
				$resource('./elencoLoggerMessaggi',{}).query().$promise.then(function(data) {
					$rootScope.loggerMessaggi=data;
				});
			}
			$rootScope.selezionaAllenatoreOperaCome=function(allenatore){
				$rootScope.idgiocatoreOperaCome=allenatore.id;
				$rootScope.nomegiocatoreOperaCome=allenatore.nome;
				
			}
			$rootScope.getFromMapSpesoTotale=function(tipo, nome){
				if (!$rootScope || !$rootScope.mapSpesoTotale) return 0;
				if (tipo=='SPESO'){
					if(!$rootScope.mapSpesoTotale[nome]) return 0;
					return $rootScope.mapSpesoTotale[nome].speso;
				}
				if (tipo=='CONTA'){
					if(!$rootScope.mapSpesoTotale[nome]) return 0;
					return $rootScope.mapSpesoTotale[nome].conta;
				}
				if (tipo=='CONTAP'){
					if(!$rootScope.mapSpesoTotale[nome]||!$rootScope.mapSpesoTotale[nome].contaP) return 0;
					return $rootScope.mapSpesoTotale[nome].contaP;
				}
				if (tipo=='CONTAD'){
					if(!$rootScope.mapSpesoTotale[nome]||!$rootScope.mapSpesoTotale[nome].contaD) return 0;
					return $rootScope.mapSpesoTotale[nome].contaD;
				}
				if (tipo=='CONTAC'){
					if(!$rootScope.mapSpesoTotale[nome]||!$rootScope.mapSpesoTotale[nome].contaC) return 0;
					return $rootScope.mapSpesoTotale[nome].contaC;
				}
				if (tipo=='CONTAA'){
					if(!$rootScope.mapSpesoTotale[nome]||!$rootScope.mapSpesoTotale[nome].contaA) return 0;
					return $rootScope.mapSpesoTotale[nome].contaA;
				}
				if (tipo=='CONTAALL'){
					if(!$rootScope.mapSpesoTotale[nome]||!$rootScope.mapSpesoTotale[nome].contaAll) return 0;
					return $rootScope.mapSpesoTotale[nome].contaAll;
				}
				if (tipo=='SPESOP'){
					if(!$rootScope.mapSpesoTotale[nome]||!$rootScope.mapSpesoTotale[nome].spesoP) return 0;
					return $rootScope.mapSpesoTotale[nome].spesoP;
				}
				if (tipo=='SPESOD'){
					if(!$rootScope.mapSpesoTotale[nome]||!$rootScope.mapSpesoTotale[nome].spesoD) return 0;
					return $rootScope.mapSpesoTotale[nome].spesoD;
				}
				if (tipo=='SPESOC'){
					if(!$rootScope.mapSpesoTotale[nome]||!$rootScope.mapSpesoTotale[nome].spesoC) return 0;
					return $rootScope.mapSpesoTotale[nome].spesoC;
				}
				if (tipo=='SPESOA'){
					if(!$rootScope.mapSpesoTotale[nome]||!$rootScope.mapSpesoTotale[nome].spesoA) return 0;
					return $rootScope.mapSpesoTotale[nome].spesoA;
				}
				if (tipo=='SPESOALL'){
					if(!$rootScope.mapSpesoTotale[nome]||!$rootScope.mapSpesoTotale[nome].spesoAll) return 0;
					return $rootScope.mapSpesoTotale[nome].spesoAll;
				}
				if (tipo=='MAXRILANCIO'){
					if(!$rootScope.mapSpesoTotale || !$rootScope.mapSpesoTotale[nome]){
						return $rootScope.budget-$rootScope.numMinAcquisti+1;
					}else{
						return $rootScope.mapSpesoTotale[nome].maxRilancio;
					}
				}
			}
			$rootScope.cancellaOfferta=function(offerta){
				if (window.confirm("Cancello offerta di:" + offerta.allenatore + " per " + offerta.giocatore + "(" + offerta.ruolo + ") " + offerta.squadra + " vinto a " + offerta.costo)){
					$rootScope.tokenDispositiva=Math.floor(Math.random()*(10000)+1);
					$resource('./cancellaOfferta',{}).save({'offerta':offerta,'idgiocatore':$rootScope.idgiocatore,'tokenDispositiva':$rootScope.tokenDispositiva}).$promise.then(function(data) {
						if(data.esitoDispositiva == 'OK'){
							$rootScope.cronologiaOfferte=data.ret;
						}
						else {
							alert('Cancella offerta. Errore!')
						}
					});
				}
			}
			$rootScope.aggiornaCronologiaOfferte=function(){
				$resource('./elencoCronologiaOfferte',{}).query().$promise.then(function(data) {
					$rootScope.cronologiaOfferte=data;
				});
			}
			$rootScope.selezionaCalciatore=function(calciatore){
				$rootScope.selCalciatore=calciatore.id+'@'+calciatore.nome;
				$rootScope.selCalciatoreId=calciatore.id;
				$rootScope.selCalciatoreRuolo=calciatore.ruolo;
				$rootScope.selCalciatoreMacroRuolo=calciatore.macroRuolo;
				$rootScope.selCalciatoreNome=calciatore.nome;
				$rootScope.selCalciatoreSquadra=calciatore.squadra;
				$rootScope.selCalciatoreUnder23=calciatore.under23;
			}
			$rootScope.confermaConfigIniziale=function(){
				if ($rootScope.numeroUtenti>0){
					if($rootScope.isMantra) {
						$rootScope.maxP=$rootScope.maxA;
						$rootScope.numAcquisti=$rootScope.maxA;
					}
					else {
						$rootScope.numAcquisti=$rootScope.maxP+$rootScope.maxD+$rootScope.maxC+$rootScope.maxA;
					}
					$rootScope.numMinAcquisti=$rootScope.minP+$rootScope.minD+$rootScope.minC+$rootScope.minA;
					$resource('./inizializzaLega',{}).save({'minP':$rootScope.minP,'minD':$rootScope.minD,'minC':$rootScope.minC,'minA':$rootScope.minA,'maxP':$rootScope.maxP,'maxD':$rootScope.maxD,'maxC':$rootScope.maxC,'maxA':$rootScope.maxA,'numAcquisti':$rootScope.numAcquisti,'numMinAcquisti':$rootScope.numMinAcquisti,'durataAsta':$rootScope.durataAstaDefault,'budget':$rootScope.budget,'numUtenti':$rootScope.numeroUtenti,'isATurni':$rootScope.isATurni,'isSingle':$rootScope.isSingle,'isMantra':$rootScope.isMantra}).$promise.then(function(data) {
						if(data.esitoDispositiva == 'OK'){
							$rootScope.ricaricaIndex(false);
							if(data.isATurni=="S")
								$rootScope.isATurni=true;
							else
								$rootScope.isATurni=false;
							if(data.isSingle=="S")
								$rootScope.isSingle=true;
							else
								$rootScope.isSingle=false;
							if(data.isMantra=="S")
								$rootScope.isMantra=true;
							else
								$rootScope.isMantra=false;
					    	$rootScope.connectWS().then(function(){
						        setTimeout(function () {
									$rootScope.callDoConnect("GIOC0",0,"");
									window.location.href = './admin.html';
						        }, 1000);
					        });
						}
						else {
							alert('Conferma config iniziale. Errore!')
						}
					});
				}
			}
			$rootScope.ritornaIndex=function(){
				window.location.href = './index.html';
			}
			$rootScope.caricaAdmin=function(){
				window.location.href = './admin.html';
			}
			$rootScope.azzera=function(){
				if (window.confirm("Sicuro??????????? CANCELLERAI TUTTO IL DB")){
					$rootScope.tokenDispositiva=Math.floor(Math.random()*(10000)+1);
					$resource('./azzera',{}).save({'conferma':'S','idgiocatore':$rootScope.idgiocatore,'tokenDispositiva':$rootScope.tokenDispositiva}).$promise.then(function(data) {
						if(data.esitoDispositiva == 'OK'){
							$rootScope.sendMsg(JSON.stringify({'operazione':'azzera', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
							$rootScope.ricaricaIndex(true);
						}
						else {
							alert('Azzera. Errore!')
						}

					});
				}
			}
			$rootScope.ricaricaIndex=function(chiudi){
				$resource('./init',{}).get().$promise.then(function(data) {
					$rootScope.connectWS();
					if (data.DA_CONFIGURARE){
						$rootScope.config=true;
						$rootScope.scegliMantra();
						if(chiudi){
							window.location.href = './index.html';
						}
					} else {
						$rootScope.config=false;
						$rootScope.nomegiocatore=data.giocatoreLoggato;
						if ($rootScope.nomegiocatore){
							$rootScope.idgiocatore=data.idLoggato;
//							$rootScope.doConnect();
							$rootScope.pinga();
						}
						if(data.isATurni=="S")
							$rootScope.isATurni=true;
						else
							$rootScope.isATurni=false;
						if(data.isSingle=="S")
							$rootScope.isSingle=true;
						else
							$rootScope.isSingle=false;
						if(data.isMantra=="S")
							$rootScope.isMantra=true;
						else
							$rootScope.isMantra=false;
						$rootScope.utenti=data.utenti;
						$rootScope.turno=data.turno;
						$rootScope.budget=data.budget;
						$rootScope.durataAsta=data.durataAsta;
						$rootScope.durataAstaDefault=data.durataAsta;
						$rootScope.numAcquisti=data.numAcquisti;
						$rootScope.numMinAcquisti=data.numMinAcquisti;
						$rootScope.maxP=data.maxP;
						$rootScope.maxD=data.maxD;
						$rootScope.maxC=data.maxC;
						$rootScope.maxA=data.maxA;
						$rootScope.minP=data.minP;
						$rootScope.minD=data.minD;
						$rootScope.minC=data.minC;
						$rootScope.minA=data.minA;
						$rootScope.nomeGiocatoreTurno=data.nomeGiocatoreTurno;
						$rootScope.giocatoriPerSquadra=data.giocatoriPerSquadra;
						$rootScope.mapSpesoTotale=data.mapSpesoTotale;
						$rootScope.elencoAllenatori=data.elencoAllenatori;
						$rootScope.aggiornaTimePing($rootScope.timePing);
						$rootScope.calciatori=data.calciatori;
						if ($rootScope.idgiocatore>-1) {
							$rootScope.preferiti=data.preferiti[$rootScope.idgiocatore];
							$rootScope.coreografaPreferiti();
						}
					}
	            })}

			$rootScope.coreografaPreferiti=function(){
				if ($rootScope.preferiti){
					angular.forEach($rootScope.calciatori, function(value,chiave) {
						if ($rootScope.preferiti.indexOf(value.id)>-1) value.preferito=true; else value.preferito=false; 
					});
				}
				
			}
			
			$rootScope.ricaricaIndex(false);
			$rootScope.sendMsg=function(s){
				try {
				    if (ws.readyState === 1) {
				    	ws.send(s);
				    	$rootScope.connessioneKO="";
				    }
				    else 
				    {
				    	if (ws.readyState ==0){
					    	$rootScope.connessioneKO="Connessione al backend in corso...";
				    	}
				    	else if (ws.readyState ==3){
					    	$rootScope.connessioneKO="Backend non raggiungibile";
					    	$rootScope.connectWS().then(function(){
						        setTimeout(function () {
									$rootScope.ricaricaIndex(false);
						        }, 1000);
					        });
				    	}
				    	else {
					    	$rootScope.connessioneKO="Errore di connessione";
				    	}
				    }
	            } catch (error) {
			    	$rootScope.connessioneKO=error;
	            }				
			}
			$rootScope.latenza = function(u){
				var ret = -1;
				angular.forEach($rootScope.pingUtenti, function(value,chiave) {
					if(chiave == u.nome)
						ret = value.checkPing;
					});
				return ret;


				
				return u.checkPing;
			}
			$rootScope.getMessaggio = function(message){
				if (message){
					var msg = JSON.parse(message);
//					console.log(msg);
					if (msg.RICHIESTA){
						var t=msg.RICHIESTA + "-" + new Date().getTime();
//						console.log(t);
						$rootScope.RICHIESTA=t;
					}
					if (msg.azzera){
						if(msg.azzera=='X' || msg.azzera==$rootScope.idgiocatore){
							$resource('./cancellaSessioneNomeUtente',{}).save().$promise.then(function(data) {
								$rootScope.idgiocatore="";
								$rootScope.nomegiocatore="";
								if(msg.azzera=='X') $rootScope.elencoAllenatori=[];
						});
					}
					}
					if (msg.isATurni){
						if (msg.isATurni=="S")
							$rootScope.isATurni=true;
						else
							$rootScope.isATurni=false;
					}
					if (msg.isSingle){
						if (msg.isSingle=="S")
							$rootScope.isSingle=true;
						else
							$rootScope.isSingle=false;
					}
					if (msg.avviaAsta){
						$rootScope.forzaAllenatore="";
						$rootScope.forzaOfferta=0;
						$rootScope.firstAbilitaForza=false;
					}
					if (msg.isMantra){
						if (msg.isMantra=="S")
							$rootScope.isMantra=true;
						else
							$rootScope.isMantra=false;
					}
					if (msg.selCalciatoreMacroRuolo){
						$rootScope.selCalciatoreMacroRuolo=msg.selCalciatoreMacroRuolo;
					}
					if (msg.loggerMessaggi){
						$rootScope.loggerMessaggi=msg.loggerMessaggi;
					}
					if (msg.elencoAllenatori){
						$rootScope.elencoAllenatori=msg.elencoAllenatori;
					}
					if (msg.utentiRinominati){
						angular.forEach(msg.utentiRinominati, function(nuovoNome,vecchioNome) {
							if ($rootScope.nomegiocatore==vecchioNome){
								$rootScope.nomegiocatore=nuovoNome;
								$resource('./aggiornaSessioneNomeUtente',{}).save({'nuovoNome':nuovoNome}).$promise.then(function(data) {
								});
								
							}
						});
					}
					if (msg.verificaDispositiva){
						if (msg.verificaDispositiva==$rootScope.idgiocatore){
							if($rootScope.tokenDispositiva>=0){
								$rootScope.sendMsg(JSON.stringify({'operazione':'verificaDispositiva', 'tokenDispositiva':$rootScope.tokenDispositiva, 'idgiocatore':$rootScope.idgiocatore}));
								$rootScope.tokenDispositiva=-1;
							}
						}
					}
					if (msg.calciatori){
						$rootScope.calciatori=msg.calciatori;
						$rootScope.coreografaPreferiti();
					}
					if (msg.preferiti){
						if ($rootScope.idgiocatore>-1) {
							$rootScope.preferiti=msg.preferiti[$rootScope.idgiocatore];
							$rootScope.coreografaPreferiti();
						}
					}
					if (msg.cronologiaOfferte){
						$rootScope.cronologiaOfferte=msg.cronologiaOfferte;
					}
					if (msg.messaggi){
						//$rootScope.messaggi.push(msg.messaggio);
						$rootScope.messaggi=msg.messaggi;
					}
					if (msg.utenti){
						$rootScope.utenti=msg.utenti;
					}
					if (msg.pingUtenti){
						$rootScope.pingUtenti=msg.pingUtenti;
					}
					if (msg.RESET_UTENTE){
						if (msg.RESET_UTENTE==$rootScope.tokenUtente){
							$rootScope.nomegiocatore="";
							alert("Utente esistente. Riconnettiti!");
						}
					}			
					if (msg.clearOfferta){
						$rootScope.clearOfferta();
					}
					if (msg.giocatoreTimeout){
						$rootScope.giocatoreTimeout=msg.giocatoreTimeout;
					}
					if (msg.millisFromPausa){
						$rootScope.millisFromPausa=msg.millisFromPausa;
					}
					if (msg.timeout){
						if (msg.timeout=='N') $rootScope.timeout=null; else $rootScope.timeout=msg.timeout;
					}
					if (msg.offertaVincente){
						$rootScope.offertaVincente=msg.offertaVincente;
						if($rootScope.isSingle){
							$rootScope.offerta=$rootScope.offertaVincente.offerta;
						} else{
							if($rootScope.autoAllineaOC) {
								$rootScope.offertaPrivOC=$rootScope.offertaVincente.offerta;
							}
							if($rootScope.autoAllinea) {
								$rootScope.offertaPriv=$rootScope.offertaVincente.offerta;
							}
						}
						if (msg.offertaVincente.confermaForza){
							if ($rootScope.tokenCasuale==msg.offertaVincente.tokenCasuale) $rootScope.conferma();
						}
					}
					if (msg.durataAsta){
						$rootScope.durataAsta=msg.durataAsta;
					}
					if (msg.contaTempo){
						if (msg.contaTempo>$rootScope.durataAsta*1000)
							$rootScope.contaTempo=$rootScope.durataAsta*1000;
						else
							$rootScope.contaTempo=msg.contaTempo;
					}
					if (msg.sSemaforoAttivo){
						if (msg.sSemaforoAttivo=='S')
							$rootScope.bSemaforoAttivo=true;
						else
							$rootScope.bSemaforoAttivo=false;
					}
					if (msg.timeStart){
						$rootScope.timeStart=msg.timeStart;
						if (msg.timeStart==3 && !$rootScope.firstAbilitaForza) {
							$rootScope.forzaOfferta=$rootScope.offertaVincente.offerta;
							$rootScope.forzaAllenatore=$rootScope.offertaVincente.idgiocatore;
							$rootScope.firstAbilitaForza=true;
							$rootScope.abilitaForza=true;
						}
					}
					if(msg.turno){
						$rootScope.turno=msg.turno;
					}
					if(msg.nomeGiocatoreTurno){
						$rootScope.nomeGiocatoreTurno=msg.nomeGiocatoreTurno;
					}
					if(msg.mapSpesoTotale){
						$rootScope.mapSpesoTotale=msg.mapSpesoTotale;
					}
					if(msg.giocatoriPerSquadra){
						$rootScope.giocatoriPerSquadra=msg.giocatoriPerSquadra;
					}
					if (msg.utentiScaduti){
						$rootScope.utentiScaduti=msg.utentiScaduti;
					}
					$rootScope.$apply();
				}
			}
			$rootScope.pinga = function(){
//				if ($rootScope.nomegiocatore)
					$rootScope.sendMsg(JSON.stringify({'operazione':'ping','nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
			}
			$rootScope.aggiornaTimePing= function(timePing) {
				$interval.cancel(a);
				a=$interval(function() {$rootScope.pinga();}, timePing);
			}
			var a=$interval(function() {$rootScope.pinga();}, $rootScope.timePing);
			$rootScope.liberaSemaforo = function() {
				$rootScope.bSemaforoAttivo=true;
				$rootScope.sendMsg(JSON.stringify({'operazione':'liberaSemaforo'}));
			}
/*			$rootScope.verificaAvviaAsta = function(ng) {
				if(!$rootScope.selCalciatoreMacroRuolo) return true;
				var max;
				if($rootScope.selCalciatoreMacroRuolo == 'P') max=$rootScope.maxP;
				if($rootScope.selCalciatoreMacroRuolo == 'D') max=$rootScope.maxD;
				if($rootScope.selCalciatoreMacroRuolo == 'C') max=$rootScope.maxC;
				if($rootScope.selCalciatoreMacroRuolo == 'A') max=$rootScope.maxA;
				return $rootScope.getFromMapSpesoTotale('CONTA'+$rootScope.selCalciatoreMacroRuolo,ng)<max;
			}*/

			$rootScope.iniziaTurno = function(ng,turno) {
				angular.forEach($rootScope.elencoAllenatori, function(value,chiave) {
					if(value.ordine == turno)
						$rootScope.inizia(ng,value.id);
					});
			}
			
			$rootScope.inizia = function(ng,ig) {
				$rootScope.bSemaforoAttivo=false;
				$rootScope.timeStart=0;
				$rootScope.contaTempo=0;
				$rootScope.sendMsg(JSON.stringify({'operazione':'start', 'selCalciatoreMacroRuolo':$rootScope.selCalciatoreMacroRuolo,'selCalciatore':$rootScope.selCalciatore, 'nomegiocatoreOperaCome':$rootScope.nomegiocatore, 'idgiocatoreOperaCome':$rootScope.idgiocatore,'nomegiocatore':ng,'idgiocatore':ig}));
				$rootScope.selCalciatore="";
				if (false){//PROVE LETTURA
					$resource('./leggi',{}).save({'nome':$rootScope.selCalciatoreNome}).$promise.then(function(data) {
						var audio = new Audio('./riproduci.wav');
						audio.play();
					});
				}
			}
			$rootScope.azzeraTempo=function(){
						$rootScope.sendMsg(JSON.stringify({'operazione':'azzeraTempo', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
			};
			$rootScope.conferma = function(){
				$rootScope.messaggi=[];
				$rootScope.bSemaforoAttivo=true;
				$rootScope.tokenDispositiva=Math.floor(Math.random()*(10000)+1);
				$resource('./confermaAsta',{}).save({'offerta':$rootScope.offertaVincente,'idgiocatore':$rootScope.idgiocatore,'tokenDispositiva':$rootScope.tokenDispositiva}).$promise.then(function(data) {
					if(data.esitoDispositiva == 'OK'){
						$rootScope.sendMsg(JSON.stringify({'operazione':'confermaAsta', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
					}
					else {
						alert('Conferma. Errore!')
					}
				});
			}
			$rootScope.clearOfferta=function(){
				$rootScope.offertaVincente="";
				$rootScope.filterRuolo="";
//				$rootScope.filterMacroRuolo="";
				$rootScope.filterNome="";
				$rootScope.filterSquadra="";
				$rootScope.filterQuotazione="";
				$rootScope.filterPreferito=false;
				$rootScope.selCalciatore="";
				$rootScope.selCalciatoreId="";
				$rootScope.selCalciatoreRuolo="";
				$rootScope.selCalciatoreMacroRuolo="";
				$rootScope.selCalciatoreNome="";
				$rootScope.selCalciatoreSquadra="";
				$rootScope.selCalciatoreSquadra="";
				$rootScope.offertaOC=1;
				$rootScope.offerta=1;
				$rootScope.offertaPrivOC=1;
				$rootScope.offertaPriv=1;
				$rootScope.idgiocatoreOperaCome=-1;
				$rootScope.nomegiocatoreOperaCome="";
			}
			$rootScope.aggiornaAutoAllinea = function(accendi){
				$rootScope.autoAllinea=accendi;
			}
			$rootScope.allineaOfferta = function(){
				$rootScope.offerta=$rootScope.offertaVincente.offerta;
			}
			$rootScope.forza= function(conferma){
				$rootScope.tokenCasuale=Math.floor(Math.random()*(100000)+1);
				$rootScope.sendMsg(JSON.stringify({'operazione':'forza', 'conferma':conferma, 'tokenCasuale':$rootScope.tokenCasuale,'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore,'forzaAllenatore':$rootScope.forzaAllenatore,'forzaOfferta':$rootScope.forzaOfferta}));
				$rootScope.abilitaForza=false;
			}
			$rootScope.incrementaOfferta = function(ng,ig,val){
				$rootScope.offerta=$rootScope.offerta+val;
				$rootScope.inviaOfferta(ng,ig);
			}
			$rootScope.azzeraOfferta = function(){
				$rootScope.offerta=1;
				$rootScope.inviaOfferta($rootScope.offertaVincente.nomegiocatore,$rootScope.offertaVincente.idgiocatore,true);
			}
			
			$rootScope.inviaOffertaLibera = function(ng,ig,val){
				$rootScope.offerta=val;
				$rootScope.inviaOfferta(ng,ig);
			}
			$rootScope.annulla = function(){
				if (window.confirm("Annullo offerta di:" + $rootScope.offertaVincente.nomegiocatore + " per " + $rootScope.offertaVincente.giocatore.nome + "(" + $rootScope.offertaVincente.giocatore.ruolo + ") " + $rootScope.offertaVincente.giocatore.squadra + " vinto a " + $rootScope.offertaVincente.offerta)){
					$rootScope.messaggi=[];
					$rootScope.bSemaforoAttivo=true;
					$rootScope.sendMsg(JSON.stringify({'operazione':'annullaAsta', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
				}
			}
			$rootScope.inviaOfferta = function(ng,ig,azzera){
				$rootScope.offertaPriv=$rootScope.offerta;
				$rootScope.offertaPrivOC=$rootScope.offerta;
				$rootScope.sendMsg(JSON.stringify({'operazione':'inviaOfferta', 'maxRilancio':$rootScope.getFromMapSpesoTotale('MAXRILANCIO',ng),'nomegiocatore':ng, 'idgiocatore':ig, 'nomegiocatoreOperaCome':$rootScope.nomegiocatore, 'idgiocatoreOperaCome':$rootScope.idgiocatore, 'offerta':$rootScope.offerta,'azzera':azzera}));
			}
			$rootScope.cancellaUtente = function(u) {
				$rootScope.sendMsg(JSON.stringify({'operazione':'cancellaUtente', 'nomegiocatore':u.nome, 'idgiocatore':u.id}));
			}
			$rootScope.terminaAsta= function() {
				$rootScope.sendMsg(JSON.stringify({'operazione':'terminaAsta', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
			}
			$rootScope.resumeAsta= function() {
				$rootScope.sendMsg(JSON.stringify({'operazione':'resumeAsta', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
			}
			$rootScope.pausaAsta= function() {
				$rootScope.sendMsg(JSON.stringify({'operazione':'pausaAsta', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
			}
			
			$rootScope.calcolaIsAdmin= function() {
				$rootScope.isAdmin=false;
				angular.forEach($rootScope.elencoAllenatori, function(value,chiave) {
					if(value.id == $rootScope.idgiocatore)
						if(value.isAdmin) $rootScope.isAdmin=true;
					});
			}
			$rootScope.$watch("elencoAllenatori", function(newValue, oldValue) {
				$rootScope.calcolaIsAdmin();
			});
			$rootScope.$watch("selCalciatoreMacroRuolo", function(newValue, oldValue) {
				if(!newValue) return true;
				var max;
				if (!$rootScope.isMantra){
					if(newValue == 'P') max=$rootScope.maxP;
					if(newValue == 'D') max=$rootScope.maxD;
					if(newValue == 'C') max=$rootScope.maxC;
					if(newValue == 'A') max=$rootScope.maxA;
				}
				$rootScope.avviabili=[];
				angular.forEach($rootScope.elencoAllenatori, function(value,chiave) {
					var avv=false;
					if ($rootScope.isMantra){
						max=$rootScope.maxA;
//						if(newValue!='P'){
//							max=max-$rootScope.minP+$rootScope.getFromMapSpesoTotale('CONTAP',value.nome);
//						}
						if($rootScope.getFromMapSpesoTotale('CONTAALL',value.nome)<max) avv=true;
					} 
					else {
						if($rootScope.getFromMapSpesoTotale('CONTA'+newValue,value.nome)<max) avv=true;
					}
					if(avv && $rootScope.getFromMapSpesoTotale('MAXRILANCIO',value.nome)<=0) avv=false;
					if(avv){
						$rootScope.avviabili.push(value.nome)					
					}
				});
				

			});
			
			
			$rootScope.sort = {
				    column: 'Ruolo',
				    descending: false
				};
			$rootScope.selectedCls = function(column) {
				    return column == $rootScope.sort.column && 'sort-' + $rootScope.sort.descending;
				};

				$rootScope.changeSorting = function(column) {
				    var sort = $rootScope.sort;
				    if (sort.column == column) {
				        sort.descending = !sort.descending;
				    } else {
				        sort.column = column;
				        sort.descending = false;
				    }
				};			
	}
)


app.directive('trackProgressBar', [function () {

  return {
    restrict: 'E', // element
    scope: {
      colVal: '@', // bound to 'col-val' attribute, playback progress
      curVal: '@', // bound to 'cur-val' attribute, playback progress
      maxVal: '@'  // bound to 'max-val' attribute, track duration
    },
    template: '<div class="progress-bar-bkgd"><div class="progress-bar-marker"></div></div>',

    link: function ($scope, element, attrs) {
      // grab element references outside the update handler
      var progressBarBkgdElement = angular.element(element[0].querySelector('.progress-bar-bkgd')),
          progressBarMarkerElement = angular.element(element[0].querySelector('.progress-bar-marker'));

      // set the progress-bar-marker width when called
      function updateProgress() {
        var progress = 0,
            currentValue = $scope.curVal,
            maxValue = $scope.maxVal,
            // recompute overall progress bar width inside the handler to adapt to viewport changes
            progressBarWidth = progressBarBkgdElement.prop('clientWidth');

        if ($scope.maxVal) {
          // determine the current progress marker's width in pixels
          progress = Math.min(currentValue, maxValue) / maxValue * progressBarWidth;
        }

        // set the marker's width
        progressBarMarkerElement.css('width', progress + 'px');
        //console.log(currentValue + "-" + maxValue + "-" + progress + "-" +  $scope.colVal);
        if ($scope.colVal<2) progressBarMarkerElement.css('background-color', 'green');
        if ($scope.colVal==2) progressBarMarkerElement.css('background-color', 'yellow');
        if ($scope.colVal==3) progressBarMarkerElement.css('background-color', 'red');
      }

      // curVal changes constantly, maxVal only when a new track is loaded
      $scope.$watch('curVal', updateProgress);
      $scope.$watch('maxVal', updateProgress);
    }
  };
}]);
app.directive('capitalize', function() {
    return {
      require: 'ngModel',
      link: function(scope, element, attrs, modelCtrl) {
        var capitalize = function(inputValue) {
          if (inputValue == undefined) inputValue = '';
          var capitalized = inputValue.toUpperCase();
          if (capitalized !== inputValue) {
            // see where the cursor is before the update so that we can set it back
            var selection = element[0].selectionStart;
            modelCtrl.$setViewValue(capitalized);
            modelCtrl.$render();
            // set back the cursor after rendering
            element[0].selectionStart = selection;
            element[0].selectionEnd = selection;
          }
          return capitalized;
        }
        modelCtrl.$parsers.push(capitalize);
        capitalize(scope[attrs.ngModel]);
      }
    }});
app.controller('ModalDemoCtrl', function ($uibModal, $log, $rootScope) {
	  var pc = this;
	  pc.data = "..."; 

	  $rootScope.open = function (size,pwd) {
	    var modalInstance = $uibModal.open({
	      animation: true,
	      ariaLabelledBy: 'modal-title',
	      ariaDescribedBy: 'modal-body',
	      templateUrl: 'modale.html',
	      controller: 'ModalInstanceCtrl',
	      controllerAs: 'pc',
	      size: size,
	      resolve: {
	        data: function () {
	          return pwd;
	        }
	      }
	    });
		pc.data=pwd;

	    modalInstance.result.then(function () {
//	      alert("now I'll close the modal");
	    });
	  };
	});

app.controller('ModalInstanceCtrl', function ($uibModalInstance, data, $rootScope, $resource) {
	  var pc = this;
	  pc.data = data;
	  
	  pc.ok = function (modalePwd) {
		  
			$resource('./cripta',{}).get({'pwd':modalePwd,'key':$rootScope.tmpNpme}).$promise.then(function(data) {
				pc.data="CONTROLLO PASSWORD...";
				if (data.value==$rootScope.origPwd){
					$rootScope.nomegiocatore=$rootScope.tmpNpme;
					$rootScope.idgiocatore=$rootScope.tmpId;
					$rootScope.doConnect();
				    $uibModalInstance.close();
		        }
				else{
					pc.data="PASSWORD ERRATA";
				}
			});
		  
		  
	  };

	  pc.cancel = function () {
	    //{...}
//	    alert("You clicked the cancel button."); 
	    $uibModalInstance.dismiss('cancel');
	  };
	});

app.filter('myTableFilter', function($rootScope){
	  return function(dataArray) {
	      if (!dataArray) {
	          return;
	      }
	      else {
	           return dataArray.filter(function(item){
	              var termInMacroRuolo=true;
	              if($rootScope.filterMacroRuolo) termInMacroRuolo=item.macroRuolo.toLowerCase().indexOf($rootScope.filterMacroRuolo.toLowerCase()) > -1;
	              var termInRuolo=true;
	              if($rootScope.filterRuolo) termInRuolo=item.ruolo.toLowerCase().indexOf($rootScope.filterRuolo.toLowerCase()) > -1;
	              var termInNome=true;
	              if($rootScope.filterNome) termInNome = item.nome.toLowerCase().indexOf($rootScope.filterNome.toLowerCase()) > -1;
	              var termInSquadra=true;
	              if($rootScope.filterSquadra) termInSquadra = item.squadra.toLowerCase().indexOf($rootScope.filterSquadra.toLowerCase()) > -1;
	              var termInPreferito=true;
	              if($rootScope.filterPreferito) termInPreferito = item.preferito;
	              var termInQuotazione=true;
	              if($rootScope.filterQuotazione >0) termInQuotazione = item.quotazione >= $rootScope.filterQuotazione;
	              if($rootScope.filterQuotazione <0){
	            	  var tmp=-$rootScope.filterQuotazione;
	            	  termInQuotazione = item.quotazione <= tmp;
	              }
	              return termInRuolo && termInMacroRuolo && termInNome && termInSquadra && termInPreferito && termInQuotazione;
	              
	              
	           });
	      } 
	  }    
	});
