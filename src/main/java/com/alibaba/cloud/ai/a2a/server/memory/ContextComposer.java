package com.alibaba.cloud.ai.a2a.server.memory;

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
