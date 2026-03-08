package com.rafaellima.hojeafestaenossa.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a configuração a todos os endpoints
                .allowedOrigins("*") // Permite requisições de qualquer origem (ideal para desenvolvimento)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "TRACE", "CONNECT") // Métodos HTTP
                                                                                                       // permitidos
                .allowedHeaders("*"); // Permite todos os headers na requisição
    }
}
