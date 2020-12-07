package scrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
	private static final String SPONTIT_USERID = "daniele_carlucci6695";
	private static final String SPONTIT_KEY = "AHWBE7T65FG8ED9N7OTV3D84P6G8YESOUJ4DGP25IW7P9DEDGETVF24EYMH63O9H8ZWB6Y37Q1IIFP4AV8ZNW5DYF7FNPRPAYWHB";
	private static final String PUSHOVER_USERKEY = "uw954kdfx5t6osgzc2ui24qi1zhhw3";
	private static final String PUSHOVER_TOKEN = "agg59xr6jtavbnmnpt5b97nino25u2";
	private static final String ID_ICONA_NOTIFICA = "170";
	private static final String URL_NOTIFICA = "http://192.168.1.83:7080/fantalive-0.0.1-SNAPSHOT/";
	private static final String KEY_PUSHSAFER = "fjEebDC2MDAFaFNU3Ndx";
	private static final String APPKEY_FG = "4ab27d6de1e92c810c6d4efc8607065a735b917f";
	public static final int DELTA_VIVA_FG=2;
	public static final int DELTA_LUCCICAR_FG=3;
	public static final String COMP_VIVA_FG = "250964";
	public static final String COMP_LUCCICAR_FG = "306919";

	public static String AUTH_FS = "E4919FA22B99D77B3C784F5E241F2BE9516813B2E51B4EE3EA5A1F33F2D2EC684C010AB311B58BB8B5178A800AE0CF12AFF5AF83C37F983B6B81997CE046DC5AC2D2E126D7CBFD41F72D61BC94A9A25E79787E8FFFB059C8985A720964804B6545F525324C27961054EEA49DC8BBAA2963190E3E";
	public static final int DELTA_FS=3;
	static final int NUM_PARTITE_FS = 4;
	public static final String COMP_FS = "123506";
	private static final int PRIMA_GIORNATA_FS = 10675622;

	private static final String SPORT_ID_LIVE_GAZZETTA = "1";
	private static final String COMP_ID_LIVE_GAZZETTA = "21";
	private static final String I_LIVE_FANTACALCIO = "15";

	public static int GIORNATA = 10;
	public static final String ROOT="/tmp/";
	
	
	static Map<Integer, String> sq=null;
	static HashMap<Integer, String[]> eventi=null;
	static List<ConfigCampionato> files=null;
	public static List<String> sqBeDaEscluedere= new ArrayList<String>();
	static List<String> sqBeCaricate=null;
	public static List<String> sqDaEv= null;
	static Map<String, Giocatore> oldSnapshot=null;

