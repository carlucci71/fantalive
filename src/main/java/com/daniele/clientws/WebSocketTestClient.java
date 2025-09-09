package com.daniele.clientws;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@ClientEndpoint
public class WebSocketTestClient {

    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Sessione aperta!");
    }

    @OnMessage
    public void onMessage(String message) {
         System.out.println("Messaggio ricevuto: " + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Connessione chiusa: " + closeReason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Errore: " + throwable.getMessage());
    }

    public void sendMessage(String message) {
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(message);
        }
    }


    public static void main(String[] args) throws Exception {
        /*
        String uri = "ws://85.235.148.177:8080/messaggi-websocket";
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        WebSocketTestClient client = new WebSocketTestClient();
        container.connectToServer(client, new URI(uri));

        Scanner scanner = new Scanner(System.in);
        System.out.println("Scrivi un messaggio da inviare, 'exit' per uscire:");
        while (true) {
            String input = scanner.nextLine();
            if ("exit".equalsIgnoreCase(input)) {
                break;
            }
            client.sendMessage(input);
        }
        scanner.close();
         */
        Random random = new Random();
        WebSocketTestClient clientExt = new WebSocketTestClient();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        int max=100;
        for (int i=0;i<=max;i++){
            executor.execute(()->{
                try {
                    String uri = "ws://85.235.148.177:8080/messaggi-websocket";
                    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                    WebSocketTestClient client;
                    if (random.nextBoolean()){
                        client = new WebSocketTestClient();
                    } else {
                        client=clientExt;
                    }

                    container.connectToServer(client, new URI(uri));
                    String messaggio = "{\"operazione\":\"inviaOfferta\",\"maxRilancio\":475,\"nomegiocatore\":\"Daniele\",\"idgiocatore\":\"5\",\"nomegiocatoreOperaCome\":\"Daniele\",\"idgiocatoreOperaCome\":\"5\",\"offerta\":"
                            + (random.nextInt(500) + 1)
                            + "}";
                    client.sendMessage(messaggio);
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }

            });
        }
    }
}
