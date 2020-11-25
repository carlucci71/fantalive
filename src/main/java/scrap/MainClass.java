package scrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MainClass extends SpringBootServletInitializer{
	public static void main(String[] args) {
		SpringApplication.run(MainClass.class, args);
	}

}



