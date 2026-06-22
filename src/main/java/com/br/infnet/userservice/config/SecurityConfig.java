package com.br.infnet.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("!test")
public class SecurityConfig {

    // ============================================================
    // FILTRO PÚBLICO
    // ============================================================
    @Bean
    @Order(1)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) {
        http
                .securityMatcher(
                        // Health checks
                        "/actuator/**",
                        "/health",
                        "/test/**",

                        // Endpoints públicos
                        "/usuarios/novo",
                        "/usuarios/listar-usernames",
                        "/usuarios/{id}/perfil",
                        "/usuarios/{id}/seller-info",

                        // Endpoints internos
                        "/usuarios/status",
                        "/usuarios/{id}"
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    // ============================================================
    // FILTRO PRIVADO
    // ============================================================
    @Bean
    @Order(2)
    public SecurityFilterChain privateFilterChain(HttpSecurity http) {
        http
                .securityMatcher("/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/usuarios/me",
                                "/usuarios/listar-pfps",
                                "/usuarios/trocar-pfp",
                                "/usuarios/deletar/{id}"
                        ).authenticated()

                        //Fallback
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}