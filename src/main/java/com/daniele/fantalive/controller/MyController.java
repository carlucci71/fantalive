package com.daniele.fantalive.controller;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daniele.fantalive.bl.FantaCronacaLiveBOT;
import com.daniele.fantalive.bl.FantaLiveBOT;
import com.daniele.fantalive.bl.Main;
import com.daniele.fantalive.bl.RisultatiConRitardoBOT;
import com.daniele.fantalive.configurazione.SocketHandlerFantalive;
import com.daniele.fantalive.entity.Salva;
import com.daniele.fantalive.model.Giocatore;
import com.daniele.fantalive.model.PartitaSimulata;
import com.daniele.fantalive.model.Return;
import com.daniele.fantalive.model.Squadra;
import com.daniele.fantalive.repository.SalvaRepository;
import com.daniele.fantalive.util.Constant;

@Component
@RestController
@RequestMapping({ "/fantalive/" })
public class MyController {


	@Autowired Constant constant;
	@Autowired SocketHandlerFantalive socketHandlerFantalive;
	@Autowired SalvaRepository salvaRepository;

	@PostConstruct
	private void post() throws Exception {
		Main.init(salvaRepository,socketHandlerFantalive,constant, true);
		if (!constant.DISABILITA_NOTIFICA_TELEGRAM) {
			Main.fantaLiveBot = FantaLiveBOT.inizializza("WEBAPP");
			Main.fantaCronacaLiveBot = FantaCronacaLiveBOT.inizializza("WEBAPP");
			Main.risultatiConRitardoBOT = RisultatiConRitardoBOT.inizializza("WEBAPP");
			Main.inviaCronacaNotifica("Server riavviato: " + Main.MIO_IP,null);
		}
	}
 

	@Scheduled(fixedRate = 60000)
	public void scheduleKeepAlive() throws Exception {
		if (constant.ABILITA_REFRESH) {
			keepAlive();
		}
	}
	@GetMapping("/getMyFile")
	public String getFile() throws Exception {
		return keepAlive();
	}
	@GetMapping("/simulaF1")
	public Map<String, Object> simulaF1() throws Exception {
		Map<String, Object> ret=new HashMap<>();

		List<Double> f1=new ArrayList<>();
		f1.add(25d);
		f1.add(18d);
		f1.add(15d);
		f1.add(12d);
		f1.add(10d);
		f1.add(8d);
		f1.add(6d);
		f1.add(4d);

		HashMap<String, Double> classifica=new HashMap<>();
		classifica.put("Atletico Mikatanto",23.0);
		classifica.put("Universal",8.0);
		classifica.put("C. H. MOLLE",24.0);
		classifica.put("VincereAManiBasse",37.0);
		classifica.put("Canosa di Puglia...",28.0);
		classifica.put("Atletico Conc",25.0);
		classifica.put("tavolino",31.0);
		classifica.put("Jonny Fighters",20.0);		
		
		Map<String, Double> totale=new HashMap<>();
		datiSingolaProiezione(Main.proiezioneFS("BE", "B IO Conc"),totale);
		datiSingolaProiezione(Main.proiezioneFS("BE", "B Claudio New"),totale);
		datiSingolaProiezione(Main.proiezioneFS("BE", "B Roby Fra"),totale);
		datiSingolaProiezione(Main.proiezioneFS("BE", "B Dante Gio"),totale);
		Map<String, Double> att = new TreeMap(new ReverseOrderTreemap(totale));
		att.putAll(totale);
		Set<String> keySet = att.keySet();
		int pos=0;
		for (String key : keySet) {
			classifica.put(key, classifica.get(key)+f1.get(pos));
			pos++;
		}
		//att.forEach((k,v)->System.err.println(k+"="+v));
		ret.put("att", att);
		Map<String, Double> cl = new TreeMap(new ReverseOrderTreemap(classifica));
		cl.putAll(classifica);
		//cl.forEach((k,v)->System.err.println(k+"="+v));
		ret.put("classifica", cl);
		return ret;
	}

