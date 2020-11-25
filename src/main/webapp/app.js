var app = angular.module('app', [ 'ngResource','ngAnimate', 'ngSanitize', 'ui.bootstrap' ]);
app.run(
		function($rootScope, $resource, $interval,$q,$timeout){
			$rootScope.configura=false;
			$rootScope.init=function(){
				$rootScope.nomiSquadre();
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$rootScope.loading1=true;
				$rootScope.loading2=true;
				$resource('./getFantaSoccerAuth',{}).get().$promise.then(function(data) {
					$rootScope.getFantaSoccerAuth=data.body;
					$rootScope.loading1=false;
					$rootScope.loading=$rootScope.loading1&&$rootScope.loading2;
					if (!$rootScope.loading) $rootScope.fine=new Date();
				}).catch(function(error) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
					alert("Errore: " + error.data.message);
				});
				$resource('./getGiornata',{}).get().$promise.then(function(data) {
					$rootScope.giornata=data.giornata;
					$rootScope.loading2=false;
					$rootScope.loading=$rootScope.loading1&&$rootScope.loading2;
					if (!$rootScope.loading) $rootScope.fine=new Date();
				}).catch(function(error) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
					alert("Errore: " + error.data.message);
				});
			}
			$rootScope.nomiSquadre=function(){
				var deferred = $q.defer();
				$resource('./nomiSquadre',{}).query().$promise.then(function(data) {
					$rootScope.squadre=data;
					$rootScope.ricaricaSqBeDaEscluedere();
					$rootScope.ricaricaSqEv();
					deferred.resolve("Hi");
				});
				return deferred.promise;	
			}
			$rootScope.ricaricaIndex=function(){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
//				$rootScope.init();
				$rootScope.loading=true;
				//$rootScope.result=[];
				var conLive=false;
				$resource('./test',{conLive}).get().$promise.then(function(data) {
//					$rootScope.loading=false;
					$rootScope.result=data;
//					$rootScope.fine=new Date();
					conLive=true;
					$resource('./test',{conLive}).get().$promise.then(function(data) {
						$rootScope.loading=false;
						$rootScope.result=data;
						$rootScope.fine=new Date();
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
			}
			/*
			$rootScope.cancellaSquadre = function(){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./cancellaSquadre',{}).save({}).$promise.then(function(data) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
				}).catch(function(error) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
					alert("Errore: " + error.data.message);
				});
			}
			*/
			$rootScope.preparaSquadre = function(){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./preparaSquadre',{}).save({}).$promise.then(function(data) {
					$rootScope.loading=false;
					$rootScope.fine=new Date();
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
				$resource('./setFantaSoccerAuth',{}).save({'body':$rootScope.getFantaSoccerAuth}).$promise.then(function(data) {
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
			$rootScope.addSqBeDaEscluedere = function(){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./addSqBeDaEscluedere',{}).save({'sqBeDaEscluedere':$rootScope.sqBeDaEscluedere}).$promise.then(function(data) {

					
					$rootScope.nomiSquadre().then(function(){
						$rootScope.fine=new Date();
						$rootScope.loading=false;
			        });
					
					
					
//					$rootScope.nomiSquadre();
//					$rootScope.fine=new Date();
//					$rootScope.loading=false;
				}).catch(function(error) {
					$rootScope.fine=new Date();
					$rootScope.loading=false;
					alert("Errore: " + error.data.message);
				});
			}
			$rootScope.ricaricaSqBeDaEscluedere = function(){
//				$rootScope.inizio=new Date();
//				$rootScope.fine="";
//				$rootScope.loading=true;
				$resource('./ricaricaSqBeDaEscluedere',{}).get().$promise.then(function(data) {
					$rootScope.listaSqBeDaEscluedere=data.sq;
//					$rootScope.fine=new Date();
//					$rootScope.loading=false;
				}).catch(function(error) {
//					$rootScope.fine=new Date();
//					$rootScope.loading=false;
					alert("Errore: " + error);
				});
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
			$rootScope.ricaricaSqEv = function(){
//				$rootScope.inizio=new Date();
//				$rootScope.fine="";
//				$rootScope.loading=true;
				$resource('./ricaricaSqEv',{}).get().$promise.then(function(data) {
					$rootScope.squadre2=[];
					$rootScope.listaSqEv=data.sq;
//					console.log($rootScope.squadre);
	                   angular.forEach($rootScope.squadre, function(value,chiave) {
	                	   if (!$rootScope.isInLista(value))
	                	   {
	                		   $rootScope.squadre2.push(value);
	                	   }
	                   });
//					$rootScope.fine=new Date();
//					$rootScope.loading=false;
				}).catch(function(error) {
//					$rootScope.fine=new Date();
//					$rootScope.loading=false;
					alert("Errore: " + error);
				});
			}
			$rootScope.delSqBeDaEscluedere = function(){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./delSqBeDaEscluedere',{}).save({'sqBeDaEscluedere':$rootScope.delSqBeDaEscludere}).$promise.then(function(data) {
					$rootScope.nomiSquadre().then(function(){
						$rootScope.fine=new Date();
						$rootScope.loading=false;
			        });

					
					//					$rootScope.nomiSquadre();
//					$rootScope.fine=new Date();
//					$rootScope.loading=false;
				}).catch(function(error) {
					$rootScope.fine=new Date();
					$rootScope.loading=false;
					alert("Errore: " + error.data.message);
				});
			}
			$rootScope.addSqEv = function(){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./addSqEv',{}).save({'sqEv':$rootScope.sqEv}).$promise.then(function(data) {
					$rootScope.nomiSquadre().then(function(){
						$rootScope.fine=new Date();
						$rootScope.loading=false;
			        });

					
					//					$rootScope.nomiSquadre();
//					$rootScope.fine=new Date();
//					$rootScope.loading=false;
				}).catch(function(error) {
					$rootScope.fine=new Date();
					$rootScope.loading=false;
					alert("Errore: " + error.data.message);
				});
			}
			$rootScope.delSqEvid = function(){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				$resource('./delSqEv',{}).save({'sqEv':$rootScope.delSqEv}).$promise.then(function(data) {
					$rootScope.nomiSquadre().then(function(){
						$rootScope.fine=new Date();
						$rootScope.loading=false;
			        });

					
					
					//					$rootScope.nomiSquadre();
//					$rootScope.fine=new Date();
//					$rootScope.loading=false;
				}).catch(function(error) {
					$rootScope.fine=new Date();
					$rootScope.loading=false;
					alert("Errore: " + error.data.message);
				});
			}
			/*
			$rootScope.caricaFile = function(){
				$rootScope.inizio=new Date();
				$rootScope.fine="";
				$rootScope.loading=true;
				var f = document.getElementById('file').files[0], r = new FileReader();
				r.onloadend = function(e) {
					var data = e.target.result;
					$resource('./caricaFile',{}).save({'file':data}).$promise.then(function(data) {
						$rootScope.fine=new Date();
						$rootScope.loading=false;
					}).catch(function(error) {
						$rootScope.fine=new Date();
						$rootScope.loading=false;
						alert("Errore: " + error.data.message);
					});
				}
				r.readAsBinaryString(f);
			}
			*/
			$rootScope.ico={
					"22": "assist hight +1.5",
					"11": "gol vittoria",
					"12": "gol pareggio",
					"24": "assist medium1 +1",
					"14": "uscito",
					"15": "entrato",
					"16": "gol annullato",
					"17": "infortunio",
					"1": "ammonito -0.5",
					"2": "espulso -1",
					"3": "gol +3",
					"4": "gol subito -1",
					"7": "rigore parato +3",
					"8": "rigore sbagliato -3",
					"9": "rigore segnato +3",
					"20": "assist low +0.5",
					"21": "assist medium2 +1",
			};
			$rootScope.weight=function(giocatore){
				if (giocatore.orario.tag=='FullTime') return "bold";
				if (giocatore.codEventi.indexOf(14)>-1) return "bold";
			}
			$rootScope.backColor=function(giocatore){
				if (giocatore.squadraGioca) return "lightgray";
			}
			$rootScope.inEv=function(squadra){
				if (squadra.evidenza) return "red";
				return "orange";
			}
			$rootScope.txtColor=function(giocatore){
				if (giocatore.voto==0) return "red";
			}
			$rootScope.getOrario=function(orario){
				if (orario.tag=='FullTime') return "";
				if (orario.tag=='PreMatch'){
					var ret="";
					ret = ret + orario.val.substring(8,10);
					ret = ret + "/" + orario.val.substring(5,7);
					ret = ret + " " + (1+Number(orario.val.substring(11,13)));
					ret = ret + ":" + orario.val.substring(14,16);
					return ret;
				}
				return orario.val;
			}
			$rootScope.getVoto=function(g){
				if (!g.squadraGioca) return " ";
				if (g.voto==0) return "--";
				return g.voto;
			}
			$rootScope.getFantaVoto=function(g){
				if (!g.squadraGioca) return " ";
				if (g.voto==0) return "--";
				return g.modificatore+g.voto;
			}
			$rootScope.desIco=function(r){
				return $rootScope.ico[r];
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
		}

)


app.filter('giocFilter', function($rootScope){
	  return function(dataArray) {
	      if (!dataArray) {
	          return;
	      }
	      else {
	           return dataArray.filter(function(item){
	        	   if (!$rootScope.giocFilter) return true;
	        	   if ($rootScope.giocFilter=="") return true;
	        	   if (item.nome.toUpperCase().indexOf($rootScope.giocFilter.toUpperCase())>-1) return true;
	        	   return false;
	           });
	      } 
	  }    
	});

app.filter('sqFilter', function($rootScope){
	  return function(dataArray) {
	      if (!dataArray) {
	          return;
	      }
	      else {
	           return dataArray.filter(function(item){
	        	   if (!$rootScope.giocFilter) return true;
	        	   if ($rootScope.giocFilter=="") return true;
	        	   var ret = false;
                   angular.forEach(item.titolari, function(value,chiave) {
                	   if (value.nome.toUpperCase().indexOf($rootScope.giocFilter.toUpperCase())>-1) ret= true;
	        	   })
                   angular.forEach(item.riserve, function(value,chiave) {
                	   if (value.nome.toUpperCase().indexOf($rootScope.giocFilter.toUpperCase())>-1) ret= true;
	        	   })
	        	   return ret;

	           });
	      } 
	  }    
	});
