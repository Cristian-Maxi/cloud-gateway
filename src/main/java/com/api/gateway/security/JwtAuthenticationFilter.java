package com.api.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GatewayFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtil;

    public JwtAuthenticationFilter(JwtUtils jwtUtils){
        this.jwtUtil = jwtUtils;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString(); // Revisar
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        logger.info("Token with Bearer: " + token);
        if(!exchange.getRequest().getPath().toString().startsWith("/api/auth")){
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                logger.info("Token: " + token);
                try {
                    if (jwtUtil.validateToken(token)) {
                        String username = jwtUtil.getUsernameFromToken(token);
                        logger.info("Username: " + username);
                        List<String> roles = jwtUtil.getAuthoritiesFromToken(token);
                        logger.info("Roles: " + roles);
                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header("X-User-Name", username)
                                .header("X-User-Authorities", String.join(",", roles))
                                .build();
                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    } else {
                        return onError(exchange, "Invalid JWT Token", HttpStatus.UNAUTHORIZED);
                    }
                } catch (Exception e) {
                    return onError(exchange, "JWT Token validation failed", HttpStatus.UNAUTHORIZED);
                }
            } else {
                return onError(exchange, "Authorization header is missing or invalid", HttpStatus.UNAUTHORIZED);
            }
        }
        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorJson = String.format("{\"error\": \"%s\"}", err);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}