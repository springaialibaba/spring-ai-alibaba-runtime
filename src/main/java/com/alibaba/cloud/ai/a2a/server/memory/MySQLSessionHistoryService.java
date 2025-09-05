package com.alibaba.cloud.ai.a2a.server.memory;

import com.alibaba.cloud.ai.a2a.server.memory.entity.SessionEntity;
import com.alibaba.cloud.ai.a2a.server.memory.entity.SessionMessageEntity;
import com.alibaba.cloud.ai.a2a.server.memory.repository.SessionMessageRepository;
import com.alibaba.cloud.ai.a2a.server.memory.repository.SessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 基于MySQL的会话历史服务实现
 */
public class MySQLSessionHistoryService implements SessionHistoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(MySQLSessionHistoryService.class);
    
    private SessionRepository sessionRepository;
    private SessionMessageRepository sessionMessageRepository;
    private ObjectMapper objectMapper;
    
    public void setSessionRepository(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }
    
    public void setSessionMessageRepository(SessionMessageRepository sessionMessageRepository) {
        this.sessionMessageRepository = sessionMessageRepository;
    }
    
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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
                // 简单的健康检查：尝试查询数据库
                sessionRepository.count();
                return true;
            } catch (Exception e) {
                logger.error("MySQL会话服务健康检查失败", e);
                return false;
            }
        });
    }
    
    @Override
    public CompletableFuture<Session> createSession(String userId, Optional<String> sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sid = sessionId.filter(s -> s != null && !s.trim().isEmpty())
                    .orElse(UUID.randomUUID().toString());
                
                // 检查会话是否已存在
                if (sessionRepository.existsBySessionId(sid)) {
                    logger.warn("会话已存在: {}", sid);
                    Optional<Session> existingSession = getSession(userId, sid).get();
                    return existingSession.orElseThrow(() -> new RuntimeException("获取已存在会话失败"));
                }
                
                SessionEntity entity = new SessionEntity(sid, userId);
                sessionRepository.save(entity);
                
                Session session = new Session(sid, userId, new ArrayList<>());
                logger.debug("创建会话成功，用户: {}, 会话: {}", userId, sid);
                
                return session;
                
            } catch (Exception e) {
                logger.error("创建会话失败", e);
                throw new RuntimeException("创建会话失败", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Optional<Session>> getSession(String userId, String sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Optional<SessionEntity> entityOpt = sessionRepository.findByUserIdAndSessionId(userId, sessionId);
                
                if (entityOpt.isPresent()) {
                    List<Message> messages = getSessionMessages(sessionId);
                    
                    Session session = new Session(sessionId, userId, messages);
                    logger.debug("获取会话成功，用户: {}, 会话: {}, 消息数: {}", 
                        userId, sessionId, messages.size());
                    
                    return Optional.of(session);
                } else {
                    // 如果会话不存在，创建一个新的
                    logger.debug("会话不存在，创建新会话，用户: {}, 会话: {}", userId, sessionId);
                    Session newSession = createSession(userId, Optional.of(sessionId)).get();
                    return Optional.of(newSession);
                }
                
            } catch (Exception e) {
                logger.error("获取会话失败", e);
                return Optional.empty();
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> deleteSession(String userId, String sessionId) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 先删除会话消息
                sessionMessageRepository.deleteBySessionId(sessionId);
                
                // 再删除会话
                sessionRepository.deleteBySessionId(sessionId);
                
                logger.debug("删除会话成功，用户: {}, 会话: {}", userId, sessionId);
                
            } catch (Exception e) {
                logger.error("删除会话失败", e);
                throw new RuntimeException("删除会话失败", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<Session>> listSessions(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<SessionEntity> entities = sessionRepository.findByUserIdOrderByLastActivityDesc(userId);
                
                List<Session> sessions = entities.stream()
                    .map(entity -> {
                        Session session = new Session(entity.getSessionId(), entity.getUserId(), new ArrayList<>());
                        return session;
                    })
                    .collect(Collectors.toList());
                
                logger.debug("列出用户会话成功，用户: {}, 会话数: {}", userId, sessions.size());
                
                return sessions;
                
            } catch (Exception e) {
                logger.error("列出用户会话失败", e);
                return Collections.emptyList();
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
                
                // 更新会话活动时间
                Optional<SessionEntity> entityOpt = sessionRepository.findBySessionId(session.getId());
                if (entityOpt.isPresent()) {
                    SessionEntity entity = entityOpt.get();
                    entity.setLastActivity(new java.sql.Timestamp(System.currentTimeMillis()).toLocalDateTime());
                    sessionRepository.save(entity);
                }
                
                // 添加消息到会话
                for (Message message : messages) {
                    SessionMessageEntity messageEntity = new SessionMessageEntity();
                    messageEntity.setSessionId(session.getId());
                    messageEntity.setMessageType(message.getType());
                    messageEntity.setContent(serializeMessageContent(message.getContent()));
                    messageEntity.setMetadata(serializeMetadata(message.getMetadata()));
                    
                    sessionMessageRepository.save(messageEntity);
                }
                
                // 更新会话对象
                session.getMessages().addAll(messages);
                
                logger.debug("添加消息到会话成功，会话: {}, 消息数: {}", 
                    session.getId(), messages.size());
                
            } catch (Exception e) {
                logger.error("添加消息到会话失败", e);
                throw new RuntimeException("添加消息到会话失败", e);
            }
        });
    }
    
    /**
     * 获取会话的所有消息
     */
    private List<Message> getSessionMessages(String sessionId) {
        try {
            List<SessionMessageEntity> messageEntities = sessionMessageRepository
                .findBySessionIdOrderByCreatedAtAsc(sessionId);
            
            return messageEntities.stream()
                .map(this::convertToMessage)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("获取会话消息失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 序列化消息内容
     */
    private String serializeMessageContent(List<MessageContent> content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            logger.warn("序列化消息内容失败", e);
            return "[]";
        }
    }
    
    /**
     * 序列化元数据
     */
    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            logger.warn("序列化元数据失败", e);
            return null;
        }
    }
    
    /**
     * 反序列化消息内容
     */
    private List<MessageContent> deserializeMessageContent(String contentJson) {
        try {
            if (contentJson == null || contentJson.trim().isEmpty()) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(contentJson, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, MessageContent.class));
        } catch (JsonProcessingException e) {
            logger.warn("反序列化消息内容失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 反序列化元数据
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> deserializeMetadata(String metadataJson) {
        try {
            if (metadataJson == null || metadataJson.trim().isEmpty()) {
                return null;
            }
            return objectMapper.readValue(metadataJson, Map.class);
        } catch (JsonProcessingException e) {
            logger.warn("反序列化元数据失败", e);
            return null;
        }
    }
    
    /**
     * 将SessionMessageEntity转换为Message
     */
    private Message convertToMessage(SessionMessageEntity entity) {
        Message message = new Message();
        message.setType(entity.getMessageType());
        message.setContent(deserializeMessageContent(entity.getContent()));
        message.setMetadata(deserializeMetadata(entity.getMetadata()));
        return message;
    }
}
