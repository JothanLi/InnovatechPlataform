package com.innovatech.tareas_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Manejo de excepciones cuando una tarea no es encontrada
     */
    @ExceptionHandler(TareaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTareaNotFoundException(
            TareaNotFoundException ex,
            WebRequest request) {
        log.error("Error: Tarea no encontrada - {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Tarea No Encontrada")
                .mensaje(ex.getMessage())
                .ruta(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Manejo de excepciones de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorValidationResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        log.error("Error de validación: {}", ex.getMessage());

        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String nombreCampo = ((FieldError) error).getField();
            String mensajeError = error.getDefaultMessage();
            errores.put(nombreCampo, mensajeError);
        });

        ErrorValidationResponse errorResponse = ErrorValidationResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Error de Validación")
                .mensaje("Errores en los campos enviados")
                .errores(errores)
                .ruta(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Manejo de excepciones genéricas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error Interno del Servidor")
                .mensaje("Ocurrió un error inesperado")
                .ruta(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
