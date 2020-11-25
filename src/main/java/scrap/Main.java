package scrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
	static Map<Integer, String> sq=null;
	static HashMap<Integer, String[]> eventi=null;
	static List<Object[]> files=null;
	private static List<String> sqBeDaEscluedere= new ArrayList<String>();
	static List<String> sqBeCaricate=null;
	public static int GIORNATA = 9;
	private static List<String> sqDaEv= new ArrayList<String>();
	static Map<String, List<Squadra>>squadre=new HashMap<String, List<Squadra>>();
	static ObjectMapper mapper;
	public static Map<String, String> keyFG=null;
	private static Map<String, Map<String, String>> orari=null;
	public static String FantaSoccerAuth = "DF2C5A1915CAF97D420731EB3FE6DBD0C1617B69F7F5FC7E5CF9BECE33EF182AF74AC88BC38F96B5F36E4A8C6AD930245B25DE87698C47EA5CE8766D56814C60AC245EE92D16156079234C5DE9FC31C9DEAD482F5084231D732E6FF652ED7F07F524037A4BC72BFBC8042993EC37F7E74ED287EF";


	public static void init() throws Exception {
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		sqBeCaricate=new ArrayList<String>();
		if (eventi ==null) {
			eventi = new HashMap<Integer, String[]>();
			eventi.put(22, new String[] {"assist hight","1.5"});
			eventi.put(11, new String[] {"gol vittoria","0"});
			eventi.put(12, new String[] {"gol pareggio","0"});
			eventi.put(24, new String[] {"assist medium1","1"});
			eventi.put(14, new String[] {"uscito","0"});
			eventi.put(15, new String[] {"entrato","0"});
			eventi.put(16, new String[] {"gol annullato","0"});
			eventi.put(17, new String[] {"infortunio","0"});
			eventi.put(1, new String[] {"ammonito","-0.5"});
			eventi.put(2, new String[] {"espulso","-1"});
			eventi.put(3, new String[] {"gol","3"});
			eventi.put(4, new String[] {"gol subito","-1"});
			eventi.put(7, new String[] {"rigore parato","3"});
			eventi.put(8, new String[] {"rigore sbagliato","-3"});
			eventi.put(9, new String[] {"rigore segnato","3"});
			eventi.put(20, new String[] {"assist low","0.5"});
			eventi.put(21, new String[] {"assist medium2","1"});
			eventi.put(10, new String[] {"autogol","-2"});
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
			files = new ArrayList<Object[]>();
			files.add(new Object[] {"luccicar",24,"FANTAGAZZETTA","luccicar"});
			files.add(new Object[] {"fantaviva",22,"FANTAGAZZETTA","alma"});
			for (int i=0;i<8;i++) {
				files.add(new Object[] {"be"+i + ".html",22,"FANTASERVICE","be"});
			}
		}
		partiteLive();
	}

	public static void main(String[] args) throws Exception {
		//		init();
		//		Main.go();
		//				Main.serializzaRosa();
		//		System.out.println(Main.deserializzaRose());
		//		getSquadre("luccicar",false);
		//		getSquadre("fantaviva",false);
		//		partiteLive();

		/*
		MyController m = new MyController();
		Main.GIORNATA=8;
		m.aggKeyFG();
		Map<String,String> headers = new HashMap<String, String>();
		headers.put("app_key", "4ab27d6de1e92c810c6d4efc8607065a735b917f");
		String lega = "luccicar";
		lega="fantaviva";
		String url = "https://leghe.fantacalcio.it/servizi/V1_LegheFormazioni/Pagina?" + keyFG.get(lega);
		//		System.out.println(url);
		String string = callHTTP(url, headers );
		System.out.println(string);
		*/
		String lega = "luccicar";
//		lega="fanta-viva";
		Map<String, String> nomiFG = getNomiFG(lega);
		System.out.println(nomiFG.get("6294023"));
	}

	private static Map<String, String> getNomiFG(String lega) throws Exception {
		Map<String, String> ret = new HashMap<String, String>();
		String url = "https://leghe.fantacalcio.it/" + lega + "/area-gioco/rose?";
		String response = callHTTP(url);
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
		// Populate cookies if needed
		BasicClientCookie cookie;
		cookie = new BasicClientCookie("FantaSoccer_Auth", FantaSoccerAuth);
		cookie.setDomain("www.fanta.soccer");
		cookie.setPath("/");
		cookieStore.addCookie(cookie);
		/*
		cookie= new BasicClientCookie("ASP.NET_SessionId", "vnfxoruxaunscezagbfadmpv");
		cookie.setDomain("www.fanta.soccer");
		cookie.setPath("/");
		cookieStore.addCookie(cookie);
		cookie = new BasicClientCookie("_fbp", "fb.1.1599919109651.1354406460");
		cookie.setDomain(".fanta.soccer");
		cookie.setPath("/");
		cookieStore.addCookie(cookie);
		cookie = new BasicClientCookie("_ga", "GA1.1.1186504348.1599919110");
		cookie.setDomain(".fanta.soccer");
		cookie.setPath("/");
		cookieStore.addCookie(cookie);
		cookie = new BasicClientCookie("__atuvc", "69%7C44%2C48%7C45%2C13%7C46%2C13%7C47%2C8%7C48");
		cookie.setDomain("www.fanta.soccer");
		cookie.setPath("/");
		cookieStore.addCookie(cookie);
		cookie = new BasicClientCookie("__atuvs", "5fbbc0e9deebe708003");
		cookie.setDomain("www.fanta.soccer");
		cookie.setPath("/");
		cookieStore.addCookie(cookie);
		cookie = new BasicClientCookie("_ga_FRMCW717B4", "GS1.1.1606140140.50.1.1606140172.0");
		cookie.setDomain(".fanta.soccer");
		cookie.setPath("/");
		cookieStore.addCookie(cookie);
		 */
		// Set the store
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		int deltaFS=3;
		try {
			for (int i=0;i<4;i++) {
				HttpGet httpget = new HttpGet("https://www.fanta.soccer/it/lega/privata/123506/dettaglipartita/" + String.valueOf(GIORNATA-deltaFS) + "/" + String.valueOf(i + 10675622 + (4*(GIORNATA-deltaFS))) + "/");
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
				Files.write(Paths.get("./" + "be" + i + ".html"), responseBody.getBytes());
			}
		} finally {
			httpclient.close();
		}
	}



	private static void partiteLive() throws Exception {
		orari=new HashMap<String, Map<String,String>>();
		String callHTTP = callHTTP("https://api2-mtc.gazzetta.it/api/v1/sports/calendar?sportId=1&competitionId=21");
		Map<String, Object> jsonToMap = jsonToMap(callHTTP);
		List<Map> l = (List<Map>) ((Map)jsonToMap.get("data")).get("games");
		for (Map map : l) {
			List<Map> lm = (List<Map>) map.get("matches");
			for (Map map2 : lm) {
				HashMap<String,String> orario=new HashMap<String,String>();
				//				System.out.println("casa: "+((Map)map2.get("awayTeam")).get("teamCode"));
				//				System.out.println("fuori: "+((Map)map2.get("homeTeam")).get("teamCode"));
				//				System.out.println("tag: "+((Map)map2.get("timing")).get("tag"));
				//				System.out.println("val: "+((Map)map2.get("timing")).get("val"));
				//				System.out.println("--------------");
				orario.put("tag", (String)((Map)map2.get("timing")).get("tag"));
				orario.put("val", ((Map)map2.get("timing")).get("val").toString());
				orari.put(((String)((Map)map2.get("awayTeam")).get("teamCode")).toUpperCase(), orario);
				orari.put(((String)((Map)map2.get("homeTeam")).get("teamCode")).toUpperCase(), orario);
			}
		}
		//		System.out.println(orari);
	}

	public static String callHTTP(String GET_URL, Map<String, String>... headers) throws Exception {
		URL obj = new URL(GET_URL);
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
			throw new RuntimeException("GET request not worked");
		}
		return response.toString(); 
	}




	public static List<Squadra> getSquadre(String lega, boolean fromFile) throws Exception {
		List<Squadra> squadre=new ArrayList<Squadra>();
		if (!fromFile) {
			Map<String,String> headers = new HashMap<String, String>();
			headers.put("app_key", "4ab27d6de1e92c810c6d4efc8607065a735b917f");
			String url = "https://leghe.fantacalcio.it/servizi/V1_LegheFormazioni/Pagina?" + keyFG.get(lega);
			//			System.out.println(url);
			String string = callHTTP(url, headers );
			Map<String, Object> jsonToMap = jsonToMap(string);
			if (jsonToMap.get("data") == null) throw new RuntimeException("aggiornare KeyFG per " + lega);
			List<Map> l = (List<Map>) ((Map<String, Object>)jsonToMap.get("data")).get("formazioni");
//			List<Rosa> rose = deserializzaRose();
			int contaSq=0;
			String ll=lega;
			if(lega.equals("fantaviva")) ll = "fanta-viva";
			Map<String, String> nomiFG = getNomiFG(ll);
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
//								Giocatore g=null;
//								String id = giocatori.get(i).get("id").toString();
								//							System.out.println(id);
								/*
								for (Rosa rosa : rose) {
									if (rosa.getLega().equals(lega)) {
										List<Giocatore> giocatoriRosa = rosa.getGiocatori();
										for (Giocatore giocatoreRosa : giocatoriRosa) {
											if (giocatoreRosa.getId().equalsIgnoreCase(id.trim())) {
												if (lega.equalsIgnoreCase("luccicar")){
													squadre.get(contaSq-1).setNome(String.valueOf(contaSq));
												} else {
													squadre.get(contaSq-1).setNome(rosa.getNome());
												}
												g=giocatoreRosa;
											}
										}
									}
								}
								*/
								
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
			/*
			if (lega.equalsIgnoreCase("luccicar")){

				for (Squadra squadra : squadre) {
					boolean ok=true;
					for (Rosa rosa : rose) {
						if (isOkRosa(rosa,squadra)) {
							squadra.setNome(rosa.getNome());
						}
					}
				}
			}
			*/
			Files.write(Paths.get("./fomrazioneFG" + lega + ".json"), toJson(squadre).getBytes());
		}
		else {
			squadre=deserializzaSquadraFG(lega);
		}
		return squadre;
	}

	private static boolean isOkRosa(Rosa rosa, Squadra squadra) {
		boolean ok=true;
		List<Giocatore> titolari = squadra.getTitolari();
		for (Giocatore giocatore : titolari) {
			if (!isInRosa(giocatore,rosa)) {
				ok=false;
			}
		}
		return ok;
	}

	private static boolean isInRosa(Giocatore giocatore, Rosa rosa) {
		boolean ok=false;
		for (Giocatore giocatoreRosa : rosa.getGiocatori()) {
			if (giocatoreRosa.getId().equalsIgnoreCase(giocatore.getId())) {
				ok=true;
			}
		}
		return ok;
	}

	private static List<Squadra> deserializzaSquadraFG(String lega) throws Exception {
		return jsonToSquadre(new String(Files.readAllBytes(Paths.get("./fomrazioneFG" + lega + ".json"))));
	}

	private static List<Rosa> deserializzaRose() throws Exception {
		return jsonToRose(new String(Files.readAllBytes(Paths.get("./rose.json"))));
	}
	public static void cancellaSquadre() throws Exception {
		if (Files.exists(Paths.get("./rose.json")))  Files.delete(Paths.get("./rose.json"));
		if (Files.exists(Paths.get("./fomrazioneFG" + "luccicar" + ".json")))  Files.delete(Paths.get("./fomrazioneFG" + "luccicar" + ".json"));
		if (Files.exists(Paths.get("./fomrazioneFG" + "fantaviva" + ".json")))Files.delete(Paths.get("./fomrazioneFG" + "fantaviva" + ".json"));
		for (int i=0;i<8;i++) {
			if (Files.exists(Paths.get("./" + "be" + i + ".html"))) {
				if (Files.exists(Paths.get("./" + "be" + i + ".html")))
					Files.delete(Paths.get("./" + "be" + i + ".html"));
			}
		}
		sqDaEv= new ArrayList<String>();
		setSqBeDaEscluedere(new ArrayList<String>());
	}
	public static void serializzaRosa() throws Exception {
		List<Rosa> rose = serializzaRosa("fanta-viva");
		rose.addAll(serializzaRosa("luccicar"));
		Files.write(Paths.get("./rose.json"), toJson(rose).getBytes());
	}
	private static List<Rosa> serializzaRosa(String lega) throws Exception {
		Map<String, List<String>> urls=recuperaUrlGiocatoriFG(lega);
		Iterator<String> keySq = urls.keySet().iterator();
		List<Rosa> rose = new ArrayList<Rosa>();
		while (keySq.hasNext()) {
			String sq = (String) keySq.next();
			Rosa rosa = new Rosa();
			rosa.setLega(lega.replaceAll("-", ""));
			rose.add(rosa);
			rosa.setNome(sq);
			List<String> urlss = urls.get(sq);
			for (String url : urlss) {
				String id = url.substring(url.lastIndexOf("/")+1);
				Giocatore giocatore=new Giocatore();
				rosa.getGiocatori().add(giocatore);
				giocatore.setId(id);
				String nomeGiocatore=url.substring(0,url.lastIndexOf("/"));
				nomeGiocatore=nomeGiocatore.substring(nomeGiocatore.lastIndexOf("/")+1);
				giocatore.setNomeTrim(nomeGiocatore.replaceAll(" ", ""));
				giocatore.setNome(nomeGiocatore);
				Document doc = Jsoup.connect(url).get();
				/*
				List<String> eachText = doc.select("li").eachText();
				for (String string : eachText) {
					if (string.startsWith("Ruolo Mantra")) {
					}
				}
				 */
				Elements select = doc.select("li");
				for (int i=0;i<select.size();i++) {
					Element element = select.get(i);
					String string = element.text();
					if (string.startsWith("Squadra")) {
						giocatore.setSquadra(string.substring(string.lastIndexOf(" ")+1).substring(0,3).toUpperCase());
					}
					if (lega.equalsIgnoreCase("luccicar") && string.startsWith("Ruolo Classic")) {
						giocatore.setRuolo(string.substring(string.length()-1));
					}
					if (lega.equalsIgnoreCase("fanta-viva") && string.startsWith("Ruolo Mantra")) {
						List<String> eachText = element.select("span").eachText();
						String ruolo="";
						for (String string2 : eachText) {
							ruolo=ruolo+string2.substring(string2.lastIndexOf(" ")+1) + " ";
						}
						giocatore.setRuolo(ruolo.substring(0,ruolo.length()-1));
					}

				}

			}
		}
		return rose;
	}


	private static Map<String, List<String>> recuperaUrlGiocatoriFG(String lega) throws Exception {
		Map<String, List<String>> urls = new HashMap<String, List<String>>();
		String GET_URL = "https://leghe.fantacalcio.it/" + lega + "/area-gioco/rose?";
		String response = callHTTP(GET_URL);

		Document doc = Jsoup.parse(response);
		Elements select = doc.select(".list-rosters-item");

		int size = select.size();
		for (int i=0;i<size;i++) {
			Element element = select.get(i);
			String squadra = element.select("H4").first().text();
			List<String> urlss = new ArrayList<String>();
			Elements player = element.select(".player-link");
			for (int k=0;k<player.size();k++) {
				String attr = player.get(k).attr("href");
				//					System.out.println(attr);
				urlss.add(attr);
			}
			urls.put(squadra, urlss);
		}

		return urls;
	}

	public static Map<String, Return> go(boolean conLive) throws Exception {
		init();
		List<Return> go = new ArrayList<Return>();
		for (Object object[] : files) {
			String file = (String)object[0];
			Integer numGiocatori = (Integer)object[1];
			String tipo = (String)object[2];
			String campionato = (String)object[3];
			Return r = getReturn(file,numGiocatori,tipo, campionato, conLive);
			go.add(r);
		}
		Map<String, Return> ret =new HashMap<String, Return>();
		for (Return retAtt : go) {
			String campionato = retAtt.getCampionato();
			Return returns = ret.get(campionato);
			if(returns==null) {
				returns=new Return();
				ret.put(campionato, returns);
			}
			returns.setCampionato(campionato);
			returns.setNome(campionato);
			List<Squadra> squadre = returns.getSquadre();
			for (Squadra sq : retAtt.getSquadre()) {
				if (!getSqBeDaEscluedere().contains(sq.getNome()) && !sqBeCaricate.contains(sq.getNome())) {
					if (sqDaEv.contains(sq.getNome())) {
						sq.setEvidenza(true);
					}
					squadre.add(sq);
					sqBeCaricate.add(sq.getNome());
				}
			}
			returns.setSquadre(squadre);
		}
		return ret;
	}

	private static Return getReturn(String file,Integer numGiocatori,String tipo, String campionato, boolean conLive) throws Exception {
		Return r=new Return();
		r.setNome(file.toUpperCase());
		r.setCampionato(campionato.toUpperCase());
		//		if (squadre.get(file)==null) 
		{
			squadre.put(file, valorizzaSquadre(file,numGiocatori,tipo));
		}
		Iterator<Integer> iterator = sq.keySet().iterator();
		List<Live> lives = new ArrayList<Live>();
		if (conLive) {
			while (iterator.hasNext()) {
				Integer integer = (Integer) iterator.next();
				List<Map<String, Object>> sendGET = sendGET(integer,GIORNATA);
				Live live = new Live();
				live.setSquadra(sq.get(integer));
				live.setGiocatori(sendGET);
				lives.add(live);
			}
		}
		for (Live live : lives) {
			for (Map<String, Object> gg : live.getGiocatori()) {
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
							modificatore=modificatore-100;
						}else {
							ev = ev + strings[0] + "   ";
							modificatore=modificatore+Double.parseDouble(strings[1]);
						}
						codEventi.add(Integer.parseInt(string));
					}
					gg.put("eventodecodificato", ev);
					gg.put("codEventi", codEventi);
					gg.put("modificatore", modificatore);
				}
			}
		}
		for (Squadra squadra : squadre.get(file)) {
			for (Giocatore giocatore : squadra.getTitolari()) {
				findGiocatoreInLives(giocatore, lives,tipo);
				if (giocatore != null && giocatore.getSquadra()!=null) {
					giocatore.setOrario(orari.get(giocatore.getSquadra().toUpperCase()));
				}
			}
		}
		for (Squadra squadra : squadre.get(file)) {
			for (Giocatore giocatore : squadra.getRiserve()) {
				findGiocatoreInLives(giocatore, lives,tipo);
				if (giocatore != null && giocatore.getSquadra()!=null) {
					giocatore.setOrario(orari.get(giocatore.getSquadra().toUpperCase()));
				}
			}
		}
		r.setSquadre(squadre.get(file));
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
					//					System.out.println(nomeGiocatoreLive);//TODO
					String ruoloLive=g.get("ruolo").toString();
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
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (tipo.equals("FANTAGAZZETTA")) {
			//ROSE
//			List<Rosa> rose=new ArrayList<Rosa>();
			InputStream inputS;
			if (false) {
				/*
				inputS = classLoader.getResourceAsStream("Rose_" + nomefile + ".xlsx");
				Workbook workbook = new XSSFWorkbook(inputS);
				Sheet sheet = workbook.getSheetAt(0);
				rose = estraiRose(sheet,rose,0);
				rose = estraiRose(sheet,rose,5);
				inputS.close();
				workbook.close();
				*/
			} else {
//				rose=deserializzaRose();
			}
			//FILE
			if (false) {/*
				inputS = classLoader.getResourceAsStream(nomefile + ".mhtml");
				Document doc = Jsoup.parse(inputS, "UTF-8", "http://example.com/");
				inputS.close();
				Elements resultLinks = doc.select("h4");
				int pos=0;
				Squadra squadra = new Squadra();
				for (int i=0;i<resultLinks.size();i++) {
					Element x = resultLinks.get(i);
					if (x.toString().contains("media-heading")) {
						String testo = x.ownText().replace("= ", "");
						squadra = new Squadra();
						squadra.setNome(testo);
						squadre.add(squadra);
						pos++;
					}
				}
				resultLinks = doc.select("span");
				pos=0;
				int contaSq=0;
				for (int i=0;i<resultLinks.size();i++) {
					Element x = resultLinks.get(i);
					if (x.toString().contains("player-name") && !x.toString().contains("smart-player-roles")) {
						String testo = x.text().replace("= ", "").replace("</a>", "").replace("=", "").replace("</span>", "");
						Giocatore giocatore = findGiocatoreByNome(rose,squadre.get(contaSq).getNome(),testo.replaceAll(" ", "")); 
						if (pos<11) squadre.get(contaSq).getTitolari().add(giocatore);
						else squadre.get(contaSq).getRiserve().add(giocatore);
						pos++;
						if (pos>numGiocatori) {
							pos=0;
							contaSq++;
						}
					}
				}
			*/}else {
				squadre.addAll(deserializzaSquadraFG(nomefile));
			}

		} else {
			//classLoader.getResourceAsStream(nomefile);
			if (Files.exists(Paths.get("./" + nomefile))) {
				byte[] inputS = Files.readAllBytes(Paths.get("./" + nomefile));
				Document doc = Jsoup.parse(new String(inputS));
				squadre.add(getFromFS(doc, "Casa"));
				squadre.add(getFromFS(doc, "Trasferta"));
			}
		}

		return squadre;
	}

	private static Giocatore findGiocatoreByNome(List<Rosa> rose, String nomeSquadra, String nomeGiocatore) {
		for (Rosa rosa : rose) {
			if (rosa.getNome().equals(nomeSquadra)) {
				for (Giocatore giocatore : rosa.getGiocatori()) {
					if (giocatore.getNomeTrim().equalsIgnoreCase(nomeGiocatore)) {
						return giocatore;
					}
				}
			}
		}
		return null;
	}

	private static Squadra getFromFS(Document doc, String dove ) {
		Element first = doc.select(".table-formazione" + dove.toLowerCase() + "-fantapartita").first();
		Elements select = first.select("th");
		Squadra squadra = new Squadra();
		squadra.setNome(select.first().text());
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
				if (nomeG.equalsIgnoreCase("")) nomeG="";
				if (nomeG.equalsIgnoreCase("")) nomeG="";


				//						if (nomeG.contains("alvao")) {//TODO
				//							System.out.println();
				//						}

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

	private static List<Rosa> estraiRose(Sheet sheet,List<Rosa> rose, int offset) {
		Iterator<Row> rowIterator = sheet.iterator();
		int oldRowNum=-1;
		Rosa rosa=null;
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			int rowNum = row.getRowNum();
			if(rowNum<4) continue;
			Cell cell = row.getCell(0+offset);
			if (cell!=null) {
				String colonna1 = cell.getStringCellValue();
				if (rowNum>oldRowNum+1 && !"".equals(colonna1)) {
					rosa = new Rosa();
					rose.add(rosa);
					rosa.setNome(colonna1);
				} else {
					String colonna2 = row.getCell(1+offset).getStringCellValue();
					if (!"".equals(colonna2) && !"Calciatore".equals(colonna2)) {
						Giocatore giocatore=new Giocatore();
						giocatore.setNomeTrim(colonna2.replaceAll(" ", ""));
						giocatore.setNome(colonna2);
						giocatore.setRuolo(colonna1);
						giocatore.setSquadra(row.getCell(2+offset).getStringCellValue());
						rosa.getGiocatori().add(giocatore);
					}
				}
			}
			oldRowNum=rowNum;
		}
		return rose;
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
	private static List<Rosa> jsonToRose(String json){
		try
		{
			return mapper.readValue(json, new TypeReference<List<Rosa>>(){});
		} catch (Exception e)
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



	private static List<Map<String, Object>> sendGET(int sq, int giornata) throws Exception {
		return jsonToList(callHTTP("https://www.fantacalcio.it/api/live/" + sq + "?g=" + giornata + "&i=15"));
	}

	public static List<String> getSqDaEv() {
		return sqDaEv;
	}

	public static void setSqDaEv(List<String> sqDaEv) {
		Main.sqDaEv = sqDaEv;
	}

	public static List<String> getSqBeDaEscluedere() {
		return sqBeDaEscluedere;
	}

	public static void setSqBeDaEscluedere(List<String> sqBeDaEscluedere) {
		Main.sqBeDaEscluedere = sqBeDaEscluedere;
	}



}
