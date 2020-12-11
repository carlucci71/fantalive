package fantalive.bl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

import fantalive.configurazione.SocketHandler;
import fantalive.entity.ConfigCampionato;
import fantalive.entity.Giocatore;
import fantalive.entity.Live;
import fantalive.entity.Notifica;
import fantalive.entity.Return;
import fantalive.entity.Squadra;
import fantalive.util.Constant;

public class Main {
	public static final String URL_NOTIFICA_NAS = "http://192.168.1.83:7080/fantalive-0.0.1-SNAPSHOT/";
	public static final String URL_NOTIFICA_HEROKU = "https://fantalive71.herokuapp.com/";
	
	public static final int DELTA_VIVA_FG=2;
	public static final int DELTA_LUCCICAR_FG=3;
	public static final String COMP_VIVA_FG = "250964";
	public static final String COMP_LUCCICAR_FG = "306919";

	public static final int DELTA_FS=3;
	public static final int NUM_PARTITE_FS = 4;
	public static final String COMP_FS = "123506";
	private static final int PRIMA_GIORNATA_FS = 10675622;

	private static final String SPORT_ID_LIVE_GAZZETTA = "1";
	private static final String COMP_ID_LIVE_GAZZETTA = "21";
	private static final String I_LIVE_FANTACALCIO = "15";

	public static int GIORNATA = 10;
	public static final String ROOT="/tmp/";
	public static Map<String,Object> toSocket;
	public static String MIO_IP;
	
	public static FantaLiveBOT fantaLiveBot;

	public static enum Campionati {BE, FANTAVIVA, LUCCICAR};
	
	static Map<Integer, String> sq=null;
	public static HashMap<Integer, String[]> eventi=null;
	static List<ConfigCampionato> files=null;
	public static List<String> sqDaEv= null;
	static Map<String, Giocatore> oldSnapshot=null;

//	static Map<String, List<Squadra>>squadre=new HashMap<String, List<Squadra>>();
	static ObjectMapper mapper;
	public static Map<String, String> keyFG=null;

