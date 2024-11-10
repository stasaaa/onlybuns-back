package com.isa.onlybuns_back.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {

    private static final String SECRET_KEY = "YXR0ZW1wdHRoZXJlZm9yZWVhcnRoZGVjbGFyZWR0cm91Ymxlc3RyaXBiZWxvd2Rvb3I=";

    public String  extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Map<String, Object> extractClaims, UserDetails userDetails) {
        String email = userDetails.getUsername();

        return Jwts
                .builder()
                .setClaims(extractClaims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userEmail = extractUsername(token);
        //TODO za sledece kt - ovde se dodaje isTokenExpire
        return (userEmail.equals(userDetails.getUsername()));
    }

    private boolean isTokenExpired(String token) {
        return extractExparation(token).before(new Date());
    }

    private Date extractExparation(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
