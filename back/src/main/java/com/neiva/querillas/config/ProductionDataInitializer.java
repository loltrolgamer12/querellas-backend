package com.neiva.querillas.config;

import com.neiva.querillas.domain.entity.*;
import com.neiva.querillas.domain.repo.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Inicialización de datos esenciales para producción
 * Solo crea catálogos necesarios (Estados, Temas, Comunas)
 * NO crea usuarios de prueba
 */
@Configuration
@Profile("prod")
public class ProductionDataInitializer {

    @Bean
    CommandLineRunner initProductionData(
            EstadoRepository estadoRepo,
            TemaRepository temaRepo,
            ComunaRepository comunaRepo
    ) {
        return args -> {
            System.out.println("======================================");
            System.out.println("Inicializando datos de producción...");
            System.out.println("======================================");

            // Crear Estados (requeridos para el sistema)
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
                System.out.println("✓ Estados del sistema creados");
            }

            // Crear Temas (ejemplos iniciales, personalizar según necesidad)
            if (temaRepo.count() == 0) {
                temaRepo.saveAll(Arrays.asList(
                    new Tema(null, "Ruido Excesivo"),
                    new Tema(null, "Contaminación Ambiental"),
                    new Tema(null, "Ocupación Espacio Público"),
                    new Tema(null, "Problemas de Convivencia"),
                    new Tema(null, "Animales Domésticos")
                ));
                System.out.println("✓ Temas iniciales creados");
            }

            // Crear Comunas (personalizar según la ciudad)
            if (comunaRepo.count() == 0) {
                comunaRepo.saveAll(Arrays.asList(
                    new Comuna(null, "Comuna 1 - Centro"),
                    new Comuna(null, "Comuna 2 - Norte"),
                    new Comuna(null, "Comuna 3 - Sur"),
                    new Comuna(null, "Comuna 4 - Este"),
                    new Comuna(null, "Comuna 5 - Oeste")
                ));
                System.out.println("✓ Comunas creadas");
            }

            System.out.println("======================================");
            System.out.println("Sistema listo para producción");
            System.out.println("IMPORTANTE: Crear usuario administrador");
            System.out.println("======================================");
        };
    }
}
