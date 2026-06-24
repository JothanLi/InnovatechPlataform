package com.innovatech.api_gateway.facade;

import com.innovatech.api_gateway.dto.LoginRequest;
import com.innovatech.api_gateway.dto.LoginResponse;
import com.innovatech.api_gateway.dto.MiembroAuthResponse;
import com.innovatech.api_gateway.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class AuthFacade {

    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final RestClient equiposRestClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthFacade(
            RestClient equiposRestClient,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.equiposRestClient = equiposRestClient;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        MiembroAuthResponse miembro = buscarMiembroPorEmail(request.username());

        if (!ESTADO_ACTIVO.equals(miembro.estado())) {
            throw new DisabledException("El usuario se encuentra inactivo");
        }

        if (!passwordEncoder.matches(request.password(), miembro.passwordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        List<String> roles = List.of(miembro.rol());

        return new LoginResponse(
                "Bearer",
                jwtService.generateToken(miembro.email(), roles),
                jwtService.getExpirationSeconds(),
                miembro.email(),
                roles
        );
    }

    private MiembroAuthResponse buscarMiembroPorEmail(String email) {
        try {
            return equiposRestClient.get()
                    .uri("/api/v1/equipos/miembros/auth/{email}", email)
                    .retrieve()
                    .body(MiembroAuthResponse.class);
        } catch (RestClientResponseException exception) {
            throw new BadCredentialsException("Credenciales inválidas", exception);
        }
    }
}
