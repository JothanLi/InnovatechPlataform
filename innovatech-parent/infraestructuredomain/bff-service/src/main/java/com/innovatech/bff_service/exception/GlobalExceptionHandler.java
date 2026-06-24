package com.innovatech.bff_service.exception;

import com.innovatech.bff_service.dto.ErrorResponse;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.NotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse manejarNotFound(
            FeignException.NotFound exception,
            HttpServletRequest request
    ) {
        return ErrorResponse.builder()
                .fecha(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Recurso no encontrado")
                .mensaje("El recurso solicitado no existe en alguno de los microservicios.")
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(FeignException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse manejarErrorMicroservicio(
            FeignException exception,
            HttpServletRequest request
    ) {
        return ErrorResponse.builder()
                .fecha(LocalDateTime.now())
                .status(HttpStatus.BAD_GATEWAY.value())
                .error("Error de comunicación con microservicio")
                .mensaje("No fue posible obtener respuesta desde uno de los microservicios.")
                .path(request.getRequestURI())
                .build();
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
}
