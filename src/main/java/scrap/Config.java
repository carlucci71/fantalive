package scrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Component
public class Config extends SpringBootServletInitializer implements WebSocketConfigurer {

	@Autowired SocketHandler socketHandler;
	
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(socketHandler, "/messaggi-websocket")
		.addInterceptors(new HttpSessionIdHandshakeInterceptor());
	}

}
