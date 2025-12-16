package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.Usuario;
import com.neiva.querillas.domain.model.EstadoUsuario;
import com.neiva.querillas.domain.model.RolUsuario;
import com.neiva.querillas.domain.repo.UsuarioRepository;
import com.neiva.querillas.web.dto.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Listar usuarios con paginación y filtro opcional por rol
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional(readOnly = true)
    public PaginaUsuarioResponse listar(RolUsuario rol, int page, int size) {
        log.info("Listando usuarios - rol: {}, page: {}, size: {}", rol, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "creadoEn"));
        Page<Usuario> usuariosPage = usuarioRepository.findAllByRol(rol, pageable);

        return PaginaUsuarioResponse.builder()
                .items(usuariosPage.getContent().stream()
                        .map(this::convertirAResponse)
                        .toList())
                .page(usuariosPage.getNumber())
                .size(usuariosPage.getSize())
                .total(usuariosPage.getTotalElements())
                .build();
    }

    /**
     * Obtener detalle de un usuario por ID
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        log.info("Obteniendo usuario por ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        return convertirAResponse(usuario);
    }

    /**
     * Crear un nuevo usuario
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public UsuarioResponse crear(UsuarioCreateDTO dto) {
        log.info("Creando usuario - email: {}, rol: {}", dto.getEmail(), dto.getRol());

        // Validar que el email no exista
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + dto.getEmail());
        }

        // Validar que si el rol es INSPECTOR, debe tener zona asignada
        if (dto.getRol() == RolUsuario.INSPECTOR) {
            if (dto.getZona() == null) {
                throw new IllegalArgumentException("Los usuarios con rol INSPECTOR deben tener una zona asignada (NEIVA o CORREGIMIENTO)");
            }
        }

        // Hashear la contraseña
        String passwordHasheado = passwordEncoder.encode(dto.getPassword());

        // Crear el usuario
        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .password(passwordHasheado)
                .rol(dto.getRol())
                .estado(EstadoUsuario.ACTIVO)
                .zona(dto.getZona())
                .build();

        usuario = usuarioRepository.save(usuario);
        log.info("Usuario creado con ID: {}", usuario.getId());

        return convertirAResponse(usuario);
    }

    /**
     * Actualizar un usuario existente
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateDTO dto) {
        log.info("Actualizando usuario ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        // Validar que el email no esté en uso por otro usuario
        if (!usuario.getEmail().equals(dto.getEmail())) {
            if (usuarioRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Ya existe otro usuario con el email: " + dto.getEmail());
            }
        }

        // Validar que si el rol es INSPECTOR, debe tener zona asignada
        if (dto.getRol() == RolUsuario.INSPECTOR) {
            if (dto.getZona() == null) {
                throw new IllegalArgumentException("Los usuarios con rol INSPECTOR deben tener una zona asignada (NEIVA o CORREGIMIENTO)");
            }
        }

        // Actualizar campos
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefono(dto.getTelefono());
        usuario.setRol(dto.getRol());
        usuario.setEstado(dto.getEstado());
        usuario.setZona(dto.getZona());

        usuario = usuarioRepository.save(usuario);
        log.info("Usuario actualizado: {}", usuario.getId());

        return convertirAResponse(usuario);
    }

    /**
     * Cambiar estado de un usuario (bloquear/desbloquear)
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public UsuarioResponse cambiarEstado(Long id, CambioEstadoUsuarioDTO dto) {
        log.info("Cambiando estado de usuario ID: {} a {}", id, dto.getNuevoEstado());

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        usuario.setEstado(dto.getNuevoEstado());
        usuario = usuarioRepository.save(usuario);

        log.info("Estado de usuario {} cambiado a: {}. Motivo: {}", id, dto.getNuevoEstado(), dto.getMotivo());

        return convertirAResponse(usuario);
    }

    /**
     * Eliminar un usuario (soft delete - cambiar estado a NO_DISPONIBLE)
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando usuario ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        usuario.setEstado(EstadoUsuario.NO_DISPONIBLE);
        usuarioRepository.save(usuario);

        log.info("Usuario {} marcado como NO_DISPONIBLE", id);
    }

    /**
     * Convertir entidad Usuario a DTO de respuesta
     */
    private UsuarioResponse convertirAResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol())
                .estado(usuario.getEstado())
                .zona(usuario.getZona())
                .creadoEn(usuario.getCreadoEn())
                .actualizadoEn(usuario.getActualizadoEn())
                .build();
    }
}
