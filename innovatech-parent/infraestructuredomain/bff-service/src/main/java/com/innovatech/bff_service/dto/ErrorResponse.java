package com.innovatech.bff_service.dto;

import java.time.LocalDateTime;

public class ErrorResponse {

    private LocalDateTime fecha;
    private int status;
    private String error;
    private String mensaje;
    private String path;

    public ErrorResponse() {
    }

    public ErrorResponse(
            LocalDateTime fecha,
            int status,
            String error,
            String mensaje,
            String path
    ) {
        this.fecha = fecha;
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.path = path;
    }

    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public static class ErrorResponseBuilder {

        private LocalDateTime fecha;
        private int status;
        private String error;
        private String mensaje;
        private String path;

        public ErrorResponseBuilder fecha(LocalDateTime fecha) {
            this.fecha = fecha;
            return this;
        }

        public ErrorResponseBuilder status(int status) {
            this.status = status;
            return this;
        }

        public ErrorResponseBuilder error(String error) {
            this.error = error;
            return this;
        }

        public ErrorResponseBuilder mensaje(String mensaje) {
            this.mensaje = mensaje;
            return this;
        }

        public ErrorResponseBuilder path(String path) {
            this.path = path;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(
                    fecha,
                    status,
                    error,
                    mensaje,
                    path
            );
        }
    }
}