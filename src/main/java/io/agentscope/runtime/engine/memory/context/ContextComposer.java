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

import io.agentscope.runtime.engine.memory.model.Message;
import io.agentscope.runtime.engine.memory.model.Session;
import io.agentscope.runtime.engine.memory.service.MemoryService;
import io.agentscope.runtime.engine.memory.service.SessionHistoryService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 上下文组合器
 * 负责组合会话历史和内存信息
 */
public class ContextComposer {
    
    /**
     * 组合上下文信息
     * 
     * @param requestInput 当前输入消息
     * @param session 会话对象
     * @param memoryService 可选的内存服务
     * @param sessionHistoryService 可选的会话历史服务
     * @return CompletableFuture<Void> 异步组合结果
     */
    public static CompletableFuture<Void> compose(
            List<Message> requestInput,
            Session session,
            Optional<MemoryService> memoryService,
            Optional<SessionHistoryService> sessionHistoryService) {
        
        return CompletableFuture.runAsync(() -> {
            // 处理会话历史
            if (sessionHistoryService.isPresent()) {
                try {
                    sessionHistoryService.get().appendMessage(session, requestInput).get();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to append message to session history", e);
                }
            } else {
                session.getMessages().addAll(requestInput);
            }
            
            // 处理内存
            if (memoryService.isPresent()) {
                try {
                    // 搜索相关记忆
                    List<Message> memories = memoryService.get()
                            .searchMemory(session.getUserId(), requestInput, Optional.of(Map.of("top_k", 5)))
                            .get();
                    
                    // 添加当前消息到内存
                    memoryService.get()
                            .addMemory(session.getUserId(), requestInput, Optional.of(session.getId()))
                            .get();
                    
                    // 将记忆添加到会话消息的开头
                    session.getMessages().addAll(0, memories);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to process memory", e);
                }
            }
        });
    }
}
