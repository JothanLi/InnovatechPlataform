package com.innovatech.api_gateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private final int requestsPerMinute;
    private final Map<String, RequestWindow> windows = new ConcurrentHashMap<>();

    public RateLimitingFilter(
            @Value("${innovatech.security.ratelimit.requests-per-minute}") int requestsPerMinute
    ) {
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String clientIp = resolveClientIp(request);
        long currentMinute = Instant.now().getEpochSecond() / 60;
        RequestWindow window = windows.compute(clientIp, (ip, existingWindow) -> {
            if (existingWindow == null || existingWindow.minute != currentMinute) {
                return new RequestWindow(currentMinute);
            }
            return existingWindow;
        });

        if (window.counter.incrementAndGet() > requestsPerMinute) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Límite de peticiones excedido\",\"mensaje\":\"Intente nuevamente más tarde\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RequestWindow {
        private final long minute;
        private final AtomicInteger counter = new AtomicInteger(0);

        private RequestWindow(long minute) {
            this.minute = minute;
        }
    }
}
