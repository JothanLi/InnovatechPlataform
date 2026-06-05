package com.innovatech.bff_service.exception;

import com.innovatech.bff_service.dto.ErrorResponse;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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