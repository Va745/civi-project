package com.civicpulse.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.Date;
import java.util.Properties;

public class JWTUtil {
    private static String SECRET_KEY;
    private static long EXPIRATION_TIME;
    private static Key key;

    static {
        try {
            loadProperties();
            key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load JWT configuration", e);
        }
    }

    private static void loadProperties() throws IOException {
        Properties props = new Properties();
        InputStream input = JWTUtil.class.getClassLoader()
                .getResourceAsStream("config.properties");
        
        if (input == null) {
            throw new IOException("Unable to find config.properties");
        }
        
        props.load(input);
        SECRET_KEY = props.getProperty("jwt.secret");
        EXPIRATION_TIME = Long.parseLong(props.getProperty("jwt.expiration"));
        input.close();
    }

    /**
     * Generate JWT token for a user
     */
    public static String generateToken(int userId, String role, String name) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .claim("name", name)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Validate and parse JWT token
     */
    public static Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extract user ID from token
     */
    public static Integer getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims != null) {
            return Integer.parseInt(claims.getSubject());
        }
        return null;
    }

    /**
     * Extract role from token
     */
    public static String getRoleFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims != null) {
            return claims.get("role", String.class);
        }
        return null;
    }

    /**
     * Check if token is expired
     */
    public static boolean isTokenExpired(String token) {
        Claims claims = validateToken(token);
        if (claims != null) {
            return claims.getExpiration().before(new Date());
        }
        return true;
    }
}
