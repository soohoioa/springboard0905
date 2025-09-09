package com.project.board0905.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtProperties props;

    public String createAccessToken(String username, String role) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.getAccessExpMin(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .issuer(props.getIssuer())
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("role", role)
                .signWith(props.key(), Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(String username) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.getRefreshExpDays(), ChronoUnit.DAYS);
        return Jwts.builder()
                .issuer(props.getIssuer())
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("typ", "refresh")
                .signWith(props.key(), Jwts.SIG.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith((SecretKey) props.key()).build().parseSignedClaims(token);
    }

    public boolean isExpired(String token) {
        try {
            return parse(token).getPayload().getExpiration().before(new Date());
        } catch (JwtException e) { return true; }
    }

    public String getUsername(String token) { return parse(token).getPayload().getSubject(); }
    public String getRole(String token) { return String.valueOf(parse(token).getPayload().get("role")); }
}
