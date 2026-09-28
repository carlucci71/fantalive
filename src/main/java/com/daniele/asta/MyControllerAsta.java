package com.daniele.asta;

import com.daniele.asta.session.AstaSessionRegistry;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
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

import javax.sound.sampled.AudioFileFormat.Type;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
import java.util.Optional;

@Component
@RestController
@RequestMapping({"/fantaasta/"})
public class MyControllerAsta {

    private static final Logger log = LoggerFactory.getLogger(MyControllerAsta.class);

    private static final String JSESSIONID = "JSESSIONID=";
    private static final String PAGINA = "PAGINA=";
    @Autowired
    Environment environment;
    private Calendar calUnder23;
    @Autowired
    HttpSession httpSession;
    @Autowired
    AllenatoriRepository allenatoriRepository;
    @Autowired
    FantaroseRepository fantaroseRepository;
    @Autowired
    GiocatoriRepository giocatoriRepository;
    @Autowired
    GiocatoriFavoritiRepository giocatoriFavoritiRepository;
    @Autowired
    LoggerRepository loggerRepository;
    @Autowired
    ConfigurazioneRepository configurazioneRepository;
    @Autowired
    Criptaggio criptaggio;
    @Autowired
    EntityManager em;
    @Autowired
    SocketHandler socketHandler;
    @Autowired
    AstaSessionRegistry sessionRegistry;
    private Map<String, Map<String, Long>> mapSpesoTotale = new HashMap();
    private Integer numAcquisti = 0;
    private Integer numMinAcquisti = 0;
    private Integer maxP = 0;
    private Integer maxD = 0;
    private Integer maxC = 0;
    private Integer maxA = 0;
    private Integer minP = 0;
    private Integer minD = 0;
    private Integer minC = 0;
    private Integer minA = 0;
    private Integer budget = 0;
    private Integer durataAsta = 0;
    private String turno = "0";
    private String nomeGiocatoreTurno = "";
    private Boolean isATurni;
    private Boolean isSingle;
    private Boolean isMantra;
    private Map<Integer, List<Integer>> favoriti = new HashMap<>();


    @Autowired
    HttpSessionConfig httpSessionConfig;

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
        List<Giocatori> ret = new ArrayList<>();
        Iterable<Giocatori> findAll = giocatoriRepository.findAll();
        for (Giocatori giocatore : findAll) {
            String url = "https://www.fantacalcio.it/squadre/giocatore/" + giocatore.getNome() + "/" + giocatore.getId();
            org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
            List<String> eachText = doc.select("li").eachText();
            String data = "";
            for (String string : eachText) {
                if (string.startsWith("Data di nascita")) {
                    data = string;
                }
            }
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, Integer.parseInt(data.substring(22, 26)));
            c.set(Calendar.MONTH, Integer.parseInt(data.substring(19, 21)) - 1);
            c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(data.substring(16, 18)));
            giocatore.setDataNascita(c);
            giocatoriRepository.save(giocatore);
            ret.add(giocatore);
        }
        return ret;
    }
/*
    @RequestMapping("/sesH")
    public Map<String, Object> sesH() {
        Map<String, Object> ret = new HashMap<>();
        List<HttpSession> activeSessions = httpSessionConfig.getActiveSessions();
        Map<String, Object> m = new HashMap<>();
        for (HttpSession hs : activeSessions) {
            List<Object> l = new ArrayList<>();
            l.add(e(hs, "SPRING_SECURITY_SAVED_REQUEST"));
            l.add(e(hs, "SPRING_SECURITY_CONTEXT"));
            l.add("creation   :" + d(hs.getCreationTime()));
            l.add("last access:" + d(hs.getLastAccessedTime()));
            l.add("max inactive interval:" + String.valueOf(hs.getMaxInactiveInterval()));
            l.add("new:" + hs.isNew());
            m.put(hs.getId(), l);
        }
        ret.put("sessioni", m);
        return ret;
    }
*/
    private String d(Long l) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(l);
        return sdf.format(c.getTime());
    }
