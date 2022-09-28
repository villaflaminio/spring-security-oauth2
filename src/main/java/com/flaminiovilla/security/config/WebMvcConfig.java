package com.flaminiovilla.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The web MVC configuration class.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // Define the max age for CORS.
    private final long MAX_AGE_SECS = 3600;

    /**
     * Add CORS mapping to the registry.
     * @param registry the CORS registry we configure.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
        .allowedOrigins("http://localhost:3000", "http://localhost:8080", "*")
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(MAX_AGE_SECS);
    }
}
