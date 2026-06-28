package com.innovatech.api_gateway.facade;

import com.innovatech.api_gateway.dto.LoginRequest;
import com.innovatech.api_gateway.dto.LoginResponse;
import com.innovatech.api_gateway.dto.MiembroAuthResponse;
import com.innovatech.api_gateway.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthFacade {

    private final JwtService jwtService;
    private final RestClient equiposRestClient;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        String email = request.getUsername();
        String password = request.getPassword();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email y contraseña son obligatorios");
        }

        MiembroAuthResponse miembro;

        try {
            miembro = equiposRestClient
                    .get()
                    .uri("/api/v1/equipos/miembros/auth/{email}", email)
                    .retrieve()
                    .body(MiembroAuthResponse.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        if (miembro == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        if (!"ACTIVO".equalsIgnoreCase(miembro.estado())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario inactivo");
        }

        if (!passwordEncoder.matches(password, miembro.passwordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        List<String> roles = List.of(miembro.rol());

        String token = jwtService.generateToken(miembro.email(), roles);

        return new LoginResponse(
                "Bearer",
                token,
                86400,
                miembro.email(),
                roles
        );
    }
}