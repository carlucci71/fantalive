package scrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@SpringBootApplication
@EnableScheduling
@EnableWebSocket

public class MainClass extends SpringBootServletInitializer implements WebSocketConfigurer {
	public static void main(String[] args) {
		SpringApplication.run(MainClass.class, args);
	}
	
	@Autowired SocketHandler socketHandler;
	
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(socketHandler, "/messaggi-websocket")
		.addInterceptors(new HttpSessionIdHandshakeInterceptor());
	}
}



