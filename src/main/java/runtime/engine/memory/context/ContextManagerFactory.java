package runtime.engine.memory.context;

import runtime.engine.memory.service.MemoryService;
import runtime.engine.memory.service.SessionHistoryService;
import runtime.engine.memory.persistence.memory.service.RedisMemoryService;
import runtime.engine.memory.persistence.session.RedisSessionHistoryService;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 上下文管理器工厂类
 * 提供便捷的方法来创建ContextManager实例
 */
public class ContextManagerFactory {
    
    /**
     * 创建默认的上下文管理器（使用内存实现）
     * 
     * @return ContextManager 默认的上下文管理器
     */
    public static ContextManager createDefault() {
        return new ContextManager();
    }
    
    /**
     * 创建使用Redis的上下文管理器
     * 
     * @param redisTemplate Redis模板
     * @return ContextManager 使用Redis的上下文管理器
     */
    public static ContextManager createWithRedis(RedisTemplate<String, String> redisTemplate) {
        MemoryService memoryService = new RedisMemoryService(redisTemplate);
        SessionHistoryService sessionHistoryService = new RedisSessionHistoryService(redisTemplate);
        
        return new ContextManager(
                ContextComposer.class,
                sessionHistoryService,
                memoryService
        );
    }
    
    /**
     * 创建自定义的上下文管理器
     * 
     * @param memoryService 内存服务
     * @param sessionHistoryService 会话历史服务
     * @return ContextManager 自定义的上下文管理器
     */
    public static ContextManager createCustom(
            MemoryService memoryService,
            SessionHistoryService sessionHistoryService) {
        return new ContextManager(
                ContextComposer.class,
                sessionHistoryService,
                memoryService
        );
    }
    
    /**
     * 创建上下文管理器并异步启动
     * 
     * @param memoryService 可选的内存服务
     * @param sessionHistoryService 可选的会话历史服务
     * @return CompletableFuture<ContextManager> 异步创建的上下文管理器
     */
    public static CompletableFuture<ContextManager> createContextManager(
            Optional<MemoryService> memoryService,
            Optional<SessionHistoryService> sessionHistoryService) {
        
        return CompletableFuture.supplyAsync(() -> {
            ContextManager manager = new ContextManager(
                    ContextComposer.class,
                    sessionHistoryService.orElse(null),
                    memoryService.orElse(null)
            );
            
            try {
                manager.start().get();
                return manager;
            } catch (Exception e) {
                throw new RuntimeException("Failed to start context manager", e);
            }
        });
    }
}
