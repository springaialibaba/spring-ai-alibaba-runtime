package runtime.engine.schemas.context;

import runtime.engine.schemas.agent.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * 会话类
 * 对应Python版本的session_history_service.py中的Session类
 */
public class Session {
    
    private String id;
    private String userId;
    private List<Message> messages;
    
    public Session() {
        this.messages = new ArrayList<>();
    }
    
    public Session(String id, String userId) {
        this();
        this.id = id;
        this.userId = userId;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
    }
    
    /**
     * 添加消息
     */
    public void addMessage(Message message) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(message);
    }
    
    /**
     * 获取最后一条消息
     */
    public Message getLastMessage() {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1);
    }
    
    /**
     * 获取消息数量
     */
    public int getMessageCount() {
        return messages != null ? messages.size() : 0;
    }
}
