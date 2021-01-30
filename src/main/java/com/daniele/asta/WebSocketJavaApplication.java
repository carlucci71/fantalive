package com.daniele.asta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

public class WebSocketJavaApplication extends SpringBootServletInitializer{

	public static void main(String[] args) {
		SpringApplication.run(WebSocketJavaApplication.class, args);
	}
}
