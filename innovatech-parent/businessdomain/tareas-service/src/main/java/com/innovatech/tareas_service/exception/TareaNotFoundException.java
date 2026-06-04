package com.innovatech.tareas_service.exception;

public class TareaNotFoundException extends RuntimeException {

    public TareaNotFoundException(String mensaje) {
        super(mensaje);
    }

    public TareaNotFoundException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
