package com.innovatech.api_gateway.controller;

import com.innovatech.api_gateway.dto.LoginRequest;
import com.innovatech.api_gateway.dto.LoginResponse;
import com.innovatech.api_gateway.facade.AuthFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthFacade authFacade;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authFacade.login(request);
    }
}