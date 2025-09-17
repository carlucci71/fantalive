package com.daniele.fantalive.configurazione;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

//@Configuration
//@EnableWebSecurity
@Order(0)
@Profile("protetto")
public class WebSecurityConfigProtetto {

    @Value("${SECURITY_USER_NAME}")
    private String nomeLega;

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername(nomeLega)
                .password("{noop}pwd")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/index2.html").permitAll()
                .requestMatchers("/fantalive/app.js").permitAll()
                .requestMatchers("/fantalive/setKeepAliveEnd/**").permitAll()
                .requestMatchers("/**").hasRole("USER")
            )
            .formLogin(form -> form
                .defaultSuccessUrl("/")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
