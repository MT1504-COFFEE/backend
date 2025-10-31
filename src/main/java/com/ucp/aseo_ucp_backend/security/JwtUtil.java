package com.ucp.aseo_ucp_backend.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.ucp.aseo_ucp_backend.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration.ms}")
    private long jwtExpirationInMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extrae el email (username) del token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extrae la fecha de expiración del token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extrae un claim específico usando una función
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extrae todos los claims del token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(getSigningKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }

    // Verifica si el token ha expirado
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Genera un token para un usuario
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        // Puedes añadir claims adicionales si los necesitas (ej. rol, nombre completo)
        claims.put("role", user.getRole().name());
        claims.put("fullName", user.getFullName());
        claims.put("userId", user.getId()); // Añadir ID de usuario
        return createToken(claims, user.getEmail());
    }
     // Sobrecarga para UserDetails (usado a menudo con Spring Security)
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Aquí podrías necesitar cargar el User completo para obtener rol/nombre si UserDetails no los tiene
        // O añadir los roles/authorities de UserDetails a los claims
         claims.put("role", userDetails.getAuthorities().stream().findFirst().orElseThrow().getAuthority()); // Asume una sola autoridad = rol
        return createToken(claims, userDetails.getUsername());
    }


    // Crea el token JWT
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                   .setClaims(claims)
                   .setSubject(subject) // Usamos el email como subject
                   .setIssuedAt(new Date(System.currentTimeMillis()))
                   .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                   .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                   .compact();
    }

    // Valida el token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
     // Valida solo el token (sin UserDetails)
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) { // Captura excepciones de JWT (expirado, malformado, etc.)
            return false;
        }
    }
}