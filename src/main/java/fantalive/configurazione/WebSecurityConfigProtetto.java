package fantalive.configurazione;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@Configuration
@Order(0)
@Profile(value = "protetto")
	public class WebSecurityConfigProtetto extends WebSecurityConfigurerAdapter {

	@Value("${security.user.name}")
	private String nomeLega;

	@Override
    protected void configure(AuthenticationManagerBuilder auth)
      throws Exception {
		auth
          .inMemoryAuthentication()
          .withUser(nomeLega)
          .password("pwd")
          .roles("USER");
    }
	
	/*
	  @Override
	  protected void configure(HttpSecurity http) throws Exception {
	    http
	    .csrf().disable()
	    .requiresChannel()
	      .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
	      .requiresSecure();
	    
	  }
	  */
	
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
    	 httpSecurity
         .authorizeRequests()
             .antMatchers("/**").hasRole("USER")
//         .antMatchers("/login.html", "/loginAPI").permitAll().anyRequest()
//         .authenticated()
          .and()
          	 .formLogin()
             .defaultSuccessUrl("/")
             .permitAll()
          .and()
          	  .csrf()
              .disable();             
    }	
	
	}

