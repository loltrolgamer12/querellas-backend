package com.neiva.querillas.security;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
public class InMemoryUserDetailsService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;

    public InMemoryUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if ("directora".equals(username)) {
            return User.builder()
                    .username("directora")
                    .password(passwordEncoder.encode("demo123"))  // 👈 ahora BCrypt
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_DIRECTORA")))
                    .build();
        }

        if ("auxiliar".equals(username)) {
            return User.builder()
                    .username("auxiliar")
                    .password(passwordEncoder.encode("demo123"))
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_AUXILIAR")))
                    .build();
        }

        if ("inspector".equals(username)) {
            return User.builder()
                    .username("inspector")
                    .password(passwordEncoder.encode("demo123"))
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_INSPECTOR")))
                    .build();
        }

        // opcional: admin
        if ("admin".equals(username)) {
            return User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .build();
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }
}
