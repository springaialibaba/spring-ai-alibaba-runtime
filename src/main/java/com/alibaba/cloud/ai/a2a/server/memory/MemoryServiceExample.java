package com.alibaba.cloud.ai.a2a.server.memory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 内存服务使用示例
 * 展示如何使用ContextManager进行会话和内存管理
 */
@Component
public class MemoryServiceExample {
    
    @Autowired
    private ContextManager contextManager;
    
    /**
     * 示例：处理用户消息
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param userMessage 用户消息
     * @return CompletableFuture<String> 异步处理结果
     */
    public CompletableFuture<String> processUserMessage(String userId, String sessionId, String userMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 获取或创建会话
                Session session = contextManager.composeSession(userId, sessionId).get();
                
                // 2. 创建用户消息对象
                MessageContent content = new MessageContent("text", userMessage);
                Message message = new Message(MessageType.USER, List.of(content));
                
                // 3. 组合上下文（包括历史消息和记忆）
                contextManager.composeContext(session, List.of(message)).get();
                
                // 4. 模拟AI响应
                String aiResponse = "AI回复: " + userMessage + " (基于历史上下文)";
                MessageContent responseContent = new MessageContent("text", aiResponse);
                Message responseMessage = new Message(MessageType.ASSISTANT, List.of(responseContent));
                
                // 5. 将AI响应添加到会话
                contextManager.append(session, List.of(responseMessage)).get();
                
                return aiResponse;
            } catch (Exception e) {
                throw new RuntimeException("处理用户消息失败", e);
            }
        });
    }
    
    /**
     * 示例：搜索相关记忆
     * 
     * @param userId 用户ID
     * @param query 查询文本
     * @return CompletableFuture<List<Message>> 异步搜索结果
     */
    public CompletableFuture<List<Message>> searchMemories(String userId, String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MemoryService memoryService = contextManager.getMemoryService();
                if (memoryService == null) {
                    return List.<Message>of();
                }
                
                MessageContent content = new MessageContent("text", query);
                Message message = new Message(MessageType.MESSAGE, List.of(content));
                
                return memoryService.searchMemory(userId, List.of(message), Optional.of(Map.of("top_k", 5))).get();
            } catch (Exception e) {
                throw new RuntimeException("搜索记忆失败", e);
            }
        });
    }
    
    /**
     * 示例：列出用户的所有会话
     * 
     * @param userId 用户ID
     * @return CompletableFuture<List<Session>> 异步会话列表
     */
    public CompletableFuture<List<Session>> listUserSessions(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                SessionHistoryService sessionService = contextManager.getSessionHistoryService();
                if (sessionService == null) {
                    return List.<Session>of();
                }
                
                return sessionService.listSessions(userId).get();
            } catch (Exception e) {
                throw new RuntimeException("列出用户会话失败", e);
            }
        });
    }
    
    /**
     * 示例：删除用户的所有记忆
     * 
     * @param userId 用户ID
     * @return CompletableFuture<Void> 异步删除结果
     */
    public CompletableFuture<Void> clearUserMemory(String userId) {
        return CompletableFuture.runAsync(() -> {
            try {
                MemoryService memoryService = contextManager.getMemoryService();
                if (memoryService != null) {
                    memoryService.deleteMemory(userId, Optional.empty()).get();
                }
            } catch (Exception e) {
                throw new RuntimeException("清除用户记忆失败", e);
            }
        });
    }
}
