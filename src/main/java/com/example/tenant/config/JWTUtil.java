package com.example.tenant.config;

import com.example.tenant.entity.master.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JWTUtil implements Serializable {

    @Serial
    private static final long serialVersionUID = -2550185165626007488L;


    @Value("${app.security.jwt.expiration}")
    private long expiration;

    @Value("${app.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${app.jwt-secret}")
    private String secret;

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Boolean ignoreTokenExpiration(String token) {
        // here you specify tokens, for that the expiration is ignored
        return false;
    }

    public String generateAccessToken(AppUser userDetails, int level) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("level",level);
        return buildToken(claims, userDetails.getUsername(), expiration);
    }

    public String generateRefreshToken(AppUser userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return buildToken(claims, userDetails.getUsername(), refreshTokenExpiration);
    }

    private String buildToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis()  + expiration))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }

    public Boolean canTokenBeRefreshed(String token) {
        return (!isTokenExpired(token) || ignoreTokenExpiration(token));
    }

    public Boolean validateToken(String token, AppUser userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public int getLevelFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody();

        Integer level = claims.get("level", Integer.class);
        return level != null ? level : 1;
    }
}
