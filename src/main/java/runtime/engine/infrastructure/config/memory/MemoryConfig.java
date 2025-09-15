package runtime.engine.infrastructure.config.memory;

import runtime.engine.memory.service.EmbeddingService;
import runtime.engine.memory.service.MemoryService;
import runtime.engine.memory.service.SessionHistoryService;
import runtime.engine.memory.persistence.memory.service.InMemoryMemoryService;
import runtime.engine.memory.persistence.memory.service.MySQLMemoryService;
import runtime.engine.memory.persistence.memory.service.RedisMemoryService;
import runtime.engine.memory.persistence.memory.service.SimpleEmbeddingService;
import runtime.engine.memory.persistence.session.InMemorySessionHistoryService;
import runtime.engine.memory.persistence.session.MySQLSessionHistoryService;
import runtime.engine.memory.persistence.session.RedisSessionHistoryService;
import runtime.engine.memory.persistence.memory.repository.MemoryRepository;
import runtime.engine.memory.persistence.memory.repository.SessionMessageRepository;
import runtime.engine.memory.persistence.memory.repository.SessionRepository;
import runtime.engine.memory.context.ContextManager;
import runtime.engine.memory.context.ContextManagerFactory;
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
@EnableConfigurationProperties(MemoryProperties.class)
public class MemoryConfig {
    
    /**
     * 默认的内存服务（内存实现）
     */
    @Bean
    @ConditionalOnProperty(name = "memory.service.type", havingValue = "memory", matchIfMissing = true)
    public MemoryService inMemoryMemoryService(MemoryProperties memoryProperties) {
        InMemoryMemoryService service = new InMemoryMemoryService();
        service.setMemoryProperties(memoryProperties);
        return service;
    }
    
    /**
     * 默认的会话历史服务（内存实现）
     */
    @Bean
    @ConditionalOnProperty(name = "memory.service.type", havingValue = "memory", matchIfMissing = true)
    public SessionHistoryService inMemorySessionHistoryService() {
        return new InMemorySessionHistoryService();
    }
    
    /**
     * Redis内存服务
     */
    @Bean
    @ConditionalOnProperty(name = "memory.service.type", havingValue = "redis")
    public MemoryService redisMemoryService(RedisTemplate<String, String> redisTemplate, MemoryProperties memoryProperties) {
        RedisMemoryService service = new RedisMemoryService(redisTemplate);
        service.setMemoryProperties(memoryProperties);
        return service;
    }
    
    /**
     * Redis会话历史服务
     */
    @Bean
    @ConditionalOnProperty(name = "memory.service.type", havingValue = "redis")
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
                                          EmbeddingService embeddingService, MemoryProperties memoryProperties) {
        MySQLMemoryService service = new MySQLMemoryService();
        service.setMemoryRepository(memoryRepository);
        service.setObjectMapper(objectMapper);
        service.setEmbeddingService(embeddingService);
        service.setMemoryProperties(memoryProperties);
        return service;
    }
    
    /**
     * MySQL会话历史服务
     */
    @Bean
    @ConditionalOnProperty(name = "memory.service.type", havingValue = "mysql")
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
