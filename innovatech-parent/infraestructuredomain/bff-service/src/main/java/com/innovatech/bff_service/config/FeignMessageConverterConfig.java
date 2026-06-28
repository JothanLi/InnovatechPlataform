package com.innovatech.bff_service.config;

import org.springframework.boot.http.converter.autoconfigure.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.List;

@Configuration
public class FeignMessageConverterConfig {

    @Bean
    public HttpMessageConverters httpMessageConverters() {
        return new HttpMessageConverters(List.of(
                new ByteArrayHttpMessageConverter(),
                new StringHttpMessageConverter(),
                new FormHttpMessageConverter(),
                new MappingJackson2HttpMessageConverter()
        ));
    }
}
