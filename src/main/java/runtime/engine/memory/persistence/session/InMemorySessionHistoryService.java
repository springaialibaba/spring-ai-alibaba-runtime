package runtime.engine.memory.persistence.session;

import runtime.engine.memory.model.Message;
import runtime.engine.memory.model.Session;
import runtime.engine.memory.service.SessionHistoryService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存实现的会话历史服务
 * 将所有会话数据存储在字典中，适用于开发、测试和不需要持久化的场景
 */
public class InMemorySessionHistoryService implements SessionHistoryService {
    
    private final Map<String, Map<String, Session>> sessions = new ConcurrentHashMap<>();
    
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
        return CompletableFuture.completedFuture(true);
    }
    
    @Override
    public CompletableFuture<Session> createSession(String userId, Optional<String> sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            String sid = sessionId.filter(s -> s != null && !s.trim().isEmpty())
                    .orElse(UUID.randomUUID().toString());
            
            Session session = new Session(sid, userId, new ArrayList<>());
            sessions.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                    .put(sid, session);
            
            return deepCopy(session);
        });
    }
    
    @Override
    public CompletableFuture<Optional<Session>> getSession(String userId, String sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            Session session = sessions.getOrDefault(userId, Collections.emptyMap())
                    .get(sessionId);
            
            if (session == null) {
                session = new Session(sessionId, userId, new ArrayList<>());
                sessions.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                        .put(sessionId, session);
            }
            
            return Optional.of(deepCopy(session));
        });
    }
    
    @Override
    public CompletableFuture<Void> deleteSession(String userId, String sessionId) {
        return CompletableFuture.runAsync(() -> {
            Map<String, Session> userSessions = sessions.get(userId);
            if (userSessions != null) {
                userSessions.remove(sessionId);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<Session>> listSessions(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Session> userSessions = sessions.getOrDefault(userId, Collections.emptyMap());
            
            // 为了提高性能和减少数据传输，返回的会话对象不包含详细的响应历史
            return userSessions.values().stream()
                    .map(this::createSessionWithoutHistory)
                    .collect(Collectors.toList());
        });
    }
    
    @Override
    public CompletableFuture<Void> appendMessage(Session session, List<Message> messages) {
        return CompletableFuture.runAsync(() -> {
            if (messages == null || messages.isEmpty()) {
                return;
            }
            
            // 更新传入的会话对象
            session.getMessages().addAll(messages);
            
            // 更新内存中的副本
            Map<String, Session> userSessions = sessions.get(session.getUserId());
            if (userSessions != null) {
                Session storageSession = userSessions.get(session.getId());
                if (storageSession != null) {
                    storageSession.getMessages().addAll(messages);
                }
            }
        });
    }
    
    /**
     * 创建不包含历史记录的会话副本
     * 
     * @param session 原始会话
     * @return 不包含历史记录的会话副本
     */
    private Session createSessionWithoutHistory(Session session) {
        Session copy = deepCopy(session);
        copy.setMessages(new ArrayList<>());
        return copy;
    }
    
    /**
     * 深拷贝会话对象
     * 
     * @param session 要拷贝的会话
     * @return 深拷贝的会话对象
     */
    private Session deepCopy(Session session) {
        Session copy = new Session();
        copy.setId(session.getId());
        copy.setUserId(session.getUserId());
        copy.setMessages(new ArrayList<>(session.getMessages()));
        return copy;
    }
}
