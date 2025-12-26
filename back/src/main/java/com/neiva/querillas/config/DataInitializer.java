package com.neiva.querillas.config;

import com.neiva.querillas.domain.entity.*;
import com.neiva.querillas.domain.model.*;
import com.neiva.querillas.domain.repo.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Arrays;

@Configuration
@Profile("dev")
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            EstadoRepository estadoRepo,
            TemaRepository temaRepo,
            ComunaRepository comunaRepo,
            UsuarioRepository usuarioRepo,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            System.out.println("======================================");
            System.out.println("Inicializando datos de prueba...");
            System.out.println("======================================");

            // Crear Estados
            if (estadoRepo.count() == 0) {
                estadoRepo.saveAll(Arrays.asList(
                    Estado.builder().modulo("QUERELLA").nombre("RECIBIDA").creadoEn(OffsetDateTime.now()).build(),
                    Estado.builder().modulo("QUERELLA").nombre("ASIGNADA").creadoEn(OffsetDateTime.now()).build(),
                    Estado.builder().modulo("QUERELLA").nombre("EN_INVESTIGACION").creadoEn(OffsetDateTime.now()).build(),
                    Estado.builder().modulo("QUERELLA").nombre("RESUELTA").creadoEn(OffsetDateTime.now()).build(),
                    Estado.builder().modulo("QUERELLA").nombre("CERRADA").creadoEn(OffsetDateTime.now()).build(),
                    Estado.builder().modulo("DESPACHO").nombre("PENDIENTE").creadoEn(OffsetDateTime.now()).build(),
                    Estado.builder().modulo("DESPACHO").nombre("EN_PROCESO").creadoEn(OffsetDateTime.now()).build(),
                    Estado.builder().modulo("DESPACHO").nombre("FINALIZADO").creadoEn(OffsetDateTime.now()).build()
                ));
                System.out.println("✓ Estados creados");
            }

            // Crear Temas
            if (temaRepo.count() == 0) {
                temaRepo.saveAll(Arrays.asList(
                    new Tema(null, "Ruido Excesivo"),
                    new Tema(null, "Contaminación Ambiental"),
                    new Tema(null, "Ocupación Espacio Público"),
                    new Tema(null, "Problemas de Convivencia"),
                    new Tema(null, "Animales Domésticos")
                ));
                System.out.println("✓ Temas creados");
            }

            // Crear Comunas
            if (comunaRepo.count() == 0) {
                comunaRepo.saveAll(Arrays.asList(
                    new Comuna(null, "Comuna 1 - Centro"),
                    new Comuna(null, "Comuna 2 - Norte"),
                    new Comuna(null, "Comuna 3 - Sur")
                ));
                System.out.println("✓ Comunas creadas");
            }

            // Crear Usuarios de prueba (solo si no existen)
            String hashedPassword = passwordEncoder.encode("password123");
            System.out.println("Hash BCrypt generado para password123: " + hashedPassword);

            // Buscar o crear usuario director
            Usuario director = usuarioRepo.findByEmail("director@querellas.com")
                    .orElseGet(() -> {
                        Usuario u = Usuario.builder()
                                .nombre("Juan Director")
                                .email("director@querellas.com")
                                .telefono("3001234567")
                                .password(hashedPassword)
                                .rol(RolUsuario.DIRECTOR)
                                .estado(EstadoUsuario.ACTIVO)
                                .creadoEn(OffsetDateTime.now())
                                .build();
                        return usuarioRepo.save(u);
                    });

            // Buscar o crear usuario auxiliar
            Usuario auxiliar = usuarioRepo.findByEmail("auxiliar@querellas.com")
                    .orElseGet(() -> {
                        Usuario u = Usuario.builder()
                                .nombre("María Auxiliar")
                                .email("auxiliar@querellas.com")
                                .telefono("3001234568")
                                .password(hashedPassword)
                                .rol(RolUsuario.AUXILIAR)
                                .estado(EstadoUsuario.ACTIVO)
                                .creadoEn(OffsetDateTime.now())
                                .build();
                        return usuarioRepo.save(u);
                    });

            // Buscar o crear usuario inspector
            Usuario inspector = usuarioRepo.findByEmail("inspector1@querellas.com")
                    .orElseGet(() -> {
                        Usuario u = Usuario.builder()
                                .nombre("Pedro Inspector")
                                .email("inspector1@querellas.com")
                                .telefono("3001234569")
                                .password(hashedPassword)
                                .rol(RolUsuario.INSPECTOR)
                                .estado(EstadoUsuario.ACTIVO)
                                .zona(ZonaInspector.NEIVA)
                                .creadoEn(OffsetDateTime.now())
                                .build();
                        return usuarioRepo.save(u);
                    });

            System.out.println("✓ Usuarios de prueba disponibles (password: password123)");

            System.out.println("======================================");
            System.out.println("Datos de prueba cargados correctamente");
            System.out.println("Usuarios disponibles:");
            System.out.println("  - director@querellas.com / password123");
            System.out.println("  - auxiliar@querellas.com / password123");
            System.out.println("  - inspector1@querellas.com / password123");
            System.out.println("======================================");
        };
    }
}
