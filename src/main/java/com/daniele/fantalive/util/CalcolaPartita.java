package com.daniele.fantalive.util;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.daniele.MainClass;
import com.daniele.fantalive.bl.Main;
import com.daniele.fantalive.model.Giocatore;
import com.daniele.fantalive.model.Squadra;
import com.daniele.fantalive.repository.SalvaRepository;


public class CalcolaPartita {
	private static final Integer MAX_GIORNATA_DA_CALCOLARE = 50;
	private static final boolean RECUPERA_FROM_DB=false;
	private static final boolean USA_SPRING=false;
	private static final boolean SOLO_COPPA_CAMPIONI=false;
	private static final boolean SOLO_COPPA_ITALIA=false;
	private static final boolean SOLO_CAMPIONATO=false;
	private static final String PARTITE = "Partite";
	private static final String PUNTEGGI = "Punteggi";
	private static final String PUNTI = "Punti";
	private static final String GIOCATE = "Giocate";
	private static ConfigurableApplicationContext ctx;
	private int iMaxGG=0;
	Set<String> nomiFS=new HashSet<>();

	public static void main(String[] args) throws Exception {
		CalcolaPartita cp = new CalcolaPartita();
//		cp.bu();
		cp.go(args);
//		cp.printNomi();
		if (USA_SPRING) {
			if (false) {
				ctx.stop();
				ctx.close();
			}
		}
		System.exit(0);
	}
	private void bu() {
		Map<String, Object> totaliF1=new HashMap<>(); 
		int i = 128;
		totaliF1.put("a",i);
		totaliF1.put("b",i);
		
		
		Map<String, Object> map = new TreeMap(new ReverseOrderTreemap(totaliF1));
		Set<String> keySet = totaliF1.keySet();
		for (String k : keySet) {
			map.put(k, totaliF1.get(k));
		}
//		map.putAll(totaliF1);
		
		

		
		
		System.out.println(map.keySet().size());
		
	}
	private void printNomi() throws Exception {
		System.out.println("-------------------------");
		Set<String> nomiFG=new HashSet<>();
		for (int i=1;i<iMaxGG;i++) {
			Iterator<Integer> iterator = Main.sq.keySet().iterator();
			while (iterator.hasNext()) {
				Integer integer = (Integer) iterator.next();
				String sqFromLive = (String) Main.callHTTP("GET", "application/json; charset=UTF-8", String.format(Constant.URL_LIVE_FG,integer, i, Constant.I_LIVE_FANTACALCIO), null).get("response");
				List<Map<String, Object>> jsonToList = Main.jsonToList(sqFromLive);
				for (Map<String, Object> map : jsonToList) {
					nomiFG.add(map.get("nome") + "@" + Main.sq.get(integer));
				}
			}
		}
		

		
		
		for (String nome : nomiFG) {
			System.out.println(nome);
		}
		System.out.println("-------------------------");
		for (String nome : nomiFS) {
			String[] split = nome.split("@");
			String nomeFromFG = Main.getNomeFromFG(split[0], nomiFG);
			System.out.println(nome + "@" + nomeFromFG);
		}
		System.out.println("-------------------------");
	}
	
	
	private static boolean ePari(int numero) {
		if ((numero % 2) == 0) {
			return true;
		} else {
			return false;
		}
	}

	class ReverseOrderTreemap implements Comparator<Object> {
		Map<String, Object> map;
		public ReverseOrderTreemap(Map<String, Object> map) {
			this.map = map;
		}
		public int compare(Object o1, Object o2) {
			if (map.get(o2) == map.get(o1))
				return 1;
			else
				if (map.get(o2) instanceof Integer) {
					int compareTo = ((Integer) map.get(o2)).compareTo((Integer) map.get(o1));
					if (compareTo==0) {
						compareTo=1;
					}
					return compareTo;
				}
				else if (map.get(o2) instanceof Double) {
					int compareTo = ((Double) map.get(o2)).compareTo((Double) map.get(o1));
					if (compareTo==0) {
						compareTo=1;
					}
					return compareTo;
				} else {
					throw new RuntimeException("Tipo non gestito");
				}

		}
	}

