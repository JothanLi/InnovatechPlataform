package com.innovatech.api_gateway.config;

import com.innovatech.api_gateway.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/actuator/health",
                                "/actuator/info",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/**")
                        .hasAnyRole("PROJECT_MANAGER", "SCRUM_MASTER", "DEVELOPER", "QA", "DEVOPS", "UI_UX")
                        .requestMatchers(HttpMethod.POST, "/api/v1/bff/proyectos", "/api/v1/proyectos/**")
                        .hasRole("PROJECT_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/bff/proyectos/**", "/api/v1/proyectos/**")
                        .hasRole("PROJECT_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/bff/proyectos/**", "/api/v1/proyectos/**")
                        .hasRole("PROJECT_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/bff/tareas", "/api/v1/tareas/**")
                        .hasAnyRole("PROJECT_MANAGER", "SCRUM_MASTER", "DEVELOPER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/bff/tareas/**", "/api/v1/tareas/**")
                        .hasAnyRole("PROJECT_MANAGER", "SCRUM_MASTER", "DEVELOPER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/bff/tareas/**", "/api/v1/tareas/**")
                        .hasAnyRole("PROJECT_MANAGER", "SCRUM_MASTER", "DEVELOPER", "QA")
                        .requestMatchers(HttpMethod.POST, "/api/v1/bff/miembros", "/api/v1/bff/asignaciones", "/api/v1/equipos/**")
                        .hasAnyRole("PROJECT_MANAGER", "SCRUM_MASTER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/**")
                        .hasRole("PROJECT_MANAGER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
