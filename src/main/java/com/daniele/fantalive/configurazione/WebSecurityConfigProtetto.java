package com.daniele.fantalive.configurazione;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@EnableWebSecurity
@Configuration
@Order(0)
@Profile(value = "protetto")
	public class WebSecurityConfigProtetto extends WebSecurityConfigurerAdapter {

	@Value("${SECURITY_USER_NAME}")
	private String nomeLega;

	@Override
    protected void configure(AuthenticationManagerBuilder auth)
      throws Exception {
		auth
          .inMemoryAuthentication()
          .withUser(nomeLega)
          .password("{noop}pwd")
          .roles("USER");
    }
	
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
    	 httpSecurity
             .authorizeRequests()
     	     .antMatchers("/index2.html").permitAll()
     	     .antMatchers("/fantalive/app.js").permitAll()
      	     .antMatchers("/fantalive/setKeepAliveEnd/**").permitAll()
             .antMatchers("/**").hasRole("USER")
          .and()
          	 .formLogin()
             .defaultSuccessUrl("/")
             .permitAll()
          .and()
          	  .csrf()
              .disable()             
              .sessionManagement()
              .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
              .sessionFixation().none();
    	 }	
	
	}


