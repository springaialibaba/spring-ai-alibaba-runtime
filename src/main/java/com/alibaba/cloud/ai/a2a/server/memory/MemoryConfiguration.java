package com.alibaba.cloud.ai.a2a.server.memory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 内存服务配置类
 * 提供Spring Boot自动配置支持
 */
@Configuration
@EnableConfigurationProperties(MemoryServiceProperties.class)
public class MemoryConfiguration {
    
    /**
     * 默认的内存服务（内存实现）
     */
    @Bean
    @ConditionalOnProperty(name = "memory.service.type", havingValue = "memory", matchIfMissing = true)
    public MemoryService inMemoryMemoryService() {
        return new InMemoryMemoryService();
    }
    
    /**
     * 默认的会话历史服务（内存实现）
     */
    @Bean
    @ConditionalOnProperty(name = "session.service.type", havingValue = "memory", matchIfMissing = true)
    public SessionHistoryService inMemorySessionHistoryService() {
        return new InMemorySessionHistoryService();
    }
    
    /**
     * Redis内存服务
     */
    @Bean
    @ConditionalOnProperty(name = "memory.service.type", havingValue = "redis")
    public MemoryService redisMemoryService(RedisTemplate<String, String> redisTemplate) {
        return new RedisMemoryService(redisTemplate);
    }
    
    /**
     * Redis会话历史服务
     */
    @Bean
    @ConditionalOnProperty(name = "session.service.type", havingValue = "redis")
    public SessionHistoryService redisSessionHistoryService(RedisTemplate<String, String> redisTemplate) {
        return new RedisSessionHistoryService(redisTemplate);
    }
    
    /**
     * 上下文管理器
     */
    @Bean
    public ContextManager contextManager(
            MemoryService memoryService,
            SessionHistoryService sessionHistoryService) {
        return ContextManagerFactory.createCustom(memoryService, sessionHistoryService);
    }
}
