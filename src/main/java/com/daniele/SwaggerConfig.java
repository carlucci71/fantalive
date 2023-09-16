package com.daniele;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

//@Configuration
public class SwaggerConfig{
    @Bean
    public Docket getDocket() {
    	return new Docket(DocumentationType.SWAGGER_2)
    		    .select()
    		    .apis(RequestHandlerSelectors.basePackage("com.daniele.asta"))
    		    .build();    }
    
    @Bean
    public InternalResourceViewResolver defaultViewResolver() {
        return new InternalResourceViewResolver();
    }    
    
}
