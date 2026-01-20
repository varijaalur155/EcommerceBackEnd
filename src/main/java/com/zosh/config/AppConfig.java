package com.zosh.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class AppConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // ✅ STATELESS JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ✅ AUTHORIZATION RULES
            .authorizeHttpRequests(auth -> auth
                // PUBLIC (NO JWT)
                .requestMatchers(
                        "/auth/**",
                        "/sellers/**",
                        "/login",
                        "/register"
                ).permitAll()

                // PROTECTED
                .requestMatchers("/api/**").authenticated()

                .anyRequest().permitAll()
            )

            // ✅ JWT FILTER (SAFE)
            .addFilterBefore(jwtTokenValidator(), BasicAuthenticationFilter.class)

            // ✅ SECURITY BASICS
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    /**
     * ✅ Register JWT filter as a bean
     * Allows Spring to manage lifecycle
     */
    @Bean
    public JwtTokenValidator jwtTokenValidator() {
        return new JwtTokenValidator();
    }

    // ✅ CORS
    private CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration cfg = new CorsConfiguration();
            cfg.setAllowedOrigins(Arrays.asList(
                    "https://varijashoppingstore.vercel.app",
                    "http://localhost:3000"
            ));
            cfg.setAllowedMethods(Collections.singletonList("*"));
            cfg.setAllowedHeaders(Collections.singletonList("*"));
            cfg.setExposedHeaders(Collections.singletonList("Authorization"));
            cfg.setAllowCredentials(true);
            cfg.setMaxAge(3600L);
            return cfg;
        };
    }

    // ✅ PASSWORD ENCODER
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ REST TEMPLATE (BREVO)
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
