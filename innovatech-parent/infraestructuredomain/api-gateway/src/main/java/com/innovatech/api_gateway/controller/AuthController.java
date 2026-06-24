package com.innovatech.api_gateway.controller;

import com.innovatech.api_gateway.dto.LoginRequest;
import com.innovatech.api_gateway.dto.LoginResponse;
import com.innovatech.api_gateway.facade.AuthFacade;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthFacade authFacade;

    public AuthController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authFacade.login(request);
    }
}
