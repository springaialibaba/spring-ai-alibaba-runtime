/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.agentscope.runtime.engine.memory.context;

import io.agentscope.runtime.engine.memory.service.MemoryService;
import io.agentscope.runtime.engine.memory.service.SessionHistoryService;
import io.agentscope.runtime.engine.memory.persistence.memory.service.RedisMemoryService;
import io.agentscope.runtime.engine.memory.persistence.session.RedisSessionHistoryService;
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
