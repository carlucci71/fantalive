package scrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
@RequestMapping({ "/" })
public class MyController {

	public MyController() throws Exception {
		super();
		Main.init();
		aggKeyFG();
	}

	private String calcolaAggKey(String lega) throws Exception {
		int giornata=Main.GIORNATA-Main.DELTA_VIVA_FG;
		if (lega.equalsIgnoreCase("luccicar")) giornata=Main.GIORNATA-Main.DELTA_LUCCICAR_FG;
		String url = "https://leghe.fantacalcio.it/" + lega + "/formazioni/" + giornata;
		String string = Main.callHTTP(url);
		string = string.substring(string.indexOf(".s('tmp', ")+11);
		string=string.substring(0,string.indexOf(")"));
		string = string.replace("|", "@");
		String[] split = string.split("@");
		return split[1];
	}
	
	public void aggKeyFG() throws Exception {
		int giornata=Main.GIORNATA;
		Main.keyFG=new HashMap<String, String>();
		Main.keyFG.put("fantaviva", "id_comp=" + Main.COMP_VIVA_FG + "&r=" + String.valueOf(giornata - Main.DELTA_VIVA_FG)  + "&f=" + String.valueOf(giornata - Main.DELTA_VIVA_FG) + "_" + calcolaAggKey("fanta-viva") + ".json");
		Main.keyFG.put("luccicar", "id_comp=" + Main.COMP_LUCCICAR_FG + "&r=" + String.valueOf(giornata - Main.DELTA_LUCCICAR_FG) + "&f=" + String.valueOf(giornata - Main.DELTA_LUCCICAR_FG) + "_" + calcolaAggKey("luccicar") + ".json");
	}
	
	@RequestMapping("/test")
	public Map<String, Return>  test(boolean conLive) throws Exception {
		Map<String, Return> go = Main.go(conLive);
		return go;
	}
	
	@GetMapping("/nomiSquadre")
	public List<String> getNomiSquadre() throws Exception {
		List<String> ret = new ArrayList<String>();
		Map<String, Return> go = Main.go(false);
		Iterator<String> iterator = go.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			List<Squadra> sq = go.get(key).getSquadre();
			for (Squadra squadra : sq) {
				ret.add(squadra.getNome());
			}
		}
		return ret;
	}
	@PostMapping("/salva")
	public void salva(@RequestBody Map<String,Return> body) throws Exception  {
		Return r = body.get("r");
		Files.write(Paths.get("./fomrazioneFG" + r.getNome().toLowerCase() + ".json"), Main.toJson(r.getSquadre()).getBytes());
	}
	@PostMapping("/simulaCambi")
	public Squadra simulaCambi(@RequestBody Map<String,Squadra> body)  {
		Squadra sq = body.get("sq");
		Squadra squadra = new Squadra();
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
	public void addSqEv(@RequestBody Map<String,String> body)  {
		Main.getSqDaEv().add(body.get("sqEv"));
	}
	@PostMapping("/addSqBeDaEscluedere")
	public void addSqBeDaEscluedere(@RequestBody Map<String,String> body)  {
		Main.getSqBeDaEscluedere().add(body.get("sqBeDaEscluedere"));
	}
	@PostMapping("/delSqEv")
	public void delSqEv(@RequestBody Map<String,String> body)  {
		Main.getSqDaEv().remove(body.get("sqEv"));
	}
	@GetMapping("/ricaricaSqBeDaEscluedere")
	public Map<String,Object> ricaricaSqBeDaEscluedere()  {
		Map<String,Object> ret = new HashMap<String, Object>();
		ret.put("sq",Main.getSqBeDaEscluedere());
		return ret;
	}
	@GetMapping("/ricaricaSqEv")
	public Map<String,Object> ricaricaSqEv()  {
		Map<String,Object> ret = new HashMap<String, Object>();
		ret.put("sq",Main.getSqDaEv());
		return ret;
	}
	@PostMapping("/delSqBeDaEscluedere")
	public void delSqBeDaEscluedere(@RequestBody Map<String,String> body)  {
		Main.getSqBeDaEscluedere().remove(body.get("sqBeDaEscluedere"));
	}
	@PostMapping("/setGiornata")
	public void setGiornata(@RequestBody Map<String,Integer> body)  {
		Main.GIORNATA=body.get("giornata");
	}
	@GetMapping("/getGiornata")
	public Map<String, Integer> getGiornata() {
		Map<String, Integer> ret = new HashMap<String, Integer>();
		ret.put("giornata", Main.GIORNATA);
		return ret;
	}
	@PostMapping("/setFantaSoccerAuth")
	public void setFantaSoccerAuth(@RequestBody Map<String,String> body)  {
		Main.AUTH_FS=body.get("body");
	}
	@PostMapping("/preparaSquadre")
	public void preparaSquadre() throws Exception {
		aggKeyFG();
		Main.cancellaSquadre();
		Main.getSquadre("luccicar");
		Main.getSquadre("fanta-viva");
		Main.scaricaBe();
	}
	@GetMapping("/getFantaSoccerAuth")
	public Map<String, String> getFantaSoccerAuth() {
		Map<String, String> ret=new HashMap<String, String>();
		ret.put("body", Main.AUTH_FS);
		return ret;
	}
	
}
