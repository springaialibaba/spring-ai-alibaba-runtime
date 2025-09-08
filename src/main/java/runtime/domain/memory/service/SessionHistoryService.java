package runtime.domain.memory.service;

import runtime.domain.memory.model.Message;
import runtime.domain.memory.model.Session;
import runtime.shared.common.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 会话历史管理服务接口
 * 定义创建、检索、更新和删除对话会话的标准接口
 */
public interface SessionHistoryService extends Service {
    
    /**
     * 为指定用户创建新会话
     * 
     * @param userId 用户标识符
     * @param sessionId 可选的会话ID，如果为null则自动生成
     * @return CompletableFuture<Session> 异步创建的新会话对象
     */
    CompletableFuture<Session> createSession(String userId, Optional<String> sessionId);
    
    /**
     * 检索特定会话
     * 
     * @param userId 用户标识符
     * @param sessionId 要检索的会话标识符
     * @return CompletableFuture<Optional<Session>> 异步检索结果，如果找到则返回会话对象，否则返回空
     */
    CompletableFuture<Optional<Session>> getSession(String userId, String sessionId);
    
    /**
     * 删除特定会话
     * 
     * @param userId 用户标识符
     * @param sessionId 要删除的会话标识符
     * @return CompletableFuture<Void> 异步删除结果
     */
    CompletableFuture<Void> deleteSession(String userId, String sessionId);
    
    /**
     * 列出指定用户的所有会话
     * 
     * @param userId 用户标识符
     * @return CompletableFuture<List<Session>> 异步会话列表结果
     */
    CompletableFuture<List<Session>> listSessions(String userId);
    
    /**
     * 向特定会话的历史记录追加消息
     * 
     * @param session 要追加消息的会话
     * @param messages 要追加的消息或消息列表
     * @return CompletableFuture<Void> 异步追加结果
     */
    CompletableFuture<Void> appendMessage(Session session, List<Message> messages);
}
