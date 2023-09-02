package com.daniele.fantalive.configurazione;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Component
public class Config extends SpringBootServletInitializer implements WebSocketConfigurer {

	@Autowired SocketHandlerFantalive socketHandlerFantalive;
	
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(socketHandlerFantalive, "fantalive/fantalive-websocket")
		.addInterceptors(new HttpSessionIdHandshakeInterceptor());
	}

}
