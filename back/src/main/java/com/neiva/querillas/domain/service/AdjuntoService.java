package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.Adjunto;
import com.neiva.querillas.domain.entity.Querella;
import com.neiva.querillas.domain.entity.Usuario;
import com.neiva.querillas.domain.repo.AdjuntoRepository;
import com.neiva.querillas.domain.repo.QuerellaRepository;
import com.neiva.querillas.domain.repo.UsuarioRepository;
import com.neiva.querillas.web.dto.AdjuntoResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdjuntoService {

    private final AdjuntoRepository adjuntoRepository;
    private final QuerellaRepository querellaRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${adjuntos.storage.path:/tmp/querellas/adjuntos}")
    private String storagePath;

    @Value("${adjuntos.max-file-size:10485760}") // 10MB por defecto
    private long maxFileSize;

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "application/pdf",
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /**
     * Listar adjuntos de una querella
     */
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    @Transactional(readOnly = true)
    public List<AdjuntoResponse> listar(Long querellaId) {
        log.info("Listando adjuntos de querella {}", querellaId);

        // Validar que la querella existe
        if (!querellaRepository.existsById(querellaId)) {
            throw new EntityNotFoundException("Querella no encontrada con ID: " + querellaId);
        }

        List<Adjunto> adjuntos = adjuntoRepository.findByQuerellaIdOrderByCreadoEnDesc(querellaId);

        return adjuntos.stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Subir adjunto
     */
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    @Transactional
    public AdjuntoResponse subir(Long querellaId, MultipartFile archivo, String descripcion) {
        log.info("Subiendo adjunto para querella {} - archivo: {}", querellaId, archivo.getOriginalFilename());

        // Validar que la querella existe
        Querella querella = querellaRepository.findById(querellaId)
                .orElseThrow(() -> new EntityNotFoundException("Querella no encontrada con ID: " + querellaId));

        // Validar tamaño del archivo
        if (archivo.getSize() > maxFileSize) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido de " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // Validar tipo de archivo
        String contentType = archivo.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Tipos permitidos: JPG, PNG, PDF, DOC, DOCX");
        }

        // Obtener usuario actual
        Long usuarioId = obtenerUsuarioActualId();
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        // Generar nombre único para el archivo
        String nombreOriginal = archivo.getOriginalFilename();
        String extension = nombreOriginal != null && nombreOriginal.contains(".")
                ? nombreOriginal.substring(nombreOriginal.lastIndexOf("."))
                : "";
        String nombreUnico = UUID.randomUUID().toString() + extension;

        // Crear directorio de storage si no existe
        Path directorioStorage = Paths.get(storagePath);
        try {
            Files.createDirectories(directorioStorage);
        } catch (IOException e) {
            log.error("Error creando directorio de storage", e);
            throw new RuntimeException("Error al crear directorio de almacenamiento", e);
        }

        // Guardar archivo en filesystem
        Path rutaArchivo = directorioStorage.resolve(nombreUnico);
        try {
            Files.copy(archivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
            log.info("Archivo guardado en: {}", rutaArchivo);
        } catch (IOException e) {
            log.error("Error guardando archivo", e);
            throw new RuntimeException("Error al guardar archivo", e);
        }

        // Crear registro en BD
        Adjunto adjunto = Adjunto.builder()
                .querella(querella)
                .nombreArchivo(nombreOriginal)
                .tipoArchivo(contentType)
                .tamanoBytes(archivo.getSize())
                .rutaStorage(rutaArchivo.toString())
                .descripcion(descripcion)
                .cargadoPor(usuario)
                .build();

        adjunto = adjuntoRepository.save(adjunto);
        log.info("Adjunto creado con ID: {}", adjunto.getId());

        return convertirAResponse(adjunto);
    }

    /**
     * Descargar adjunto
     */
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    @Transactional(readOnly = true)
    public Resource descargar(Long querellaId, Long adjuntoId) {
        log.info("Descargando adjunto {} de querella {}", adjuntoId, querellaId);

        Adjunto adjunto = adjuntoRepository.findById(adjuntoId)
                .orElseThrow(() -> new EntityNotFoundException("Adjunto no encontrado con ID: " + adjuntoId));

        // Validar que el adjunto pertenece a la querella
        if (!adjunto.getQuerella().getId().equals(querellaId)) {
            throw new IllegalArgumentException("El adjunto no pertenece a la querella especificada");
        }

        // Leer archivo del filesystem
        try {
            Path rutaArchivo = Paths.get(adjunto.getRutaStorage());
            Resource resource = new UrlResource(rutaArchivo.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("No se puede leer el archivo: " + adjunto.getNombreArchivo());
            }
        } catch (Exception e) {
            log.error("Error leyendo archivo", e);
            throw new RuntimeException("Error al descargar archivo", e);
        }
    }

    /**
     * Obtener información de adjunto (para headers de descarga)
     */
    @Transactional(readOnly = true)
    public Adjunto obtenerPorId(Long adjuntoId) {
        return adjuntoRepository.findById(adjuntoId)
                .orElseThrow(() -> new EntityNotFoundException("Adjunto no encontrado con ID: " + adjuntoId));
    }

    /**
     * Eliminar adjunto
     */
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    @Transactional
    public void eliminar(Long querellaId, Long adjuntoId) {
        log.info("Eliminando adjunto {} de querella {}", adjuntoId, querellaId);

        Adjunto adjunto = adjuntoRepository.findById(adjuntoId)
                .orElseThrow(() -> new EntityNotFoundException("Adjunto no encontrado con ID: " + adjuntoId));

        // Validar que el adjunto pertenece a la querella
        if (!adjunto.getQuerella().getId().equals(querellaId)) {
            throw new IllegalArgumentException("El adjunto no pertenece a la querella especificada");
        }

        // Eliminar archivo del filesystem
        try {
            Path rutaArchivo = Paths.get(adjunto.getRutaStorage());
            Files.deleteIfExists(rutaArchivo);
            log.info("Archivo eliminado: {}", rutaArchivo);
        } catch (IOException e) {
            log.error("Error eliminando archivo del filesystem", e);
            // No lanzamos excepción para que se pueda eliminar el registro de BD
        }

        // Eliminar registro de BD
        adjuntoRepository.delete(adjunto);
        log.info("Adjunto {} eliminado", adjuntoId);
    }

    /**
     * Obtener el ID del usuario actualmente autenticado
     */
    private Long obtenerUsuarioActualId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuario no autenticado");
        }

        String username = authentication.getName();

        // Mapeo temporal de usuarios en memoria a IDs
        return switch (username) {
            case "directora" -> 1L;
            case "auxiliar" -> 2L;
            case "inspector" -> 3L;
            default -> 1L;
        };
    }

    /**
     * Convertir entidad Adjunto a DTO de respuesta
     */
    private AdjuntoResponse convertirAResponse(Adjunto adjunto) {
        return AdjuntoResponse.builder()
                .id(adjunto.getId())
                .nombreArchivo(adjunto.getNombreArchivo())
                .tipoArchivo(adjunto.getTipoArchivo())
                .tamanoBytes(adjunto.getTamanoBytes())
                .descripcion(adjunto.getDescripcion())
                .cargadoPor(AdjuntoResponse.CargadoPorDTO.builder()
                        .id(adjunto.getCargadoPor().getId())
                        .nombre(adjunto.getCargadoPor().getNombre())
                        .build())
                .creadoEn(adjunto.getCreadoEn())
                .url("/api/querellas/" + adjunto.getQuerella().getId() + "/adjuntos/" + adjunto.getId() + "/descargar")
                .build();
    }
}
