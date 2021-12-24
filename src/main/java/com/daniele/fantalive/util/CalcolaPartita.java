package com.daniele.fantalive.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
	private static final Integer MAX_GIORNATA_DA_CALCOLARE = 10;
	private static final boolean SALVA_FILE=false;
	private static final int ICASA=2;
	private static final boolean USA_SPRING=false;
	private static final String PARTITE = "Partite";
	private static final String PUNTEGGI = "Punteggi";
	private static final String PUNTI = "Punti";
	private static final String GIOCATE = "Giocate";

	public static void main(String[] args) throws Exception {
		CalcolaPartita cp = new CalcolaPartita();
		cp.go(args);
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
					return ((Integer) map.get(o2)).compareTo((Integer) map.get(o1));
				}
				else if (map.get(o2) instanceof Double) {
					return ((Double) map.get(o2)).compareTo((Double) map.get(o1));
				} else {
					throw new RuntimeException("Tipo non gestito");
				}

		}
	}

	private void go(String[] args) throws Exception {
		ConfigurableApplicationContext ctx;
		if (USA_SPRING) {
			ctx = new SpringApplicationBuilder(MainClass.class)
					.profiles("DEV")
					.web(true).run(args);
			Main.init(ctx.getBean(SalvaRepository.class),null,ctx.getBean(Constant.class));
			if (false) {
				if (Main.fantaLiveBot.isRunning()) {
					Main.fantaLiveBot.stopBot();
					Main.fantaCronacaLiveBot.stopBot();
				} 
			}
		}

		HashSet<String> hs=new HashSet<>();
		hs.add("2"+ "\t" + "Jonny Fighters"+ "\t" + "C. H. MOLLE");
		hs.add("2"+ "\t" + "Atletico Mikatanto"+ "\t" + "tavolino");
		hs.add("4"+ "\t" + "C. H. MOLLE"+ "\t" + "Atletico Mikatanto");
		hs.add("4"+ "\t" + "tavolino"+ "\t" + "Jonny Fighters");
		hs.add("6"+ "\t" + "Jonny Fighters"+ "\t" + "Atletico Mikatanto");
		hs.add("6"+ "\t" + "C. H. MOLLE"+ "\t" + "tavolino");
		hs.add("8"+ "\t" + "tavolino"+ "\t" + "C. H. MOLLE");
		hs.add("8"+ "\t" + "Atletico Mikatanto"+ "\t" + "Jonny Fighters");
		hs.add("10"+ "\t" + "Atletico Mikatanto"+ "\t" + "C. H. MOLLE");
		hs.add("10"+ "\t" + "Jonny Fighters"+ "\t" + "tavolino");
		hs.add("2"+ "\t" + "Universal"+ "\t" + "VincereAManiBasse");
		hs.add("2"+ "\t" + "Canosa di Puglia..."+ "\t" + "Atletico Conc");
		hs.add("4"+ "\t" + "VincereAManiBasse"+ "\t" + "Canosa di Puglia...");
		hs.add("4"+ "\t" + "Atletico Conc"+ "\t" + "Universal");
		hs.add("6"+ "\t" + "Universal"+ "\t" + "Canosa di Puglia...");
		hs.add("6"+ "\t" + "VincereAManiBasse"+ "\t" + "Atletico Conc");
		hs.add("8"+ "\t" + "Atletico Conc"+ "\t" + "VincereAManiBasse");
		hs.add("8"+ "\t" + "Canosa di Puglia..."+ "\t" + "Universal");
		hs.add("10"+ "\t" + "Canosa di Puglia..."+ "\t" + "VincereAManiBasse");
		hs.add("10"+ "\t" + "Universal"+ "\t" + "Atletico Conc");

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

		System.err.println("---------------------" + " Scontri " + "---------------------");
		for (int ggDaCalcolare=1;ggDaCalcolare<=MAX_GIORNATA_DA_CALCOLARE;ggDaCalcolare++) {
			//			System.err.println("***************:" + ggDaCalcolare);
			String tokenNomeFile = "STORICO_" + ggDaCalcolare  + "_";
			if (SALVA_FILE) {
				Main.scaricaBe(ggDaCalcolare + Constant.DELTA_FS,tokenNomeFile);
			}
			List<Squadra> squadre = Main.getSquadreFromFS(tokenNomeFile,false, true);
			for (Squadra squadra : squadre) {
				List<Giocatore> titolari = squadra.getTitolari();
				List<Giocatore> riserve = squadra.getRiserve();
				List<Giocatore> nuoviTitolari = new ArrayList<>();
				for (Giocatore titolare : titolari) {
					if (titolare.isEsce()) {
						Giocatore r = null;
						for (Giocatore riserva : riserve) {
							if (riserva.isEntra() && titolare.getRuolo().equalsIgnoreCase(riserva.getRuolo())) {
								if (r==null)  {
									nuoviTitolari.add(riserva);
									r=riserva;
								}
							}
						}
						if (r != null) {
							riserve.remove(r);
							riserve.add(titolare);
						}
					} else {
						nuoviTitolari.add(titolare);
					}
				}
				squadra.setTitolariOriginali(titolari);
				squadra.setTitolari(nuoviTitolari);
			}
			//			System.err.println("*********************");
			//			System.err.println(squadre);
			//			System.err.println("*********************");
			Map<String, Object> totaliF1=new HashMap<>(); 
			for (int i1=0;i1<squadre.size();i1++) {
				Squadra squadra1=squadre.get(i1);
				String nome1 = squadra1.getNome();
				totaliF1.put(nome1, squadra1.getTotaleTitolari());					
				for (int i2=0;i2<squadre.size();i2++) {
					boolean isCampionato=false;
					if (i2-i1!=1 || ePari(i2)) {
						if (false) continue;//campionato
					}
					else {
						isCampionato=true;
					}
					Squadra squadra2=squadre.get(i2);
					String nome2 = squadra2.getNome();
					String k = Integer.toString(ggDaCalcolare) + "\t" + nome1 + "\t" + nome2;
					if (false) {//solo coppa
						if (!hs.contains(k)) continue;
					}
					if (!nome1.equals(nome2)){
						if (nome1.startsWith("Jonny") && nome2.startsWith("C.") && ggDaCalcolare==2) {
							//							System.out.println();
						}
						squadra1.setModificatoreDifesa(squadra2.getModificatoreDifesaDaAssegnare());
						squadra2.setModificatoreDifesa(squadra1.getModificatoreDifesaDaAssegnare());
						calcolaModificatoreCentrocampo(squadra1, squadra2);
						int iGolCasa = getGol(squadra1.getTotale()+ICASA);
						int iGolTrasferta = getGol(squadra2.getTotale());
						if (iGolCasa > 0 && iGolCasa == iGolTrasferta && ( Math.abs(squadra1.getTotale() +ICASA - squadra2.getTotale()) >= 4))//FIXME BUG
						{
							/*
						Scarto stessa fascia 4
						Il valore numerico di questo fattore � pari a 4 punti. Si applica nella seguente maniera: se i punteggi delle due squadre si trovano nella stessa fascia di gol,
						 affinch� chi ha totalizzato il punteggio pi� alto vinca la partita � necessario che lo scarto tra i punteggi sia maggiore o uguale allo "scarto stessa fascia". In tal caso viene assegnato un gol in pi� alla squadra con punteggio pi� alto. In caso contrario la partita finisce in pareggio.
						Esempi:
						66 - 71. Entrambe i punteggi ricadono nella fascia di 1 gol. Con le fasce rigide la partita finirebbe 1-1. Utilizzando il fattore in questione, poich� lo scarto dei punteggi � pari a 5 >= 4 (scarto stessa fascia), la partita finisce 1-2.
						67 - 70. Entrambe i punteggi ricadono nella fascia di 1 gol. Con le fasce rigide la partita finirebbe 1-1. Anche utilizzando il fattore in questione, poich� lo scarto dei punteggi � pari a 3 <= 4 (scarto stessa fascia), la partita finisce 1-1.
							 */		
							if (squadra1.getTotale() + ICASA > squadra2.getTotale())
							{
								iGolCasa++;
							}
							else
							{
								iGolTrasferta++;
							}
						}

						if (iGolCasa != iGolTrasferta && ( Math.abs(squadra1.getTotale() + ICASA - squadra2.getTotale()) <= 1))
						{
							/*
						Scarto fasce diverse 1
						Il valore numerico di questo fattore � pari a 3 punti. Si applica nella seguente maniera: se i punteggi delle due squadre si trovano in fasce diverse di gol, 
						affinch� chi ha totalizzato il punteggio pi� alto vinca la partita � necessario che lo scarto tra i punteggi sia maggiore o uguale allo "scarto fasce diverse". 
						Se � minore la partita finisce in pareggio assegnando un gol in pi� alla squadra con punteggio pi� basso.
						Esempi:
						71 - 73. Il primo punteggio ricade nella fascia di 1 gol. Il secondo ricade nella fascia dei 2 gol. Con le fasce rigide la partita finirebbe 1-2. Utilizzando il fattore in questione, poich� lo scarto dei punteggi � pari solo a 2 <= 3 (scarto fasce diverse), la partita finisce 2-2.
						70 - 73. Il primo punteggio ricade nella fascia di 1 gol. Il secondo ricade nella fascia dei 2 gol. Con le fasce rigide la partita finirebbe 1-2. Anche utilizzando il fattore in questione, poich� lo scarto dei punteggi � pari solo a 3 >= 3 (scarto fasce diverse), la partita finisce comunque 1-2.		 
							 */
							if (squadra1.getTotale() + ICASA < squadra2.getTotale())
							{
								iGolCasa++;
							}
							else
							{
								iGolTrasferta++;
							}
						}
						/*
						System.err.println(
								"Giornata: " + ggDaCalcolare + "\n"
								+ nome1 + " --> " + iGolCasa + "\n"
//								+ "\tTot: " + new BigDecimal(squadra1.getTotale(), MathContext.DECIMAL128).setScale(2,BigDecimal.ROUND_HALF_UP).add(new BigDecimal(ICASA)) + "\n" 
//								+ "\tMod Difesa: " + squadra1.getModificatoreDifesa() + "\n" 
//								+ "\tMod Centrocampo: " + squadra1.getModificatoreCentrocampo() + "\n" 
//								+ "\tMod Attacco: " + squadra1.getModificatoreAttacco() + "\n" 
//								+ "\tMalus formazione automatica: " + squadra1.getMalusFormazioneAutomatica() + "\n" 
								+ nome2 + " --> " + iGolTrasferta + "\n"
//								+ "\tTot: " + new BigDecimal(squadra2.getTotale(), MathContext.DECIMAL128).setScale(2,BigDecimal.ROUND_HALF_UP).add(new BigDecimal("0")) + "\n"
//								+ "\tMod Difesa: " + squadra2.getModificatoreDifesa() + "\n" 
//								+ "\tMod Centrocampo: " + squadra2.getModificatoreCentrocampo() + "\n" 
//								+ "\tMod Attacco: " + squadra2.getModificatoreAttacco() + "\n" 
//								+ "\tMalus formazione automatica: " + squadra2.getMalusFormazioneAutomatica() + "\n" 
								);
						;
						 */
						System.err.println(k+"\t" + iGolCasa +"\t" + iGolTrasferta );
						if (iGolCasa>iGolTrasferta) {
							Integer tot = (Integer) totaliBR.get(nome1);
							if (tot==null) {
								tot = 0;
							}
							tot=tot + 3;
							totaliBR.put(nome1, tot);
							if (isCampionato) {
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
								iScontri1.put(PUNTEGGI, ((String)iScontri1.get(PUNTEGGI) + squadra1.getTotale()+ICASA + "-" + squadra2.getTotale()));
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
								iScontri2.put(PUNTEGGI, ((String)iScontri2.get(PUNTEGGI) + squadra2.getTotale() + "-" + squadra1.getTotale()+ICASA));
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
							if (isCampionato) {
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
								iScontri1.put(PUNTEGGI, ((String)iScontri1.get(PUNTEGGI) + squadra1.getTotale()+ICASA + "-" + squadra2.getTotale()));
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
								iScontri2.put(PUNTEGGI, ((String)iScontri2.get(PUNTEGGI) + squadra2.getTotale() + "-" + squadra1.getTotale()+ICASA));
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
							if (isCampionato) {
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
								iScontri1.put(PUNTEGGI, ((String)iScontri1.get(PUNTEGGI) + squadra1.getTotale()+ICASA + "-" + squadra2.getTotale()));
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
								iScontri2.put(PUNTEGGI, ((String)iScontri2.get(PUNTEGGI) + squadra2.getTotale() + "-" + squadra1.getTotale()+ICASA));
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
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				conta++;
				Double punti = (Double) entry.getValue();
				if (!punti.equals(oldValue)) {
					pos=conta;
				}
				Integer puntiF1 = f1.get(pos);
				String nome = entry.getKey();
				Integer t = (Integer) totaliFormula1.get(nome);
				if (t==null) {
					t=0;
				}
				t=t+puntiF1;
				totaliFormula1.put(nome, t);
				/*
				Squadra s = new Squadra();
				s.setNome(nome);
				s.tmp=punti;
				al.add(s);
				*/
				//System.err.println(nome + "-" + punti + "-" + puntiF1);
		    }			
//			aggiornaF1(al, ggDaCalcolare, totaliFormula1BIS, f1);


//			map.forEach((k,v)->System.err.println(k+"="+v));
//			System.err.println(totaliF1);
		}

		System.err.println("---------------------" + " F1 " + "---------------------");
		Map<String, Object> map = new TreeMap(new ReverseOrderTreemap(totaliFormula1));
		map.putAll(totaliFormula1);
		map.forEach((k,v)->System.err.println(k+"="+v));

		/*
		System.err.println("---------------------" + " F1BIS " + "---------------------");
		map = new TreeMap(new ReverseOrderTreemap(totaliFormula1BIS));
		map.putAll(totaliFormula1BIS);
		map.forEach((k,v)->System.err.println(k+"="+v));
		*/
		
		System.err.println("---------------------" + " Battle royal " + "---------------------");
		/*
		List<Entry<String, Integer>> list = new ArrayList<>(totali.entrySet());
		list.sort(Entry.comparingByValue());
		list.forEach(System.err::println);		
		 */
		/*
		Stream<Map.Entry<String, Integer>> sorted =
				totali.entrySet().stream()
			       .sorted(Map.Entry.comparingByValue());
		sorted.forEach(System.err::println);		
		 */
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




		if (USA_SPRING) {
			if (false) {
				ctx.stop();
				ctx.close();
			}
		}
		System.exit(0);

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
			double totalePunti = squadra.tmp;
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
	
	
	private static int getGol(double elabora) {
		int iGolCasa = 0;
		if (elabora<66)
		{
			iGolCasa=0;
		}
		else if (elabora<72)
		{
			iGolCasa=1;
		}
		else if (elabora<78)
		{
			iGolCasa=2;
		}
		else if (elabora<84)
		{
			iGolCasa=3;
		}
		else if (elabora<90)
		{
			iGolCasa=4;
		}
		else if (elabora<96)
		{
			iGolCasa=5;
		}
		else if (elabora<102)
		{
			iGolCasa=6;
		}
		else if (elabora<108)
		{
			iGolCasa=7;
		}
		else 
		{
			iGolCasa=8;
		}
		return iGolCasa;
	}


	private void calcolaModificatoreCentrocampo(Squadra squadra1, Squadra squadra2) {
		BigDecimal sommaC1 = generaCentrocampisti(squadra1);
		BigDecimal sommaC2 = generaCentrocampisti(squadra2);
		applicaModificatoreCentrocampo(squadra1, sommaC1, sommaC2);
		applicaModificatoreCentrocampo(squadra2, sommaC2, sommaC1);

	}

	private void applicaModificatoreCentrocampo(Squadra squadra, BigDecimal sommaC1, BigDecimal sommaC2) {
		if (sommaC1.compareTo(sommaC2)>0)
		{
			BigDecimal subtract = sommaC1.subtract(sommaC2);
			if (subtract.compareTo(new BigDecimal("1"))<0)
			{
				squadra.setModificatoreCentrocampo(0);
			}
			else if (subtract.compareTo(new BigDecimal("2"))<0)
			{
				squadra.setModificatoreCentrocampo(0.5);
			}
			else if (subtract.compareTo(new BigDecimal("3"))<0)
			{
				squadra.setModificatoreCentrocampo(1);
			}
			else if (subtract.compareTo(new BigDecimal("4"))<0)
			{
				squadra.setModificatoreCentrocampo(1.5);
			}
			else if (subtract.compareTo(new BigDecimal("5"))<0)
			{
				squadra.setModificatoreCentrocampo(2);
			}
			else if (subtract.compareTo(new BigDecimal("6"))<0)
			{
				squadra.setModificatoreCentrocampo(2.5);
			}
			else if (subtract.compareTo(new BigDecimal("7"))<0)
			{
				squadra.setModificatoreCentrocampo(3);
			}
			else if (subtract.compareTo(new BigDecimal("8"))<0)
			{
				squadra.setModificatoreCentrocampo(3.5);
			}
			else 
			{
				squadra.setModificatoreCentrocampo(4);
			}
		}
		else
		{
			squadra.setModificatoreCentrocampo(0);
		}
	}

	private BigDecimal generaCentrocampisti(Squadra squadra) {
		BigDecimal ret=new BigDecimal("0");
		List<Giocatore> titolari = squadra.getTitolari();
		for (Giocatore giocatore : titolari) {
			if (giocatore.getRuolo().equalsIgnoreCase("C")) {
				ret=ret.add(new BigDecimal(Double.toString(giocatore.getVoto())));
			}
		}
		int iContaTitolariOriginali=0;//FIXME BUG
		for(Giocatore giocatore : squadra.getTitolariOriginali()) {
			if (giocatore.getRuolo().equalsIgnoreCase("C")) {
				iContaTitolariOriginali++;
			}
		}
		for (int i=iContaTitolariOriginali;i<5;i++) {
			ret=ret.add(new BigDecimal("5"));
		}
		return ret;
	}

}
