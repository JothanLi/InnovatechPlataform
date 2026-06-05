package com.innovatech.tareas_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TareasServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TareasServiceApplication.class, args);
	}
}