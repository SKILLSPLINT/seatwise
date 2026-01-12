package com.seatwise.user_service.Utils;

import enums.ERole;
import exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtils {

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public static  String generateToken(UUID userId, ERole role, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId.toString());
        return generateRefreshToken(claims, email);
    }

    public static String generateRefreshToken(Map<String, Object> extraClaims, String email) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 604800000))
                .signWith(getSigningKey())
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("Please provide valid token: " + e.getMessage());
        }
    }

    private static SecretKey getSigningKey() {
        byte[] key = Decoders.BASE64.decode("3D41112DCF3F434B58C192E379DF13D41112DCF3F434B58C192E379DF1");
        return Keys.hmacShaKeyFor(key);
    }

    /**
     * Validates token structure, signature, and expiration
     *
     * @param token JWT token to validate
     * @return true if the token is valid (not expired and properly signed)
     */
    public boolean validateToken(String token) {
        try {
            // This will throw exception if token is invalid or improperly signed
            extractAllClaims(token);
            // Check if the token is expired
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates token and checks if it matches the given username
     *
     * @param token    JWT token to validate
     * @param username Username to match against a token subject
     * @return true if the token is valid and matches the username
     */
    public boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = extractUserName(token);
            return (Objects.equals(tokenUsername, username) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        final Date expirationDate = extractClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

}
