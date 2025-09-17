package com.daniele.fantalive.configurazione;

//@Configuration
//@Order(100)
//@EnableWebSecurity
//@Profile(("!protetto"))
public class WebSecurityConfig {
/*
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .requiresChannel(channel -> channel
        .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
        .requiresSecure()
      );
    return http.build();
  }

 */
}
