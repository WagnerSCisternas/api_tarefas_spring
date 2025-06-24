package com.wasc.tarefa.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {	

    // Chave secreta para assinar e verificar o JWT. Mantenha esta chave SEGURA!
    // Gerada aleatoriamente para este exemplo. Em produção, use uma chave forte e gerenciada de forma segura.
    
	@Value("${jwt.secret:UmaChaveSecretaMuitoForteParaAssinarSeusTokensJWTQueNinguemVaiAdivinharERUmMinimoDeTrintaEDoisBytes}")
	private String SECRET_KEY;
	
    //	Tempo de expiração do token em milissegundos \(ex\: 10 horas\)
	@Value("${jwt.expiration:36000000}")// 10 horas	
    private long EXPIRATION_TIME;

    // Pega a chave de assinatura
    private Key getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extrai o username do token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    } 

    // Extrai a data de expiração do token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extrai um claim específico do token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extrai todos os claims do token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    // Verifica se o token expirou
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Gera o token para um UserDetails
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Você pode adicionar claims personalizados aqui, se precisar
        return createToken(claims, userDetails.getUsername());
    }

    // Cria o token JWT
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Expira em EXPIRATION_TIME
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Valida o token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}