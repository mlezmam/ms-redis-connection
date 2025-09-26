# High-Performance Redis Cache Service

This project implements a high-performance Redis cache service using Spring Boot, Lettuce driver, and JSON serialization for Java 21.

## Features

- **High Performance**: Optimized Lettuce configuration with connection pooling
- **JSON Serialization**: Automatic object-to-JSON conversion using Jackson
- **TTL Support**: Optional Time-To-Live for cache entries
- **Type Safety**: Generic methods for type-safe operations
- **Comprehensive Operations**: Read, Write, Update, Delete, TTL management
- **Error Handling**: Robust error handling with logging
- **Spring Boot Integration**: Ready-to-use Spring components

## Cache Operations

### Read Operation
```java
// Get a cached object by key and type
SomeDto dto = cacheService.get("some-key", SomeDto.class).orElse(null);
```

### Write Operations
```java
// Store object without TTL (persistent)
cacheService.put("some-key", dto);

// Store object with TTL
cacheService.put("some-key", dto, Duration.ofMinutes(30));
```

### Update Operations
```java
// Update existing key (preserves existing TTL)
boolean updated = cacheService.update("some-key", newDto);

// Update existing key with new TTL
boolean updated = cacheService.update("some-key", newDto, Duration.ofHours(1));
```

### Delete Operation
```java
// Remove key from cache
boolean deleted = cacheService.delete("some-key");
```

### TTL Management
```java
// Update TTL for existing key
boolean updated = cacheService.updateTtl("some-key", Duration.ofMinutes(45));

// Get remaining TTL
Optional<Duration> ttl = cacheService.getTtl("some-key");
```

### Utility Operations
```java
// Check if key exists
boolean exists = cacheService.exists("some-key");
```

## Configuration

### Redis Configuration (application.yml)
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: ${REDIS_TIMEOUT:5000}
      lettuce:
        pool:
          max-active: ${REDIS_POOL_MAX_ACTIVE:25}
          max-idle: ${REDIS_POOL_MAX_IDLE:25}
          min-idle: ${REDIS_POOL_MIN_IDLE:10}
          max-wait: ${REDIS_POOL_MAX_WAIT:500}
```

### Performance Optimizations

1. **Lettuce Driver**: Async, reactive Redis client with connection pooling
2. **RESP3 Protocol**: Latest Redis protocol for better performance
3. **TCP No Delay**: Reduced network latency
4. **Keep Alive**: Persistent connections
5. **String Serialization**: Efficient key/value serialization
6. **Jackson Configuration**: Optimized JSON processing

## Usage Examples

### Basic String Caching
```java
@Service
public class UserService {
    
    @Autowired
    private CacheService cacheService;
    
    public String getUserName(String userId) {
        return cacheService.get("user:" + userId, String.class)
            .orElseGet(() -> {
                String name = fetchFromDatabase(userId);
                cacheService.put("user:" + userId, name, Duration.ofMinutes(15));
                return name;
            });
    }
}
```

### Complex Object Caching
```java
@Service
public class ProductService {
    
    @Autowired
    private CacheService cacheService;
    
    public ProductDto getProduct(String productId) {
        String cacheKey = "product:" + productId;
        
        return cacheService.get(cacheKey, ProductDto.class)
            .orElseGet(() -> {
                ProductDto product = fetchProductFromDatabase(productId);
                cacheService.put(cacheKey, product, Duration.ofHours(1));
                return product;
            });
    }
    
    public void updateProduct(ProductDto product) {
        // Update database
        saveToDatabase(product);
        
        // Update cache
        String cacheKey = "product:" + product.getId();
        if (cacheService.exists(cacheKey)) {
            cacheService.update(cacheKey, product);
        }
    }
}
```

### REST API Usage
The service includes a REST controller for testing and direct usage:

```bash
# Store a value
POST /api/v1/cache/my-key?ttlSeconds=300
Content-Type: application/json
{
  "id": "123",
  "name": "Test Product",
  "price": 99.99
}

# Retrieve a value
GET /api/v1/cache/my-key?type=com.liverpool.dto.SampleDto

# Update a value
PUT /api/v1/cache/my-key?ttlSeconds=600
Content-Type: application/json
{
  "id": "123",
  "name": "Updated Product",
  "price": 149.99
}

# Delete a value
DELETE /api/v1/cache/my-key

# Update TTL
PUT /api/v1/cache/my-key/ttl?ttlSeconds=900

# Check if exists
GET /api/v1/cache/my-key/exists

# Get TTL
GET /api/v1/cache/my-key/ttl
```

## Dependencies

The service uses the following key dependencies:
- `spring-boot-starter-data-redis` (includes Lettuce)
- `jackson-databind` for JSON serialization
- `jackson-datatype-jsr310` for Java 8 time support

## Performance Characteristics

- **Connection Pooling**: Up to 25 active connections
- **Async Operations**: Non-blocking Redis operations
- **Memory Efficient**: String-based storage with JSON serialization
- **Type Safe**: Compile-time type checking
- **Error Resilient**: Graceful error handling with Optional returns

## Best Practices

1. **Use appropriate TTL**: Set reasonable expiration times to prevent memory leaks
2. **Handle Optional results**: Always check if cached values exist
3. **Use consistent key naming**: Implement a key naming strategy (e.g., "entity:id")
4. **Monitor memory usage**: Keep track of Redis memory consumption
5. **Use update operations**: Preserve existing TTL when updating values
6. **Batch operations**: Consider using Redis pipelines for bulk operations (future enhancement)

## Testing

Run the comprehensive test suite:
```bash
mvn test
```

The tests cover all operations including edge cases and error scenarios.
