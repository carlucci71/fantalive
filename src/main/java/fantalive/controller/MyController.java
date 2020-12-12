package fantalive.controller;

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

	/*
	public MyController() throws Exception {
		super();
	}
	*/

	@PostConstruct
	private void post() throws Exception {
		Constant.CHAT_ID_FANTALIVE = Long.valueOf(System.getenv("CHAT_ID_FANTALIVE"));
		Constant.SPONTIT_KEY = System.getenv("SPONTIT_KEY");
		Constant.TOKEN_BOT_FANTALIVE = System.getenv("TOKEN_BOT_FANTALIVE");
		Constant.SPONTIT_USERID = System.getenv("SPONTIT_USERID");
		Constant.APPKEY_FG = System.getenv("APPKEY_FG");
		Constant.AUTH_FS = System.getenv("AUTH_FS");
		Main.init(salvaRepository);
		Main.fantaLiveBot = FantaLiveBOT.inizializza("WEBAPP");
	}
	
	@Scheduled(fixedRate = 5000)
	public void chckNotifica() throws Exception {
		int conta = (int) Main.toSocket.get("timeRefresh");
		if (conta==60000) {//FIXME 60000
			conta=0;
			Main.snapshot(socketHandler);
		}
		conta=conta+5000;
		Main.toSocket.put("timeRefresh", conta);
		String runningBot="STOPPED";
		if (Main.fantaLiveBot.isRunning()) {
			runningBot="RUNNING";
		}
		Main.toSocket.put("runningBot", runningBot);
		socketHandler.invia(Main.toSocket );
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
		Main.GIORNATA=(Integer)body.get("giornata");
		return ret;
	}
	@GetMapping("/getDati")
	public Map<String, Object> getDati() {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("body", Constant.AUTH_FS);
		ret.put("giornata", Main.GIORNATA);
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
