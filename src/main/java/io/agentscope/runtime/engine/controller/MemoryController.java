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
package io.agentscope.runtime.engine.controller;

import io.agentscope.runtime.engine.memory.dto.ContentItem;
import io.agentscope.runtime.engine.memory.dto.MemoryItem;
import io.agentscope.runtime.engine.memory.dto.MemoryRequest;
import io.agentscope.runtime.engine.memory.dto.MemoryResponse;
import io.agentscope.runtime.engine.memory.model.Message;
import io.agentscope.runtime.engine.memory.model.MessageContent;
import io.agentscope.runtime.engine.memory.model.MessageType;
import io.agentscope.runtime.engine.memory.model.Session;
import io.agentscope.runtime.engine.memory.service.MemoryService;
import io.agentscope.runtime.engine.memory.service.SessionHistoryService;
import io.agentscope.runtime.engine.infrastructure.config.memory.MemoryProperties;
import io.agentscope.runtime.engine.memory.context.ContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/memory")
public class MemoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(MemoryController.class);
    
    @Autowired
    private ContextManager contextManager;
    
    @Autowired
    private MemoryProperties memoryProperties;
    
    /**
     * 获取用户的所有记忆
     * 
     * @param userId 用户ID
     * @param pageNum 页码（可选，默认1）
     * @param pageSize 页面大小（可选，默认10）
     * @return 记忆列表
     */
    @GetMapping("/user/{userId}")
    public CompletableFuture<ResponseEntity<MemoryResponse>> getUserMemories(
            @PathVariable String userId,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                MemoryService memoryService = contextManager.getMemoryService();
                if (memoryService == null) {
                    return ResponseEntity.ok(MemoryResponse.error("记忆服务未启用"));
                }
                
                // 使用配置的默认值
                int actualPageNum = pageNum != null ? pageNum : 1;
                int actualPageSize = pageSize != null ? pageSize : memoryProperties.getDefaultPageSize();
                
                Map<String, Object> filters = Map.of(
                    "page_num", actualPageNum,
                    "page_size", actualPageSize
                );
                
                List<Message> messages = memoryService.listMemory(userId, Optional.of(filters)).get();
                List<MemoryItem> memoryItems = convertToMemoryItems(messages, userId);
                
                MemoryResponse response = MemoryResponse.success("获取用户记忆成功", memoryItems);
                response.setMemories(memoryItems);
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("获取用户记忆失败", e);
                return ResponseEntity.ok(MemoryResponse.error("获取用户记忆失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 获取用户特定会话的记忆
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 会话记忆列表
     */
    @GetMapping("/user/{userId}/session/{sessionId}")
    public CompletableFuture<ResponseEntity<MemoryResponse>> getSessionMemories(
            @PathVariable String userId,
            @PathVariable String sessionId) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                SessionHistoryService sessionService = contextManager.getSessionHistoryService();
                if (sessionService == null) {
                    return ResponseEntity.ok(MemoryResponse.error("会话服务未启用"));
                }
                
                Optional<Session> sessionOpt = sessionService.getSession(userId, sessionId).get();
                if (sessionOpt.isEmpty()) {
                    return ResponseEntity.ok(MemoryResponse.error("会话不存在"));
                }
                
                Session session = sessionOpt.get();
                List<MemoryItem> memoryItems = convertSessionToMemoryItems(session);
                
                MemoryResponse response = MemoryResponse.success("获取会话记忆成功", memoryItems);
                response.setMemories(memoryItems);
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("获取会话记忆失败", e);
                return ResponseEntity.ok(MemoryResponse.error("获取会话记忆失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 搜索记忆
     * 
     * @param userId 用户ID
     * @param query 搜索查询
     * @param topK 返回数量（可选，默认5）
     * @return 匹配的记忆列表
     */
    @GetMapping("/user/{userId}/search")
    public CompletableFuture<ResponseEntity<MemoryResponse>> searchMemories(
            @PathVariable String userId,
            @RequestParam String query,
            @RequestParam(required = false) Integer topK) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                MemoryService memoryService = contextManager.getMemoryService();
                if (memoryService == null) {
                    return ResponseEntity.ok(MemoryResponse.error("记忆服务未启用"));
                }
                
                // 使用配置的默认值
                int actualTopK = topK != null ? topK : memoryProperties.getDefaultTopK();
                
                // 创建搜索消息
                MessageContent content = new MessageContent("text", query);
                Message searchMessage = new Message(MessageType.MESSAGE, List.of(content));
                
                Map<String, Object> filters = Map.of("top_k", actualTopK);
                List<Message> messages = memoryService.searchMemory(userId, List.of(searchMessage), Optional.of(filters)).get();
                List<MemoryItem> memoryItems = convertToMemoryItems(messages, userId);
                
                MemoryResponse response = MemoryResponse.success("搜索记忆成功", memoryItems);
                response.setMemories(memoryItems);
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("搜索记忆失败", e);
                return ResponseEntity.ok(MemoryResponse.error("搜索记忆失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 添加记忆
     * 
     * @param request 记忆请求
     * @return 添加结果
     */
    @PostMapping("/add")
    public CompletableFuture<ResponseEntity<MemoryResponse>> addMemory(@RequestBody MemoryRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(MemoryResponse.error("用户ID不能为空"));
                }
                
                if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(MemoryResponse.error("消息内容不能为空"));
                }
                
                // 创建消息
                MessageContent content = new MessageContent("text", request.getMessage());
                MessageType messageType = MessageType.valueOf(request.getMessageType().toUpperCase());
                Message message = new Message(messageType, List.of(content));
                
                // 添加到记忆
                MemoryService memoryService = contextManager.getMemoryService();
                if (memoryService != null) {
                    memoryService.addMemory(
                        request.getUserId(), 
                        List.of(message), 
                        Optional.ofNullable(request.getSessionId())
                    ).get();
                }
                
                // 添加到会话历史
                SessionHistoryService sessionService = contextManager.getSessionHistoryService();
                if (sessionService != null) {
                    Session session = contextManager.composeSession(request.getUserId(), request.getSessionId()).get();
                    sessionService.appendMessage(session, List.of(message)).get();
                }
                
                return ResponseEntity.ok(MemoryResponse.success("添加记忆成功"));
                
            } catch (Exception e) {
                logger.error("添加记忆失败", e);
                return ResponseEntity.ok(MemoryResponse.error("添加记忆失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 删除用户记忆
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID（可选，如果不提供则删除用户所有记忆）
     * @return 删除结果
     */
    @DeleteMapping("/user/{userId}")
    public CompletableFuture<ResponseEntity<MemoryResponse>> deleteMemories(
            @PathVariable String userId,
            @RequestParam(required = false) String sessionId) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                MemoryService memoryService = contextManager.getMemoryService();
                if (memoryService == null) {
                    return ResponseEntity.ok(MemoryResponse.error("记忆服务未启用"));
                }
                
                memoryService.deleteMemory(userId, Optional.ofNullable(sessionId)).get();
                
                String message = sessionId != null ? 
                    "删除会话记忆成功" : "删除用户所有记忆成功";
                
                return ResponseEntity.ok(MemoryResponse.success(message));
                
            } catch (Exception e) {
                logger.error("删除记忆失败", e);
                return ResponseEntity.ok(MemoryResponse.error("删除记忆失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 获取用户的所有会话
     * 
     * @param userId 用户ID
     * @return 会话列表
     */
    @GetMapping("/user/{userId}/sessions")
    public CompletableFuture<ResponseEntity<MemoryResponse>> getUserSessions(@PathVariable String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                SessionHistoryService sessionService = contextManager.getSessionHistoryService();
                if (sessionService == null) {
                    return ResponseEntity.ok(MemoryResponse.error("会话服务未启用"));
                }
                
                List<Session> sessions = sessionService.listSessions(userId).get();
                List<MemoryItem> sessionItems = sessions.stream()
                    .map(this::convertSessionToMemoryItem)
                    .collect(Collectors.toList());
                
                MemoryResponse response = MemoryResponse.success("获取用户会话成功", sessionItems);
                response.setMemories(sessionItems);
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("获取用户会话失败", e);
                return ResponseEntity.ok(MemoryResponse.error("获取用户会话失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 获取所有用户的所有记忆（管理员接口）
     * 
     * @param pageNum 页码（可选，默认1）
     * @param pageSize 页面大小（可选，默认50）
     * @return 所有记忆列表
     */
    @GetMapping("/all")
    public CompletableFuture<ResponseEntity<MemoryResponse>> getAllMemories(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "50") int pageSize) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                MemoryService memoryService = contextManager.getMemoryService();
                if (memoryService == null) {
                    return ResponseEntity.ok(MemoryResponse.error("记忆服务未启用"));
                }
                
                // 获取所有记忆（这里需要实现获取所有用户记忆的方法）
                List<MemoryItem> allMemories = getAllMemoriesFromAllUsers(memoryService, pageNum, pageSize);
                
                MemoryResponse response = MemoryResponse.success("获取所有记忆成功", allMemories);
                response.setMemories(allMemories);
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("获取所有记忆失败", e);
                return ResponseEntity.ok(MemoryResponse.error("获取所有记忆失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 获取所有用户列表
     * 
     * @return 用户列表
     */
    @GetMapping("/users")
    public CompletableFuture<ResponseEntity<MemoryResponse>> getAllUsers() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MemoryService memoryService = contextManager.getMemoryService();
                if (memoryService == null) {
                    return ResponseEntity.ok(MemoryResponse.error("记忆服务未启用"));
                }
                
                List<String> users = getAllUsersFromMemory(memoryService);
                
                MemoryResponse response = MemoryResponse.success("获取所有用户成功", users);
                response.setData(users);
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("获取所有用户失败", e);
                return ResponseEntity.ok(MemoryResponse.error("获取所有用户失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 获取指定用户的记忆统计信息
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    @GetMapping("/user/{userId}/stats")
    public CompletableFuture<ResponseEntity<MemoryResponse>> getUserMemoryStats(@PathVariable String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MemoryService memoryService = contextManager.getMemoryService();
                SessionHistoryService sessionService = contextManager.getSessionHistoryService();
                
                if (memoryService == null || sessionService == null) {
                    return ResponseEntity.ok(MemoryResponse.error("服务未启用"));
                }
                
                // 获取用户记忆统计
                Map<String, Object> stats = getUserMemoryStatistics(memoryService, sessionService, userId);
                
                MemoryResponse response = MemoryResponse.success("获取用户记忆统计成功", stats);
                response.setData(stats);
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("获取用户记忆统计失败", e);
                return ResponseEntity.ok(MemoryResponse.error("获取用户记忆统计失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 将Message列表转换为MemoryItem列表
     */
    private List<MemoryItem> convertToMemoryItems(List<Message> messages, String userId) {
        return messages.stream()
            .map(msg -> {
                MemoryItem item = new MemoryItem();
                item.setUserId(userId);
                item.setType(msg.getType().name());
                item.setContent(msg.getContent().stream()
                    .map(content -> new ContentItem(content.getType(), content.getText()))
                    .collect(Collectors.toList()));
                item.setMetadata(msg.getMetadata());
                return item;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 将Session转换为MemoryItem列表
     */
    private List<MemoryItem> convertSessionToMemoryItems(Session session) {
        return session.getMessages().stream()
            .map(msg -> {
                MemoryItem item = new MemoryItem();
                item.setUserId(session.getUserId());
                item.setSessionId(session.getId());
                item.setType(msg.getType().name());
                item.setContent(msg.getContent().stream()
                    .map(content -> new ContentItem(content.getType(), content.getText()))
                    .collect(Collectors.toList()));
                item.setMetadata(msg.getMetadata());
                return item;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 将Session转换为单个MemoryItem（用于会话列表）
     */
    private MemoryItem convertSessionToMemoryItem(Session session) {
        MemoryItem item = new MemoryItem();
        item.setUserId(session.getUserId());
        item.setSessionId(session.getId());
        item.setType("SESSION");
        item.setContent(List.of(new ContentItem("text", "会话ID: " + session.getId())));
        return item;
    }
    
    /**
     * 获取所有用户的所有记忆
     */
    private List<MemoryItem> getAllMemoriesFromAllUsers(MemoryService memoryService, int pageNum, int pageSize) {
        try {
            // 首先获取所有用户
            List<String> allUsers = getAllUsersFromMemory(memoryService);
            
            List<MemoryItem> allMemories = new ArrayList<>();
            
            // 遍历每个用户获取记忆
            for (String userId : allUsers) {
                try {
                    Map<String, Object> filters = Map.of(
                        "page_num", 1,
                        "page_size", 1000 // 获取每个用户的所有记忆
                    );
                    List<Message> userMemories = memoryService.listMemory(userId, Optional.of(filters)).get();
                    List<MemoryItem> userMemoryItems = convertToMemoryItems(userMemories, userId);
                    allMemories.addAll(userMemoryItems);
                } catch (Exception e) {
                    logger.warn("获取用户 {} 的记忆失败: {}", userId, e.getMessage());
                }
            }
            
            // 按时间戳排序（最新的在前）
            allMemories.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
            
            // 分页处理
            int startIndex = (pageNum - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, allMemories.size());
            
            if (startIndex >= allMemories.size()) {
                return Collections.emptyList();
            }
            
            return allMemories.subList(startIndex, endIndex);
            
        } catch (Exception e) {
            logger.error("获取所有记忆失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取所有用户列表
     */
    private List<String> getAllUsersFromMemory(MemoryService memoryService) {
        try {
            return memoryService.getAllUsers().get();
        } catch (Exception e) {
            logger.error("获取所有用户失败", e);
            return Collections.emptyList();
        }
    }
    
    
    /**
     * 获取用户记忆统计信息
     */
    private Map<String, Object> getUserMemoryStatistics(MemoryService memoryService, SessionHistoryService sessionService, String userId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 获取记忆统计
            Map<String, Object> memoryFilters = Map.of("page_num", 1, "page_size", 10000);
            List<Message> memories = memoryService.listMemory(userId, Optional.of(memoryFilters)).get();
            
            stats.put("total_memories", memories.size());
            stats.put("user_messages", memories.stream()
                .filter(msg -> msg.getType() == MessageType.USER)
                .count());
            stats.put("assistant_messages", memories.stream()
                .filter(msg -> msg.getType() == MessageType.ASSISTANT)
                .count());
            
            // 获取会话统计
            List<Session> sessions = sessionService.listSessions(userId).get();
            stats.put("total_sessions", sessions.size());
            
            // 计算平均会话长度
            if (!sessions.isEmpty()) {
                double avgSessionLength = sessions.stream()
                    .mapToInt(session -> session.getMessages().size())
                    .average()
                    .orElse(0.0);
                stats.put("avg_session_length", Math.round(avgSessionLength * 100.0) / 100.0);
            } else {
                stats.put("avg_session_length", 0.0);
            }
            
            // 获取最近活动时间
            if (!memories.isEmpty()) {
                long latestTimestamp = memories.stream()
                    .mapToLong(msg -> System.currentTimeMillis()) // 这里应该从消息中获取实际时间戳
                    .max()
                    .orElse(0L);
                stats.put("last_activity", latestTimestamp);
            } else {
                stats.put("last_activity", 0L);
            }
            
            return stats;
            
        } catch (Exception e) {
            logger.error("获取用户记忆统计失败", e);
            return Map.of("error", "获取统计信息失败: " + e.getMessage());
        }
    }
}
