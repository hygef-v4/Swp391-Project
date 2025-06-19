package org.swp391.hotelbookingsystem.config;

import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@Configuration
@EnableCaching
public class RedisConfig {
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(
                RedisSerializationContext
                    .SerializationPair
                    .fromSerializer(new GenericJackson2JsonRedisSerializer())
            );
    }
}
