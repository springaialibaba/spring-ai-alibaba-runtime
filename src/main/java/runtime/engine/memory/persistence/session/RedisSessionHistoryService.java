package runtime.engine.memory.persistence.session;

import runtime.engine.memory.model.Message;
import runtime.engine.memory.model.Session;
import runtime.engine.memory.service.SessionHistoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 基于Redis的会话历史服务实现
 */
public class RedisSessionHistoryService implements SessionHistoryService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    public RedisSessionHistoryService(RedisTemplate<String, String> redisTemplate) {
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
    
    private String getSessionKey(String userId, String sessionId) {
        return "session:" + userId + ":" + sessionId;
    }
    
    private String getIndexKey(String userId) {
        return "session_index:" + userId;
    }
    
    private String sessionToJson(Session session) throws JsonProcessingException {
        return objectMapper.writeValueAsString(session);
    }
    
    private Session sessionFromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, Session.class);
    }
    
    @Override
    public CompletableFuture<Session> createSession(String userId, Optional<String> sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sid = sessionId.filter(s -> s != null && !s.trim().isEmpty())
                        .orElse(UUID.randomUUID().toString());
                
                Session session = new Session(sid, userId, new ArrayList<>());
                String key = getSessionKey(userId, sid);
                
                redisTemplate.opsForValue().set(key, sessionToJson(session));
                redisTemplate.opsForSet().add(getIndexKey(userId), sid);
                
                return session;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create session in Redis", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Optional<Session>> getSession(String userId, String sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String key = getSessionKey(userId, sessionId);
                String sessionJson = redisTemplate.opsForValue().get(key);
                
                if (sessionJson == null) {
                    // 如果会话不存在，创建一个新的
                    Session session = new Session(sessionId, userId, new ArrayList<>());
                    redisTemplate.opsForValue().set(key, sessionToJson(session));
                    redisTemplate.opsForSet().add(getIndexKey(userId), sessionId);
                    return Optional.of(session);
                }
                
                return Optional.of(sessionFromJson(sessionJson));
            } catch (Exception e) {
                throw new RuntimeException("Failed to get session from Redis", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> deleteSession(String userId, String sessionId) {
        return CompletableFuture.runAsync(() -> {
            try {
                String key = getSessionKey(userId, sessionId);
                redisTemplate.delete(key);
                redisTemplate.opsForSet().remove(getIndexKey(userId), sessionId);
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete session from Redis", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<Session>> listSessions(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String indexKey = getIndexKey(userId);
                Set<String> sessionIds = redisTemplate.opsForSet().members(indexKey);
                
                if (sessionIds == null) {
                    return Collections.emptyList();
                }
                
                List<Session> sessions = new ArrayList<>();
                for (String sessionId : sessionIds) {
                    String key = getSessionKey(userId, sessionId);
                    String sessionJson = redisTemplate.opsForValue().get(key);
                    if (sessionJson != null) {
                        Session session = sessionFromJson(sessionJson);
                        // 为了提高性能，返回的会话不包含详细历史
                        session.setMessages(new ArrayList<>());
                        sessions.add(session);
                    }
                }
                
                return sessions;
            } catch (Exception e) {
                throw new RuntimeException("Failed to list sessions from Redis", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> appendMessage(Session session, List<Message> messages) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (messages == null || messages.isEmpty()) {
                    return;
                }
                
                // 更新传入的会话对象
                session.getMessages().addAll(messages);
                
                String userId = session.getUserId();
                String sessionId = session.getId();
                String key = getSessionKey(userId, sessionId);
                
                String sessionJson = redisTemplate.opsForValue().get(key);
                if (sessionJson != null) {
                    Session storedSession = sessionFromJson(sessionJson);
                    storedSession.getMessages().addAll(messages);
                    redisTemplate.opsForValue().set(key, sessionToJson(storedSession));
                    redisTemplate.opsForSet().add(getIndexKey(userId), sessionId);
                } else {
                    System.err.println("Warning: Session " + session.getId() +
                            " not found in storage for append_message.");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to append message to session in Redis", e);
            }
        });
    }
    
    /**
     * 删除指定用户的所有会话历史数据
     */
    public CompletableFuture<Void> deleteUserSessions(String userId) {
        return CompletableFuture.runAsync(() -> {
            try {
                String indexKey = getIndexKey(userId);
                Set<String> sessionIds = redisTemplate.opsForSet().members(indexKey);
                
                if (sessionIds != null) {
                    for (String sessionId : sessionIds) {
                        String key = getSessionKey(userId, sessionId);
                        redisTemplate.delete(key);
                    }
                }
                
                redisTemplate.delete(indexKey);
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete user sessions from Redis", e);
            }
        });
    }
}
