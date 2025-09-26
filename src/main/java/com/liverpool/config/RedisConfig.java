package com.liverpool.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import java.time.Duration;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String redisHost;

  @Value("${spring.data.redis.port}")
  private int redisPort;

  @Value("${spring.data.redis.password}")
  private String redisPassword;

  @Value("${spring.data.redis.timeout}")
  private long timeout;

  @Value("${spring.data.redis.lettuce.pool.max-active}")
  private int maxActive;

  @Value("${spring.data.redis.lettuce.pool.max-idle}")
  private int maxIdle;

  @Value("${spring.data.redis.lettuce.pool.min-idle}")
  private int minIdle;

  @Value("${spring.data.redis.lettuce.pool.max-wait}")
  private long maxWait;

  @Bean
  /**
   * Creates and configures a high-performance Redis connection factory using Lettuce client with connection pooling.
   *
   * <p>This factory establishes connections to a Redis standalone server with optimized settings for
   * production environments, including connection pooling, timeout configurations, and network optimizations.</p>
   *
   * <h3>Connection Configuration:</h3>
   * <ul>
   *   <li><strong>RedisStandaloneConfiguration:</strong>
   *     <ul>
   *       <li>setHostName/setPort: Defines Redis server address and port</li>
   *       <li>setPassword: Sets authentication if Redis requires password</li>
   *     </ul>
   *   </li>
   * </ul>
   *
   * <h3>Connection Pool Settings:</h3>
   * <ul>
   *   <li><strong>GenericObjectPoolConfig:</strong>
   *     <ul>
   *       <li>setMaxTotal: Maximum number of active connections in the pool (default: 25)</li>
   *       <li>setMaxIdle: Maximum number of idle connections maintained in pool (default: 25)</li>
   *       <li>setMinIdle: Minimum number of idle connections guaranteed (default: 10)</li>
   *       <li>setMaxWait: Maximum wait time to obtain connection from pool (default: 500ms)</li>
   *       <li>setTestOnBorrow/Return/WhileIdle: Validates connections before use, on return, and periodically</li>
   *     </ul>
   *   </li>
   * </ul>
   *
   * <h3>Network Optimizations:</h3>
   * <ul>
   *   <li><strong>SocketOptions:</strong>
   *     <ul>
   *       <li>connectTimeout: TCP connection establishment timeout</li>
   *       <li>keepAlive: Maintains TCP connections alive to detect disconnections</li>
   *       <li>tcpNoDelay: Disables Nagle's algorithm for reduced latency</li>
   *     </ul>
   *   </li>
   *   <li><strong>TimeoutOptions:</strong>
   *     <ul>
   *       <li>fixedTimeout: Fixed timeout for Redis command execution</li>
   *     </ul>
   *   </li>
   * </ul>
   *
   * <h3>Client Behavior:</h3>
   * <ul>
   *   <li><strong>ClientOptions:</strong>
   *     <ul>
   *       <li>protocolVersion(RESP3): Uses latest Redis protocol for better performance</li>
   *       <li>autoReconnect: Enables automatic reconnection on connection loss</li>
   *       <li>disconnectedBehavior(REJECT_COMMANDS): Rejects commands when disconnected instead of queuing</li>
   *     </ul>
   *   </li>
   * </ul>
   *
   * <h3>Configuration Sources:</h3>
   * Configuration values are injected from application.yml properties:
   * <ul>
   *   <li>spring.data.redis.host - Redis server hostname</li>
   *   <li>spring.data.redis.port - Redis server port</li>
   *   <li>spring.data.redis.password - Redis authentication password</li>
   *   <li>spring.data.redis.timeout - Command execution timeout</li>
   *   <li>spring.data.redis.lettuce.pool.* - Connection pool settings</li>
   * </ul>
   *
   * @return {@link LettuceConnectionFactory} configured Redis connection factory with pooling support
   * @throws IllegalStateException if Redis configuration properties are invalid
   *
   * @see LettuceConnectionFactory
   * @see LettucePoolingClientConfiguration
   * @see GenericObjectPoolConfig
   * @see RedisStandaloneConfiguration
   *
   * @since 1.0.0
   */
  public LettuceConnectionFactory redisConnectionFactory() {
    // Configure Redis standalone
    RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
    redisConfig.setHostName(redisHost);
    redisConfig.setPort(redisPort);
    if (!redisPassword.isEmpty()) {
      redisConfig.setPassword(redisPassword);
    }

    // Configure connection pool
    GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
    poolConfig.setMaxTotal(maxActive);
    poolConfig.setMaxIdle(maxIdle);
    poolConfig.setMinIdle(minIdle);
    poolConfig.setMaxWait(Duration.ofMillis(maxWait));
    poolConfig.setTestOnBorrow(true);
    poolConfig.setTestOnReturn(true);
    poolConfig.setTestWhileIdle(true);

    // Configure socket options for performance
    SocketOptions socketOptions = SocketOptions.builder()
        .connectTimeout(Duration.ofMillis(timeout))
        .keepAlive(true)
        .tcpNoDelay(true)
        .build();

    // Configure timeout options
    TimeoutOptions timeoutOptions = TimeoutOptions.builder()
        .fixedTimeout(Duration.ofMillis(timeout))
        .build();

    // Configure client options for high performance
    ClientOptions clientOptions = ClientOptions.builder()
        .socketOptions(socketOptions)
        .timeoutOptions(timeoutOptions)
        .protocolVersion(ProtocolVersion.RESP3)
        .autoReconnect(true)
        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
        .build();

    // Configure Lettuce client with pooling
    LettucePoolingClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.builder()
        .poolConfig(poolConfig)
        .clientOptions(clientOptions)
        .commandTimeout(Duration.ofMillis(timeout))
        .build();

    return new LettuceConnectionFactory(redisConfig, clientConfiguration);
  }

  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // Use String serializers for both keys and values
    StringRedisSerializer stringSerializer = new StringRedisSerializer();
    template.setKeySerializer(stringSerializer);
    template.setValueSerializer(stringSerializer);
    template.setHashKeySerializer(stringSerializer);
    template.setHashValueSerializer(stringSerializer);

    template.setEnableTransactionSupport(false); // Better performance
    template.afterPropertiesSet();

    return template;
  }

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.findAndRegisterModules();
    return mapper;
  }
}
