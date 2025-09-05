package com.alibaba.cloud.ai.a2a.server.memory;

import com.alibaba.cloud.ai.a2a.server.memory.repository.MemoryRepository;
import com.alibaba.cloud.ai.a2a.server.memory.repository.SessionMessageRepository;
import com.alibaba.cloud.ai.a2a.server.memory.repository.SessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
     * 默认的嵌入服务
     */
    @Bean
    public EmbeddingService embeddingService() {
        return new SimpleEmbeddingService();
    }
    
    /**
     * MySQL内存服务
     */
    @Bean
    @ConditionalOnProperty(name = "memory.service.type", havingValue = "mysql")
    public MemoryService mysqlMemoryService(MemoryRepository memoryRepository, ObjectMapper objectMapper, 
                                          EmbeddingService embeddingService) {
        MySQLMemoryService service = new MySQLMemoryService();
        service.setMemoryRepository(memoryRepository);
        service.setObjectMapper(objectMapper);
        service.setEmbeddingService(embeddingService);
        return service;
    }
    
    /**
     * MySQL会话历史服务
     */
    @Bean
    @ConditionalOnProperty(name = "session.service.type", havingValue = "mysql")
    public SessionHistoryService mysqlSessionHistoryService(SessionRepository sessionRepository, 
                                                           SessionMessageRepository sessionMessageRepository, 
                                                           ObjectMapper objectMapper) {
        MySQLSessionHistoryService service = new MySQLSessionHistoryService();
        service.setSessionRepository(sessionRepository);
        service.setSessionMessageRepository(sessionMessageRepository);
        service.setObjectMapper(objectMapper);
        return service;
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
