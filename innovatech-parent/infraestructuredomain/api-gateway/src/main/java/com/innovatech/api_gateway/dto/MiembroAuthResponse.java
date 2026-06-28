package com.innovatech.api_gateway.dto;

public record MiembroAuthResponse(
        Long id,
        String email,
        String passwordHash,
        String rol,
        String estado
) {
}