	private void go(String[] args) throws Exception {
		int contaCoppe=0;
		if (SOLO_COPPA_CAMPIONI) contaCoppe++;
		if (SOLO_COPPA_ITALIA) contaCoppe++;
		if (SOLO_CAMPIONATO) contaCoppe++;
		if (contaCoppe>1) {
			throw new RuntimeException("SOLO_COPPA_ITALIA, SOLO_CAMPIONATO e SOLO_COPPA_CAMPIONI sono alternativi tra di loro");
		}
		if (USA_SPRING) {
			ctx = new SpringApplicationBuilder(MainClass.class)
					.profiles("DEV")
					.web(true).run(args);
			Main.init(ctx.getBean(SalvaRepository.class),null,ctx.getBean(Constant.class), false);
			if (false) {
				if (Main.fantaLiveBot.isRunning()) {
					Main.fantaLiveBot.stopBot();
					Main.fantaCronacaLiveBot.stopBot();
				} 
			}
		} else {
			Constant c=null;
			Class<?> cl = Class.forName("com.daniele.fantalive.util.ConstantDevelop");
			Method method = cl.getDeclaredMethod("constant");
			c = (Constant) method.invoke(c);		
			Main.init(null,null,c, false);
		}

		HashSet<String> hsPartiteCoppaItalia=new HashSet<>();
		hsPartiteCoppaItalia.add("25"+ "\t" + "C. H. MOLLE"+ "\t" + "tavolino");
		hsPartiteCoppaItalia.add("25"+ "\t" + "Canosa di Puglia..."+ "\t" + "Atletico Mikatanto");
		hsPartiteCoppaItalia.add("25"+ "\t" + "Universal"+ "\t" + "Atletico Conc");
		hsPartiteCoppaItalia.add("25"+ "\t" + "VincereAManiBasse"+ "\t" + "Jonny Fighters");
		
		hsPartiteCoppaItalia.add("26"+ "\t" + "tavolino"+ "\t" + "C. H. MOLLE");
		hsPartiteCoppaItalia.add("26"+ "\t" + "Atletico Mikatanto"+ "\t" + "Canosa di Puglia...");
		hsPartiteCoppaItalia.add("26"+ "\t" + "Atletico Conc"+ "\t" + "Universal");
		hsPartiteCoppaItalia.add("26"+ "\t" + "Jonny Fighters"+ "\t" + "VincereAManiBasse");

		hsPartiteCoppaItalia.add("27"+ "\t" + "C. H. MOLLE"+ "\t" + "Atletico Mikatanto");
		hsPartiteCoppaItalia.add("27"+ "\t" + "Jonny Fighters"+ "\t" + "Atletico Conc");

		hsPartiteCoppaItalia.add("28"+ "\t" + "Atletico Mikatanto"+ "\t" + "C. H. MOLLE");
		hsPartiteCoppaItalia.add("28"+ "\t" + "Atletico Conc"+ "\t" + "Jonny Fighters");

		hsPartiteCoppaItalia.add("29"+ "\t" + "TBD"+ "\t" + "TBD");

		hsPartiteCoppaItalia.add("30"+ "\t" + "TBD"+ "\t" + "TBD");

		HashSet<String> hsPartiteCoppaCampioni=new HashSet<>();
		hsPartiteCoppaCampioni.add("2"+ "\t" + "Jonny Fighters"+ "\t" + "C. H. MOLLE");
		hsPartiteCoppaCampioni.add("2"+ "\t" + "Atletico Mikatanto"+ "\t" + "tavolino");
		hsPartiteCoppaCampioni.add("4"+ "\t" + "C. H. MOLLE"+ "\t" + "Atletico Mikatanto");
		hsPartiteCoppaCampioni.add("4"+ "\t" + "tavolino"+ "\t" + "Jonny Fighters");
		hsPartiteCoppaCampioni.add("6"+ "\t" + "Jonny Fighters"+ "\t" + "Atletico Mikatanto");
		hsPartiteCoppaCampioni.add("6"+ "\t" + "C. H. MOLLE"+ "\t" + "tavolino");
		hsPartiteCoppaCampioni.add("8"+ "\t" + "tavolino"+ "\t" + "C. H. MOLLE");
		hsPartiteCoppaCampioni.add("8"+ "\t" + "Atletico Mikatanto"+ "\t" + "Jonny Fighters");
		hsPartiteCoppaCampioni.add("10"+ "\t" + "Atletico Mikatanto"+ "\t" + "C. H. MOLLE");
		hsPartiteCoppaCampioni.add("10"+ "\t" + "Jonny Fighters"+ "\t" + "tavolino");
		hsPartiteCoppaCampioni.add("12"+ "\t" + "tavolino"+ "\t" + "Atletico Mikatanto");
		hsPartiteCoppaCampioni.add("12"+ "\t" + "C. H. MOLLE"+ "\t" + "Jonny Fighters");
		hsPartiteCoppaCampioni.add("14"+ "\t" + "Jonny Fighters"+ "\t" + "C. H. MOLLE");
		hsPartiteCoppaCampioni.add("14"+ "\t" + "Atletico Mikatanto"+ "\t" + "tavolino");
		hsPartiteCoppaCampioni.add("16"+ "\t" + "C. H. MOLLE"+ "\t" + "Atletico Mikatanto");
		hsPartiteCoppaCampioni.add("16"+ "\t" + "tavolino"+ "\t" + "Jonny Fighters");
		hsPartiteCoppaCampioni.add("18"+ "\t" + "Jonny Fighters"+ "\t" + "Atletico Mikatanto");
		hsPartiteCoppaCampioni.add("18"+ "\t" + "C. H. MOLLE"+ "\t" + "tavolino");
		hsPartiteCoppaCampioni.add("20"+ "\t" + "tavolino"+ "\t" + "C. H. MOLLE");
		hsPartiteCoppaCampioni.add("20"+ "\t" + "Atletico Mikatanto"+ "\t" + "Jonny Fighters");
		hsPartiteCoppaCampioni.add("22"+ "\t" + "Atletico Mikatanto"+ "\t" + "C. H. MOLLE");
		hsPartiteCoppaCampioni.add("22"+ "\t" + "Jonny Fighters"+ "\t" + "tavolino");
		hsPartiteCoppaCampioni.add("24"+ "\t" + "tavolino"+ "\t" + "Atletico Mikatanto");
		hsPartiteCoppaCampioni.add("24"+ "\t" + "C. H. MOLLE"+ "\t" + "Jonny Fighters");
		
		hsPartiteCoppaCampioni.add("2"+ "\t" + "Universal"+ "\t" + "VincereAManiBasse");
		hsPartiteCoppaCampioni.add("2"+ "\t" + "Canosa di Puglia..."+ "\t" + "Atletico Conc");
		hsPartiteCoppaCampioni.add("4"+ "\t" + "VincereAManiBasse"+ "\t" + "Canosa di Puglia...");
		hsPartiteCoppaCampioni.add("4"+ "\t" + "Atletico Conc"+ "\t" + "Universal");
		hsPartiteCoppaCampioni.add("6"+ "\t" + "Universal"+ "\t" + "Canosa di Puglia...");
		hsPartiteCoppaCampioni.add("6"+ "\t" + "VincereAManiBasse"+ "\t" + "Atletico Conc");
		hsPartiteCoppaCampioni.add("8"+ "\t" + "Atletico Conc"+ "\t" + "VincereAManiBasse");
		hsPartiteCoppaCampioni.add("8"+ "\t" + "Canosa di Puglia..."+ "\t" + "Universal");
		hsPartiteCoppaCampioni.add("10"+ "\t" + "Canosa di Puglia..."+ "\t" + "VincereAManiBasse");
		hsPartiteCoppaCampioni.add("10"+ "\t" + "Universal"+ "\t" + "Atletico Conc");
		hsPartiteCoppaCampioni.add("12"+ "\t" + "Atletico Conc"+ "\t" + "Canosa di Puglia...");
		hsPartiteCoppaCampioni.add("12"+ "\t" + "VincereAManiBasse"+ "\t" + "Universal");
		hsPartiteCoppaCampioni.add("14"+ "\t" + "Universal"+ "\t" + "VincereAManiBasse");
		hsPartiteCoppaCampioni.add("14"+ "\t" + "Canosa di Puglia..."+ "\t" + "Atletico Conc");
		hsPartiteCoppaCampioni.add("16"+ "\t" + "VincereAManiBasse"+ "\t" + "Canosa di Puglia...");
		hsPartiteCoppaCampioni.add("16"+ "\t" + "Atletico Conc"+ "\t" + "Universal");
		hsPartiteCoppaCampioni.add("18"+ "\t" + "Universal"+ "\t" + "Canosa di Puglia...");
		hsPartiteCoppaCampioni.add("18"+ "\t" + "VincereAManiBasse"+ "\t" + "Atletico Conc");
		hsPartiteCoppaCampioni.add("20"+ "\t" + "Atletico Conc"+ "\t" + "VincereAManiBasse");
		hsPartiteCoppaCampioni.add("20"+ "\t" + "Canosa di Puglia..."+ "\t" + "Universal");
		hsPartiteCoppaCampioni.add("22"+ "\t" + "Canosa di Puglia..."+ "\t" + "VincereAManiBasse");
		hsPartiteCoppaCampioni.add("22"+ "\t" + "Universal"+ "\t" + "Atletico Conc");
		hsPartiteCoppaCampioni.add("24"+ "\t" + "Atletico Conc"+ "\t" + "Canosa di Puglia...");
		hsPartiteCoppaCampioni.add("24"+ "\t" + "VincereAManiBasse"+ "\t" + "Universal");

		hsPartiteCoppaCampioni.add("26"+ "\t" + "Universal"+ "\t" + "tavolino");
		hsPartiteCoppaCampioni.add("26"+ "\t" + "C. H. MOLLE"+ "\t" + "VincereAManiBasse");
		
		hsPartiteCoppaCampioni.add("28"+ "\t" + "tavolino"+ "\t" + "Universal");
		hsPartiteCoppaCampioni.add("28"+ "\t" + "VincereAManiBasse"+ "\t" + "C. H. MOLLE");

		hsPartiteCoppaCampioni.add("30"+ "\t" + "TBD"+ "\t" + "TBD");
		
		HashMap<Integer, Integer> f1=new HashMap<>();
		f1.put(1, 25);
		f1.put(2, 18);
		f1.put(3, 15);
		f1.put(4, 12);
		f1.put(5, 10);
		f1.put(6, 8);
		f1.put(7, 6);
		f1.put(8, 4);


		Map<String, Object> totaliBR=new HashMap<>(); 
		Map<String, Object> totaliFormula1=new HashMap<>(); 
		//		Map<String, Object> totaliFormula1BIS=new HashMap<>(); 
		Map<String, Map<String, Map<String, Object>>> totaliScontri=new HashMap<>(); 

		for (int ggDaCalcolare=1;ggDaCalcolare<=MAX_GIORNATA_DA_CALCOLARE;ggDaCalcolare++) {
			ZonedDateTime zonedDateTime = Main.calendarioInizioGiornata.get(ggDaCalcolare + Constant.DELTA_FS);
			ZonedDateTime now = ZonedDateTime.now();
			if (zonedDateTime != null && now.isAfter(zonedDateTime)) {
				iMaxGG=ggDaCalcolare + Constant.DELTA_FS;
//				System.out.println(ggDaCalcolare + " - " + Main.calendarioInizioGiornata.get(ggDaCalcolare + Constant.DELTA_FS) + " - " + Main.calendario.get(ggDaCalcolare + Constant.DELTA_FS));

				//			System.err.println("***************:" + ggDaCalcolare);
				String tokenNomeFile = "STORICO_" + ggDaCalcolare  + "_";
				if (!RECUPERA_FROM_DB) {
					Main.scaricaBe(ggDaCalcolare + Constant.DELTA_FS,tokenNomeFile);
				}
				List<Squadra> squadre = Main.getSquadreFromFS(tokenNomeFile,false, true);
				for (Squadra squadra : squadre) {
					List<Giocatore> riserve = squadra.getRiserve();
					List<Giocatore> titolari = squadra.getTitolari();
					for (Giocatore titolare : titolari) {
						nomiFS.add(titolare.getNome() + "@" + titolare.getSquadra());
					}
					for (Giocatore riserva : riserve) {
						nomiFS.add(riserva.getNome() + "@" + riserva.getSquadra());
					}
					
				}
				Main.applicaCambi(squadre);
				//			System.err.println("*********************");
				//			System.err.println(squadre);
				//			System.err.println("*********************");
				Map<String, Object> totaliF1=new HashMap<>(); 
				System.err.println("+++ Scontri +++");
				for (int i1=0;i1<squadre.size();i1++) {
					Squadra squadra1=squadre.get(i1);
					String nome1 = squadra1.getNome();
					totaliF1.put(nome1, squadra1.getTotaleTitolari());					
					for (int i2=0;i2<squadre.size();i2++) {
						boolean calcolaGiornataCorrente=false;
						if (i2-i1!=1 || ePari(i2)) {
							if (SOLO_CAMPIONATO) continue;//campionato
						}
						else {
							calcolaGiornataCorrente=true;
						}
						Squadra squadra2=squadre.get(i2);
						String nome2 = squadra2.getNome();
						String k = Integer.toString(ggDaCalcolare) + "\t" + nome1 + "\t" + nome2;

						if (ggDaCalcolare==6 && nome1.equalsIgnoreCase("Jonny Fighters") && nome2.equalsIgnoreCase("Atletico Mikatanto")) {
							System.out.println();
						}
						
						if (SOLO_COPPA_CAMPIONI) {
							if (!hsPartiteCoppaCampioni.contains(k)) continue;
							calcolaGiornataCorrente=true;
						}

						if (SOLO_COPPA_ITALIA) {
							if (!hsPartiteCoppaItalia.contains(k)) continue;
							calcolaGiornataCorrente=true;
						}
						if (!nome1.equals(nome2)){
							Main.calcolaScontro(squadra1,squadra2, ggDaCalcolare);
							int iGolCasa=squadra1.getGolSimulazione();
							int iGolTrasferta=squadra2.getGolSimulazione();
							System.err.println(Integer.toString(ggDaCalcolare) + "\t" + nome1 + "\t" + nome2+"\t" + iGolCasa +"\t" + iGolTrasferta );
							if (iGolCasa>iGolTrasferta) {
								Integer tot = (Integer) totaliBR.get(nome1);
								if (tot==null) {
									tot = 0;
								}
								tot=tot + 3;
								totaliBR.put(nome1, tot);
								if (calcolaGiornataCorrente) {
									//add1
									Map<String, Map<String,Object>> mapScontri1 = totaliScontri.get(nome1);
									if (mapScontri1==null) {
										mapScontri1=new HashMap<>();
									}
									Map<String, Object> iScontri1 = mapScontri1.get(nome2);
									if (iScontri1==null) {
										iScontri1 = new HashMap<>();
										iScontri1.put(GIOCATE, 0);
										iScontri1.put(PUNTI, 0);
										iScontri1.put(PARTITE, "");
										iScontri1.put(PUNTEGGI, "");
									}
									else {
										iScontri1.put(PARTITE, ((String)iScontri1.get(PARTITE) + "#"));
										iScontri1.put(PUNTEGGI, ((String)iScontri1.get(PUNTEGGI) + "#"));
										if ("tavolino".equals(nome1)) {
											//										System.out.println();
										}
									}
									iScontri1.put(GIOCATE, ((Integer)iScontri1.get(GIOCATE))+1);
									iScontri1.put(PUNTI, ((Integer)iScontri1.get(PUNTI))+3);
									iScontri1.put(PARTITE, ((String)iScontri1.get(PARTITE) + iGolCasa + "-" + iGolTrasferta));
									iScontri1.put(PUNTEGGI, ((String)iScontri1.get(PUNTEGGI) + squadra1.getTotale()+Constant.ICASA + "-" + squadra2.getTotale()));
									mapScontri1.put(nome2, iScontri1);
									totaliScontri.put(nome1, mapScontri1);
									//add2
									Map<String, Map<String,Object>> mapScontri2 = totaliScontri.get(nome2);
									if (mapScontri2==null) {
										mapScontri2=new HashMap<>();
									}
									Map<String,Object> iScontri2 = mapScontri2.get(nome1);
									if (iScontri2==null) {
										iScontri2 = new HashMap<>();
										iScontri2.put(GIOCATE, 0);
										iScontri2.put(PUNTI, 0);
										iScontri2.put(PARTITE, "");
										iScontri2.put(PUNTEGGI, "");
									}
									else {
										iScontri2.put(PARTITE, ((String)iScontri2.get(PARTITE) + "#"));
										iScontri2.put(PUNTEGGI, ((String)iScontri2.get(PUNTEGGI) + "#"));
										if ("tavolino".equals(nome2)) {
											//										System.out.println();
										}
									}
									iScontri2.put(GIOCATE, ((Integer)iScontri2.get(GIOCATE))+1);
									iScontri2.put(PUNTI, ((Integer)iScontri2.get(PUNTI))+0);
									iScontri2.put(PARTITE, ((String)iScontri2.get(PARTITE) + iGolTrasferta + "-" + iGolCasa));
									iScontri2.put(PUNTEGGI, ((String)iScontri2.get(PUNTEGGI) + squadra2.getTotale() + "-" + squadra1.getTotale()+Constant.ICASA));
									mapScontri2.put(nome1, iScontri2);
									totaliScontri.put(nome2, mapScontri2);
								}
							}
							else if (iGolTrasferta>iGolCasa) {
								Integer tot = (Integer) totaliBR.get(nome2);
								if (tot==null) {
									tot = 0;
								}
								tot=tot + 3;
								totaliBR.put(nome2, tot);
								if (calcolaGiornataCorrente) {
									//add1
									Map<String, Map<String,Object>> mapScontri1 = totaliScontri.get(nome1);
									if (mapScontri1==null) {
										mapScontri1=new HashMap<>();
									}
									Map<String,Object> iScontri1 = mapScontri1.get(nome2);
									if (iScontri1==null) {
										iScontri1 = new HashMap<>();
										iScontri1.put(GIOCATE, 0);
										iScontri1.put(PUNTI, 0);
										iScontri1.put(PARTITE, "");
										iScontri1.put(PUNTEGGI, "");
									}
									else {
										iScontri1.put(PARTITE, ((String)iScontri1.get(PARTITE) + "#"));
										iScontri1.put(PUNTEGGI, ((String)iScontri1.get(PUNTEGGI) + "#"));
										if ("tavolino".equals(nome1)) {
											//										System.out.println();
										}
									}
									iScontri1.put(GIOCATE, ((Integer)iScontri1.get(GIOCATE))+1);
									iScontri1.put(PUNTI, ((Integer)iScontri1.get(PUNTI))+0);
									iScontri1.put(PARTITE, ((String)iScontri1.get(PARTITE) + iGolCasa + "-" + iGolTrasferta));
									iScontri1.put(PUNTEGGI, ((String)iScontri1.get(PUNTEGGI) + squadra1.getTotale()+Constant.ICASA + "-" + squadra2.getTotale()));
									mapScontri1.put(nome2, iScontri1);
									totaliScontri.put(nome1, mapScontri1);
									//add2
									Map<String, Map<String,Object>> mapScontri2 = totaliScontri.get(nome2);
									if (mapScontri2==null) {
										mapScontri2=new HashMap<>();
									}
									Map<String,Object> iScontri2 = mapScontri2.get(nome1);
									if (iScontri2==null) {
										iScontri2 = new HashMap<>();
										iScontri2.put(GIOCATE, 0);
										iScontri2.put(PUNTI, 0);
										iScontri2.put(PARTITE, "");
										iScontri2.put(PUNTEGGI, "");
									}
									else {
										iScontri2.put(PARTITE, ((String)iScontri2.get(PARTITE) + "#"));
										iScontri2.put(PUNTEGGI, ((String)iScontri2.get(PUNTEGGI) + "#"));
										if ("tavolino".equals(nome2)) {
											//										System.out.println();
										}
									}
									iScontri2.put(GIOCATE, ((Integer)iScontri2.get(GIOCATE))+1);
									iScontri2.put(PUNTI, ((Integer)iScontri2.get(PUNTI))+3);
									iScontri2.put(PARTITE, ((String)iScontri2.get(PARTITE) + iGolTrasferta + "-" + iGolCasa));
									iScontri2.put(PUNTEGGI, ((String)iScontri2.get(PUNTEGGI) + squadra2.getTotale() + "-" + squadra1.getTotale()+Constant.ICASA));
									mapScontri2.put(nome1, iScontri2);
									totaliScontri.put(nome2, mapScontri2);
								}
							} else {
								Integer tot1 = (Integer) totaliBR.get(nome1);
								if (tot1==null) {
									tot1 = 0;
								}
								tot1=tot1+1;
								totaliBR.put(nome1, tot1);
								Integer tot2 = (Integer) totaliBR.get(nome2);
								if (tot2==null) {
									tot2 = 0;
								}
								tot2=tot2+1;
								totaliBR.put(nome2, tot2);
								if (calcolaGiornataCorrente) {
									//add1
									Map<String, Map<String,Object>> mapScontri1 = totaliScontri.get(nome1);
									if (mapScontri1==null) {
										mapScontri1=new HashMap<>();
									}
									Map<String,Object> iScontri1 = mapScontri1.get(nome2);
									if (iScontri1==null) {
										iScontri1 = new HashMap<>();
										iScontri1.put(GIOCATE, 0);
										iScontri1.put(PUNTI, 0);
										iScontri1.put(PARTITE, "");
										iScontri1.put(PUNTEGGI, "");
									}
									else {
										iScontri1.put(PARTITE, ((String)iScontri1.get(PARTITE) + "#"));
										iScontri1.put(PUNTEGGI, ((String)iScontri1.get(PUNTEGGI) + "#"));
										if ("tavolino".equals(nome1)) {
											//										System.out.println();
										}
									}
									iScontri1.put(GIOCATE, ((Integer)iScontri1.get(GIOCATE))+1);
									iScontri1.put(PUNTI, ((Integer)iScontri1.get(PUNTI))+1);
									iScontri1.put(PARTITE, ((String)iScontri1.get(PARTITE) + iGolCasa + "-" + iGolTrasferta));
									iScontri1.put(PUNTEGGI, ((String)iScontri1.get(PUNTEGGI) + squadra1.getTotale()+Constant.ICASA + "-" + squadra2.getTotale()));
									mapScontri1.put(nome2, iScontri1);
									totaliScontri.put(nome1, mapScontri1);
									//add2
									Map<String, Map<String,Object>> mapScontri2 = totaliScontri.get(nome2);
									if (mapScontri2==null) {
										mapScontri2=new HashMap<>();
									}
									Map<String,Object> iScontri2 = mapScontri2.get(nome1);
									if (iScontri2==null) {
										iScontri2 = new HashMap<>();
										iScontri2.put(GIOCATE, 0);
										iScontri2.put(PUNTI, 0);
										iScontri2.put(PARTITE, "");
										iScontri2.put(PUNTEGGI, "");
									}
									else {
										iScontri2.put(PARTITE, ((String)iScontri2.get(PARTITE) + "#"));
										iScontri2.put(PUNTEGGI, ((String)iScontri2.get(PUNTEGGI) + "#"));
										if ("tavolino".equals(nome2)) {
											//										System.out.println();
										}
									}
									iScontri2.put(PUNTI, ((Integer)iScontri2.get(PUNTI))+1);
									iScontri2.put(GIOCATE, ((Integer)iScontri2.get(GIOCATE))+1);
									iScontri2.put(PARTITE, ((String)iScontri2.get(PARTITE) + iGolTrasferta + "-" + iGolCasa));
									iScontri2.put(PUNTEGGI, ((String)iScontri2.get(PUNTEGGI) + squadra2.getTotale() + "-" + squadra1.getTotale()+Constant.ICASA));
									mapScontri2.put(nome1, iScontri2);
									totaliScontri.put(nome2, mapScontri2);
								}
							}
						}
					}
				}
				//	System.err.println("---------------------" + " F1... " + "---------------------");
				Map<String, Object> map = new TreeMap(new ReverseOrderTreemap(totaliF1));
				map.putAll(totaliF1);
				Double oldValue=0d;
				int pos=0;
				int conta=0;
				//			ArrayList<Squadra> al=new ArrayList<>();
				System.err.println();
				System.err.println("+++ F1 della giornata " + ggDaCalcolare + " +++");
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					conta++;
					Double punti = (Double) entry.getValue();
					if (!punti.equals(oldValue)) {
						pos=conta;
					}
					oldValue=punti;
					Integer puntiF1 = f1.get(pos);
					String nome = entry.getKey();
					Integer t = (Integer) totaliFormula1.get(nome);
					if (t==null) {
						t=0;
					}
					t=t+puntiF1;
					totaliFormula1.put(nome, t);
					System.err.println(nome + " - " + entry.getValue() + "-" + puntiF1);
				}		
				System.err.println();
			}
		}

