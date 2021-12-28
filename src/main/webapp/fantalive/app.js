var app = angular.module('app', [ 'ngResource','ngAnimate', 'ngSanitize', 'ui.bootstrap' ]);
app.run(
		function($rootScope, $resource, $interval,$q,$timeout){
			$rootScope.configura=false;
			$rootScope.testoLog="";
			$rootScope.getOrari=false;
			$rootScope.getFree=false;
			$rootScope.getLives=false;
			$rootScope.contaNomiDati=-1;
			$rootScope.notificaCampionato="BE";
			$rootScope.notificaSquadra="Universal";
			$rootScope.init=function(){
				$rootScope.connectWS();
					$rootScope.ricaricaIndex().then(function(){
						$rootScope.inizio=new Date();
						$rootScope.fine="";
						$rootScope.loading=true;
						$resource('./getDati',{}).get().$promise.then(function(data) {
							$rootScope.fantaSoccerAuth=data.body;
							$rootScope.giornata=data.giornata;
							$rootScope.eventi=data.eventi;
							$rootScope.loading=false;
							$rootScope.fine=new Date();
						}).catch(function(error) {
							$rootScope.loading=false;
							$rootScope.fine=new Date();
							if (error && error.data && error.data.message)
								alert("Errore-1-1: " + error.data.message);
							else if (error && error.data)
								alert("Errore-1-2: " + error.data);
							else 
								alert("Errore-1-3: " + angular.toJson(error));
						});
					});
			}
			var a=$interval(function() {$rootScope.chkConnectWS();}, 5000);
			$rootScope.$watch("result", function(newValue, oldValue) {
				$rootScope.aggiornamento="";
                angular.forEach(newValue, function(value,chiave) {
                	if (value.aggiornamento){
                		$rootScope.aggiornamento=value.aggiornamento;
                	}
                });
			});
			function base64DecodeUnicode(str) {
			    // Convert Base64 encoded bytes to percent-encoding, and then get the original string.
			    percentEncodedStr = atob(str).split('').map(function(c) {
			        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
			    }).join('');


			    return decodeURIComponent(percentEncodedStr);
			}
			function formatDate(d) {
			        month = '' + (d.getMonth() + 1),
			        day = '' + d.getDate(),
			        year = d.getFullYear();

			        hour = d.getHours();
			        min = d.getMinutes();
			        sec = d.getSeconds();
			        
			    if (month.length < 2) 
			        month = '0' + month;
			    if (day.length < 2) 
			        day = '0' + day;

			    if (hour.length < 2) 
			    	hour = '0' + hour;
			    if (min.length < 2) 
			    	min = '0' + min;
			    if (sec.length < 2) 
			    	sec = '0' + sec;
			    
			    var ret=[year, month, day].join('-');
			    ret=ret + " " + [hour,min,sec].join(':');
			    return ret;
			}
			$rootScope.leftFloating=670;
			$rootScope.shiftFloating = function(sh){
				$rootScope.leftFloating+=sh;
				document.getElementById("floatingRectangle").style.left = $rootScope.leftFloating;
			}
			$rootScope.visPartitaLive="";
			$rootScope.visualizzaLive=function(camp){
				$rootScope.visPartitaLive=camp;
			}
			$rootScope.partitaLive={};
			$rootScope.getResultMyCampionato= function(campionato){
				var ret={};
                angular.forEach($rootScope.result, function(value,camp) {
                	if (campionato==camp){
                		ret=value;
                	}
                });
                return ret;
			}
			$rootScope.getSquadraByCampionatoAndName= function(camp,nomeSq){
				var ret={};
                angular.forEach($rootScope.result, function(value,camp) {
	                angular.forEach(value.squadre, function(sq,chiave2) {
	                	if (camp==camp && nomeSq==sq.nome){
	                		ret=sq;
	                	}
	                });
                });
                return ret;
			}
			$rootScope.toggleCheckbox = function(camp,sq){
				var pos=-1;
				if ($rootScope.partitaLive[camp]){
					pos=$rootScope.partitaLive[camp].indexOf(sq.nome);
				}else{
					$rootScope.partitaLive[camp]=[];
				}
				if (pos==-1) {
					$rootScope.partitaLive[camp].push(sq.nome);
				} else {
					$rootScope.partitaLive[camp].splice(pos,1);
				}
			};
			$rootScope.getMessaggio = function(message){
				if (message){
					var msg = JSON.parse(message);
//					console.log(msg);
					if (msg.res){
						$rootScope.result=msg.res;
		                angular.forEach($rootScope.result, function(value,camp) {
			                angular.forEach(value.squadre, function(sq,chiave2) {
			                	if ($rootScope.partitaLive[camp].indexOf(sq.nome)>-1){
//			    					$rootScope.partitaLive[camp].push(sq.nome);
			                	}
			                });
		                });

						
					}
					if (msg.timeRefresh){
						$rootScope.timeRefresh=msg.timeRefresh;
					}
					if (msg.notifica){
						$rootScope.testoLog="<b>Notifica " + formatDate(new Date()) + "</b>\n" + base64DecodeUnicode(msg.notifica) + "\n" + $rootScope.testoLog ;
					}
					if (msg.miniNotifica){
						$rootScope.testoLog="<b>Live " + formatDate(new Date()) + "</b>\n" + base64DecodeUnicode(msg.miniNotifica) + "\n" + $rootScope.testoLog ;
					}
					if (msg.liveFromFile){
						$rootScope.liveFromFile=msg.liveFromFile;
					}
					if (msg.disabilitaNotificaTelegram){
						$rootScope.disabilitaNotificaTelegram=msg.disabilitaNotificaTelegram;
					}
					if (msg.runningBot){
						$rootScope.runningBot=msg.runningBot;
					}
//					if (msg.lastKeepAlive){
//						$rootScope.lastKeepAlive=msg.lastKeepAlive;
//					}
					if (msg.lastRefresh){
						$rootScope.lastRefresh=msg.lastRefresh;
					}
					if (msg.keepAliveEnd){
						$rootScope.keepAliveEnd=msg.keepAliveEnd;
					}
					if (msg.visKeepAlive){
						$rootScope.visKeepAlive=msg.visKeepAlive;
					}
				}
				$rootScope.$apply();
			};
			$rootScope.startStopBot = function() {
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./startStopBot',{}).save().$promise.then(function(data) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
				}).catch(function(error) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
					if (error && error.data && error.data.message)
						alert("Errore-2-1: " + error.data.message);
					else if (error && error.data)
						alert("Errore-2-2: " + error.data);
					else 
						alert("Errore-2-3: " + angular.toJson(error));
				});
			}
			$rootScope.chkConnectWS = function() {
				if (!ws)
				{ 
					$rootScope.connectWS();					
				}
				else if (ws.readyState != 1) {
					$rootScope.connectWS();					
				}
			}
			$rootScope.getNomiTesto = function(){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./getNomiTesto',{}).get({}).$promise.then(function(data) {
					$rootScope.nomiTesto=data.nomiTesto;
					$rootScope.loading=false;
					$rootScope.fine=new Date();
				});
				
			}
			$rootScope.getNomiData = function(){
				$rootScope.fileLives="";
				$rootScope.fileOrari="";
				$rootScope.testoLog="";
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./getNomiData',{}).get({}).$promise.then(function(data) {
					$rootScope.nomiData=data.nomiTesto;
					$rootScope.contaNomiDati=$rootScope.nomiData.length-1;
					$rootScope.loading=false;
					$rootScope.fine=new Date();
				});
				
			}
			$rootScope.svecchiaFile = function(tipoFile){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./svecchiaFile',{}).get({}).$promise.then(function(data) {
					$rootScope.nomiTesto=data.nomiTesto;
					$rootScope.loading=false;
					$rootScope.fine=new Date();
				});
				
			}
			$rootScope.putTestiFromData = function(){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./caricaFileFromData',{}).save({'snapPartite':$rootScope.fileOrari,'lives':$rootScope.fileLives}).$promise.then(function(data) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
				});
			}
			$rootScope.getPutTestiFromData = function(){
				$rootScope.testoLog='';
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				var name=$rootScope.nomiData[$rootScope.contaNomiDati];
				$rootScope.contaNomiDati--;
				$rootScope.fileCaricato=name;
				$resource('./caricaFileFromDataByName',{}).save({'name':name}).$promise.then(function(data) {
					$rootScope.fileLives=data.lives;
					$rootScope.fileOrari=data.orari;
					$rootScope.loading=false;
					$rootScope.fine=new Date();
				});
			}
			$rootScope.verificaNotifica = function(){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$rootScope.contaNomiDati--;
				$rootScope.testoLog='';
				$resource('./verificaNotifica',{}).save({'campionato':$rootScope.notificaCampionato, 'squadra':$rootScope.notificaSquadra}).$promise.then(function(data) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
				});
			}
			$rootScope.getTestiFromData = function(){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$rootScope.fileCaricato=$rootScope.nomeFileGet;
				$resource('./getTestiFromData',{}).save({'getTestiFromData':$rootScope.nomeFileGet}).$promise.then(function(data) {
					$rootScope.fileLives=data.lives;
					$rootScope.fileOrari=data.orari;
					$rootScope.file=data.file;
					$rootScope.loading=false;
					$rootScope.fine=new Date();
				}).catch(function(error) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
					if (error && error.data && error.data.message)
						alert("Errore-12-1: " + error.data.message);
					else if (error && error.data)
						alert("Errore-12-2: " + error.data);
					else 
						alert("Errore-12-3: " + angular.toJson(error));
				});
				
			}
			$rootScope.getFile = function(tipoFile){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$rootScope.file="";
				if (tipoFile == 'LD'){
					$rootScope.getOrari=false;
					$rootScope.getFree=false;
					$rootScope.getLives=true;
					$resource('./getLivesFromDb',{}).get({}).$promise.then(function(data) {
						$rootScope.file=data.file;
						$rootScope.loading=false;
						$rootScope.fine=new Date();
					});
				}
				if (tipoFile == 'FREE'){
					$rootScope.getFree=true;
					$rootScope.getOrari=false;
					$rootScope.getLives=false;
					$resource('./getFreeFromDb',{}).save({'nomeFileGet':$rootScope.nomeFileGet}).$promise.then(function(data) {
						$rootScope.file=data.file;
						$rootScope.loading=false;
						$rootScope.fine=new Date();
					});
				}
				if (tipoFile == 'OD'){
					$rootScope.getFree=false;
					$rootScope.getOrari=true;
					$rootScope.getLives=false;
					$resource('./getOrariFromDb',{}).get({}).$promise.then(function(data) {
						$rootScope.file=data.file;
						$rootScope.loading=false;
						$rootScope.fine=new Date();
					});
				}
				if (tipoFile == 'LL'){
					$rootScope.getFree=false;
					$rootScope.getOrari=false;
					$rootScope.getLives=true;
					$resource('./getLivesFromLive',{}).get({}).$promise.then(function(data) {
						$rootScope.file=data.file;
						$rootScope.loading=false;
						$rootScope.fine=new Date();
					});
				}
				if (tipoFile == 'OL'){
					$rootScope.getFree=false;
					$rootScope.getOrari=true;
					$rootScope.getLives=false;
					$resource('./getOrariFromLive',{}).get({}).$promise.then(function(data) {
						$rootScope.file=data.file;
						$rootScope.loading=false;
						$rootScope.fine=new Date();
					});
				}
			}
			$rootScope.caricaFile = function(tipoFile){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./caricaFile',{}).save({'file':$rootScope.file,'tipoFile':tipoFile,'nomeFileGet':$rootScope.nomeFileGet}).$promise.then(function(data) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
				});
			}
			$rootScope.connectWS = function() {
				var deferred = $q.defer();
				deferred.resolve("Hi");
				var loc = window.location;
				var new_uri;
				if (loc.protocol === "https:") {
					new_uri = "wss:";
				} else {
					new_uri = "ws:";
				}
				new_uri += "//" + loc.host  + "/fantalive/fantalive-websocket";
				document.cookie = 'PAGINA=' + loc.href + '; path=/';				
				ws = new WebSocket(new_uri);
				ws.onmessage = function(data){
					$rootScope.getMessaggio(data.data);
				}
				ws.onclose = function(){
					console.log("connessione chiusa");
				}
				return deferred.promise;			
			}
			$rootScope.salva=function(r){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./salva',{}).save({'r':r}).$promise.then(function(data) {
					$rootScope.result=data;
					$rootScope.loading=false;
					$rootScope.fine=new Date();
				}).catch(function(error) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
					if (error && error.data && error.data.message)
						alert("Errore-3-1: " + error.data.message);
					else if (error && error.data)
						alert("Errore-3-2: " + error.data);
					else 
						alert("Errore-3-3: " + angular.toJson(error));
				});
			}
			$rootScope.visSimulaCambi=function(r,sq){
				if (!r.conLive) return false;
				var iContaT=0;
                angular.forEach(sq.titolari, function(value,chiave) {
                	if (value.cambio) iContaT++;
                });
				if (iContaT==0) return false;
				var iContaR=0;
                angular.forEach(sq.riserve, function(value,chiave) {
                	if (value.cambio) iContaR++;
                });
				if (iContaT==iContaR) 
					return true;
				else
					return false;
			}
			$rootScope.delPartitaSimulata=function(sq,nome){
				var ps=[];
                angular.forEach(sq.partiteSimulate, function(value,key) {
                	if (value.nome!=nome){
                		ps.push(value);
                	}
                		
                });
				sq.partiteSimulate=ps;
			}
			$rootScope.simulaPartiteLive =function(){
				$rootScope.partiteLive={};
				$rootScope.nomiPartiteLive=[];
                angular.forEach($rootScope.result, function(value,camp) {
	                angular.forEach(value.squadre, function(sq,chiave2) {
	                    angular.forEach(sq.partiteSimulate, function(ps,key) {
	                    	if (!$rootScope.partiteLive[ps.nome]){
	                    		$rootScope.nomiPartiteLive.push(ps.nome);
	                    		$rootScope.partiteLive[ps.nome]={};
	                    		$rootScope.partiteLive[ps.nome].squadre=[];
	                    	}
                    		$rootScope.partiteLive[ps.nome].squadre.push(ps);
	                    	if (ps.casa){
	                    		$rootScope.partiteLive[ps.nome].squadre.reverse();
	                    	}
		                });
	                });
	                $rootScope.nomiPartiteLive.sort();
                    $rootScope.visPartitaLive=$rootScope.nomiPartiteLive[0];
                });
			}
			$rootScope.addPartitaSimulata=function(campionato,squadra,nome,casa){
				var ps={};
				if (casa){
					ps.casa=true;
				}else{
					ps.casa=false;
				}
				ps.campionato=campionato;
				ps.nome=nome;
				ps.squadra=squadra.nome;
				squadra.partiteSimulate.push(ps);
			}
			$rootScope.simulaCambi=function(r,sq){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./simulaCambi',{}).save({'sq':sq}).$promise.then(function(data) {
					r.squadre.splice(sq.prog, 1,data);
					$rootScope.loading=false;
					$rootScope.fine=new Date();
				}).catch(function(error) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
					if (error && error.data && error.data.message)
						alert("Errore-4-1: " + error.data.message);
					else if (error && error.data)
						alert("Errore-4-2: " + error.data);
					else 
						alert("Errore-4-3: " + angular.toJson(error));
				});
			}
			$rootScope.calcolaProiezione=function(squadralive){
				var casa=0;
				if (squadralive.casa){
					casa=2;
				}
				return $rootScope.getSquadraByCampionatoAndName(squadralive.campionato,squadralive.squadra).proiezione + casa;
			}
			$rootScope.ricaricaIndex=function(){
				var deferred = $q.defer();
				$rootScope.result={};
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				var conLive=false;
				$resource('./test',{conLive}).get().$promise.then(function(data) {
					$rootScope.result=data;
					conLive=true;
					$resource('./test',{conLive}).get().$promise.then(function(data) {
						$rootScope.result=data;
//						squadra.evidenza
						$rootScope.partitaLive={};
		                angular.forEach(data, function(value,camp) {
		                	$rootScope.partitaLive[camp]=[];
			                angular.forEach(value.squadre, function(sq,chiave2) {
			                	if (sq.evidenza){
			    					$rootScope.partitaLive[camp].push(sq.nome);
			                	}
			                });
		                });
						$rootScope.loading=false;
						$rootScope.fine=new Date();
						deferred.resolve("Hix");
					}).catch(function(error) {
						$rootScope.loading=false;
						$rootScope.fine=new Date();
						if (error && error.data && error.data.message)
							alert("Errore-5-1: " + error.data.message);
						else if (error && error.data)
							alert("Errore-5-2: " + error.data);
						else 
							alert("Errore-5-3: " + angular.toJson(error));
					});
				}).catch(function(error) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
					if (error && error.data && error.data.message)
						alert("Errore-6-1: " + error.data.message);
					else if (error && error.data)
						alert("Errore-6-2: " + error.data);
					else 
						alert("Errore-6-3: " + angular.toJson(error));
				});
				return deferred.promise;	
			}
			$rootScope.endConfigura = function(){
				$rootScope.configura=false;
				$rootScope.ricaricaIndex();
			}
			$rootScope.preparaSquadre = function(){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$rootScope.configura=false;
				$rootScope.result={};
				$resource('./preparaSquadre',{}).save({}).$promise.then(function(data) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
					$rootScope.ricaricaIndex();
				}).catch(function(error) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
					if (error && error.data && error.data.message)
						alert("Errore-7-1: " + error.data.message);
					else if (error && error.data)
						alert("Errore-7-2: " + error.data);
					else 
						alert("Errore-7-3: " + angular.toJson(error));
				});
			}
			$rootScope.setFantaSoccerAuth = function(){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./setFantaSoccerAuth',{}).save({'body':$rootScope.fantaSoccerAuth}).$promise.then(function(data) {
					$rootScope.fine=new Date();
					$rootScope.loading=false;
				}).catch(function(error) {
					$rootScope.fine=new Date();
					$rootScope.loading=false;
					if (error && error.data && error.data.message)
						alert("Errore-8-1: " + error.data.message);
					else if (error && error.data)
						alert("Errore-8-2: " + error.data);
					else 
						alert("Errore-8-3: " + angular.toJson(error));
				});
			}
			$rootScope.accendiKeepAlive = function(verso){
				$rootScope.setKeepAliveEnd(true, "./fantalive");
				window.location.href = './fantalive/index.html';
			}
			$rootScope.setKeepAliveEnd = function(verso, prefix){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource(prefix + '/setKeepAliveEnd',{}).save({'verso': verso}).$promise.then(function(x) {
					console.log(x);
					$rootScope.fine=new Date();
					$rootScope.loading=false;
				}).catch(function(error) {
					$rootScope.fine=new Date();
					$rootScope.loading=false;
					if (error && error.data && error.data.message)
						alert("Errore-9-1: " + error.data.message);
					else if (error && error.data)
						alert("Errore-9-2: " + error.data);
					else 
						alert("Errore-9-3: " + angular.toJson(error));
				});
			}
			$rootScope.setGiornata = function(){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./setGiornata',{}).save({'giornata':$rootScope.giornata}).$promise.then(function() {
					$rootScope.fine=new Date();
					$rootScope.loading=false;
				}).catch(function(error) {
					$rootScope.fine=new Date();
					$rootScope.loading=false;
					if (error && error.data && error.data.message)
						alert("Errore-9-1: " + error.data.message);
					else if (error && error.data)
						alert("Errore-9-2: " + error.data);
					else 
						alert("Errore-9-3: " + angular.toJson(error));
				});
			}
			$rootScope.vediEvidenza=function(s){
				if ($rootScope.listaSqEv.indexOf(s)<0) return s;
				return s + " (*)";
			}
			$rootScope.isInLista = function(s){
				var ret = false;
                angular.forEach($rootScope.listaSqEv, function(value,chiave) {
                	if (s===value){
                		ret = true;
                	}
                });
				return ret;
			}
			$rootScope.backLoading=function(){
				if ($rootScope.loading) return "lightyellow";
				return "white";
			}
			$rootScope.changeSqEv=function(squadra){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.result={};
				$rootScope.loading=true;
				if (squadra.evidenza){
					$rootScope.delSqEv=squadra.nome;
					$rootScope.delSqEvid().then(function(){
						$rootScope.fine=new Date();
						$rootScope.loading=false;
					});
				} else {
					$rootScope.sqEv=squadra.nome;
					$rootScope.addSqEv().then(function(){
						$rootScope.fine=new Date();
						$rootScope.loading=false;
					});
				}
			}
			$rootScope.addSqEv = function(){
				var deferred = $q.defer();
				$resource('./addSqEv',{}).save({'sqEv':$rootScope.sqEv}).$promise.then(function(data) {
						$rootScope.result=data;
						deferred.resolve("Hi4");
				}).catch(function(error) {
					if (error && error.data && error.data.message)
						alert("Errore-10-1: " + error.data.message);
					else if (error && error.data)
						alert("Errore-10-2: " + error.data);
					else 
						alert("Errore-10-3: " + angular.toJson(error));
				});
				return deferred.promise;	
			}
			$rootScope.delSqEvid = function(){
				var deferred = $q.defer();
				$resource('./delSqEv',{}).save({'sqEv':$rootScope.delSqEv}).$promise.then(function(data) {
						$rootScope.result=data;
						deferred.resolve("Hi3");
				}).catch(function(error) {
					if (error && error.data && error.data.message)
						alert("Errore-11-1: " + error.data.message);
					else if (error && error.data)
						alert("Errore-11-2: " + error.data);
					else 
						alert("Errore-11-3: " + angular.toJson(error));
				});
				return deferred.promise;	
			}
			$rootScope.simPresente=function(squadra){
				if (squadra.partiteSimulate.length>0) return "red";
			}
			$rootScope.campLive=function(partitaSimulata){
//				if (partitaSimulata==$rootScope.visPartitaLive) return "blue";
				if (partitaSimulata.substring(0,1)=="B")  return "green";
				if (partitaSimulata.substring(0,1)=="F")  return "blue";
				return "orange";
			}
			$rootScope.campLiveSfondo=function(partitaSimulata){
				if (partitaSimulata==$rootScope.visPartitaLive) return "gray";
			}
			$rootScope.inEv=function(squadra){
				if (squadra.evidenza) return "red";
				return "orange";
			}
			$rootScope.hiddenVis=[];
			$rootScope.hiddenVisSq=[];
			$rootScope.changeVis=function(r){
				var ind=$rootScope.hiddenVis.indexOf(r);
				if (ind>-1){
					$rootScope.hiddenVis.splice(ind,1);
				}else{
					$rootScope.hiddenVis.push(r);
				} 
			}
			$rootScope.changeVisSq=function(r){
				var ind=$rootScope.hiddenVisSq.indexOf(r);
				if (ind>-1){
					$rootScope.hiddenVisSq.splice(ind,1);
				}else{
					$rootScope.hiddenVisSq.push(r);
				} 
			}
			$rootScope.doFilterGioc=function(item){
	        	   var gF=true;
	        	   if ($rootScope.giocFilter!=""){
		        	   gF=false;
		        	   if ($rootScope.giocFilter=="") gF = true;
		        	   if (!$rootScope.giocFilter)  gF=true;
		        	   else if (item.nome && item.nome.toUpperCase().indexOf($rootScope.giocFilter.toUpperCase())>-1) gF = true;
	        	   }
	        	   
	        	   var gS=true;
	        	   if ($rootScope.sqFilter!=""){
		        	   gS=false;
		        	   if ($rootScope.sqFilter=="") gS = true;
		        	   if (!$rootScope.sqFilter) gS=true;
		        	   else if (item.squadra && item.squadra.toUpperCase().indexOf($rootScope.sqFilter.toUpperCase())>-1) gS = true;
	        	   }
	        	   
	        	   var lS=true;
	        	   if($rootScope.liveFilter && (item.orario.tag=='Postponed' || item.orario.tag=='FullTime' || item.orario.tag=='PreMatch')) lS=false;
	        	   if($rootScope.liveFilter && (item.codEventi.indexOf(14)>-1)) lS=false;
	        	   
	        	   return gF && gS && lS;
			}
		}

)


