package com.neiva.querillas.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableMethodSecurity(prePostEnabled = true)
public class TestSecurityConfig {

  @Bean
  SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
    // desactiva CSRF en tests y requiere autenticación por defecto (MockMvc usa @WithMockUser)
    http.csrf(csrf -> csrf.disable());
    http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
    return http.build();
  }

  @Bean
  UserDetailsService userDetailsService(PasswordEncoder encoder) {
    return new InMemoryUserDetailsManager(
      User.withUsername("dir").password(encoder.encode("x")).roles("DIRECTORA").build(),
      User.withUsername("aux").password(encoder.encode("x")).roles("AUXILIAR").build(),
      User.withUsername("insp").password(encoder.encode("x")).roles("INSPECTOR").build()
    );
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    // para test: sin encriptar
    return NoOpPasswordEncoder.getInstance();
  }
}
