package com.liverpool.service.impl;

import com.liverpool.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Optional<String> get(String key) {
        try {
            String jsonValue = redisTemplate.opsForValue().get(key);
            if (jsonValue == null) {
                log.debug("Key '{}' not found in cache", key);
                return Optional.empty();
            }

            log.debug("Successfully retrieved value for key '{}'", key);
            return Optional.of(jsonValue);

        } catch (Exception e) {
            log.error("Error retrieving value for key '{}': {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, String jsonValue) {
        try {
            redisTemplate.opsForValue().set(key, jsonValue);
            log.debug("Successfully stored value for key '{}'", key);
        } catch (Exception e) {
            log.error("Error storing value for key '{}': {}", key, e.getMessage());
            throw new RuntimeException("Failed to store value in cache", e);
        }
    }

    @Override
    public void put(String key, String jsonValue, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, jsonValue, ttl);
            log.debug("Successfully stored value for key '{}' with TTL {}", key, ttl);
        } catch (Exception e) {
            log.error("Error storing value for key '{}' with TTL: {}", key, e.getMessage());
            throw new RuntimeException("Failed to store value in cache", e);
        }
    }

    @Override
    public boolean update(String key, String jsonValue) {
        try {
            // Check if key exists first
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                log.debug("Key '{}' does not exist, cannot update", key);
                return false;
            }

            // Get current TTL to preserve it
            Long ttlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);

            if (ttlSeconds != null && ttlSeconds > 0) {
                // Preserve existing TTL
                redisTemplate.opsForValue().set(key, jsonValue, Duration.ofSeconds(ttlSeconds));
            } else {
                // No TTL or persistent key
                redisTemplate.opsForValue().set(key, jsonValue);
            }

            log.debug("Successfully updated value for key '{}'", key);
            return true;

        } catch (Exception e) {
            log.error("Error updating value for key '{}': {}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(String key, String jsonValue, Duration ttl) {
        try {
            // Check if key exists first
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                log.debug("Key '{}' does not exist, cannot update", key);
                return false;
            }

            redisTemplate.opsForValue().set(key, jsonValue, ttl);
            log.debug("Successfully updated value for key '{}' with new TTL {}", key, ttl);
            return true;

        } catch (Exception e) {
            log.error("Error updating value for key '{}' with TTL: {}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            boolean result = Boolean.TRUE.equals(deleted);
            log.debug("Delete operation for key '{}': {}", key, result ? "successful" : "key not found");
            return result;
        } catch (Exception e) {
            log.error("Error deleting key '{}': {}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateTtl(String key, Duration ttl) {
        try {
            Boolean result = redisTemplate.expire(key, ttl);
            boolean success = Boolean.TRUE.equals(result);
            log.debug("TTL update for key '{}': {}", key, success ? "successful" : "key not found");
            return success;
        } catch (Exception e) {
            log.error("Error updating TTL for key '{}': {}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Error checking existence of key '{}': {}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<Duration> getTtl(String key) {
        try {
            Long ttlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttlSeconds == null || ttlSeconds < 0) {
                // Key doesn't exist or has no expiration
                return Optional.empty();
            }
            return Optional.of(Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.error("Error getting TTL for key '{}': {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Iterable<String> listAllKeys() {
        try {
            // Use Redis pattern to match all keys
            return redisTemplate.keys("*");
        } catch (Exception e) {
            log.error("Error listing all keys: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
}
