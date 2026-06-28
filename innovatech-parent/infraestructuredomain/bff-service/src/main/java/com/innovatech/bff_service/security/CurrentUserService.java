package com.innovatech.bff_service.security;

import com.innovatech.bff_service.client.EquipoClient;
import com.innovatech.bff_service.dto.MiembroEquipoResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Service
public class CurrentUserService {

    private final String secret;
    private final EquipoClient equipoClient;

    public CurrentUserService(
            @Value("${innovatech.security.jwt.secret:${JWT_SECRET:innovatech_super_secret_key_2026_para_jwt_seguro_123456789}}") String secret,
            EquipoClient equipoClient
    ) {
        this.secret = secret;
        this.equipoClient = equipoClient;
    }

    public CurrentUser getCurrentUser() {
        String token = resolveBearerToken();

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String email = claims.getSubject();

        MiembroEquipoResponse miembro = equipoClient.listarMiembros()
                .stream()
                .filter(item -> item != null && item.getEmail() != null)
                .filter(item -> email.equalsIgnoreCase(item.getEmail()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no registrado"
                ));

        List<String> rolesToken = extractRoles(claims);

        String rolFinal = rolesToken.isEmpty()
                ? miembro.getRol()
                : rolesToken.get(0);

        rolFinal = normalizarRol(rolFinal);

        if (rolFinal == null || rolFinal.isBlank()) {
            rolFinal = normalizarRol(miembro.getRol());
        }

        return new CurrentUser(
                miembro.getId(),
                miembro.getEmail(),
                rolFinal,
                buildDisplayName(miembro)
        );
    }

    private String resolveBearerToken() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "No hay contexto de autenticacion"
            );
        }

        HttpServletRequest request = attributes.getRequest();
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token JWT requerido"
            );
        }

        return authorizationHeader.substring(7);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Claims claims) {
        Object roles = claims.get("roles");

        if (roles instanceof List<?>) {
            return ((List<?>) roles)
                    .stream()
                    .map(String::valueOf)
                    .map(this::normalizarRol)
                    .filter(rol -> rol != null && !rol.isBlank())
                    .toList();
        }

        String role = claims.get("role", String.class);

        if (role == null || role.isBlank()) {
            return List.of();
        }

        return List.of(normalizarRol(role));
    }

    private String normalizarRol(String rol) {
        if (rol == null) {
            return "";
        }

        String rolNormalizado = rol.trim().toUpperCase();

        if (rolNormalizado.startsWith("ROLE_")) {
            rolNormalizado = rolNormalizado.substring(5);
        }

        return rolNormalizado;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private String buildDisplayName(MiembroEquipoResponse miembro) {
        return String.join(
                " ",
                List.of(
                        valueOrEmpty(miembro.getNombres()),
                        valueOrEmpty(miembro.getApellidoPaterno())
                )
        ).trim();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    public record CurrentUser(
            Long id,
            String email,
            String role,
            String displayName
    ) {
        public boolean isAdmin() {
            return "ADMIN".equalsIgnoreCase(role);
        }

        public boolean canManageProjectWork() {
            return isAdmin()
                    || "PROJECT_MANAGER".equalsIgnoreCase(role)
                    || "SCRUM_MASTER".equalsIgnoreCase(role);
        }
    }
}