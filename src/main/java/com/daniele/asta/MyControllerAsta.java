package com.daniele.asta;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.transaction.Transactional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketSession;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.daniele.fantalive.dto.ExportMantra;
import com.daniele.fantalive.dto.GiocatoriPerSquadra;
import com.daniele.fantalive.dto.SpesoTotale;
import com.daniele.fantalive.entity.Allenatori;
import com.daniele.fantalive.entity.Configurazione;
import com.daniele.fantalive.entity.Fantarose;
import com.daniele.fantalive.entity.Giocatori;
import com.daniele.fantalive.entity.GiocatoriFavoriti;
import com.daniele.fantalive.entity.LoggerMessaggi;
import com.daniele.fantalive.repository.AllenatoriRepository;
import com.daniele.fantalive.repository.ConfigurazioneRepository;
import com.daniele.fantalive.repository.FantaroseRepository;
import com.daniele.fantalive.repository.GiocatoriFavoritiRepository;
import com.daniele.fantalive.repository.GiocatoriRepository;
import com.daniele.fantalive.repository.LoggerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.AudioPlayer;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;

@Component
@RestController
@RequestMapping({ "/fantaasta/" })
public class MyControllerAsta {

	private static final String JSESSIONID = "JSESSIONID=";
	private static final String PAGINA = "PAGINA=";
	@Autowired Environment environment;	
	private Calendar calUnder23;
	@Autowired HttpSession httpSession;
	@Autowired AllenatoriRepository allenatoriRepository;
	@Autowired FantaroseRepository fantaroseRepository;
	@Autowired GiocatoriRepository giocatoriRepository;
	@Autowired GiocatoriFavoritiRepository giocatoriFavoritiRepository;
	@Autowired LoggerRepository loggerRepository;
	@Autowired ConfigurazioneRepository configurazioneRepository;
	@Autowired Criptaggio criptaggio; 
	@Autowired EntityManager em;
	@Autowired SocketHandler socketHandler;
	private Map<String, Map<String, Long>> mapSpesoTotale = new HashMap();
	private Integer numAcquisti=0;
	private Integer numMinAcquisti=0;
	private Integer maxP=0;
	private Integer maxD=0;
	private Integer maxC=0;
	private Integer maxA=0;
	private Integer minP=0;
	private Integer minD=0;
	private Integer minC=0;
	private Integer minA=0;
	private Integer budget=0;
	private Integer durataAsta=0;
	private String turno="0";
	private String nomeGiocatoreTurno="";
	private Boolean isATurni;
	private Boolean isSingle;
	private Boolean isMantra;
	private Map<Integer,List<Integer>> favoriti=new HashMap<>();

	
	@Autowired HttpSessionConfig httpSessionConfig;
	
	@RequestMapping("/visFvm")
	public void visFmv() throws Exception {
		socketHandler.visFmv();
	}
	
