package com.innovatech.api_gateway.controller;

import com.innovatech.api_gateway.dto.AuthResponse;
import com.innovatech.api_gateway.dto.LoginRequest;
import com.innovatech.api_gateway.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        if ("admin".equals(request.getUsername()) && "admin123".equals(request.getPassword())) {
            String token = jwtUtil.generarToken(request.getUsername(), "ADMIN");
            return new AuthResponse(token, "Bearer", request.getUsername(), "ADMIN");
        }

        if ("viewer".equals(request.getUsername()) && "viewer123".equals(request.getPassword())) {
            String token = jwtUtil.generarToken(request.getUsername(), "VIEWER");
            return new AuthResponse(token, "Bearer", request.getUsername(), "VIEWER");
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
    }
}