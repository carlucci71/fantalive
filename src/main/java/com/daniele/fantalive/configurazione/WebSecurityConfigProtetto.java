package com.daniele.fantalive.configurazione;

//@Configuration
//@EnableWebSecurity
//@Order(0)
//@Profile("protetto")
public class WebSecurityConfigProtetto {
/*
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

 */
}
