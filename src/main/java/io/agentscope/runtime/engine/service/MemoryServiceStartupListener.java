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
package io.agentscope.runtime.engine.service;

import io.agentscope.runtime.engine.memory.context.ContextManager;
import io.agentscope.runtime.engine.infrastructure.config.memory.MemoryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 记忆服务启动监听器
 * 在应用启动完成后自动启动记忆服务
 */
@Component
@ConditionalOnProperty(name = "memory.service.auto-start", havingValue = "true", matchIfMissing = true)
public class MemoryServiceStartupListener {
    
    private static final Logger logger = LoggerFactory.getLogger(MemoryServiceStartupListener.class);
    
    @Autowired
    private ContextManager contextManager;
    
    @Autowired
    private MemoryProperties properties;
    
    /**
     * 应用启动完成后自动启动记忆服务
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("应用启动完成，正在启动记忆服务...");
        
        CompletableFuture.runAsync(() -> {
            try {
                // 检查服务是否已经启动
                if (contextManager.hasService("memory") || contextManager.hasService("session")) {
                    logger.info("记忆服务已经启动，跳过自动启动");
                    return;
                }
                
                // 启动记忆服务
                contextManager.start().get();
                logger.info("记忆服务启动成功");
                
                // 执行健康检查（如果配置启用）
                if (properties.isHealthCheckOnStart()) {
                    contextManager.healthCheck().thenAccept(healthStatus -> {
                        logger.info("记忆服务健康检查结果: {}", healthStatus);
                        boolean allHealthy = healthStatus.values().stream().allMatch(Boolean::booleanValue);
                        if (allHealthy) {
                            logger.info("所有记忆服务组件运行正常");
                        } else {
                            logger.warn("部分记忆服务组件运行异常: {}", healthStatus);
                        }
                    }).exceptionally(throwable -> {
                        logger.error("记忆服务健康检查失败", throwable);
                        return null;
                    });
                }
                
            } catch (Exception e) {
                logger.error("记忆服务启动失败", e);
                logger.warn("记忆服务启动失败，但应用将继续运行。请检查配置或手动启动服务。");
            }
        });
    }
}
