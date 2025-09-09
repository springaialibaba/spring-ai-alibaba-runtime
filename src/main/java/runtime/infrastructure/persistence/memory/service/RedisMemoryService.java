package runtime.infrastructure.persistence.memory.service;

import runtime.domain.memory.model.Message;
import runtime.domain.memory.model.MessageContent;
import runtime.domain.memory.model.MessageType;
import runtime.domain.memory.service.MemoryService;
import runtime.infrastructure.config.memory.MemoryProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.HashOperations;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 基于Redis的内存服务实现
 */
public class RedisMemoryService implements MemoryService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String DEFAULT_SESSION_ID = "default_session";
    private MemoryProperties memoryProperties;
    
    public RedisMemoryService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public CompletableFuture<Void> start() {
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public CompletableFuture<Void> stop() {
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public CompletableFuture<Boolean> health() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String pong = redisTemplate.getConnectionFactory().getConnection().ping();
                return "PONG".equals(pong);
            } catch (Exception e) {
                return false;
            }
        });
    }
    
    private String getUserKey(String userId) {
        return "user_memory:" + userId;
    }
    
    private String serialize(List<Message> messages) throws JsonProcessingException {
        return objectMapper.writeValueAsString(messages);
    }
    
    private List<Message> deserialize(String messagesJson) throws JsonProcessingException {
        if (messagesJson == null || messagesJson.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return objectMapper.readValue(messagesJson, new TypeReference<List<Message>>() {});
    }
    
    @Override
    public CompletableFuture<Void> addMemory(String userId, List<Message> messages, Optional<String> sessionId) {
        return CompletableFuture.runAsync(() -> {
            try {
                String key = getUserKey(userId);
                String field = sessionId.orElse(DEFAULT_SESSION_ID);
                
                HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
                String existingJson = hashOps.get(key, field);
                List<Message> existingMessages = deserialize(existingJson);
                
                List<Message> allMessages = new ArrayList<>(existingMessages);
                allMessages.addAll(messages);
                
                hashOps.put(key, field, serialize(allMessages));
            } catch (Exception e) {
                throw new RuntimeException("Failed to add memory to Redis", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<Message>> searchMemory(String userId, List<Message> messages, Optional<Map<String, Object>> filters) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String key = getUserKey(userId);
                if (messages == null || messages.isEmpty()) {
                    return Collections.emptyList();
                }
                
                Message lastMessage = messages.get(messages.size() - 1);
                String query = getQueryText(lastMessage);
                if (query == null || query.trim().isEmpty()) {
                    return Collections.emptyList();
                }
                
                Set<String> keywords = Arrays.stream(query.toLowerCase().split("\\s+"))
                        .collect(Collectors.toSet());

                System.out.println("keywords: "+keywords);
                
                HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
                Map<String, String> allFields = hashOps.entries(key);
                
                List<Message> allMessages = new ArrayList<>();
                for (String sessionId : allFields.keySet()) {
                    String messagesJson = allFields.get(sessionId);
                    List<Message> sessionMessages = deserialize(messagesJson);
                    allMessages.addAll(sessionMessages);
                }
                
                List<Message> matchedMessages = allMessages.stream()
                        .filter(msg -> {
                            String content = getQueryText(msg);
                            if (content != null) {
                                String contentLower = content.toLowerCase();
                                return keywords.stream().anyMatch(keyword -> contentLower.contains(keyword));
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
                
                if (filters.isPresent() && filters.get().containsKey("top_k")) {
                    Object topKObj = filters.get().get("top_k");
                    if (topKObj instanceof Integer) {
                        int topK = (Integer) topKObj;
                        int startIndex = Math.max(0, matchedMessages.size() - topK);
                        return matchedMessages.subList(startIndex, matchedMessages.size());
                    }
                }
                
                return matchedMessages;
            } catch (Exception e) {
                throw new RuntimeException("Failed to search memory in Redis", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<Message>> listMemory(String userId, Optional<Map<String, Object>> filters) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String key = getUserKey(userId);
                HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
                Map<String, String> allFields = hashOps.entries(key);
                
                List<Message> allMessages = new ArrayList<>();
                for (String sessionId : allFields.keySet().stream().sorted().collect(Collectors.toList())) {
                    String messagesJson = allFields.get(sessionId);
                    List<Message> sessionMessages = deserialize(messagesJson);
                    allMessages.addAll(sessionMessages);
                }
                
                if (filters.isPresent()) {
                    Map<String, Object> filterMap = filters.get();
                    int pageNum = (Integer) filterMap.getOrDefault("page_num", 1);
                    int pageSize = (Integer) filterMap.getOrDefault("page_size", 
                        memoryProperties != null ? memoryProperties.getDefaultPageSize() : 10);
                    
                    int startIndex = (pageNum - 1) * pageSize;
                    int endIndex = Math.min(startIndex + pageSize, allMessages.size());
                    
                    if (startIndex >= allMessages.size()) {
                        return Collections.emptyList();
                    }
                    
                    return allMessages.subList(startIndex, endIndex);
                }
                
                return allMessages;
            } catch (Exception e) {
                throw new RuntimeException("Failed to list memory from Redis", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> deleteMemory(String userId, Optional<String> sessionId) {
        return CompletableFuture.runAsync(() -> {
            try {
                String key = getUserKey(userId);
                HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
                
                if (sessionId.isPresent()) {
                    hashOps.delete(key, sessionId.get());
                } else {
                    redisTemplate.delete(key);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete memory from Redis", e);
            }
        });
    }
    
    /**
     * 清空所有内存数据
     */
    public CompletableFuture<Void> clearAllMemory() {
        return CompletableFuture.runAsync(() -> {
            try {
                Set<String> keys = redisTemplate.keys(getUserKey("*"));
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to clear all memory from Redis", e);
            }
        });
    }
    
    /**
     * 删除指定用户的所有内存数据
     */
    public CompletableFuture<Void> deleteUserMemory(String userId) {
        return CompletableFuture.runAsync(() -> {
            try {
                String key = getUserKey(userId);
                redisTemplate.delete(key);
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete user memory from Redis", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<String>> getAllUsers() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Set<String> keys = redisTemplate.keys(getUserKey("*"));
                if (keys == null) {
                    return Collections.emptyList();
                }
                
                return keys.stream()
                    .map(key -> key.substring("user_memory:".length()))
                    .collect(Collectors.toList());
            } catch (Exception e) {
                throw new RuntimeException("Failed to get all users from Redis", e);
            }
        });
    }
    
    /**
     * 从消息中获取查询文本
     */
    private String getQueryText(Message message) {
        if (message == null || message.getContent() == null) {
            return null;
        }
        
        if (message.getType() == MessageType.MESSAGE) {
            return message.getContent().stream()
                    .filter(content -> "text".equals(content.getType()))
                    .map(MessageContent::getText)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        
        return null;
    }
    
    /**
     * 设置记忆配置属性
     */
    public void setMemoryProperties(MemoryProperties memoryProperties) {
        this.memoryProperties = memoryProperties;
    }
}
