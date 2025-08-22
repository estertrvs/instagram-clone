package br.edu.ifpb.instagram.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private final int jwtExpirationMs = 86400000; // 24 hours expiration time

    // ===== CHAVE CORRIGIDA (AGORA COM 64 CARACTERES) =====
    private static final String SECRET_KEY = "umaChaveMuitoSeguraDePeloMenos64CaracteresParaHS512JwtAlgoritmo!";

    private final Key jwtSecretKey;

    public JwtUtils() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {

        String username = authentication.getName();

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(this.jwtSecretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(this.jwtSecretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // A exceção de chave fraca não será mais lançada,
            // mas outras exceções (token expirado, malformado) serão capturadas aqui.
            System.err.println("Token inválido: " + e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(this.jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}