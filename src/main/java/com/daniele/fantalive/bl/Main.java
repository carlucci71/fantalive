package com.daniele.fantalive.bl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.Charsets;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;
import org.springframework.core.io.ClassPathResource;

import com.daniele.fantalive.configurazione.SocketHandlerFantalive;
import com.daniele.fantalive.entity.Salva;
import com.daniele.fantalive.model.CambiaTag;
import com.daniele.fantalive.model.ConfigCampionato;
import com.daniele.fantalive.model.Giocatore;
import com.daniele.fantalive.model.Live;
import com.daniele.fantalive.model.Notifica;
import com.daniele.fantalive.model.PartitaSimulata;
import com.daniele.fantalive.model.Return;
import com.daniele.fantalive.model.RigaNotifica;
import com.daniele.fantalive.model.Squadra;
import com.daniele.fantalive.repository.SalvaRepository;
import com.daniele.fantalive.util.Constant;
import com.daniele.fantalive.util.ThreadSeparato;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

	public static Map<String,Object> toSocket;
	public static String MIO_IP;
	public static FantaLiveBOT fantaLiveBot;
	public static FantaCronacaLiveBOT fantaCronacaLiveBot;
	private static Map<Integer, String> sq=null;
	private static Map<String, Integer> sqStatusMatch=new HashMap<>();
	public static HashMap<Integer, String[]> eventi=null;
	private static List<ConfigCampionato> configsCampionato=null;
	public static List<String> sqDaEv= null;
	private static Map<String, Giocatore> oldSnapshot=null;
	private static SalvaRepository salvaRepository=null;

	private static SocketHandlerFantalive socketHandlerFantalive=null;
	private static ScheduledExecutorService executor = null;	
	private static Constant constant=null;
	private static List<Live> oldSnapLives=null;
	private static Map<String, Map<String, String>> oldSnapOrari=null;
	static ObjectMapper mapper;
	static Map<String , String> getIconaIDGioc;	
	static Map<String , Integer> statusMatch;	
	static Map<Integer , ZonedDateTime> calendario;	
	static Set<String > sqJB;	
	static Set<String > giocJB;	
	public static Map<String, String> keyFG=null;
	public static int timeRefresh = 0;

	public static void init(SalvaRepository salvaRepositorySpring, SocketHandlerFantalive socketHandlerSpring, Constant constantSpring) throws Exception {
		executor = Executors.newSingleThreadScheduledExecutor();	
		salvaRepository=salvaRepositorySpring;
		socketHandlerFantalive=socketHandlerSpring;
		constant=constantSpring;
		constant.AUTH_FS=getAuthFS();
		if (calendario==null) {
			calendario = new LinkedHashMap();
			String http = getHTTP("https://www.goal.com/it/notizie/calendario-serie-a-2021-2022-completo/161ug15ioiflh19whgevwxviur");
			//			https://www.tomshw.it/culturapop/calendario-serie-a-2021-22-risultati-e-dove-vedere-le-partite/		
			//			System.out.println(http);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss Z");
			Document doc = Jsoup.parse(http);
			Elements elements = doc.getElementsByClass("tableizer-table");
			for (int i=0;i<elements.size();i++) {
				Element element = elements.get(i);
				Elements elementsTR = element.getElementsByTag("TR");
				String giornata = elementsTR.get(0).text();
				if (giornata.trim().equals("")) continue;
				String giorno="";
				String mese="";
				int iGiornata = Integer.parseInt(giornata.substring(0,giornata.indexOf("ª")));
				for (int ix=1;ix<elementsTR.size();ix++) {
					Elements elementsTD = elementsTR.get(ix).getElementsByTag("TD");
					if (elementsTD.size()<3) continue;
					giorno = elementsTD.get(0).text();
					giorno = lpad(giorno.substring(0,giorno.indexOf("/")),2,'0');
					mese = elementsTD.get(0).text();
					mese = lpad(mese.substring(mese.indexOf("/")+1),2,'0');
				}
				String anno="2021";
				if (Integer.parseInt(mese)<8) anno = "2022";
				ZonedDateTime parse = ZonedDateTime.parse(giorno + "/" + mese + "/" + anno + " - 23:59:00 +0000", dtf);
				calendario.put(iGiornata, parse);
			}

			ZonedDateTime now = ZonedDateTime.now();
			//			now=now.withDayOfMonth(19);
			Set<Integer> keySet = calendario.keySet();
			for (Integer attG : keySet) {
				ZonedDateTime zonedDateTime = calendario.get(attG);
				if (now.isAfter(zonedDateTime)) {
					Constant.GIORNATA = attG +1;
				}
			}
			//			System.out.println(Constant.GIORNATA);
		}
		Main.aggKeyFG();
		if (sqDaEv==null) {
			inizializzaSqDaEv();
		}
		if (toSocket==null) {
			toSocket=new HashMap<>();
			toSocket.put("timeRefresh", 0);
		}
		if (mapper==null) {
			mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}
		if (sqJB==null) {
			sqJB = new HashSet() {{
				//				add("bebocar");
				//				add("FC Cherwood");
				//				add("Juventiboss"); 

			}};
		}
		if (giocJB==null) {
			giocJB = new HashSet() {{
				add("Michela_2000");
				add("PeppePalmentieri");
				add("Andrea_S94");
				add("bebocar");
				add("Zamplano");
				add("Mambo Fc");
				add("Il sigaro di Lippi");
			}};
		}
		if (statusMatch==null) {
			statusMatch = new HashMap() {{
				put("PreMatch",    0);
				put("FirstHalf",    1);
				put("HalfTime",    2);
				put("SecondHalf",    3);
				put("FullTime",    4);
				put("Postponed",    5);
				put("Cancelled",    6);
				put("Walkover",    7);
			}};
		}
		if (getIconaIDGioc==null) {
			getIconaIDGioc = new HashMap() {{
				put("T01",    Constant.T1);
				put("T02",    Constant.T2);
				put("T03",    Constant.T3);
				put("T04",    Constant.T4);
				put("T05",    Constant.T5);
				put("T06",    Constant.T6);
				put("T07",    Constant.T7);
				put("T08",    Constant.T8);
				put("T09",    Constant.T9);
				put("T10",    Constant.T10);
				put("T11",    Constant.T11);
				put("R01",    Constant.R1);
				put("R02",    Constant.R2);
				put("R03",    Constant.R3);
				put("R04",    Constant.R4);
				put("R05",    Constant.R5);
				put("R06",    Constant.R6);
				put("R07",    Constant.R7);
				put("R08",    Constant.R8);
				put("R09",    Constant.R9);
				put("R10",    Constant.R10);
				put("R11",    Constant.R11);
				put("R12",    Constant.R12);
				put("R13",    Constant.R13);
				put("R14",    Constant.R14);
				put("R15",    Constant.R15);
				put("R16",    Constant.R16);
				put("R17",    Constant.R17);
				put("R18",    Constant.R18);
				put("R19",    Constant.R19);
				put("R20",    Constant.R20);
			}};
		}

		if (eventi ==null) {
			eventi = new HashMap<Integer, String[]>();
			eventi.put(1000, new String[] {"portiere imbattuto","1","1","1","1","S",Constant.IMBATTUTO});
			eventi.put(1, new String[] {"ammonito","-0.5","-0.5","-0.5","-0.5","S",Constant.AMMONITO});
			eventi.put(2, new String[] {"espulso","-1","-1","-1","-1","S",Constant.ESPULSO});
			eventi.put(3, new String[] {"gol","3","3","3","3","S",Constant.GOL});
			eventi.put(4, new String[] {"gol subito","-1","-1","-1","-1","S",Constant.GOL_SUBITO});
			eventi.put(7, new String[] {"rigore parato","3","3","3","3","S",Constant.RIGORE_PARATO});
			eventi.put(8, new String[] {"rigore sbagliato","-3","-3","-3","-3","S",Constant.RIGORE_SBAGLIATO});
			eventi.put(9, new String[] {"rigore segnato","3","3","2","2","S",Constant.RIGORE_SEGNATO});
			eventi.put(10, new String[] {"autogol","-2","-3","-3","-3","S",Constant.AUTOGOL});
			eventi.put(11, new String[] {"gol vittoria","0","0","0","0","N",Constant.GOL});
			eventi.put(12, new String[] {"gol pareggio","0","0","0","0","N",Constant.GOL});
			eventi.put(14, new String[] {"uscito","0","0","0","0","S",Constant.USCITO});
			eventi.put(15, new String[] {"entrato","0","0","0","0","S",Constant.ENTRATO});
			eventi.put(16, new String[] {"gol annullato","0","0","0","0","N",Constant.GOL_ANNULLATO});
			eventi.put(17, new String[] {"infortunio","0","0","0","0","N",Constant.INFORTUNIO});
			eventi.put(20, new String[] {"assist low1","0.5","1","1","1","S",Constant.ASSIST});
			eventi.put(21, new String[] {"assist medium1","1","1","1","1","S",Constant.ASSIST});
			eventi.put(22, new String[] {"assist hight1","1.5","1","1","1","S",Constant.ASSIST});
			eventi.put(23, new String[] {"assist low2","0.5","1","1","1","S",Constant.ASSIST});
			eventi.put(24, new String[] {"assist medium2","1","1","1","1","S",Constant.ASSIST});
			eventi.put(25, new String[] {"assist hight2","1.5","1","1","1","S",Constant.ASSIST});
		}
		if (sq==null) {
			sq = new LinkedHashMap<Integer, String>();
			sq.put(1, "ATA");
			sq.put(24, "BEN");
			sq.put(2, "BOL");
			sq.put(21, "CAG");
			sq.put(22, "CRO");
			sq.put(6, "FIO");
			sq.put(8, "GEN");
			sq.put(9, "INT");
			sq.put(10, "JUV");
			sq.put(11, "LAZ");
			sq.put(12, "MIL");
			sq.put(13, "NAP");
			sq.put(107, "PAR");
			sq.put(15, "ROM");
			sq.put(16, "SAM");
			sq.put(17, "SAS");
			sq.put(129, "SPE");
			sq.put(18, "TOR");
			sq.put(19, "UDI");
			sq.put(20, "VER");
			sq.put(5, "EMP");
			sq.put(138, "VEN");
			sq.put(137, "SAL");
		}
		if (configsCampionato==null) {
			configsCampionato = new ArrayList<ConfigCampionato>();
			configsCampionato.add(new ConfigCampionato(24,"FANTAGAZZETTA",Constant.Campionati.JB.name()));
			configsCampionato.add(new ConfigCampionato(24,"FANTAGAZZETTA",Constant.Campionati.LUCCICAR.name()));
			configsCampionato.add(new ConfigCampionato(22,"FANTAGAZZETTA",Constant.Campionati.FANTAVIVA.name()));
			configsCampionato.add(new ConfigCampionato(22,"FANTASERVICE",Constant.Campionati.BE.name()));
		}
	}

	private static String getMinuto(String squadra, Map<String, Map<String, String>> snapOrari) {
		Set<String> keySet = snapOrari.keySet();
		for (String key : keySet) {
			if (squadra.equalsIgnoreCase(key) && snapOrari.get(key) != null && 
					(snapOrari.get(key).get("tag").equalsIgnoreCase("FirstHalf") || snapOrari.get(key).get("tag").equalsIgnoreCase("SecondHalf")) ) {
				return " " + snapOrari.get(key).get("val") + Constant.OROLOGIO + " ";
			}
		}
		return "";
	}


	private static String livesUguali(List<Live> snapLives, Map<String, Map<String, String>> snapOrari) {
		StringBuilder desMiniNotifica=new StringBuilder();
		List<CambiaTag> cambiaTag = orariUguali(snapOrari);
		for (CambiaTag sq : cambiaTag) {
			desMiniNotifica.append("Cambio tag: " + sq + "\n");
		}
		try {
			boolean liveGiocPresente=false;
			boolean liveSqPresente=false;
			for (Live snapLive : snapLives) {
				liveSqPresente=false;
				String snapSq=snapLive.getSquadra();
				for (Live oldLive : oldSnapLives) {
					if(oldLive.getSquadra().equals(snapSq)) {
						liveSqPresente=true;
						for (Map<String,Object> snapMap : snapLive.getGiocatori()) {
							liveGiocPresente=false;
							String snapGioc=(String) snapMap.get("nome");
							Double snapVoto = 0d;
							if (snapMap.get("voto") != null) {
								snapVoto=(Double) snapMap.get("voto");
							}
							for (Map<String,Object> oldMap : oldLive.getGiocatori()) {
								String oldGioc=(String) oldMap.get("nome");
								if (snapGioc.equals(oldGioc)) {
									liveGiocPresente=true;
									Double oldVoto = 0d;
									if (oldMap.get("voto") != null) {
										oldVoto=(Double) oldMap.get("voto");
									}
									if (!snapVoto.equals(oldVoto)) {
										desMiniNotifica.append("Sono cambiati i voti per: " + snapGioc + " da " + oldVoto + " a " + snapVoto);
										desMiniNotifica.append(getMinuto(snapLive.getSquadra(),snapOrari)).append("\n");
									}
									if (!snapMap.get("evento").toString().equals(oldMap.get("evento").toString())) {
										desMiniNotifica.append("Sono cambiati gli eventi per: " + snapGioc + ". Ora sono: ");
										String snapEvento = snapMap.get("evento").toString();
										String[] splitSnapEvento = snapEvento.split(",");
										for (String string : splitSnapEvento) {
											if (!string.equals("")) {
												Integer attSnapEvento = Integer.parseInt(string);
												desMiniNotifica.append(desEvento(attSnapEvento, Constant.Campionati.BE.name()) + " ");
											}
										}
										desMiniNotifica.append(getMinuto(snapLive.getSquadra(),snapOrari)).append("\n");
									}

								}
							}
							if (!liveGiocPresente) {
								desMiniNotifica.append("Nel vecchio live il giocatore non era presente: " + snapGioc);
								desMiniNotifica.append(getMinuto(snapLive.getSquadra(),snapOrari)).append("\n");
							}
						}
						if (isFormazioneIniziale(snapLive,oldLive)) {
							desMiniNotifica.append("Formazioni iniziali: " + snapSq );
							desMiniNotifica.append(getMinuto(snapLive.getSquadra(),snapOrari)).append("\n");
						}
					}
				}
				if (!liveSqPresente) {
					//					desMiniNotifica.append(" Nel vecchio live la squadra non era non presente: " + snapSq);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		}
		return desMiniNotifica.toString();
	}
	private static boolean isFormazioneIniziale(Live snapLive, Live oldLive) {
		return snapLive.getGiocatori().size()==12  && 
				((Double)snapLive.getGiocatori().get(0).get("voto")>0) &&
				((Double)oldLive.getGiocatori().get(0).get("voto")==0);

	}
	private static List<CambiaTag> orariUguali(Map<String, Map<String, String>> snapOrari) {
		List<CambiaTag> ret = new ArrayList<>();
		try {
			Set<String> squadre = snapOrari.keySet();
			for (String squadra : squadre) {
				Map<String, String> snapMap = snapOrari.get(squadra);
				Map<String, String> oldMap = oldSnapOrari.get(squadra);
				if (!snapMap.get("tag").equals(oldMap.get("tag"))) {
					CambiaTag cambiaTag=new CambiaTag();
					cambiaTag.setSquadra(squadra);
					cambiaTag.setTag(snapMap.get("tag"));
					ret.add(cambiaTag);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		}
		return ret;
	}

	public static void snapshot(boolean salva) throws Exception {
		timeRefresh=0;
		Calendar c = Calendar.getInstance();
		boolean snap=false;
		Map<String, Object> getLives = getLives(constant.LIVE_FROM_FILE);
		List<Live> snapLives = (List<Live>) getLives.get("lives");
		Map<String, Map<String, String>> snapOrari = (Map<String, Map<String, String>>) getLives.get("orari");
		String desMiniNotifica="";
		if (oldSnapLives != null) {
			desMiniNotifica= livesUguali(snapLives, snapOrari);
			if (!desMiniNotifica.equals("")) {
				snap=true;
			}
		}
		oldSnapLives=snapLives;
		oldSnapOrari=snapOrari;
		Calendar c2 = Calendar.getInstance();
		//		System.out.println("GET LIVES:" + (c2.getTimeInMillis()-c.getTimeInMillis()));

		Map<String, Return> go = postGo(true, null, null,snapLives,snapOrari);
		Iterator<String> campionati = go.keySet().iterator();
		Map<String,Giocatore> snapshot = new HashMap<String, Giocatore>();
		while (campionati.hasNext()) {
			String campionato = (String) campionati.next();
			Return r = go.get(campionato);
			List<Squadra> squadre = r.getSquadre();
			for (Squadra squadra : squadre) {
				if (sqDaEv.contains(squadra.getNome())) 
				{
					for (Giocatore giocatore : squadra.getTitolari()) {
						if (giocatore.getNome() != null) {
							snapshot.put(r.getCampionato().replaceAll("#", "") + "#" + squadra.getNome().replaceAll("#", "") + "#" + giocatore.getNome().replaceAll("#", ""), giocatore);
						}
					}
					for (Giocatore giocatore : squadra.getRiserve()) {
						if (giocatore.getNome() != null) {
							snapshot.put(r.getCampionato().replaceAll("#", "") + "#" + squadra.getNome().replaceAll("#", "") + "#" + giocatore.getNome().replaceAll("#", ""), giocatore);
						}
					}
				}
			}
		}
		if (snap) {
			Instant instant = Instant.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
			ZoneId zoneId = ZoneId.of( "Europe/Rome" );
			ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
			String time=zdt.format(formatter);
			if (salva) {
				upsertSalva(time + "-" + "orari", toJson(snapOrari));
				upsertSalva(time + "-" + "lives", toJson(snapLives));
			}
			if (oldSnapshot!=null) {
				Iterator<String> iterator = snapshot.keySet().iterator();
				Map<String, Map<String,List<Notifica>>> notifiche = new HashMap();
				Set<String> squadreSchieratoNonSchierato=new HashSet<>();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					Giocatore oldGioc = oldSnapshot.get(key);
					Giocatore newGioc = snapshot.get(key);
					List<Map<Integer,Integer>> findNuoviEventi = findNuoviEventi(oldGioc, newGioc);
					Map<String,RigaNotifica> mapEventi=new HashMap<>();
					String oldTag = oldGioc.getOrario().get("tag");
					String newTag = newGioc.getOrario().get("tag");
					if (newTag.equalsIgnoreCase("PreMatch") && newGioc.isNotificaLive()==false && !oldGioc.isSquadraGioca() && newGioc.isSquadraGioca()) {
						mapEventi.put("NON SCHIERATO",new RigaNotifica(0, "NON SCHIERATO", Constant.NON_SCHIERATO));
						squadreSchieratoNonSchierato.add(newGioc.getSquadra());
					}
					if (newTag.equalsIgnoreCase("PreMatch") && newGioc.isNotificaLive()==true && !oldGioc.isSquadraGioca() && newGioc.isSquadraGioca()) {
						mapEventi.put("SCHIERATO",new RigaNotifica(0, "SCHIERATO", Constant.SCHIERATO));
						squadreSchieratoNonSchierato.add(newGioc.getSquadra());
					}
					if (!squadreSchieratoNonSchierato.contains(newGioc.getSquadra())) {
						boolean segnalaTag=false;
						if (!oldTag.equalsIgnoreCase(newTag)) {
							/* PreMatch Postponed Cancelled Walkover    */
							if (newTag.equals("FirstHalf") && oldTag.equals("PreMatch")) {
								mapEventi.put(newTag,new RigaNotifica(0, newTag, Constant.SEMAFORO_1));
								segnalaTag=true;
							}
							if (newTag.equals("HalfTime")) {
								//								mapEventi.put(newTag,new RigaNotifica(0, newTag, Constant.PAUSA));
								//								segnalaTag=true;
							}
							if (newTag.equals("SecondHalf")) {
								//								mapEventi.put(newTag,new RigaNotifica(0, newTag, Constant.SEMAFORO_2));
								//								segnalaTag=true;
							}
							if (newTag.equals("FullTime")) {
								mapEventi.put(newTag,new RigaNotifica(0, newTag, Constant.FINE_PARTITA));
								segnalaTag=true;
							}
						}
						if (findNuoviEventi.size()>0) {
							for (Map<Integer,Integer> nuovoEvento : findNuoviEventi) {
								Integer ev = nuovoEvento.keySet().iterator().next();
								mapEventi.put(Main.eventi.get(ev)[0],new RigaNotifica(nuovoEvento.get(ev), Main.eventi.get(ev)[0], Main.eventi.get(ev)[6]));
							}
						}

						if (segnalaTag) {
							List<Map<Integer,Integer>> findTuttiEventi = findNuoviEventi(null, newGioc);
							for (Map<Integer,Integer> nuovoEvento : findTuttiEventi) {
								Integer ev = nuovoEvento.keySet().iterator().next();
								mapEventi.put(Main.eventi.get(ev)[0],new RigaNotifica(nuovoEvento.get(ev), Main.eventi.get(ev)[0], Main.eventi.get(ev)[6]));
							}
						}
					}

					if (mapEventi.size()>0) {
						String[] splitKey = key.split("#");
						Notifica notifica = new Notifica();
						Map<String,List<Notifica>> notificheSquadreDelCampionato   = notifiche.get(splitKey[0]);
						if (notificheSquadreDelCampionato == null) {
							notificheSquadreDelCampionato=new HashMap<String, List<Notifica>>();
							notifiche.put(splitKey[0], notificheSquadreDelCampionato);
						}
						List<Notifica> notificheSquadra = notificheSquadreDelCampionato.get(splitKey[1]);
						if (notificheSquadra==null) {
							notificheSquadra=new ArrayList();
							notificheSquadreDelCampionato.put(splitKey[1], notificheSquadra);
						}
						notificheSquadra.add(notifica);
						notifica.setCampionato(splitKey[0]);
						notifica.setAllenatore(splitKey[1]);
						notifica.setSquadra(newGioc.getSquadra());
						notifica.setGiocatore(splitKey[2]);
						notifica.setOrario(newGioc.getOrario());
						notifica.setId(newGioc.getIdGioc());
						notifica.setRuolo(newGioc.getRuolo());
						notifica.setEventi(mapEventi);
						notifica.setVoto(newGioc.getVoto() + newGioc.getModificatore()); 
						if (newGioc.isCambio()) {
							notifica.setCambio("(X)");
						}
						//						System.out.println(notifica);
					}
				}
				Calendar cc=Calendar.getInstance();
				Set<String> keySet = notifiche.keySet();
				if (keySet!= null && keySet.size()>0) {
					StringBuilder des = new StringBuilder();
					for (String camp : keySet) {
						des.append("\n<b><i>").append(Constant.PALLONE).append(camp).append("</i></b>\n");
						Map<String, List<Notifica>> sq = notifiche.get(camp);
						Iterator<String> itSq = sq.keySet().iterator();
						while (itSq.hasNext()) {
							String sqN = (String) itSq.next();
							des.append("\n<b>").append(sqN).append("</b>\n");
							List<Notifica> listN = sq.get(sqN);
							Collections.sort(listN);
							for (Notifica notifica : listN) {
								String ret = " <b>" + getIconaIDGioc.get(notifica.getId()) + "</b> " + 
										getDesRuolo(notifica.getCampionato(), notifica.getRuolo()) + " " + 
										notifica.getGiocatore() + notifica.getCambio()  + " " + notifica.getSquadra() + " " + notifica.getVoto();
								Set<String> ks = notifica.getEventi().keySet();
								for (String key : ks) {
									RigaNotifica rigaNotifica = notifica.getEventi().get(key);
									if (notifica.getEventi().get(key).getConta()==0) {
										ret = ret + "\n     "  + rigaNotifica.getIcona() + "  " + rigaNotifica.getTesto();
									}
								}
								for (String key : ks) {
									RigaNotifica rigaNotifica = notifica.getEventi().get(key);
									Integer contaEv=rigaNotifica.getConta();
									if (contaEv != null && contaEv>0) {
										ret = ret + "\n     ";
										for (int i=0;i<contaEv;i++) {
											ret = ret + rigaNotifica.getIcona() + " ";
										}
										ret = ret + rigaNotifica.getTesto();//TODO ripetere icona
									}
								}
								for (String key : ks) {
									RigaNotifica rigaNotifica = notifica.getEventi().get(key);
									Integer contaEv=rigaNotifica.getConta();
									if (contaEv != null && contaEv<0) {
										ret = ret + "\n     ";
										contaEv=contaEv*-1;
										for (int i=0;i<contaEv;i++) {
											ret = ret + rigaNotifica.getIcona() + " ";
										}
										ret = ret + " --NO-- " + rigaNotifica.getTesto();//TODO ripetere icona
									}
								}
								if ((notifica.getOrario() != null && notifica.getOrario().get("tag") != null) 
										&& (notifica.getOrario().get("tag").equalsIgnoreCase("FirstHalf") || notifica.getOrario().get("tag").equalsIgnoreCase("SecondHalf")) ) {
									des.append(" " + notifica.getOrario().get("val") + Constant.OROLOGIO + " ");
								}

								des.append(ret).append("\n");
							}
						}
					}
					des.append("\n").append(getUrlNotifica());
					Main.inviaCronacaNotifica(des.toString());
					Main.inviaNotifica(des.toString());
					Calendar c4 = Calendar.getInstance();
					//					System.out.println("ONLY INVIA NOTIFICA:" + (c4.getTimeInMillis()-cc.getTimeInMillis()));
				}
			}
			Calendar c3 = Calendar.getInstance();
			//			System.out.println("SNAPSHOT:" + (c3.getTimeInMillis()-c.getTimeInMillis()));
			if (socketHandlerFantalive != null) {
				Map<String, Object> map=new LinkedHashMap<>();
				map.put("res", go);
				map.put("miniNotifica", Base64.getEncoder().encodeToString(desMiniNotifica.getBytes()));
				socketHandlerFantalive.invia(map);
				Main.inviaCronacaNotifica(desMiniNotifica.toString());
			}
			Calendar c4 = Calendar.getInstance();
			//			System.out.println("ONLY WEB SOCKET:" + (c4.getTimeInMillis()-c3.getTimeInMillis()));
		}
		oldSnapshot=snapshot;
		//		System.out.println("FINE SNAPSHOT");
	}

	public static String getUrlNotifica() {
		if (MIO_IP == null) return "";
		if (MIO_IP.equals("192.168.1.83")) {
			return "http://" + MIO_IP + Constant.URL_NOTIFICA_NAS;
		}
		else if (MIO_IP.startsWith("192"))
			return "http://" + MIO_IP + ":7080/";
		else
			return Constant.URL_NOTIFICA_HEROKU;
	}


	public static void main(String[] args) throws Exception {



		/*
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss Z");
		ZonedDateTime parse = ZonedDateTime.parse("07/11/2021 - 23:59:00 +0000", dtf);
		System.out.println(parse);


		init(salvaRepositorySpring, socketHandlerSpring, constantSpring);
		String authFS = getAuthFS();
		System.out.println(authFS);
		 */
	}

	private static String lpad(String inputString, int length, char c) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(c);
		}
		return sb.substring(inputString.length()) + inputString;	
	}

	private static String getAuthFS() throws Exception {
		String ret=null;
		String urlLoginFS = "https://www.fanta.soccer/it/login/";
		String http = getHTTP(urlLoginFS);
		Document doc = Jsoup.parse(http);
		List<FormElement> forms2 = doc.select("body").forms();
		Elements elements = forms2.get(0).elements();
		String viewStateGenerator="";
		String viewState="";
		for (int i=0;i<elements.size();i++) {
			Element element = elements.get(i);
			if ("__VIEWSTATE".equalsIgnoreCase(element.id())) {
				viewState = URLEncoder.encode(element.val(), StandardCharsets.UTF_8.toString());
			}
			if ("__VIEWSTATEGENERATOR".equalsIgnoreCase(element.id())) {
				viewStateGenerator=element.val();
			}
		}

		Map<String, String> headers=new HashMap<>();
		headers.put("User-Agent", "Mozilla");
		String contentType = "application/x-www-form-urlencoded; charset=UTF-8";
		headers.put("content-type", contentType);
		headers.put("x-requested-with", "XMLHttpRequest");
		StringBuilder body = new StringBuilder();
		body.append("ctl00%24smFantaSoccer=ctl00%24MainContent%24wuc_Login1%24upLogin%7Cctl00%24MainContent%24wuc_Login1%24btnLogin");
		body.append("&__EVENTTARGET=");
		body.append("&__EVENTARGUMENT=");
		body.append("&__VIEWSTATE=" + viewState);
		body.append("&__VIEWSTATEGENERATOR=" + viewStateGenerator);
		body.append("&ctl00%24MainContent%24wuc_Login1%24username=" + Constant.UTENTE_FS);
		body.append("&ctl00%24MainContent%24wuc_Login1%24password=" + Constant.PWD_FS);
		body.append("&ctl00%24MainContent%24wuc_Login1%24cmbSesso=M");
		body.append("&ctl00%24MainContent%24wuc_Login1%24txtNome=");
		body.append("&ctl00%24MainContent%24wuc_Login1%24txtCognome=");
		body.append("&ctl00%24MainContent%24wuc_Login1%24txtEmail=");
		body.append("&ctl00%24MainContent%24wuc_Login1%24txtConfermaEmail=");
		body.append("&ctl00%24MainContent%24wuc_Login1%24txtUsername=");
		body.append("&ctl00%24MainContent%24wuc_Login1%24txtPassword=");
		body.append("&ctl00%24MainContent%24wuc_Login1%24txtConfermaPassword=");
		body.append("&__ASYNCPOST=true");
		body.append("&ctl00%24MainContent%24wuc_Login1%24btnLogin=accedi");
		Map<String, Object> postHTTP = postHTTP(contentType,urlLoginFS,body.toString(), headers);
		//		System.out.println(postHTTP.get("response"));
		Map<String, List<String>> headerFields = (Map<String, List<String>>) postHTTP.get("headerFields");

		Set<String> keySet = headerFields.keySet();
		for (String string : keySet) {
			if ("Set-Cookie".equalsIgnoreCase(string)) {
				List<String> list = headerFields.get(string);
				for (String string2 : list) {
					String[] split = string2.split("=");
					if ("FantaSoccer_Auth".equalsIgnoreCase(split[0])) {
						ret = split[1].substring(0,split[1].indexOf(";"));
						//						System.out.println(ret);
					}
				}
			}
		}
		return ret;
	}

	public static void inviaCronacaNotifica(String msg) throws Exception {
		if (!constant.DISABILITA_NOTIFICA_TELEGRAM) {
			fantaCronacaLiveBot.inviaMessaggio(constant.CHAT_ID_FANTALIVE,msg);
		}
		else {
			System.out.println("Notifica:\n" + msg);
		}
	}
	public static void inviaNotifica(String msg) throws Exception {
		if (!constant.DISABILITA_NOTIFICA_TELEGRAM) {
			ThreadSeparato threadSeparato = new ThreadSeparato(fantaLiveBot, constant.CHAT_ID_FANTALIVE,msg);
			executor.schedule(threadSeparato, 15, TimeUnit.SECONDS);
			//			fantaLiveBot.inviaMessaggio(constant.CHAT_ID_FANTALIVE,msg,false);
		}
		else {
			System.out.println("Notifica:\n" + msg);
		}
		Map<String, Object> map=new HashMap<>();
		map.put("notifica", Base64.getEncoder().encodeToString(msg.getBytes()));
		socketHandlerFantalive.invia(map);
	}


	private static Map<String, String> getNomiFG(String lega) throws Exception {
		Map<String, String> ret = new HashMap<String, String>();
		String url = "https://leghe.fantacalcio.it/" + lega + "/area-gioco/rose?";
		String response = getHTTP(url);
		Document doc = Jsoup.parse(response);
		Elements select1 = doc.select(".list-rosters-item");
		Elements select = doc.select(".left-heading-link");
		int size = select1.size();
		for (int i=0;i<size;i++) {
			Element element1 = select1.get(i);
			String squadra = element1.select("H4").first().text();
			String giocatore = element1.select("H5").first().text();
			Element element = select.get(i);
			String link = element.select("A").attr("href");
			link=link.substring(link.lastIndexOf("=")+1);
			//			System.out.println(squadra + ";" + giocatore);
			if (!lega.equalsIgnoreCase("jb-fanta") || (lega.equalsIgnoreCase("jb-fanta") && sqJB.contains(squadra)) || (lega.equalsIgnoreCase("jb-fanta") && giocJB.contains(giocatore))){
				ret.put(link,squadra);
			}
		}
		return ret;
	}

	public static List<Squadra> getSquadreFromFS(String tokenNomeFile, boolean cancella, boolean conVoto) throws Exception {
		List<Squadra> squadre = new ArrayList<Squadra>();
		for (int i=0;i<Constant.NUM_PARTITE_FS;i++) {
			String nome = tokenNomeFile + Constant.Campionati.BE.name()+i + ".html";
			String testo=Main.getTesto(nome);
			if (testo==null) {
				throw new RuntimeException("File non trovato: " + nome);
			}
			Document doc = Jsoup.parse(testo);
			//				System.out.println(testo);
			Squadra squadraCasa = Main.getFromFS(doc, "Casa",i, conVoto);
			Squadra squadraTrasferta = Main.getFromFS(doc, "Trasferta",i, conVoto);
			double modificatoreDifesaDaAssegnareCasa = squadraCasa.getModificatoreDifesaDaAssegnare();
			double modificatoreDifesaDaAssegnareTrasferta = squadraTrasferta.getModificatoreDifesaDaAssegnare();
			squadraCasa.setModificatoreDifesaDaAssegnare(modificatoreDifesaDaAssegnareTrasferta);
			squadraTrasferta.setModificatoreDifesaDaAssegnare(modificatoreDifesaDaAssegnareCasa);
			squadre.add(squadraCasa);
			squadre.add(squadraTrasferta);
			if (cancella) {
				Main.cancellaSalva(nome);
			}
		}
		return squadre;
	}

	public static void scaricaBe(Integer gg, String tokenNomeFile) throws IOException, ClientProtocolException {
		BasicCookieStore cookieStore = new BasicCookieStore();
		BasicClientCookie cookie;
		cookie = new BasicClientCookie("FantaSoccer_Auth", constant.AUTH_FS);
		cookie.setDomain("www.fanta.soccer");
		cookie.setPath("/");
		cookieStore.addCookie(cookie);
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		try {
			for (int i=0;i<4;i++) {
				String uri = "https://www.fanta.soccer/it/lega/privata/" + Constant.COMP_FS + "/dettaglipartita/" + String.valueOf(gg-Constant.DELTA_FS) + "/" + String.valueOf(i + Constant.PRIMA_GIORNATA_FS + (Constant.NUM_PARTITE_FS*(gg-Constant.DELTA_FS-1))) + "/";
				HttpGet httpget = new HttpGet(uri);
				ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
					@Override
					public String handleResponse(
							final HttpResponse response) throws ClientProtocolException, IOException {
						int status = response.getStatusLine().getStatusCode();
						if (status >= 200 && status < 300) {
							HttpEntity entity = response.getEntity();
							return entity != null ? EntityUtils.toString(entity) : null;
						} else {
							throw new ClientProtocolException("Unexpected response status: " + status);
						}
					}

				};
				String responseBody = httpclient.execute(httpget, responseHandler);
				upsertSalva(tokenNomeFile + Constant.Campionati.BE.name() + i + ".html", responseBody);
			}
		} finally {
			httpclient.close();
		}
	}

	private static Map<String, Map<String, String>> partiteLive() throws Exception {
		Map<String, Map<String, String>> orari=null;
		orari=new HashMap<String, Map<String,String>>();
		String callHTTP = getHTTP("https://api2-mtc.gazzetta.it/api/v1/sports/calendar?sportId=" + Constant.SPORT_ID_LIVE_GAZZETTA + "&competitionId=" + Constant.COMP_ID_LIVE_GAZZETTA);
		Map<String, Object> jsonToMap = jsonToMap(callHTTP);
		List<Map> l = (List<Map>) ((Map)jsonToMap.get("data")).get("games");
		for (Map map : l) {
			List<Map> lm = (List<Map>) map.get("matches");
			for (Map map2 : lm) {
				HashMap<String,String> orario=new HashMap<String,String>();
				Map timing = (Map)map2.get("timing");
				orario.put("tag", (String)timing.get("tag"));
				Object valTiming=timing.get("val");
				if (timing.get("val") != null) {
					valTiming= valTiming.toString();

				}
				else {
					valTiming="N/A";
				}
				orario.put("val", valTiming.toString());
				orari.put(((String)((Map)map2.get("awayTeam")).get("teamCode")).toUpperCase(), orario);
				orari.put(((String)((Map)map2.get("homeTeam")).get("teamCode")).toUpperCase(), orario);
			}
		}
		return orari;
	}

	public static Map<String,Object> postHTTP(String contentType, String url, String body,  Map<String, String>... headers) throws Exception {
		//		System.out.println("POST " + url + " " + printMap(headers));
		Map <String, Object> ret = new HashMap<>();
		URL obj = new URL(url);
		HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
		postConnection.setRequestMethod("POST");
		if (headers!=null && headers.length>0) {
			Iterator<String> iterator = headers[0].keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				postConnection.setRequestProperty(key, headers[0].get(key));
			}
		}
		postConnection.setRequestProperty("content-type", contentType);
		postConnection.setDoOutput(true);
		OutputStream os = postConnection.getOutputStream();
		os.write(body.getBytes());
		os.flush();
		os.close();

		int responseCode = postConnection.getResponseCode();
		StringBuffer response = new StringBuffer();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			Map<String, List<String>> headerFields = postConnection.getHeaderFields();
			ret.put("headerFields", headerFields);
			BufferedReader in = new BufferedReader(new InputStreamReader(postConnection.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} else {
			BufferedReader bfOutputResponse = new BufferedReader(
					new InputStreamReader(postConnection.getErrorStream()));
			String outputLine;
			StringBuffer sfResponse = new StringBuffer();
			while ((outputLine = bfOutputResponse.readLine()) != null) {
				sfResponse.append(outputLine);
			}
			bfOutputResponse.close();

			// print result
			String stringResponse = sfResponse.toString();
			throw new RuntimeException("POST NOT WORKED ".concat(url).concat(" -> ").concat(body).concat("STACK:")
					.concat(stringResponse));
		}
		ret.put("response", response.toString());
		return ret; 
	}
	public static Map<String, Object> postHTTP(String url, Map<String, Object> body, Map<String, String>... headers) throws Exception {
		return postHTTP("application/json", url, toJson(body),  headers);
	}
	private static String printMap(Map<String, String>[] headers) {
		StringBuilder sb = new StringBuilder();
		if (headers!=null && headers.length>0) {
			Iterator<String> iterator = headers[0].keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				sb.append(key + " <-> " + headers[0].get(key)); 
			}
		}
		return sb.toString();
	}

	public static String getHTTP(String url, Map<String, String>... headers) throws Exception {
		//		System.out.println("GET " + url + " " + printMap(headers));
		URL obj = new URL(url);
		HttpURLConnection getConnection = (HttpURLConnection) obj.openConnection();
		getConnection.setRequestMethod("GET");
		//		getConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:94.0) Gecko/20100101 Firefox/94.0");

		if (headers!=null && headers.length>0) {
			Iterator<String> iterator = headers[0].keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				getConnection.setRequestProperty(key, headers[0].get(key));
			}
		}
		int responseCode = getConnection.getResponseCode();

		Map<String, List<String>> headerFields = getConnection.getHeaderFields();
		//		System.out.println(headerFields);

		StringBuffer response = new StringBuffer();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(getConnection.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} else {
			BufferedReader bfOutputResponse = new BufferedReader(
					new InputStreamReader(getConnection.getErrorStream()));
			String outputLine;
			StringBuffer sfResponse = new StringBuffer();
			while ((outputLine = bfOutputResponse.readLine()) != null) {
				sfResponse.append(outputLine);
			}
			bfOutputResponse.close();

			// print result
			String stringResponse = sfResponse.toString();
			throw new RuntimeException("GET NOT WORKED ".concat(url).concat(" -> ").concat("STACK:")
					.concat(stringResponse));
		}
		return response.toString(); 
	}

	public static void aggKeyFG() throws Exception {
		int giornata=constant.GIORNATA;
		Main.keyFG=new HashMap<String, String>();



		//https://leghe.fantacalcio.it/servizi/V1_LegheFormazioni/Pagina?id_comp=319871&r=1&f=1_1631365200000.json


		Main.keyFG.put(Constant.Campionati.FANTAVIVA.name(), "id_comp=" + Constant.COMP_VIVA_FG + "&r=" + String.valueOf(giornata - Constant.DELTA_VIVA_FG)  + "&f=" + String.valueOf(giornata - Constant.DELTA_VIVA_FG) + "_" + calcolaAggKey("fanta-viva") + ".json");
		Main.keyFG.put(Constant.Campionati.LUCCICAR.name(), "id_comp=" + Constant.COMP_LUCCICAR_FG + "&r=" + String.valueOf(giornata - Constant.DELTA_LUCCICAR_FG) + "&f=" + String.valueOf(giornata - Constant.DELTA_LUCCICAR_FG) + "_" + calcolaAggKey(Constant.Campionati.LUCCICAR.name()) + ".json");
		Main.keyFG.put(Constant.Campionati.JB.name(), "id_comp=" + Constant.COMP_JB_FG + "&r=" + String.valueOf(giornata - Constant.DELTA_JB_FG) + "&f=" + String.valueOf(giornata - Constant.DELTA_JB_FG) + "_" + calcolaAggKey("jb-fanta") + ".json");
	}

	private static String calcolaAggKey(String lega) throws Exception {
		int giornata=constant.GIORNATA-Constant.DELTA_VIVA_FG;
		if (lega.equalsIgnoreCase(Constant.Campionati.LUCCICAR.name())) giornata=constant.GIORNATA-Constant.DELTA_LUCCICAR_FG;
		if (lega.equalsIgnoreCase(Constant.Campionati.JB.name())) giornata=constant.GIORNATA-Constant.DELTA_JB_FG;
		String url = "https://leghe.fantacalcio.it/" + lega + "/formazioni/" + giornata + "?id=" + Constant.COMP_VIVA_FG;
		String string = Main.getHTTP(url);
		//		System.out.println(string);
		string = string.substring(string.indexOf(".s('tmp', ")+11);
		string=string.substring(0,string.indexOf(")"));
		string = string.replace("|", "@");
		String[] split = string.split("@");
		return split[1];
	}

	public static void getSquadre(String lega) throws Exception {
		try {
			String ll = lega;
			if (lega.equalsIgnoreCase(Constant.Campionati.JB.name())){
				ll = "jb-fanta";
			}
			Map<String, String> nomiFG = getNomiFG(ll);
			lega=lega.replace("-", "").toUpperCase();
			List<Squadra> squadre=new ArrayList<Squadra>();
			Map<String,String> headers = new HashMap<String, String>();
			headers.put("app_key", constant.APPKEY_FG);
			String url = "https://leghe.fantacalcio.it/servizi/V1_LegheFormazioni/Pagina?" + keyFG.get(lega);
			String string = getHTTP(url, headers );
			//		System.out.println(url + " <--> " +  headers + " <--> " + string);
			Map<String, Object> jsonToMap = jsonToMap(string);
			if (jsonToMap.get("data") == null) throw new RuntimeException("aggiornare KeyFG per " + lega);
			List<Map> l = (List<Map>) ((Map<String, Object>)jsonToMap.get("data")).get("formazioni");
			int contaSq=0;
			int progPartita=0;
			for (Map<String, List<Map>> map : l) {
				if (!lega.equalsIgnoreCase(Constant.Campionati.LUCCICAR.name()) && !lega.equalsIgnoreCase(Constant.Campionati.JB.name())) {
					progPartita++;
				}
				List<Map> list = map.get("sq");
				int contaSquadre=0;
				for (Map map2 : list) {
					String nome = nomiFG.get(map2.get("id").toString());
					if (nome == null) continue;
					contaSquadre++;
					List<Map> giocatori = (List<Map>) map2.get("pl");
					if (giocatori != null) {
						for (int i=0;i<giocatori.size();i++) {
							if (i==0) {
								Squadra squadra = new Squadra();
								squadre.add(squadra);
								squadra.setNome(nome);
								List<PartitaSimulata> partiteSimulate=new ArrayList<>();
								PartitaSimulata partitaSimulata=new PartitaSimulata();
								partitaSimulata.setCampionato(lega);
								if (!lega.equalsIgnoreCase(Constant.Campionati.LUCCICAR.name()) && !lega.equalsIgnoreCase(Constant.Campionati.JB.name()) && (contaSquadre == 1)) {
									partitaSimulata.setCasa(true);
								}
								else {
									partitaSimulata.setCasa(false);
								}
								if (lega.equalsIgnoreCase(Constant.Campionati.LUCCICAR.name())) {
									partitaSimulata.setNome("  LUCCICAR");
									squadra.setEvidenza(true);
								} 
								else if (lega.equalsIgnoreCase(Constant.Campionati.JB.name()) && squadra.getNome().equalsIgnoreCase("bebocar")) {
									partitaSimulata.setNome("  JB");
									squadra.setEvidenza(true);
								} 
								else if (lega.equalsIgnoreCase(Constant.Campionati.JB.name()) && !squadra.getNome().equalsIgnoreCase("bebocar")) {
									//								partitaSimulata.setNome("  JB");
									//								squadra.setEvidenza(true);
								} 
								else {
									partitaSimulata.setNome(getNomePartitaSimulata(lega, progPartita));
								}
								partitaSimulata.setSquadra(squadra.getNome());
								partiteSimulate.add(partitaSimulata);
								squadra.setPartiteSimulate(partiteSimulate);
								contaSq++;
							}
							Giocatore g = new Giocatore();
							g.setId(giocatori.get(i).get("id").toString());
							g.setNome(giocatori.get(i).get("n").toString());
							g.setNomeTrim(giocatori.get(i).get("n").toString().replaceAll(" ", ""));
							g.setRuolo(giocatori.get(i).get("r").toString());
							g.setSquadra(giocatori.get(i).get("t").toString());

							if (i<11) {
								squadre.get(contaSq-1).getTitolari().add(g);
							} 
							else {
								squadre.get(contaSq-1).getRiserve().add(g);
							}
						}
					}
				}
			}
			if (!lega.equalsIgnoreCase(Constant.Campionati.LUCCICAR.name()) && !lega.equalsIgnoreCase(Constant.Campionati.JB.name())) {
				adattaNomePartitaSimulata(squadre);
				String nomePartitaSimulata=null;
				for (Squadra squadra : squadre) {//todo evidenze fantaviva
					if (squadra.getNome().equalsIgnoreCase("tavolino")) {
						squadra.setEvidenza(true);
						PartitaSimulata partitaSimulata = squadra.getPartiteSimulate().get(0);
						if (partitaSimulata.isCasa()) {
							squadra.setCasaProiezione(true);
						}
						nomePartitaSimulata=partitaSimulata.getNome();
					}
				}
				for (Squadra squadra : squadre) {
					PartitaSimulata partitaSimulata = squadra.getPartiteSimulate().get(0);
					if (!squadra.getNome().equalsIgnoreCase("tavolino") && partitaSimulata.getNome().equals(nomePartitaSimulata)) {
						squadra.setEvidenza(true);
						if (partitaSimulata.isCasa()) {
							squadra.setCasaProiezione(true);
						}
					}
				}
			}
			upsertSalva(Constant.FORMAZIONE + lega , toJson(squadre));
			//		return squadre;
		}
		catch (Exception e)
		{

		}
	}

	private static List<Squadra> deserializzaSquadraFG(String lega) throws Exception {
		String testo = getTesto(Constant.FORMAZIONE + lega );
		if (testo!=null) {
			return jsonToSquadre(testo);
		}else {
			return new ArrayList<Squadra>();
		}
	}

	public static void cancellaSquadre() throws Exception {
		cancellaSalva(Constant.FORMAZIONE + Constant.Campionati.JB.name());
		cancellaSalva(Constant.FORMAZIONE + Constant.Campionati.LUCCICAR.name());
		cancellaSalva(Constant.FORMAZIONE + Constant.Campionati.FANTAVIVA.name());
		cancellaSalva(Constant.FORMAZIONE + Constant.Campionati.BE.name());
		for (int i=0;i<Constant.NUM_PARTITE_FS;i++) {
			cancellaSalva(Constant.Campionati.BE.name() + i + ".html");
		}
		inizializzaSqDaEv();

	}
	private static void inizializzaSqDaEv() {
		sqDaEv= new ArrayList<String>();
		sqDaEv.add("tavolino");
		sqDaEv.add("Tavolino");
		sqDaEv.add("daddy");
		sqDaEv.add("Team Alberto..04");
		sqDaEv.add("Team Frank..10");
	}

	private static List<Map<Integer,Integer>> findNuoviEventi(Giocatore og, Giocatore ng) {
		List<Map<Integer,Integer>> ret = new ArrayList<>();
		ciclaEventi(og, ng, ret,+1);
		ciclaEventi(ng, og, ret,-1);
		return ret;
	}

	private static void ciclaEventi(Giocatore og, Giocatore ng, List<Map<Integer, Integer>> ret, Integer verso) {
		if (ng != null) {
			for (Integer codEvento : ng.getCodEventi()) {
				if (og==null) {
					System.out.println();
				}

				if (eventi.get(codEvento)[5].equalsIgnoreCase("S")) {
					int contaNuoviEventiOld = 0;
					if (og != null) {
						contaNuoviEventiOld = contaNuoviEventi(codEvento,og);
					}
					int contaNuoviEventiNew = contaNuoviEventi(codEvento,ng);
					if (contaNuoviEventiOld != contaNuoviEventiNew) {
						if (!ret.contains(codEvento)) {
							if (codEvento==1000 && ng != null && og != null && ng.getVoto()==6 && og.getVoto()==0) {
								//								System.out.println("primo imbattuto per: " + ng.getNome());
							}
							else {
								Map<Integer, Integer> m = new HashMap<>();
								m.put(codEvento,verso*(contaNuoviEventiNew-contaNuoviEventiOld));
								ret.add(m);
							}
						}
					}
				}
			}
		}
	}
	private static int contaNuoviEventi(Integer i, Giocatore g) {
		List<Integer> codEventi = g.getCodEventi();
		int ret=0;
		for (Integer integer : codEventi) {
			if (integer.intValue() == i.intValue()) ret++;
		}
		return ret;
	}

	public synchronized static Map<String, Return> go(boolean conLive, String sqDaAddEvid, String sqDaDelEvid) throws Exception {
		List<Live> lives=new ArrayList<Live>();
		Map<String, Map<String, String>> orari=null;
		if (conLive) {
			Map<String, Object> getLives = getLives(constant.LIVE_FROM_FILE);
			lives = (List<Live>) getLives.get("lives");
			orari = (Map<String, Map<String, String>>) getLives.get("orari");

		}
		return postGo(conLive, sqDaAddEvid, sqDaDelEvid,lives,orari);
	}

	private synchronized static Map<String, Return> postGo(boolean conLive, String sqDaAddEvid, String sqDaDelEvid,List<Live> lives,Map<String, Map<String, String>> orari) throws Exception {
		List<Return> go = new ArrayList<Return>();
		for (ConfigCampionato configCampionato : configsCampionato) {
			Return r = getReturn(configCampionato, conLive, lives, orari, false);
			go.add(r);
		}
		Map<String, Return> ret =new TreeMap<String, Return>();
		sqDaEv= new ArrayList<String>();
		for (Return retAtt : go) {
			String campionato = retAtt.getCampionato();
			Return returns = ret.get(campionato);
			if(returns==null) {
				returns=new Return();
				Instant instant = Instant.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
				ZoneId zoneId = ZoneId.of( "Europe/Rome" );
				ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
				returns.setAggiornamento(zdt.format(formatter));
				returns.setConLive(conLive);
				ret.put(campionato, returns);
			}
			returns.setCampionato(campionato);
			returns.setNome(campionato);
			List<Squadra> squadre = returns.getSquadre();
			List<String> sqBeCaricate=new ArrayList<String>();
			for (Squadra sq : retAtt.getSquadre()) {

				if (sq.getNome() != null && (sq.isEvidenza() || sq.getNome().equals(sqDaAddEvid)) && !sq.getNome().equals(sqDaDelEvid)) {
					sqDaEv.add(sq.getNome());
				}
				for (int i=0;i<sq.getTitolari().size();i++) {
					Giocatore giocatore = sq.getTitolari().get(i);
					String id= String.valueOf(i+1);
					if (id.length()==1) id="0" + id;
					giocatore.setIdGioc("T" + id);
				}
				for (int i=0;i<sq.getRiserve().size();i++) {
					Giocatore giocatore = sq.getRiserve().get(i);
					String id= String.valueOf(i+1);
					if (id.length()==1) id="0" + id;
					giocatore.setIdGioc("R" + id);
				}
				if (!sqBeCaricate.contains(sq.getNome())) {
					if (sqDaEv.contains(sq.getNome())) {
						sq.setEvidenza(true);
					} else {
						sq.setEvidenza(false);
					}
					squadre.add(sq);
					sqBeCaricate.add(sq.getNome());
				}
			}
			Collections.sort(squadre);
			for (int i=0;i<squadre.size();i++) {
				squadre.get(i).setProg(i);
			}
			returns.setSquadre(squadre);
		}

		if(conLive) {
			if (ret.get(Constant.Campionati.FANTAVIVA.name()).getSquadre().size()>0) {
				upsertSalva(Constant.FORMAZIONE + Constant.Campionati.FANTAVIVA.name(), toJson(ret.get(Constant.Campionati.FANTAVIVA.name()).getSquadre()));
			}
			if (ret.get(Constant.Campionati.LUCCICAR.name()).getSquadre().size()>0) {
				upsertSalva(Constant.FORMAZIONE + Constant.Campionati.LUCCICAR.name(), toJson(ret.get(Constant.Campionati.LUCCICAR.name()).getSquadre()));
			}
			if (ret.get(Constant.Campionati.JB.name()).getSquadre().size()>0) {
				upsertSalva(Constant.FORMAZIONE + Constant.Campionati.JB.name(), toJson(ret.get(Constant.Campionati.JB.name()).getSquadre()));
			}
			if (ret.get(Constant.Campionati.BE.name()).getSquadre().size()>0) {
				if (ret.get(Constant.Campionati.BE.name()).getSquadre().size() <Constant.NUM_SQUADRE_BE) throw new RuntimeException("Squadre mangiate. PostGo");
				upsertSalva(Constant.FORMAZIONE + Constant.Campionati.BE.name(), toJson(ret.get(Constant.Campionati.BE.name()).getSquadre()));
			}
		}


		return ret;
	}

	public static Map<String, Object> getLives(boolean fromFile) throws Exception {
		Map orari;
		List<Live> lives = new ArrayList<Live>();
		if (fromFile) {
			orari =  jsonToMap(getTesto("orari"));
			lives =  jsonToLives(getTesto("lives"));

		} else {
			orari=partiteLive();
			Iterator<Integer> iterator = sq.keySet().iterator();
			while (iterator.hasNext()) {
				Integer integer = (Integer) iterator.next();
				//https://www.fantacalcio.it/api/live/10?g=23&i=15
				String sqFromLive = getHTTP("https://www.fantacalcio.it/api/live/" + integer + "?g=" + constant.GIORNATA + "&i=" + Constant.I_LIVE_FANTACALCIO);
				List<Map<String, Object>> getLiveFromFG = jsonToList(sqFromLive);
				Live live = new Live();
				live.setSquadra(sq.get(integer));
				live.setGiocatori(getLiveFromFG);
				lives.add(live);
			}
		}

		for (Live live : lives) {
			List<Map<String, Object>> giocatori = live.getGiocatori();
			for (Map<String,Object> giocatore : giocatori) {
				//				System.out.println(giocatore.get("nome") + "-" + giocatore.get("voto") + "-");
				if (giocatore.get("voto")!=null && Double.parseDouble(giocatore.get("voto").toString())==55) {
					giocatore.put("voto", 0d);
				}
			}
		}

		Set<String> keySetOrari = orari.keySet();
		for (String squadra : keySetOrari) {
			Map orario = (Map) orari.get(squadra);
			//			System.out.println(squadra + "-" + orario.get("tag"));
			String tag = getStatusMatch((String) orario.get("tag"), sqStatusMatch.get(squadra));
			orario.put("tag", tag);
			sqStatusMatch.put(squadra,statusMatch.get(tag));
		}

		Map<String, Object> ret = new HashMap<>();
		ret.put("orari", orari);
		ret.put("lives", lives);
		return ret;
	}

	private static String getStatusMatch(String tag, Integer oldStatusMatch) {
		if (oldStatusMatch!=null) {
			Integer attStatusMatch = statusMatch.get(tag);
			if (oldStatusMatch>attStatusMatch) {
				Set<String> keySetSM = statusMatch.keySet();
				for (String keySM : keySetSM) {
					if (statusMatch.get(keySM).equals(oldStatusMatch)) {
						return keySM;
					}
				}
			}
		}
		return tag;
	}

	private static Return getReturn(ConfigCampionato configCampionato, boolean conLive, List<Live> lives,Map<String, Map<String, String>> orari, boolean conVoto) throws Exception {
		Integer numGiocatori = configCampionato.getNumGiocatori();
		String tipo = configCampionato.getTipo();
		String campionato = configCampionato.getCampionato();
		Return r=new Return();
		r.setNome(campionato.toUpperCase());
		r.setCampionato(campionato.toUpperCase());
		Map<String, List<Squadra>>squadre=new HashMap<String, List<Squadra>>();
		squadre.put(campionato, valorizzaSquadre(campionato,numGiocatori,tipo));
		Set<String> keySet = squadre.keySet();
		for (String string : keySet) {
			List<Squadra> list = squadre.get(string);
			for (Squadra squadra : list) {
				for (int i=0;i<squadra.getTitolari().size();i++) {
					Giocatore giocatore = squadra.getTitolari().get(i);
					giocatore.setModificatore(0);
					giocatore.setVoto(0);
					giocatore.setSquadraGioca(false);
					giocatore.setEvento("");
					giocatore.setNotificaLive(false);
					giocatore.setCodEventi(new ArrayList<Integer>());
				}
				for (int i=0;i<squadra.getRiserve().size();i++) {
					Giocatore giocatore = squadra.getRiserve().get(i);
					giocatore.setModificatore(0);
					giocatore.setVoto(0);
					giocatore.setSquadraGioca(false);
					giocatore.setEvento("");
					giocatore.setNotificaLive(false);
					giocatore.setCodEventi(new ArrayList<Integer>());
				}
			}
		}
		for (Live live : lives) {
			for (Map<String, Object> gg : live.getGiocatori()) {
				//				System.out.println(gg.get("nome") + ";" + gg.get("ruolo") + ";" + live.getSquadra());
				double modificatore=0;
				String evento = (String) gg.get("evento");
				String ev="";
				List<Integer> codEventi=new ArrayList<Integer>();
				if (!evento.equals("")) {
					String[] split = evento.split(",");
					for (String eventoAtt : split) {
						String[] eventiAtt = eventi.get(Integer.parseInt(eventoAtt));
						if (eventiAtt==null) {
							throw new RuntimeException("Evento non censito: " + eventoAtt + " per " + gg.get("nome"));
						}
						if (eventiAtt[5].equals("N")) continue;
						if (eventiAtt==null) {
							ev = ev + "?" + "   ";
							modificatore=modificatore-1000;
						}else {
							ev = ev + eventiAtt[0] + "   ";
							int pos=1;
							if (r.getCampionato().equalsIgnoreCase(Constant.Campionati.FANTAVIVA.name())) pos=1;
							if (r.getCampionato().equalsIgnoreCase(Constant.Campionati.LUCCICAR.name())) pos=2;
							if (r.getCampionato().equalsIgnoreCase(Constant.Campionati.BE.name())) pos=3;
							if (r.getCampionato().equalsIgnoreCase(Constant.Campionati.JB.name())) pos=4;
							modificatore=modificatore+Double.parseDouble(eventiAtt[pos]);
						}
						codEventi.add(Integer.parseInt(eventoAtt));
					}
					gg.put("eventodecodificato", ev);
					gg.put("codEventi", codEventi);
					gg.put("modificatore", modificatore);
				}
			}
		}
		for (Squadra squadra : squadre.get(campionato)) {
			for (Giocatore giocatore : squadra.getTitolari()) {
				findGiocatoreInLives(giocatore, lives,tipo, conVoto);
				if (giocatore != null && giocatore.getRuolo() != null && (giocatore.getRuolo().equalsIgnoreCase("POR") || giocatore.getRuolo().equalsIgnoreCase("P"))) {
					if (giocatore.getVoto()>0 && !giocatore.getCodEventi().contains(4) && !giocatore.getCodEventi().contains(1000)) {
						giocatore.getCodEventi().add(1000);
						giocatore.setModificatore(giocatore.getModificatore()+1);
					}
				}
				if (giocatore != null && giocatore.getSquadra()!=null) {
					if (orari != null) {
						giocatore.setOrario(orari.get(giocatore.getSquadra().toUpperCase()));
					}
				}
			}
		}
		for (Squadra squadra : squadre.get(campionato)) {
			for (Giocatore giocatore : squadra.getRiserve()) {
				findGiocatoreInLives(giocatore, lives,tipo, conVoto);
				if (giocatore.getRuolo().equalsIgnoreCase("POR") || giocatore.getRuolo().equalsIgnoreCase("P")) {
					if (giocatore.getVoto()>0 && !giocatore.getCodEventi().contains(4) && !giocatore.getCodEventi().contains(1000)) {
						giocatore.getCodEventi().add(1000);
						giocatore.setModificatore(giocatore.getModificatore()+1);
					}
				}
				if (giocatore != null && giocatore.getSquadra()!=null) {
					if (orari != null) {
						giocatore.setOrario(orari.get(giocatore.getSquadra().toUpperCase()));
					}
				}
			}
		}
		r.setSquadre(squadre.get(campionato));
		r.setConLive(conLive);
		return r;
	}

	private static void findGiocatoreInLives(Giocatore giocatore, List<Live> lives, String tipo, boolean conVoto) {
		for (Iterator iterator = lives.iterator(); iterator.hasNext();) {
			Live live = (Live) iterator.next();
			if (giocatore == null) {
				//								System.out.println(live + "-" + giocatore);
			}
			if (giocatore != null && giocatore.getSquadra() != null && live.getSquadra().equals(giocatore.getSquadra().toUpperCase())) {
				List<Map<String, Object>> giocatori = live.getGiocatori();
				if (giocatori.size()>11) {
					for (Map<String, Object> map : giocatori) {
						if (map.get("voto")!=null && Double.parseDouble(map.get("voto").toString()) > 0) {
							/*
							if (map.get("nome")!= null &&  map.get("nome").toString().toUpperCase().startsWith("DZ")) {
								System.out.println();
							}
							 */
							giocatore.setSquadraGioca(true);
						}
					}
				}

				for (Map<String, Object> g : giocatori) {
					String nomeGiocatoreLive=g.get("nome").toString();
					List<Integer> codEventi=new ArrayList<Integer>();
					if (g.get("codEventi") != null) {
						codEventi = (List<Integer>) g.get("codEventi");
					}
					String eventodecodificato="";
					if (g.get("eventodecodificato") != null) {
						eventodecodificato = g.get("eventodecodificato").toString();
					}
					double modificatore=0;
					if (g.get("modificatore") != null) {
						modificatore = (Double) g.get("modificatore");
					}
					String votoLive=g.get("voto").toString();
					String eventoLive=g.get("evento").toString();
					if (tipo.equals("FANTAGAZZETTA") && 
							giocatore.getNomeTrim().equalsIgnoreCase(nomeGiocatoreLive.replaceAll(" ", "")
									)
							|| 
							(tipo.equals("FANTASERVICE") && 
									giocatore.getNome().substring(0,giocatore.getNome().lastIndexOf(" ")).replaceAll(" ", "").equalsIgnoreCase(nomeGiocatoreLive.replaceAll(" ", "")))	
							) {
						giocatore.setVoto(Double.parseDouble(votoLive));
						giocatore.setEvento(eventodecodificato);
						giocatore.setCodEventi(codEventi);
						giocatore.setModificatore(modificatore);
						giocatore.setNotificaLive(true);
					}
				}
			}
		}

	}

	private static List<Squadra> valorizzaSquadre(String nomefile, int numGiocatori, String tipo) throws Exception {
		List<Squadra> squadre=new ArrayList<Squadra>();
		squadre.addAll(deserializzaSquadraFG(nomefile));
		return squadre;
	}

	public static Squadra getFromFS(Document doc, String dove, int progPartita, boolean conVoto ) {
		Element first = doc.select(".table-formazione" + dove.toLowerCase() + "-fantapartita").first();
		Elements select = first.select("th");
		Squadra squadra = new Squadra();
		String nomeSq = select.first().text();
		if (nomeSq.contains("-") &&  nomeSq.lastIndexOf(" ")>-1) {
			nomeSq=nomeSq.substring(0,nomeSq.lastIndexOf(" "));
		}
		squadra.setNome(nomeSq);

		List<PartitaSimulata> partiteSimulate=new ArrayList<>();
		PartitaSimulata partitaSimulata=new PartitaSimulata();
		partitaSimulata.setCampionato(Constant.Campionati.BE.name());
		if (dove.equalsIgnoreCase("Casa")) {
			partitaSimulata.setCasa(true);
		}
		else {
			partitaSimulata.setCasa(false);
		}
		partitaSimulata.setNome(getNomePartitaSimulata(Constant.Campionati.BE.name(), progPartita));
		partitaSimulata.setSquadra(squadra.getNome());
		partiteSimulate.add(partitaSimulata);
		squadra.setPartiteSimulate(partiteSimulate);


		for (int i=0;i<11;i++) {
			Giocatore giocatore = estraiGiocatoreFromFS(doc,i,dove,"Titolari", conVoto);
			if (giocatore != null) {
				squadra.getTitolari().add(giocatore);
			}
		}
		for (int i=0;i<20;i++) {
			Giocatore giocatore = estraiGiocatoreFromFS(doc,i,dove,"Panchinari", conVoto);
			if (giocatore != null) {
				squadra.getRiserve().add(giocatore);
			}
		}

		if (conVoto) {
			Element element = doc.select(".table-formazione" + dove.toLowerCase() + "-fantapartita").get(1);
			Elements elementsByTagTR = element.getElementsByTag("TR");
			for (int ix=0;ix<elementsByTagTR.size();ix++) {
				Element elementAtt = elementsByTagTR.get(ix);
				Elements elementsByTagTD = elementAtt.getElementsByTag("TD");
				if (elementsByTagTD.size() == 2) {
					String val;
					String des;
					if (dove.equalsIgnoreCase("casa")) {
						val=elementsByTagTD.get(0).text();
						des=elementsByTagTD.get(1).text();
					}
					else {
						val=elementsByTagTD.get(1).text();
						des=elementsByTagTD.get(0).text();
					}
					if (des.equalsIgnoreCase("Modificatore difesa")) {
						squadra.setModificatoreDifesaDaAssegnare(Double.parseDouble(val.replace(",", ".")));
					}
					if (des.equalsIgnoreCase("Modificatore attacco")) {
						squadra.setModificatoreAttacco(Double.parseDouble(val.replace(",", ".")));
					}
					if (des.equalsIgnoreCase("Malus formazione automatica")) {
						squadra.setMalusFormazioneAutomatica(Double.parseDouble(val.replace(",", ".")));
					}
				}
			}
		}

		return squadra;
	}
	public static void adattaNomePartitaSimulata(List<Squadra> squadre) {
		Map<String, List<String>> m = new HashMap<>();
		for (Squadra squadra : squadre) {
			List<PartitaSimulata> partiteSimulate = squadra.getPartiteSimulate();
			for (PartitaSimulata partitaSimulata : partiteSimulate) {
				List<String> list = m.get(partitaSimulata.getNome());
				if (list == null) {
					list=new ArrayList<>();
				}
				list.add(partitaSimulata.getSquadra());
				m.put(partitaSimulata.getNome(), list);
			}
		}

		for (Squadra squadra : squadre) {
			List<PartitaSimulata> partiteSimulate = squadra.getPartiteSimulate();
			for (PartitaSimulata partitaSimulata : partiteSimulate) {
				String nuovoNome=partitaSimulata.getNome().substring(0,partitaSimulata.getNome().length()-1);
				List<String> listaSquadre = m.get(partitaSimulata.getNome());
				for (String nomeSquadra : listaSquadre) {
					nuovoNome= nuovoNome + " " + nomeSquadra.substring(0,3);
				}
				partitaSimulata.setNome(nuovoNome);
			}
		}
	}
	private static String getNomePartitaSimulata(String lega,int progPartita) {
		return lega.substring(0,1) + (1+progPartita);
	}

	private static Giocatore estraiGiocatoreFromFS(Document doc, int i, String dove, String ruolo, boolean conVoto) {

		Element first = doc.select("#MainContent_wuc_DettagliPartita1_rpt" + ruolo + dove + "_lblNome_" + i).first();
		Giocatore giocatore = null;
		if (first != null) {
			giocatore = new Giocatore();
			String text = first.text();
			String squadra=text.substring(text.indexOf("(")+1,text.length()-1);
			if(!text.equalsIgnoreCase("-")) {
				//				String squadra = first.childNodes().get(1).toString().substring(2,5);
				String nomeG=text.substring(0,text.indexOf("(")-1);
				if (nomeG.equalsIgnoreCase("Fabian Ruiz .")) nomeG="Ruiz .";
				if (nomeG.equalsIgnoreCase("Leao R.")) nomeG="Rafael Leao ";
				if (nomeG.equalsIgnoreCase("Zapata D.")) nomeG="Zapata D. ";
				if (nomeG.equalsIgnoreCase("Samu Castillejo .")) nomeG="Castillejo ";
				if (nomeG.equalsIgnoreCase("Kulusekvski D.")) nomeG="Kulusevski ";
				if (nomeG.equalsIgnoreCase("Joao Pedro Galvao .")) nomeG="Joao Pedro ";
				if (nomeG.equalsIgnoreCase("Ricci M.")) nomeG="Ricci M. ";
				if (nomeG.equalsIgnoreCase("Hernandez T.")) nomeG="Hernandez T. ";
				if (nomeG.equalsIgnoreCase("Kessie F.")) nomeG="Kessie' ";
				if (nomeG.equalsIgnoreCase("Rafael Toloi .")) nomeG="Toloi ";
				if (nomeG.equalsIgnoreCase("Martinez L.")) nomeG="Martinez L. ";
				if (nomeG.equalsIgnoreCase("Donnarumma G.")) nomeG="Donnarumma G. ";
				if (nomeG.equalsIgnoreCase("Pezzella G.")) nomeG="Pezzella Giu. ";
				if (nomeG.equalsIgnoreCase("Gerard Deulofeu .")) nomeG="Deulofeu ";
				if (nomeG.equalsIgnoreCase("Brahim Diaz .")) nomeG="Diaz B. ";
				if (nomeG.equalsIgnoreCase("Nwankwo S.")) nomeG="Simy ";
				if (nomeG.equalsIgnoreCase("Cristiano Ronaldo .")) nomeG="Ronaldo ";
				if (nomeG.equalsIgnoreCase("Roger Ibanez .")) nomeG="Ibanez ";
				if (nomeG.equalsIgnoreCase("Lopez M.")) nomeG="Lopez M. ";
				if (nomeG.equalsIgnoreCase("Gonzalo Villar .")) nomeG="Villar ";
				if (nomeG.equalsIgnoreCase("Kouame C.")) nomeG="Kouame' ";
				if (nomeG.equalsIgnoreCase("Ferrari G.")) nomeG="Ferrari G. ";
				if (nomeG.equalsIgnoreCase("Montipo L.")) nomeG="Montipo' ";
				if (nomeG.equalsIgnoreCase("Nkoulou N.")) nomeG="N'Koulou ";
				if (nomeG.equalsIgnoreCase("Jose Callejon .")) nomeG="Callejon ";
				if (nomeG.equalsIgnoreCase("Rodriguez R.")) nomeG="Rodriguez R. ";
				if (nomeG.equalsIgnoreCase("Pellegrini L.")) nomeG="Pellegrini Lo. ";
				if (nomeG.equalsIgnoreCase("Balde K.")) nomeG="Keita B. ";
				if (nomeG.equalsIgnoreCase("Donnarumma A.")) nomeG="Donnarumma An. ";
				if (nomeG.equalsIgnoreCase("Alex Sandro .")) nomeG="Alex Sandro ";
				if (nomeG.equalsIgnoreCase("Junior Messias ."))  nomeG="Messias ";
				if (nomeG.equalsIgnoreCase("Borja Mayoral .")) nomeG="Mayoral ";
				if (nomeG.equalsIgnoreCase("N'Zola M.")) nomeG="Nzola ";
				if (nomeG.equalsIgnoreCase("Molina N.") && squadra.equalsIgnoreCase("UDI")) nomeG="Molina N. ";
				if (nomeG.equalsIgnoreCase("Danilo .") && squadra.equalsIgnoreCase("BOL")) nomeG="Danilo LAR. ";
				/*
			if (nomeG.toUpperCase().contains("ESSI")) {
				System.err.println("*" + nomeG + "*");
			}
				 */

				if (nomeG.equalsIgnoreCase("")) nomeG="";
				if (nomeG.equalsIgnoreCase("")) nomeG="";
				if (nomeG.equalsIgnoreCase("")) nomeG="";
				if (nomeG.equalsIgnoreCase("")) nomeG="";
				if (nomeG.equalsIgnoreCase("")) nomeG="";
				if (nomeG.equalsIgnoreCase("")) nomeG="";
				/*
				if (nomeG.contains("alvao")) {//TODO
					System.out.println();
				}
				 */
				giocatore.setNome(nomeG);
				giocatore.setNomeTrim(nomeG.replaceAll(" ", ""));
				giocatore.setSquadra(squadra);
				if (conVoto) {
					String textVoto = doc.select("#MainContent_wuc_DettagliPartita1_rpt" + ruolo + dove + "_lblVoto_" + i).first().text();
					if (!textVoto.equals("-") && !textVoto.equals("s.v.")) {
						giocatore.setVoto(Double.parseDouble(textVoto.replace(",", ".")));
					}
					Elements elementsByTag = doc.select("#MainContent_wuc_DettagliPartita1_rpt" + ruolo + dove + "_lblBM_" + i).first().getElementsByTag("IMG");
					for (int ix=0;ix<elementsByTag.size();ix++) {
						Element element = elementsByTag.get(ix);
						String tipoBM = element.attr("data-original-title");
						addBM(tipoBM,giocatore);
					}
					Elements iconaEntreEsce = doc.select("#MainContent_wuc_DettagliPartita1_rpt" +  ruolo + dove +  "_imgIcona_" + i);
					int size = iconaEntreEsce.size();
					if (size > 0) {
						if ("Entra".equalsIgnoreCase(iconaEntreEsce.first().attr("data-original-title"))) {
							giocatore.setEntra(true);
						}
						if ("Esce".equalsIgnoreCase(iconaEntreEsce.first().attr("data-original-title"))) {
							giocatore.setEsce(true);
						}
					}

				}
				first = doc.select("#MainContent_wuc_DettagliPartita1_rpt" + ruolo + dove + "_lblRuolo_" + i).first();
				giocatore.setRuolo(first.text());
			}
		}
		return giocatore; 
	}

	private static void addBM(String tipoBM, Giocatore giocatore) {
		giocatore.getModificatori().add(tipoBM);
		if (tipoBM.equals("1 assist (+1)"))
		{
			giocatore.setModificatore(giocatore.getModificatore()+1);
		} 
		else if (tipoBM.equals("Ammonito (-0,5)"))
		{
			giocatore.setModificatore(giocatore.getModificatore()-0.5);
		} 
		else if (tipoBM.equals("Espulso (-1)"))
		{
			giocatore.setModificatore(giocatore.getModificatore()-1);
		} 
		else if (tipoBM.equals("porta imbattuta (1)"))
		{
			giocatore.setModificatore(giocatore.getModificatore()+1);
		} 
		else if (tipoBM.equals("1 gol subito (-1)"))
		{
			giocatore.setModificatore(giocatore.getModificatore()-1);
		} 
		else if (tipoBM.equals("1 rigore segnato (+2)"))
		{
			giocatore.setModificatore(giocatore.getModificatore()+2);
			giocatore.setNumGol(giocatore.getNumGol()+1);
		} 
		else if (tipoBM.equals("1 gol segnato (+3)"))
		{
			giocatore.setModificatore(giocatore.getModificatore()+3);
			giocatore.setNumGol(giocatore.getNumGol()+1);
		}
		else if (tipoBM.equals("1 rigore fallito (-3)"))
		{
			giocatore.setModificatore(giocatore.getModificatore()-3);
		}
		else if (tipoBM.equals("1 rigore parato (+3)"))
		{
			giocatore.setModificatore(giocatore.getModificatore()+3);
		}
		else if (tipoBM.equals("1 autorete (-3)"))
		{
			giocatore.setModificatore(giocatore.getModificatore()-3);
		}
		else
		{
			throw new RuntimeException("Gestire BM:" + tipoBM);
		}
	}

	public static String toJson(Object o)
	{
		try
		{
			byte[] data = mapper.writeValueAsBytes(o);
			return new String(data, Charsets.ISO_8859_1);
		} catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		} 
	}
	public static List<Squadra> jsonToSquadre(String json){
		try
		{
			return mapper.readValue(json, new TypeReference<List<Squadra>>(){});
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	public static List<Live> jsonToLives(String json){
		try
		{
			return mapper.readValue(json, new TypeReference<List<Live>>(){});
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	public static List<Map<String, Object>> jsonToList(String json)
	{
		try
		{
			return mapper.readValue(json, new TypeReference<List<Map<String, Object>>>(){});
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	public static Map<String, Object> jsonToMap(String json)
	{
		try
		{
			return mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	public static void svecchiaFile() {
		Iterable<Salva> findAll = salvaRepository.findAll();
		for (Salva salva : findAll) {
			if(!salva.getNome().equals("lives") && !salva.getNome().equals("orari") && !salva.getNome().startsWith(Constant.FORMAZIONE)) {
				salvaRepository.delete(salva);
			}
		}
	}
	public static List<String> getNomiTesto(String like) {
		List<String> ret = new ArrayList<>();
		Iterable<Salva> findAll = salvaRepository.findAll();
		for (Salva salva : findAll) {
			if (like.equals("%") || salva.getNome().startsWith(like))
				ret.add(salva.getNome());
		}
		Collections.sort(ret, Collections.reverseOrder());
		return ret;
	}
	public static List<Salva> getValTesto(String like) {
		List<Salva> ret = new ArrayList<>();
		Iterable<Salva> findAll = salvaRepository.findAll();
		for (Salva salva : findAll) {
			if (like.equals("%") || (salva != null && salva.getNome() != null && salva.getNome().startsWith(like)))
				ret.add(salva);
		}
		return ret;
	}
	public static String getTesto(String nome) throws Exception {
		if (salvaRepository==null) {
			return getTestoNoSpring(nome);
		}
		Salva findOne = salvaRepository.findOne(nome);
		if (findOne==null) return null;
		return findOne.getTesto();
	}
	public static String getTestoNoSpring(String nome) throws Exception {
		Properties prop = new Properties();
		prop.load(new ClassPathResource("application-DEV.properties").getInputStream());
		String datasourceUrl = prop.getProperty("spring.datasource.url");
		Connection connection = DriverManager.getConnection(datasourceUrl);
		PreparedStatement prepareStatement = connection.prepareStatement("select * from salva where nome=?");
		prepareStatement.setString(1, nome);
		ResultSet rs = prepareStatement.executeQuery();
		rs.next();
		return rs.getString("testo");
	}
	public static void upsertSalva(String nome, String testo) {
		Salva findOne = salvaRepository.findOne(nome);
		if (findOne==null) {
			findOne=new Salva();
			findOne.setNome(nome);
		}
		findOne.setTesto(testo);
		salvaRepository.save(findOne);
	}
	public static boolean esisteSalva(String nome) {
		return salvaRepository.exists(nome);
	}

	public static void cancellaSalva(String nome) {
		if (salvaRepository.exists(nome)) {
			salvaRepository.delete(nome);
		}
	}
	public static Map<String, Object>  getPartitaSimulata(Long chatId, String nomePartitaSimulata) throws Exception{
		Map<String, Object> ret = new HashMap<>();
		List<String> squadreKey=new ArrayList<>();
		List<String> squadreCasa=new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		Map<String, Return> go = Main.go(true,null, null);
		for (String campionato : go.keySet()) {
			Return return1 = go.get(campionato);
			List<Squadra> squadre = return1.getSquadre();
			for (Squadra squadra : squadre) {
				List<PartitaSimulata> partiteSimulate = squadra.getPartiteSimulate();
				for (PartitaSimulata partitaSimulata : partiteSimulate) {
					if (partitaSimulata.getNome().equalsIgnoreCase(nomePartitaSimulata)) {
						sb.append(proiezioneSquadra(squadra, partitaSimulata.isCasa()));
						if (partitaSimulata.isCasa()) {
							squadreCasa.add(squadra.getNome());
						}
						squadreKey.add(campionato + "-" + squadra.getNome());
						ret.put("campionato", campionato);
					}
				}
			}
		}
		ret.put("testo", sb.toString());
		ret.put("squadre", squadreKey);
		ret.put("squadreCasa", squadreCasa);
		return ret;
	}
	public static String getDettaglio(Long chatId, String campionato, String squadra, String casa){
		try {
			Map<String, Return> go = Main.go(true, null, null);
			Return return1 = go.get(campionato);
			List<Squadra> squadre = return1.getSquadre();
			for (Squadra sq : squadre) {
				if (sq.getNome().equalsIgnoreCase(squadra)) {
					StringBuilder testo = new StringBuilder();
					testo
					.append("\n<b>")
					.append(sq.getNome())
					.append("</b>\n");

					for (Giocatore giocatore : sq.getTitolari()) {
						dettaglioTestoGiocatore(testo, giocatore,campionato);
					}
					testo
					.append("\n")
					.append("Giocatori con voto: ")
					.append(sq.getContaTitolari())
					.append("\n")
					.append("Media votati: ")
					.append(sq.getMediaTitolari())
					.append("\n")
					.append("Ancora da giocare: ")
					.append(sq.getContaSquadraTitolariNonGioca())
					.append("\n")
					.append("Totale: ")
					.append(sq.getTotaleTitolari())
					.append("\n")
					.append("\n");

					for (Giocatore giocatore : sq.getRiserve()) {
						dettaglioTestoGiocatore(testo, giocatore,campionato);
					}
					testo
					.append("\n")
					.append("Giocatori con voto: ")
					.append(sq.getContaRiserve())
					.append("\n")
					.append("Ancora da giocare: ")
					.append(sq.getContaSquadraRiserveNonGioca())
					.append("\n\n")
					.append("Proiezione --> <b><i>")
					.append(casa.equalsIgnoreCase("S")?sq.getProiezione()+2 + "(*)":sq.getProiezione())
					.append("</i></b>\n\n");

					if(chatId.intValue() == Constant.CHAT_ID_FANTALIVE.intValue()) {
						testo
						.append("\n")
						.append(Main.getUrlNotifica());
					}


					//					System.err.println(testo.toString());
					return testo.toString(); 
				}
			}
			return ""; 
		}
		catch (Exception e ) {
			throw new RuntimeException(e);
		}
	}

	private static String getDesRuolo(String campionato, String ruolo) {
		String testo="";
		if (campionato.toUpperCase().equalsIgnoreCase(Constant.Campionati.FANTAVIVA.name())) {
			testo=ruolo;
		}
		else {
			if(ruolo.equalsIgnoreCase("P")) testo=Constant.P;
			if(ruolo.equalsIgnoreCase("D")) testo=Constant.D;
			if(ruolo.equalsIgnoreCase("C")) testo=Constant.C;
			if(ruolo.equalsIgnoreCase("A")) testo=Constant.A;
		}
		return testo;
	}
	private static void dettaglioTestoGiocatore(StringBuilder testo, Giocatore giocatore, String campionato) {
		testo
		//		.append("\uD83C\uDC06")
		//		.append(" ")
		.append(getIconaIDGioc.get(giocatore.getIdGioc()))
		.append(" ")
		.append(conVoto(giocatore))
		.append("  ")
		.append(getDesRuolo(campionato, giocatore.getRuolo()))
		.append("  ")
		.append("<b>")
		.append(giocatore.getNome())
		.append("</b>")
		.append("  ")
		.append(giocatore.getSquadra())
		.append("  ")
		.append(getVoto(giocatore))
		.append("  ")
		.append("<b>")
		.append(getFantaVoto(giocatore))
		.append("</b>");
		if (giocatore.isCambiato()) {
			testo.append("(*)");
		}
		testo
		//		.append("\n")
		.append(getOrario(giocatore.getOrario()))
		.append("\n");

		for (Integer evento : giocatore.getCodEventi()) {
			testo
			.append(desEvento(evento,campionato))
			.append("  ");
		}
		testo
		.append("\n");
	}
	private static String getVoto(Giocatore g) {
		if (!g.isSquadraGioca()) return " ";
		if (g.getVoto()==0) return "NV";
		return String.valueOf(g.getVoto());
	}
	private static String getFantaVoto(Giocatore g) {
		if (!g.isSquadraGioca()) return " ";
		if (g.getVoto()==0) return "NV";
		return String.valueOf(g.getVoto()+g.getModificatore());
	}
	private static String getOrario(Map<String,String> orario){
		String tag = orario.get("tag");
		if (tag.equals("FullTime") || tag.equals("Postponed") || tag.equals("Cancelled") || tag.equals("Walkover")) return " " + tag + Constant.DEFINITIVA + " ";;
		if (tag.equals("PreMatch")){
			String ret="";
			ret = ret + orario.get("val").substring(8,10);
			ret = ret + "/" + orario.get("val").substring(5,7);
			ret = ret + " " + (1+Integer.parseInt(orario.get("val").substring(11,13)));
			ret = ret + ":" + orario.get("val").substring(14,16);
			return ret;
			//			return " " + ret + Constant.SCHEDULATA + " ";
		}
		return " " + orario.get("val") + Constant.OROLOGIO + " ";
	}

	private static boolean chkPartitaFinita(Giocatore giocatore){
		if (giocatore.getOrario().get("tag").equals("FullTime")) return true;
		if (giocatore.getCodEventi().contains(14)) return true;
		return false;

	}
	private static String partitaFinita(Giocatore giocatore){
		if (chkPartitaFinita(giocatore))  return Constant.PARTITA_FINITA;
		return Constant.PARTITA_NON_FINITA;
	}
	private static String conVoto(Giocatore giocatore){
		if (giocatore.getVoto()==0) {
			if (giocatore.isSquadraGioca() && chkPartitaFinita(giocatore) ) {
				return Constant.NO_VOTO_FINITO;
			}
			else if (!giocatore.isSquadraGioca()) {
				return Constant.NO_VOTO_IN_CORSO;
			}
			else {
				return Constant.NO_VOTO_DA_INIZIARE;
			}
		}
		return Constant.OK_VOTO;
	}
	private static String desEvento(Integer ev,String r){
		String[] evento = Main.eventi.get(ev);
		String iconaEvento = evento[5];
		String ret = " " + iconaEvento + " " + evento[0];
		String valEvento = valEvento(evento,r);
		if (!"0".equals(valEvento)) {
			ret = ret + " (" + valEvento + ") "; 
		}
		return ret;
	}
	private static String valEvento(String[] evento,String r){
		int pos=0;
		if (r.equals(Constant.Campionati.FANTAVIVA.name())) pos=1;
		if (r.equals(Constant.Campionati.LUCCICAR.name())) pos=2;
		if (r.equals(Constant.Campionati.BE.name())) pos=3;
		if (r.equals(Constant.Campionati.JB.name())) pos=4;
		return evento[pos];
	}

	public static Set<String> getElencoGiocatori(String filtro) throws Exception {
		Set<String> ret=new TreeSet<>();
		Set<String> retUC=new TreeSet<>();
		Map<String, Return> go = Main.go(false,null, null);
		for (String campionato : go.keySet()) {
			Return return1 = go.get(campionato);
			List<Squadra> squadre = return1.getSquadre();
			String tipo="FANTAGAZZETTA";
			if (return1.getCampionato().equalsIgnoreCase(Constant.Campionati.BE.toString())) {
				tipo="FANTASERVICE";
			}
			for (Squadra squadra : squadre) {
				for (Giocatore giocatore : squadra.getTitolari()) {
					String filtroGiocatore = getFiltroGiocatore(tipo, giocatore);
					if (filtroGiocatore.toUpperCase().contains(filtro.toUpperCase()) && !retUC.contains(filtroGiocatore.toUpperCase())) {
						retUC.add(filtroGiocatore.toUpperCase());
						ret.add(filtroGiocatore);
					}
				}
				for (Giocatore giocatore : squadra.getRiserve()) {
					String filtroGiocatore = getFiltroGiocatore(tipo, giocatore);
					if (filtroGiocatore.toUpperCase().contains(filtro.toUpperCase()) && !retUC.contains(filtroGiocatore.toUpperCase())) {
						retUC.add(filtroGiocatore.toUpperCase());
						retUC.add(filtroGiocatore);
						ret.add(filtroGiocatore);
					}
				}
			}
		}
		return ret;
	}

	private static String getFiltroGiocatore(String tipo, Giocatore giocatore) {
		String nomeRet="";
		if (tipo.equals("FANTAGAZZETTA")) {
			nomeRet=giocatore.getNomeTrim();
		}
		else {
			nomeRet=giocatore.getNome().substring(0,giocatore.getNome().lastIndexOf(" ")).replaceAll(" ", "");
		}
		return nomeRet + " - " + giocatore.getSquadra().toUpperCase();
	}

	public static String getDettaglioGiocatore(String filtro) throws Exception {
		Map<String, Return> go = Main.go(true,null, null);
		Giocatore gioc=null;
		List<Map<String,String>> dati=new ArrayList<>();
		for (String campionato : go.keySet()) {
			Return return1 = go.get(campionato);
			List<Squadra> squadre = return1.getSquadre();
			String tipo="FANTAGAZZETTA";
			if (return1.getCampionato().equalsIgnoreCase(Constant.Campionati.BE.toString())) {
				tipo="FANTASERVICE";
			}
			for (Squadra squadra : squadre) {
				for (Giocatore giocatore : squadra.getTitolari()) {
					String filtroGiocatore = getFiltroGiocatore(tipo, giocatore);
					if (filtroGiocatore.equalsIgnoreCase(filtro.toUpperCase())) {
						Map<String, String> dato=new HashMap<>();
						dato.put("riga", campionato + " " + squadra.getNome() + " " + giocatore.getIdGioc());
						dati.add(dato);
						gioc=giocatore;
					}
				}
				for (Giocatore giocatore : squadra.getRiserve()) {
					String filtroGiocatore = getFiltroGiocatore(tipo, giocatore);
					if (filtroGiocatore.equalsIgnoreCase(filtro.toUpperCase())) {
						Map<String, String> dato=new HashMap<>();
						dato.put("riga", campionato + " " + squadra.getNome() + " " + giocatore.getIdGioc());
						dati.add(dato);
						gioc=giocatore;
					}
				}
			}
		}
		String ret=gioc.getNome() + "(" + gioc.getRuolo() + ") " + gioc.getSquadra() + "[";
		for (Integer ev : gioc.getCodEventi()) {
			ret =ret + " " + eventi.get(ev)[0];
		}
		ret=ret+ "] voto=" + gioc.getVoto()  
		+ " modificatore=" + gioc.getModificatore() + ", FM=" + (gioc.getModificatore() + gioc.getVoto())
		+ " orario=" + gioc.getOrario() + "\n"; 		
		for (Map<String, String> dato : dati) {
			ret = ret + "\n" + dato.get("riga");
		}
		return ret;
	}


	public static Set<String> getpartiteSimulate(String campionato) throws Exception {
		Set<String> ret=new TreeSet<>();
		Map<String, Return> go = Main.go(false,null, null);
		Return return1 = go.get(campionato);
		List<Squadra> squadre = return1.getSquadre();
		for (Squadra squadra : squadre) {
			List<PartitaSimulata> partiteSimulate = squadra.getPartiteSimulate();
			for (PartitaSimulata partitaSimulata : partiteSimulate) {
				ret.add(partitaSimulata.getNome());
			}
		}
		return ret;
	}
	public static Map<String, Object> proiezioni(String campionato) throws Exception {
		Map<String, Object> ret = new HashMap<>();
		List<String> squadreRet=new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		Map<String, Return> go = go(true, null, null);
		Set<String> keySet = go.keySet();
		for (String key : keySet) {
			if (campionato.equals("ALL") || campionato.equals(key)) {
				sb
				.append("\n<b>")
				.append(key)
				.append("</b>\n");
				Return return1 = go.get(key);
				List<Squadra> squadre = return1.getSquadre();
				for (Squadra squadra : squadre) {
					if (squadra.isEvidenza()) {
						sb.append(proiezioneSquadra(squadra,squadra.isCasaProiezione()));
						squadreRet.add(key + "-" + squadra.getNome());
					}

				}
			}
		}
		ret.put("testo", sb.toString());
		ret.put("squadre", squadreRet);
		return ret;
	}

	private static StringBuilder proiezioneSquadra(Squadra squadra, boolean casa) {
		int iCasa=0;
		if (casa) iCasa=2;
		StringBuilder sb=new StringBuilder();
		sb
		.append("\n<b>")
		.append(squadra.getNome())
		.append("</b>\n")
		.append("Titolari")
		.append("\n")
		.append("Giocatori con voto: ")
		.append(squadra.getContaTitolari())
		.append("\n")
		.append("Media votati: ")
		.append(squadra.getMediaTitolari())
		.append("\n")
		.append("Ancora da giocare: ")
		.append(squadra.getContaSquadraTitolariNonGioca())
		.append("\n")
		.append("Totale: ")
		.append(squadra.getTotaleTitolari())
		.append("\n")
		.append("\n")
		.append("Riserve")
		.append("\n")
		.append("Giocatori con voto: ")
		.append(squadra.getContaRiserve())
		.append("\n")
		.append("Ancora da giocare: ")
		.append(squadra.getContaSquadraRiserveNonGioca())
		.append("\n\n")
		.append("Proiezione --> <b><i>")
		.append(squadra.getProiezione()+iCasa)
		.append("</i></b>\n\n");
		return sb;
	}


}
