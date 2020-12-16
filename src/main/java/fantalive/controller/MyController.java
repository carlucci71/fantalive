package fantalive.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fantalive.bl.FantaLiveBOT;
import fantalive.bl.Main;
import fantalive.configurazione.SocketHandler;
import fantalive.entity.Salva;
import fantalive.model.Giocatore;
import fantalive.model.Return;
import fantalive.model.Squadra;
import fantalive.repository.SalvaRepository;
import fantalive.util.Constant;

@Component
@RestController
@RequestMapping({ "/" })
public class MyController {

	
	@Autowired SocketHandler socketHandler;
	@Autowired SalvaRepository salvaRepository;

	@PostConstruct
	private void post() throws Exception {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
		if(System.getenv("KEEP_ALIVE_START") != null) {
			Constant.KEEP_ALIVE_START = LocalDateTime.parse(System.getenv("KEEP_ALIVE_START"), formatter);
		}
		else {
			Constant.KEEP_ALIVE_START = LocalDateTime.now();
		}
		if(System.getenv("KEEP_ALIVE_END") != null) {
			Constant.KEEP_ALIVE_END = LocalDateTime.parse(System.getenv("KEEP_ALIVE_END"), formatter);
		}
		else {
			Constant.KEEP_ALIVE_END = LocalDateTime.now();
		}
		Constant.DISABILITA_NOTIFICA_TELEGRAM = Boolean.valueOf(System.getenv("DISABILITA_NOTIFICA_TELEGRAM"));
		Constant.LIVE_FROM_FILE = Boolean.valueOf(System.getenv("LIVE_FROM_FILE"));
		Constant.CHAT_ID_FANTALIVE = Long.valueOf(System.getenv("CHAT_ID_FANTALIVE"));
		Constant.SPONTIT_KEY = System.getenv("SPONTIT_KEY");
		Constant.TOKEN_BOT_FANTALIVE = System.getenv("TOKEN_BOT_FANTALIVE");
		Constant.SPONTIT_USERID = System.getenv("SPONTIT_USERID");
		Constant.APPKEY_FG = System.getenv("APPKEY_FG");
		Constant.AUTH_FS = System.getenv("AUTH_FS");
		Constant.GIORNATA = Integer.valueOf(System.getenv("GIORNATA"));
		Main.init(salvaRepository,socketHandler);
		if (!Constant.DISABILITA_NOTIFICA_TELEGRAM) {
			Main.fantaLiveBot = FantaLiveBOT.inizializza("WEBAPP");
		}
	}
	@Scheduled(fixedRate = 1500000)
	@GetMapping("/getMyFile")
	public String getFile() throws Exception {
		String ret="";
		LocalDateTime now = LocalDateTime.now();
		if (Constant.KEEP_ALIVE_START.isBefore(now) && Constant.KEEP_ALIVE_END.isAfter(now)) {
			String http = Main.getHTTP("https://fantalive71.herokuapp.com/");
			ret="Keep Alive!";
			Main.inviaNotifica(ret);
		} else {
			ret = Constant.KEEP_ALIVE_START.toString() + "/" + Constant.KEEP_ALIVE_END.toString() + "-->" + now.toString();
			System.out.println(ret);
		}
		return ret;
	}
	@Scheduled(fixedRate = 5000)
	public void chckNotifica() throws Exception {
		int conta = (int) Main.toSocket.get("timeRefresh");
		if (conta==20000) {//FIXME 20000
			conta=0;
			Main.snapshot();
		}
		conta=conta+5000;
		Main.toSocket.put("timeRefresh", conta);
		Main.toSocket.put("liveFromFile", Constant.LIVE_FROM_FILE);
		Main.toSocket.put("disabilitaNotificaTelegram", Constant.DISABILITA_NOTIFICA_TELEGRAM);
		
		String runningBot="STOPPED";
		if (Main.fantaLiveBot != null && Main.fantaLiveBot.isRunning()) {
			runningBot="RUNNING";
		}
		Main.toSocket.put("runningBot", runningBot);
		socketHandler.invia(Main.toSocket );
	}
	@RequestMapping("/getOrariFromDb")
	public Map<String, String> getOrariFromDb() throws Exception {
		Map<String, String> ret = new HashMap<>();
		ret.put("file", Main.getTesto("orari.json"));
		return ret;
	}
	@RequestMapping("/getLivesFromDb")
	public Map<String, String> getLivesFromDb() throws Exception {
		Map<String, String> ret = new HashMap<>();
		ret.put("file", Main.getTesto("lives.json"));
		return ret;
	}
	@PostMapping("/getFreeFromDb")
	public Map<String, String> getFreeFromDb(@RequestBody Map<String, String> body) throws Exception {
		Map<String, String> ret = new HashMap<>();
		ret.put("file", Main.getTesto(body.get("nomeFileGet")));
		return ret;
	}
	@RequestMapping("/getOrariFromLive")
	public Map<String, String> getOrariFromLive() throws Exception {
		Map<String, String> ret = new HashMap<>();
		Map<String, Object> lives = Main.getLives(false);
		ret.put("file", Main.toJson(lives.get("orari")));
		return ret;
	}
	@RequestMapping("/getLivesFromLive")
	public Map<String, String> getLivesFromLive() throws Exception {
		Map<String, String> ret = new HashMap<>();
		Map<String, Object> lives = Main.getLives(false);
		ret.put("file", Main.toJson(lives.get("lives")));
		return ret;
	}
	@PostMapping("/getTestiFromData")
	public Map<String, String> getTestiFromData(@RequestBody Map<String,String> body) throws Exception {
		Map<String, String>  ret=new HashMap<>();
		List<Salva> valTesto = Main.getValTesto(body.get("getTestiFromData"));
		for (Salva salva : valTesto) {
			ret.put(salva.getNome().substring(24).replace(".json", ""), salva.getTesto());
		}
		return ret;
	}
	@PostMapping("/caricaFileFromDataByName")
	public Map<String, String>  caricaFileFromDataByName(@RequestBody Map<String,String> body) throws Exception {
		Map<String, String>  ret=new HashMap<>();
		String orari = Main.getTesto(body.get("name")+"-orari.json");
		String lives = Main.getTesto(body.get("name")+"-lives.json");
		Main.upsertSalva("orari.json", orari);
		Main.upsertSalva("lives.json", lives);
		ret.put("orari", orari );
		ret.put("lives", lives );
		return ret;
	}
	@PostMapping("/caricaFileFromData")
	public void caricaFileFromData(@RequestBody Map<String,Object> body) throws Exception {
		Main.upsertSalva("orari.json", (String) body.get("orari"));
		Main.upsertSalva("lives.json", (String) body.get("lives"));
	}
	@PostMapping("/caricaFile")
	public void caricaFile(@RequestBody Map<String,Object> body) throws Exception {
		String content = (String) body.get("file");
		String tipoFile = (String) body.get("tipoFile");
		if (tipoFile.equalsIgnoreCase("O")) {
			Main.upsertSalva("orari.json", content);
		}
		if (tipoFile.equalsIgnoreCase("L")) {
			Main.upsertSalva("lives.json", content);
		}
		if (tipoFile.equalsIgnoreCase("Free")) {
			Main.upsertSalva((String) body.get("nomeFileGet"), content);
		}
	}
	@RequestMapping("/getNomiTesto")
	public Map<String, Object> getNomiTesto() throws Exception {
		Map<String, Object> ret = new HashMap<>();
		ret.put("nomiTesto", Main.getNomiTesto("%"));
		return ret;
	}
	@RequestMapping("/getNomiData")
	public Map<String, Object> getNomiData() throws Exception {
		Map<String, Object> ret = new HashMap<>();
		List<String> nomiTesto = Main.getNomiTesto("202");
		List<String> lista=new ArrayList<>();
		for (String testo : nomiTesto) {
			String data = testo.substring(0,23);
			if (!lista.contains(data)) {
				lista.add(data);
			}
		}
		ret.put("nomiTesto", lista);
		return ret;
	}
	@RequestMapping("/svecchiaFile")
	public Map<String, Object> svecchiaFile() throws Exception {
		Map<String, Object> ret = new HashMap<>();
		Main.svecchiaFile();
		ret.put("nomiTesto", Main.getNomiTesto("%"));
		return ret;
	}
	@RequestMapping("/test")
	public Map<String, Return>  test(boolean conLive) throws Exception {
		Map<String, Return> go = Main.go(conLive,null, null);
		return go;
	}
	/*
	@GetMapping("/nomiSquadre")
	public List<String> getNomiSquadre() throws Exception {
		return Main.getNomiSquadre();
	}
	 */

