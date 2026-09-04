package com.apishield.apishield.service;

import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
        private  String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String email,String role){


        return  Jwts.builder()
                .subject(email)//Stores the user's email inside the JWT:
                .claim("role",role)
                .issuedAt(new Date())//Stores when the token was created.
                .expiration(new Date(System.currentTimeMillis()+expiration))//Sets when the token expires.
                .signWith(getSigningKey())//Signs the JWT using our secret key.
                .compact();//Creates the final JWT string:
    }
    public String extractEmail(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }
    public String extractRole(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }


}