app.filter('filtraGiocatore', function($rootScope){
	  return function(dataArray) {
	      if (!dataArray) {
	          return;
	      }
	      else {
	           return dataArray.filter(function(item){
	        	   return $rootScope.doFilterGioc(item);
	           });
	      } 
	  }    
	});

app.filter('filtraSquadra', function($rootScope){
	  return function(dataArray) {
	      if (!dataArray) {
	          return;
	      }
	      else {
	           return dataArray.filter(function(item){
	        	   var ret = false;
                   angular.forEach(item.titolari, function(value,chiave) {
                	   if ($rootScope.doFilterGioc(value)) ret = true;
	        	   })
                   angular.forEach(item.riserve, function(value,chiave) {
                	   if ($rootScope.doFilterGioc(value)) ret = true;
	        	   })
	        	   var fF=true;
	        	   if($rootScope.favFilter && !item.evidenza) fF=false;
	        	   return ret && fF;
	           });
	      } 
	  }    
	});
app.factory('httpRequestInterceptor', function () {
	  return {
	    request: function (config) {
//		      config.headers = {'X-CSRF-Token':'prova'}
	    	config.headers = {'Content-Type': 'application/json'}
	      return config;
	    }
	  };
	});
app.config(function ($httpProvider) {
  $httpProvider.interceptors.push('httpRequestInterceptor');
});
app.directive("visualizzasquadra", function() {
	return {
		restrict: "E",
		templateUrl: "/fantalive/squadraTemplate.html",
		scope: {
			rcampionato: "=",
			rlive: "=",
			eventi: "=",
			squadra: "="
		},
		link: function($scope, element, attrs) {
			$scope.getFantaVoto=function(g){
				if (!g.squadraGioca) return " ";
				if (g.voto==0) return "NV";
				return g.modificatore+g.voto;
			}
			$scope.getOrario=function(orario){
				if (orario.tag=='FullTime' || orario.tag=='Postponed' || orario.tag=='Cancelled' || orario.tag=='Walkover') return orario.tag;
				if (orario.tag=='PreMatch'){
					var ret="";
					ret = ret + orario.val.substring(8,10);
					ret = ret + "/" + orario.val.substring(5,7);
					ret = ret + " " + (1+Number(orario.val.substring(11,13)));
					ret = ret + ":" + orario.val.substring(14,16);
					return ret;
				}
				return orario.val + "Min";
			}
			$scope.desEvento=function(ev,r){
				if (!$scope.eventi) return "";
				var evento = $scope.eventi[ev];
				var ret = evento[0];
				ret = ret + " <-> " + $scope.valEvento(evento,r); 
				return ret;
			}
			$scope.getVoto=function(g){
				if (!g.squadraGioca) return " ";
				if (g.voto==0) return "NV";
				return g.voto;
			}
			$scope.valEvento=function(evento,r){
				var pos=0;
				if (r=="FANTAVIVA") pos=1;
				if (r=="LUCCICAR") pos=2;
				if (r=="BE") pos=3;
				if (r=="JB") pos=4;
				return evento[pos];
			}
			$scope.weight=function(giocatore){
				if (giocatore.orario.tag=='FullTime') return "bold";
				if (giocatore.codEventi.indexOf(14)>-1) return "bold";
			}
			$scope.txtColor=function(giocatore){
				if (giocatore.voto==0) return "red";
			}
			$scope.backColor=function(giocatore){
				if (giocatore.squadraGioca) return "lightgray";
			}
		}
	};
})

/*
$rootScope.getFantaVoto=function(g){
	if (!g.squadraGioca) return " ";
	if (g.voto==0) return "NV";
	return g.modificatore+g.voto;
}
*/
