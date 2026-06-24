package com.innovatech.api_gateway.dto;

import java.util.List;

public record LoginResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        String username,
        List<String> roles
) {
}
