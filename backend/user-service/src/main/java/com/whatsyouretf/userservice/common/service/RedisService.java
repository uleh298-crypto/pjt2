package com.whatsyouretf.userservice.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PENDING_SIGNUP_PREFIX = "pending_signup:";

    public void savePendingSignup(String email, Object data, long ttlMinutes) {
        String key = PENDING_SIGNUP_PREFIX + email;
        redisTemplate.opsForValue().set(key, data, ttlMinutes, TimeUnit.MINUTES);
        log.debug("Saved pending signup for email: {}", email);
    }

    public <T> Optional<T> getPendingSignup(String email, Class<T> clazz) {
        String key = PENDING_SIGNUP_PREFIX + email;
        Object data = redisTemplate.opsForValue().get(key);
        if (data == null) {
            return Optional.empty();
        }
        try {
            T result = objectMapper.convertValue(data, clazz);
            return Optional.of(result);
        } catch (Exception e) {
            log.error("Failed to deserialize pending signup for email: {}", email, e);
            return Optional.empty();
        }
    }

    public void deletePendingSignup(String email) {
        String key = PENDING_SIGNUP_PREFIX + email;
        redisTemplate.delete(key);
        log.debug("Deleted pending signup for email: {}", email);
    }

    public boolean hasPendingSignup(String email) {
        String key = PENDING_SIGNUP_PREFIX + email;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