	@PostMapping("/startStopBot")
	public void  startStopBot() throws Exception  {
		if (Main.fantaLiveBot.isRunning()) {
			Main.fantaLiveBot.stopBot();
		} else {
			Main.fantaLiveBot.startBot();
		}
	}
	@PostMapping("/salva")
	public Map<String, Return> salva(@RequestBody Map<String,Return> body) throws Exception  {
		Return r = body.get("r");
//		Files.write(Paths.get(Main.ROOT + "fomrazioneFG" + r.getNome().toLowerCase() + ".json"), Main.toJson(r.getSquadre()).getBytes());
		Main.upsertSalva("fomrazioneFG" + r.getNome().toLowerCase() + ".json", Main.toJson(r.getSquadre()));
		return test(true);
	}
	@PostMapping("/simulaCambi")
	public Squadra simulaCambi(@RequestBody Map<String,Squadra> body)  {
		Squadra sq = body.get("sq");
		Squadra squadra = new Squadra();
		squadra.setDeltaModificatore(sq.getDeltaModificatore());
		squadra.setNome(sq.getNome());
		squadra.setEvidenza(sq.isEvidenza());
		List<Giocatore> nuovaListaGiocatori=new ArrayList<Giocatore>();
		int iContaPosizione=0;
		for (Giocatore giocatore : sq.getTitolari()) {
			if (giocatore.isCambio()) {
				nuovaListaGiocatori.add(findPerScambio(sq.getRiserve(),iContaPosizione));
				iContaPosizione++;
			} else {
				nuovaListaGiocatori.add(giocatore);
			}
		}
		squadra.setTitolari(nuovaListaGiocatori);

		nuovaListaGiocatori=new ArrayList<Giocatore>();
		iContaPosizione=0;
		for (Giocatore giocatore : sq.getRiserve()) {
			if (giocatore.isCambio()) {
				nuovaListaGiocatori.add(findPerScambio(sq.getTitolari(),iContaPosizione));
				iContaPosizione++;
			} else {
				nuovaListaGiocatori.add(giocatore);
			}
		}
		squadra.setRiserve(nuovaListaGiocatori);
		return squadra;
	}
	private Giocatore findPerScambio(List<Giocatore> giocatori, int iPosScambio) {
		int iConta=0;
		for (Giocatore giocatore : giocatori) {
			if (giocatore.isCambio()) {
				if (iConta==iPosScambio) {
					giocatore.setCambiato(!giocatore.isCambiato());
					return giocatore;
				}
				iConta++;
			}
		}
		return null;
	}
	@PostMapping("/addSqEv")
	public Map<String, Return>  addSqEv(@RequestBody Map<String,String> body) throws Exception  {
		return Main.go(true,body.get("sqEv"),null);
	}
	@PostMapping("/delSqEv")
	public Map<String, Return>  delSqEv(@RequestBody Map<String,String> body) throws Exception  {
		return Main.go(true,null,body.get("sqEv"));
	}
	@PostMapping("/setGiornata")
	public Map<String, Object> setGiornata(@RequestBody Map<String,Object> body)  {
		Map<String, Object> ret = new HashMap<String, Object>();
		Constant.GIORNATA=(Integer)body.get("giornata");
		return ret;
	}
	@GetMapping("/getDati")
	public Map<String, Object> getDati() {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("body", Constant.AUTH_FS);
		ret.put("giornata", Constant.GIORNATA);
		ret.put("eventi", Main.eventi);
		return ret;
	}
	@PostMapping("/setFantaSoccerAuth")
	public void setFantaSoccerAuth(@RequestBody Map<String,String> body)  {
		Constant.AUTH_FS=body.get("body");
	}
	@PostMapping("/preparaSquadre")
	public void preparaSquadre(@RequestBody Map<String,Integer> body) throws Exception {
		try
		{
			Main.aggKeyFG();
			Main.cancellaSquadre();
			Main.getSquadre("luccicar");
			Main.getSquadre("fanta-viva");
			Main.scaricaBe();
			List<Squadra> squadre = new ArrayList<Squadra>();
			for (int i=0;i<Main.NUM_PARTITE_FS;i++) {
				String nome = "be"+i + ".html";
				String testo=Main.getTesto(nome);
				Document doc = Jsoup.parse(testo);
				squadre.add(Main.getFromFS(doc, "Casa"));
				squadre.add(Main.getFromFS(doc, "Trasferta"));
				Main.cancellaSalva(nome);
				/*
				String nomeFile = Main.ROOT + "be"+i + ".html";
				if (Files.exists(Paths.get(nomeFile))) {
					byte[] inputS = Files.readAllBytes(Paths.get(nomeFile));
					Document doc = Jsoup.parse(new String(inputS));
					squadre.add(Main.getFromFS(doc, "Casa"));
					squadre.add(Main.getFromFS(doc, "Trasferta"));
					Files.delete(Paths.get(nomeFile));
				}
				*/
			}
//			Files.write(Paths.get(Main.ROOT + "fomrazioneFG" + "be" + ".json"), Main.toJson(squadre).getBytes());
			Main.upsertSalva("fomrazioneFG" + "be" + ".json", Main.toJson(squadre));
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
	}

}
