package com.project.board0905.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenStore {

    private final StringRedisTemplate redis;

    private String rtKey(String username) { return "auth:rt:" + username; }
    private String blKey(String token)    { return "auth:bl:" + token; }

    public void saveRefresh(String username, String refreshToken, long ttlDays) {
        redis.opsForValue().set(rtKey(username), refreshToken, Duration.ofDays(ttlDays));
    }
    public Optional<String> getRefresh(String username) {
        return Optional.ofNullable(redis.opsForValue().get(rtKey(username)));
    }
    public void deleteRefresh(String username) { redis.delete(rtKey(username)); }

    public void blacklist(String accessToken, long ttlSeconds) {
        redis.opsForValue().set(blKey(accessToken), "1", Duration.ofSeconds(ttlSeconds));
    }
    public boolean isBlacklisted(String accessToken) {
        return redis.hasKey(blKey(accessToken));
    }
}
