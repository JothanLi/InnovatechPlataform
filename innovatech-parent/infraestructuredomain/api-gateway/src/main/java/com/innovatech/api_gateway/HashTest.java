package com.innovatech.api_gateway;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "Password123";
        String hashNuevo = encoder.encode(password);

        System.out.println("HASH NUEVO:");
        System.out.println(hashNuevo);

        System.out.println("VALIDACION:");
        System.out.println(encoder.matches(password, hashNuevo));
    }
}