package com.innovatech.tareas_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.innovatech.tareas_service.repository")
@EnableJpaAuditing
public class DataSourceConfig {
}
