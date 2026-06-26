package com.innovatech.bff_service.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovatech.bff_service.dto.ErrorResponse;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> manejarErrorMicroservicio(
            FeignException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = resolverStatus(exception.status());

        String contenido = exception.contentUTF8();

        String mensaje = "Feign falló. " +
                "statusFeign=" + exception.status() +
                ", methodKey=" + exception.request().httpMethod() +
                ", url=" + exception.request().url() +
                ", body=" + (contenido == null || contenido.isBlank() ? "SIN_BODY" : contenido);

        System.out.println("ERROR FEIGN BFF >>> " + mensaje);

        ErrorResponse response = ErrorResponse.builder()
                .fecha(LocalDateTime.now())
                .status(status.value())
                .error(resolverTituloError(status))
                .mensaje(mensaje)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse manejarValidaciones(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> validaciones = new HashMap<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            validaciones.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ErrorResponse.builder()
                .fecha(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Error de validación")
                .mensaje("Existen campos inválidos en la solicitud: " + validaciones)
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse manejarErrorGeneral(
            Exception exception,
            HttpServletRequest request
    ) {
        return ErrorResponse.builder()
                .fecha(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error interno del BFF")
                .mensaje(exception.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    private HttpStatus resolverStatus(int statusFeign) {
        HttpStatus status = HttpStatus.resolve(statusFeign);

        if (status == null) {
            return HttpStatus.BAD_GATEWAY;
        }

        return status;
    }

    private String resolverTituloError(HttpStatus status) {
        if (status.is4xxClientError()) {
            return "Solicitud rechazada por microservicio";
        }

        if (status.is5xxServerError()) {
            return "Error de comunicación con microservicio";
        }

        return "Respuesta de microservicio";
    }

    private String extraerMensajeFeign(FeignException exception, String fallback) {
        String contenido = exception.contentUTF8();

        if (contenido == null || contenido.isBlank()) {
            return fallback;
        }

        try {
            JsonNode json = objectMapper.readTree(contenido);

            if (json.hasNonNull("mensaje")) {
                return json.get("mensaje").asText();
            }

            if (json.hasNonNull("message")) {
                return json.get("message").asText();
            }

            if (json.hasNonNull("error")) {
                return json.get("error").asText();
            }

            return contenido;
        } catch (Exception ignored) {
            return contenido;
        }
    }
}