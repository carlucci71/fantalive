package scrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableScheduling
@EnableWebSocket
@EnableAsync(proxyTargetClass = true) 
@EnableCaching(proxyTargetClass = true) 

public class MainClass  {
	public static void main(String[] args) {
		SpringApplication.run(MainClass.class, args);
	}
	
}