	public static void init() throws Exception {
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
		if (eventi ==null) {
			eventi = new HashMap<Integer, String[]>();
			eventi.put(1000, new String[] {"portiere imbattuto","1","1","1","S"});
			eventi.put(22, new String[] {"assist hight","1.5","1","1","S"});
			eventi.put(11, new String[] {"gol vittoria","0","0","0","N"});
			eventi.put(12, new String[] {"gol pareggio","0","0","0","N"});
			eventi.put(24, new String[] {"assist medium1","1","1","1","S"});
			eventi.put(14, new String[] {"uscito","0","0","0","S"});
			eventi.put(15, new String[] {"entrato","0","0","0","S"});
			eventi.put(16, new String[] {"gol annullato","0","0","0","N"});
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
	}

	public static void snapshot(SocketHandler socketHandler) throws Exception {
		if (false) {//FIXME false
			System.out.println("FOTO");
		}
		Map<String, Return> go = go(true, null, null);
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
						snapshot.put(r.getCampionato().replaceAll("#", "") + "#" + squadra.getNome().replaceAll("#", "") + "#" + giocatore.getNome().replaceAll("#", ""), giocatore);
					}
					for (Giocatore giocatore : squadra.getRiserve()) {
						snapshot.put(r.getCampionato().replaceAll("#", "") + "#" + squadra.getNome().replaceAll("#", "") + "#" + giocatore.getNome().replaceAll("#", ""), giocatore);
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
				List<Map<Integer,Integer>> findNuoviEventi = findNuoviEventi(oldGioc, newGioc);
				Map<String,Integer> eventi=new HashMap<>();
				String oldTag = oldGioc.getOrario().get("tag");
				String newTag = newGioc.getOrario().get("tag");
				if (newTag.equalsIgnoreCase("PreMatch") && newGioc.getVoto() ==0 && newGioc.isSquadraGioca() && !oldGioc.isSquadraGioca()) {
					eventi.put("NON SCHIERATO",null);
				}
				if (newTag.equalsIgnoreCase("PreMatch") && oldGioc.getVoto() != newGioc.getVoto()) {
					eventi.put("SCHIERATO",null);
				}
				if (!oldTag.equalsIgnoreCase(newTag)) {
					
/*
PreMatch
Postponed
Cancelled
Walkover
FirstHalf
HalfTime
SecondHalf
FullTime
*/
					
					eventi.put(newTag,null);
				}
				if (findNuoviEventi.size()>0 || !oldTag.equalsIgnoreCase(newTag)) {
					for (Map<Integer,Integer> nuovoEvento : findNuoviEventi) {
						Integer ev = nuovoEvento.keySet().iterator().next();
						eventi.put(Main.eventi.get(ev)[0],nuovoEvento.get(ev));
					}
				}
				if (eventi.size()>0) {
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
					notifica.setSquadra(splitKey[1]);
					notifica.setGiocatore(splitKey[2]);
					notifica.setId(newGioc.getIdGioc());
					notifica.setEventi(eventi);
					notifica.setVoto(newGioc.getVoto() + newGioc.getModificatore()); 
					if (newGioc.isCambio()) {
						notifica.setCambio("(X)");
					}
				}
			}
    		Set<String> keySet = notifiche.keySet();
    		if (keySet!= null && keySet.size()>0) {
        		StringBuilder des = new StringBuilder();
    			for (String camp : keySet) {
					des.append("\n").append(camp).append("\n");
					Map<String, List<Notifica>> sq = notifiche.get(camp);
					Iterator<String> itSq = sq.keySet().iterator();
					while (itSq.hasNext()) {
						String sqN = (String) itSq.next();
						des.append("\t").append(sqN).append("\n");
						List<Notifica> listN = sq.get(sqN);
						Collections.sort(listN);
						for (Notifica notifica : listN) {
							String ret = notifica.getGiocatore() + notifica.getCambio() + " " + notifica.getId() + " " + notifica.getVoto();
							Set<String> ks = notifica.getEventi().keySet();
							for (String key : ks) {
								if (notifica.getEventi().get(key)==null) {
									ret = ret + "\n\t\t\t "  + "  " + key;
								}
							}
							for (String key : ks) {
								if (notifica.getEventi().get(key) != null && notifica.getEventi().get(key)>0) {
									ret = ret + "\n\t\t\t "  + notifica.getEventi().get(key) + " " + key;
								}
							}
							for (String key : ks) {
								if (notifica.getEventi().get(key) != null && notifica.getEventi().get(key)<0) {
									ret = ret + "\n\t\t\t "  + (notifica.getEventi().get(key) * -1) + " --NO-- " + key;
								}
							}
							des.append("\t\t").append(ret).append("\n");
						}
					}
				}
    			des.append("\n").append(getUrlNotifica());
        		Main.inviaNotifica(des.toString());
    		}
    	}
    	oldSnapshot=snapshot;
    	if (socketHandler != null) {
    		Map<String, Object> map=new HashMap<>();
    		map.put("res", go);
    		socketHandler.invia(map);
    	}
	}
	
	public static String getUrlNotifica() {
		if (MIO_IP.equals("192.168.1.83")) {
			return "http://" + MIO_IP + URL_NOTIFICA_NAS;
		}
		else if (MIO_IP.startsWith("192"))
			return "http://" + MIO_IP + ":7080/";
		else
			return URL_NOTIFICA_HEROKU;
	}
	
	
	public static void main(String[] args) throws Exception {
		
	
		
		
//		init();
		/*
		Iterator<String> iterator;
    	snapshot(null);
		iterator = oldSnapshot.keySet().iterator();
		while (iterator.hasNext()) {
			String k = (String) iterator.next();
			Giocatore giocatore = oldSnapshot.get(k);
			if (giocatore.getNome().toUpperCase().startsWith("RIBER")){
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
    	*/
	}
	
	public static void inviaNotifica(String msg) throws Exception {
		String urlNotifica;
		Map<String, String> body;
		Map<String, String> headers;
		fantaLiveBot.inviaMessaggio(Constant.CHAT_ID_FANTALIVE,msg,false);
		if(false) {//FIXME false
			urlNotifica = "https://api.spontit.com/v3/push";
			body = new HashMap<String, String>();
			body.put("pushTitle", "FantaLive");
			//body.put("subtitle", "Aggiornamento");
			body.put("content", msg);
			body.put("link", getUrlNotifica());

//			body.put("channelName", "daniele");
			//body.put("schedule", 1591982947);
			//body.put("expirationStamp", 1592414947);
			//body.put("openLinkInApp", "true");
			//body.put("iOSDeepLink", "photos-redirect://");


			headers = new HashMap<String, String>();
			headers.put("X-Authorization", Constant.SPONTIT_KEY);
			headers.put("X-UserId", Constant.SPONTIT_USERID);
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
		cookie = new BasicClientCookie("FantaSoccer_Auth", Constant.AUTH_FS);
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



	private static Map<String, Map<String, String>> partiteLive() throws Exception {
		Map<String, Map<String, String>> orari=null;
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
		return orari;
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
			throw new RuntimeException("POST NOT WORKED ".concat(url).concat(" -> ").concat("STACK:")
					.concat(stringResponse));
		}
		return response.toString(); 
	}
	
	public static void aggKeyFG() throws Exception {
		int giornata=Main.GIORNATA;
		Main.keyFG=new HashMap<String, String>();
		Main.keyFG.put("fantaviva", "id_comp=" + Main.COMP_VIVA_FG + "&r=" + String.valueOf(giornata - Main.DELTA_VIVA_FG)  + "&f=" + String.valueOf(giornata - Main.DELTA_VIVA_FG) + "_" + calcolaAggKey("fanta-viva") + ".json");
		Main.keyFG.put("luccicar", "id_comp=" + Main.COMP_LUCCICAR_FG + "&r=" + String.valueOf(giornata - Main.DELTA_LUCCICAR_FG) + "&f=" + String.valueOf(giornata - Main.DELTA_LUCCICAR_FG) + "_" + calcolaAggKey("luccicar") + ".json");
	}
	
	private static String calcolaAggKey(String lega) throws Exception {
		int giornata=Main.GIORNATA-Main.DELTA_VIVA_FG;
		if (lega.equalsIgnoreCase("luccicar")) giornata=Main.GIORNATA-Main.DELTA_LUCCICAR_FG;
		String url = "https://leghe.fantacalcio.it/" + lega + "/formazioni/" + giornata;
		String string = Main.getHTTP(url);
		string = string.substring(string.indexOf(".s('tmp', ")+11);
		string=string.substring(0,string.indexOf(")"));
		string = string.replace("|", "@");
		String[] split = string.split("@");
		return split[1];
	}

	public static List<Squadra> getSquadre(String lega) throws Exception {
		Map<String, String> nomiFG = getNomiFG(lega);
		lega=lega.replace("-", "");
		List<Squadra> squadre=new ArrayList<Squadra>();
		Map<String,String> headers = new HashMap<String, String>();
		headers.put("app_key", Constant.APPKEY_FG);
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
		for (Integer codEvento : ng.getCodEventi()) {
    		if (eventi.get(codEvento)[4].equalsIgnoreCase("S")) {
    			int contaNuoviEventiOld = contaNuoviEventi(codEvento,og);
    			int contaNuoviEventiNew = contaNuoviEventi(codEvento,ng);
    			if (contaNuoviEventiOld != contaNuoviEventiNew) {
    				if (!ret.contains(codEvento)) {
    					Map<Integer, Integer> m = new HashMap<>();
    					m.put(codEvento,verso*(contaNuoviEventiNew-contaNuoviEventiOld));
        				ret.add(m);
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
//		init();//TODO serve?
		List<Return> go = new ArrayList<Return>();
		List<Live> lives=new ArrayList<Live>();
		Map<String, Map<String, String>> orari=null;
		if (conLive) {
			Map<String, Object> getLives = getLives();
			lives = (List<Live>) getLives.get("lives");
			orari = (Map<String, Map<String, String>>) getLives.get("orari");
		}
		for (ConfigCampionato configCampionato : files) {
			Return r = getReturn(configCampionato, conLive, lives, orari);
			go.add(r);
		}
		Map<String, Return> ret =new TreeMap<String, Return>();
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
			sqDaEv= new ArrayList<String>();
			for (Squadra sq : retAtt.getSquadre()) {
				
				if (sq.getNome() != null && (sq.isEvidenza() || sq.getNome().equalsIgnoreCase(sqDaAddEvid)) && !sq.getNome().equalsIgnoreCase(sqDaDelEvid)) {
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
			returns.setSquadre(squadre);
		}
		
		if(conLive) {
			if (ret.get(Campionati.FANTAVIVA.name()).getSquadre().size()>0) Files.write(Paths.get(ROOT + "fomrazioneFG" + "fantaviva" + ".json"), toJson(ret.get(Campionati.FANTAVIVA.name()).getSquadre()).getBytes());
			if (ret.get(Campionati.LUCCICAR.name()).getSquadre().size()>0) Files.write(Paths.get(ROOT + "fomrazioneFG" + "luccicar" + ".json"), toJson(ret.get(Campionati.LUCCICAR.name()).getSquadre()).getBytes());
			if (ret.get(Campionati.BE.name()).getSquadre().size()>0) Files.write(Paths.get(ROOT + "fomrazioneFG" + "be" + ".json"), toJson(ret.get(Campionati.BE.name()).getSquadre()).getBytes());
		}
		
		
		return ret;
	}
	
	private static Map<String, Object> getLives() throws Exception {
		Map orari=partiteLive();
		List<Live> lives = new ArrayList<Live>();
		if (false) {//FIXME false
			orari =  jsonToMap(new String(Files.readAllBytes(Paths.get(ROOT + "orari.json"))));
			lives =  jsonToLives(new String(Files.readAllBytes(Paths.get(ROOT + "lives.json"))));
		} else {
			Iterator<Integer> iterator = sq.keySet().iterator();
			while (iterator.hasNext()) {
				Integer integer = (Integer) iterator.next();
				String sqFromLive = getHTTP("https://www.fantacalcio.it/api/live/" + integer + "?g=" + GIORNATA + "&i=" + I_LIVE_FANTACALCIO);
				List<Map<String, Object>> getLiveFromFG = jsonToList(sqFromLive);
				Live live = new Live();
				live.setSquadra(sq.get(integer));
				live.setGiocatori(getLiveFromFG);
				lives.add(live);
			}
			if (false) {//FIXME false
				Files.write(Paths.get(ROOT + "orari.json"), toJson(orari).getBytes());//
				Files.write(Paths.get(ROOT + "lives.json"), toJson(lives).getBytes());
			}
		}
		Map<String, Object> ret = new HashMap<>();
		ret.put("orari", orari);
		ret.put("lives", lives);
		return ret;
	}

	private static Return getReturn(ConfigCampionato configCampionato, boolean conLive, List<Live> lives,Map<String, Map<String, String>> orari) throws Exception {
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
					giocatore.setCodEventi(new ArrayList<Integer>());
				}
				for (int i=0;i<squadra.getRiserve().size();i++) {
					Giocatore giocatore = squadra.getRiserve().get(i);
					giocatore.setModificatore(0);
					giocatore.setVoto(0);
					giocatore.setSquadraGioca(false);
					giocatore.setEvento("");
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
					for (String string : split) {
						String[] strings = eventi.get(Integer.parseInt(string));
						if (strings[4].equals("N")) continue;
						if (strings==null) {
							ev = ev + "?" + "   ";
							modificatore=modificatore-1000;
						}else {
							ev = ev + strings[0] + "   ";
							int pos=1;
							if (r.getCampionato().equalsIgnoreCase(Campionati.FANTAVIVA.name())) pos=1;
							if (r.getCampionato().equalsIgnoreCase(Campionati.LUCCICAR.name())) pos=2;
							if (r.getCampionato().equalsIgnoreCase(Campionati.BE.name())) pos=3;
							
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
					if (orari != null) {
						giocatore.setOrario(orari.get(giocatore.getSquadra().toUpperCase()));
					}
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

	private static void findGiocatoreInLives(Giocatore giocatore, List<Live> lives, String tipo) {
		for (Iterator iterator = lives.iterator(); iterator.hasNext();) {
			Live live = (Live) iterator.next();
			if (giocatore == null) {
				//								System.out.println(live + "-" + giocatore);
			}
			if (giocatore != null && giocatore.getSquadra() != null && live.getSquadra().equals(giocatore.getSquadra().toUpperCase())) {
				if (live.getGiocatori().size()>0) {
					giocatore.setSquadraGioca(true);
				}
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
		if (nomeSq.contains("-") &&  nomeSq.lastIndexOf(" ")>-1) {
			nomeSq=nomeSq.substring(0,nomeSq.lastIndexOf(" "));
		}
		squadra.setNome(nomeSq);
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
	private static List<Live> jsonToLives(String json){
		try
		{
			return mapper.readValue(json, new TypeReference<List<Live>>(){});
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

}
