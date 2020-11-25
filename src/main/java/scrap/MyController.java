package scrap;

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
		int giornata=Main.GIORNATA-Main.deltaFG;
		if (lega.equalsIgnoreCase("luccicar")) giornata=Main.GIORNATA-Main.deltaFGLuccicar;
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
		Main.keyFG.put("fantaviva", "id_comp=" + Main.COMP_VIVA + "&r=" + String.valueOf(giornata - Main.deltaFG)  + "&f=" + String.valueOf(giornata - Main.deltaFG) + "_" + calcolaAggKey("fanta-viva") + ".json");
		Main.keyFG.put("luccicar", "id_comp=" + Main.COMP_LUCCICAR + "&r=" + String.valueOf(giornata - Main.deltaFGLuccicar) + "&f=" + String.valueOf(giornata - Main.deltaFGLuccicar) + "_" + calcolaAggKey("luccicar") + ".json");
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
		Main.FantaSoccerAuth=body.get("body");
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
		ret.put("body", Main.FantaSoccerAuth);
		return ret;
	}
	
}
