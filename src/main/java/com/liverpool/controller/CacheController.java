package com.liverpool.controller;

import com.liverpool.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheService cacheService;

    @GetMapping("/{key}")
    public ResponseEntity<String> getValue(@PathVariable String key) {
        Optional<String> value = cacheService.get(key);

        if (value.isPresent()) {
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(value.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{key}")
    public ResponseEntity<String> putValue(@PathVariable String key,
                                          @RequestBody String jsonValue,
                                          @RequestParam(required = false) Long ttlSeconds) {
        try {
            if (ttlSeconds != null && ttlSeconds > 0) {
                cacheService.put(key, jsonValue, Duration.ofSeconds(ttlSeconds));
            } else {
                cacheService.put(key, jsonValue);
            }
            return ResponseEntity.ok("Value stored successfully");
        } catch (Exception e) {
            log.error("Error storing value: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to store value");
        }
    }

    @PutMapping("/{key}")
    public ResponseEntity<String> updateValue(@PathVariable String key,
                                             @RequestBody String jsonValue,
                                             @RequestParam(required = false) Long ttlSeconds) {
        boolean success;
        if (ttlSeconds != null && ttlSeconds > 0) {
            success = cacheService.update(key, jsonValue, Duration.ofSeconds(ttlSeconds));
        } else {
            success = cacheService.update(key, jsonValue);
        }

        if (success) {
            return ResponseEntity.ok("Value updated successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<String> deleteValue(@PathVariable String key) {
        boolean deleted = cacheService.delete(key);
        if (deleted) {
            return ResponseEntity.ok("Value deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{key}/ttl")
    public ResponseEntity<String> updateTtl(@PathVariable String key,
                                           @RequestParam long ttlSeconds) {
        boolean success = cacheService.updateTtl(key, Duration.ofSeconds(ttlSeconds));
        if (success) {
            return ResponseEntity.ok("TTL updated successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{key}/exists")
    public ResponseEntity<Map<String, Boolean>> checkExists(@PathVariable String key) {
        boolean exists = cacheService.exists(key);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/{key}/ttl")
    public ResponseEntity<Map<String, Object>> getTtl(@PathVariable String key) {
        Optional<Duration> ttl = cacheService.getTtl(key);
        if (ttl.isPresent()) {
            return ResponseEntity.ok(Map.of(
                "ttl", ttl.get().getSeconds(),
                "exists", true
            ));
        } else {
            return ResponseEntity.ok(Map.of("exists", false));
        }
    }

    @GetMapping("/keys")
    public ResponseEntity<List<String>> getAllKeys() {
        try {
            Iterable<String> keys = cacheService.listAllKeys();
            List<String> keyList = StreamSupport.stream(keys.spliterator(), false)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(keyList);
        } catch (Exception e) {
            log.error("Error retrieving all keys: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
