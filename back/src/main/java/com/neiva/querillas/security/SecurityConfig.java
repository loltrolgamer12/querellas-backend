package com.neiva.querillas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)   // 👈 IMPORTANTE para que funcionen @PreAuthorize
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter; // 👈 ya tienes esta clase

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas
                .requestMatchers(
                    "/api/auth/**",
                    "/auth/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health"
                ).permitAll()
                // Todo lo demás requiere estar autenticado
                .anyRequest().authenticated()
            )
            // Deshabilitamos login por formulario y basic
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable())
            // Filtro JWT antes del UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // @Bean
    // PasswordEncoder passwordEncoder() {
    //     return new BCryptPasswordEncoder();
    // }
}





// package com.neiva.querillas.security;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.config.Customizer;
// import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.crypto.password.NoOpPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// @Configuration
// @EnableMethodSecurity
// public class SecurityConfig {

//     private final JwtAuthFilter jwtAuthFilter;

//     public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
//         this.jwtAuthFilter = jwtAuthFilter;
//     }

//     @Bean
//     SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http
//             .csrf(csrf -> csrf.disable())
//             // Usa el bean corsConfigurationSource definido en CorsConfig
//             .cors(Customizer.withDefaults())
//             .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//             .authorizeHttpRequests(auth -> auth
//                 // públicos
//                 .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
//                 .requestMatchers("/auth/login").permitAll()
//                 // el resto requiere auth (deja así para que los tests de 403 funcionen)
//                 .anyRequest().authenticated()
//             )
//             .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

//         return http.build();
//     }

//     @Bean
//     PasswordEncoder passwordEncoder() {
//         // Solo para desarrollo/demo; en prod usa BCrypt
//         return NoOpPasswordEncoder.getInstance();
//     }

//     @Bean
//     AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
//         return cfg.getAuthenticationManager();
//     }
// }



// // package com.neiva.querillas.security;

// // import org.springframework.context.annotation.Bean;
// // import org.springframework.context.annotation.Configuration;
// // import org.springframework.security.authentication.*;
// // import org.springframework.security.config.Customizer;
// // import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// // import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// // import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// // import org.springframework.security.config.http.SessionCreationPolicy;
// // import org.springframework.security.crypto.password.*;
// // import org.springframework.security.web.SecurityFilterChain;
// // import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// // import org.springframework.web.cors.*;

// // import java.util.List;

// // @Configuration
// // @EnableMethodSecurity  
// // public class SecurityConfig {

// //     private final JwtAuthFilter jwtAuthFilter;

// //     public SecurityConfig(JwtAuthFilter f) {
// //         this.jwtAuthFilter = f;
// //     }

// //     @Bean
// //     SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
// //         http
// //             .csrf(csrf -> csrf.disable())
// //             .cors(c -> c.configurationSource(corsConfigurationSource()))
// //             .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
// //             .authorizeHttpRequests(auth -> auth
// //                 // Swagger + Actuator abiertos
// //                 .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()

// //                 // Auth abierto
// //                 .requestMatchers("/auth/login").permitAll()

// //                 // Querellas: lectura pública (ajústalo a tu política)
// //                 .requestMatchers("/api/querellas", "/api/querellas/**").permitAll()

// //                 // Todo lo demás protegido
// //                 .anyRequest().authenticated()
// //             )
// //             .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

// //         return http.build();
// //     }

// //     @Bean
// //     PasswordEncoder passwordEncoder() {
// //         // Para DEMO usamos sin hash. En prod usa BCrypt.
// //         return NoOpPasswordEncoder.getInstance();
// //         // return new BCryptPasswordEncoder();
// //     }

// //     @Bean
// //     AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
// //         return cfg.getAuthenticationManager();
// //     }

// //     @Bean
// //     CorsConfigurationSource corsConfigurationSource() {
// //         CorsConfiguration cfg = new CorsConfiguration();
// //         cfg.setAllowedOrigins(List.of("*")); // Ajusta para tu front
// //         cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
// //         cfg.setAllowedHeaders(List.of("Authorization","Content-Type"));
// //         cfg.setExposedHeaders(List.of("Authorization"));
// //         cfg.setAllowCredentials(false);
// //         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
// //         source.registerCorsConfiguration("/**", cfg);
// //         return source;
// //     }
// // }



// // // package com.neiva.querillas.security;

// // // import org.springframework.context.annotation.Bean;
// // // import org.springframework.context.annotation.Configuration;
// // // import org.springframework.security.config.Customizer;
// // // import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// // // import org.springframework.security.web.SecurityFilterChain;

// // // @Configuration
// // // public class SecurityConfig {

// // //     @Bean
// // //     SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
// // //         http
// // //             .csrf(csrf -> csrf.disable())
// // //             .cors(Customizer.withDefaults())
// // //             .authorizeHttpRequests(auth -> auth
// // //                 .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/api/**").permitAll()
// // //                 .anyRequest().permitAll()   // ← por ahora abierto; luego lo cerramos con JWT
// // //             );
// // //         return http.build();
// // //     }
// // // }
