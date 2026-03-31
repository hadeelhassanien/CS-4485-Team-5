package com.example.demo;

// backend/src/main/java/com/example/demo/WebConfig.java
// (adjust the package name to match your project's package structure)


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")  // your Vite dev server
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}