	@RequestMapping("/aggiornaDataNascita")
	public List<Giocatori> aggiornaDataNascita() throws Exception {
		Configurazione configurazione = getConfigurazione();
		if (!configurazione.isMantra()) {
			throw new RuntimeException("Funzionalità solo per mantra");
		}
		List<Giocatori>  ret = new ArrayList<>();
		Iterable<Giocatori> findAll = giocatoriRepository.findAll();
		for (Giocatori giocatore : findAll) {
			String url="https://www.fantacalcio.it/squadre/giocatore/" + giocatore.getNome() + "/" + giocatore.getId();
			org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
			List<String> eachText = doc.select("li").eachText();
			String data="";
			for (String string : eachText) {
				if (string.startsWith("Data di nascita")) {
					data=string;
				}
			}
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, Integer.parseInt(data.substring(22,26)));
			c.set(Calendar.MONTH, Integer.parseInt(data.substring(19,21))-1);
			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(data.substring(16,18)));
			giocatore.setDataNascita(c);
			giocatoriRepository.save(giocatore);
			ret.add(giocatore);
		}
		return ret;
	}
	
	@RequestMapping("/sesH")
	public Map<String, Object> sesH() {
		Map<String, Object> ret = new HashMap<>();
		List<HttpSession> activeSessions = httpSessionConfig.getActiveSessions();
		Map<String, Object> m=new HashMap<>();
		for (HttpSession hs : activeSessions) {
			List<Object> l = new ArrayList<>();
			l.add(e(hs,"SPRING_SECURITY_SAVED_REQUEST"));
			l.add(e(hs,"SPRING_SECURITY_CONTEXT"));
			l.add("creation   :"+d(hs.getCreationTime()));
			l.add("last access:"+d(hs.getLastAccessedTime()));
			l.add("max inactive interval:" + String.valueOf(hs.getMaxInactiveInterval()));
			l.add("new:"+hs.isNew());
			m.put(hs.getId(), l);
		}
		ret.put("sessioni",m);
		return ret;
	}

	private String d(Long l) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(l);
		return sdf.format(c.getTime());
	}
	private String e(HttpSession hs,String name) {
		String s=name + ":";
		if(hs.getAttribute(name) != null) {
			s=s + hs.getAttribute(name).toString();
			if (hs.getAttribute(name) instanceof DefaultSavedRequest) {
				List<Cookie> cookies = ((DefaultSavedRequest)hs.getAttribute(name)).getCookies();
				for (Cookie cookie : cookies) {
					s = s + "COOKIE:" + cookie.getName() + " " + cookie.getValue() + " ";
				}
			}
		}
		return s;
	}
	
	
	@RequestMapping("/sesW")
	public Map<String, Object> sesW() {
		Map<String, Object> ret = new HashMap<>();
		List<WebSocketSession> sessions = socketHandler.getSessions();
		Map<String,List<LogSocket>> l=new LinkedHashMap<>();
		for (WebSocketSession webSocketSession : sessions) {
			if(!webSocketSession.isOpen()) continue;
			LogSocket ls=new LogSocket();
			String jSessionID="";
			String pagina="";
			List<String> listCookie = webSocketSession.getHandshakeHeaders().get(HttpHeaders.COOKIE);
			for (String cookie : listCookie) {
				String[] split = cookie.split(";");
				for (String string : split) {
					if (string.startsWith(" ")) string=string.substring(1);
					if (string.startsWith(JSESSIONID)) jSessionID=string.substring(JSESSIONID.length());
					if (string.startsWith(PAGINA)) {
						pagina=string.substring(PAGINA.length());
						pagina=pagina.substring(pagina.indexOf("/")+1);
						pagina=pagina.substring(pagina.indexOf("/")+1);
						pagina=pagina.substring(pagina.indexOf("/")+1);
						if(pagina.trim().equalsIgnoreCase("")) pagina="index.html";
						pagina=pagina.substring(0,pagina.indexOf("."));
					}
				}
			}
			List<LogSocket> list = l.get(jSessionID);
			if(list==null) list=new ArrayList<>();
//			ls.setjSessionId(jSessionID);
			ls.setPagina(pagina);
//			ls.setHandDate(webSocketSession.getHandshakeHeaders().getDate());
//			ls.setHandExpire(webSocketSession.getHandshakeHeaders().getExpires());
//			ls.setHandIfModifiedSince(webSocketSession.getHandshakeHeaders().getIfModifiedSince());
//			ls.setHandIfUnmodifiedSince(webSocketSession.getHandshakeHeaders().getIfUnmodifiedSince());
//			ls.setHandLastModify(webSocketSession.getHandshakeHeaders().getLastModified());
//			ls.setHandOrigin(webSocketSession.getHandshakeHeaders().getOrigin());
			ls.setHandAgent(webSocketSession.getHandshakeHeaders().get(HttpHeaders.USER_AGENT));
			ls.setId(webSocketSession.getId());
//			ls.setLocal(webSocketSession.getLocalAddress().toString());
//			ls.setOpen(webSocketSession.isOpen());
			ls.setRemote(webSocketSession.getRemoteAddress().toString());
			list.add(ls);
			l.put(jSessionID,list);
		}
		ret.put("sessioni", l);
		return ret;
	}

	public static void main(String[] args) throws Exception {
//		MyController m = new MyController();
//		Map<String,Object> body=new HashMap<>();
//		body.put("nome", "cristinao ronaldo");
//		m.leggi(body);
		
		
//		System.setProperty("freetts.voices",  "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");		
//		 VoiceManager voiceManager = VoiceManager.getInstance();
//			VoiceManager vm = VoiceManager.getInstance();
//		    Voice helloVoice = vm.getVoice("kevin16");
//
//		    helloVoice.allocate();
//		    helloVoice.speak("Adorante");
//		    helloVoice.deallocate();
		
		
		
	}
	
	
	
	@PostMapping("/leggi")
	public Map<String, Object> leggi(@RequestBody Map<String,Object> body) throws Exception {
		System.setProperty("freetts.voices",  "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");		
		VoiceManager vm = VoiceManager.getInstance();
	    Voice voice = vm.getVoice("kevin16");
	    voice.allocate();
	    AudioPlayer  audioPlayer = new SingleFileAudioPlayer(".//src//main//webapp//riproduci",Type.WAVE);
        voice.setAudioPlayer(audioPlayer);
	    voice.speak("start " + body.get("nome"));
	    voice.deallocate();
	    audioPlayer.close();	
		Map<String, Object> ret = new HashMap<>();
//	    byte[] readAllBytes = Files.readAllBytes(Paths.get(".//outputCR.wav"));
//		String encodeToString = Base64.getEncoder().encodeToString(readAllBytes);
//		ret.put("fileEncoded", encodeToString);
//		ret.put("file", readAllBytes);
//		System.out.println(encodeToString);
	    return ret;
	}
	
	@RequestMapping("/init")
	public Map<String, Object> init() throws IOException {
		calUnder23=Calendar.getInstance();
		calUnder23.add(Calendar.YEAR, -23);
		Map<String, Object> ret = new HashMap<>();
		Configurazione configurazione = getConfigurazione();
		if (configurazione==null || configurazione.getNumeroGiocatori()==null) {
			ret.put("DA_CONFIGURARE", "x");
		}
		else {
			String giocatoreLoggato = (String) httpSession.getAttribute("nomeGiocatoreLoggato");
			String idLoggato = (String) httpSession.getAttribute("idLoggato");
			if (giocatoreLoggato != null) {
				if(socketHandler.getUtentiLoggati().contains(giocatoreLoggato)) {
					ret.put("giocatoreLoggato", giocatoreLoggato);
					ret.put("idLoggato", idLoggato);
				} else {
					httpSession.removeAttribute("nomeGiocatoreLoggato");
					httpSession.removeAttribute("idLoggato");
				}
			}
			Iterable<Allenatori> allAllenatori = getAllAllenatori();
			for (Allenatori allenatori : allAllenatori) {
				if(allenatori.getOrdine()==Integer.parseInt(getTurno())) {
					setNomeGiocatoreTurno(allenatori.getNome());
				}
			}
			setNumAcquisti(configurazione.getNumeroAcquisti());
			setNumMinAcquisti(configurazione.getNumeroMinAcquisti());
			setMaxP(configurazione.getMaxP());
			setMaxD(configurazione.getMaxD());
			setMaxC(configurazione.getMaxC());
			setMaxA(configurazione.getMaxA());
			setMinP(configurazione.getMinP());
			setMinD(configurazione.getMinD());
			setMinC(configurazione.getMinC());
			setMinA(configurazione.getMinA());
			setBudget(configurazione.getBudget());
			setDurataAsta(configurazione.getDurataAsta());
			isATurni = configurazione.getIsATurni();
			if(isATurni) {
				ret.put("isATurni", "S");
			}
			else {
				ret.put("isATurni", "N");
			}
			Boolean configIsSingle = configurazione.getIsSingle();
			if (configIsSingle==null) configIsSingle=false;
			setIsSingle(configIsSingle);
			if(getIsSingle()) {
				ret.put("isSingle", "S");
			}
			else {
				ret.put("isSingle", "N");
			}
			setIsMantra(configurazione.isMantra());
			if(getIsMantra()) {
				ret.put("isMantra", "S");
			}
			else {
				ret.put("isMantra", "N");
			}
			ret.put("numAcquisti", numAcquisti);
			ret.put("numMinAcquisti", numMinAcquisti);
			ret.put("utenti", socketHandler.getUtentiLoggati());
			ret.put("maxP", maxP);
			ret.put("maxD", maxD);
			ret.put("maxC", maxC);
			ret.put("maxA", maxA);
			ret.put("minP", minP);
			ret.put("minD", minD);
			ret.put("minC", minC);
			ret.put("minA", minA);
			ret.put("budget", budget);
			ret.put("durataAsta", durataAsta);
			ret.put("elencoAllenatori", allAllenatori);
			ret.put("nomeGiocatoreTurno", getNomeGiocatoreTurno());
			ret.put("giocatoriPerSquadra",giocatoriPerSquadra());
			ret.put("calciatori", getGiocatoriLiberi());
			ret.put("mapSpesoTotale",mapSpesoTotale);
			ret.put("turno", getTurno());
			aggiornaFavoriti((String) httpSession.getAttribute("idLoggato"));
			ret.put("preferiti", favoriti);
		}
		return ret;
	}
	
	
	/*
	 curl -X POST "http://localhost:8081/restore" -H "accept: application/json" -H "Content-Type: application/json" -d "{ \"PATH\": \"C:\\restoreAs.txt\"}"
	 */
	@PostMapping("/restore")
	@Transactional 
	public Map<String, Object> restore(@RequestBody Map<String,Object> body) throws Exception {
		Map<String,Object> ret = new HashMap<>();
		String string = (String) body.get("PATH");
		List<String> readAllLines = Files.readAllLines(Paths.get(string));
		for (String sql : readAllLines) {
			if (sql.toUpperCase().startsWith("SELECT")) continue;
			if (sql.toUpperCase().startsWith("update giocatori set data_nascita".toUpperCase())) continue;
			if (sql.toUpperCase().startsWith("insert into giocatori_favoriti".toUpperCase())) continue;
			if (sql.toUpperCase().startsWith("CREATE")) {
				String tableName=sql.toUpperCase().replace("CREATE TABLE ", "");
				tableName=tableName.substring(0,tableName.indexOf(" "));
				try {
					String sqlString = "DROP TABLE if exists " + tableName;
//					System.out.println(sqlString);
					Query qy = em.createNativeQuery(sqlString);
					qy.executeUpdate();
				}
				catch (Exception e) {
					System.out.println("Drop table:" + tableName + " in errore");
				}
			}
			//			System.out.println(sql);
			Query qy = em.createNativeQuery(sql);
			try {
//				System.out.println(sql);
				qy.executeUpdate();
			}
			catch (Exception e) {
				System.out.println(sql);
				System.out.println("Errore:" + sql + e.getMessage());
			}
		}
		ret.put("out", readAllLines);
		return ret;
	}

	private static final Map<String, String> macroRuoliMantra = new HashMap<>();
    static {
    	macroRuoliMantra.put("Por", "P");
    	
    	macroRuoliMantra.put("Dd", "D");
    	macroRuoliMantra.put("Ds", "D");
    	macroRuoliMantra.put("Dc", "D");

    	macroRuoliMantra.put("E", "C");
    	macroRuoliMantra.put("C", "C");
    	macroRuoliMantra.put("M", "C");

    	macroRuoliMantra.put("W", "A");
    	macroRuoliMantra.put("T", "A");
    	macroRuoliMantra.put("Pc", "A");
    	macroRuoliMantra.put("A", "A");
    }	
	
	@PostMapping("/addFav")
	public Map<String, Object> addFav(@RequestBody Map<String,Object> body) throws Exception {
		Map<String,Object> ret = new HashMap<>();
		if (body.get("idgiocatore") != null) {
			Integer calciatoreId = (Integer) body.get("calciatoreId");
			String idgiocatore=body.get("idgiocatore").toString();
			Boolean aggiungi = (Boolean) body.get("aggiungi");
			if(aggiungi) {
				GiocatoriFavoriti favorite = new GiocatoriFavoriti();
				favorite.setIdAllenatore(Integer.parseInt(idgiocatore));
				favorite.setIdGiocatore(calciatoreId);
				favorite.setNota("");
				giocatoriFavoritiRepository.save(favorite);
			} else {
				GiocatoriFavoriti favorite = giocatoriFavoritiRepository.getFavorite(calciatoreId,Integer.parseInt(idgiocatore));
				giocatoriFavoritiRepository.delete(favorite);
			}
			aggiornaFavoriti(idgiocatore);
			socketHandler.notificaPreferiti(favoriti);
		}
		return ret;
	}

	public void aggiornaFavoriti(String idgiocatore) throws IOException {
		if (idgiocatore != null) {
			Iterable<GiocatoriFavoriti> listaFavoriti = giocatoriFavoritiRepository.getListaFavoriti(Integer.parseInt(idgiocatore));
			List<Integer> list = new ArrayList<>();
			for (GiocatoriFavoriti giocatoriFavoriti : listaFavoriti) {
				list.add(giocatoriFavoriti.getIdGiocatore());
			}
			favoriti.put(Integer.parseInt(idgiocatore), list);
		}
	}

	
	
	
	@PostMapping("/caricaFile")
	public Map<String, Object> caricaFile(@RequestBody Map<String,Object> body,HttpServletRequest request) throws Exception {
		Map<String,Object> ret = new HashMap<>();
		if(isOkDispositiva(body)) {
			byte[] byteContent = Base64.getDecoder().decode((String) body.get("file"));
			String tipoFile = (String) body.get("tipo");
			giocatoriRepository.deleteAll();
			if("FS".equalsIgnoreCase(tipoFile)) {
				try { //vecchia versione il file fornito era in xml
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					String content = new String(byteContent);
					InputSource is = new InputSource(new StringReader(content));
					Document parse = builder.parse(is);
					NodeList childNodes = parse.getChildNodes().item(0).getChildNodes();
					for (int i=0;i<childNodes.getLength();i++) {
						if (i>0) {
							Node tr = childNodes.item(i);
							NodeList childNodesTr = tr.getChildNodes();
							String id = childNodesTr.item(0).getTextContent(); 
							String squadra = childNodesTr.item(3).getTextContent(); 
							String nome = childNodesTr.item(1).getTextContent() + " " + childNodesTr.item(2).getTextContent(); 
							String ruolo = childNodesTr.item(4).getTextContent(); 
							String quotazione = childNodesTr.item(6).getTextContent(); 
							Giocatori giocatori = new Giocatori();
							giocatori.setId(Integer.parseInt(id));
							giocatori.setNome(nome);
							giocatori.setQuotazione(Integer.parseInt(quotazione));
							giocatori.setRuolo(ruolo);
							giocatori.setMacroRuolo(ruolo);
							giocatori.setSquadra(squadra);
							giocatoriRepository.save(giocatori);
						}
					}
				}
				catch (SAXParseException e){
					InputStream targetStream = new ByteArrayInputStream(byteContent);
					Workbook workbook =  new HSSFWorkbook(targetStream);
					Sheet sheet = workbook.getSheetAt(0);
					Iterator<Row> rowIterator = sheet.iterator();
					boolean bPrima=true;
					while (rowIterator.hasNext()) {
						Row currentRow = rowIterator.next();
						if (!bPrima) {
							Giocatori giocatori = new Giocatori();
							giocatori.setId(new Double(currentRow.getCell(0).getNumericCellValue()).intValue());
							giocatori.setNome(currentRow.getCell(1).getStringCellValue() + " " + currentRow.getCell(2).getStringCellValue());
							giocatori.setSquadra(currentRow.getCell(3).getStringCellValue());
							giocatori.setRuolo(currentRow.getCell(4).getStringCellValue());
							giocatori.setMacroRuolo(currentRow.getCell(4).getStringCellValue());
							giocatori.setQuotazione(new Double(currentRow.getCell(6).getNumericCellValue()).intValue());
							giocatori.setFvm(currentRow.getCell(7).getNumericCellValue());
							giocatoriRepository.save(giocatori);
						}
						bPrima=false;
					}
					workbook.close();
				}
			}
			else if("MANTRA".equalsIgnoreCase(tipoFile)) {
				String content = new String(byteContent);
				String[] split = content.split("\n");
				for(int i=0;i<split.length;i++) {
					String riga = split[i];
					String[] colonne = riga.split("\t");
					Giocatori giocatori = new Giocatori();
					giocatori.setId(Integer.parseInt(colonne[0]));
					giocatori.setNome(colonne[2]);
					try
					{
						giocatori.setQuotazione(Integer.parseInt(colonne[6].replace("\r", "")));
					}
					catch (Exception e)
					{	
						giocatori.setQuotazione(-1);
					}
					String ruolo = colonne[1].replaceAll("\"", "");
					giocatori.setRuolo(ruolo);
					String primoRuolo=ruolo;
					if(ruolo.indexOf(";")>0)
						primoRuolo=ruolo.substring(0,ruolo.indexOf(";"));
					giocatori.setMacroRuolo(macroRuoliMantra.get(primoRuolo));
					giocatori.setSquadra(colonne[3]);
					giocatoriRepository.save(giocatori);
				}
			}
			else {
				throw new RuntimeException("Tipo file non riconoscituo:" + tipoFile);
			}
			socketHandler.notificaCaricaFile(request.getRemoteAddr());
			ret.put("esitoDispositiva", "OK");
		}
		else {
			ret.put("esitoDispositiva", "KO");
		}
		
		return ret;

	}
	
	private boolean isOkDispositiva(@RequestBody Map<String, Object> body) {
		/*
		try {
			Integer tokenDispositiva = (Integer) body.get("tokenDispositiva");
			String idgiocatore =  null;
			idgiocatore=body.get("idgiocatore").toString();
			socketHandler.verificaTokenDispositiva(idgiocatore);
			long timeout=0;
			Integer tokenVerifica = socketHandler.getTokenVerifica();
			while(tokenVerifica<0 && timeout<2000) {
				tokenVerifica = socketHandler.getTokenVerifica();
				timeout=timeout+100;
				Thread.currentThread().sleep(100);
			}
			socketHandler.setTokenVerifica(-1);
			if(tokenVerifica<0) return false;
			return tokenDispositiva.equals(tokenVerifica);
		} catch (Exception e) {
			return false;
		}
		*/
		return true;
	}
	
	@PostMapping("/cancellaOfferta")
	public Map<String,Object>  cancellaOfferta(@RequestBody Map<String, Object> body,HttpServletRequest request) throws Exception {
		Map<String,Object> ret = new HashMap<>();
		if(isOkDispositiva(body)) {
			Map<String, Object> mapOfferta = (Map)body.get("offerta");
			Integer idGiocatore=(Integer) mapOfferta.get("idGiocatore");
			fantaroseRepository.delete(idGiocatore);
			socketHandler.notificaCancellaOfferta(mapOfferta,request.getRemoteAddr(),String.valueOf(idGiocatore));
			ret.put("ret", elencoCronologiaOfferte());
			ret.put("esitoDispositiva", "OK");
		}
		else {
			ret.put("esitoDispositiva", "KO");
		}
		return ret;
	}

	@PostMapping("/azzera")
	public Map<String,Object> azzera(@RequestBody Map<String, Object> body) throws Exception {
		Map<String,Object> ret = new HashMap<>();
		if(isOkDispositiva(body)) {
			ret.put("esitoDispositiva", "OK");
			if (body.get("conferma") != null && body.get("conferma").toString().equalsIgnoreCase("S")) {
				giocatoriRepository.deleteAll();
				fantaroseRepository.deleteAll();
				allenatoriRepository.deleteAll();
				giocatoriFavoritiRepository.deleteAll();
				loggerRepository.deleteAll();
				Configurazione configurazione = getConfigurazione();
				configurazione.setNumeroGiocatori(null);
				configurazioneRepository.save(configurazione);
			}
		}
		else {
			ret.put("esitoDispositiva", "KO");
		}
		return ret;
	}
	@PostMapping("/inizializzaLega")
	public Map<String, Object> inizializzaLega(@RequestBody Map<String, Object> body,HttpServletRequest request) throws Exception {
		Configurazione configurazione = getConfigurazione();
		Map<String,Object> ret = new HashMap<>();
		if(configurazione == null || configurazione.getNumeroGiocatori() == null) {
			Integer numUtenti=(Integer) body.get("numUtenti");
			setDurataAsta((Integer) body.get("durataAsta"));
			setBudget((Integer) body.get("budget"));
			setNumAcquisti((Integer) body.get("numAcquisti"));
			setNumMinAcquisti((Integer) body.get("numMinAcquisti"));
			setMaxP((Integer) body.get("maxP"));
			setMaxD((Integer) body.get("maxD"));
			setMaxC((Integer) body.get("maxC"));
			setMaxA((Integer) body.get("maxA"));
			setMinP((Integer) body.get("minP"));
			setMinD((Integer) body.get("minD"));
			setMinC((Integer) body.get("minC"));
			setMinA((Integer) body.get("minA"));
			isATurni=(Boolean) body.get("isATurni");
			setIsSingle((Boolean) body.get("isSingle"));
			setIsMantra((Boolean) body.get("isMantra"));
			if (configurazione==null) configurazione=new Configurazione();
			configurazione.setId(0);
			configurazione.setNumeroGiocatori(numUtenti);
			configurazione.setBudget(getBudget());
			configurazione.setDurataAsta(getDurataAsta());
			configurazione.setNumeroAcquisti(getNumAcquisti());
			configurazione.setNumeroMinAcquisti(getNumMinAcquisti());
			configurazione.setMaxP(getMaxP());
			configurazione.setMaxD(getMaxD());
			configurazione.setMaxC(getMaxC());
			configurazione.setMaxA(getMaxA());
			configurazione.setMinP(getMinP());
			configurazione.setMinD(getMinD());
			configurazione.setMinC(getMinC());
			configurazione.setMinA(getMinA());
			configurazione.setIsATurni(isATurni);
			configurazione.setIsSingle(getIsSingle());
			configurazione.setMantra(getIsMantra());
			configurazioneRepository.save(configurazione);
			for(int i=0;i<numUtenti;i++) {
				Allenatori al = new Allenatori();
				al.setId(i);
				al.setOrdine(i);
				if (i==0) {
					al.setIsAdmin(true);
					String giocatoreLoggato = (String) httpSession.getAttribute("nomeGiocatoreLoggato");
					if(giocatoreLoggato==null) {
						al.setNome("GIOC0");
					}
					else {
						al.setNome(giocatoreLoggato);
					}
				}
				else {
					al.setIsAdmin(false);
					al.setNome("GIOC"+i);
				}
				al.setPwd("");
				allenatoriRepository.save(al);
				socketHandler.notificaInizializzaLega(request.getRemoteAddr());
			}
			ret.put("esitoDispositiva", "OK");
		}
		else {
			ret.put("esitoDispositiva", "KO");
		}
		return ret;
	}
	@PostMapping("aggiornaSessioneNomeUtente")
	public void aggiornaSessioneNomeUtente(@RequestBody Map<String, Object> body) {
		httpSession.setAttribute("nomeGiocatoreLoggato", (String)body.get("nuovoNome"));
	}
	@PostMapping("cancellaSessioneNomeUtente")
	public  Map<String,Object>   cancellaSessioneNomeUtente() {
		httpSession.removeAttribute("nomeGiocatoreLoggato");
		httpSession.removeAttribute("idLoggato");
		Map<String,Object>  ret = new HashMap<>();
		ret.put("esito", "OK");
		return ret;
	}
	@PostMapping("/aggiornaConfigLega")
	public  Map<String,Object>  aggiornaConfigLega(@RequestBody Map<String, Object> body,HttpServletRequest request) throws Exception {
		Map <String, Object> ret = new HashMap<>();
		if(isOkDispositiva(body)) {
			Map <String, String> utentiRinominati = new HashMap<>();
			int i=0;
			setBudget((Integer) body.get("budget"));
			setNumAcquisti((Integer) body.get("numAcquisti"));
			setNumMinAcquisti((Integer) body.get("numMinAcquisti"));
			setMaxP((Integer) body.get("maxP"));
			setMaxD((Integer) body.get("maxD"));
			setMaxC((Integer) body.get("maxC"));
			setMaxA((Integer) body.get("maxA"));
			setMinP((Integer) body.get("minP"));
			setMinD((Integer) body.get("minD"));
			setMinC((Integer) body.get("minC"));
			setMinA((Integer) body.get("minA"));
			setDurataAsta((Integer) body.get("durataAsta"));
			Boolean admin = (Boolean) body.get("admin");
			isATurni = (Boolean) body.get("isATurni");
			setIsSingle((Boolean) body.get("isSingle"));
			List<Map<String, Object>> elencoAllenatori = (List<Map<String, Object>>) body.get("elencoAllenatori");
			for (Map<String, Object> map : elencoAllenatori) {
				Allenatori al = allenatoriRepository.findOne((Integer) map.get("id"));
				String nuovoNome = (String) map.get("nuovoNome");
				String vecchioNome=al.getNome();
				String giocatoreLoggato = (String) httpSession.getAttribute("nomeGiocatoreLoggato");
				if (!vecchioNome.equalsIgnoreCase(nuovoNome)) {
					utentiRinominati.put(vecchioNome, nuovoNome);
					if(giocatoreLoggato.equalsIgnoreCase(vecchioNome)) {
						ret.put("nuovoNomeLoggato", nuovoNome);
						ret.put("vecchioNomeLoggato", vecchioNome);
						//					httpSession.setAttribute("nomeGiocatoreLoggato", nuovoNome);
					}
				}
				al.setNome(nuovoNome);
				String pwd = (String) map.get("pwd");
				if (!pwd.equalsIgnoreCase(al.getPwd()))
					al.setPwd(criptaggio.encrypt(pwd,nuovoNome));
				if("true".equalsIgnoreCase(map.get("isAdmin").toString()))
					al.setIsAdmin(true);
				else
					al.setIsAdmin(false);
				if (admin) al.setOrdine((Integer) map.get("ordine"));
				i++;
				allenatoriRepository.save(al);
			}
			Configurazione configurazione = getConfigurazione();
			configurazione.setIsATurni(isATurni);
			configurazione.setIsSingle(getIsSingle());
			configurazione.setBudget(getBudget());
			configurazione.setDurataAsta(getDurataAsta());
			configurazione.setNumeroAcquisti(getNumAcquisti());
			configurazione.setNumeroMinAcquisti(getNumMinAcquisti());
			configurazione.setMaxP(getMaxP());
			configurazione.setMaxD(getMaxD());
			configurazione.setMaxC(getMaxC());
			configurazione.setMaxA(getMaxA());
			configurazione.setMinP(getMinP());
			configurazione.setMinD(getMinD());
			configurazione.setMinC(getMinC());
			configurazione.setMinA(getMinA());
			configurazioneRepository.save(configurazione);
			if(isATurni) {
				ret.put("isATurni", "S");
			}
			else {
				ret.put("isATurni", "N");
			}
			if(getIsSingle()) {
				ret.put("isSingle", "S");
			}
			else {
				ret.put("isSingle", "N");
			}
			if(getIsMantra()) {
				ret.put("isMantra", "S");
			}
			else {
				ret.put("isMantra", "N");
			}
			ret.put("esitoDispositiva", "OK");
			socketHandler.aggiornaConfigLega(utentiRinominati,getAllAllenatori(),configurazione, request.getRemoteAddr());
		}
		else {
			ret.put("esitoDispositiva", "KO");
		}
		return ret;
	}

	@GetMapping("/cripta")
	public Map<String,String> cripta(@RequestParam(name = "pwd") String pwd,@RequestParam(name = "key") String key) throws Exception {
		Map <String, String> m = new HashMap<>();
		m.put("value", criptaggio.encrypt(pwd, key));
		return m;
	}
