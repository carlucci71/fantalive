package fantalive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.daniele.MainClass;
import com.daniele.fantalive.bl.FantaCronacaLiveBOT;
import com.daniele.fantalive.bl.FantaLiveBOT;
import com.daniele.fantalive.bl.Main;
import com.daniele.fantalive.bl.RisultatiConRitardoBOT;
import com.daniele.fantalive.model.Live;
import com.daniele.fantalive.util.Constant;
import com.daniele.fantalive.util.Constant.Campionati;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=MainClass.class) 
public class FantaLiveTest {

	@Autowired Constant constant;
	
    @Test
    public void testCalendario() throws Exception{
    	Map<Integer, LocalDate> calendario = Main.calendario;
    	LocalDate zonedDateTime = calendario.get(1);
		assertEquals(zonedDateTime.getMonth().getValue(), 8);
    }

    @Test
    public void testOldsnap() throws Exception{
		String testoCallback = (String) Main.getOldSnapPartite(false).get("testo");
		assertTrue(testoCallback.indexOf("Orsolini")>0);
    }

    @Test
    public void testDettaglioNotifica() throws Exception{
		Map<String, Object> lives = Main.getLives(Constant.LIVE_FROM_FILE);
		List<Live> l = (List<Live>) lives.get("lives");
		Integer evento=null;
		for (Live live : l) {
			for (Map gioc : live.getGiocatori()) {
				if (((String)gioc.get("nome")).equals("MUSSO")) {
					evento = Integer.parseInt((String) gioc.get("evento"));
				}
			}
		}
		assertEquals(evento, new Integer(4));
    }
 
    @Test
    public void testElencoGiocatori() throws Exception{
		Set<String> elencoGiocatori = Main.getElencoGiocatori("Vla");
		String dettaglioGiocatore =null;
		for (String giocatore : elencoGiocatori) {
			dettaglioGiocatore = Main.getDettaglioGiocatore(giocatore);
		}
		assertTrue(dettaglioGiocatore.indexOf("Sersale")>0);
    }

    @Test
    public void testProiezioni()  throws Exception{
    	Map<String, Object> proiezioni = Main.proiezioni(Campionati.BE.name());
    	assertTrue(((String)(proiezioni.get("testo"))).indexOf("tavolino")>0);
    }

    @Test
    public void testDettaglioCampionatoBE()  throws Exception{
		Set<String> getpartiteSimulate = Main.getpartiteSimulate(Campionati.BE.name());
		for (String partitaSimulata : getpartiteSimulate) {
			if (partitaSimulata.equals("B Gio IO")) {
				Map<String, Object> proiezioni = Main.getPartitaSimulata(constant.CHAT_ID_FANTALIVE,partitaSimulata, null);
				List<String> squadre = (List<String>) proiezioni.get("squadre");
				List<String> squadreCasa = (List<String>) proiezioni.get("squadreCasa");
				String testo = (String) proiezioni.get("testo");
				assertTrue(testo.indexOf("tavolino")>0);
				String dett = Main.getDettaglio(constant.CHAT_ID_FANTALIVE,Campionati.BE.name(), squadre.get(0).substring(squadre.get(0).indexOf("-")+1), (squadre.contains(squadreCasa.get(0))?"S":"N"), partitaSimulata, false);
				assertTrue(dett.indexOf("Raspadori")>0);
			}
		}
    }

    @Test
    public void testDettaglioCampionatoFantaviva()  throws Exception{
		Set<String> getpartiteSimulate = Main.getpartiteSimulate(Campionati.FANTAVIVA.name());
		for (String partitaSimulata : getpartiteSimulate) {
			if (partitaSimulata.equals("F Andrea IO")) {
				Map<String, Object> proiezioni = Main.getPartitaSimulata(constant.CHAT_ID_FANTALIVE,partitaSimulata, null);
				List<String> squadre = (List<String>) proiezioni.get("squadre");
				List<String> squadreCasa = (List<String>) proiezioni.get("squadreCasa");
				String testo = (String) proiezioni.get("testo");
				assertTrue(testo.indexOf("Tavolino")>0);
				String dett = Main.getDettaglio(constant.CHAT_ID_FANTALIVE,Campionati.FANTAVIVA.name(), squadre.get(0).substring(squadre.get(0).indexOf("-")+1), (squadre.contains(squadreCasa.get(0))?"S":"N"), partitaSimulata, false);
				assertTrue(dett.indexOf("Patricio")>0);
			}
		}
    }
    
    @Test
    public void testTelegram() throws Exception{
    	if (Main.fantaLiveBot==null) {
			Main.fantaLiveBot = FantaLiveBOT.inizializza("WEBAPP");
			Main.fantaCronacaLiveBot = FantaCronacaLiveBOT.inizializza("WEBAPP");
			Main.risultatiConRitardoBOT = RisultatiConRitardoBOT.inizializza("WEBAPP");
			constant.DISABILITA_NOTIFICA_TELEGRAM=false;
    	}
		boolean runningOLD = Main.fantaLiveBot.isRunning();
		if (runningOLD) {
			Main.fantaLiveBot.stopBot();
			Main.fantaCronacaLiveBot.stopBot();
			Main.risultatiConRitardoBOT.stopBot();
		} else {
			Main.fantaLiveBot.startBot();
			Main.fantaCronacaLiveBot.startBot();
			Main.inviaCronacaNotifica("TEST");
			Main.inviaCronacaNotifica("TEST Ritardato");
			Main.risultatiConRitardoBOT.startBot();
		}
		boolean running = Main.fantaLiveBot.isRunning();
		assertTrue(running != runningOLD);
		if (running) {
			Main.fantaLiveBot.stopBot();
			Main.fantaCronacaLiveBot.stopBot();
			Main.risultatiConRitardoBOT.stopBot();
		} else {
			Main.fantaLiveBot.startBot();
			Main.fantaCronacaLiveBot.startBot();
			Main.inviaCronacaNotifica("TEST");
			Main.inviaCronacaNotifica("TEST Ritardato");
			Main.risultatiConRitardoBOT.startBot();
		}
		running = Main.fantaLiveBot.isRunning();
		assertTrue(running == runningOLD);
    }
    
}