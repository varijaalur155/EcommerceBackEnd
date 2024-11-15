package com.zosh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration  // This tells Spring that this is a configuration file
public class WebConfig {

    // Define a Bean to set up CORS configuration
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Allow all paths (/**) to handle cross-origin requests
                registry.addMapping("/**")
                        .allowedOrigins("*") // Replace with your Vercel frontend URL
                        .allowedMethods("GET", "POST", "PUT", "DELETE") // Add more methods if needed
                        .allowedHeaders("*") // Allow all headers, or specify if you need only certain headers
                        .allowCredentials(true) // Allow cookies and credentials for cross-origin requests
                        .maxAge(3600); // Cache the CORS response for 1 hour to reduce preflight requests
            }
        };
    }
}

