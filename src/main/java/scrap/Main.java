package scrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
	private static final String APPKEY_FG = "4ab27d6de1e92c810c6d4efc8607065a735b917f";
	public static final int DELTA_VIVA_FG=2;
	public static final int DELTA_LUCCICAR_FG=3;
	public static final String COMP_VIVA_FG = "250964";
	public static final String COMP_LUCCICAR_FG = "306919";

	public static String AUTH_FS = "8538C0634FC56205D32B0CBE915A8D81A15DFFCA5105C6B7A99691A500E5A2707D72518CCA8A924C356C270B28E435C8DA4F82F5CD01347F4EE7AD0945CAE244689990582C79B1B820F3337E9CACCFC8CB4F91402F181C39B868CDE518ED6F3EFAC974A9C7EA206A03E3110334472C4B750AA70E";
	public static final int DELTA_FS=3;
	static final int NUM_PARTITE_FS = 4;
	public static final String COMP_FS = "123506";
	private static final int PRIMA_GIORNATA_FS = 10675622;

	private static final String SPORT_ID_LIVE_GAZZETTA = "1";
	private static final String COMP_ID_LIVE_GAZZETTA = "21";
	private static final String I_LIVE_FANTACALCIO = "15";

	public static int GIORNATA = 9;
	public static final String ROOT="/tmp/";
	
	
	static Map<Integer, String> sq=null;
	static HashMap<Integer, String[]> eventi=null;
	static List<ConfigCampionato> files=null;
	public static List<String> sqBeDaEscluedere= new ArrayList<String>();
	static List<String> sqBeCaricate=null;
	public static List<String> sqDaEv= null;

	static Map<String, List<Squadra>>squadre=new HashMap<String, List<Squadra>>();
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
			eventi.put(1000, new String[] {"portiere imbattuto","1","1","1"});
			eventi.put(22, new String[] {"assist hight","1.5","1","1"});
			eventi.put(11, new String[] {"gol vittoria","0","0","0"});
			eventi.put(12, new String[] {"gol pareggio","0","0","0"});
			eventi.put(24, new String[] {"assist medium1","1","1","1"});
			eventi.put(14, new String[] {"uscito","0","0","0"});
			eventi.put(15, new String[] {"entrato","0","0","0"});
			eventi.put(16, new String[] {"gol annullato","0","0","0"});
			eventi.put(17, new String[] {"infortunio","0","0","0"});
			eventi.put(1, new String[] {"ammonito","-0.5","-0.5","-0.5"});
			eventi.put(2, new String[] {"espulso","-1","-1","-1"});
			eventi.put(3, new String[] {"gol","3","3","3"});
			eventi.put(4, new String[] {"gol subito","-1","-1","-1"});
			eventi.put(7, new String[] {"rigore parato","3","3","3"});
			eventi.put(8, new String[] {"rigore sbagliato","-3","-3","-3"});
			eventi.put(9, new String[] {"rigore segnato","3","3","2"});
			eventi.put(20, new String[] {"assist low","0.5","1","1"});
			eventi.put(21, new String[] {"assist medium2","1","1","1"});
			eventi.put(10, new String[] {"autogol","-2","-3","-3"});
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

	public static void main(String[] args) throws Exception {
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
		String callHTTP = callHTTP("https://api2-mtc.gazzetta.it/api/v1/sports/calendar?sportId=" + SPORT_ID_LIVE_GAZZETTA + "&competitionId=" + COMP_ID_LIVE_GAZZETTA);
		Map<String, Object> jsonToMap = jsonToMap(callHTTP);
		List<Map> l = (List<Map>) ((Map)jsonToMap.get("data")).get("games");
		for (Map map : l) {
			List<Map> lm = (List<Map>) map.get("matches");
			for (Map map2 : lm) {
				HashMap<String,String> orario=new HashMap<String,String>();
				orario.put("tag", (String)((Map)map2.get("timing")).get("tag"));
				orario.put("val", ((Map)map2.get("timing")).get("val").toString());
				orari.put(((String)((Map)map2.get("awayTeam")).get("teamCode")).toUpperCase(), orario);
				orari.put(((String)((Map)map2.get("homeTeam")).get("teamCode")).toUpperCase(), orario);
			}
		}
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
			throw new RuntimeException("GET request not worked:" + GET_URL);
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
		String string = callHTTP(url, headers );
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
		sqDaEv.add("tavolino 3-4-3");
		sqDaEv.add("Tavolino");
		sqDaEv.add("daddy");
	}
	public static Map<String, Return> go(boolean conLive) throws Exception {
		init();
		List<Return> go = new ArrayList<Return>();
		for (ConfigCampionato configCampionato : files) {
			Return r = getReturn(configCampionato, conLive);
			go.add(r);
		}
		Map<String, Return> ret =new TreeMap<String, Return>();
		for (Return retAtt : go) {
			String campionato = retAtt.getCampionato();
			Return returns = ret.get(campionato);
			if(returns==null) {
				returns=new Return();
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
				}
				for (int i=0;i<sq.getRiserve().size();i++) {
					Giocatore giocatore = sq.getRiserve().get(i);
					giocatore.setIdGioc("R" + (i+1));
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
		
		if(conLive) {
			if (!toJson(ret.get("FANTAVIVA").getSquadre()).equals("")) Files.write(Paths.get(ROOT + "fomrazioneFG" + "fantaviva" + ".json"), toJson(ret.get("FANTAVIVA").getSquadre()).getBytes());
			if (!toJson(ret.get("LUCCICAR").getSquadre()).equals("")) Files.write(Paths.get(ROOT + "fomrazioneFG" + "luccicar" + ".json"), toJson(ret.get("LUCCICAR").getSquadre()).getBytes());
			if (!toJson(ret.get("BE").getSquadre()).equals("")) Files.write(Paths.get(ROOT + "fomrazioneFG" + "be" + ".json"), toJson(ret.get("BE").getSquadre()).getBytes());
		}
		
		
		return ret;
	}

	private static Return getReturn(ConfigCampionato configCampionato, boolean conLive) throws Exception {
		Integer numGiocatori = configCampionato.getNumGiocatori();
		String tipo = configCampionato.getTipo();
		String campionato = configCampionato.getCampionato();
		Return r=new Return();
		r.setNome(campionato.toUpperCase());
		r.setCampionato(campionato.toUpperCase());
		squadre.put(campionato, valorizzaSquadre(campionato,numGiocatori,tipo));
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



	private static List<Map<String, Object>> sendGET(int sq, int giornata) throws Exception {
		return jsonToList(callHTTP("https://www.fantacalcio.it/api/live/" + sq + "?g=" + giornata + "&i=" + I_LIVE_FANTACALCIO));
	}

}
