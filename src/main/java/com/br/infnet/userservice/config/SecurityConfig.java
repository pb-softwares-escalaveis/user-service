package com.br.infnet.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("!test")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        // ========================================
                        // 1. ENDPOINTS PÚBLICOS
                        // ========================================
                        .requestMatchers(HttpMethod.POST, "/usuarios/novo").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuarios/{id}/perfil").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuarios/{id}/seller-info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuarios/listar-usernames").permitAll()
                        .requestMatchers("/actuator/**", "/health").permitAll()

                        // ========================================
                        // 2. ENDPOINTS INTERNOS (comunicação entre microsserviços)
                        // ========================================
                        .requestMatchers(HttpMethod.GET, "/usuarios/status").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuarios/{id}").permitAll()

                        // ========================================
                        // 3. ENDPOINTS PRIVADOS
                        // ========================================
                        .requestMatchers(HttpMethod.GET, "/usuarios/listar-pfps").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/usuarios/trocar-pfp").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/usuarios/{id}").authenticated()

                        //Fallback
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}