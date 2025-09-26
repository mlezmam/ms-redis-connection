package com.liverpool.service;

import java.time.Duration;
import java.util.Optional;

public interface CacheService {

    /**
     * Read operation - Get JSON value by key
     */
    Optional<String> get(String key);

    /**
     * Write operation - Store JSON string
     */
    void put(String key, String jsonValue);

    /**
     * Write operation - Store JSON string with TTL
     */
    void put(String key, String jsonValue, Duration ttl);

    /**
     * Update operation - Update existing key with new JSON value (keeps existing TTL if any)
     */
    boolean update(String key, String jsonValue);

    /**
     * Update operation - Update existing key with new JSON value and TTL
     */
    boolean update(String key, String jsonValue, Duration ttl);

    /**
     * Delete operation - Remove key from cache
     */
    boolean delete(String key);

    /**
     * Update TTL operation - Update TTL for existing key
     */
    boolean updateTtl(String key, Duration ttl);

    /**
     * Check if key exists
     */
    boolean exists(String key);

    /**
     * Get remaining TTL for a key
     */
    Optional<Duration> getTtl(String key);

    /**
     * List all keys in the cache
     */
    Iterable<String> listAllKeys();
}
