package com.jirapat.personalfinance.api.service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.jirapat.personalfinance.api.entity.RefreshToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private static final String TOKEN_PREFIX = "refresh_token:";
    private static final String USER_TOKENS_PREFIX = "user_refresh_tokens:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpirationMs;

    public String createRefreshToken(Long userId) {
        String token = UUID.randomUUID().toString();
        long ttlSeconds = refreshExpirationMs / 1000;

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userId(userId)
                .ttlSeconds(ttlSeconds)
                .build();

        // Store token -> RefreshToken with TTL
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, refreshToken, ttlSeconds, TimeUnit.SECONDS);

        // Add to user's token set (for logout/revoke-all)
        String userKey = USER_TOKENS_PREFIX + userId;
        redisTemplate.opsForSet().add(userKey, token);
        redisTemplate.expire(userKey, ttlSeconds, TimeUnit.SECONDS);

        log.debug("Refresh token created for userId: {}", userId);
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        Object value = redisTemplate.opsForValue().get(tokenKey);
        if (value instanceof RefreshToken rt) {
            return Optional.of(rt);
        }
        return Optional.empty();
    }

    public void revokeToken(String token, Long userId) {
        redisTemplate.delete(TOKEN_PREFIX + token);
        redisTemplate.opsForSet().remove(USER_TOKENS_PREFIX + userId, token);
        log.debug("Refresh token revoked for userId: {}", userId);
    }

    public void revokeAllTokens(Long userId) {
        String userKey = USER_TOKENS_PREFIX + userId;
        Set<Object> tokens = redisTemplate.opsForSet().members(userKey);

        if (tokens != null && !tokens.isEmpty()) {
            for (Object token : tokens) {
                redisTemplate.delete(TOKEN_PREFIX + token);
            }
        }

        redisTemplate.delete(userKey);
        log.debug("All refresh tokens revoked for userId: {}", userId);
    }
}