/*
	@GetMapping("/decripta")
	public String decripta(@RequestParam(name = "pwd") String pwd,@RequestParam(name = "key") String key) throws Exception {
		return criptaggio.decrypt(pwd, key);
	}
*/	
	@PostMapping("/confermaAsta")
	public synchronized Map<String, Object> confermaAsta(@RequestBody Map<String, Object> body) throws Exception {
		Map<String,Object> ret = new HashMap<>();
		if(isOkDispositiva(body)) {
			String idgiocatore =  ((Map)body.get("offerta")).get("idgiocatore").toString();
			String idCalciatore = ((Map)body.get("offerta")).get("idCalciatore").toString();
			Fantarose findOne = fantaroseRepository.findOne(Integer.parseInt(idCalciatore));
			if (findOne == null) {
				Integer offerta = (Integer) ((Map)body.get("offerta")).get("offerta");
				Calendar c = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
				String stm = sdf.format(c.getTime());
				Fantarose fantarosa = new Fantarose();
				fantarosa.setCosto(offerta);
				fantarosa.setIdAllenatore(Integer.parseInt(idgiocatore));
				fantarosa.setIdGiocatore(Integer.parseInt(idCalciatore));
				fantarosa.setSqlTime(stm);
				fantaroseRepository.save(fantarosa);
				ret.put("insert", "OK");
			}
			else {
				ret.put("insert", "KO");
			}
			ret.put("esitoDispositiva", "OK");
		}
		else {
			ret.put("esitoDispositiva", "KO");
		}
		return ret;
	}

	/*
	@RequestMapping("/x")
	public Iterable<Fantarose> x() {
		return fantaroseRepository.x();
	}
	*/

	@RequestMapping("/spesoAllenatori")
	public List<Map<String, Object>>  spesoAllenatori() {
		try {
			String sql = "select sum(costo) costo, a.nome from fantarose f, allenatori a where a.id = idAllenatore group by a.nome";
			Query qy = em.createNativeQuery(sql);
			List<Object[]> resultList = qy.getResultList();
			List<Map<String, Object>> ret = new ArrayList<>();
			for (Object[] row : resultList) {
				Map<String, Object> m = new HashMap<>();
				m.put("costo",  row[0]);
				m.put("nome",row[1]);
				ret.add(m);
			}
			return ret;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Value("${security.user.name}")
	private String nomeLega;
	
	@RequestMapping(value = "/esportaMantra")
	public void esportaMantra(HttpServletResponse response) throws IOException {    
		String csvFileName = nomeLega + "_export_per_sito.csv";
		response.setContentType("text/csv");
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"",csvFileName);
		response.setHeader(headerKey, headerValue);		
		Iterable<ExportMantra> exportMantra = fantaroseRepository.exportMantra();
		String oldAll="";
		StringBuilder s = new StringBuilder();
		for (ExportMantra ex : exportMantra) {
			if (!ex.getNome().equalsIgnoreCase(oldAll)) {
				oldAll=ex.getNome();
				s.append("$,$,$\n");
			}
			s.append(ex.getNome() + "," + ex.getIdGiocatore() + "," + ex.getCosto() + "\n");
		}
		response.getWriter().print(s);
	}
	
	@RequestMapping(value = "/esporta")
	public void esporta(HttpServletResponse response) throws IOException {    
		String csvFileName = nomeLega + "_export.csv";
		response.setContentType("text/csv");
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"",csvFileName);
		response.setHeader(headerKey, headerValue);
		Map<String, Map<String, Object>> giocatoriPerSquadra = giocatoriPerSquadra();
		Iterator<String> iterator = giocatoriPerSquadra.keySet().iterator();
		StringBuilder s = new StringBuilder();
		s.append("allenatore" + ";\"" + "ruolo"+ "\";" + "costo"+ ";" + "squadra"+ ";" + "giocatore" + "\n");
		while (iterator.hasNext()) {
			String allenatore = (String) iterator.next();
			Map m = (Map) giocatoriPerSquadra.get(allenatore).get("ruoli");
			Collection<List> giocatori = m.values();
			for (List<Map> giocatore : giocatori) {
				for (Map map : giocatore) {
					s.append(allenatore + ";\"" + map.get("ruolo")+ "\";" + map.get("costo")+ ";" + map.get("squadra")+ ";" + map.get("giocatore") + "\n");
				}
			}
		}
		response.getWriter().print(s);
	}
	
	@RequestMapping("/giocatoriPerSquadra")
	public Map<String, Map<String, Object>> giocatoriPerSquadra() {
		setMapSpesoTotale(new HashMap());
		Iterable<SpesoTotale> spesoTotale = fantaroseRepository.spesoTotale();
		for (SpesoTotale speso : spesoTotale) {
			Map<String, Long> tmp = getMapSpesoTotale().get(speso.getNome());
			if (tmp==null) tmp=new HashMap();
			tmp.put("speso", tmp.get("speso")==null?speso.getCosto():tmp.get("speso") + speso.getCosto());
			tmp.put("conta", tmp.get("conta")==null?speso.getConta():tmp.get("conta") + speso.getConta());
			
			
			long quantiDaPrendere=0;
			if(numMinAcquisti<tmp.get("conta")) {
				quantiDaPrendere=0;
			} else {
				quantiDaPrendere=numMinAcquisti-tmp.get("conta");
			}
			//budget-quantiDaPrendere-speso
			int adding=1;
			if(quantiDaPrendere<1) adding=0;
			tmp.put("maxRilancio", budget-quantiDaPrendere-tmp.get("speso") +adding );
//			System.out.println(budget + ";"+speso.getNome() +";"+tmp.get("speso") + ";" + numMinAcquisti + ";" + tmp.get("conta") + ";" + quantiDaPrendere + ";");
			tmp.put("speso"+speso.getMacroRuolo(),speso.getCosto());
			tmp.put("conta"+speso.getMacroRuolo(),speso.getConta());
//			if(!speso.getMacroRuolo().equalsIgnoreCase("P")) {
				tmp.put("spesoAll",(tmp.get("spesoAll")==null?0:tmp.get("spesoAll"))+speso.getCosto());
				tmp.put("contaAll",(tmp.get("contaAll")==null?0:tmp.get("contaAll"))+speso.getConta());
//			}
			getMapSpesoTotale().put(speso.getNome(), tmp);
		}
		Iterable<GiocatoriPerSquadra> giocatoriPerSquadra = fantaroseRepository.giocatoriPerSquadra();
		Map<String, Map<String, Object>> ret = new LinkedHashMap<>();
		for (GiocatoriPerSquadra giocatorePerSquadra : giocatoriPerSquadra) {
			String allenatore = giocatorePerSquadra.getAllenatore();
			Map<String, Long> spese = getMapSpesoTotale().get(allenatore);
			Map<String, List<Map<String,Object>>> mapRuoli =null;
			if(ret.get(allenatore) != null)
				mapRuoli = (Map<String, List<Map<String, Object>>>) ret.get(allenatore).get("ruoli");
			if(mapRuoli==null) {
				mapRuoli=new LinkedHashMap<>();
			}
			String ruolo = giocatorePerSquadra.getMacroRuolo();
			List<Map<String,Object>> list = mapRuoli.get(ruolo);
			if (list==null) {
				list=new ArrayList<>();
			}
			Map<String,Object> riga=new HashMap<>();
			riga.put("ruolo", giocatorePerSquadra.getRuolo());
			riga.put("giocatore", giocatorePerSquadra.getGiocatore());
			if(giocatorePerSquadra.getDataNascita()!=null && giocatorePerSquadra.getDataNascita().get(Calendar.YEAR)<2020 && giocatorePerSquadra.getDataNascita().after(calUnder23)) {
				riga.put("under23", "*");
			}
			riga.put("squadra", giocatorePerSquadra.getSquadra());
			riga.put("costo", giocatorePerSquadra.getCosto());
			list.add(riga);
			mapRuoli.put(ruolo, list);
			Map<String, Object> t = new HashMap<>();
			t.put("ruoli", mapRuoli);
			t.put("spese", spese);
			ret.put(allenatore, t);
		}
		return ret;
	}

	@RequestMapping("/spesoTotale")
	public Iterable<SpesoTotale>  spesoTotale() {
		return fantaroseRepository.spesoTotale();
	}
	
	@RequestMapping("/elencoCronologiaOfferte")
	public List<Map<String, Object>>  elencoCronologiaOfferte() {
		try {
			String sql = "select a.Nome allenatore, g.Squadra, g.Ruolo, g.nome giocatore, Costo, sqlTime, idGiocatore, idAllenatore   from  fantarose f, " + 
					"giocatori g, allenatori a  where g.id = idGiocatore and a.id = idAllenatore order by sqlTime desc";
			Query qy = em.createNativeQuery(sql);
			List<Object[]> resultList = qy.getResultList();
			List<Map<String, Object>> ret = new ArrayList<>();
			for (Object[] row : resultList) {
				Map<String, Object> m = new HashMap<>();
				m.put("allenatore",  row[0]);
				m.put("squadra", row[1]);
				m.put("ruolo",  row[2]);
				m.put("giocatore",  row[3]);
				m.put("costo",  row[4]);
				m.put("sqlTime",row[5]);
				m.put("idGiocatore",  row[6]);
				m.put("idAllenatore", row[7]);
				ret.add(m);
			}
			return ret;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@RequestMapping("/elencoOfferte")
	public List<Map<String, Object>>  elencoOfferte() {
		try {
			String sql = "select a.Nome allenatore, g.Squadra, g.Ruolo, g.nome giocatore, Costo, sqlTime from fantarose f, giocatori g, " + 
					"allenatori a where g.id = idGiocatore and a.id = idAllenatore order by allenatore, ruolo desc, giocatore";
			Query qy = em.createNativeQuery(sql);
			List<Object[]> resultList = qy.getResultList();
			List<Map<String, Object>> ret = new ArrayList<>();
			for (Object[] row : resultList) {
				Map<String, Object> m = new HashMap<>();
				m.put("allenatore", row[0]);
				m.put("squadra", row[1]);
				m.put("ruolo", row[2]);
				m.put("giocatore", row[3]);
				m.put("costo", row[4]);
				m.put("sqlTime", row[5]);
				ret.add(m);
			}
			return ret;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private ObjectMapper mapper = new ObjectMapper();
	public String toJson(Object o)
	{
		try
		{
			byte[] data = mapper.writeValueAsBytes(o);
			return new String(data);//, Charsets.ISO_8859_1
		} catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		} 
	}
	public List<Map<String, Object>> jsonToList(String json)
	{
		try
		{
			return mapper.readValue(json, new TypeReference<List<Map<String, Object>>>(){});
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@RequestMapping("/riepilogoAllenatori")
	public List<Map<String, Object>>  riepilogoAllenatori() {
		try {
			String sql = "select count(ruolo) conta, ruolo, a.nome nome from fantarose f, allenatori a, giocatori g where g.id=idGiocatore " + 
					"and a.id = idAllenatore group by a.nome ,ruolo order by a.nome, ruolo desc";
			Query qy = em.createNativeQuery(sql);
			List<Object[]> resultList = qy.getResultList();
			List<Map<String, Object>> ret = new ArrayList<>();
			for (Object[] row : resultList) {
				Map<String, Object> m = new HashMap<>();
				m.put("conta",  row[0]);
				m.put("ruolo",row[1]);
				m.put("nome",row[2]);
				ret.add(m);
			}
			return ret;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

//	@Cacheable(cacheNames = "allenatori")
	@GetMapping(path="/allAllenatori")
	public @ResponseBody Iterable<Allenatori> getAllAllenatori() {
		Iterable<Allenatori> findAll = allenatoriRepository.getAllenatoriOrderByOrdine();
		for (Allenatori allenatori : findAll) {
			allenatori.setNuovoNome(allenatori.getNome());
		}
		return findAll;
	}	

	@GetMapping(path="/allFantarose")
	public @ResponseBody Iterable<Fantarose> getAllFantarose() {
		return fantaroseRepository.findAll();
	}	

	@GetMapping(path="/allGiocatori")
	public @ResponseBody Iterable<Giocatori> getAllGiocatori() {
		return giocatoriRepository.findAll();
	}	
	@GetMapping(path="/elencoLoggerMessaggi")
	public @ResponseBody Iterable<LoggerMessaggi> elencoLoggerMessaggi() {
		return loggerRepository.findAll();
	}	
	
	@GetMapping(path="/configurazione")
	public @ResponseBody Configurazione getConfigurazione() {
		Iterator<Configurazione> iterator = configurazioneRepository.findAll().iterator();
		if (!iterator.hasNext()) return null;
		return iterator.next();
	}	

	@GetMapping(path="/giocatoriLiberi")
	public @ResponseBody List<Map<String, Object>> getGiocatoriLiberi() {
		/*
		Page<Object[]> resultListPaginata = giocatoriRepository.getGiocatoriLiberiPaginati(new PageRequest(2, 3));
		System.out.println(resultListPaginata.getNumber());
		*/
		List<Object[]> resultList = giocatoriRepository.getGiocatoriLiberi();
		List<Map<String, Object>> ret = new ArrayList<>();
		for (Object[] row : resultList) {
			Map<String, Object> m = new HashMap<>();
			m.put("id",  row[0]);
			m.put("squadra",  row[1]);
			m.put("nome",  row[2]);
			m.put("ruolo",  row[3]);
			m.put("macroRuolo",  row[4]);
			m.put("quotazione",  row[5]);
			m.put("fvm",  row[7]);
			if(row[6] != null) {
				Calendar c = (Calendar) row[6];
				if (c.get(Calendar.YEAR)<2020 && c.after(calUnder23)) m.put("under23", "SI");
			}
			ret.add(m);
		}
		return ret;
	}
	
	public String getNomeGiocatoreTurno() {
		return nomeGiocatoreTurno;
	}
	public void setNomeGiocatoreTurno(String nomeGiocatoreTurno) {
		this.nomeGiocatoreTurno = nomeGiocatoreTurno;
	}
	public String getTurno() {
		return turno;
	}
	public void setTurno(String turno) {
		this.turno = turno;
	}
	public Boolean getIsATurni() {
		return isATurni;
	}
	public void setIsATurni(Boolean isATurni) {
		this.isATurni = isATurni;
	}
	public Integer getBudget() {
		return budget;
	}
	public void setBudget(Integer budget) {
		this.budget = budget;
	}
	public Integer getNumAcquisti() {
		return numAcquisti;
	}
	public void setNumAcquisti(Integer numAcquisti) {
		this.numAcquisti = numAcquisti;
	}
	public Map<String, Map<String, Long>> getMapSpesoTotale() {
		return mapSpesoTotale;
	}
	public void setMapSpesoTotale(Map<String, Map<String, Long>> mapSpesoTotale) {
		this.mapSpesoTotale = mapSpesoTotale;
	}


	public Boolean getIsMantra() {
		return isMantra;
	}


	public void setIsMantra(Boolean isMantra) {
		this.isMantra = isMantra;
	}


	public Integer getMaxP() {
		return maxP;
	}


	public void setMaxP(Integer maxP) {
		this.maxP = maxP;
	}


	public Integer getMaxD() {
		return maxD;
	}


	public void setMaxD(Integer maxD) {
		this.maxD = maxD;
	}


	public Integer getMaxC() {
		return maxC;
	}


	public void setMaxC(Integer maxC) {
		this.maxC = maxC;
	}


	public Integer getMaxA() {
		return maxA;
	}


	public void setMaxA(Integer maxA) {
		this.maxA = maxA;
	}


	public Integer getNumMinAcquisti() {
		return numMinAcquisti;
	}


	public void setNumMinAcquisti(Integer numMinAcquisti) {
		this.numMinAcquisti = numMinAcquisti;
	}

	public Integer getDurataAsta() {
		return durataAsta;
	}

	public void setDurataAsta(Integer durataAsta) {
		this.durataAsta = durataAsta;
	}

	public Integer getMinP() {
		return minP;
	}

	public void setMinP(Integer minP) {
		this.minP = minP;
	}

	public Integer getMinD() {
		return minD;
	}

	public void setMinD(Integer minD) {
		this.minD = minD;
	}

	public Integer getMinC() {
		return minC;
	}

	public void setMinC(Integer minC) {
		this.minC = minC;
	}

	public Integer getMinA() {
		return minA;
	}

	public void setMinA(Integer minA) {
		this.minA = minA;
	}

	public Map<Integer,List<Integer>> getFavoriti() {
		return favoriti;
	}

	public void setFavoriti(Map<Integer,List<Integer>> favoriti) {
		this.favoriti = favoriti;
	}

	public Boolean getIsSingle() {
		return isSingle;
	}

	public void setIsSingle(Boolean isSingle) {
		this.isSingle = isSingle;
	}
	
}
