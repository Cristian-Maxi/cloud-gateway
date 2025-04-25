package com.api.gateway.config;

import com.api.gateway.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class GateWayConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public RouteLocator customRouter(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route("user-auth", r -> r.path("/api/auth/**")
                        .uri("lb://user"))
                .route("user", r -> r.path("/api/user/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://user"))
                .route("user-docs", r -> r.path("/user/v3/api-docs")
                        .uri("lb://user"))

                .route("point-of-sale", r -> r.path("/api/pointOfSale/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://pointsalecost"))
                .route("cost", r -> r.path("/api/cost/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://pointsalecost"))
                .route("point-of-sale-docs", r -> r.path("/pointsalecost/v3/api-docs")
                        .uri("lb://pointsalecost"))

                .route("accreditations", r -> r.path("/api/accreditations/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://accreditations"))
                .route("accreditations-docs", r -> r.path("/accreditations/v3/api-docs")
                        .uri("lb://accreditations"))

                .build();
    }
}