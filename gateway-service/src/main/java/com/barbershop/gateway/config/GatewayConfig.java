package com.barbershop.gateway.config;

import com.barbershop.gateway.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authenticationFilter;
    
    @Value("${auth.service.url}")
    private String authServiceUrl;
    
    @Value("${user.service.url}")
    private String userServiceUrl;
    
    @Value("${booking.service.url}")
    private String bookingServiceUrl;

    public GatewayConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes (public)
                .route("auth-register", r -> r
                        .path("/api/auth/register")
                        .uri(authServiceUrl))
                .route("auth-login", r -> r
                        .path("/api/auth/login")
                        .uri(authServiceUrl))
                .route("auth-verify", r -> r
                        .path("/api/auth/verify/**")
                        .uri(authServiceUrl))
                        .route("auth-resend", r -> r
                                .path("/api/auth/resend-verification")
                                .uri(authServiceUrl))
                
                // Auth Service Routes (protected)
                .route("auth-protected", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri(authServiceUrl))
                
                // User Service Routes (protected)
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri(userServiceUrl))
                
                // Booking Service Routes (public - view services)
                .route("booking-services-public", r -> r
                        .path("/api/services")
                        .uri(bookingServiceUrl))
                
                // Booking Service Routes (protected)
                .route("booking-service", r -> r
                        .path("/api/bookings/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri(bookingServiceUrl))
                
                // Barber Service Routes (public - GET /api/barbers for listing)
                // IMPORTANT: This must come BEFORE /api/barbers/** route
                .route("barbers-public-get", r -> r
                        .path("/api/barbers")
                        .and()
                        .method("GET")
                        .uri(bookingServiceUrl))
                
                // Barber Service Routes (protected - all other operations)
                // This catches POST, PUT, DELETE, and /api/barbers/admin/** routes
                // Note: /api/barbers (exact match) is handled above, this only catches /api/barbers/**
                .route("barber-service", r -> r
                        .path("/api/barbers/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri(bookingServiceUrl))
                
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:80"));
        corsConfig.setMaxAge(3600L);
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}


