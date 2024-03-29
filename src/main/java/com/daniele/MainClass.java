package com.daniele;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableScheduling
@EnableWebSocket
@EnableAsync(proxyTargetClass = true) 
@EnableCaching(proxyTargetClass = true) 
//@EnableWebMvc

public class MainClass  {
	public static void main(String[] args) {
		SpringApplication.run(MainClass.class, args);
	}
	
}