/*
    private String e(HttpSession hs, String name) {
        String s = name + ":";
        if (hs.getAttribute(name) != null) {
            s = s + hs.getAttribute(name).toString();
            if (hs.getAttribute(name) instanceof DefaultSavedRequest) {
                List<Cookie> cookies = ((DefaultSavedRequest) hs.getAttribute(name)).getCookies();
                for (Cookie cookie : cookies) {
                    s = s + "COOKIE:" + cookie.getName() + " " + cookie.getValue() + " ";
                }
            }
        }
        return s;
    }
*/

    @RequestMapping("/sesW")
    public Map<String, Object> sesW() {
        Map<String, Object> ret = new HashMap<>();
        List<WebSocketSession> sessions = socketHandler.getSessions();
        Map<String, List<LogSocket>> l = new LinkedHashMap<>();
        for (WebSocketSession webSocketSession : sessions) {
            if (!webSocketSession.isOpen()) continue;
            LogSocket ls = new LogSocket();
            String jSessionID = "";
            String pagina = "";
            List<String> listCookie = webSocketSession.getHandshakeHeaders().get(HttpHeaders.COOKIE);
            for (String cookie : listCookie) {
                String[] split = cookie.split(";");
                for (String string : split) {
                    if (string.startsWith(" ")) string = string.substring(1);
                    if (string.startsWith(JSESSIONID)) jSessionID = string.substring(JSESSIONID.length());
                    if (string.startsWith(PAGINA)) {
                        pagina = string.substring(PAGINA.length());
                        pagina = pagina.substring(pagina.indexOf("/") + 1);
                        pagina = pagina.substring(pagina.indexOf("/") + 1);
                        pagina = pagina.substring(pagina.indexOf("/") + 1);
                        if (pagina.trim().equalsIgnoreCase("")) pagina = "index.html";
                        pagina = pagina.substring(0, pagina.indexOf("."));
                    }
                }
            }
            List<LogSocket> list = l.get(jSessionID);
            if (list == null) list = new ArrayList<>();
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
            l.put(jSessionID, list);
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
    public Map<String, Object> leggi(@RequestBody Map<String, Object> body) throws Exception {
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        VoiceManager vm = VoiceManager.getInstance();
        Voice voice = vm.getVoice("kevin16");
        voice.allocate();
        AudioPlayer audioPlayer = new SingleFileAudioPlayer(".//src//main//webapp//riproduci", Type.WAVE);
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
        calUnder23 = Calendar.getInstance();
        calUnder23.add(Calendar.YEAR, -23);
        Map<String, Object> ret = new HashMap<>();
        Configurazione configurazione = getConfigurazione();
        if (configurazione == null || configurazione.getNumeroGiocatori() == null) {
            ret.put("DA_CONFIGURARE", "x");
        } else {
            String giocatoreLoggato = (String) httpSession.getAttribute("nomeGiocatoreLoggato");
            String idLoggato = (String) httpSession.getAttribute("idLoggato");
            sessionRegistry.pruneStaleBindings();
            if (giocatoreLoggato != null) {
                ret.put("giocatoreLoggato", giocatoreLoggato);
                ret.put("idLoggato", idLoggato);
                ret.put("onlineWs", sessionRegistry.isLoggedIn(giocatoreLoggato));
            }
            ret.put("utenti", sessionRegistry.getLoggedUserNames());
            Iterable<Allenatori> allAllenatori = getAllAllenatori();
            for (Allenatori allenatori : allAllenatori) {
                if (allenatori.getOrdine() == Integer.parseInt(getTurno())) {
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
            if (isATurni) {
                ret.put("isATurni", "S");
            } else {
                ret.put("isATurni", "N");
            }
            Boolean configIsSingle = configurazione.getIsSingle();
            if (configIsSingle == null) configIsSingle = false;
            setIsSingle(configIsSingle);
            if (getIsSingle()) {
                ret.put("isSingle", "S");
            } else {
                ret.put("isSingle", "N");
            }
            setIsMantra(configurazione.isMantra());
            if (getIsMantra()) {
                ret.put("isMantra", "S");
            } else {
                ret.put("isMantra", "N");
            }
            ret.put("numAcquisti", numAcquisti);
            ret.put("numMinAcquisti", numMinAcquisti);
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
            ret.put("numeroGiocatori", configurazione.getNumeroGiocatori());
            ret.put("nomeLega", resolveNomeLega(configurazione));
            ret.put("elencoAllenatori", allAllenatori);
            ret.put("nomeGiocatoreTurno", getNomeGiocatoreTurno());
            ret.put("giocatoriPerSquadra", giocatoriPerSquadra());
            ret.put("calciatori", getGiocatoriLiberi());
            ret.put("mapSpesoTotale", mapSpesoTotale);
            ret.put("turno", getTurno());
            aggiornaFavoriti((String) httpSession.getAttribute("idLoggato"));
            ret.put("preferiti", favoriti);
            ret.put("astaSnapshot", socketHandler.buildAstaSnapshot());
        }
        return ret;
    }


    /*
     curl -X POST "http://localhost:8081/restore" -H "accept: application/json" -H "Content-Type: application/json" -d "{ \"PATH\": \"C:\\restoreAs.txt\"}"
     */
    @PostMapping("/restore")
    @Transactional
    public Map<String, Object> restore(@RequestBody Map<String, Object> body) throws Exception {
        Map<String, Object> ret = new HashMap<>();
        String string = (String) body.get("PATH");
        List<String> readAllLines = Files.readAllLines(Paths.get(string));
        for (String sql : readAllLines) {
            if (sql.toUpperCase().startsWith("SELECT")) continue;
            if (sql.toUpperCase().startsWith("update giocatori set data_nascita".toUpperCase())) continue;
            if (sql.toUpperCase().startsWith("insert into giocatori_favoriti".toUpperCase())) continue;
            if (sql.toUpperCase().startsWith("CREATE")) {
                String tableName = sql.toUpperCase().replace("CREATE TABLE ", "");
                tableName = tableName.substring(0, tableName.indexOf(" "));
                try {
                    String sqlString = "DROP TABLE if exists " + tableName;
//					System.out.println(sqlString);
                    Query qy = em.createNativeQuery(sqlString);
                    qy.executeUpdate();
                } catch (Exception e) {
                    System.out.println("Drop table:" + tableName + " in errore");
                }
            }
            //			System.out.println(sql);
            Query qy = em.createNativeQuery(sql);
            try {
//				System.out.println(sql);
                qy.executeUpdate();
            } catch (Exception e) {
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
    public Map<String, Object> addFav(@RequestBody Map<String, Object> body) throws Exception {
        Map<String, Object> ret = new HashMap<>();
        if (body.get("idgiocatore") != null) {
            Object rawId = body.get("calciatoreId");
            Integer calciatoreId = rawId instanceof Number ? ((Number) rawId).intValue() : null;
            if (calciatoreId == null && rawId != null) {
                try {
                    calciatoreId = Integer.parseInt(rawId.toString());
                } catch (NumberFormatException ignored) {
                    calciatoreId = null;
                }
            }
            if (calciatoreId == null) {
                ret.put("errore", "calciatoreId mancante");
                return ret;
            }
            String idgiocatore = body.get("idgiocatore").toString();
            Boolean aggiungi = body.get("aggiungi") instanceof Boolean
                    ? (Boolean) body.get("aggiungi")
                    : Boolean.parseBoolean(String.valueOf(body.get("aggiungi")));
            if (Boolean.TRUE.equals(aggiungi)) {
                GiocatoriFavoriti favorite = new GiocatoriFavoriti();
                favorite.setIdAllenatore(Integer.parseInt(idgiocatore));
                favorite.setIdGiocatore(calciatoreId);
                favorite.setNota("");
                giocatoriFavoritiRepository.save(favorite);
            } else {
                GiocatoriFavoriti favorite = giocatoriFavoritiRepository.getFavorite(calciatoreId, Integer.parseInt(idgiocatore));
                if (favorite != null) {
                    giocatoriFavoritiRepository.delete(favorite);
                }
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
    public Map<String, Object> caricaFile(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Map<String, Object> ret = new HashMap<>();
        if (!isOkDispositiva(body)) {
            ret.put("esitoDispositiva", "KO");
            ret.put("errore", "Verifica dispositiva fallita");
            return ret;
        }
        try {
            byte[] byteContent = Base64.getDecoder().decode((String) body.get("file"));
            String tipoFile = (String) body.get("tipo");
            String fileName = body.get("fileName") != null ? body.get("fileName").toString() : "";
            tipoFile = resolveTipoFile(tipoFile, byteContent, fileName);
            giocatoriRepository.deleteAll();
            int count;
            if ("FS".equalsIgnoreCase(tipoFile)) {
                count = caricaFs(byteContent);
            } else if ("MANTRA".equalsIgnoreCase(tipoFile)) {
                count = caricaMantra(byteContent);
            } else {
                ret.put("esitoDispositiva", "KO");
                ret.put("errore", "Tipo file non riconosciuto: " + tipoFile);
                return ret;
            }
            if (count == 0) {
                ret.put("esitoDispositiva", "KO");
                ret.put("errore", "Nessun giocatore importato: verifica formato file");
                return ret;
            }
            socketHandler.notificaCaricaFile(request.getRemoteAddr());
            ret.put("esitoDispositiva", "OK");
            ret.put("numGiocatori", count);
        } catch (Exception e) {
            ret.put("esitoDispositiva", "KO");
            ret.put("errore", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
        return ret;
    }

    @PostMapping("/cancellaQuotazioni")
    @Transactional
    public Map<String, Object> cancellaQuotazioni(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        Map<String, Object> ret = new HashMap<>();
        if (!isOkDispositiva(body)) {
            ret.put("esitoDispositiva", "KO");
            ret.put("errore", "Verifica dispositiva fallita");
            return ret;
        }
        giocatoriRepository.deleteAll();
        giocatoriFavoritiRepository.deleteAll();
        socketHandler.notificaCaricaFile(request.getRemoteAddr());
        ret.put("esitoDispositiva", "OK");
        ret.put("numGiocatori", 0);
        return ret;
    }

    private String resolveTipoFile(String tipoFile, byte[] byteContent, String fileName) {
        if (isExcelContent(byteContent)) {
            return "FS";
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) {
            return "FS";
        }
        if (lower.endsWith(".txt")) {
            return "MANTRA";
        }
        return tipoFile;
    }

    private boolean isExcelContent(byte[] byteContent) {
        if (byteContent == null || byteContent.length < 4) {
            return false;
        }
        // OLE2 (.xls)
        if (byteContent[0] == (byte) 0xD0 && byteContent[1] == (byte) 0xCF) {
            return true;
        }
        // ZIP (.xlsx)
        return byteContent[0] == 'P' && byteContent[1] == 'K';
    }

    private int caricaFs(byte[] byteContent) throws Exception {
        try {
            return caricaFsXml(byteContent);
        } catch (SAXParseException e) {
            return caricaFsExcel(byteContent);
        }
    }

    private int caricaFsXml(byte[] byteContent) throws Exception {
        int count = 0;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        String content = new String(byteContent);
        InputSource is = new InputSource(new StringReader(content));
        Document parse = builder.parse(is);
        NodeList childNodes = parse.getChildNodes().item(0).getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (i > 0) {
                Node tr = childNodes.item(i);
                NodeList childNodesTr = tr.getChildNodes();
                String id = childNodesTr.item(0).getTextContent();
                String squadra = childNodesTr.item(3).getTextContent();
                String nome = childNodesTr.item(1).getTextContent() + " " + childNodesTr.item(2).getTextContent();
                String ruolo = childNodesTr.item(4).getTextContent();
                String quotazione = childNodesTr.item(6).getTextContent();
                Giocatori giocatori = new Giocatori();
                giocatori.setId(Integer.parseInt(id));
                giocatori.setNome(nome.trim());
                giocatori.setQuotazione(Integer.parseInt(quotazione));
                giocatori.setRuolo(ruolo);
                giocatori.setMacroRuolo(ruolo);
                giocatori.setSquadra(squadra);
                giocatoriRepository.save(giocatori);
                count++;
            }
        }
        return count;
    }

    private int caricaFsExcel(byte[] byteContent) throws Exception {
        int count = 0;
        DataFormatter formatter = new DataFormatter();
        try (InputStream targetStream = new ByteArrayInputStream(byteContent);
                Workbook workbook = WorkbookFactory.create(targetStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row currentRow : sheet) {
                if (currentRow == null) {
                    continue;
                }
                Integer id = parseCellInt(currentRow.getCell(0), formatter);
                if (id == null || id <= 0) {
                    continue;
                }
                Giocatori giocatori = new Giocatori();
                giocatori.setId(id);
                String cognome = formatCell(currentRow.getCell(1), formatter);
                String nome = formatCell(currentRow.getCell(2), formatter);
                giocatori.setNome((cognome + " " + nome).trim());
                giocatori.setSquadra(formatCell(currentRow.getCell(3), formatter));
                String ruolo = formatCell(currentRow.getCell(4), formatter);
                giocatori.setRuolo(ruolo);
                giocatori.setMacroRuolo(ruolo);
                Integer quotazione = parseCellInt(currentRow.getCell(6), formatter);
                giocatori.setQuotazione(quotazione != null ? quotazione : -1);
                if (currentRow.getLastCellNum() > 7) {
                    Cell fvmCell = currentRow.getCell(7);
                    if (fvmCell != null) {
                        try {
                            giocatori.setFvm(Double.parseDouble(formatter.formatCellValue(fvmCell).replace(",", ".")));
                        } catch (Exception ignored) {
                            // colonna FVM opzionale
                        }
                    }
                }
                giocatoriRepository.save(giocatori);
                count++;
            }
        }
        return count;
    }

    private int caricaMantra(byte[] byteContent) {
        int count = 0;
        String content = new String(byteContent);
        String[] split = content.split("\n");
        for (String riga : split) {
            if (riga == null || riga.trim().isEmpty()) {
                continue;
            }
            String[] colonne = riga.split("\t");
            if (colonne.length < 7) {
                continue;
            }
            try {
                Giocatori giocatori = new Giocatori();
                giocatori.setId(Integer.parseInt(colonne[0].trim()));
                giocatori.setNome(colonne[2].trim());
                try {
                    giocatori.setQuotazione(Integer.parseInt(colonne[6].replace("\r", "").trim()));
                } catch (Exception e) {
                    giocatori.setQuotazione(-1);
                }
                String ruolo = colonne[1].replaceAll("\"", "");
                giocatori.setRuolo(ruolo);
                String primoRuolo = ruolo;
                if (ruolo.indexOf(";") > 0) {
                    primoRuolo = ruolo.substring(0, ruolo.indexOf(";"));
                }
                giocatori.setMacroRuolo(macroRuoliMantra.get(primoRuolo));
                giocatori.setSquadra(colonne[3].trim());
                giocatoriRepository.save(giocatori);
                count++;
            } catch (Exception ignored) {
                // salta righe intestazione o malformate
            }
        }
        return count;
    }

    private String formatCell(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    private Integer parseCellInt(Cell cell, DataFormatter formatter) {
        String value = formatCell(cell, formatter);
        if (value.isEmpty()) {
            return null;
        }
        try {
            return (int) Double.parseDouble(value.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return (int) Double.parseDouble(value.toString().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean toBool(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value == null) {
            return false;
        }
        return "true".equalsIgnoreCase(value.toString()) || "S".equalsIgnoreCase(value.toString());
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
    public Map<String, Object> cancellaOfferta(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        Map<String, Object> ret = new HashMap<>();
        if (isOkDispositiva(body)) {
            Map<String, Object> mapOfferta = (Map) body.get("offerta");
            if (mapOfferta == null || mapOfferta.get("idGiocatore") == null) {
                ret.put("esitoDispositiva", "KO");
                ret.put("errore", "idGiocatore mancante");
                return ret;
            }
            Integer idGiocatore = toInt(mapOfferta.get("idGiocatore"));
            if (idGiocatore == null) {
                ret.put("esitoDispositiva", "KO");
                ret.put("errore", "idGiocatore non valido");
                return ret;
            }
            fantaroseRepository.deleteById(idGiocatore);
            socketHandler.notificaCancellaOfferta(mapOfferta, request.getRemoteAddr(), String.valueOf(idGiocatore));
            ret.put("ret", elencoCronologiaOfferte());
            ret.put("esitoDispositiva", "OK");
        } else {
            ret.put("esitoDispositiva", "KO");
        }
        return ret;
    }

    @PostMapping("/azzera")
    public Map<String, Object> azzera(@RequestBody Map<String, Object> body) throws Exception {
        Map<String, Object> ret = new HashMap<>();
        if (!isOkDispositiva(body)) {
            ret.put("esitoDispositiva", "KO");
            return ret;
        }
        if (body.get("conferma") != null && body.get("conferma").toString().equalsIgnoreCase("S")) {
            giocatoriRepository.deleteAll();
            fantaroseRepository.deleteAll();
            allenatoriRepository.deleteAll();
            giocatoriFavoritiRepository.deleteAll();
            loggerRepository.deleteAll();
            Configurazione configurazione = getConfigurazione();
            if (configurazione != null) {
                configurazione.setNumeroGiocatori(null);
                configurazione.setNomeLega(null);
                configurazioneRepository.save(configurazione);
            }
            socketHandler.disconnectAll();
            socketHandler.resetForDevTest();
        }
        ret.put("esitoDispositiva", "OK");
        return ret;
    }

    @PostMapping("/inizializzaLega")
    public Map<String, Object> inizializzaLega(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        Configurazione configurazione = getConfigurazione();
        Map<String, Object> ret = new HashMap<>();
        if (configurazione == null || configurazione.getNumeroGiocatori() == null) {
            Integer numUtenti = toInt(body.get("numUtenti"));
            if (numUtenti == null || numUtenti < 2) {
                ret.put("esitoDispositiva", "KO");
                ret.put("errore", "Numero partecipanti non valido");
                return ret;
            }
            setDurataAsta(toInt(body.get("durataAsta")));
            setBudget(toInt(body.get("budget")));
            setNumAcquisti(toInt(body.get("numAcquisti")));
            setNumMinAcquisti(toInt(body.get("numMinAcquisti")));
            setMaxP(toInt(body.get("maxP")));
            setMaxD(toInt(body.get("maxD")));
            setMaxC(toInt(body.get("maxC")));
            setMaxA(toInt(body.get("maxA")));
            setMinP(toInt(body.get("minP")));
            setMinD(toInt(body.get("minD")));
            setMinC(toInt(body.get("minC")));
            setMinA(toInt(body.get("minA")));
            isATurni = toBool(body.get("isATurni"));
            setIsSingle(toBool(body.get("isSingle")));
            setIsMantra(toBool(body.get("isMantra")));
            if (configurazione == null) configurazione = new Configurazione();
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
            String nomeLegaBody = body.get("nomeLega") != null ? body.get("nomeLega").toString().trim() : "";
            if (!nomeLegaBody.isEmpty()) {
                configurazione.setNomeLega(nomeLegaBody);
            }
            configurazioneRepository.save(configurazione);
            for (int i = 0; i < numUtenti; i++) {
                Allenatori al = new Allenatori();
                al.setId(i);
                al.setOrdine(i);
                if (i == 0) {
                    al.setIsAdmin(true);
                    String giocatoreLoggato = (String) httpSession.getAttribute("nomeGiocatoreLoggato");
                    if (giocatoreLoggato == null) {
                        al.setNome("GIOC0");
                    } else {
                        al.setNome(giocatoreLoggato);
                    }
                } else {
                    al.setIsAdmin(false);
                    al.setNome("GIOC" + i);
                }
                al.setPwd("");
                allenatoriRepository.save(al);
            }
            socketHandler.notificaInizializzaLega(request.getRemoteAddr());
            ret.put("esitoDispositiva", "OK");
            ret.put("utentiCreati", numUtenti);
            ret.put("nomeLega", resolveNomeLega(configurazione));
        } else {
            ret.put("esitoDispositiva", "KO");
            ret.put("errore", "Lega già inizializzata (" + configurazione.getNumeroGiocatori() + " partecipanti). Usa reset/azzera per ricominciare.");
        }
        return ret;
    }

    @PostMapping("aggiornaSessioneNomeUtente")
    public void aggiornaSessioneNomeUtente(@RequestBody Map<String, Object> body) {
        httpSession.setAttribute("nomeGiocatoreLoggato", (String) body.get("nuovoNome"));
    }

    @PostMapping("cancellaSessioneNomeUtente")
    public Map<String, Object> cancellaSessioneNomeUtente() {
        httpSession.removeAttribute("nomeGiocatoreLoggato");
        httpSession.removeAttribute("idLoggato");
        Map<String, Object> ret = new HashMap<>();
        ret.put("esito", "OK");
        return ret;
    }

    @PostMapping("/aggiornaConfigLega")
    public Map<String, Object> aggiornaConfigLega(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        Map<String, Object> ret = new HashMap<>();
        if (isOkDispositiva(body)) {
            Map<String, String> utentiRinominati = new HashMap<>();
            int i = 0;
            setBudget(toInt(body.get("budget")));
            setNumAcquisti(toInt(body.get("numAcquisti")));
            setNumMinAcquisti(toInt(body.get("numMinAcquisti")));
            setMaxP(toInt(body.get("maxP")));
            setMaxD(toInt(body.get("maxD")));
            setMaxC(toInt(body.get("maxC")));
            setMaxA(toInt(body.get("maxA")));
            setMinP(toInt(body.get("minP")));
            setMinD(toInt(body.get("minD")));
            setMinC(toInt(body.get("minC")));
            setMinA(toInt(body.get("minA")));
            int nuovaDurataAsta = toInt(body.get("durataAsta"));
            if (nuovaDurataAsta > 0) {
                setDurataAsta(nuovaDurataAsta);
            }
            Boolean admin = toBool(body.get("admin"));
            isATurni = toBool(body.get("isATurni"));
            setIsSingle(toBool(body.get("isSingle")));
            setIsMantra(toBool(body.get("isMantra")));
            List<Map<String, Object>> elencoAllenatori = (List<Map<String, Object>>) body.get("elencoAllenatori");
            if (elencoAllenatori == null) {
                ret.put("esitoDispositiva", "KO");
                ret.put("errore", "elencoAllenatori mancante");
                return ret;
            }
            for (Map<String, Object> map : elencoAllenatori) {
                Integer id = toInt(map.get("id"));
                if (id == null) {
                    continue;
                }
                Allenatori al = allenatoriRepository.findById(id).orElse(null);
                if (al == null) {
                    continue;
                }
                String nuovoNome = map.get("nuovoNome") != null ? map.get("nuovoNome").toString().trim() : al.getNome();
                if (nuovoNome.isEmpty()) {
                    nuovoNome = al.getNome();
                }
                String vecchioNome = al.getNome();
                String giocatoreLoggato = (String) httpSession.getAttribute("nomeGiocatoreLoggato");
                if (!vecchioNome.equalsIgnoreCase(nuovoNome)) {
                    utentiRinominati.put(vecchioNome, nuovoNome);
                    if (giocatoreLoggato != null && giocatoreLoggato.equalsIgnoreCase(vecchioNome)) {
                        ret.put("nuovoNomeLoggato", nuovoNome);
                        ret.put("vecchioNomeLoggato", vecchioNome);
                        httpSession.setAttribute("nomeGiocatoreLoggato", nuovoNome);
                    }
                }
                al.setNome(nuovoNome);
                String pwd = map.get("pwd") != null ? map.get("pwd").toString() : "";
                String storedPwd = al.getPwd() != null ? al.getPwd() : "";
                if (!pwd.equalsIgnoreCase(storedPwd)) {
                    al.setPwd(criptaggio.encrypt(pwd, nuovoNome));
                }
                if (toBool(map.get("isAdmin"))) {
                    al.setIsAdmin(true);
                } else {
                    al.setIsAdmin(false);
                }
                if (admin && map.get("ordine") != null) {
                    al.setOrdine(toInt(map.get("ordine")));
                }
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
            configurazione.setMantra(getIsMantra());
            String nomeLegaCfg = body.get("nomeLega") != null ? body.get("nomeLega").toString().trim() : "";
            if (!nomeLegaCfg.isEmpty()) {
                configurazione.setNomeLega(nomeLegaCfg);
            }
            configurazioneRepository.save(configurazione);
            syncConfigFromDb();
            log.info("[CONFIG] aggiornaConfigLega nomeLega={} budget={} durataAsta={}s isATurni={} isSingle={} isMantra={} maxP={} maxA={}",
                    configurazione.getNomeLega(), getBudget(), getDurataAsta(), isATurni, getIsSingle(), getIsMantra(), getMaxP(), getMaxA());
            if (isATurni) {
                ret.put("isATurni", "S");
            } else {
                ret.put("isATurni", "N");
            }
            if (getIsSingle()) {
                ret.put("isSingle", "S");
            } else {
                ret.put("isSingle", "N");
            }
            if (getIsMantra()) {
                ret.put("isMantra", "S");
            } else {
                ret.put("isMantra", "N");
            }
            ret.put("esitoDispositiva", "OK");
            ret.put("nomeLega", resolveNomeLega(configurazione));
            ret.put("budget", getBudget());
            ret.put("durataAsta", getDurataAsta());
            ret.put("numAcquisti", getNumAcquisti());
            ret.put("numMinAcquisti", getNumMinAcquisti());
            ret.put("maxP", getMaxP());
            ret.put("maxD", getMaxD());
            ret.put("maxC", getMaxC());
            ret.put("maxA", getMaxA());
            ret.put("minP", getMinP());
            ret.put("minD", getMinD());
            ret.put("minC", getMinC());
            ret.put("minA", getMinA());
            Allenatori adminSalvato = trovaAllenatoreAdminSalvato();
            if (adminSalvato != null) {
                String idLoggatoStr = (String) httpSession.getAttribute("idLoggato");
                Integer idLoggato = null;
                if (idLoggatoStr != null && !idLoggatoStr.isEmpty()) {
                    try {
                        idLoggato = Integer.parseInt(idLoggatoStr);
                    } catch (NumberFormatException ignored) {
                        idLoggato = null;
                    }
                }
                if (idLoggato == null || !idLoggato.equals(adminSalvato.getId())) {
                    httpSession.setAttribute("nomeGiocatoreLoggato", adminSalvato.getNome());
                    httpSession.setAttribute("idLoggato", String.valueOf(adminSalvato.getId()));
                    ret.put("sessioneAllenatore", adminSalvato.getNome());
                    ret.put("sessioneAllenatoreId", adminSalvato.getId());
                    log.info("[CONFIG] sessione allineata al nuovo admin: {} (id={})", adminSalvato.getNome(), adminSalvato.getId());
                }
            }
            socketHandler.aggiornaConfigLega(utentiRinominati, getAllAllenatori(), configurazione, request.getRemoteAddr());
        } else {
            ret.put("esitoDispositiva", "KO");
        }
        return ret;
    }

    private Allenatori trovaAllenatoreAdminSalvato() {
        Allenatori admin = null;
        for (Allenatori al : getAllAllenatori()) {
            if (Boolean.TRUE.equals(al.getIsAdmin())) {
                if (admin == null || (al.getOrdine() != null && admin.getOrdine() != null && al.getOrdine() < admin.getOrdine())) {
                    admin = al;
                }
            }
        }
        return admin;
    }

    @GetMapping("/cripta")
    public Map<String, String> cripta(@RequestParam(name = "pwd") String pwd, @RequestParam(name = "key") String key) throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("value", criptaggio.encrypt(pwd, key));
        return m;
    }
/*
	@GetMapping("/decripta")
	public String decripta(@RequestParam(name = "pwd") String pwd,@RequestParam(name = "key") String key) throws Exception {
		return criptaggio.decrypt(pwd, key);
	}
*/

    @PostMapping("/disconnectAll")
    public Map<String, Object> disconnectAll() throws Exception {
        Map<String, Object> ret = new HashMap<>();
        socketHandler.disconnectAll();
        ret.put("esitoDispositiva", "KO");
        return ret;
    }


    @PostMapping("/confermaAsta")
    public synchronized Map<String, Object> confermaAsta(@RequestBody Map<String, Object> body) throws Exception {
        Map<String, Object> ret = new HashMap<>();
        if (isOkDispositiva(body)) {
            Map<String, Object> offertaServer = socketHandler.getOffertaVincente();
            if (offertaServer == null || offertaServer.isEmpty() || offertaServer.get("idgiocatore") == null
                    || offertaServer.get("idCalciatore") == null || offertaServer.get("offerta") == null) {
                ret.put("esitoDispositiva", "KO");
                ret.put("errore", "Nessuna offerta attiva sul server");
                return ret;
            }
            String idgiocatore = offertaServer.get("idgiocatore").toString();
            String idCalciatore = offertaServer.get("idCalciatore").toString();
            Optional<Fantarose> findById = fantaroseRepository.findById(Integer.parseInt(idCalciatore));
            if (findById.isPresent()) {//ALTRIMENTI DA ERRORE DOPPIO INSERT CON DOPPIO ADMIN
                ret.put("insert", "OK");
            } else {
                Integer offerta = ((Number) offertaServer.get("offerta")).intValue();
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
            try {
                socketHandler.confirmAstaAndBroadcast("HTTP");
            } catch (IOException e) {
                ret.put("esitoDispositiva", "KO");
                ret.put("errore", "Conferma salvata ma broadcast fallito: " + e.getMessage());
                return ret;
            }
            ret.put("esitoDispositiva", "OK");
        } else {
            ret.put("esitoDispositiva", "KO");
            ret.put("errore", "Verifica dispositiva fallita");
        }
        return ret;
    }

    @PostMapping("/assegnaGiocatore")
    public synchronized Map<String, Object> assegnaGiocatore(@RequestBody Map<String, Object> body,
                                                             HttpServletRequest request) throws Exception {
        Map<String, Object> ret = new HashMap<>();
        if (!isOkDispositiva(body)) {
            ret.put("esitoDispositiva", "KO");
            ret.put("errore", "Verifica dispositiva fallita");
            return ret;
        }
        Integer idCalciatore = toInt(body.get("idCalciatore"));
        Integer idAllenatore = toInt(body.get("idAllenatore"));
        Integer costo = toInt(body.get("costo"));
        if (idCalciatore == null || idAllenatore == null || costo == null || costo < 1) {
            ret.put("esitoDispositiva", "KO");
            ret.put("errore", "Dati assegnazione non validi");
            return ret;
        }
        if (fantaroseRepository.findById(idCalciatore).isPresent()) {
            ret.put("esitoDispositiva", "KO");
            ret.put("errore", "Giocatore già assegnato");
            return ret;
        }
        Optional<Giocatori> giocatoreOpt = giocatoriRepository.findById(idCalciatore);
        if (!giocatoreOpt.isPresent()) {
            ret.put("esitoDispositiva", "KO");
            ret.put("errore", "Giocatore non trovato");
            return ret;
        }
        Allenatori allenatore = null;
        for (Allenatori a : getAllAllenatori()) {
            if (a.getId() != null && a.getId().intValue() == idAllenatore.intValue()) {
                allenatore = a;
                break;
            }
        }
        if (allenatore == null) {
            ret.put("esitoDispositiva", "KO");
            ret.put("errore", "Squadra non trovata");
            return ret;
        }
        Giocatori g = giocatoreOpt.get();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Fantarose fantarosa = new Fantarose();
        fantarosa.setCosto(costo);
        fantarosa.setIdAllenatore(idAllenatore);
        fantarosa.setIdGiocatore(idCalciatore);
        fantarosa.setSqlTime(sdf.format(c.getTime()));
        fantaroseRepository.save(fantarosa);
        try {
            socketHandler.assegnaDirettoAndBroadcast(
                    request.getRemoteAddr(),
                    g.getNome(),
                    g.getRuolo(),
                    g.getSquadra(),
                    allenatore.getNome(),
                    costo);
        } catch (IOException e) {
            ret.put("esitoDispositiva", "KO");
            ret.put("errore", "Assegnazione salvata ma broadcast fallito: " + e.getMessage());
            return ret;
        }
        ret.put("esitoDispositiva", "OK");
        ret.put("insert", "OK");
        return ret;
    }

	/*
	@RequestMapping("/x")
	public Iterable<Fantarose> x() {
		return fantaroseRepository.x();
	}
	*/

    @RequestMapping("/spesoAllenatori")
    public List<Map<String, Object>> spesoAllenatori() {
        try {
            String sql = "select sum(costo) costo, a.nome from fantarose f, allenatori a where a.id = idAllenatore group by a.nome";
            Query qy = em.createNativeQuery(sql);
            List<Object[]> resultList = qy.getResultList();
            List<Map<String, Object>> ret = new ArrayList<>();
            for (Object[] row : resultList) {
                Map<String, Object> m = new HashMap<>();
                m.put("costo", row[0]);
                m.put("nome", row[1]);
                ret.add(m);
            }
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Value("${security.user.name}")
    private String nomeLegaDefault;

    private String resolveNomeLega(Configurazione configurazione) {
        if (configurazione != null && configurazione.getNomeLega() != null) {
            String n = configurazione.getNomeLega().trim();
            if (!n.isEmpty()) {
                return n;
            }
        }
        return nomeLegaDefault != null ? nomeLegaDefault : "FantaAsta";
    }

    private String resolveNomeLega() {
        return resolveNomeLega(getConfigurazione());
    }

    @RequestMapping(value = "/esportaMantra")
    public void esportaMantra(HttpServletResponse response) throws IOException {
        String csvFileName = resolveNomeLega() + "_export_per_sito.csv";
        response.setContentType("text/csv");
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
        response.setHeader(headerKey, headerValue);
        Iterable<ExportMantra> exportMantra = fantaroseRepository.exportMantra();
        String oldAll = "";
        StringBuilder s = new StringBuilder();
        for (ExportMantra ex : exportMantra) {
            if (!ex.getNome().equalsIgnoreCase(oldAll)) {
                oldAll = ex.getNome();
                s.append("$,$,$\n");
            }
            s.append(ex.getNome() + "," + ex.getIdGiocatore() + "," + ex.getCosto() + "\n");
        }
        response.getWriter().print(s);
    }

    @RequestMapping(value = "/esporta")
    public void esporta(HttpServletResponse response) throws IOException {
        String csvFileName = resolveNomeLega() + "_export.csv";
        response.setContentType("text/csv");
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
        response.setHeader(headerKey, headerValue);
        Map<String, Map<String, Object>> giocatoriPerSquadra = giocatoriPerSquadra();
        Iterator<String> iterator = giocatoriPerSquadra.keySet().iterator();
        StringBuilder s = new StringBuilder();
        s.append("allenatore" + ";\"" + "ruolo" + "\";" + "costo" + ";" + "squadra" + ";" + "giocatore" + "\n");
        while (iterator.hasNext()) {
            String allenatore = (String) iterator.next();
            Map m = (Map) giocatoriPerSquadra.get(allenatore).get("ruoli");
            Collection<List> giocatori = m.values();
            for (List<Map> giocatore : giocatori) {
                for (Map map : giocatore) {
                    s.append(allenatore + ";\"" + map.get("ruolo") + "\";" + map.get("costo") + ";" + map.get("squadra") + ";" + map.get("giocatore") + "\n");
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
            if (tmp == null) tmp = new HashMap();
            tmp.put("speso", tmp.get("speso") == null ? speso.getCosto() : tmp.get("speso") + speso.getCosto());
            tmp.put("conta", tmp.get("conta") == null ? speso.getConta() : tmp.get("conta") + speso.getConta());


            long quantiDaPrendere = 0;
            if (numMinAcquisti < tmp.get("conta")) {
                quantiDaPrendere = 0;
            } else {
                quantiDaPrendere = numMinAcquisti - tmp.get("conta");
            }
            //budget-quantiDaPrendere-speso
            int adding = 1;
            if (quantiDaPrendere < 1) adding = 0;
            tmp.put("maxRilancio", budget - quantiDaPrendere - tmp.get("speso") + adding);
//			System.out.println(budget + ";"+speso.getNome() +";"+tmp.get("speso") + ";" + numMinAcquisti + ";" + tmp.get("conta") + ";" + quantiDaPrendere + ";");
            tmp.put("speso" + speso.getMacroRuolo(), speso.getCosto());
            tmp.put("conta" + speso.getMacroRuolo(), speso.getConta());
//			if(!speso.getMacroRuolo().equalsIgnoreCase("P")) {
            tmp.put("spesoAll", (tmp.get("spesoAll") == null ? 0 : tmp.get("spesoAll")) + speso.getCosto());
            tmp.put("contaAll", (tmp.get("contaAll") == null ? 0 : tmp.get("contaAll")) + speso.getConta());
//			}
            getMapSpesoTotale().put(speso.getNome(), tmp);
        }
        Iterable<GiocatoriPerSquadra> giocatoriPerSquadra = fantaroseRepository.giocatoriPerSquadra();
        Map<String, Map<String, Object>> ret = new LinkedHashMap<>();
        for (GiocatoriPerSquadra giocatorePerSquadra : giocatoriPerSquadra) {
            String allenatore = giocatorePerSquadra.getAllenatore();
            Map<String, Long> spese = getMapSpesoTotale().get(allenatore);
            Map<String, List<Map<String, Object>>> mapRuoli = null;
            if (ret.get(allenatore) != null)
                mapRuoli = (Map<String, List<Map<String, Object>>>) ret.get(allenatore).get("ruoli");
            if (mapRuoli == null) {
                mapRuoli = new LinkedHashMap<>();
            }
            String ruolo = giocatorePerSquadra.getMacroRuolo();
            List<Map<String, Object>> list = mapRuoli.get(ruolo);
            if (list == null) {
                list = new ArrayList<>();
            }
            Map<String, Object> riga = new HashMap<>();
            riga.put("ruolo", giocatorePerSquadra.getRuolo());
            riga.put("giocatore", giocatorePerSquadra.getGiocatore());
            if (giocatorePerSquadra.getDataNascita() != null && giocatorePerSquadra.getDataNascita().get(Calendar.YEAR) < 2020 && giocatorePerSquadra.getDataNascita().after(calUnder23)) {
                riga.put("under23", "*");
            }
            riga.put("squadra", giocatorePerSquadra.getSquadra());
            riga.put("costo", giocatorePerSquadra.getCosto());
            riga.put("idGiocatore", giocatorePerSquadra.getIdGiocatore());
            riga.put("idAllenatore", giocatorePerSquadra.getIdAllenatore());
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
    public Iterable<SpesoTotale> spesoTotale() {
        return fantaroseRepository.spesoTotale();
    }

    @RequestMapping("/elencoCronologiaOfferte")
    public List<Map<String, Object>> elencoCronologiaOfferte() {
        try {
            String sql = "select a.Nome allenatore, g.Squadra, g.Ruolo, g.nome giocatore, Costo, sqlTime, idGiocatore, idAllenatore   from  fantarose f, " +
                    "giocatori g, allenatori a  where g.id = idGiocatore and a.id = idAllenatore order by sqlTime desc";
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
                m.put("idGiocatore", row[6]);
                m.put("idAllenatore", row[7]);
                ret.add(m);
            }
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping("/elencoOfferte")
    public List<Map<String, Object>> elencoOfferte() {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectMapper mapper = new ObjectMapper();

    public String toJson(Object o) {
        try {
            byte[] data = mapper.writeValueAsBytes(o);
            return new String(data);//, Charsets.ISO_8859_1
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Map<String, Object>> jsonToList(String json) {
        try {
            return mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping("/riepilogoAllenatori")
    public List<Map<String, Object>> riepilogoAllenatori() {
        try {
            String sql = "select count(ruolo) conta, ruolo, a.nome nome from fantarose f, allenatori a, giocatori g where g.id=idGiocatore " +
                    "and a.id = idAllenatore group by a.nome ,ruolo order by a.nome, ruolo desc";
            Query qy = em.createNativeQuery(sql);
            List<Object[]> resultList = qy.getResultList();
            List<Map<String, Object>> ret = new ArrayList<>();
            for (Object[] row : resultList) {
                Map<String, Object> m = new HashMap<>();
                m.put("conta", row[0]);
                m.put("ruolo", row[1]);
                m.put("nome", row[2]);
                ret.add(m);
            }
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //	@Cacheable(cacheNames = "allenatori")
    @GetMapping(path = "/allAllenatori")
    public @ResponseBody Iterable<Allenatori> getAllAllenatori() {
        Iterable<Allenatori> findAll = allenatoriRepository.getAllenatoriOrderByOrdine();
        for (Allenatori allenatori : findAll) {
            allenatori.setNuovoNome(allenatori.getNome());
        }
        return findAll;
    }

    @GetMapping(path = "/allFantarose")
    public @ResponseBody Iterable<Fantarose> getAllFantarose() {
        return fantaroseRepository.findAll();
    }

    @GetMapping(path = "/allGiocatori")
    public @ResponseBody Iterable<Giocatori> getAllGiocatori() {
        return giocatoriRepository.findAll();
    }

    @GetMapping(path = "/elencoLoggerMessaggi")
    public @ResponseBody Iterable<LoggerMessaggi> elencoLoggerMessaggi() {
        return loggerRepository.findAll();
    }

    @GetMapping(path = "/configurazione")
    public @ResponseBody Configurazione getConfigurazione() {
        Iterator<Configurazione> iterator = configurazioneRepository.findAll().iterator();
        if (!iterator.hasNext()) return null;
        return iterator.next();
    }

    @GetMapping(path = "/giocatoriLiberi")
    public @ResponseBody List<Map<String, Object>> getGiocatoriLiberi() {
		/*
		Page<Object[]> resultListPaginata = giocatoriRepository.getGiocatoriLiberiPaginati(new PageRequest(2, 3));
		System.out.println(resultListPaginata.getNumber());
		*/
        List<Object[]> resultList = giocatoriRepository.getGiocatoriLiberi();
        List<Map<String, Object>> ret = new ArrayList<>();
        for (Object[] row : resultList) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", row[0]);
            m.put("squadra", row[1]);
            m.put("nome", row[2]);
            m.put("ruolo", row[3]);
            m.put("macroRuolo", row[4]);
            m.put("quotazione", row[5]);
            m.put("fvm", row[7]);
            if (row[6] != null) {
                Calendar c = (Calendar) row[6];
                if (c.get(Calendar.YEAR) < 2020 && c.after(calUnder23)) m.put("under23", "SI");
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

    /** Allinea durata asta in-memory con il valore persistito in configurazione. */
    public void syncDurataAstaFromDb() {
        syncConfigFromDb();
    }

    /** Allinea tutta la configurazione in-memory con i valori persistiti nel DB. */
    public void syncConfigFromDb() {
        Configurazione configurazione = getConfigurazione();
        if (configurazione == null) {
            return;
        }
        if (configurazione.getBudget() != null) {
            setBudget(configurazione.getBudget());
        }
        if (configurazione.getDurataAsta() != null && configurazione.getDurataAsta() > 0) {
            setDurataAsta(configurazione.getDurataAsta());
        }
        if (configurazione.getNumeroAcquisti() != null) {
            setNumAcquisti(configurazione.getNumeroAcquisti());
        }
        if (configurazione.getNumeroMinAcquisti() != null) {
            setNumMinAcquisti(configurazione.getNumeroMinAcquisti());
        }
        setMaxP(configurazione.getMaxP());
        setMaxD(configurazione.getMaxD());
        setMaxC(configurazione.getMaxC());
        setMaxA(configurazione.getMaxA());
        setMinP(configurazione.getMinP());
        setMinD(configurazione.getMinD());
        setMinC(configurazione.getMinC());
        setMinA(configurazione.getMinA());
        if (configurazione.getIsATurni() != null) {
            isATurni = configurazione.getIsATurni();
        }
        if (configurazione.getIsSingle() != null) {
            setIsSingle(configurazione.getIsSingle());
        }
        setIsMantra(configurazione.isMantra());
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

    public Map<Integer, List<Integer>> getFavoriti() {
        return favoriti;
    }

    public void setFavoriti(Map<Integer, List<Integer>> favoriti) {
        this.favoriti = favoriti;
    }

    public Boolean getIsSingle() {
        return isSingle;
    }

    public void setIsSingle(Boolean isSingle) {
        this.isSingle = isSingle;
    }

}