	class ReverseOrderTreemap implements Comparator<String>  {
		Map<String, Double> map;
		public ReverseOrderTreemap(Map<String, Double> map) {
			this.map = map;
		}
		public int compare(String o1, String o2) {
			if (map.get(o2) == map.get(o1))
				return 1;
			else {
				int compareTo = ((Double) map.get(o2)).compareTo((Double) map.get(o1));
				if (compareTo==0) {
					compareTo=1;
				}
				return compareTo;
				
			}

		}
	}


	private void datiSingolaProiezione(Map<String, Object> proiezioneFS,Map<String, Double> totale) {
		Set<String> keySet = proiezioneFS.keySet();
		for (String key : keySet) {
			Map<String, Object> map = (Map<String, Object>) proiezioneFS.get(key);
			List<Map<String, Object>> teams = (List<Map<String, Object>>) map.get("teams");
			for (Map<String,Object> team : teams) {
				String nome = (String) team.get("nome");
				BigDecimal bd=new BigDecimal(0);
				System.out.println(team.get("nome"));
				List<Map<String, Object>> players = (List<Map<String, Object>>) team.get("players");
				for (Map<String,Object> player : players) {
					if (player.get("played").equals(true)) {
						System.out.println(
										player.get("fantavoto") + " - " +
										player.get("squadraGioca") + " - " +
										player.get("nome") + " - " +
										player.get("played") + " - " 
								);
						bd=bd.add(new BigDecimal((Double) player.get("fantavoto")));
					}
				}
				totale.put(nome, bd.doubleValue());
				System.out.println();
			}
		}
	}

	
	private String keepAlive() throws Exception {
		String ret="";
		ZonedDateTime now = ZonedDateTime.now();
		if (constant.KEEP_ALIVE_END.isAfter(now)) {
			long between = 0;
			if (Constant.LAST_REFRESH != null) between = ChronoUnit.MINUTES.between(Constant.LAST_REFRESH,now);
			if (between >25) {
				String http = (String) Main.callHTTP("GET", "application/json; charset=UTF-8",String.format(Constant.URL_KEEP_ALIVE_HEROKU), null).get("response");
				ret=Constant.KEEP_ALIVE + " Keep Alive!";
				Main.inviaCronacaNotifica(ret, null);
				System.out.println("REFRESH!!");
				Constant.LAST_REFRESH=ZonedDateTime.now();
			}
			else {
//				System.out.println("non ancora refresh:" + between);
				ret = "NON ANCORA REFRESH:" + between;
			}
		} else {
			ret = Constant.dateTimeFormatterOut.format(now) + " --> " + Constant.dateTimeFormatterOut.format(constant.KEEP_ALIVE_END);
			System.out.println("NON NECESSARIO REFRESH!!");
		}
		return ret;
	}
	@Scheduled(fixedRate = 10000)
	public void chckNotifica() throws Exception {
		if (constant.ABILITA_REFRESH) {
	
			Main.timeRefresh = (int) Main.toSocket.get("timeRefresh");
			if (Main.timeRefresh==Constant.SCHEDULED_SNAP) {
				Main.snapshot(true);
			}
			Main.timeRefresh=Main.timeRefresh+5000;
			Main.toSocket.put("timeRefresh", Main.timeRefresh);
			Main.toSocket.put("liveFromFile", constant.LIVE_FROM_FILE);
			Main.toSocket.put("disabilitaNotificaTelegram", constant.DISABILITA_NOTIFICA_TELEGRAM);
	//		if (Constant.LAST_KEEP_ALIVE != null) Main.toSocket.put("lastKeepAlive", Constant.dateTimeFormatterOut.format(Constant.LAST_KEEP_ALIVE));
			if (Constant.LAST_REFRESH != null) Main.toSocket.put("lastRefresh", Constant.dateTimeFormatterOut.format(Constant.LAST_REFRESH));
			Main.toSocket.put("keepAliveEnd", Constant.dateTimeFormatterOut.format(Constant.KEEP_ALIVE_END));
			String visKeepAlive = "N";
			ZonedDateTime now = ZonedDateTime.now();
			if (Constant.KEEP_ALIVE_END.isAfter(now)) {
				visKeepAlive="S";
			}
			Main.toSocket.put("visKeepAlive", visKeepAlive);
			
			String runningBot="STOPPED";
			if (Main.fantaLiveBot != null && Main.fantaLiveBot.isRunning()) {
				runningBot="RUNNING";
			}
			Main.toSocket.put("runningBot", runningBot);
			socketHandlerFantalive.invia(Main.toSocket );
		}
	}
	@RequestMapping("/getOrariFromDb")
	public Map<String, String> getOrariFromDb() throws Exception {
		Map<String, String> ret = new HashMap<>();
		ret.put("file", Main.getTesto("snapPartite"));
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
		ret.put("file", Main.toJson(lives.get("snapPartite")));
		return ret;
	}
	@RequestMapping("/getLivesFromLive")
	public Map<String, String> getLivesFromLive() throws Exception {
		Map<String, String> ret = new HashMap<>();
		Map<String, Object> lives = Main.getLives(false);
		ret.put("file", Main.toJson(lives.get("lives")));
		return ret;
	}
	@PostMapping("/simulaForzata")
	public Map<String, String> simulaForzata(@RequestBody Map<String,String> body) throws Exception {
		Map<String, String>  ret=new HashMap<>();
		System.out.println(Main.toJson(body));
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
		Main.inviaNotifica(Main.getDettaglio(Constant.CHAT_ID_FANTALIVE,body.get("campionato"),body.get("squadra"),"N", null, false));
		return ret;
	}
	@PostMapping("/caricaFileFromDataByName")
	public Map<String, String>  caricaFileFromDataByName(@RequestBody Map<String,String> body) throws Exception {
		Map<String, String>  ret=new HashMap<>();
		String orari = Main.getTesto(body.get("name")+"-orari");
		String lives = Main.getTesto(body.get("name")+"-lives");
		Main.upsertSalva("snapPartite", orari);
		Main.upsertSalva("lives", lives);
		Main.snapshot(false);
		ret.put("snapPartite", orari );
		ret.put("lives", lives );
		return ret;
	}
	@PostMapping("/caricaFileFromData")
	public void caricaFileFromData(@RequestBody Map<String,Object> body) throws Exception {
		Main.upsertSalva("snapPartite", (String) body.get("snapPartite"));
		Main.upsertSalva("lives", (String) body.get("lives"));
	}
	@PostMapping("/caricaFile")
	public void caricaFile(@RequestBody Map<String,Object> body) throws Exception {
		String content = (String) body.get("file");
		String tipoFile = (String) body.get("tipoFile");
		if (tipoFile.equalsIgnoreCase("O")) {
			Main.upsertSalva("snapPartite", content);
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
		} else {
			Main.fantaLiveBot.startBot();
		}
		if (Main.fantaCronacaLiveBot.isRunning()) {
			Main.fantaCronacaLiveBot.stopBot();
		} else {
			Main.fantaCronacaLiveBot.startBot();
		}
		if (Main.risultatiConRitardoBOT.isRunning()) {
			Main.risultatiConRitardoBOT.stopBot();
		} else {
			Main.risultatiConRitardoBOT.startBot();
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
		if (r.getNome().equalsIgnoreCase(Constant.Campionati.BE.name()) && r.getSquadre().size() <Constant.NUM_SQUADRE_BE) throw new RuntimeException("Squadre mangiate. Salva!");
		Main.upsertSalva(Constant.FORMAZIONE + r.getNome(), Main.toJson(r.getSquadre()));
		return test(true);
	}
	
	
	@PostMapping("/cancellaProiezione/{ind}")
	public Map<String, Object>  cancellaProiezione(@PathVariable String ind) throws Exception {
		String nomeFile=new String(Base64.getDecoder().decode(ind));
		salvaRepository.delete(nomeFile);
		List<String> ret=new ArrayList<>();
		List<Salva>  ls =salvaRepository.findSimulazioniName("%" + nomeFile.substring(nomeFile.indexOf("-")));
		ls.forEach((salva) -> {
			ret.add(salva.getNome());
		});
		Map m = new HashMap<>();
		m.put("lista", ret);
		return m;
	}
	
	
	
	
	@PostMapping("/proiezioneStorica/{ind}")
	public Map<String, Object> proiezioneFGStorica(@PathVariable String ind) throws Exception {
		String nomeFile=new String(Base64.getDecoder().decode(ind));
		Salva findOne = salvaRepository.findOne(nomeFile);
		Map fromJson = Main.fromJson(findOne.getTesto(), Map.class);
		/*
		List<Map> l = (List<Map>) ((Map)fromJson.get("data")).get("teams");
		for (Map m : l) {
			m.put("nome", m.get("nome") +"-"+ nomeFile.substring(0,23));
			m.put("total", 23);
		}
		*/
		return fromJson;
	}

	@PostMapping("/proiezioneFG/{lega}/{sfide}")
	public Map<String, Object> proiezioneFG(@PathVariable String lega,@PathVariable String sfide,@RequestBody List<Squadra> sq) throws Exception {
		return Main.proiezioneFG(Main.aliasCampionati.get(lega), sq, sfide, null);
	}

	@PostMapping("/proiezioneFG_name/{lega}/{sfide}")
	public Map<String, Object>  proiezioneFG_name(@PathVariable String lega,@PathVariable String sfide,@RequestBody List<Squadra> sq) throws Exception {
		List<Salva> ls =  Main.proiezioneFG_name(Main.aliasCampionati.get(lega), sq, sfide, null);
		List<String> ret=new ArrayList<>();
		ls.forEach((salva) -> {
			ret.add(salva.getNome());
		});
		Map m = new HashMap<>();
		m.put("lista", ret);
		return m;
	}
	
	@PostMapping("/proiezioneFS/{lega}/{nomePartitaSimulata}")
	public Map<String, Object> proiezioneFS(@PathVariable String lega,@PathVariable String nomePartitaSimulata) throws Exception {
		return Main.proiezioneFS(lega, nomePartitaSimulata);
	}
	
	@PostMapping("/proiezioneFS_name/{lega}/{nomePartitaSimulata}")
	public Map<String, Object> proiezioneFS_name(@PathVariable String lega,@PathVariable String nomePartitaSimulata) throws Exception {
		List<Salva> ls =  Main.proiezioneFS_name(Main.aliasCampionati.get(lega), nomePartitaSimulata);
		List<String> ret=new ArrayList<>();
		ls.forEach((salva) -> {
			ret.add(salva.getNome());
		});
		Map m = new HashMap<>();
		m.put("lista", ret);
		return m;
	}
	
	@PostMapping("/simulaCambiMantra/{lega}")
	public Squadra simulaCambiMantra(@PathVariable String lega,@RequestBody Map<String,Squadra> body) throws Exception  {
		List<String> assenti=new ArrayList<>();
		Squadra sq = body.get("sq");
		for (Giocatore giocatore : sq.getTitolari()) {
			if (giocatore.isMantraCambio()) {
				assenti.add(giocatore.getId());
			}
		}
		for (Giocatore giocatore : sq.getRiserve()) {
			if (giocatore.isMantraCambio()) {
				assenti.add(giocatore.getId());
			}
		}
		Map<String, Object> simulaCambiMantra = Main.simulaCambiMantra(Main.aliasCampionati.get(lega), assenti, sq);
		Squadra squadra = sq.clonaSquadra();
		List<Giocatore> nuovaListaGiocatori=new ArrayList<Giocatore>();
		int iContaPosizione=0;
		List<Map<String, Object>> calciatoriEntra = (List<Map<String, Object>>) simulaCambiMantra.get("calciatoriEntra");
		List<Map<String, Object>> calciatoriNonEntra = (List<Map<String, Object>>) simulaCambiMantra.get("calciatoriNonEntra");
		
		for (Giocatore giocatore : sq.getTitolari()) {
			Giocatore entra = entra(giocatore, calciatoriEntra, "E", squadra.getTitolariOriginali());
			if (entra != null) {
				nuovaListaGiocatori.add(entra);
			}
		}
		for (Giocatore giocatore : sq.getRiserve()) {
			Giocatore entra = entra(giocatore, calciatoriEntra, "E", squadra.getTitolariOriginali());
			if (entra != null) {
				nuovaListaGiocatori.add(entra);
			}
		}
		Collections.sort(nuovaListaGiocatori, new Comp());
		squadra.setTitolari(nuovaListaGiocatori);

		nuovaListaGiocatori=new ArrayList<Giocatore>();
		for (Giocatore giocatore : sq.getTitolari()) {
			Giocatore entra = entra(giocatore, calciatoriNonEntra, "U", squadra.getTitolariOriginali());
			if (entra != null) {
				nuovaListaGiocatori.add(entra);
			}
		}
		for (Giocatore giocatore : sq.getRiserve()) {
			Giocatore entra = entra(giocatore, calciatoriNonEntra, "U", squadra.getTitolariOriginali());
			if (entra != null) {
				nuovaListaGiocatori.add(entra);
			}
		}
		Collections.sort(nuovaListaGiocatori, new Comp());
		squadra.setRiserve(nuovaListaGiocatori);

		int iContaTitolari = squadra.getTitolari().size();
		if (iContaTitolari < 11) {
			Map <String, Integer> contaMacroRuoli=new HashMap<>();
			contaMacroRuoli.put("P", 0);
			contaMacroRuoli.put("D", 0);
			contaMacroRuoli.put("C", 0);
			contaMacroRuoli.put("A", 0);
			for (Giocatore giocatore : squadra.getTitolari()) {
				String macroRuolo = macroRuoliMantra.get(giocatore.getRuolo().split(";")[0]);
				Integer conta = contaMacroRuoli.get(macroRuolo);
				conta++;
				contaMacroRuoli.put(macroRuolo, conta);
			}
//			System.out.println(contaMacroRuoli);
//			System.out.println(simulaCambiMantra.get("moduloS"));
			Integer contaP = contaMacroRuoli.get("P");
			if (contaP==0) {
				for (Giocatore titolare : squadra.getTitolariOriginali()) {
					String macroRuolo = macroRuoliMantra.get(titolare.getRuolo().split(";")[0]);
					if (macroRuolo.equals("P")) {
						Giocatore giocatore = getGiocatore(titolare.getId(), squadra.getRiserve());
						giocatore.setNonCambiabile(true);
						giocatore.setCambio(false);
						giocatore.setCambiato(false);
						squadra.getTitolari().add(0,giocatore);
						List<Giocatore> nuoveRiserve = new ArrayList<>();
						for (Giocatore riserva : squadra.getRiserve()) {
							if (!riserva.getId().equals(giocatore.getId())) {
								nuoveRiserve.add(riserva);
							}
						}
						squadra.setRiserve(nuoveRiserve);
					}
				}
				iContaTitolari++;
			}

			for (Giocatore titolare : squadra.getTitolariOriginali()) {
				if (iContaTitolari>=11) continue;
				Giocatore giocatore = getGiocatore(titolare.getId(), squadra.getRiserve());
				if (giocatore != null) {
					giocatore.setNonCambiabile(true);
					giocatore.setCambio(false);
					giocatore.setCambiato(false);
					squadra.getTitolari().add(giocatore);
					List<Giocatore> nuoveRiserve = new ArrayList<>();
					for (Giocatore riserva : squadra.getRiserve()) {
						if (!riserva.getId().equals(giocatore.getId())) {
							nuoveRiserve.add(riserva);
						}
					}
					squadra.setRiserve(nuoveRiserve);
					iContaTitolari++;
				}
			}

		}
		
		return squadra;
	}
	
	private Giocatore getGiocatore(String id, List<Giocatore> list) {
		for (Giocatore giocatore : list) {
			if (giocatore.getId().equals(id)) {
				return giocatore;
			}
		}
		return null;
	}
	
	private class Comp implements Comparator<Giocatore> {

		public int compare(final Giocatore g1, final Giocatore g2) {
				String r1 = macroRuoliMantra.get(g1.getRuolo().split(";")[0]);
				String r2 = macroRuoliMantra.get(g2.getRuolo().split(";")[0]);
				return r2.compareTo(r1);
		}

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
	
	
	private Giocatore entra(Giocatore giocatore, List<Map<String, Object>> listaCalciatori,  String enUs, List<Giocatore> titolariOriginali) {
		String id = giocatore.getId();
		for (Map<String, Object> calciatore : listaCalciatori) {
			Integer idSimulaCalciatore = (Integer) calciatore.get("id");
			if (Integer.parseInt(id)==idSimulaCalciatore) {
				giocatore.setMantraMalus((Double) calciatore.get("malus"));
				if (isTitolareOriginale(giocatore, titolariOriginali) && enUs.equals("E") || !isTitolareOriginale(giocatore, titolariOriginali) && enUs.equals("U")) {
					giocatore.setCambiato(false);
					giocatore.setCambio(false);
				} else {
					giocatore.setCambiato(true);
					giocatore.setCambio(true);
				}
				giocatore.setNonCambiabile(false);
				return giocatore;
			}
		}
		return null;
	}
	
	private boolean isTitolareOriginale(Giocatore giocatore, List<Giocatore> titolariOriginali) {
		for (Giocatore titolareOriginale : titolariOriginali) {
			if (giocatore.getId().equals(titolareOriginale.getId())) {
				return true;
			}
		}
		return false;
	}
	
	
	@PostMapping("/simulaCambi")
	public Squadra simulaCambi(@RequestBody Map<String,Squadra> body) throws Exception  {
		Squadra sq = body.get("sq");
		Squadra squadra = sq.clonaSquadra();
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
	
	@PostMapping("/setKeepAliveEnd")
	public Map<String, Object> setKeepAliveEnd(@RequestBody Map<String,Object> body) throws Exception  {
		return Main.setKeepAliveEnd(body);
	}
	@PostMapping("/setGiornata")
	public Map<String, Object> setGiornata(@RequestBody Map<String,Object> body)  {
		Map<String, Object> ret = new HashMap<String, Object>();
		constant.GIORNATA=(Integer)body.get("giornata");
		Main.oldSnapLives=null;
		Main.oldSnapOrari=null;
		Main.sqStatusMatch=new HashMap<>();

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
	public void preparaSquadre(@RequestBody Map<String,Object> body) throws Exception {
		try
		{
			Main.aggKeyFG();
			String clearDB=(String) body.get("clearDB");
			if (clearDB.equalsIgnoreCase("S")) {
				Main.clearDB();
			}else {
				Main.cancellaSquadre();
			}
			Main.getSquadreFromFG(Constant.Campionati.LUCCICAR.name());
			Main.getSquadreFromFG(Constant.Campionati.JB.name());
			Main.getSquadreFromFG(Constant.Campionati.FANTAVIVA.name());
			if (Constant.GIORNATA-Constant.DELTA_FS>0){
				Main.scaricaBe(Constant.GIORNATA,"");
				List<Squadra> squadre = Main.getSquadreFromFS("",true, false);
				Main.adattaNick(Constant.Campionati.BE.name(), squadre);
				Main.adattaNomePartitaSimulata(squadre, Constant.Campionati.BE.name());
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
						}
					}
				}
				Main.upsertSalva(Constant.FORMAZIONE + Constant.Campionati.BE.name(), Main.toJson(squadre));
			}
			Main.verificaOggiGioca();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
	}





}
