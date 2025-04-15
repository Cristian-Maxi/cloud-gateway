package com.api.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class GateWayConfig {

    @Bean
    public RouteLocator customRouter(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route("pointOfSale-service", r -> r.path("/api/pointOfSale/**")
                        .uri("lb://pointsalecost"))
                .route("pointOfSale-service", r -> r.path("/api/cost/**")
                        .uri("lb://pointsalecost"))
                .route("accreditations-service", r -> r.path("/api/accreditations/**")
                        .uri("lb://accreditations"))
                .build();
    }
}