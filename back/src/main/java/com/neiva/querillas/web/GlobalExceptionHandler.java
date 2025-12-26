package com.neiva.querillas.web;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    // ---------- Helpers ----------
    private Map<String, Object> baseBody(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        if (message != null && !message.isBlank()) {
            body.put("message", message);
        }
        return body;
    }

    // 400 - Errores de validación de @Valid en bodies DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Validación fallida");
        Map<String, String> fields = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        body.put("fields", fields);
        return ResponseEntity.badRequest().body(body);
    }

    // 400 - Errores de validación en parámetros
    // @RequestParam/@PathVariable/@Validated
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Validación de parámetros fallida");
        Map<String, String> fields = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage(),
                        (a, b) -> a));
        body.put("fields", fields);
        return ResponseEntity.badRequest().body(body);
    }

    // 400 - Body malformado / JSON inválido / tipo no convertible
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleNotReadable(HttpMessageNotReadableException ex) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                "Cuerpo de la solicitud inválido o malformado");
        return ResponseEntity.badRequest().body(body);
    }

    // 400 - Falta un parámetro obligatorio
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                "Falta el parámetro obligatorio: " + ex.getParameterName());
        return ResponseEntity.badRequest().body(body);
    }

    // 400 - Tipo de parámetro no coincide (ej: id no numérico)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String msg = "Parámetro '" + ex.getName() + "' no tiene el tipo esperado";
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "BAD_REQUEST", msg);
        return ResponseEntity.badRequest().body(body);
    }

    // 400 - Regla de negocio rota (tú lanzas IllegalArgumentException)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    // 404 - Entidad no encontrada
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFound(EntityNotFoundException ex) {
        Map<String, Object> body = baseBody(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 404 - Ruta no encontrada (si habilitas throw-exception-if-no-handler-found)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handleNoHandler(NoHandlerFoundException ex) {
        Map<String, Object> body = baseBody(HttpStatus.NOT_FOUND, "NOT_FOUND", "Recurso no encontrado");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 405 - Método HTTP no soportado
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        Map<String, Object> body = baseBody(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Método no permitido");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    // 403 - Acceso denegado (cuando integres seguridad real)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> body = baseBody(HttpStatus.FORBIDDEN, "FORBIDDEN", "Acceso denegado");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // 409 - Violaciones de integridad (UNIQUE, CHECK, FK)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex) {
        // Ojo: no expongas detalles sensibles de BD en producción
        Map<String, Object> body = baseBody(HttpStatus.CONFLICT, "CONFLICT",
                "Violación de integridad de datos");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // // 500 - Fallback genérico
    // @ExceptionHandler(Exception.class)
    // public ResponseEntity<?> handleGeneric(Exception ex) {
    // // Loguéalo con nivel ERROR en un logger real; aquí no exponemos detalles
    // Map<String, Object> body = baseBody(HttpStatus.INTERNAL_SERVER_ERROR,
    // "INTERNAL_SERVER_ERROR",
    // "Ha ocurrido un error inesperado");
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    // }

    // 400 - Regla de negocio: transición de estado no permitida
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException ex) {
        Map<String, Object> body = baseBody(
                HttpStatus.BAD_REQUEST,
                "TRANSICION_NO_PERMITIDA",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

}
