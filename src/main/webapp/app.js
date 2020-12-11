var app = angular.module('app', [ 'ngResource','ngAnimate', 'ngSanitize', 'ui.bootstrap' ]);
app.run(
		function($rootScope, $resource, $interval,$q,$timeout){
			$rootScope.configura=false;
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
							alert("Errore: " + error.data.message);
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
			$rootScope.getMessaggio = function(message){
				if (message){
					var msg = JSON.parse(message);
//					console.log(msg);
					if (msg.res){
						$rootScope.result=msg.res;
					}
					if (msg.timeRefresh){
						$rootScope.timeRefresh=msg.timeRefresh;
					}
					if (msg.runningBot){
						$rootScope.runningBot=msg.runningBot;
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
					alert("Errore: " + error);
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
				new_uri += loc.href.substring(loc.protocol.length) + "messaggi-websocket";
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
			$rootScope.visSimulaCambi=function(sq,r){
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
					alert("Errore: " + error);
				});
			}
			$rootScope.simulaCambi=function(r,sq, ind){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./simulaCambi',{}).save({'sq':sq}).$promise.then(function(data) {
					r.squadre.splice(ind, 1,data);
					$rootScope.loading=false;
					$rootScope.fine=new Date();
				}).catch(function(error) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
					alert("Errore: " + error);
				});
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
						$rootScope.loading=false;
						$rootScope.result=data;
						$rootScope.fine=new Date();
						deferred.resolve("Hix");
					}).catch(function(error) {
						$rootScope.loading=false;
						$rootScope.fine=new Date();
						alert("Errore: " + error.data.message);
					});
				}).catch(function(error) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
					alert("Errore: " + error.data.message);
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
				$resource('./preparaSquadre',{}).save({'_crfs':'prova'}).$promise.then(function(data) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
					$rootScope.ricaricaIndex();
				}).catch(function(error) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
					alert("Errore: " + error.data.message);
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
					alert("Errore: " + error.data.message);
				});
			}
			$rootScope.setGiornata = function(){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./setGiornata',{}).save({'giornata':$rootScope.giornata}).$promise.then(function(data) {
					$rootScope.fine=new Date();
					$rootScope.loading=false;
				}).catch(function(error) {
					$rootScope.fine=new Date();
					$rootScope.loading=false;
					alert("Errore: " + error.data.message);
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
			$rootScope.weight=function(giocatore){
				if (giocatore.orario.tag=='FullTime') return "bold";
				if (giocatore.codEventi.indexOf(14)>-1) return "bold";
			}
			$rootScope.txtColor=function(giocatore){
				if (giocatore.voto==0) return "red";
			}
			$rootScope.backColor=function(giocatore){
				if (giocatore.squadraGioca) return "lightgray";
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
					alert("Errore: " + error.data.message);
				});
				return deferred.promise;	
			}
			$rootScope.delSqEvid = function(){
				var deferred = $q.defer();
				$resource('./delSqEv',{}).save({'sqEv':$rootScope.delSqEv}).$promise.then(function(data) {
						$rootScope.result=data;
						deferred.resolve("Hi3");
				}).catch(function(error) {
					alert("Errore: " + error.data.message);
				});
				return deferred.promise;	
			}
			$rootScope.inEv=function(squadra){
				if (squadra.evidenza) return "red";
				return "orange";
			}
			$rootScope.getOrario=function(orario){
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
			$rootScope.getVoto=function(g){
				if (!g.squadraGioca) return " ";
				if (g.voto==0) return "NV";
				return g.voto;
			}
			$rootScope.getFantaVoto=function(g){
				if (!g.squadraGioca) return " ";
				if (g.voto==0) return "NV";
				return g.modificatore+g.voto;
			}
			$rootScope.desEvento=function(ev,r){
				if (!$rootScope.eventi) return "";
				var evento = $rootScope.eventi[ev];
				var ret = evento[0];
				ret = ret + " <-> " + $rootScope.valEvento(evento,r); 
				return ret;
			}
			$rootScope.valEvento=function(evento,r){
				var pos=0;
				if (r=="FANTAVIVA") pos=1;
				if (r=="LUCCICAR") pos=2;
				if (r=="BE") pos=3;
				return evento[pos];
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
