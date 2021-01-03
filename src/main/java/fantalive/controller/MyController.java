package fantalive.controller;

import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fantalive.bl.FantaCronacaLiveBOT;
import fantalive.bl.FantaLiveBOT;
import fantalive.bl.Main;
import fantalive.bl.Main.Campionati;
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


	@Autowired Constant constant;
	@Autowired SocketHandler socketHandler;
	@Autowired SalvaRepository salvaRepository;

	@PostConstruct
	private void post() throws Exception {
		Main.init(salvaRepository,socketHandler,constant);
		if (!constant.DISABILITA_NOTIFICA_TELEGRAM) {
			Main.fantaLiveBot = FantaLiveBOT.inizializza("WEBAPP");
			Main.fantaCronacaLiveBot = FantaCronacaLiveBOT.inizializza("WEBAPP");
		}
	}


	@Scheduled(fixedRate = 60000)
	public void scheduleKeepAlive() throws Exception {
		keepAlive();
	}
	@GetMapping("/getMyFile")
	public String getFile() throws Exception {
		return keepAlive();
	}
	private String keepAlive() throws Exception {
		String ret="";
		ZonedDateTime now = ZonedDateTime.now();
		if (constant.KEEP_ALIVE_END.isAfter(now)) {
			long between = 0;
			if (Constant.LAST_REFRESH != null) between = ChronoUnit.MINUTES.between(Constant.LAST_REFRESH,now);
			if (between >25) {
				String http = Main.getHTTP("https://fantalive71.herokuapp.com/");
				ret=Constant.KEEP_ALIVE + " Keep Alive!";
				Main.inviaCronacaNotifica(ret);
				System.out.println("REFRESH!!");
				Constant.LAST_REFRESH=ZonedDateTime.now();
			}
			else {
				System.out.println("non ancora refresh:" + between);
				ret = "NON ANCORA REFRESH:" + between;
			}
		} else {
			ret = Constant.dateTimeFormatterOut.format(now) + " --> " + Constant.dateTimeFormatterOut.format(constant.KEEP_ALIVE_END);
			System.out.println("NON NECESSARIO REFRESH!!");
		}
		return ret;
	}
	@Scheduled(fixedRate = 5000)
	public void chckNotifica() throws Exception {
		Main.timeRefresh = (int) Main.toSocket.get("timeRefresh");
		if (Main.timeRefresh==Constant.SCHEDULED_SNAP) {
			Main.snapshot(true);
		}
		Main.timeRefresh=Main.timeRefresh+5000;
		Main.toSocket.put("timeRefresh", Main.timeRefresh);
		Main.toSocket.put("liveFromFile", constant.LIVE_FROM_FILE);
		Main.toSocket.put("disabilitaNotificaTelegram", constant.DISABILITA_NOTIFICA_TELEGRAM);
		if (Constant.LAST_KEEP_ALIVE != null) Main.toSocket.put("lastKeepAlive", Constant.dateTimeFormatterOut.format(Constant.LAST_KEEP_ALIVE));
		if (Constant.LAST_REFRESH != null) Main.toSocket.put("lastRefresh", Constant.dateTimeFormatterOut.format(Constant.LAST_REFRESH));
		Main.toSocket.put("keepAliveEnd", Constant.dateTimeFormatterOut.format(Constant.KEEP_ALIVE_END));
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
		ret.put("file", Main.getTesto("orari"));
		return ret;
	}
	@RequestMapping("/getLivesFromDb")
	public Map<String, String> getLivesFromDb() throws Exception {
		Map<String, String> ret = new HashMap<>();
		ret.put("file", Main.getTesto("lives"));
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
			ret.put(salva.getNome().substring(24), salva.getTesto());
		}
		return ret;
	}
	@PostMapping("/verificaNotifica")
	public Map<String, String>  verificaNotifica(@RequestBody Map<String,String> body) throws Exception {
		Map<String, String>  ret=new HashMap<>();
		Main.inviaNotifica(Main.getDettaglio(Constant.CHAT_ID_FANTALIVE,body.get("campionato"),body.get("squadra")));
		return ret;
	}
	@PostMapping("/caricaFileFromDataByName")
	public Map<String, String>  caricaFileFromDataByName(@RequestBody Map<String,String> body) throws Exception {
		Map<String, String>  ret=new HashMap<>();
		String orari = Main.getTesto(body.get("name")+"-orari");
		String lives = Main.getTesto(body.get("name")+"-lives");
		Main.upsertSalva("orari", orari);
		Main.upsertSalva("lives", lives);
		Main.snapshot(false);
		ret.put("orari", orari );
		ret.put("lives", lives );
		return ret;
	}
	@PostMapping("/caricaFileFromData")
	public void caricaFileFromData(@RequestBody Map<String,Object> body) throws Exception {
		Main.upsertSalva("orari", (String) body.get("orari"));
		Main.upsertSalva("lives", (String) body.get("lives"));
	}
	@PostMapping("/caricaFile")
	public void caricaFile(@RequestBody Map<String,Object> body) throws Exception {
		String content = (String) body.get("file");
		String tipoFile = (String) body.get("tipoFile");
		if (tipoFile.equalsIgnoreCase("O")) {
			Main.upsertSalva("orari", content);
		}
		if (tipoFile.equalsIgnoreCase("L")) {
			Main.upsertSalva("lives", content);
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
			Main.fantaCronacaLiveBot.stopBot();
		} else {
			Main.fantaLiveBot.startBot();
			Main.fantaCronacaLiveBot.startBot();
		}
	}
	@PostMapping("/salva")
	public Map<String, Return> salva(@RequestBody Map<String,Return> body) throws Exception  {
		Return r = body.get("r");
		List<Squadra> squadre = r.getSquadre();
		Set set = new HashSet<>();
		for (Squadra squadra : squadre) {
			set.add(squadra.getNome());
		}
		if (set.size() != squadre.size()) throw new RuntimeException("Nomi doppi!!");
		if (r.getNome().equalsIgnoreCase(Campionati.BE.name()) && r.getSquadre().size() <Constant.NUM_SQUADRE_BE) throw new RuntimeException("Squadre mangiate. Salva!");
		Main.upsertSalva(Constant.FORMAZIONE + r.getNome(), Main.toJson(r.getSquadre()));
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
		constant.GIORNATA=(Integer)body.get("giornata");
		return ret;
	}
	@GetMapping("/testIcone")
	public Map<String, Object>  testIcone() throws Exception {

		List<String[]> lista=new ArrayList<>();
//		lista.add(new String [] {Constant.PARTITA_FINITA,"PARTITA_FINITA"});
		lista.add(new String [] {Constant.PARTITA_NON_FINITA,"PARTITA_NON_FINITA"});
		lista.add(new String [] {Constant.OK_VOTO,"OK_VOTO"});
		lista.add(new String [] {Constant.NO_VOTO_IN_CORSO,"NO_VOTO_IN_CORSO"});
//		lista.add(new String [] {Constant.PALLONE,"PALLONE"});
//		lista.add(new String [] {Constant.NO_VOTO_FINITO,"NO_VOTO_FINITO"});
		lista.add(new String [] {Constant.RIGORE_PARATO,"RIGORE PARATO"});
		lista.add(new String [] {Constant.NO_VOTO_DA_INIZIARE,"NO_VOTO_DA_INIZIARE"});
		lista.add(new String [] {Constant.SCHIERATO,"SCHIERATO"});
		lista.add(new String [] {Constant.NON_SCHIERATO,"NON SCHIERATO"});
		lista.add(new String [] {Constant.SEMAFORO_1,"SEMAFORO1"});
		lista.add(new String [] {Constant.SEMAFORO_2,"SEMAFORO2"});
		lista.add(new String [] {Constant.IMBATTUTO,"IMBATTUTO"});
		lista.add(new String [] {Constant.ASSIST,"ASSIST"});	
		lista.add(new String [] {Constant.GOL,"GOL"});
//		lista.add(new String [] {Constant.USCITO,"USCITO"});
//		lista.add(new String [] {Constant.ENTRATO,"ENTRATO"});
		lista.add(new String [] {Constant.GOL_ANNULLATO,"ANNULLATO"});
//		lista.add(new String [] {Constant.INFORTUNIO,"INFORTUNIO"});
//		lista.add(new String [] {Constant.AMMONITO,"AMMONITO"});
//		lista.add(new String [] {Constant.ESPULSO,"ESPULSO"});
		lista.add(new String [] {Constant.GOL_SUBITO,"SUBITO"});
		lista.add(new String [] {Constant.RIGORE_SBAGLIATO,"RIGORE SBAGLIATO"});  
		lista.add(new String [] {Constant.RIGORE_SEGNATO,"RIGORE SEGNATO"});
		lista.add(new String [] {Constant.AUTOGOL,"AUTOGOL"});	
//		lista.add(new String [] {Constant.CIAO,"CIAO"});
		lista.add(new String [] {Constant.KEEP_ALIVE,"KEEP ALIVE"});
		lista.add(new String [] {Constant.P,"P"});
		lista.add(new String [] {Constant.D,"D"});
		lista.add(new String [] {Constant.C,"C"});
		lista.add(new String [] {Constant.A,"A"});
		lista.add(new String [] {Constant.T1,"T1"});
		lista.add(new String [] {Constant.R19,"R19"});
		lista.add(new String [] {Constant.PAUSA,"PAUSA"});
		lista.add(new String [] {Constant.FINE_PARTITA,"FINE_PARTITA"});
		lista.add(new String [] {Constant.OROLOGIO,"OROLOGIO"});
		lista.add(new String [] {Constant.SCHEDULATA,"SCHEDULATA"});
		lista.add(new String [] {Constant.DEFINITIVA,"DEFINITIVA"});
		
		Map<String, Object> ret = new LinkedHashMap();
		for (String[] msg : lista) {
			String rep="";
			for(int i=0;i<msg[0].length();i++) {
				rep = rep + "\\u" + Integer.toHexString(msg[0].charAt(i)).toUpperCase();
			}
			rep = rep + " --> ";
			byte[] bytes = msg[0].getBytes();
			for (int i = 0; i < bytes.length; i++) {
				rep = rep + bytes[i] + ",";
			}
			
			ret.put(rep, msg[0] + " --> " + msg[1]);
//			Main.inviaNotifica(msg[0]);
		}
		return ret;
	}
	@GetMapping("/fromHex")
	public static String fromHex(@RequestParam String i0) throws UnsupportedEncodingException {
		String ret;
		String valueOf;
		int parseInt = Integer.parseInt(i0,16);
		if (parseInt>65536) {
			int c1 = parseInt-65536;  
			int d1=c1/1024;
			String high=Integer.toHexString(d1 + 55296).toUpperCase();
			String low=Integer.toHexString(c1-(d1*1024)+56320).toUpperCase();
			ret = "\\u" + high + "\\u" + low;
			valueOf = String.valueOf(new char[] {(char)Integer.parseInt(high, 16),(char)Integer.parseInt(low, 16)});
		}
		else {
			ret = "\\u" + i0 ;
			valueOf = String.valueOf(new char[] {(char)Integer.parseInt(i0, 16)});
		}
		return i0 + " --> " + ret + "-->" + valueOf;
	}
	public static void main(String[] args) throws Exception {
		System.out.println(fromHex("1F153"));
		System.out.println(fromHex("1F152"));
		System.out.println(fromHex("1F150"));
//		System.out.println(fromHex("2620"));
//		diz(new Byte[] {-30});
//		diz(new Byte[] {-16,-97});
		/*
		http://localhost:8080/dizIcone?i0=-16,-97
		http://localhost:8080/dizIcone?i0=-30
		http://localhost:8080/testIcone
		
		http://localhost:8080/fromHex?i0=1F0A2
		http://localhost:8080/fromHex?i0=2620
		 */
	}
	@GetMapping("/dizIcone")
	public static Map<String, Object>  diz(@RequestParam Byte[] i0) throws Exception {
		Map<String, Object> ret = new LinkedHashMap<String, Object>();
		{
			for (int i2=-128;i2<128;i2++) {
				for (int i3=-128;i3<128;i3++) {
					byte[] b=new byte[2+i0.length];
					int conta=0;
					for (Byte byte1 : i0) {
						b[conta]=byte1;
						conta++;
					}
					b[conta]=(byte) i2;
					conta++;
					b[conta]=(byte) i3;
					
					String msg = new String(b);
					String rep="";
					boolean bSkip=false;
					for(int ix=0;ix<msg.length();ix++) {
						String hexString = Integer.toHexString(msg.charAt(ix)).toUpperCase();
						if (hexString.equals("FFFD")) {
							bSkip=true;
						}
						rep = rep + "\\u" + hexString;
					}
					rep = rep + " --> ";
					for (Byte byte1 : i0) {
						rep = rep + byte1 + ",";
					}
					rep = rep + i2 + ",";
					rep = rep + i3 + ",";
					if (!bSkip) {
						ret.put(rep, msg);
						System.out.println(msg + "                        " +  rep);
					}
				}
			}
		}
		return ret;
	}
	@GetMapping("/getDati")
	public Map<String, Object> getDati() {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("body", constant.AUTH_FS);
		ret.put("giornata", constant.GIORNATA);
		ret.put("eventi", Main.eventi);
		return ret;
	}
	@PostMapping("/setFantaSoccerAuth")
	public void setFantaSoccerAuth(@RequestBody Map<String,String> body)  {
		constant.AUTH_FS=body.get("body");
	}
	@PostMapping("/preparaSquadre")
	public void preparaSquadre(@RequestBody Map<String,Integer> body) throws Exception {
		try
		{
			Main.aggKeyFG();
			Main.cancellaSquadre();
			Main.getSquadre(Campionati.LUCCICAR.name());
			Main.getSquadre("fanta-viva");
			Main.scaricaBe();
			List<Squadra> squadre = new ArrayList<Squadra>();
			for (int i=0;i<Main.NUM_PARTITE_FS;i++) {
				String nome = Campionati.BE.name()+i + ".html";
				String testo=Main.getTesto(nome);
				Document doc = Jsoup.parse(testo);
				squadre.add(Main.getFromFS(doc, "Casa"));
				squadre.add(Main.getFromFS(doc, "Trasferta"));
				Main.cancellaSalva(nome);
			}
			Main.upsertSalva(Constant.FORMAZIONE + Campionati.BE.name(), Main.toJson(squadre));
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
	}

}
