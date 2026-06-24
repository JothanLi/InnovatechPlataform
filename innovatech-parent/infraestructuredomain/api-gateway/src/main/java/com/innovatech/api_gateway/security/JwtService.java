package com.innovatech.api_gateway.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final String secret;
    private final long expirationSeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${innovatech.security.jwt.secret}") String secret,
            @Value("${innovatech.security.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expirationSeconds = expirationMinutes * 60;
    }

    public String generateToken(String username, List<String> roles) {
        Instant now = Instant.now();

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", username);
        claims.put("roles", roles);
        claims.put("iat", now.getEpochSecond());
        claims.put("exp", now.plusSeconds(expirationSeconds).getEpochSecond());

        String unsignedToken = encodeJson(header) + "." + encodeJson(claims);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public JwtPayload validate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Token JWT inválido");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);

        if (!constantTimeEquals(expectedSignature, parts[2])) {
            throw new IllegalArgumentException("Firma JWT inválida");
        }

        Map<String, Object> claims = decodeJson(parts[1]);
        long expiration = ((Number) claims.get("exp")).longValue();

        if (Instant.now().getEpochSecond() >= expiration) {
            throw new IllegalArgumentException("Token JWT expirado");
        }

        String username = String.valueOf(claims.get("sub"));
        List<String> roles = ((List<?>) claims.get("roles"))
                .stream()
                .map(String::valueOf)
                .toList();

        return new JwtPayload(username, roles);
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(value);
            return base64UrlEncode(json);
        } catch (Exception exception) {
            throw new IllegalStateException("No fue posible generar el JWT", exception);
        }
    }

    private Map<String, Object> decodeJson(String value) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(value);
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new IllegalArgumentException("Token JWT inválido", exception);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return base64UrlEncode(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("No fue posible firmar el JWT", exception);
        }
    }

    private String base64UrlEncode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private boolean constantTimeEquals(String first, String second) {
        return MessageDigestLikeEquals.equals(first.getBytes(StandardCharsets.UTF_8), second.getBytes(StandardCharsets.UTF_8));
    }

    public record JwtPayload(String username, List<String> roles) {
    }
}
