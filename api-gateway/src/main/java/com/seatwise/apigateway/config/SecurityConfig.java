package com.seatwise.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges ->
                        exchanges.pathMatchers("/api/v1/auth/login",
                                        "/api/v1/auth/admin/register",
                                        "/api/v1/auth/register",
//                                        swagger
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/webjars/**",
                                        "/user-service/api-docs/**",
                                        "/event-service/api-docs/**",
                                        "/file-service/api-docs/**",
                                        "/booking-service/api-docs/**",
                                        "/notification-service/api-docs/**").
                                permitAll()
                                .pathMatchers(HttpMethod.POST, "/api/v1/events").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/api/v1/events/").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/api/v1/events/").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.GET,"/api/v1/events").permitAll()
                                .pathMatchers(HttpMethod.GET,"/api/v1/events/**").permitAll()
                                .anyExchange().authenticated())
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
