package com.project.board0905.auth.jwt;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter @Setter
public class JwtProperties {
    private String issuer;
    private String secret;
    private long accessExpMin;
    private long refreshExpDays;

    public SecretKey key() {
        // secret이 Base64 인코딩된 값이라면 아래 주석 사용:
        // return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
