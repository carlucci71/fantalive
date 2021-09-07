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
						$rootScope.abilitaForza=false;
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
			$rootScope.forza= function(){
				$rootScope.sendMsg(JSON.stringify({'operazione':'forza', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore,'forzaAllenatore':$rootScope.forzaAllenatore,'forzaOfferta':$rootScope.forzaOfferta}));
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
