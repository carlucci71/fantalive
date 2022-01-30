package fantalive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.daniele.MainClass;
import com.daniele.fantalive.bl.Main;
import com.daniele.fantalive.model.Live;
import com.daniele.fantalive.util.Constant;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=MainClass.class) 
public class FantaLiveTest {

//	@Autowired SalvaRepository salvaRepository;
	
    @Test
    public void testCalendario() throws Exception{
    	Map<Integer, ZonedDateTime> calendario = Main.calendario;
    	ZonedDateTime zonedDateTime = calendario.get(1);
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
    
}