		System.err.println("---------------------" + " F1 " + "---------------------");
		Map<String, Object> map = new TreeMap(new ReverseOrderTreemap(totaliFormula1));
		map.putAll(totaliFormula1);
		map.forEach((k,v)->System.err.println(k+"="+v));


		System.err.println("---------------------" + " Battle royal " + "---------------------");
		map = new TreeMap(new ReverseOrderTreemap(totaliBR));
		map.putAll(totaliBR);
		map.forEach((k,v)->System.err.println(k+"="+v));


		System.err.println("---------------------" + " Campionato " + "---------------------");
		for (String keyOriz : totaliScontri.keySet()) {
			System.err.print(keyOriz + "\t");
			//			System.err.println(key + " --> " + totaliScontri.get(key));
		}
		System.err.println();
		for (String keyOriz : totaliScontri.keySet()) {
			System.err.print(keyOriz + "\t");
			for (String keyVert : totaliScontri.keySet()) {
				if (keyVert.equals(keyOriz)) {
					System.err.print("-");
				}else {
					System.err.print(totaliScontri.get(keyOriz).get(keyVert));
				}
				System.err.print("\t");
			}
			System.err.println();
		}

	}

	private void aggiornaF1(List<Squadra> squadre, int gg, Map<String, Object> totaliFormula1BIS, HashMap<Integer, Integer> f1) throws Exception
	{
		HashMap<String,Integer> hmRigheNomi = new HashMap<String,Integer>();
		hmRigheNomi.put("xx",1);

		double dOldValue = -1;
		int iContaPosizioni=0;
		int iContaScarto = 1;
		for (Squadra squadra : squadre) 
		{
			double totalePunti = 0;
			if (totalePunti == dOldValue)
			{
				iContaScarto++;
			}
			else
			{
				dOldValue = totalePunti;
				iContaPosizioni=iContaPosizioni+iContaScarto;
				iContaScarto=1;
			}
			System.err.println(gg + "-" + squadra.getNome() + "-" + totalePunti + "-" + iContaPosizioni);

			Integer t = (Integer) totaliFormula1BIS.get(squadra.getNome());
			if (t==null) {
				t=0;
			}
			t=t+f1.get(iContaPosizioni);
			totaliFormula1BIS.put(squadra.getNome(), t);
		}
	}

}
