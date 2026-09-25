package com.daniele.asta;

import com.daniele.MainClass;
import com.daniele.asta.support.AstaHttpClient;
import com.daniele.asta.support.AstaTestFixture;
import com.daniele.asta.support.AstaWsClient;
import com.daniele.asta.session.AstaSessionRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainClass.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AstaWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private SocketHandler socketHandler;

    @Autowired
    private AstaSessionRegistry sessionRegistry;

    @Autowired
    private AstaTestFixture fixture;

    private String baseUrl;

    @Before
    public void setUp() {
        baseUrl = "http://localhost:" + port;
        fixture.resetAndSeed();
        sessionRegistry.clearAll();
    }

    @After
    public void tearDown() {
        sessionRegistry.clearAll();
    }

    @Test
    public void connectSingleUser_sessionsAndInitCoherent() throws Exception {
        try (AstaHttpClient http = new AstaHttpClient(baseUrl);
             AstaWsClient ws = new AstaWsClient()) {
            http.init();
            ws.connect(baseUrl, http.getJsessionId());
            ws.connetti("GIOC0", 0);
            ws.waitForConnettiOk(5000);

            assertEquals(1, socketHandler.getSessions().size());
            assertEquals(1, socketHandler.getUtentiLoggati().size());
            assertTrue(sessionRegistry.isLoggedIn("GIOC0"));

            Map<String, Object> init = http.init();
            assertEquals("GIOC0", init.get("giocatoreLoggato"));
            assertEquals(true, init.get("onlineWs"));
        }
    }

    @Test
    public void twoUsersParallel_sessionsMatchUtenti() throws Exception {
        try (AstaHttpClient http0 = new AstaHttpClient(baseUrl);
             AstaHttpClient http1 = new AstaHttpClient(baseUrl);
             AstaWsClient ws0 = new AstaWsClient();
             AstaWsClient ws1 = new AstaWsClient()) {
            http0.init();
            http1.init();
            ws0.connect(baseUrl, http0.getJsessionId());
            ws1.connect(baseUrl, http1.getJsessionId());
            ws0.connetti("GIOC0", 0);
            ws1.connetti("GIOC1", 1);
            ws0.waitForConnettiOk(5000);
            ws1.waitForConnettiOk(5000);

            assertEquals(2, socketHandler.getSessions().size());
            assertEquals(2, socketHandler.getUtentiLoggati().size());
            assertTrue(sessionRegistry.isLoggedIn("GIOC0"));
            assertTrue(sessionRegistry.isLoggedIn("GIOC1"));
        }
    }

    @Test
    public void reconnectSameUser_keepsSingleBinding() throws Exception {
        try (AstaHttpClient http = new AstaHttpClient(baseUrl)) {
            http.init();
            String jsessionId = http.getJsessionId();

            try (AstaWsClient ws1 = new AstaWsClient()) {
                ws1.connect(baseUrl, jsessionId);
                ws1.connetti("GIOC0", 0);
                ws1.waitForConnettiOk(5000);
            }

            try (AstaWsClient ws2 = new AstaWsClient()) {
                ws2.connect(baseUrl, jsessionId);
                ws2.connetti("GIOC0", 0);
                ws2.waitForConnettiOk(5000);
                assertEquals(1, socketHandler.getSessions().size());
                assertEquals(1, socketHandler.getUtentiLoggati().size());
            }
        }
    }

    @Test
    public void secondaryPagesHttpOnly_doNotIncreaseWsSessions() throws Exception {
        try (AstaHttpClient http = new AstaHttpClient(baseUrl);
             AstaWsClient ws = new AstaWsClient()) {
            http.init();
            ws.connect(baseUrl, http.getJsessionId());
            ws.connetti("GIOC0", 0);
            ws.waitForConnettiOk(5000);
            int sessionsAfterLogin = socketHandler.getSessions().size();

            http.fetchPage("/fantaasta/liberi.html");
            http.init();
            http.fetchPage("/fantaasta/riepilogo.html");
            http.init();
            http.fetchPage("/fantaasta/cronologiaOfferte.html");
            http.init();
            http.fetchPage("/fantaasta/logger.html");
            Map<String, Object> init = http.init();

            assertEquals(sessionsAfterLogin, socketHandler.getSessions().size());
            assertEquals(1, socketHandler.getUtentiLoggati().size());
            assertEquals(true, init.get("onlineWs"));
        }
    }

    @Test
    public void auctionWinner_visibleOnBothClients() throws Exception {
        try (AstaHttpClient http0 = new AstaHttpClient(baseUrl);
             AstaHttpClient http1 = new AstaHttpClient(baseUrl);
             AstaWsClient ws0 = new AstaWsClient();
             AstaWsClient ws1 = new AstaWsClient()) {
            http0.init();
            http1.init();
            ws0.connect(baseUrl, http0.getJsessionId());
            ws1.connect(baseUrl, http1.getJsessionId());
            ws0.connetti("GIOC0", 0);
            ws1.connetti("GIOC1", 1);
            ws0.waitForConnettiOk(5000);
            ws1.waitForConnettiOk(5000);

            ws0.sendOperazione("start", Map.of(
                    "nomegiocatore", "GIOC0",
                    "idgiocatore", "0",
                    "nomegiocatoreOperaCome", "GIOC0",
                    "idgiocatoreOperaCome", "0",
                    "selCalciatore", "1@" + AstaTestFixture.PLAYER_1,
                    "selCalciatoreMacroRuolo", "A"
            ));
            ws0.waitFor(msg -> msg.get("offertaVincente") != null, 5000);

            ws1.sendOperazione("inviaOfferta", Map.of(
                    "nomegiocatore", "GIOC1",
                    "idgiocatore", "1",
                    "nomegiocatoreOperaCome", "GIOC1",
                    "idgiocatoreOperaCome", "1",
                    "offerta", 12,
                    "maxRilancio", 500
            ));

            ws0.waitForOffertaVincente("GIOC1", 12, 5000);
            ws1.waitForOffertaVincente("GIOC1", 12, 5000);
        }
    }

    @Test
    public void confirmAuction_updatesSpentAndClearsOffer() throws Exception {
        try (AstaHttpClient http = new AstaHttpClient(baseUrl);
             AstaWsClient ws = new AstaWsClient()) {
            http.init();
            ws.connect(baseUrl, http.getJsessionId());
            ws.connetti("GIOC0", 0);
            ws.waitForConnettiOk(5000);

            ws.sendOperazione("start", Map.of(
                    "nomegiocatore", "GIOC0",
                    "idgiocatore", "0",
                    "nomegiocatoreOperaCome", "GIOC0",
                    "idgiocatoreOperaCome", "0",
                    "selCalciatore", "1@" + AstaTestFixture.PLAYER_1,
                    "selCalciatoreMacroRuolo", "A"
            ));
            ws.waitFor(msg -> msg.get("offertaVincente") != null, 5000);

            ws.sendOperazione("inviaOfferta", Map.of(
                    "nomegiocatore", "GIOC0",
                    "idgiocatore", "0",
                    "nomegiocatoreOperaCome", "GIOC0",
                    "idgiocatoreOperaCome", "0",
                    "offerta", 20,
                    "maxRilancio", 500
            ));
            ws.waitForOffertaVincente("GIOC0", 20, 5000);

            ws.sendOperazione("terminaAsta", Map.of(
                    "nomegiocatore", "GIOC0",
                    "idgiocatore", "0"
            ));
            ws.waitFor(msg -> "DA_CONFERMARE".equals(msg.get("faseAsta")), 5000);

            ws.sendOperazione("confermaAsta", Map.of(
                    "nomegiocatore", "GIOC0",
                    "idgiocatore", "0"
            ));
            Map<String, Object> confirmed = ws.waitFor(msg -> {
                Object speso = msg.get("mapSpesoTotale");
                return speso instanceof Map && !((Map<?, ?>) speso).isEmpty();
            }, 8000);

            Map<?, ?> speso = (Map<?, ?>) confirmed.get("mapSpesoTotale");
            assertTrue(speso.containsKey("GIOC0"));
            assertFalse(confirmed.containsKey("offertaVincente"));
        }
    }

    @Test
    public void secondAuction_afterConfirmWorks() throws Exception {
        try (AstaHttpClient http = new AstaHttpClient(baseUrl);
             AstaWsClient ws = new AstaWsClient()) {
            http.init();
            ws.connect(baseUrl, http.getJsessionId());
            ws.connetti("GIOC0", 0);
            ws.waitForConnettiOk(5000);

            runAuction(ws, 2, AstaTestFixture.PLAYER_1, 15);
            runAuction(ws, 3, AstaTestFixture.PLAYER_2, 18);

            Map<String, Object> init = http.init();
            List<?> calciatori = (List<?>) init.get("calciatori");
            assertTrue(calciatori != null);
            assertEquals(1, calciatori.size());
        }
    }

    @Test
    public void nonAdminCannotBidAsOtherUser() throws Exception {
        try (AstaHttpClient http0 = new AstaHttpClient(baseUrl);
             AstaHttpClient http1 = new AstaHttpClient(baseUrl);
             AstaWsClient ws0 = new AstaWsClient();
             AstaWsClient ws1 = new AstaWsClient()) {
            http0.init();
            http1.init();
            ws0.connect(baseUrl, http0.getJsessionId());
            ws1.connect(baseUrl, http1.getJsessionId());
            ws0.connetti("GIOC0", 0);
            ws1.connetti("GIOC1", 1);
            ws0.waitForConnettiOk(5000);
            ws1.waitForConnettiOk(5000);

            ws0.sendOperazione("start", Map.of(
                    "nomegiocatore", "GIOC0",
                    "idgiocatore", "0",
                    "nomegiocatoreOperaCome", "GIOC0",
                    "idgiocatoreOperaCome", "0",
                    "selCalciatore", "1@" + AstaTestFixture.PLAYER_1,
                    "selCalciatoreMacroRuolo", "A"
            ));
            ws0.waitFor(msg -> msg.get("offertaVincente") != null, 5000);
            ws1.clearMessages();

            ws1.sendOperazione("inviaOfferta", Map.of(
                    "nomegiocatore", "GIOC0",
                    "idgiocatore", "0",
                    "nomegiocatoreOperaCome", "GIOC1",
                    "idgiocatoreOperaCome", "1",
                    "offerta", 12,
                    "maxRilancio", 500
            ));
            Map<String, Object> err = ws1.waitFor(
                    msg -> msg.get("erroreOperazione") != null, 5000);
            assertTrue(err.get("erroreOperazione").toString().contains("admin"));
        }
    }

    @Test
    public void nonAdminCannotLiberaSemaforo() throws Exception {
        try (AstaHttpClient http = new AstaHttpClient(baseUrl);
             AstaWsClient ws = new AstaWsClient()) {
            http.init();
            ws.connect(baseUrl, http.getJsessionId());
            ws.connetti("GIOC1", 1);
            ws.waitForConnettiOk(5000);

            ws.sendOperazione("liberaSemaforo", Map.of(
                    "nomegiocatoreOperaCome", "GIOC1"
            ));
            Map<String, Object> err = ws.waitFor(
                    msg -> msg.get("erroreOperazione") != null, 5000);
            assertTrue(err.get("erroreOperazione").toString().contains("admin"));
        }
    }

    @Test
    public void disconnectAll_notifiesClientsAndClearsSessions() throws Exception {
        try (AstaHttpClient http0 = new AstaHttpClient(baseUrl);
             AstaHttpClient http1 = new AstaHttpClient(baseUrl);
             AstaWsClient ws0 = new AstaWsClient();
             AstaWsClient ws1 = new AstaWsClient()) {
            http0.init();
            http1.init();
            ws0.connect(baseUrl, http0.getJsessionId());
            ws1.connect(baseUrl, http1.getJsessionId());
            ws0.connetti("GIOC0", 0);
            ws1.connetti("GIOC1", 1);
            ws0.waitForConnettiOk(5000);
            ws1.waitForConnettiOk(5000);
            assertEquals(2, socketHandler.getUtentiLoggati().size());

            http0.postJson("/fantaasta/disconnectAll", Map.of("tokenDispositiva", 1));

            ws0.waitFor(msg -> Boolean.TRUE.equals(msg.get("DISCONNECT_ALL")), 5000);
            ws1.waitFor(msg -> Boolean.TRUE.equals(msg.get("DISCONNECT_ALL")), 5000);
            assertEquals(0, socketHandler.getSessions().size());
            assertEquals(0, socketHandler.getUtentiLoggati().size());

            Map<String, Object> init0 = http0.init();
            assertFalse(init0.containsKey("giocatoreLoggato"));
        }
    }

    @Test
    public void ping_keepsSessionAlive() throws Exception {
        try (AstaHttpClient http = new AstaHttpClient(baseUrl);
             AstaWsClient ws = new AstaWsClient()) {
            http.init();
            ws.connect(baseUrl, http.getJsessionId());
            ws.connetti("GIOC0", 0);
            ws.waitForConnettiOk(5000);

            ws.sendOperazione("ping", Map.of(
                    "nomegiocatore", "GIOC0",
                    "idgiocatore", "0"
            ));
            Thread.sleep(300);
            assertEquals(1, socketHandler.getUtentiLoggati().size());
            assertTrue(sessionRegistry.isLoggedIn("GIOC0"));
        }
    }

    private void runAuction(AstaWsClient ws, int playerId, String playerName, int winningBid) throws Exception {
        ws.sendOperazione("start", Map.of(
                "nomegiocatore", "GIOC0",
                "idgiocatore", "0",
                "nomegiocatoreOperaCome", "GIOC0",
                "idgiocatoreOperaCome", "0",
                "selCalciatore", playerId + "@" + playerName,
                "selCalciatoreMacroRuolo", "A"
        ));
        ws.waitFor(msg -> msg.get("offertaVincente") != null, 5000);
        ws.sendOperazione("inviaOfferta", Map.of(
                "nomegiocatore", "GIOC0",
                "idgiocatore", "0",
                "nomegiocatoreOperaCome", "GIOC0",
                "idgiocatoreOperaCome", "0",
                "offerta", winningBid,
                "maxRilancio", 500
        ));
        ws.waitForOffertaVincente("GIOC0", winningBid, 5000);
        ws.sendOperazione("terminaAsta", Map.of("nomegiocatore", "GIOC0", "idgiocatore", "0"));
        ws.waitFor(msg -> "DA_CONFERMARE".equals(msg.get("faseAsta")), 5000);
        ws.sendOperazione("confermaAsta", Map.of("nomegiocatore", "GIOC0", "idgiocatore", "0"));
        ws.waitFor(msg -> msg.get("mapSpesoTotale") != null, 8000);
        ws.clearMessages();
    }
}
