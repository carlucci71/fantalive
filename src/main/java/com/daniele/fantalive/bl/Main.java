package com.daniele.fantalive.bl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
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

import javax.net.ssl.SSLHandshakeException;

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
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import com.daniele.MainClass;
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
import com.daniele.fantalive.util.Constant.Campionati;
import com.daniele.fantalive.util.ThreadSeparato;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Main {

	public static Map<String,Object> toSocket;
	public static String MIO_IP;
	public static FantaLiveBOT fantaLiveBot;
	public static FantaCronacaLiveBOT fantaCronacaLiveBot;
	public static RisultatiConRitardoBOT risultatiConRitardoBOT;
	public static Map<Integer, String> sq=null;
	public static Map<String, Integer> sqStatusMatch=new HashMap<>();
	public static Map<Integer, String[]> eventi=null;
	public static Map<String, Integer> fontiVoti=new HashMap<>();
	public static HashMap<String, Object> modificatori=new HashMap<>();
	private static List<ConfigCampionato> configsCampionato=null;
	public static List<String> sqDaEv= null;
	private static Map<String, Giocatore> oldSnapshot=null;
	private static SalvaRepository salvaRepository=null;

	private static SocketHandlerFantalive socketHandlerFantalive=null;
	private static ScheduledExecutorService executor = null;	
	private static Constant constant=null;
	public static List<Live> oldSnapLives=null;
	public static Map<String, Map<String, String>> oldSnapOrari=null;
	static ObjectMapper mapper;
	static Map<String , String> getIconaIDGioc;	
	static Map<String , Integer> statusMatch;	
	static Map<String , String> nickPlayer;	
	static Map<String , String> reverseNickPlayer;	
	static Map<Integer, String > reverseStatusMatch;	
	public static Map<Integer , ZonedDateTime> calendario;	
	public static Map<Integer , ZonedDateTime> calendarioInizioGiornata;	
	static Set<String > sqJB;	
	static Set<String > giocJB;	
	public static Map<String, String> keyFG=null;
	public static int timeRefresh = 0;

	public static Logger logger = Logger.getLogger(Main.class);



	public static void init(SalvaRepository salvaRepositorySpring, SocketHandlerFantalive socketHandlerSpring, Constant constantSpring, boolean valorizzaBMFG) throws Exception {
		executor = Executors.newSingleThreadScheduledExecutor();	
		salvaRepository=salvaRepositorySpring;
		socketHandlerFantalive=socketHandlerSpring;
		constant=constantSpring;
		constant.AUTH_FS=getAuthFS();
		if (calendario==null) {
			calendario = new LinkedHashMap();
			calendarioInizioGiornata = new LinkedHashMap<>();
			String http = (String) callHTTP("GET", "application/json; charset=UTF-8", String.format(Constant.URL_CALENDARIO), null).get("response");
			//			System.out.println(http);
			//			https://www.tomshw.it/culturapop/calendario-serie-a-2021-22-risultati-e-dove-vedere-le-partite/		
			//			System.out.println(http);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss Z");
			Document doc = Jsoup.parse(http);//, StandardCharsets.UTF_8.toString()
			Elements elements = doc.getElementsByClass("table-container-scroll");
			for (int i=0;i<elements.size();i++) {
				Element element = elements.get(i);
				Elements elementsTR = element.getElementsByTag("TR");
				String giornata = elementsTR.get(0).text();
				if (giornata.trim().equals("")) continue;
				String ultimoGiorno="";
				String ultimoMese="";
				int iGiornata;
				try {
					iGiornata = Integer.parseInt(giornata.substring(0,giornata.indexOf("ª")));
				}
				catch (Exception e ) {
					iGiornata = Integer.parseInt(giornata.substring(0,giornata.indexOf("ª")-1));
				}
				String primoGiorno=null;
				String primoMese=null;
				for (int ix=1;ix<elementsTR.size();ix++) {
					Elements elementsTD = elementsTR.get(ix).getElementsByTag("TD");
					if (elementsTD.size()<3) continue;
					ultimoGiorno = elementsTD.get(0).text();
					ultimoGiorno = lpad(ultimoGiorno.substring(0,ultimoGiorno.indexOf("/")),2,'0');
					ultimoMese = elementsTD.get(0).text();
					ultimoMese = lpad(ultimoMese.substring(ultimoMese.indexOf("/")+1),2,'0');
					if (primoGiorno == null) {
						primoGiorno=ultimoGiorno;
						primoMese=ultimoMese;
					}
				}
				String ultimoAnno="2021";
				if (Integer.parseInt(ultimoMese)<8) ultimoAnno = "2022";
				ZonedDateTime parseZDT = ZonedDateTime.parse(ultimoGiorno + "/" + ultimoMese + "/" + ultimoAnno + " - 23:59:00 +0000", dtf);
				calendario.put(iGiornata, parseZDT);
				String primoAnno="2021";
				if (Integer.parseInt(primoMese)<8) primoAnno = "2022";
				parseZDT = ZonedDateTime.parse(primoGiorno + "/" + primoMese + "/" + primoAnno + " - 23:59:00 +0000", dtf);
				calendarioInizioGiornata.put(iGiornata, parseZDT);
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
			if (constant.GIORNATA_FORZATA!=null) {
				constant.GIORNATA=constant.GIORNATA_FORZATA;
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
			//			mapper = new ObjectMapper();
			mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
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
		if (nickPlayer==null) {
			nickPlayer = new HashMap() {{
				put("LUCCICAR-daddy","IO");
				put("LUCCICAR-Team Frank..10","fra");
				put("LUCCICAR-Team Alberto..04","bebo");
				put("FANTAVIVA-AFC Richmond","Angy");
				put("FANTAVIVA-Real Colizzati","Dum");
				put("FANTAVIVA-Tavolino","IO");
				put("FANTAVIVA-Reset Fc","Vale");
				put("FANTAVIVA-AS Sersale 1975","Ben");
				put("FANTAVIVA-Longobarda","Ale");
				put("FANTAVIVA-A.C. Avanti Cristo","KK");
				put("FANTAVIVA-ASC Melizzano","Dario");
				put("FANTAVIVA-perqualcheciacciinpiu","Gigi");
				put("FANTAVIVA-Ciulone","Andrea");
				put("BE-Atletico Mikatanto","Dante");
				put("BE-VincereAManiBasse","Roby");
				put("BE-Universal","Claudio");
				put("BE-Atletico Conc","Conc");
				put("BE-Jonny Fighters","Gio");
				put("BE-C. H. MOLLE","Fra");
				put("BE-tavolino","IO");
				put("BE-Canosa di Puglia...","New");
			}};
			reverseNickPlayer = new HashMap();
			for(Map.Entry<String, String> entry : nickPlayer.entrySet()){
				reverseNickPlayer.put(entry.getValue(), entry.getKey());
			}		
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
				put("Notplayed",    8);
			}};
		}
		if (reverseStatusMatch==null) {
			/*
			reverseStatusMatch = new HashMap() {{
				put(0,"PreMatch");
				put(1,"FirstHalf");
				put(2,"HalfTime");
				put(3,"SecondHalf");
				put(4,"FullTime");
				put(5,"Postponed");
				put(6,"Cancelled");
				put(7,"Walkover");
				put(8,"Notplayed");
			}};
			 */
			reverseStatusMatch = new HashMap();
			for(Map.Entry<String, Integer> entry : statusMatch.entrySet()){
				reverseStatusMatch.put(entry.getValue(), entry.getKey());
			}		
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
			/*
			 	descrizione
				FANTAVIVA
				LUCCICAR
				BE
				JB
				attivo
				icona
				des_FG
			 */



			eventi = new HashMap<Integer, String[]>();
			eventi.put(1000, new String[] {"portiere imbattuto","","","1","","S",Constant.IMBATTUTO,"portiere_imbattuto"});
			eventi.put(1, new String[] {"ammonito","","","-0.5","","S",Constant.AMMONITO,"ammonizione"});
			eventi.put(2, new String[] {"espulso","","","-1","","S",Constant.ESPULSO,"espulsione"});
			eventi.put(3, new String[] {"gol","","","3","","S",Constant.GOL,"gol_segnato"});
			eventi.put(4, new String[] {"gol subito","","","-1","","S",Constant.GOL_SUBITO,"gol_subito"});
			eventi.put(7, new String[] {"rigore parato","","","3","","S",Constant.RIGORE_PARATO,"rigore_parato"});
			eventi.put(8, new String[] {"rigore sbagliato","","","-3","","S",Constant.RIGORE_SBAGLIATO,"rigore_sbagliato"});
			eventi.put(9, new String[] {"rigore segnato","","","2","","S",Constant.RIGORE_SEGNATO,"rigore_segnato"});
			eventi.put(10, new String[] {"autogol","","","-3","","S",Constant.AUTOGOL,"autogol"});
			eventi.put(11, new String[] {"gol vittoria","","","0","","N",Constant.GOL,"gol_decisivo_vittoria"});
			eventi.put(12, new String[] {"gol pareggio","","","0","","N",Constant.GOL,"gol_decisivo_pareggio"});
			eventi.put(14, new String[] {"uscito","","","0","","S",Constant.USCITO,""});
			eventi.put(15, new String[] {"entrato","","","0","","S",Constant.ENTRATO,""});
			eventi.put(16, new String[] {"gol annullato","","","0","","N",Constant.GOL_ANNULLATO,""});
			eventi.put(17, new String[] {"infortunio","","","0","","N",Constant.INFORTUNIO,""});
			eventi.put(20, new String[] {"assist involontario","","","1","","S",Constant.ASSIST,"assist_inv"});
			eventi.put(21, new String[] {"assist soft","","","1","","S",Constant.ASSIST,"assist_soft"});
			eventi.put(22, new String[] {"assist","","","1","","S",Constant.ASSIST,"assist"});
			eventi.put(23, new String[] {"assist_gold","","","1","","S",Constant.ASSIST,"assist_gold"});
			eventi.put(24, new String[] {"assist movimento livello medio","","","1","","S",Constant.ASSIST,"assistMovimentoLvMedio"});
			eventi.put(25, new String[] {"assist movimento livello alto","","","1","","S",Constant.ASSIST,""});
			if (valorizzaBMFG) {
				Map<String, Object> bmFantaviva = (Map<String, Object>) bm_FG(Main.aliasCampionati.get(Constant.Campionati.FANTAVIVA.name()));
				Map<String, Object> bmJB = (Map<String, Object>) bm_FG(Main.aliasCampionati.get(Constant.Campionati.JB.name()));
				Map<String, Object> bmLuccicar = (Map<String, Object>) bm_FG(Main.aliasCampionati.get(Constant.Campionati.LUCCICAR.name()));
				for (Integer key : eventi.keySet()) {
					String[] valori = eventi.get(key);
					String kFg=valori[7];
					fontiVoti.put(aliasCampionati.get(Campionati.FANTAVIVA.name()), (Integer)((Map)bmFantaviva.get("fonte")).get("fonte_voti"));
					fontiVoti.put(aliasCampionati.get(Campionati.LUCCICAR.name()), (Integer)((Map)bmLuccicar.get("fonte")).get("fonte_voti"));
					fontiVoti.put(aliasCampionati.get(Campionati.JB.name()), (Integer)((Map)bmJB.get("fonte")).get("fonte_voti"));

					overrideBM(bmFantaviva, valori, kFg,1);
					overrideBM(bmLuccicar, valori, kFg,2);
					overrideBM(bmJB, valori, kFg,4);
				}
				modificatori.put(Constant.Campionati.FANTAVIVA.name(), ((Map)bmFantaviva.get("modificatori")));
				modificatori.put(Constant.Campionati.JB.name(), ((Map)bmJB.get("modificatori")));
				modificatori.put(Constant.Campionati.LUCCICAR.name(), ((Map)bmLuccicar.get("modificatori")));
			}
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
			configsCampionato.add(new ConfigCampionato(24,"FANTAGAZZETTA",Constant.Campionati.JB.name(),"NOMANTRA","TUTTI"));
			configsCampionato.add(new ConfigCampionato(24,"FANTAGAZZETTA",Constant.Campionati.LUCCICAR.name(),"NOMANTRA","F1"));
			configsCampionato.add(new ConfigCampionato(22,"FANTAGAZZETTA",Constant.Campionati.FANTAVIVA.name(),"MANTRA","PARTITE"));
			configsCampionato.add(new ConfigCampionato(22,"FANTASERVICE",Constant.Campionati.BE.name(),"NOMANTRA","PARTITE"));
		}
	}

	private static void overrideBM(Map<String, Object> bm, String[] valori, String kFg, int posizione) {
		bm=(Map<String, Object>) bm.get("bonus_malus");
		Double newval=null;
		if (!kFg.equals("")) {
			Object object = bm.get(kFg);
			if (object instanceof List) {
				newval=(Double) ((List)object).get(0);
			} else {
				newval = (Double) object;
			}
		}
		if (newval==null) {
			newval=new Double(0);
		}
		valori[posizione]=Double.toString(newval);
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
												desMiniNotifica.append(desEvento(attSnapEvento, Constant.Campionati.BE.name()) + " ");//FIXME CAMPIONATO FISSO
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
				upsertSalva(time + "-" + "snapPartite", toJson(getLives.get("snapPartite")));//snapOrari
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
					Main.inviaCronacaNotifica(des.toString(), 15);
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
				Main.inviaCronacaNotifica(desMiniNotifica.toString(),15);
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


	private static void mainSpring(String[] args) throws Exception {
		ConfigurableApplicationContext ctx;
		ctx = new SpringApplicationBuilder(MainClass.class)
				.profiles("DEV")
				.web(true).run(args);
		Main.init(ctx.getBean(SalvaRepository.class),null,ctx.getBean(Constant.class), false);
		/*******/
		//....
		/*******/
		ctx.stop();
		ctx.close();
	}

	private static void mainBatch(String[] args) throws Exception {
		Constant c=null;
		Class<?> cl = Class.forName("com.daniele.fantalive.util.ConstantDevelop");
		Method method = cl.getDeclaredMethod("constant");
		c = (Constant) method.invoke(c);		
		init(null, null, c, false);
		//		Map<Integer, Float> votiAlvin = getVotiAlvin(24);
		//		System.out.println(votiAlvin);
		System.out.println(getVotiFS(24, false));

	}
	public static  String getNomeFromFG(String nomeFS, Set<String> nomiFG) {
		String ret=null;
		ret = cercaNomeFs(nomeFS, nomiFG, ret);
		if (ret==null) {
			ret = cercaNomeFs(nomeFS.concat(" "), nomiFG, ret);
		}
		return ret;
	}

	private static String cercaNomeFs(String nomeFS, Set<String> nomiFG, String ret) {
		for (String nomeFG : nomiFG) {
			String[] split = nomeFG.split("@");
			//			System.out.println(split[0].substring(0,split[0].lastIndexOf(" ")).replaceAll(" ", "")); //.equalsIgnoreCase(nomeGiocatoreLive.replaceAll(" ", "")))	
			String nomeFindFS = nomeFS.substring(0,nomeFS.lastIndexOf(" ")).replaceAll(" ", "");
			String nomeFindFG = split[0].replaceAll(" ", "");
//			if (nomeFS.toUpperCase().indexOf("APAT")>-1 && nomeFindFG.toUpperCase().indexOf("APAT")>-1) {
//				System.out.println();
//			}
			if (nomeFindFS.equalsIgnoreCase(nomeFindFG))
			{
				ret = nomeFG;
			}
		}
		return ret;
	}
	private static List<Squadra> getVotiFS(int giornata, boolean rileggi) throws Exception {
		/*
		Map<Integer, Double> ret = new HashMap<>();
		Set<String> nomiFG=new HashSet<>();
		Map<String, Integer> idFG=new HashMap<>();
		Iterator<Integer> iterator = Main.sq.keySet().iterator();
		while (iterator.hasNext()) {
			Integer integer = (Integer) iterator.next();
			String sqFromLive = (String) Main.callHTTP("GET", "application/json; charset=UTF-8", String.format(Constant.URL_LIVE_FG,integer, giornata, Constant.I_LIVE_FANTACALCIO), null).get("response");
			List<Map<String, Object>> jsonToList = Main.jsonToList(sqFromLive);
			for (Map<String, Object> map : jsonToList) {
				String nome = (String) map.get("nome");
				nomiFG.add(nome);
				idFG.put(nome,  (Integer) map.get("id"));
			}
		}
		 */
		String tokenNomeFile = "LIVE_" + giornata  + "_";
		if (rileggi) {
			Main.scaricaBe(giornata,tokenNomeFile);
		}
		List<Squadra> squadre = Main.getSquadreFromFS(tokenNomeFile,false, true);
		/*
		for (Squadra squadra : squadre) {
			for (Giocatore giocatore : squadra.getTitolari()) {
				String nomeFromFG = getNomeFromFG(giocatore.getNome(), nomiFG);
				if (giocatore.getVoto() != 0) {
					ret.put(idFG.get(nomeFromFG), giocatore.getVoto());
				}
			}
			for (Giocatore giocatore : squadra.getRiserve()) {
				String nomeFromFG = getNomeFromFG(giocatore.getNome(), nomiFG);
				if (giocatore.getVoto() != 0) {
					ret.put(idFG.get(nomeFromFG), giocatore.getVoto());
				}
			}
		}
		return ret;
		 */
		return squadre;
		/*
		Map<Integer, Float> votiAlvin = getVotiAlvin(Constant.GIORNATA);
		for (Squadra squadra : squadre) {
			for (Giocatore giocatore : squadra.getTitolari()) {
				Float votoAlvin = votiAlvin.get(Integer.parseInt(giocatore.getId()));
				if (votoAlvin != null) {
					giocatore.setVoto(Double.valueOf(Float.valueOf(votoAlvin).toString()).doubleValue());
				}
			}
			for (Giocatore giocatore : squadra.getRiserve()) {
				Float votoAlvin = votiAlvin.get(giocatore.getId());
				if (votoAlvin != null) {
					giocatore.setVoto(Double.valueOf(Float.valueOf(votoAlvin).toString()).doubleValue());
				}
			}
		}
		 */	


	}

	private static Map<Integer, Float> getVotiAlvin(int giornata) throws Exception {
		Map<Integer, Float> ret = new HashMap<>();
		String http = (String) callHTTP("GET", "application/json; charset=UTF-8", "https://www.fantacalcio.it/voti-fantacalcio-serie-a/2021-22/" + giornata, null).get("response");
		Document doc = Jsoup.parse(http);//, StandardCharsets.UTF_8.toString()
		Elements elements = doc.getElementsByAttribute("role");
		for (int i=0;i<elements.size();i++) {
			Element elementTR = elements.get(i);
			String role=elementTR.attr("role");
			Integer id=null;
			Float voto=null;
			if (role.equalsIgnoreCase("row")) {
				List<Node> childNodes = elementTR.childNodes();
				for (Node node : childNodes) {
					if ("td".equalsIgnoreCase(node.nodeName())) {
						List<Node> childTRNodes = node.childNodes();
						for (Node nodeTR : childTRNodes) {
							if ("a".equalsIgnoreCase(nodeTR.nodeName())) {
								id = Integer.parseInt(nodeTR.attr("href").split("/")[6]);
							}
							else {
								boolean isRel=false;
								boolean isDataSource2=false;
								List<Attribute> asList = nodeTR.parentNode().attributes().asList();
								for (Attribute attribute : asList) {
									if ("data-source".equalsIgnoreCase(attribute.getKey()) && "2".equalsIgnoreCase(attribute.getValue())) {
										isDataSource2=true;
									}
									if ("class".equalsIgnoreCase(attribute.getKey()) && "rel".equalsIgnoreCase(attribute.getValue())) {
										isRel=true;
									}
								}
								if (isDataSource2 && isRel && nodeTR.childNodeSize()>0) {
									try
									{
										voto=Float.parseFloat(nodeTR.childNode(0).toString().replace(",", "."));
									}
									catch (Exception e)
									{

									}
								}
							}
						}
					}
				}
			}
			if (voto != null) {
				ret.put(id, voto);
			}
		}
		return ret;
	}
	public static void main(String[] args) throws Exception {
		//				mainSpring(args);
		mainBatch(args);


		/*
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss Z");
		ZonedDateTime parse = ZonedDateTime.parse("07/11/2021 - 23:59:00 +0000", dtf);
		System.out.println(parse);


		init(salvaRepositorySpring, socketHandlerSpring, constantSpring);
		String authFS = getAuthFS();
		System.out.println(authFS);
		 */
	}

	public static Map<String, Object> proiezioneFG(String lega, List<Squadra> squadre, String sfide, String squadraSimulata) throws Exception{
		Map<String, Object> jsonToMap;
		Map bodyMap = new HashMap<>();
		bodyMap.put("username", constant.UTENTE_FG);
		bodyMap.put("password", constant.PWD_FG);
		Map<String, String> headers=new HashMap<>();
		headers.put("app_key", constant.APPKEY_FG_MOBILE);
		Map<String, Object> postHTTP = callHTTP("POST", "application/json; charset=UTF-8",String.format(Constant.URL_LOGIN_APP_FG),toJson(bodyMap), headers);
		String response = (String) postHTTP.get("response");
		Map<String, Object> mapResponse = jsonToMap(response);//dati del login
		Map data = (Map) mapResponse.get("data");
		Map utente = (Map) data.get("utente");			
		String user_token = (String) utente.get("utente_token");
		List<Map> leghe = (List<Map>) data.get("leghe");			
		String lega_token = "";
		//		int id_squadra=0;
		for (Map legaAtt : leghe) {
			if (((String)legaAtt.get("alias")).equalsIgnoreCase(lega)) {
				lega_token = (String) legaAtt.get("token");
				//				id_squadra = (int) legaAtt.get("id_squadra");

			}
		}

		String idComp="";
		if (lega.equalsIgnoreCase(aliasCampionati.get(Constant.Campionati.JB.name()))) {
			idComp = Constant.COMP_JB_FG;
		} else if (lega.equalsIgnoreCase(aliasCampionati.get(Constant.Campionati.FANTAVIVA.name()))) {
			idComp = Constant.COMP_VIVA_FG;
		} else if (lega.equalsIgnoreCase(aliasCampionati.get(Constant.Campionati.LUCCICAR.name()))) {
			idComp = Constant.COMP_LUCCICAR_FG;
		} else {
			throw new RuntimeException("lega non configurata: " + lega);
		}

		Map<String, Object> ret = new HashMap<>();
		if (fontiVoti.get(lega) == 3) {
			Map<Integer, Float> votiAlvin = getVotiAlvin(Constant.GIORNATA);
			for (Squadra squadra : squadre) {
				for (Giocatore giocatore : squadra.getTitolari()) {
					Float votoAlvin = votiAlvin.get(Integer.parseInt(giocatore.getId()));
					if (votoAlvin != null) {
						giocatore.setVoto(Double.valueOf(Float.valueOf(votoAlvin).toString()).doubleValue());
					}
				}
				for (Giocatore giocatore : squadra.getRiserve()) {
					Float votoAlvin = votiAlvin.get(giocatore.getId());
					if (votoAlvin != null) {
						giocatore.setVoto(Double.valueOf(Float.valueOf(votoAlvin).toString()).doubleValue());
					}
				}
			}
		}


		if (sfide.equalsIgnoreCase("PARTITE")) {
			ret = callProiezioneFG(squadre, user_token, lega_token, idComp, squadraSimulata);
		} else {
			for (Squadra squadra : squadre) {
				List<Squadra> singola = new ArrayList<>();
				singola.add(squadra);
				mapResponse = callProiezioneFG(singola, user_token, lega_token, idComp, squadraSimulata);
				ret.put("state", mapResponse.get("state"));
				ret.put("success", mapResponse.get("success"));
				ret.put("error_msgs", mapResponse.get("error_msgs"));
				ret.put("token", mapResponse.get("token"));
				ret.put("update", mapResponse.get("update"));
				Map<String, Object> mapDataResponse = (Map<String, Object>) mapResponse.get("data");
				Map<String, Object> mapDataRet = (Map<String, Object>) ret.get("data");
				if (mapDataRet==null) {
					mapDataRet = new HashMap<>();
				}
				mapDataRet.put("ris", mapDataResponse.get("ris"));
				mapDataRet.put("s", mapDataResponse.get("s"));
				mapDataRet.put("adv", mapDataResponse.get("adv"));
				mapDataRet.put("msg", mapDataResponse.get("msg"));

				List<Map<String, Object>> listDataTeamsResponse = (List<Map<String, Object>>) mapDataResponse.get("teams");
				List<Map<String, Object>> listDataTeamsRet = (List<Map<String, Object>>) mapDataRet.get("teams");
				if (listDataTeamsRet==null) {
					listDataTeamsRet = new ArrayList<>();
				}
				for (Map<String,Object> map : listDataTeamsResponse) {
					listDataTeamsRet.add(map);
				}
				mapDataRet.put("teams", listDataTeamsRet);
				ret.put("data", mapDataRet);
				//				System.out.println(toJson(ret));
			}
		}
		//System.out.println(toJson(ret));
		Instant instant = Instant.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
		ZoneId zoneId = ZoneId.of( "Europe/Rome" );
		ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
		String time=zdt.format(formatter);
		String sq="";
		for (Squadra squadra : squadre) {
			sq=sq+";" + squadra.getNome();
		}
		String x="-" + "simulaFG" + "-" + sfide + "-" + lega + "-" + sq + "-" + (squadraSimulata==null?"":squadraSimulata);
		upsertSalva(time + x, toJson(ret));
		return ret;
	}
	
	private static Map<String, Object> callProiezioneFG(List<Squadra> squadre, String user_token, String lega_token, String idComp, String squadraSimulata)
			throws Exception {
		Map<String, String> headers;
		Map<String, Object> postHTTP;
		String response;
		Map<String, Object> mapResponse;
		Map mapBody=new HashMap<>();
		List<Map> teams = new ArrayList<>();
		for (Squadra sq : squadre) {
			teams.add(generaTeamsSquadra(sq, teams));
		}
		mapBody.put("ris", "");
		mapBody.put("teams", teams);


		headers=new HashMap<>();
		headers.put("app_key", constant.APPKEY_FG_MOBILE);
		headers.put("lega_token", lega_token);
		headers.put("user_token", user_token);
		String giornata = String.valueOf(Constant.GIORNATA - Constant.DELTA_VIVA_FG);
		postHTTP = callHTTP("POST","application/json; charset=UTF-8",String.format(Constant.URL_PROIEZIONI_FG, idComp,giornata),toJson(mapBody), headers);
		response = (String) postHTTP.get("response");
		mapResponse = jsonToMap(response);//System.out.println(toJson(mapBody));
		List listErrorMessage = (List)mapResponse.get("error_msgs");
		if (listErrorMessage != null) {
			throw new RuntimeException(listErrorMessage.get(0).toString());
		}
		teams = (List<Map>) ((Map<String, Object>)mapResponse.get("data")).get("teams");
		List<Map> teamsRet = new ArrayList<>();
		for (int i=0;i<squadre.size();i++) {
			Squadra squadra = squadre.get(i);
			if (squadraSimulata == null || squadra.getNome().equals(squadraSimulata)) {
				Map<String, Object> map = teams.get(i);
				map.put("nome", squadra.getNome());
				List<Map<String, Object>> pl = (List<Map<String, Object>>) map.get("players");
				for (Map<String, Object> mapPl : pl) {
					ripristinaInfo(mapPl, squadra);
				}
				teamsRet.add(map);
			}
		}
		((Map<String, Object>)mapResponse.get("data")).put("teams", teamsRet);
		return mapResponse;
	}

	private static void ripristinaInfo(Map<String, Object> map, Squadra sq) {
		List<Giocatore> titolari = new ArrayList<Giocatore>();
		List<Integer> codEventi=null;
		Double fantavoto=null;
		Double voto=null;
		Double rank=null;
		String nome=null;
		Boolean squadraGioca=null;
		Boolean capitano=false;
		Boolean viceCapitano=false;
		for (Giocatore giocatore2 : sq.getTitolari()) {
			if (giocatore2.getId().equals(map.get("id").toString())){
				if (giocatore2.isCapitano()) {
					capitano=true;
				}
				if (giocatore2.isViceCapitano()) {
					viceCapitano=true;
				}
				codEventi=giocatore2.getCodEventi();
				rank=giocatore2.getVoto();
				nome=giocatore2.getNome();
				if (giocatore2.isSquadraGioca()) {
					fantavoto=giocatore2.getVoto() + giocatore2.getModificatore();
					voto=giocatore2.getVoto();
				} else {
					fantavoto=6d;
					voto=6d;
				}
				squadraGioca = giocatore2.isSquadraGioca();
			}
		}
		for (Giocatore giocatore2 : sq.getRiserve()) {
			if (giocatore2.getId().equals(map.get("id").toString())){
				if (giocatore2.isCapitano()) {
					capitano=true;
				}
				if (giocatore2.isViceCapitano()) {
					viceCapitano=true;
				}
				codEventi=giocatore2.getCodEventi();
				rank=giocatore2.getVoto();
				nome=giocatore2.getNome();
				if (giocatore2.isSquadraGioca()) {
					fantavoto=giocatore2.getVoto() + giocatore2.getModificatore();
					voto=giocatore2.getVoto();
				} else {
					fantavoto=6d;
					voto=6d;
				}
				squadraGioca = giocatore2.isSquadraGioca();
			}
		}
		map.put("bm",codEventi);
		map.put("rank",rank);
		map.put("voto",voto);
		map.put("nome",nome);
		map.put("nome",nome);
		map.put("capitano",capitano);
		map.put("viceCapitano",viceCapitano);
		map.put("fantavoto",fantavoto);
		map.put("squadraGioca",squadraGioca);
	}



	private static Map generaTeamsSquadra(Squadra sq, List<Map> teams) {
		String modulo = sq.getModulo();
		Map mapT = new HashMap<>();
		mapT.put("id", Integer.parseInt(sq.getIdSquadra()));//id squadra
		mapT.put("modulo", modulo);//modulo
		mapT.put("moduloS", "");
		mapT.put("fattore", 0);//fattore casa
		mapT.put("total", 0);//totale
		mapT.put("cap", sq.getCapitano());//capitano
		mapT.put("bmp", 0);//bonus portiere
		mapT.put("bmd", 0);//bonus difesa
		mapT.put("bmc", 0);//bonus capitano
		mapT.put("bma", 0);//bonus attacco
		mapT.put("bmfp", 0);//bonus fp
		mapT.put("bmr", 0);//bonus rendimento
		mapT.put("bmcap", 0);// bonus capitano
		mapT.put("ru", 0);//??
		List<Map> players = new ArrayList<>();
		addPlayer(players, addEventiInOriginali(sq, sq.getTitolariOriginali()), null, true);//aggiunto giocatore
		addPlayer(players, addEventiInOriginali(sq, sq.getRiserveOriginali()), null, true);//aggiunto giocatore
		mapT.put("players", players);
		return mapT;
	}

	private static List<Giocatore> addEventiInOriginali(Squadra sq, List<Giocatore> l) {
		List<Giocatore> titolari = new ArrayList<Giocatore>();
		for (Giocatore giocatore : l) {
			List<Integer> codEventi=null;
			Double voto=null;
			Boolean squadraGioca=null;
			for (Giocatore giocatore2 : sq.getTitolari()) {
				if (giocatore2.getId().equals(giocatore.getId())){
					codEventi=giocatore2.getCodEventi();
					voto=giocatore2.getVoto();
					squadraGioca = giocatore2.isSquadraGioca();
				}
			}
			for (Giocatore giocatore2 : sq.getRiserve()) {
				if (giocatore2.getId().equals(giocatore.getId())){
					codEventi=giocatore2.getCodEventi();
					voto=giocatore2.getVoto();
					squadraGioca = giocatore2.isSquadraGioca();
				}
			}
			giocatore.setCodEventi(codEventi);
			giocatore.setVoto(voto);
			giocatore.setSquadraGioca(squadraGioca);
			titolari.add(giocatore);
		}
		return titolari;
	}

	private static Map<String, Object> bm_FG(String lega) throws Exception {
		Map bodyMap = new HashMap<>();
		bodyMap.put("username", constant.UTENTE_FG);
		bodyMap.put("password", constant.PWD_FG);
		Map<String, String> headers=new HashMap<>();
		headers.put("app_key", constant.APPKEY_FG_MOBILE);
		Map<String, Object> mapPutHTTP = callHTTP("PUT","application/json", String.format(Constant.URL_LOGIN_FG), toJson(bodyMap), headers);
		List<String> listCookie = ((List<String>)((Map)mapPutHTTP.get("headerFields")).get("Set-Cookie"));
		String cookieName = "LegheFG2_Leghe2021";
		String cookieValue=null;
		for (String cookie : listCookie) {
			int indexOf = cookie.indexOf(cookieName);
			if (indexOf>-1) {
				cookieValue=cookie.substring(cookieName.length()+1);
				cookieValue=cookieValue.substring(0,cookieValue.indexOf("; expires="));
			}
		}
		String responseBody;
		BasicCookieStore cookieStore = new BasicCookieStore();
		BasicClientCookie cookie;
		cookie = new BasicClientCookie(cookieName, cookieValue);
		cookie.setDomain("leghe.fantacalcio.it");
		cookie.setPath("/");
		cookieStore.addCookie(cookie);
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		try {
			String uri = "https://leghe.fantacalcio.it/" + lega + "/gestione-lega/opzioni-calcolo";
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
			responseBody = httpclient.execute(httpget, responseHandler);
		} finally {
			httpclient.close();
		}
		String token = "__.s('lo', __.d('";
		responseBody=responseBody.substring(responseBody.indexOf(token)+token.length());
		responseBody=responseBody.substring(0,responseBody.indexOf("'));"));
		byte[] decode = Base64.getDecoder().decode(responseBody.getBytes());
		String ret = new String(decode);
		Map<String, Object> jsonToMap = jsonToMap(ret);
		return jsonToMap;
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
		String http = (String) callHTTP("GET", "application/json; charset=UTF-8", String.format(Constant.URL_LOGIN_FS), null).get("response");
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
		Map<String, Object> postHTTP = callHTTP("POST",contentType,Constant.URL_LOGIN_FS,body.toString(), headers);
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

	public static void inviaCronacaNotifica(String msg, Integer ritardo) throws Exception {
		if (!constant.DISABILITA_NOTIFICA_TELEGRAM) {
			if (ritardo != null) {
				ThreadSeparato threadSeparato = new ThreadSeparato(fantaCronacaLiveBot, constant.CHAT_ID_FANTALIVE,msg);
				executor.schedule(threadSeparato, ritardo, TimeUnit.SECONDS);
			} else {
				fantaCronacaLiveBot.inviaMessaggio(constant.CHAT_ID_FANTALIVE,msg);
			}
		}
		else {
			System.out.println("Notifica:\n" + msg);
		}
	}


	public static void inviaRisultatiNotifica(String msg) throws Exception {
		if (!constant.DISABILITA_NOTIFICA_TELEGRAM) {
			ThreadSeparato threadSeparato = new ThreadSeparato(risultatiConRitardoBOT, constant.CHAT_ID_FANTALIVE,msg);
			executor.schedule(threadSeparato, 15, TimeUnit.SECONDS);
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
		String response = (String) callHTTP("GET", "application/json; charset=UTF-8", url, null).get("response");
		Document doc = Jsoup.parse(response);
		Elements select1 = doc.select(".list-rosters-item");
		Elements select = doc.select(".left-heading-link");
		int size = select1.size();
		for (int i=0;i<size;i++) {
			Element element1 = select1.get(i);//TODO QUI????
			String squadra = element1.select("H4").first().text();
			String giocatore = element1.select("H5").first().text();
			Element element = select.get(i);
			String link = element.select("A").attr("href");
			link=link.substring(link.lastIndexOf("=")+1);
			//			System.out.println(squadra + ";" + giocatore);
			if (!lega.equalsIgnoreCase(aliasCampionati.get(Constant.Campionati.JB.name())) || (lega.equalsIgnoreCase(aliasCampionati.get(Constant.Campionati.JB.name())) && sqJB.contains(squadra)) || (lega.equalsIgnoreCase(aliasCampionati.get(Constant.Campionati.JB.name())) && giocJB.contains(giocatore))){
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
			squadre.add(squadraCasa);
			squadre.add(squadraTrasferta);
			if (cancella) {
				Main.cancellaSalva(nome);
			}
		}
		return squadre;
	}

	public static void scaricaBe(Integer gg, String tokenNomeFile) throws Exception {
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

	private static Map<String, Map<String, Object>> oldSnapPartite=new LinkedHashMap();

	private static Map<String, Map<String, Object>> partiteLive() throws Exception {
		Map<String, Map<String, Object>> snapPartite=new LinkedHashMap();
		Map<String, Object> jsonToMap;
		//https://api2-mtc.gazzetta.it/api/v1/sports/calendar?sportId=1&competitionId=21&day=23
		String callHTTP= (String) callHTTP("GET", "application/json; charset=UTF-8", "https://api2-mtc.gazzetta.it/api/v1/sports/calendar?sportId=" + Constant.SPORT_ID_LIVE_GAZZETTA + "&competitionId=" + Constant.COMP_ID_LIVE_GAZZETTA + "&day=" + constant.GIORNATA, null).get("response");

		/*
{
"id": 9987,
"t": 63908564,
"id_a": 5,
"id_b": 17,
"g_a": 1,
"g_b": 5,
"p_t": "2022-01-09T13:31:24",
"s_t": "2022-01-09T14:34:21",
"d": "2022-01-09T13:30:00",
"sto": 4,
"m_a": "4231",
"m_b": "4231",
"n_a": "Empoli",
"n_b": "Sassuolo"
},


{
    VEN vs MIL = {
        tag = FullTime,
        val = 92,
        VEN = {
            gol = 0,
            RETI = []
        },
        MIL = {
            gol = 3,
            RETI = [{
                    tipo = Goal,
                    minuto = 2,
                    giocatore = Zlatan Ibrahimovic
                }, {
                    tipo = Goal,
                    minuto = 48,
                    giocatore = Theo Hernández
                }, {
                    tipo = Penalty,
                    minuto = 59,
                    giocatore = Theo Hernández
                }
            ]
        }
    }
}		 */
		jsonToMap = jsonToMap(callHTTP);
		List<Map> l = (List<Map>) ((Map)jsonToMap.get("data")).get("games");
		for (Map map : l) {
			List<Map> lm = (List<Map>) map.get("matches");
			for (Map map2 : lm) {
				Map awayTeam = (Map)map2.get("awayTeam");
				Map homeTeam = (Map)map2.get("homeTeam");
				String sqFuori = ((String)awayTeam.get("teamCode")).toUpperCase();
				String sqCasa = ((String)homeTeam.get("teamCode")).toUpperCase();


				Map<String, Object> partite = new LinkedHashMap();
				Map timing = (Map)map2.get("timing");
				String tag = (String)timing.get("tag");
				Object valTiming=timing.get("val");
				if (timing.get("val") != null) {
					valTiming= valTiming.toString();

				}
				else {
					valTiming="N/A";
				}
				String first_half_stop=null;
				String second_half_start=null;
				List<Map> stats = (List<Map>) timing.get("stat");
				if (stats != null) {
					for (Map stat : stats) {
						if ("first_half_stop".equalsIgnoreCase(stat.get("Type").toString())) {
							first_half_stop = (String) stat.get("value");
						}
						if ("second_half_start".equalsIgnoreCase(stat.get("Type").toString())) {
							second_half_start = (String) stat.get("value");
						}
					}
				}
				partite.put("first_half_stop",first_half_stop);
				partite.put("second_half_start",second_half_start);
				partite.put("tag",tag);
				partite.put("val", valTiming.toString());
				Map<String, Object> sq = new HashMap<>();
				sq.put("gol", homeTeam.get("score"));
				List<Map> goals = (List<Map>) ((Map)homeTeam.get("starData")).get("goals");
				List<Map> reti = new ArrayList<>();
				for (Map goal : goals) {
					Map rete = new LinkedHashMap<>();
					rete.put("tipo", goal.get("goalType"));
					rete.put("goalTimestamp", goal.get("goalTimestamp"));
					rete.put("minuto", goal.get("goalAbsoluteTime"));
					rete.put("giocatore", ((Map)goal.get("goalPlayer")).get("playerName"));
					reti.add(rete);
				}
				sq.put("RETI", reti);
				partite.put(sqCasa, sq);
				sq = new HashMap<>();
				sq.put("gol", awayTeam.get("score"));
				goals = (List<Map>) ((Map)awayTeam.get("starData")).get("goals");
				reti = new ArrayList<>();
				for (Map goal : goals) {
					Map rete = new LinkedHashMap<>();
					rete.put("tipo", goal.get("goalType"));
					rete.put("goalTimestamp", goal.get("goalTimestamp"));
					rete.put("minuto", goal.get("goalAbsoluteTime"));
					rete.put("giocatore", ((Map)goal.get("goalPlayer")).get("playerName"));
					reti.add(rete);
				}
				sq.put("RETI", reti);
				partite.put(sqFuori, sq);
				String key = sqCasa + " vs " + sqFuori;
				snapPartite.put(key, partite);
			}
		}
		return snapPartite;
	}

	private static void overrideTag(String s1, String s2, Map<String, String> mapSnap) throws Exception {
		//https://d2lhpso9w1g8dk.cloudfront.net/web/risorse/dati/live/16/live_21.json
		if (false) {
			ZonedDateTime zonedDateTime = calendarioInizioGiornata.get(Constant.GIORNATA);
			ZonedDateTime now = ZonedDateTime.now();
			if (now.isAfter(zonedDateTime)) {
				String callHTTP= (String) callHTTP("GET", "application/json; charset=UTF-8", "https://d2lhpso9w1g8dk.cloudfront.net/web/risorse/dati/live/" + Constant.I_LIVE_FANTACALCIO + "/live_" + Constant.GIORNATA + ".json", null).get("response");
				Map<String, Object> jsonToMap=jsonToMap(callHTTP);
				List<Map<String, Object>> incontri = (List<Map<String, Object>>) ((Map)jsonToMap.get("data")).get("inc");
				for (Map<String, Object> incontro : incontri) {
					String incSqCasa = sq.get(incontro.get("id_a"));
					String incSqFuori = sq.get(incontro.get("id_b"));
					if (incSqCasa.equals(s1) || incSqCasa.equals(s2)) {
						String newVal = (String) incontro.get("d");
						String newTag = reverseStatusMatch.get(incontro.get("sto"));
						if (mapSnap != null && !mapSnap.get("tag").equals(newTag)) {
							mapSnap.put("tag", newTag);
							mapSnap.put("val", newVal);
						}
					}
					if (incSqFuori.equals(s1) || incSqFuori.equals(s2)) {
						String newVal = (String) incontro.get("d");
						String newTag = reverseStatusMatch.get(incontro.get("sto"));
						if (mapSnap != null && !mapSnap.get("tag").equals(newTag)) {
							mapSnap.put("tag", newTag);
							mapSnap.put("val", newVal);
						}
					}

				}
			}
		}
	}
	public static Map<String,Object> callHTTP(String verbo, String contentType, String url, String body,  Map<String, String>... headers) throws Exception {
		//		System.out.println(verbo + " " + url + " " + printMap(headers));
		Map <String, Object> ret = new HashMap<>();
		URL obj = new URL(url);
		HttpURLConnection connectionHTTP = (HttpURLConnection) obj.openConnection();
		connectionHTTP.setRequestMethod(verbo);
		if (headers!=null && headers.length>0) {
			Iterator<String> iterator = headers[0].keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				connectionHTTP.setRequestProperty(key, headers[0].get(key));
			}
		}
		if (!verbo.equals("GET")) {
			connectionHTTP.setRequestProperty("content-type", contentType);
			connectionHTTP.setDoOutput(true);
			OutputStream os = connectionHTTP.getOutputStream();
			os.write(body.getBytes());
			os.flush();
			os.close();
		}
		else {
			if (body != null) {
				throw new RuntimeException("Per le chiamate con verbo GET non valorizzare il body");
			}
		}
		int responseCode=0;
		try
		{
			responseCode = connectionHTTP.getResponseCode();
		}
		catch (SSLHandshakeException e)
		{
			throw new RuntimeException("Aggiornare i certificati per: " + url);
		}
		StringBuffer response = new StringBuffer();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			Map<String, List<String>> headerFields = connectionHTTP.getHeaderFields();
			ret.put("headerFields", headerFields);
			BufferedReader in = new BufferedReader(new InputStreamReader(connectionHTTP.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} else {
			BufferedReader bfOutputResponse = new BufferedReader(
					new InputStreamReader(connectionHTTP.getErrorStream()));
			String outputLine;
			StringBuffer sfResponse = new StringBuffer();
			while ((outputLine = bfOutputResponse.readLine()) != null) {
				sfResponse.append(outputLine);
			}
			bfOutputResponse.close();
			String stringResponse = sfResponse.toString();
			throw new RuntimeException(verbo + " NOT WORKED ".concat(url).concat(" -> ").concat((body==null?"":body)).concat("STACK:")
					.concat(stringResponse));
		}
		ret.put("response", response.toString());
		return ret; 
	}
	public static Map<String,Object> postHTTP_DEPRECATA(String contentType, String url, String body,  Map<String, String>... headers) throws Exception {
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
			String stringResponse = sfResponse.toString();
			throw new RuntimeException("POST NOT WORKED ".concat(url).concat(" -> ").concat(body).concat("STACK:")
					.concat(stringResponse));
		}
		ret.put("response", response.toString());
		return ret; 
	}
	public static Map<String,Object> putHTTP_DEPRECATA(String contentType, String url, String body,  Map<String, String>... headers) throws Exception {
		//		System.out.println("POST " + url + " " + printMap(headers));
		Map <String, Object> ret = new HashMap<>();
		URL obj = new URL(url);
		HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
		postConnection.setRequestMethod("PUT");
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
			postConnection.getHeaderField("LegheFG2_Leghe2021");
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
			String stringResponse = sfResponse.toString();
			throw new RuntimeException("POST NOT WORKED ".concat(url).concat(" -> ").concat(body).concat("STACK:")
					.concat(stringResponse));
		}
		ret.put("response", response.toString());
		return ret; 
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

	public static String getHTTP_DEPRECATO(String url, Map<String, String>... headers) throws Exception {
		//		System.out.println("GET " + url + " " + printMap(headers));
		URL obj = new URL(url);
		HttpURLConnection getConnection = (HttpURLConnection) obj.openConnection();
		getConnection.setRequestMethod("GET");
		if (headers!=null && headers.length>0) {
			Iterator<String> iterator = headers[0].keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				getConnection.setRequestProperty(key, headers[0].get(key));
			}
		}
		int responseCode = getConnection.getResponseCode();
		Map<String, List<String>> headerFields = getConnection.getHeaderFields();
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
			String stringResponse = sfResponse.toString();
			throw new RuntimeException("GET NOT WORKED ".concat(url).concat(" -> ").concat("STACK:")
					.concat(stringResponse));
		}
		return response.toString(); 
	}

	public static void aggKeyFG() throws Exception {
		int giornata=constant.GIORNATA;
		Main.keyFG=new HashMap<String, String>();
		Main.keyFG.put(Constant.Campionati.FANTAVIVA.name(), "id_comp=" + Constant.COMP_VIVA_FG + "&r=" + String.valueOf(giornata - Constant.DELTA_VIVA_FG)  + "&f=" + String.valueOf(giornata - Constant.DELTA_VIVA_FG) + "_" + calcolaAggKey(Main.aliasCampionati.get(Constant.Campionati.FANTAVIVA.name())) + ".json");
		Main.keyFG.put(Constant.Campionati.LUCCICAR.name(), "id_comp=" + Constant.COMP_LUCCICAR_FG + "&r=" + String.valueOf(giornata - Constant.DELTA_LUCCICAR_FG) + "&f=" + String.valueOf(giornata - Constant.DELTA_LUCCICAR_FG) + "_" + calcolaAggKey(Main.aliasCampionati.get(Constant.Campionati.LUCCICAR.name())) + ".json");
		Main.keyFG.put(Constant.Campionati.JB.name(), "id_comp=" + Constant.COMP_JB_FG + "&r=" + String.valueOf(giornata - Constant.DELTA_JB_FG) + "&f=" + String.valueOf(giornata - Constant.DELTA_JB_FG) + "_" + calcolaAggKey(Main.aliasCampionati.get(Constant.Campionati.JB.name())) + ".json");
	}

	public static final Map<String, String> aliasCampionati = new HashMap<>();
	static {
		aliasCampionati.put(Constant.Campionati.LUCCICAR.name(), Constant.Campionati.LUCCICAR.name());
		aliasCampionati.put(Constant.Campionati.BE.name(), Constant.Campionati.BE.name());
		aliasCampionati.put(Constant.Campionati.JB.name(), "jb-fanta");
		aliasCampionati.put(Constant.Campionati.FANTAVIVA.name(), "fanta-viva");
	}	



	private static String calcolaAggKey(String lega) throws Exception {
		int giornata=0;
		if (lega.equalsIgnoreCase(aliasCampionati.get(Constant.Campionati.FANTAVIVA.name()))) giornata=constant.GIORNATA-Constant.DELTA_VIVA_FG;
		if (lega.equalsIgnoreCase(aliasCampionati.get(Constant.Campionati.LUCCICAR.name()))) giornata=constant.GIORNATA-Constant.DELTA_LUCCICAR_FG;
		if (lega.equalsIgnoreCase(aliasCampionati.get(Constant.Campionati.JB.name()))) giornata=constant.GIORNATA-Constant.DELTA_JB_FG;
		String string = (String) Main.callHTTP("GET", "application/json; charset=UTF-8", String.format(Constant.URL_FORMAZIONI_FG, lega,giornata,  Constant.COMP_VIVA_FG), null).get("response");
		//		System.out.println(string);
		string = string.substring(string.indexOf(".s('tmp', ")+11);
		string=string.substring(0,string.indexOf(")"));
		string = string.replace("|", "@");
		String[] split = string.split("@");
		return split[1];
	}

	public static void getSquadreFromFG(String lega) throws Exception {
		try {
			String aliasLega = aliasCampionati.get(lega);
			Map<String, String> nomiFG = getNomiFG(aliasLega);
			lega=lega.replace("-", "").toUpperCase();
			List<Squadra> squadre=new ArrayList<Squadra>();
			Map<String,String> headers = new HashMap<String, String>();
			headers.put("app_key", constant.APPKEY_FG);
			String url = "https://leghe.fantacalcio.it/servizi/V1_LegheFormazioni/Pagina?" + keyFG.get(lega);
			String string = (String) callHTTP("GET", "application/json; charset=UTF-8", url, null, headers ).get("response");
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
					String capitano = (String) map2.get("cap");
					String c="";
					String vc="";
					if (capitano != null) {
						String[] cap = capitano.split(";");
						if (cap.length>0) {
							c=cap[0];
						}
						if (cap.length>1) {
							vc=cap[1];
						}
					}
					List<Map> giocatori = (List<Map>) map2.get("pl");
					if (giocatori != null) {
						for (int i=0;i<giocatori.size();i++) {
							if (i==0) {
								Squadra squadra = new Squadra();
								squadre.add(squadra);
								squadra.setNome(nome);
								squadra.setCapitano(capitano);
								String modulo = map2.get("m").toString();
								squadra.setModulo(modulo.substring(0,modulo.indexOf(";")));
								squadra.setIdSquadra(map2.get("id").toString());
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
									partitaSimulata.setNome("  JB");
									squadra.setEvidenza(false);
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
							if (g.getId().equals(c)) {
								g.setCapitano(true);
							}
							if (g.getId().equals(vc)) {
								g.setViceCapitano(true);
							}
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
				adattaNick(lega, squadre);
				adattaNomePartitaSimulata(squadre, lega);
				String nomePartitaSimulata=null;
				for (Squadra squadra : squadre) {//todo evidenze fantaviva
					if (squadra.getNome().equalsIgnoreCase("tavolino")) {
						squadra.setEvidenza(true);
						PartitaSimulata partitaSimulata = squadra.getPartiteSimulate().get(0);
						if (partitaSimulata.isCasa()) {
							squadra.setCasaProiezione(true);
						}
						else {
							squadra.setCasaProiezione(false);
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
						} else {
							squadra.setCasaProiezione(false);
						}
					}
				}
			}

			for (Squadra squadra : squadre) {
				List<Giocatore> originali = new ArrayList<>();
				for (Giocatore giocatore : squadra.getTitolari()) {
					originali.add(giocatore);
				}
				squadra.setTitolariOriginali(originali);
			}

			for (Squadra squadra : squadre) {
				List<Giocatore> originali = new ArrayList<>();
				for (Giocatore giocatore : squadra.getRiserve()) {
					originali.add(giocatore);
				}
				squadra.setRiserveOriginali(originali);
			}
			upsertSalva(Constant.FORMAZIONE + lega , toJson(squadre));
			//		return squadre;
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
	}

	public static void adattaNick(String lega, List<Squadra> squadre) {
		for (Squadra squadra : squadre) {
			squadra.setNick(nickPlayer.get(lega + "-" + squadra.getNome()));
		}
	}

	public static Map<String, Object> simulaCambiMantra(String lega, List<String> assenti, Squadra sq) throws Exception {
		Map<String, Object> jsonToMap;
		Map bodyMap = new HashMap<>();
		bodyMap.put("username", constant.UTENTE_FG);
		bodyMap.put("password", constant.PWD_FG);
		Map<String, String> headers=new HashMap<>();
		headers.put("app_key", constant.APPKEY_FG_MOBILE);
		Map<String, Object> postHTTP = callHTTP("POST","application/json; charset=UTF-8",String.format(Constant.URL_LOGIN_APP_FG),toJson(bodyMap), headers);
		String response = (String) postHTTP.get("response");
		Map<String, Object> mapResponse = jsonToMap(response);//dati del login
		Map data = (Map) mapResponse.get("data");
		Map utente = (Map) data.get("utente");			
		String user_token = (String) utente.get("utente_token");
		List<Map> leghe = (List<Map>) data.get("leghe");			
		String lega_token = "";
		int id_squadra=0;
		for (Map legaAtt : leghe) {
			if (((String)legaAtt.get("alias")).equalsIgnoreCase(lega)) {
				lega_token = (String) legaAtt.get("token");
				id_squadra = (int) legaAtt.get("id_squadra");

			}
		}
		String modulo = sq.getModulo();
		Map mapBody = new HashMap<>();
		List<Map> teams = new ArrayList<>();
		Map mapT = new HashMap<>();
		mapT.put("id", id_squadra);//id squadra
		mapT.put("modulo", modulo);//modulo
		mapT.put("moduloS", "");
		List<Map> players = new ArrayList<>();
		addPlayer(players, sq.getTitolariOriginali(), assenti, false);//aggiunto giocatore
		addPlayer(players, sq.getRiserveOriginali(), assenti, false);//aggiunto giocatore
		mapT.put("players", players);
		teams.add(mapT);
		mapBody.put("teams", teams);
		headers=new HashMap<>();
		headers.put("app_key", constant.APPKEY_FG_MOBILE);
		headers.put("lega_token", lega_token);
		headers.put("user_token", user_token);
		postHTTP = callHTTP("POST","application/json; charset=UTF-8","https://appleghe.fantacalcio.it/api/v1/V1_LegaFormazioni/Simulatore",toJson(mapBody), headers);
		response = (String) postHTTP.get("response");
		mapResponse = jsonToMap(response);
		List listErrorMessage = (List)mapResponse.get("error_msgs");
		if (listErrorMessage != null) {
			throw new RuntimeException(listErrorMessage.get(0).toString());
		}
		teams = (List<Map>) ((Map)mapResponse.get("data")).get("teams");
		Map mapRisposta = teams.get(0);
		players = (List<Map>) mapRisposta.get("players");
		List<Map<String,Object>> calciatori=new ArrayList<>();
		for (Giocatore giocatore : sq.getTitolari()) {
			Integer idCalciatore = Integer.parseInt(giocatore.getId());
			Double malus=null;
			Boolean played=null;
			Double rank=null;
			for (Map player : players) {
				if (((Integer)player.get("id")).equals(idCalciatore)) {
					malus = (Double) player.get("malus");
					played = (Boolean) player.get("played");
					rank = (Double) player.get("rank");
				}
			}
			Map<String,Object> calciatore = new HashMap<>();
			calciatore.put("malus", malus);
			calciatore.put("played", played);
			calciatore.put("rank", rank);
			calciatore.put("id",idCalciatore);
			calciatore.put("n",giocatore.getNome());
			calciatore.put("s",giocatore.getSquadra());
			calciatore.put("r",giocatore.getRuolo());
			calciatori.add(calciatore);
		}
		for (Giocatore giocatore : sq.getRiserve()) {
			Integer idCalciatore = Integer.parseInt(giocatore.getId());
			Double malus=null;
			Boolean played=null;
			Double rank=null;
			for (Map player : players) {
				if (((Integer)player.get("id")).equals(idCalciatore)) {
					malus = (Double) player.get("malus");
					played = (Boolean) player.get("played");
					rank = (Double) player.get("rank");
				}
			}
			Map<String,Object> calciatore = new HashMap<>();
			calciatore.put("malus", malus);
			calciatore.put("played", played);
			calciatore.put("rank", rank);
			calciatore.put("id",idCalciatore);
			calciatore.put("n",giocatore.getNome());
			calciatore.put("s",giocatore.getSquadra());
			calciatore.put("r",giocatore.getRuolo());
			calciatori.add(calciatore);
		}



		Map<String, Object> ret = new HashMap<>();
		ret.put("modulo",mapRisposta.get("modulo"));
		ret.put("moduloS",mapRisposta.get("moduloS"));
		List<Map<String, Object>>  calciatoriEntra = new ArrayList<>();
		List<Map<String, Object>>  calciatoriNonEntra = new ArrayList<>();
		for (Map<String,Object> calciatore : calciatori) {
			Map<String, Object> calciatoreDef = new HashMap<>();
			calciatoreDef.put("id",calciatore.get("id"));
			calciatoreDef.put("nome",calciatore.get("n"));
			calciatoreDef.put("squadra",calciatore.get("s"));
			calciatoreDef.put("ruolo",calciatore.get("r"));
			calciatoreDef.put("malus",(calciatore.get("malus")==null?"N/A":calciatore.get("malus")));
			calciatoreDef.put("played",(calciatore.get("played")==null?false:calciatore.get("played")));
			Double rank = (Double) calciatore.get("rank");
			calciatoreDef.put("rank",((rank==null?"N/A":rank.compareTo(new Double("6"))==0?"S":(rank.compareTo(new Double("56"))==0?"N":"???"))));
			if (((Boolean)calciatoreDef.get("played"))) {
				calciatoriEntra.add(calciatoreDef);
			} else {
				calciatoriNonEntra.add(calciatoreDef);
			}
		}
		ret.put("calciatoriEntra",calciatoriEntra);
		ret.put("calciatoriNonEntra",calciatoriNonEntra);
		return ret;
	}

	private static void addPlayer(List<Map> players, List<Giocatore> giocatori,List<String> assenti, boolean proiezione) {
		for (Giocatore gioc : giocatori) {
			Map player = new HashMap<>();
			player.put("id",gioc.getId());
			if (proiezione) {
				player.put("bm",gioc.getCodEventi());
				player.put("totBm",0);
				double voto = gioc.getVoto();
				if (voto == 0 && gioc.isSquadraGioca()) {
					voto=56;
				} else if (voto == 0 && !gioc.isSquadraGioca()) {
					voto=6;
				}
				player.put("rank",voto);
			} else {
				if (assenti.contains(gioc.getId())) {
					player.put("rank",56);
				} else {
					player.put("rank",6);
				}
			}
			player.put("played",false);
			player.put("malus",0);

			players.add(player);
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
	public static void clearDB() throws Exception {
		salvaRepository.deleteAll();
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
				returns.setTipologia(retAtt.getTipologia());
				returns.setSfide(retAtt.getSfide());
				returns.setTipo(retAtt.getTipo());
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
		List<Live> lives = new ArrayList<Live>();
		Map<String, Map<String, Object>> snapPartite;
		if (fromFile) {
			snapPartite =  jsonToSnapPartite(getTesto("snapPartite")); 
			lives =  jsonToLives(getTesto("lives"));

		} else {
			snapPartite=partiteLive();
			lives=recuperaLives();
		}
		Map<String, Map<String, String>> orari=new HashMap<>();
		for (String kkk : snapPartite.keySet()) {
			Map<String, Object> partita = snapPartite.get(kkk);
			String tag = "";
			String val = "";
			List<String> alSq=new ArrayList<>();
			for (Map.Entry<String, Object> s : partita.entrySet()) {
				if (s.getKey().equalsIgnoreCase("tag")) {
					tag = (String) s.getValue();
				}
				else if (s.getKey().equalsIgnoreCase("val")) {
					val = (String) s.getValue();
				}
				else if (s.getKey().equalsIgnoreCase("first_half_stop")) {

				}
				else if (s.getKey().equalsIgnoreCase("second_half_start")) {

				}
				else {
					alSq.add(s.getKey());
				}
			}
			//costruisce orari da snapPartite
			Map<String, String> map = new HashMap<>();
			String sqCasa=alSq.get(0);
			String sqFuori=alSq.get(1);
			map.put("tag", tag);
			map.put("val", val);
			overrideTag(sqCasa,sqFuori, map);
			orari.put(sqCasa, map);
			orari.put(sqFuori, map);
			generaNotificheRisultati(partita, tag, sqCasa, sqFuori);
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
		ret.put("snapPartite", snapPartite);
		ret.put("lives", lives);
		oldSnapPartite=snapPartite;
		return ret;
	}

	private static void generaNotificheRisultati(Map<String, Object> partita, String tag, String sqCasa, String sqFuori) throws Exception {
		String key = sqCasa + " vs " + sqFuori;
		//confronta con oldSnapPartite
		Map<String, Object> oldPartita = oldSnapPartite.get(key);
		StringBuilder messaggio=null;
		if (oldPartita != null) {
			boolean cambioTag=false;
			String oldTag = (String) oldPartita.get("tag");
			if (!tag.equals(oldTag)) {
				cambioTag=true;
				/*
				if (messaggio==null) {
					messaggio=new StringBuilder(key + "\n");
				}
				messaggio.append(tag + "\n");
				 */
			}
			if (cambioTag) {
				String testoCallback = visSnapPartita(new ArrayList<>(), sqCasa + " vs " + sqFuori, partita);
				messaggio=new StringBuilder(testoCallback);
			} else {
				Integer oldGolCasa = (Integer) ((Map)oldPartita.get(sqCasa)).get("gol");
				Integer oldGolFuori = (Integer) ((Map)oldPartita.get(sqFuori)).get("gol");
				Integer golCasa = (Integer) ((Map)partita.get(sqCasa)).get("gol");
				Integer golFuori = (Integer) ((Map)partita.get(sqFuori)).get("gol");
				if (oldGolCasa != golCasa || oldGolFuori != golFuori) {
					if (messaggio==null) {
						messaggio=new StringBuilder(key + "\n");
					}
					messaggio.append("Risultato:" + golCasa + "-" + golFuori + "\n");
				}

				List<Map> oldRetiCasa = (List<Map>) ((Map)oldPartita.get(sqCasa)).get("RETI");
				List<Map> retiCasa = (List<Map>) ((Map)partita.get(sqCasa)).get("RETI");
				if (oldRetiCasa.size()>retiCasa.size()) {
					if (messaggio==null) {
						messaggio=new StringBuilder(key + "\n");
					}
					messaggio.append("Correzione reti per " + sqCasa + ": " + logReti(retiCasa) + "\n");
				}
				for (int i=0;i<retiCasa.size();i++) {
					Map mapRetiCasa = retiCasa.get(i);
					if (oldRetiCasa.size() > i) {
						Map mapOldRetiCasa = oldRetiCasa.get(i);
						if (!mapRetiCasa.get("tipo").equals(mapOldRetiCasa.get("tipo")) || !mapRetiCasa.get("minuto").equals(mapOldRetiCasa.get("minuto"))
								|| !mapRetiCasa.get("giocatore").equals(mapOldRetiCasa.get("giocatore"))) {
							if (messaggio==null) {
								messaggio=new StringBuilder(key + "\n");
							}
							messaggio.append(String.format("Correzione rete di %s : da %s a %s \n", sqCasa,logRete(mapOldRetiCasa), logRete(mapRetiCasa)));
						}
					} else {
						if (messaggio==null) {
							messaggio=new StringBuilder(key + "\n");
						}
						messaggio.append(logRete(mapRetiCasa));
					}
				}

				List<Map> oldRetiFuori = (List<Map>) ((Map)oldPartita.get(sqFuori)).get("RETI");
				List<Map> retiFuori = (List<Map>) ((Map)partita.get(sqFuori)).get("RETI");
				if (oldRetiFuori.size()>retiFuori.size()) {
					if (messaggio==null) {
						messaggio=new StringBuilder(key + "\n");
					}
					messaggio.append("Correzione reti per " + sqFuori + ": " + logReti(retiFuori) + "\n");
				}
				for (int i=0;i<retiFuori.size();i++) {
					Map mapRetiFuori = retiFuori.get(i);
					if (oldRetiFuori.size() > i) {
						Map mapOldRetiFuori = oldRetiFuori.get(i);
						if (!mapRetiFuori.get("tipo").equals(mapOldRetiFuori.get("tipo")) || !mapRetiFuori.get("minuto").equals(mapOldRetiFuori.get("minuto"))
								|| !mapRetiFuori.get("giocatore").equals(mapOldRetiFuori.get("giocatore"))) {
							if (messaggio==null) {
								messaggio=new StringBuilder(key + "\n");
							}
							messaggio.append(String.format("Correzione rete di %s : da %s a %s \n", sqCasa,logRete(mapOldRetiFuori), logRete(mapRetiFuori)));
						}
					} else {
						if (messaggio==null) {
							messaggio=new StringBuilder(key + "\n");
						}
						messaggio.append(logRete(mapRetiFuori));
					}
				}
			}
			if (messaggio != null) {
				try {
					inviaRisultatiNotifica(messaggio.toString());
				} catch (Exception e)
				{
					e.printStackTrace(System.out);
				}
			}
		}
	}

	private static String logRete(Map mapRete) {
		return MessageFormat.format("{0} {1} al {2}\n", mapRete.get("tipo"), mapRete.get("giocatore"), mapRete.get("minuto"));
	}

	private static String logReti(List<Map> listMmapRete) {
		StringBuilder sb = new StringBuilder();
		for (Map mapRete : listMmapRete) {
			sb.append(MessageFormat.format("{0} {1} al {2}\n", mapRete.get("tipo"), mapRete.get("giocatore"), mapRete.get("minuto")));
		}
		return sb.toString();
	}

	private static List<Live> recuperaLives() throws Exception {
		List<Live> lives = new ArrayList<Live>();
		Iterator<Integer> iterator = sq.keySet().iterator();
		while (iterator.hasNext()) {
			Integer integer = (Integer) iterator.next();
			//https://www.fantacalcio.it/api/live/10?g=23&i=16 g è la giornata
			String sqFromLive = (String) callHTTP("GET", "application/json; charset=UTF-8", "https://www.fantacalcio.it/api/live/" + integer + "?g=" + constant.GIORNATA + "&i=" + Constant.I_LIVE_FANTACALCIO, null).get("response");
			List<Map<String, Object>> getLiveFromFG = jsonToList(sqFromLive);
			Live live = new Live();
			live.setSquadra(sq.get(integer));
			live.setGiocatori(getLiveFromFG);
			lives.add(live);
		}
		return lives;
	}

	private static String getStatusMatch(String tag, Integer oldStatusMatch) {
		if (oldStatusMatch!=null) {
			Integer attStatusMatch = statusMatch.get(tag);
			if (attStatusMatch == null) {
				attStatusMatch=100;
			}
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
		r.setTipologia(configCampionato.getTipologia());
		r.setSfide(configCampionato.getSfide());
		r.setTipo(configCampionato.getTipo());
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
					/*
					if (sqStatusMatch==null) {
						System.out.println();
					}
					if (giocatore == null) {
						System.out.println();
					}
					if (giocatore.getSquadra()==null) {
						System.out.println();
					}
					if (sqStatusMatch.get(giocatore.getSquadra().toUpperCase())==null) {
						System.out.println();
					}
					 */
					if (giocatore == null || giocatore.getSquadra()==null || sqStatusMatch == null || sqStatusMatch.get(giocatore.getSquadra().toUpperCase()) == null 
							|| sqStatusMatch.get(giocatore.getSquadra().toUpperCase()) == 1|| sqStatusMatch.get(giocatore.getSquadra().toUpperCase()) == 2|| sqStatusMatch.get(giocatore.getSquadra().toUpperCase()) == 3|| sqStatusMatch.get(giocatore.getSquadra().toUpperCase()) == 4) {
						giocatore.setSquadraGioca(true);
					} else {
						giocatore.setSquadraGioca(false);
					}
					if (giocatore == null || giocatore.getSquadra()==null || sqStatusMatch == null || sqStatusMatch.get(giocatore.getSquadra().toUpperCase()) == null || sqStatusMatch.get(giocatore.getSquadra().toUpperCase()) != 8) {
						giocatore.setVoto(0);
						giocatore.setNonGioca(false);
					} else {
						giocatore.setVoto(6);
						giocatore.setNonGioca(true);
					}
					giocatore.setEvento("");
					giocatore.setNotificaLive(false);
					giocatore.setCodEventi(new ArrayList<Integer>());
				}
				for (int i=0;i<squadra.getRiserve().size();i++) {
					Giocatore giocatore = squadra.getRiserve().get(i);
					giocatore.setModificatore(0);
					if (giocatore == null || giocatore.getSquadra()==null || sqStatusMatch == null || sqStatusMatch.get(giocatore.getSquadra().toUpperCase()) == null 
							|| sqStatusMatch.get(giocatore.getSquadra().toUpperCase()) == 1|| sqStatusMatch.get(giocatore.getSquadra().toUpperCase()) == 2|| sqStatusMatch.get(giocatore.getSquadra().toUpperCase()) == 3|| sqStatusMatch.get(giocatore.getSquadra().toUpperCase()) == 4) {
						giocatore.setSquadraGioca(true);
					} else {
						giocatore.setSquadraGioca(false);
					}
					if (giocatore == null || giocatore.getSquadra()==null || sqStatusMatch == null || sqStatusMatch.get(giocatore.getSquadra().toUpperCase()) == null || sqStatusMatch.get(giocatore.getSquadra().toUpperCase()) != 8) {
						giocatore.setVoto(0);
						giocatore.setNonGioca(false);
					} else {
						giocatore.setVoto(6);
						giocatore.setNonGioca(true);
					}
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
			boolean isAmmonito=false;
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
				if (giocatore.getCodEventi().contains(1) || giocatore.getCodEventi().contains(2)) {
					isAmmonito=true;
				}
			}
			if (modificatori.get(campionato) != null) {
				if (!isAmmonito) {
					squadra.setFairPlay((double) ((Map)modificatori.get(campionato)).get("fairplay"));
				} else {
					squadra.setFairPlay(0);
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
					if (nomeGiocatoreLive.toUpperCase().indexOf("APAT")>-1 && giocatore.getNome().toUpperCase().indexOf("APAT")>-1 && tipo.equalsIgnoreCase("FANTASERVICE")) {
//						System.out.println();
					}
					if (tipo.equals("FANTAGAZZETTA") && 
							giocatore.getNomeTrim().equalsIgnoreCase(nomeGiocatoreLive.replaceAll(" ", "")
									)
							|| 
							(tipo.equals("FANTASERVICE") && 
									giocatore.getNome().substring(0,giocatore.getNome().lastIndexOf(" ")).replaceAll(" ", "").equalsIgnoreCase(nomeGiocatoreLive.replaceAll(" ", "")))	
							||
							(tipo.equals("FANTASERVICE") && 
									giocatore.getNome().concat(" ").substring(0,giocatore.getNome().concat(" ").lastIndexOf(" ")).replaceAll(" ", "").equalsIgnoreCase(nomeGiocatoreLive.replaceAll(" ", "")))	
							){
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
				}
			}
		}

		return squadra;
	}
	public static void adattaNomePartitaSimulata(List<Squadra> squadre, String lega) {
		Map<String, List<String>> m = new HashMap<>();
		for (Squadra squadra : squadre) {
			List<PartitaSimulata> partiteSimulate = squadra.getPartiteSimulate();
			for (PartitaSimulata partitaSimulata : partiteSimulate) {
				List<String> listaSquadre = m.get(partitaSimulata.getNome());
				if (listaSquadre == null) {
					listaSquadre=new ArrayList<>();
				}

				String nick = nickPlayer.get(lega + "-" + partitaSimulata.getSquadra());
				if (nick==null) {
					nick=partitaSimulata.getSquadra().substring(0,3);
				}

				listaSquadre.add(nick);
				m.put(partitaSimulata.getNome(), listaSquadre);
			}
		}

		for (Squadra squadra : squadre) {
			List<PartitaSimulata> partiteSimulate = squadra.getPartiteSimulate();
			for (PartitaSimulata partitaSimulata : partiteSimulate) {
				String nuovoNome=partitaSimulata.getNome().substring(0,partitaSimulata.getNome().length()-1);
				List<String> listaSquadre = m.get(partitaSimulata.getNome());
				for (String nomeSquadra : listaSquadre) {
					nuovoNome= nuovoNome + " " + nomeSquadra;
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
				else if (nomeG.equalsIgnoreCase("Leao R.")) nomeG="Rafael Leao ";
				else if (nomeG.equalsIgnoreCase("Samu Castillejo .")) nomeG="Castillejo ";
				else if (nomeG.equalsIgnoreCase("Joao Pedro Galvao .")) nomeG="Joao Pedro ";
				else if (nomeG.equalsIgnoreCase("Kessie F.")) nomeG="Kessie' ";
				else if (nomeG.equalsIgnoreCase("Rafael Toloi .")) nomeG="Toloi ";
				else if (nomeG.equalsIgnoreCase("Pezzella G.")) nomeG="Pezzella Giu. ";
				else if (nomeG.equalsIgnoreCase("Gerard Deulofeu .")) nomeG="Deulofeu ";
				else if (nomeG.equalsIgnoreCase("Brahim Diaz .")) nomeG="Diaz B. ";
				else if (nomeG.equalsIgnoreCase("Roger Ibanez .")) nomeG="Ibanez ";
				else if (nomeG.equalsIgnoreCase("Gonzalo Villar .")) nomeG="Villar ";
				else if (nomeG.equalsIgnoreCase("Kouame C.")) nomeG="Kouame' ";
				else if (nomeG.equalsIgnoreCase("Montipo L.")) nomeG="Montipo' ";
				else if (nomeG.equalsIgnoreCase("Nkoulou N.")) nomeG="N'Koulou ";
				else if (nomeG.equalsIgnoreCase("Jose Callejon .")) nomeG="Callejon ";
				else if (nomeG.equalsIgnoreCase("Pellegrini L.")) nomeG="Pellegrini Lo. ";
				else if (nomeG.equalsIgnoreCase("Balde K.")) nomeG="Keita B. ";
				else if (nomeG.equalsIgnoreCase("Alex Sandro .")) nomeG="Alex Sandro ";
				else if (nomeG.equalsIgnoreCase("Junior Messias ."))  nomeG="Messias ";
				else if (nomeG.equalsIgnoreCase("Borja Mayoral .")) nomeG="Mayoral ";
				else if (nomeG.equalsIgnoreCase("N'Zola M.")) nomeG="Nzola ";
				else if (nomeG.equalsIgnoreCase("Molina N.") && squadra.equalsIgnoreCase("UDI")) nomeG="Molina N. ";
				else if (nomeG.equalsIgnoreCase("Danilo .") && squadra.equalsIgnoreCase("BOL")) nomeG="Danilo LAR. ";
				else if (nomeG.equalsIgnoreCase("Bastoni A.")) nomeG="Bastoni ";
				else if (nomeG.equalsIgnoreCase("Fares M.")) nomeG="Fares ";
				else if (nomeG.equalsIgnoreCase("Simeone G.")) nomeG="Simeone ";
				else if (nomeG.equalsIgnoreCase("Martinez L.") && squadra.equalsIgnoreCase("FIO")) nomeG="Martinez Quarta ";
				else if (nomeG.equalsIgnoreCase("Martinez L.") && squadra.equalsIgnoreCase("INT")) nomeG="Martinez L. ";
				else if (nomeG.equalsIgnoreCase("Pepe Reina .")) nomeG="Reina ";
				else if (nomeG.equalsIgnoreCase("Sergio Oliveira .")) nomeG="Oliveira ";
				else if (nomeG.equalsIgnoreCase("Ikone J.")) nomeG="Ikone' ";
				else if (nomeG.equalsIgnoreCase("Cordaz A.")) nomeG="Cordaz ";
				else if (nomeG.equalsIgnoreCase("Daniel Fuzato .")) nomeG="Fuzato ";
				else if (nomeG.equalsIgnoreCase("Alvaro Odriozola .")) nomeG="Odriozola ";
				else if (nomeG.equalsIgnoreCase("Spinazzola L.")) nomeG="Spinazzola ";
				else if (nomeG.equalsIgnoreCase("Traore H.")) nomeG="Traore' Hj. ";
				else if (nomeG.equalsIgnoreCase("Nwankwo S.")) nomeG="Simy ";
				else if (nomeG.equalsIgnoreCase("Arthur Cabral .")) nomeG="Cabral ";
				
				
/*				
				if (nomeG.equalsIgnoreCase("Zapata D.")) nomeG="Zapata D. ";
				if (nomeG.equalsIgnoreCase("Ricci M.")) nomeG="Ricci M. ";
				if (nomeG.equalsIgnoreCase("Hernandez T.")) nomeG="Hernandez T. ";
				if (nomeG.equalsIgnoreCase("Lopez M.")) nomeG="Lopez M. ";
				if (nomeG.equalsIgnoreCase("Ferrari G.")) nomeG="Ferrari G. ";
				if (nomeG.equalsIgnoreCase("Rodriguez R.")) nomeG="Rodriguez R. ";
				if (nomeG.equalsIgnoreCase("Bastoni S.")) nomeG="Bastoni S. ";
				if (nomeG.equalsIgnoreCase("Milinkovic-Savic V.")) nomeG="Milinkovic-Savic V. ";
				if (nomeG.equalsIgnoreCase("Radu I.")) nomeG="Radu I. ";
				if (nomeG.equalsIgnoreCase("Ricci S.")) nomeG="Ricci S. ";
				if (nomeG.equalsIgnoreCase("Gonzalez N.")) nomeG="Gonzalez N. ";
				if (nomeG.equalsIgnoreCase("Coulibaly M.")) nomeG="Coulibaly M. ";
				if (nomeG.equalsIgnoreCase("Henderson L.")) nomeG="Henderson L. ";
*/	
				/*
				if (nomeG.contains("alvao")) {
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

	public static Map<String, Map<String, Object>> jsonToSnapPartite(String json)
	{
		try
		{
			return mapper.readValue(json, new TypeReference<Map<String, Map<String, Object>>>(){});
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	public static <T> T fromJson(String json, Class<T> clazz) throws Exception{
		return mapper.readValue(json, clazz);
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
			if(!salva.getNome().equals("lives") && !salva.getNome().equals("snapPartite") && !salva.getNome().startsWith(Constant.FORMAZIONE)) {
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
	private static Connection connectionNoSpring=null;
	private static synchronized Connection getConnectionNoSprig() throws Exception {
		if (connectionNoSpring == null) {
			Properties prop = new Properties();
			prop.load(new ClassPathResource("application-DEV.properties").getInputStream());
			String datasourceUrl = prop.getProperty("spring.datasource.url");
			connectionNoSpring = DriverManager.getConnection(datasourceUrl);
		}
		return connectionNoSpring;
	}
	private static void putSalvaNoSprint(Salva salva) throws Exception {
		Connection connection = getConnectionNoSprig();
		PreparedStatement prepareStatement = connection.prepareStatement("delete from salva where nome=?");
		prepareStatement.setString(1, salva.getNome());
		prepareStatement.execute();
		prepareStatement = connection.prepareStatement("insert into salva (nome,testo) values (?,?)");
		prepareStatement.setString(1, salva.getNome());
		prepareStatement.setString(2, salva.getTesto());
		prepareStatement.execute();
	}
	private static String getTestoNoSpring(String nome) throws Exception {
		Connection connection = getConnectionNoSprig();
		PreparedStatement prepareStatement = connection.prepareStatement("select * from salva where nome=?");
		prepareStatement.setString(1, nome);
		ResultSet rs = prepareStatement.executeQuery();
		boolean next = rs.next();
		if (next) {
			return rs.getString("testo");
		}
		else {
			return null;
		}
	}
	private static Salva getSalvaNoSpring(String nome) throws Exception {
		String testo=getTestoNoSpring(nome);
		if (testo == null) {
			return null;
		} else {
			Salva salva = new Salva();
			salva.setNome(nome);
			salva.setTesto(testo);
			return salva;

		}
	}
	public static void upsertSalva(String nome, String testo) throws Exception {
		if (salvaRepository==null) {
			Salva findOne = getSalvaNoSpring(nome);
			if (findOne==null) {
				findOne=new Salva();
				findOne.setNome(nome);
			}
			findOne.setTesto(testo);
			putSalvaNoSprint(findOne);
		} else {
			Salva findOne = salvaRepository.findOne(nome);
			if (findOne==null) {
				findOne=new Salva();
				findOne.setNome(nome);
			}
			findOne.setTesto(testo);
			salvaRepository.save(findOne);
		}
	}
	public static boolean esisteSalva(String nome) {
		return salvaRepository.exists(nome);
	}

	public static void cancellaSalva(String nome) {
		if (salvaRepository.exists(nome)) {
			salvaRepository.delete(nome);
		}
	}
	public static Map<String, Object>  getPartitaSimulata(Long chatId, String nomePartitaSimulata, String squadraSimulata) throws Exception{
		Map<String, Object> ret = new HashMap<>();
		List<String> squadreKey=new ArrayList<>();
		List<String> squadreCasa=new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		Map<String, Return> go = Main.go(true,null, null);
		List<Squadra> squadreFG=new ArrayList<>();
		String sfideFG=null;
		String campionatoFG=null;
		List<Squadra> sqFS=new ArrayList<>();
		for (String campionato : go.keySet()) {
			Return return1 = go.get(campionato);
			List<Squadra> squadre = return1.getSquadre();
			if (return1.getTipo().equalsIgnoreCase("FANTASERVICE") && nomePartitaSimulata.startsWith("B")) {
				overrideFS(squadre);
				for (Squadra squadraTmp : squadre) {
					List<PartitaSimulata> partiteSimulate = squadraTmp.getPartiteSimulate();
					for (PartitaSimulata partitaSimulata : partiteSimulate) {
						if (partitaSimulata.getNome().equalsIgnoreCase(nomePartitaSimulata)) {
							sqFS.add(squadraTmp);
						}

					}
				}
				if (sqFS.size()>0) {
					defaultGiocatoriNonAncoraVotoFS(sqFS);
					applicaCambi(sqFS);
					calcolaScontro(sqFS.get(0), sqFS.get(1), 1);
				}
			}
			for (Squadra squadra : squadre) {
				List<PartitaSimulata> partiteSimulate = squadra.getPartiteSimulate();
				for (PartitaSimulata partitaSimulata : partiteSimulate) {
					if (partitaSimulata.getNome().equalsIgnoreCase(nomePartitaSimulata)) {
						if (return1.getTipo().equalsIgnoreCase("FANTAGAZZETTA")) {
							squadreFG.add(squadra);
							campionatoFG=campionato;
							sfideFG=return1.getSfide();
						} else {
							sb.append(proiezioneSquadra(squadra, partitaSimulata.isCasa(),true));
						}
						if (partitaSimulata.isCasa()) {
							squadreCasa.add(squadra.getNome());
						}
						squadreKey.add(campionato + "-" + squadra.getNome());
						ret.put("campionato", campionato);

					}
				}
			}
		}
		Map<String, Object> proiezioneFG=null;
		if (nomePartitaSimulata.startsWith("B")) {
			proiezioneFS("BE", nomePartitaSimulata);
		} else
		{
				proiezioneFG = proiezioneFG(aliasCampionati.get(campionatoFG), squadreFG, sfideFG, squadraSimulata);
		}
		
		List<Map<String, Object>> l=new ArrayList<>();
		if (sfideFG != null) {
			Map<String, Object> p = (Map<String, Object>) proiezioneFG.get("data");
			l = (List<Map<String, Object>>) p.get("teams");
			List<String> nomi=new ArrayList<>();
			List<Double> modDif=new ArrayList<>();
			List<Double> modCapitano=new ArrayList<>();
			List<Double> fp=new ArrayList<>();
			List<Double> fattori=new ArrayList<>();
			List<String> moduli=new ArrayList<>();
			List<String> moduliS=new ArrayList<>();
			List<Double> totali=new ArrayList<>();
			List<Map<String, Object>> players = new ArrayList<>();
			for (Map<String, Object> map : l) {
				players = (List<Map<String, Object>>) map.get("players");
				modDif.add((Double) map.get("bmd"));
				modCapitano.add((Double) map.get("bmcap"));
				fp.add((Double) map.get("bmfp"));
				fattori.add((Double) map.get("fattore"));
				moduli.add((String) map.get("modulo"));
				moduliS.add((String) map.get("moduloS"));
				nomi.add((String) map.get("nome"));
				totali.add((Double) map.get("total"));
			}
			String ris = (String) p.get("ris");
			if (!ris.equals("-")) {
				sb.append("Risultato: " + ris + "\n");
			}
			for (int i=0;i<nomi.size();i++) {
				for (Squadra squadra : squadreFG) {
					String nome = nomi.get(i);
					if (squadra.getNome().equals(nome)) {
						sb.append("<b>" + nome + "</b>\n");
						sb.append("Modificatore difesa " + modDif.get(i)  + "\n");
						sb.append("Modificatore capitano " + modCapitano.get(i)  + "\n");
						sb.append("Fairplay " + fp.get(i)  + "\n");
						sb.append("Fattore casa " +  fattori.get(i)  + "\n");
						sb.append("Modulo orig " +  moduli.get(i)  + "\n");
						sb.append("Modulo " +  moduliS.get(i)  + "\n");
						sb.append("\n");
						if (squadraSimulata != null) {
							int index=0;
							for (Map<String, Object> map2 : players) {
								index++;
								//									sb.append(index + " ");
								if ((Boolean)map2.get("played")) {
									sb.append("<b>");
								}
								sb.append((String)map2.get("nome"));
								sb.append(" ");
								if ((Boolean)map2.get("played")) {
									sb.append("</b>");
									sb.append(map2.get("fantavoto"));
								}
								if ((boolean)map2.get("squadraGioca") == false) {
									sb.append("*");
								}
								sb.append(" ");
								List<Integer> bm=(List<Integer>) map2.get("bm");
								for (Integer integer : bm) {
									sb.append( desEvento(integer, campionatoFG) + " ");

								}
								if ((double)map2.get("malus") != 0) {
									sb.append(" MALUS: " + map2.get("malus"));
								}
								sb.append("\n");
							}
							sb.append("\n");
						}
						sb.append("Titolari");
						sb.append("\n");
						sb.append("Giocatori con voto: ");
						sb.append(squadra.getContaTitolari());
						sb.append(" (da giocare: " + squadra.getContaSquadraTitolariNonGioca() + ")");
						sb.append("\n");
						sb.append("Media votati: ");
						sb.append(squadra.getMediaTitolari());
//						sb.append("\n");
//						sb.append("Ancora da giocare: ");
//						sb.append(squadra.getContaSquadraTitolariNonGioca());
						sb.append("\n");
						sb.append("Totale: ");
						sb.append(squadra.getTotaleTitolari());
						sb.append("\n");
						sb.append("\n");
						sb.append("Riserve");
						sb.append("\n");
						sb.append("Giocatori con voto: ");
						sb.append(squadra.getContaRiserve());
						sb.append(" (da giocare: " + squadra.getContaSquadraRiserveNonGioca() + ")");
//						sb.append("\n");
//						sb.append("Ancora da giocare: ");
//						sb.append(squadra.getContaSquadraRiserveNonGioca());
						sb.append("\n\n");
						sb.append("Proiezione --> <b><i>");
						sb.append(totali.get(i));
						sb.append("</i></b>\n\n");
					}
				}
			}
		}
		ret.put("testo", sb.toString());
		ret.put("squadre", squadreKey);
		ret.put("squadreCasa", squadreCasa);
		ret.put("ALL", l);
		return ret;
	}

	private static void overrideFS(List<Squadra> squadre) throws Exception {
		//OVERRIDE VOTI FS
		List<Squadra> squadreFS = getVotiFS(Constant.GIORNATA, true);
		for (Squadra squadra : squadre) {
			for (Giocatore giocatore : squadra.getTitolari()) {
				for (Squadra squadraConVoto : squadreFS) {
					for (Giocatore giocatoreConVoto : squadraConVoto.getTitolari()) {
						if (giocatore.getNome().equals(giocatoreConVoto.getNome()) && giocatoreConVoto.getVoto()>0){
							giocatore.setVoto(giocatoreConVoto.getVoto());
						}
						if (giocatore.getNome().equals(giocatoreConVoto.getNome())){
							giocatore.setNumGol(giocatoreConVoto.getNumGol());
							giocatore.setEsce(giocatoreConVoto.isEsce());
							giocatore.setEntra(giocatoreConVoto.isEntra());
						}
					}
					for (Giocatore giocatoreConVoto : squadraConVoto.getRiserve()) {
						if (giocatore.getNome().equals(giocatoreConVoto.getNome()) && giocatoreConVoto.getVoto()>0){
							giocatore.setVoto(giocatoreConVoto.getVoto());
						}
						if (giocatore.getNome().equals(giocatoreConVoto.getNome())){
							giocatore.setNumGol(giocatoreConVoto.getNumGol());
							giocatore.setEsce(giocatoreConVoto.isEsce());
							giocatore.setEntra(giocatoreConVoto.isEntra());
						}
					}
				}
			}
			for (Giocatore giocatore : squadra.getRiserve()) {
				for (Squadra squadraConVoto : squadreFS) {
					for (Giocatore giocatoreConVoto : squadraConVoto.getTitolari()) {
						if (giocatore.getNome().equals(giocatoreConVoto.getNome()) && giocatoreConVoto.getVoto()>0){
							giocatore.setVoto(giocatoreConVoto.getVoto());
						}
						if (giocatore.getNome().equals(giocatoreConVoto.getNome())){
							giocatore.setNumGol(giocatoreConVoto.getNumGol());
							giocatore.setEsce(giocatoreConVoto.isEsce());
							giocatore.setEntra(giocatoreConVoto.isEntra());
						}
					}
					for (Giocatore giocatoreConVoto : squadraConVoto.getRiserve()) {
						if (giocatore.getNome().equals(giocatoreConVoto.getNome()) && giocatoreConVoto.getVoto()>0){
							giocatore.setVoto(giocatoreConVoto.getVoto());
						}
						if (giocatore.getNome().equals(giocatoreConVoto.getNome())){
							giocatore.setNumGol(giocatoreConVoto.getNumGol());
							giocatore.setEsce(giocatoreConVoto.isEsce());
							giocatore.setEntra(giocatoreConVoto.isEntra());
						}
					}
				}
			}
		}
	}
	public static Map<String, Object> proiezioneFS(String campionato, String nomePartitaSimulata) throws Exception{
		Map<String, Return> go = Main.go(true, null, null);
		Return return1 = go.get(campionato);
		StringBuilder testo = new StringBuilder();
		List<Squadra> squadre = return1.getSquadre();
		List<Squadra> sqFS=new ArrayList<>();
		String nomeSquadraCasa = reverseNickPlayer.get(nomePartitaSimulata.substring(2,nomePartitaSimulata.indexOf(" ",2))).substring(3);
		if (return1.getTipo().equalsIgnoreCase("FANTASERVICE")) {// &&  nomePartitaSimulata.startsWith("B")
			overrideFS(squadre);
			for (Squadra squadraTmp : squadre) {
				List<PartitaSimulata> partiteSimulate = squadraTmp.getPartiteSimulate();
				for (PartitaSimulata partitaSimulata : partiteSimulate) {
					if (partitaSimulata.getNome().equalsIgnoreCase(nomePartitaSimulata)) {
						sqFS.add(squadraTmp);
					}
				}
			}
			if (sqFS.size()>0) {
				defaultGiocatoriNonAncoraVotoFS(sqFS);
				applicaCambi(sqFS);
				if (sqFS.get(1).getNome().equals(nomeSquadraCasa)) {
					calcolaScontro(sqFS.get(1), sqFS.get(0), 1);
				} else {
					calcolaScontro(sqFS.get(0), sqFS.get(1), 1);
				}
			}
		}
		Map<String, Object> ret = new HashMap<>();
		Map<String, Object> map = new HashMap<>();
		map.put("ris", sqFS.get(0).getGolSimulazione() + "-" + sqFS.get(1).getGolSimulazione());
		ret.put("data", map);
		List<Map<String, Object>> teams = new ArrayList<>();
		for (Squadra squadra : sqFS) {
			Map<String, Object> team = new HashMap<>();
			team.put("nome",squadra.getNome());
			double tot=squadra.getTotale();
			team.put("bmd",squadra.getModificatoreDifesa());
			team.put("bmc",squadra.getModificatoreCentrocampo());
			team.put("bma",squadra.getModificatoreAttacco());
			if (squadra.getNome().equals(nomeSquadraCasa)) {
				team.put("fattore",2);
				tot=tot+2;
			}
			team.put("total",tot);
			List<Map<String, Object>> players = new ArrayList<>();
			for (Giocatore giocatore : squadra.getTitolari()) {
				Map<String, Object> player = new HashMap<>();
				player.put("nome", giocatore.getNome() + " (" + giocatore.getRuolo() + ")");
				//				player.put("rank","");
				player.put("played", true);
				player.put("malus", 0.0);
				player.put("totBM", 0.0);
				player.put("bm", giocatore.getCodEventi());
				player.put("voto", giocatore.getVoto());
				player.put("capitano", false);
				player.put("viceCapitano", false);
				player.put("fantavoto", giocatore.getVoto() + giocatore.getModificatore());
				player.put("squadraGioca", giocatore.isSquadraGioca());
				players.add(player);
			}
			for (Giocatore giocatore : squadra.getRiserve()) {
				Map<String, Object> player = new HashMap<>();
				player.put("nome", giocatore.getNome() + " (" + giocatore.getRuolo() + ")");
				//				player.put("rank","");
				player.put("played", false);
				player.put("malus", 0.0);
				player.put("totBM", 0.0);
				player.put("bm", giocatore.getCodEventi());
				player.put("voto", giocatore.getVoto());
				player.put("capitano", false);
				player.put("viceCapitano", false);
				player.put("fantavoto", giocatore.getVoto() + giocatore.getModificatore());
				player.put("squadraGioca", giocatore.isSquadraGioca());
				players.add(player);
			}
			team.put("players", players);
			teams.add(team);
		}
		map.put("teams", teams);
		Instant instant = Instant.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
		ZoneId zoneId = ZoneId.of( "Europe/Rome" );
		ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
		String time=zdt.format(formatter);
		upsertSalva(time + "-" + "simulaFS" + "-" + campionato + "-" + nomePartitaSimulata, toJson(ret));
		return ret;
	}

	public static List<Salva> proiezioneFG_name(String lega, List<Squadra> squadre, String sfide, String squadraSimulata) throws Exception{
		String sq="";
		for (Squadra squadra : squadre) {
			sq=sq+";" + squadra.getNome();
		}
		String x="%-" + "simulaFG" + "-" + sfide + "-" + lega + "-" + sq + "-" + (squadraSimulata==null?"":squadraSimulata);
		List<Salva>  ret =salvaRepository.findSimulazioniName(x);
		return ret;
	}

	public static List<Salva> proiezioneFS_name(String campionato, String nomePartitaSimulata) {
		String x="%-" + "simulaFS" + "-" + campionato + "-" + nomePartitaSimulata;
		List<Salva>  ret =salvaRepository.findSimulazioniName(x);
		return ret;
	}

	public static String getDettaglio(Long chatId, String campionato, String squadra, String casa, String nomePartitaSimulata, boolean soloLive) throws Exception{
		try {
			Map<String, Return> go = Main.go(true, null, null);
			Return return1 = go.get(campionato);
			StringBuilder testo = new StringBuilder();
			List<Squadra> squadre = return1.getSquadre();
			List<Squadra> sqFS=new ArrayList<>();
			if (return1.getTipo().equalsIgnoreCase("FANTASERVICE") &&  nomePartitaSimulata.startsWith("B")) {
				overrideFS(squadre);
				for (Squadra squadraTmp : squadre) {
					List<PartitaSimulata> partiteSimulate = squadraTmp.getPartiteSimulate();
					for (PartitaSimulata partitaSimulata : partiteSimulate) {
						if (partitaSimulata.getNome().equalsIgnoreCase(nomePartitaSimulata)) {
							sqFS.add(squadraTmp);
						}

					}
				}
				if (sqFS.size()>0) {
					defaultGiocatoriNonAncoraVotoFS(sqFS);
					applicaCambi(sqFS);
					calcolaScontro(sqFS.get(0), sqFS.get(1), 1);
				}
			}
			if (return1.getTipo().equalsIgnoreCase("FANTAGAZZETTA") && !nomePartitaSimulata.equals("-")) {
				Map<String, Object> partitaSimulata = getPartitaSimulata(chatId,nomePartitaSimulata,squadra);
				testo = new StringBuilder(partitaSimulata.get("testo").toString()) ;
				return testo.toString(); 
			}
			else {
				for (Squadra sq : squadre) {
					if (sq.getNome().equalsIgnoreCase(squadra)) {
						testo
						.append("\n<b>")
						.append(sq.getNome())
						.append("</b>\n");
						for (Giocatore giocatore : sq.getTitolari()) {
							dettaglioTestoGiocatore(testo, giocatore,campionato, soloLive);
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
							dettaglioTestoGiocatore(testo, giocatore,campionato, soloLive);
						}
						testo
						.append("\n")
						.append("Giocatori con voto: ")
						.append(sq.getContaRiserve())
						.append("\n")
						.append("Ancora da giocare: ")
						.append(sq.getContaSquadraRiserveNonGioca())
						.append("\n\n");
						if (nomePartitaSimulata != null && !nomePartitaSimulata.equals("-")) {
							testo.append("Cambi simulati: ")
							.append(sq.getContaCambioSimulato())
							.append("\n");
							testo.append("Modificatore Difesa: ")
							.append(sq.getModificatoreDifesa())
							.append("\n");
							testo.append("Modificatore Centrocampo: ")
							.append(sq.getModificatoreCentrocampo())
							.append("\n");
							testo.append("Modificatore Attacco: ")
							.append(sq.getModificatoreAttacco())
							.append("\n");
							testo.append("GOL: ")
							.append(sq.getGolSimulazione())
							.append("\n\n");
							testo.append("Totale --> <b><i>")
							.append(casa.equalsIgnoreCase("S")?sq.getTotale()+2:sq.getTotale())
							.append("</i></b>\n\n");
						}else {
							testo.append("Proiezione --> <b><i>")
							.append(casa.equalsIgnoreCase("S")?sq.getProiezione()+2 + "(*)":sq.getProiezione())
							.append("</i></b>\n\n");
						}
						if(chatId.intValue() == Constant.CHAT_ID_FANTALIVE.intValue()) {
							testo
							.append("\n")
							.append(Main.getUrlNotifica());
						}

						//						logger.error("@getDettaglio " + "@nome: " + sq.getNome() + "@titolari: " + sq.getTitolari() + "@TT: " + sq.getTotaleTitolari()
						//						+ "@MD:" + sq.getModificatoreDifesa()  + "@MC: " + sq.getModificatoreCentrocampo()  + "@MA: " +  sq.getModificatoreAttacco()  
						//						+ "@MAL: " + sq.getMalusFormazioneAutomatica() + "@PR: " + sq.getProiezione() + "@Casa: " + casa);

						return testo.toString(); 
					}
				}
			}
			return ""; 
		}
		catch (Exception e ) {
			throw e;
		}
	}

	private static void defaultGiocatoriNonAncoraVotoFS(List<Squadra> sqFS) {
		for (Squadra squadra : sqFS) {
			List<Giocatore> P_daCambiare = new ArrayList<>();
			List<Giocatore> D_daCambiare = new ArrayList<>();
			List<Giocatore> C_daCambiare = new ArrayList<>();
			List<Giocatore> A_daCambiare = new ArrayList<>();
			for (Giocatore giocatore : squadra.getTitolari()) {
				if (giocatore.isSquadraGioca() && giocatore.getVoto() == 0) {
					giocatore.setEsce(true);
				}
				if (giocatore.isEsce()) {

					if (giocatore.getRuolo().equals("P")) {
						P_daCambiare.add(giocatore);
					}
					if (giocatore.getRuolo().equals("D")) {
						D_daCambiare.add(giocatore);
					}
					if (giocatore.getRuolo().equals("C")) {
						C_daCambiare.add(giocatore);
					}
					if (giocatore.getRuolo().equals("A")) {
						A_daCambiare.add(giocatore);
					}
				}
				if (!giocatore.isSquadraGioca() && giocatore.getVoto() == 0) {
					giocatore.setVoto(6);
					giocatore.setNumGol(0);
					giocatore.setCambioSimulato(true);
				}
			}
			for (Giocatore giocatore : squadra.getRiserve()) {
				if (!giocatore.isSquadraGioca() && giocatore.getVoto() == 0) {
					giocatore.setVoto(6);
					giocatore.setNumGol(0);
				}
				if (giocatore.getVoto() != 0) {
					if (giocatore.getRuolo().equals("P") && P_daCambiare.size()>0) {
						giocatore.setEntra(true);
						P_daCambiare.remove(0);
					}
					if (giocatore.getRuolo().equals("D") && D_daCambiare.size()>0) {
						giocatore.setEntra(true);
						D_daCambiare.remove(0);
					}
					if (giocatore.getRuolo().equals("C") && C_daCambiare.size()>0) {
						giocatore.setEntra(true);
						C_daCambiare.remove(0);
					}
					if (giocatore.getRuolo().equals("A") && A_daCambiare.size()>0) {
						giocatore.setEntra(true);
						A_daCambiare.remove(0);
					}

				}
			}
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
	private static void dettaglioTestoGiocatore(StringBuilder testo, Giocatore giocatore, String campionato, boolean soloLive) {
		boolean visGioc=true;
		if (soloLive) {
			if (giocatore.getOrario().get("tag").equalsIgnoreCase("Postponed") || giocatore.getOrario().get("tag").equalsIgnoreCase("FullTime") 
					|| giocatore.getOrario().get("tag").equalsIgnoreCase("PreMatch")) {
				visGioc=false;
			}
			if (giocatore.getCodEventi().contains(14)) {
				visGioc=false;
			}
		}
		if (visGioc) {
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

			if (giocatore.isNonGioca()) {
				testo
				.append(" NON GIOCA ")
				.append("  ");
			}else {
				for (Integer evento : giocatore.getCodEventi()) {
					testo
					.append(desEvento(evento,campionato))
					.append("  ");
				}
			}
			testo
			.append("\n");
		}
	}
	private static String getVoto(Giocatore g) {
		if (!g.isSquadraGioca()) return " ";
		if (g.getVoto()==0) return "NV";
		return String.valueOf(g.getVoto());
	}
	private static String getFantaVoto(Giocatore g) {
		if (!g.isSquadraGioca()) return " ";
		if (g.getVoto()==0) return "NV";
		return String.format("%.2f", g.getVoto()+g.getModificatore());
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
	public static String desEvento(Integer ev,String r){
		String[] evento = Main.eventi.get(ev);
		String iconaEvento = evento[6];
		String ret = " " + iconaEvento + " " + evento[0];
		Double valEvento = valEvento(evento,r);
		if (!valEvento.equals(new Double(0))) {
			ret = ret + " (" + valEvento + ") "; 
		}
		return ret;
	}
	private static Double valEvento(String[] evento,String r){
		int pos=0;
		if (r.equals(Constant.Campionati.FANTAVIVA.name())) pos=1;
		if (r.equals(Constant.Campionati.LUCCICAR.name())) pos=2;
		if (r.equals(Constant.Campionati.BE.name())) pos=3;
		if (r.equals(Constant.Campionati.JB.name())) pos=4;
		return Double.parseDouble(evento[pos]);
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
						sb.append(proiezioneSquadra(squadra,squadra.isCasaProiezione(), false));
						squadreRet.add(key + "-" + squadra.getNome());
					}

				}
			}
		}
		ret.put("testo", sb.toString());
		ret.put("squadre", squadreRet);
		return ret;
	}

	private static StringBuilder proiezioneSquadra(Squadra squadra, boolean casa, boolean conModificatori) {
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
		.append(" (da giocare: " + squadra.getContaSquadraTitolariNonGioca() + ")")
		.append("\n")
		.append("Media votati: ")
		.append(squadra.getMediaTitolari())
//		.append("\n")
//		.append("Ancora da giocare: ")
//		.append(squadra.getContaSquadraTitolariNonGioca())
		.append("\n")
		.append("Totale: ")
		.append(squadra.getTotaleTitolari())
		.append("\n")
		.append("\n")
		.append("Riserve")
		.append("\n")
		.append("Giocatori con voto: ")
		.append(squadra.getContaRiserve())
		.append(" (da giocare: " + squadra.getContaSquadraRiserveNonGioca() + ")")
//		.append("\n")
//		.append("Ancora da giocare: ")
//		.append(squadra.getContaSquadraRiserveNonGioca())
		.append("\n\n");
		if (conModificatori) {
			sb.append("Cambi simulati: ")
			.append(squadra.getContaCambioSimulato())
			.append("\n");
			sb.append("Modificatore Difesa: ")
			.append(squadra.getModificatoreDifesa())
			.append("\n");
			sb.append("Modificatore Centrocampo: ")
			.append(squadra.getModificatoreCentrocampo())
			.append("\n");
			sb.append("Modificatore Attacco: ")
			.append(squadra.getModificatoreAttacco())
			.append("\n");
			sb.append("GOL: ")
			.append(squadra.getGolSimulazione())
			.append("\n\n");
			sb.append("Totale --> <b><i>")
			.append(squadra.getTotale()+iCasa)
			.append("</i></b>\n\n");

		}else {
			sb.append("Proiezione --> <b><i>")
			.append(squadra.getProiezione()+iCasa)
			.append("</i></b>\n\n");
		}

		//		logger.error("@proiezioneSquadra " + "@nome: " + squadra.getNome() + "@titolari: " + squadra.getTitolari() + "@TT: " + squadra.getTotaleTitolari()
		//		+ "@MD:" + squadra.getModificatoreDifesa()  + "@MC: " + squadra.getModificatoreCentrocampo()  + "@MA: " +  squadra.getModificatoreAttacco()  
		//		+ "@MAL: " + squadra.getMalusFormazioneAutomatica() + "@PR: " + squadra.getProiezione() + "@Casa: " + iCasa);

		return sb;
	}
	public static Map<String, Object> getOldSnapPartite(boolean live) throws Exception {
		Map<String, Object> ret = new LinkedHashMap<>();
		StringBuilder sb = new StringBuilder();
		if (oldSnapPartite.isEmpty()) {
			getLives(constant.LIVE_FROM_FILE);
		}
		List<String> sqRet=new ArrayList<>();
		oldSnapPartite.forEach((k,partita) -> {
			String tag = (String) partita.get("tag");
			Integer iTag = statusMatch.get(tag);
			if (! live || (live && (iTag==1 || iTag==2 || iTag==3))) {
				sb.append(visSnapPartita(sqRet, k, partita));
			}
		});
		ret.put("testo", sb.toString());
		ret.put("squadre", sqRet);
		return ret;
	}
	private static String visSnapPartita(List<String> sqRet, String keyPartita, Map<String, Object> partita) {
		StringBuilder sb=new StringBuilder();
		String tag = (String) partita.get("tag");
		String val = (String) partita.get("val");
		if (tag.equalsIgnoreCase("PreMatch")) {
			ZonedDateTime zoneDateTime = ZonedDateTime.parse(val, DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC")));
			val = zoneDateTime.format(DateTimeFormatter.ofPattern("E dd/MM/yyyy HH:mm").withZone(ZoneId.of("Europe/Rome")));
		}
		Map<ZonedDateTime, Map<String, String>> reti = new TreeMap<>();
		StringBuilder risultato=new StringBuilder();
		final String second_half_start=(String) partita.get("second_half_start");
		StringBuilder sepGol=new StringBuilder("-");
		partita.forEach((p, v) -> {  
			if (!p.toString().equals("tag") && !p.toString().equals("val") && !p.toString().equals("first_half_stop") && !p.toString().equals("second_half_start")) {
				risultato.append(((Map)v).get("gol"));
				risultato.append(sepGol);
				if (sepGol.toString().equals("-")) {
					sepGol.replace(0, 1, " ");
				} 
				List<Map> r = (List<Map>) ((Map)v).get("RETI");
				sqRet.add(p.toString());
				for (Map map : r) {
					String tipo = (String) map.get("tipo");
					String goalTimestamp = (String) map.get("goalTimestamp");
					ZonedDateTime parseZDTGoal = ZonedDateTime.parse(goalTimestamp,  DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssZ"));
					String minuto = (String) map.get("minuto");
					String giocatore = (String) map.get("giocatore");
					String squadra = p.toString();
					Map<String, String> dati = new LinkedHashMap<>();
					dati.put("tipo", tipo);
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");
					dati.put("goalTimestamp", parseZDTGoal.format(formatter));
					dati.put("giocatore", giocatore);
					dati.put("squadra", squadra);
					ZonedDateTime parseZDTSecondHalfStart=null;
					if (second_half_start != null) {
						parseZDTSecondHalfStart = ZonedDateTime.parse(second_half_start,  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssz"));
					}
					if (parseZDTSecondHalfStart == null || parseZDTGoal.isBefore(parseZDTSecondHalfStart)) {
						minuto = minuto + " PT";
					} else {
						minuto = minuto + " ST";
					}
					dati.put("minuto", minuto);
					reti.put(parseZDTGoal, dati);
				}
			}
		});
		sb.append(keyPartita + "\t" + risultato + "\n\t" + tag + " " + (val.equals("N/A")?"":val) + "\n" );
		reti.forEach((minuto,dati) -> {
			sb.append("\t" + 
					dati.get("minuto") + " " + dati.get("squadra") + " " + dati.get("tipo") + " " + dati.get("giocatore") + "\n");
		});
		sb.append("\n");
		return sb.toString();
	}
	public static Map<String, String> visKeepAliveEnd() throws Exception {
		String visKeepAlive = "N";
		if (Constant.KEEP_ALIVE_END.isAfter(ZonedDateTime.now())) {
			visKeepAlive="S";
		}
		Map<String, String> ret = new HashMap<String, String>();
		ret.put("VIS_KEEP_ALIVE", visKeepAlive);
		return ret;
	}
	public static Map<String, Object> setKeepAliveEnd(Map<String, Object> body) throws Exception {
		Map<String, Object> ret = new HashMap<String, Object>();
		boolean verso = (boolean) body.get("verso");
		if (verso) {
			constant.KEEP_ALIVE_END=ZonedDateTime.now().withHour(23).withMinute(0).withSecond(0).withZoneSameLocal(ZoneId.of("Europe/Rome"));
		} else {
			constant.KEEP_ALIVE_END=ZonedDateTime.now().plusHours(-1);
		}
		String visKeepAlive = "N";
		ZonedDateTime now = ZonedDateTime.now();
		if (Constant.KEEP_ALIVE_END.isAfter(now)) {
			visKeepAlive="S";
		}
		Main.toSocket.put("visKeepAlive", visKeepAlive);
		ret.put("KEEP_ALIVE_END", constant.KEEP_ALIVE_END);
		ret.put("VIS_KEEP_ALIVE", visKeepAlive);
		socketHandlerFantalive.invia(Main.toSocket );
		return ret;
	}

	public static void calcolaScontro(Squadra squadra1,Squadra squadra2, int ggDaCalcolare) {
		String nome1 = squadra1.getNome();
		String nome2 = squadra2.getNome();
		if (nome1.startsWith("Jonny") && nome2.startsWith("C.") && ggDaCalcolare==2) {
			//							System.out.println();
		}
		calcolaModificatoreDifesa(squadra1,squadra2);
		calcolaModificatoreDifesa(squadra2,squadra1);
		calcolaModificatoreCentrocampo(squadra1, squadra2);
		calcolaModificatoreAttacco(squadra1);
		calcolaModificatoreAttacco(squadra2);

		int iGolCasa = getGol(squadra1.getTotale()+Constant.ICASA);
		int iGolTrasferta = getGol(squadra2.getTotale());
		if (iGolCasa > 0 && iGolCasa == iGolTrasferta && ( Math.abs(squadra1.getTotale() +Constant.ICASA - squadra2.getTotale()) >= 4))//FIXME BUG
		{
			/*
	Scarto stessa fascia 4
	Il valore numerico di questo fattore � pari a 4 punti. Si applica nella seguente maniera: se i punteggi delle due squadre si trovano nella stessa fascia di gol,
	 affinch� chi ha totalizzato il punteggio pi� alto vinca la partita � necessario che lo scarto tra i punteggi sia maggiore o uguale allo "scarto stessa fascia". In tal caso viene assegnato un gol in pi� alla squadra con punteggio pi� alto. In caso contrario la partita finisce in pareggio.
	Esempi:
	66 - 71. Entrambe i punteggi ricadono nella fascia di 1 gol. Con le fasce rigide la partita finirebbe 1-1. Utilizzando il fattore in questione, poich� lo scarto dei punteggi � pari a 5 >= 4 (scarto stessa fascia), la partita finisce 1-2.
	67 - 70. Entrambe i punteggi ricadono nella fascia di 1 gol. Con le fasce rigide la partita finirebbe 1-1. Anche utilizzando il fattore in questione, poich� lo scarto dei punteggi � pari a 3 <= 4 (scarto stessa fascia), la partita finisce 1-1.
			 */		
			if (squadra1.getTotale() + Constant.ICASA > squadra2.getTotale())
			{
				iGolCasa++;
			}
			else
			{
				iGolTrasferta++;
			}
		}

		if (iGolCasa != iGolTrasferta && ( Math.abs(squadra1.getTotale() + Constant.ICASA - squadra2.getTotale()) <= 1))
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
			if (squadra1.getTotale() + Constant.ICASA < squadra2.getTotale())
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
//			+ "\tTot: " + new BigDecimal(squadra1.getTotale(), MathContext.DECIMAL128).setScale(2,BigDecimal.ROUND_HALF_UP).add(new BigDecimal(ICASA)) + "\n" 
//			+ "\tMod Difesa: " + squadra1.getModificatoreDifesa() + "\n" 
//			+ "\tMod Centrocampo: " + squadra1.getModificatoreCentrocampo() + "\n" 
//			+ "\tMod Attacco: " + squadra1.getModificatoreAttacco() + "\n" 
//			+ "\tMalus formazione automatica: " + squadra1.getMalusFormazioneAutomatica() + "\n" 
			+ nome2 + " --> " + iGolTrasferta + "\n"
//			+ "\tTot: " + new BigDecimal(squadra2.getTotale(), MathContext.DECIMAL128).setScale(2,BigDecimal.ROUND_HALF_UP).add(new BigDecimal("0")) + "\n"
//			+ "\tMod Difesa: " + squadra2.getModificatoreDifesa() + "\n" 
//			+ "\tMod Centrocampo: " + squadra2.getModificatoreCentrocampo() + "\n" 
//			+ "\tMod Attacco: " + squadra2.getModificatoreAttacco() + "\n" 
//			+ "\tMalus formazione automatica: " + squadra2.getMalusFormazioneAutomatica() + "\n" 
			);
	;
		 */
		squadra1.setGolSimulazione(iGolCasa);
		squadra2.setGolSimulazione(iGolTrasferta);
	}
	private static void calcolaModificatoreCentrocampo(Squadra squadra1, Squadra squadra2) {
		BigDecimal sommaC1 = generaCentrocampisti(squadra1);
		BigDecimal sommaC2 = generaCentrocampisti(squadra2);
		applicaModificatoreCentrocampo(squadra1, sommaC1, sommaC2);
		applicaModificatoreCentrocampo(squadra2, sommaC2, sommaC1);

	}
	private static void calcolaModificatoreAttacco(Squadra squadra) {
		//A no goal
		//<6.5 0
		//ogni 0,5 = +0,5 (max 2)
		//quindi se > 6.5 = round( voto - 6) con MAX 2
		BigDecimal ret=new BigDecimal(0);
		List<Giocatore> titolari = squadra.getTitolari();
		for (Giocatore giocatore : titolari) {
			if (giocatore.getRuolo().equalsIgnoreCase("A") && giocatore.getNumGol()==0) {
				BigDecimal voto = new BigDecimal(Double.toString(giocatore.getVoto()));
				if (voto.compareTo(new BigDecimal(6.5))>=0) {
					double add = Math.floor(voto.subtract(new BigDecimal(6)).doubleValue() * 2) / 2;
					if (add>2) {
						add=2;
					}
					ret=ret.add(new BigDecimal(add));
				}

			}
		}
		squadra.setModificatoreAttacco(ret.doubleValue());
	}
	private static void calcolaModificatoreDifesa(Squadra squadra1, Squadra squadra2) {
		//Media aritmetica voti P e D
		//Voto < 5 =2
		//ogni 0,25 = -0.5 
		//Difesa a 3 = +0.5 - Difesa a 5 = -0.5

		BigDecimal ret=new BigDecimal("0");
		List<Giocatore> titolari = squadra1.getTitolari();
		for (Giocatore giocatore : titolari) {
			if (giocatore.getRuolo().equalsIgnoreCase("P") || giocatore.getRuolo().equalsIgnoreCase("D")) {
				ret=ret.add(new BigDecimal(Double.toString(giocatore.getVoto())));
			}
		}

		int iContaTitolariOriginali=0;
		for(Giocatore giocatore : squadra1.getTitolariOriginali()) {
			if (giocatore.getRuolo().equalsIgnoreCase("P") || giocatore.getRuolo().equalsIgnoreCase("D")) {
				iContaTitolariOriginali++;
			}
		}

		BigDecimal media = ret.divide(new BigDecimal(iContaTitolariOriginali), 2, RoundingMode.HALF_UP);
		BigDecimal cap=new BigDecimal(5);
		BigDecimal voto=new BigDecimal(2);

		while (media.compareTo(cap)>=0) {
			cap=cap.add(new BigDecimal(0.25));
			voto=voto.add(new BigDecimal(-0.5));
		}
		if (iContaTitolariOriginali-1<4) {
			voto=voto.add(new BigDecimal(0.5));
		}
		if (iContaTitolariOriginali-1>4) {
			voto=voto.add(new BigDecimal(-0.5));
		}
		squadra2.setModificatoreDifesa(voto.doubleValue());
	}
	private static void applicaModificatoreCentrocampo(Squadra squadra, BigDecimal sommaC1, BigDecimal sommaC2) {
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

	private static BigDecimal generaCentrocampisti(Squadra squadra) {
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

	public static void applicaCambi(List<Squadra> squadre) {
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
	}

}
