package com.neiva.querillas.security;

import com.neiva.querillas.domain.entity.Usuario;
import com.neiva.querillas.domain.repo.UsuarioRepository;
import com.neiva.querillas.security.dto.*;
import com.neiva.querillas.web.dto.UsuarioCreateDTO;
import com.neiva.querillas.web.dto.UsuarioResponse;
import com.neiva.querillas.domain.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    /**
     * POST /api/auth/login
     * Iniciar sesión con credenciales
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        UserDetails ud = (UserDetails) auth.getPrincipal();
        String token = jwtUtil.generateToken(ud.getUsername());
        return ResponseEntity.ok(new LoginResponse(token, "Bearer"));
    }

    /**
     * POST /api/auth/register
     * Registrar un nuevo usuario y retornar token de acceso
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest req) {
        // Convertir RegisterRequest a UsuarioCreateDTO
        UsuarioCreateDTO createDTO = UsuarioCreateDTO.builder()
                .nombre(req.getNombre())
                .email(req.getEmail())
                .telefono(req.getTelefono())
                .rol(req.getRol())
                .zona(req.getZona())
                .password(req.getPassword())
                .build();

        // Crear el usuario usando el servicio
        UsuarioResponse usuarioResponse = usuarioService.crear(createDTO);

        // Generar token para el nuevo usuario
        String token = jwtUtil.generateToken(usuarioResponse.getEmail());

        // Construir respuesta
        RegisterResponse.UsuarioInfo usuarioInfo = RegisterResponse.UsuarioInfo.builder()
                .id(usuarioResponse.getId())
                .nombre(usuarioResponse.getNombre())
                .email(usuarioResponse.getEmail())
                .telefono(usuarioResponse.getTelefono())
                .rol(usuarioResponse.getRol())
                .estado(usuarioResponse.getEstado())
                .zona(usuarioResponse.getZona())
                .creadoEn(usuarioResponse.getCreadoEn())
                .build();

        RegisterResponse response = RegisterResponse.builder()
                .token(token)
                .type("Bearer")
                .usuario(usuarioInfo)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/refresh
     * Renovar token de acceso con un token válido existente
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        // Validar el token actual
        if (!jwtUtil.validate(req.getToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Extraer el username del token
        String username = jwtUtil.extractUsername(req.getToken());

        // Verificar que el usuario existe y está activo
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Generar nuevo token
        String newToken = jwtUtil.generateToken(username);

        return ResponseEntity.ok(new LoginResponse(newToken, "Bearer"));
    }
}