//	static Map<String, List<Squadra>>squadre=new HashMap<String, List<Squadra>>();
	static ObjectMapper mapper;
	public static Map<String, String> keyFG=null;
	private static Map<String, Map<String, String>> orari=null;


	public static void init() throws Exception {
		if (sqDaEv==null) inizializzaSqDaEv();
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		sqBeCaricate=new ArrayList<String>();
		if (eventi ==null) {
			eventi = new HashMap<Integer, String[]>();
			eventi.put(1000, new String[] {"portiere imbattuto","1","1","1","S"});
			eventi.put(22, new String[] {"assist hight","1.5","1","1","S"});
			eventi.put(11, new String[] {"gol vittoria","0","0","0","N"});
			eventi.put(12, new String[] {"gol pareggio","0","0","0","N"});
			eventi.put(24, new String[] {"assist medium1","1","1","1","S"});
			eventi.put(14, new String[] {"uscito","0","0","0","S"});
			eventi.put(15, new String[] {"entrato","0","0","0","S"});
			eventi.put(16, new String[] {"gol annullato","0","0","0","S"});
			eventi.put(17, new String[] {"infortunio","0","0","0","N"});
			eventi.put(1, new String[] {"ammonito","-0.5","-0.5","-0.5","S"});
			eventi.put(2, new String[] {"espulso","-1","-1","-1","S"});
			eventi.put(3, new String[] {"gol","3","3","3","S"});
			eventi.put(4, new String[] {"gol subito","-1","-1","-1","S"});
			eventi.put(7, new String[] {"rigore parato","3","3","3","S"});
			eventi.put(8, new String[] {"rigore sbagliato","-3","-3","-3","S"});
			eventi.put(9, new String[] {"rigore segnato","3","3","2","S"});
			eventi.put(20, new String[] {"assist low","0.5","1","1","S"});
			eventi.put(21, new String[] {"assist medium2","1","1","1","S"});
			eventi.put(10, new String[] {"autogol","-2","-3","-3","S"});
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
		}
		if (files==null) {
			files = new ArrayList<ConfigCampionato>();
			files.add(new ConfigCampionato(24,"FANTAGAZZETTA","luccicar"));
			files.add(new ConfigCampionato(22,"FANTAGAZZETTA","fantaviva"));
			files.add(new ConfigCampionato(22,"FANTASERVICE","be"));
		}
		partiteLive();
	}

	public static void snapshot(SocketHandler socketHandler) throws Exception {
    	Map<String, Return> go = go(true,true);
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
						snapshot.put(r.getCampionato() + "-" + squadra.getNome() + "-" + giocatore.getNome(), giocatore);
					}
					for (Giocatore giocatore : squadra.getRiserve()) {
						snapshot.put(r.getCampionato() + "-" + squadra.getNome() + "-" + giocatore.getNome(), giocatore);
					}
				}
			}
    	}
    	
    	if (oldSnapshot!=null) {
    		Iterator<String> iterator = oldSnapshot.keySet().iterator();
    		Map<String, Map<String,List<Notifica>>> notifiche = new HashMap();
    		while (iterator.hasNext()) {
				String key = (String) iterator.next();
				Giocatore oldGioc = oldSnapshot.get(key);
				Giocatore newGioc = snapshot.get(key);
				List<Integer> findNuoviEventi = findNuoviEventi(oldGioc, newGioc);
				List<String> eventi=new ArrayList<String>();
				if (!oldGioc.getOrario().get("tag").equalsIgnoreCase(newGioc.getOrario().get("tag"))) {
					eventi.add(newGioc.getOrario().get("tag"));
				}
				if (findNuoviEventi.size()>0 || !oldGioc.getOrario().get("tag").equalsIgnoreCase(newGioc.getOrario().get("tag"))) {
					for (Integer integer : findNuoviEventi) {
						eventi.add(Main.eventi.get(integer)[0]);
					}
				}
				if (eventi.size()>0) {
					String[] splitKey = key.split("-");
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
					notifica.setSquadra(splitKey[1]);
					notifica.setGiocatore(splitKey[2]);
					notifica.setId(newGioc.getIdGioc());
					notifica.setEventi(eventi);
					notifica.setVoto(newGioc.getVoto());
					if (newGioc.isCambio()) {
						notifica.setCambio("(*)");
					}
				}
			}
    		Set<String> keySet = notifiche.keySet();
    		if (keySet!= null && keySet.size()>0) {
        		StringBuilder des = new StringBuilder();
    			for (String camp : keySet) {
					des.append(camp).append(":\r");
					Map<String, List<Notifica>> sq = notifiche.get(camp);
					Iterator<String> itSq = sq.keySet().iterator();
					while (itSq.hasNext()) {
						String sqN = (String) itSq.next();
						des.append("\t").append(sqN).append(":\r");
						List<Notifica> listN = sq.get(sqN);
						for (Notifica notifica : listN) {
							des.append("\t\t").append(notifica.toString()).append("\r");
						}
					}
				}
        		Main.inviaNotifica(des.toString());
    		}
    	}
    	oldSnapshot=snapshot;
    	if (socketHandler != null) {
    		socketHandler.invia(toJson(go));
    	}
	}
	
	
	public static void main(String[] args) throws Exception {
		Iterator<String> iterator;
    	snapshot(null);
    	snapshot(null);
		iterator = oldSnapshot.keySet().iterator();
		while (iterator.hasNext()) {
			String k = (String) iterator.next();
			Giocatore giocatore = oldSnapshot.get(k);
			if (giocatore.getNome().toUpperCase().startsWith("MILI")){
				giocatore.getOrario().put("tag","xx");
				giocatore.getCodEventi().add(3);
			}
		}
    	snapshot(null);
		iterator = oldSnapshot.keySet().iterator();
		while (iterator.hasNext()) {
			String k = (String) iterator.next();
			Giocatore giocatore = oldSnapshot.get(k);
			if (giocatore.getNome().toUpperCase().startsWith("MILI")){
				giocatore.getCodEventi().add(11);
				giocatore.getCodEventi().add(3);
			}
		}
    	snapshot(null);
		iterator = oldSnapshot.keySet().iterator();
		while (iterator.hasNext()) {
			String k = (String) iterator.next();
			Giocatore giocatore = oldSnapshot.get(k);
			if (giocatore.getNome().toUpperCase().startsWith("MILI")){
				giocatore.getCodEventi().add(16);
			}
		}
    	snapshot(null);
		iterator = oldSnapshot.keySet().iterator();
		while (iterator.hasNext()) {
			String k = (String) iterator.next();
			Giocatore giocatore = oldSnapshot.get(k);
			if (giocatore.getNome().toUpperCase().startsWith("MILI")){
				giocatore.getCodEventi().add(14);
			}
		}
    	snapshot(null);
	}
	
	public static void inviaNotifica(String msg) throws Exception {
		String urlNotifica;
		Map<String, String> body;
		Map<String, String> headers;
		if (false) {
			urlNotifica = "https://www.pushsafer.com/api?k=" + KEY_PUSHSAFER + "&ut=FantaLive&m=" + URLEncoder.encode(msg, StandardCharsets.UTF_8.toString()) + "&i=" + ID_ICONA_NOTIFICA + "&u=" + URL_NOTIFICA;
			body = new HashMap<String, String>();
			postHTTP(urlNotifica,body,headers);
		}
		if (false) {
			urlNotifica="https://api.pushover.net/1/messages.json";
			body = new HashMap<String, String>();
			body.put("token", PUSHOVER_TOKEN);
			body.put("user", PUSHOVER_USERKEY);
			body.put("message", msg);
			postHTTP(urlNotifica,body,headers);
		}
		if (false) {
			System.out.println(msg);
		}
		if(true) {
			urlNotifica = "https://api.spontit.com/v3/push";
			body = new HashMap<String, String>();
			body.put("pushTitle", "FantaLive");
			//body.put("subtitle", "Aggiornamento");
			body.put("content", msg);
			body.put("link", URL_NOTIFICA);

//			body.put("channelName", "daniele");
			//body.put("schedule", 1591982947);
			//body.put("expirationStamp", 1592414947);
			//body.put("openLinkInApp", "true");
			//body.put("iOSDeepLink", "photos-redirect://");


			headers = new HashMap<String, String>();
			headers.put("X-Authorization", SPONTIT_KEY);
			headers.put("X-UserId", SPONTIT_USERID);
			postHTTP(urlNotifica,body, headers);
			
		}
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
			Element element = select.get(i);
			String link = element.select("A").attr("href");
			link=link.substring(link.lastIndexOf("=")+1);
			ret.put(link,squadra);
		}
		return ret;
	}

	public static void scaricaBe() throws IOException, ClientProtocolException {
		BasicCookieStore cookieStore = new BasicCookieStore();
		BasicClientCookie cookie;
		cookie = new BasicClientCookie("FantaSoccer_Auth", AUTH_FS);
		cookie.setDomain("www.fanta.soccer");
		cookie.setPath("/");
		cookieStore.addCookie(cookie);
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		try {
			for (int i=0;i<4;i++) {
				HttpGet httpget = new HttpGet("https://www.fanta.soccer/it/lega/privata/" + COMP_FS + "/dettaglipartita/" + String.valueOf(GIORNATA-DELTA_FS) + "/" + String.valueOf(i + PRIMA_GIORNATA_FS + (NUM_PARTITE_FS*(GIORNATA-DELTA_FS))) + "/");
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
				Files.write(Paths.get(ROOT + "be" + i + ".html"), responseBody.getBytes());
			}
		} finally {
			httpclient.close();
		}
	}



	private static void partiteLive() throws Exception {
		orari=new HashMap<String, Map<String,String>>();
		String callHTTP = getHTTP("https://api2-mtc.gazzetta.it/api/v1/sports/calendar?sportId=" + SPORT_ID_LIVE_GAZZETTA + "&competitionId=" + COMP_ID_LIVE_GAZZETTA);
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
	}

	public static String postHTTP(String url, Map<String, String> body, Map<String, String>... headers) throws Exception {
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
		postConnection.setRequestProperty("Content-Type", "application/json");
		postConnection.setDoOutput(true);
		OutputStream os = postConnection.getOutputStream();
		os.write(toJson(body).getBytes());
		os.flush();
		os.close();
		
		
		
		int responseCode = postConnection.getResponseCode();
		StringBuffer response = new StringBuffer();
		if (responseCode == HttpURLConnection.HTTP_OK) {
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
			throw new RuntimeException("POST NOT WORKED ".concat(url).concat(" -> ").concat(toJson(body)).concat("STACK:")
					.concat(stringResponse));
		}
		return response.toString(); 
	}

	
	public static String getHTTP(String url, Map<String, String>... headers) throws Exception {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		if (headers!=null && headers.length>0) {
			Iterator<String> iterator = headers[0].keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				con.setRequestProperty(key, headers[0].get(key));
			}
		}
		int responseCode = con.getResponseCode();
		StringBuffer response = new StringBuffer();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} else {
			throw new RuntimeException("GET HTTP request not worked:" + " " + url);
		}
		return response.toString(); 
	}

	public static List<Squadra> getSquadre(String lega) throws Exception {
		Map<String, String> nomiFG = getNomiFG(lega);
		lega=lega.replace("-", "");
		List<Squadra> squadre=new ArrayList<Squadra>();
		Map<String,String> headers = new HashMap<String, String>();
		headers.put("app_key", APPKEY_FG);
		String url = "https://leghe.fantacalcio.it/servizi/V1_LegheFormazioni/Pagina?" + keyFG.get(lega);
		String string = getHTTP(url, headers );
		Map<String, Object> jsonToMap = jsonToMap(string);
		if (jsonToMap.get("data") == null) throw new RuntimeException("aggiornare KeyFG per " + lega);
		List<Map> l = (List<Map>) ((Map<String, Object>)jsonToMap.get("data")).get("formazioni");
		int contaSq=0;
		for (int k=0;k<2;k++) {
			for (Map<String, List<Map>> map : l) {
				if (map.get("sq").size()>k) {
					List<Map> giocatori = (List<Map>) map.get("sq").get(k).get("pl");
					if (giocatori != null) {
						for (int i=0;i<giocatori.size();i++) {
							if (i==0) {
								Squadra squadra = new Squadra();
								squadre.add(squadra);
								squadra.setNome(nomiFG.get(map.get("sq").get(0).get("id").toString()));
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
		}
		Files.write(Paths.get(ROOT + "fomrazioneFG" + lega + ".json"), toJson(squadre).getBytes());
		return squadre;
	}

	private static List<Squadra> deserializzaSquadraFG(String lega) throws Exception {
		if (Files.exists(Paths.get(ROOT + "fomrazioneFG" + lega + ".json"))) {
			return jsonToSquadre(new String(Files.readAllBytes(Paths.get(ROOT + "fomrazioneFG" + lega + ".json"))));
		} else {
			return new ArrayList<Squadra>();
		}
	}

	public static void cancellaSquadre() throws Exception {
		if (Files.exists(Paths.get(ROOT + "fomrazioneFG" + "luccicar" + ".json")))  Files.delete(Paths.get(ROOT + "fomrazioneFG" + "luccicar" + ".json"));
		if (Files.exists(Paths.get(ROOT + "fomrazioneFG" + "fantaviva" + ".json"))) Files.delete(Paths.get(ROOT + "fomrazioneFG" + "fantaviva" + ".json"));
		if (Files.exists(Paths.get(ROOT + "fomrazioneFG" + "be" + ".json"))) Files.delete(Paths.get(ROOT + "fomrazioneFG" + "be" + ".json"));
		for (int i=0;i<Main.NUM_PARTITE_FS;i++) {
			if (Files.exists(Paths.get(ROOT + "be" + i + ".html"))) {
				if (Files.exists(Paths.get(ROOT + "be" + i + ".html")))
					Files.delete(Paths.get(ROOT + "be" + i + ".html"));
			}
		}
		inizializzaSqDaEv();
		sqBeDaEscluedere = new ArrayList<String>();
	}
	private static void inizializzaSqDaEv() {
		sqDaEv= new ArrayList<String>();
		sqDaEv.add("tavolino");
		sqDaEv.add("Tavolino");
		sqDaEv.add("daddy");
	}
	
    private static List<Integer> findNuoviEventi(Giocatore og, Giocatore ng) {
    	List<Integer> ret = new ArrayList<Integer>();
    	for (Integer integer : og.getCodEventi()) {
    		if (eventi.get(integer)[4].equalsIgnoreCase("S")) {
    			int contaNuoviEventiOld = contaNuoviEventi(integer,og);
    			int contaNuoviEventiNew = contaNuoviEventi(integer,ng);
    			if (contaNuoviEventiOld != contaNuoviEventiNew) {
    				if (!ret.contains(integer)) {
        				ret.add(integer);
    				}
    			}
    		}
    	}
    	return ret;
    }
    private static int contaNuoviEventi(Integer i, Giocatore g) {
    	List<Integer> codEventi = g.getCodEventi();
    	int ret=0;
    	for (Integer integer : codEventi) {
			if (integer.intValue() == i.intValue()) ret++;
		}
    	return ret;
    }
	
	
	public synchronized static Map<String, Return> go(boolean conLive, boolean salva) throws Exception {
		init();
		List<Return> go = new ArrayList<Return>();
		List<Live> lives=new ArrayList<Live>();
		if (conLive) {
			lives = getLives();
		}
		for (ConfigCampionato configCampionato : files) {
			Return r = getReturn(configCampionato, conLive, lives);
			go.add(r);
		}
		Map<String, Return> ret =new TreeMap<String, Return>();
		for (Return retAtt : go) {
			String campionato = retAtt.getCampionato();
			Return returns = ret.get(campionato);
			if(returns==null) {
				returns=new Return();
				Instant instant = Instant.now();
				/*
				Calendar c = Calendar.getInstance();
				TimeZone tz = TimeZone.getTimeZone("UTC");
				c.setTimeZone(tz);
				SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				*/
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
			for (Squadra sq : retAtt.getSquadre()) {
				for (int i=0;i<sq.getTitolari().size();i++) {
					Giocatore giocatore = sq.getTitolari().get(i);
					giocatore.setIdGioc("T" + (i+1));
//					if (giocatore.getVoto()==0 && conLive && returns.getCampionato().equalsIgnoreCase("BE")) System.out.println(giocatore.getNome() + ";" + giocatore.getRuolo() + ";" + giocatore.getSquadra());
				}
				for (int i=0;i<sq.getRiserve().size();i++) {
					Giocatore giocatore = sq.getRiserve().get(i);
					giocatore.setIdGioc("R" + (i+1));
//					if (giocatore.getVoto()==0 && conLive && returns.getCampionato().equalsIgnoreCase("BE")) System.out.println(giocatore.getNome() + ";" + giocatore.getRuolo() + ";" + giocatore.getSquadra());
				}
				if (!sqBeDaEscluedere.contains(sq.getNome()) && !sqBeCaricate.contains(sq.getNome())) {
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
			returns.setSquadre(squadre);
		}
		
		if(conLive && salva) {
			if (ret.get("FANTAVIVA").getSquadre().size()>0) Files.write(Paths.get(ROOT + "fomrazioneFG" + "fantaviva" + ".json"), toJson(ret.get("FANTAVIVA").getSquadre()).getBytes());
			if (ret.get("LUCCICAR").getSquadre().size()>0) Files.write(Paths.get(ROOT + "fomrazioneFG" + "luccicar" + ".json"), toJson(ret.get("LUCCICAR").getSquadre()).getBytes());
			if (ret.get("BE").getSquadre().size()>0) Files.write(Paths.get(ROOT + "fomrazioneFG" + "be" + ".json"), toJson(ret.get("BE").getSquadre()).getBytes());
		}
		
		
		return ret;
	}
	
	private static List<Live> getLives() throws Exception {
		Iterator<Integer> iterator = sq.keySet().iterator();
		List<Live> lives = new ArrayList<Live>();
		while (iterator.hasNext()) {
			Integer integer = (Integer) iterator.next();
			List<Map<String, Object>> getLiveFromFG = getLiveFromFG(integer,GIORNATA);
//			if (true && getLiveFromFG.size()>0) {//FIXME togli
//				String object = (String) getLiveFromFG.get(0).get("evento");
//				if (object.length()>0) {
//					int randomNum = ThreadLocalRandom.current().nextInt(1, 4+ 1);
//					for (int i=0;i<randomNum;i++) {
//						object=object + ",7";
//					}
//					getLiveFromFG.get(0).put("evento",object);
//				}
//			}
			Live live = new Live();
			live.setSquadra(sq.get(integer));
			live.setGiocatori(getLiveFromFG);
			lives.add(live);
		}
		return lives;
	}

	private static Return getReturn(ConfigCampionato configCampionato, boolean conLive, List<Live> lives) throws Exception {
		Integer numGiocatori = configCampionato.getNumGiocatori();
		String tipo = configCampionato.getTipo();
		String campionato = configCampionato.getCampionato();
		Return r=new Return();
		r.setNome(campionato.toUpperCase());
		r.setCampionato(campionato.toUpperCase());
		Map<String, List<Squadra>>squadre=new HashMap<String, List<Squadra>>();
		squadre.put(campionato, valorizzaSquadre(campionato,numGiocatori,tipo));
		for (Live live : lives) {
			for (Map<String, Object> gg : live.getGiocatori()) {
//				System.out.println(gg.get("nome") + ";" + gg.get("ruolo") + ";" + live.getSquadra());
				double modificatore=0;
				String evento = (String) gg.get("evento");
				String ev="";
				List<Integer> codEventi=new ArrayList<Integer>();
				if (!evento.equals("")) {
					String[] split = evento.split(",");
					for (String string : split) {
						String[] strings = eventi.get(Integer.parseInt(string));
						if (strings==null) {
							ev = ev + "?" + "   ";
							modificatore=modificatore-1000;
						}else {
							ev = ev + strings[0] + "   ";
							int pos=1;
							if (r.getCampionato().equalsIgnoreCase("FANTAVIVA")) pos=1;
							if (r.getCampionato().equalsIgnoreCase("LUCCICAR")) pos=2;
							if (r.getCampionato().equalsIgnoreCase("BE")) pos=3;
							
							modificatore=modificatore+Double.parseDouble(strings[pos]);
						}
						codEventi.add(Integer.parseInt(string));
					}
					gg.put("eventodecodificato", ev);
					gg.put("codEventi", codEventi);
					gg.put("modificatore", modificatore);
				}
			}
		}
		for (Squadra squadra : squadre.get(campionato)) {
			for (Giocatore giocatore : squadra.getTitolari()) {
				findGiocatoreInLives(giocatore, lives,tipo);
				if (giocatore != null && giocatore.getRuolo() != null && (giocatore.getRuolo().equalsIgnoreCase("POR") || giocatore.getRuolo().equalsIgnoreCase("P"))) {
					if (giocatore.getVoto()>0 && !giocatore.getCodEventi().contains(4) && !giocatore.getCodEventi().contains(1000)) {
						giocatore.getCodEventi().add(1000);
						giocatore.setModificatore(giocatore.getModificatore()+1);
					}
				}
				if (giocatore != null && giocatore.getSquadra()!=null) {
					giocatore.setOrario(orari.get(giocatore.getSquadra().toUpperCase()));
				}
			}
		}
		for (Squadra squadra : squadre.get(campionato)) {
			for (Giocatore giocatore : squadra.getRiserve()) {
				findGiocatoreInLives(giocatore, lives,tipo);
				if (giocatore.getRuolo().equalsIgnoreCase("POR") || giocatore.getRuolo().equalsIgnoreCase("P")) {
					if (giocatore.getVoto()>0 && !giocatore.getCodEventi().contains(4) && !giocatore.getCodEventi().contains(1000)) {
						giocatore.getCodEventi().add(1000);
						giocatore.setModificatore(giocatore.getModificatore()+1);
					}
				}
				if (giocatore != null && giocatore.getSquadra()!=null) {
					giocatore.setOrario(orari.get(giocatore.getSquadra().toUpperCase()));
				}
			}
		}
		r.setSquadre(squadre.get(campionato));
		r.setConLive(conLive);
		return r;
	}

	private static void findGiocatoreInLives(Giocatore giocatore, List<Live> lives, String tipo) {
		for (Iterator iterator = lives.iterator(); iterator.hasNext();) {
			Live live = (Live) iterator.next();
			if (giocatore == null) {
				//								System.out.println(live + "-" + giocatore);
			}
			if (giocatore != null && giocatore.getSquadra() != null && live.getSquadra().equals(giocatore.getSquadra().toUpperCase())) {
				if (live.getGiocatori().size()>0)
					giocatore.setSquadraGioca(true);
				else
					giocatore.setSquadraGioca(false);
				for (Map<String, Object> g : live.getGiocatori()) {
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

	public static Squadra getFromFS(Document doc, String dove ) {
		Element first = doc.select(".table-formazione" + dove.toLowerCase() + "-fantapartita").first();
		Elements select = first.select("th");
		Squadra squadra = new Squadra();
		String nomeSq = select.first().text();
		squadra.setNome(nomeSq.substring(0,nomeSq.lastIndexOf(" ")));
		for (int i=0;i<11;i++) {
			Giocatore giocatore = estraiGiocatoreFromFS(doc,i,dove,"Titolari");
			if (giocatore != null) {
				squadra.getTitolari().add(giocatore);
			}
		}
		for (int i=0;i<20;i++) {
			Giocatore giocatore = estraiGiocatoreFromFS(doc,i,dove,"Panchinari");
			if (giocatore != null) {
				squadra.getRiserve().add(giocatore);
			}
		}
		return squadra;
	}

	private static Giocatore estraiGiocatoreFromFS(Document doc, int i, String dove, String ruolo) {
		Element first = doc.select("#MainContent_wuc_DettagliPartita1_rpt" + ruolo + dove + "_lblNome_" + i).first();
		Giocatore giocatore = null;
		if (first != null) {
			giocatore = new Giocatore();
			String text = first.text();
			if(!text.equalsIgnoreCase("-")) {
				String nomeG=text.substring(0,text.indexOf("(")-1);
				if (nomeG.equalsIgnoreCase("Fabian Ruiz .")) nomeG="Ruiz .";
				if (nomeG.equalsIgnoreCase("Leao R.")) nomeG="Rafael Leao ";
				if (nomeG.equalsIgnoreCase("Zapata D.")) nomeG="Zapata D. ";
				if (nomeG.equalsIgnoreCase("Samu Castillejo .")) nomeG="Castillejo ";
				if (nomeG.equalsIgnoreCase("Kulusekvski D.")) nomeG="Kulusevski ";
				if (nomeG.equalsIgnoreCase("Joao Pedro Galvao .")) nomeG="JOAO PEDRO ";
				if (nomeG.equalsIgnoreCase("Ricci M.")) nomeG="RICCI M. ";
				if (nomeG.equalsIgnoreCase("Hernandez T.")) nomeG="HERNANDEZ T. ";
				if (nomeG.equalsIgnoreCase("Kessie F.")) nomeG="KESSIE' ";
				if (nomeG.equalsIgnoreCase("Rafael Toloi .")) nomeG="TOLOI ";
				if (nomeG.equalsIgnoreCase("Martinez L.")) nomeG="MARTINEZ L. ";
				if (nomeG.equalsIgnoreCase("Donnarumma G.")) nomeG="DONNARUMMA G. ";
				if (nomeG.equalsIgnoreCase("Pezzella G.")) nomeG="PEZZELLA GIU. ";
				if (nomeG.equalsIgnoreCase("Gerard Deulofeu .")) nomeG="DEULOFEU ";
				if (nomeG.equalsIgnoreCase("Brahim Diaz .")) nomeG="DIAZ B. ";
				if (nomeG.equalsIgnoreCase("Nwankwo S.")) nomeG="SIMY ";
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

				
				if (nomeG.equalsIgnoreCase("")) nomeG="";
				if (nomeG.equalsIgnoreCase("")) nomeG="";
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
				String squadra=text.substring(text.indexOf("(")+1,text.length()-1);
				giocatore.setNome(nomeG);
				giocatore.setNomeTrim(nomeG.replaceAll(" ", ""));
				giocatore.setSquadra(squadra);
				first = doc.select("#MainContent_wuc_DettagliPartita1_rpt" + ruolo + dove + "_lblRuolo_" + i).first();
				giocatore.setRuolo(first.text());
			}
		}
		return giocatore; 
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
	private static List<Squadra> jsonToSquadre(String json){
		try
		{
			return mapper.readValue(json, new TypeReference<List<Squadra>>(){});
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	private static List<Map<String, Object>> jsonToList(String json)
	{
		try
		{
			return mapper.readValue(json, new TypeReference<List<Map<String, Object>>>(){});
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private static Map<String, Object> jsonToMap(String json)
	{
		try
		{
			return mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}



	private static List<Map<String, Object>> getLiveFromFG(int sq, int giornata) throws Exception {
		return jsonToList(getHTTP("https://www.fantacalcio.it/api/live/" + sq + "?g=" + giornata + "&i=" + I_LIVE_FANTACALCIO));
	}

}
