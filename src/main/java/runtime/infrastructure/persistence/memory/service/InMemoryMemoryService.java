package runtime.infrastructure.persistence.memory.service;

import runtime.domain.memory.model.Message;
import runtime.domain.memory.model.MessageContent;
import runtime.domain.memory.model.MessageType;
import runtime.domain.memory.service.MemoryService;
import runtime.infrastructure.config.memory.MemoryProperties;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存实现的内存服务
 */
public class InMemoryMemoryService implements MemoryService {
    
    private final Map<String, Map<String, List<Message>>> store = new ConcurrentHashMap<>();
    private static final String DEFAULT_SESSION_ID = "default_session";
    private MemoryProperties memoryProperties;
    
    @Override
    public CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(() -> {
            store.clear();
        });
    }
    
    @Override
    public CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> {
            store.clear();
        });
    }
    
    @Override
    public CompletableFuture<Boolean> health() {
        return CompletableFuture.completedFuture(true);
    }
    
    @Override
    public CompletableFuture<Void> addMemory(String userId, List<Message> messages, Optional<String> sessionId) {
        return CompletableFuture.runAsync(() -> {
            store.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
            
            String storageKey = sessionId.orElse(DEFAULT_SESSION_ID);
            store.get(userId).computeIfAbsent(storageKey, k -> new ArrayList<>())
                    .addAll(messages);
        });
    }
    
    @Override
    public CompletableFuture<List<Message>> searchMemory(String userId, List<Message> messages, Optional<Map<String, Object>> filters) {
        return CompletableFuture.supplyAsync(() -> {
            if (!store.containsKey(userId) || messages == null || messages.isEmpty()) {
                return Collections.emptyList();
            }
            
            Message lastMessage = messages.get(messages.size() - 1);
            String query = getQueryText(lastMessage);
            if (query == null || query.trim().isEmpty()) {
                return Collections.emptyList();
            }
            
            Set<String> keywords = Arrays.stream(query.toLowerCase().split("\\s+"))
                    .collect(Collectors.toSet());
            
            List<Message> allMessages = store.get(userId).values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            
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
        });
    }
    
    @Override
    public CompletableFuture<List<Message>> listMemory(String userId, Optional<Map<String, Object>> filters) {
        return CompletableFuture.supplyAsync(() -> {
            if (!store.containsKey(userId)) {
                return Collections.emptyList();
            }
            
            List<Message> allMessages = store.get(userId).entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .flatMap(entry -> entry.getValue().stream())
                    .collect(Collectors.toList());
            
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
        });
    }
    
    @Override
    public CompletableFuture<Void> deleteMemory(String userId, Optional<String> sessionId) {
        return CompletableFuture.runAsync(() -> {
            if (!store.containsKey(userId)) {
                return;
            }
            
            if (sessionId.isPresent()) {
                store.get(userId).remove(sessionId.get());
            } else {
                store.remove(userId);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<String>> getAllUsers() {
        return CompletableFuture.supplyAsync(() -> {
            return new ArrayList<>(store.keySet());
        });
    }
    
    /**
     * 从消息中获取查询文本
     * 
     * @param message 消息对象
     * @return 查询文本，如果无法提取则返回null
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
