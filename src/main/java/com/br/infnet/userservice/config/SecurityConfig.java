package com.br.infnet.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
                        // Públicos
                        .requestMatchers(
                                "/actuator/**",
                                "/health",
                                "/usuarios/novo",
                                "/usuarios/{id}/perfil",
                                "/usuarios/{id}/seller-info",
                                "/usuarios/listar-usernames",
                                "/usuarios/status",
                                "/usuarios/{id}"
                        ).permitAll()

                        //Privados
                        .requestMatchers(
                                "/usuarios/listar-pfps",
                                "/usuarios/trocar-pfp",
                                "/usuarios/deletar/{id}"
                        ).authenticated()

                        //Fallback
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}