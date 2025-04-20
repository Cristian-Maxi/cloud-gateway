package com.api.gateway.security;

import com.api.gateway.security.variablesEnv.SecretKeyConfig;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class JwtUtils {

    @Autowired
    private SecretKeyConfig secretKeyConfig;

    public String getUsernameFromToken(String token) {
        if (token == null) {
            throw new RuntimeException("Null Token");
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKeyConfig.getSECRET_KEY());
            DecodedJWT verifier = JWT.require(algorithm)
                    .withIssuer("Java-Shark")
                    .build()
                    .verify(token);
            return verifier.getSubject();
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Error verifying token: " + e.getMessage());
        }
    }

    public boolean validateToken(String token) {
        final String username = getUsernameFromToken(token);
        return (username != null && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Date getExpirationDateFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getExpiresAt();
    }

    public List<String> getAuthoritiesFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("authorities").asList(String.class);
    }
}
