package runtime.engine.memory.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 会话类，表示一个对话会话
 * 包含对话历史，包括所有消息，由ID唯一标识
 */
public class Session {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("messages")
    private List<Message> messages;
    
    public Session() {}
    
    public Session(String id, String userId) {
        this.id = id;
        this.userId = userId;
        this.messages = List.of();
    }
    
    public Session(String id, String userId, List<Message> messages) {
        this.id = id;
        this.userId = userId;
        this.messages = messages;
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
        this.messages = messages;
    }
}
