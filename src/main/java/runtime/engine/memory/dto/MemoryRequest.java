package runtime.engine.memory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 记忆请求DTO
 */
public class MemoryRequest {
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("message_type")
    private String messageType = "USER";
    
    public MemoryRequest() {}
    
    public MemoryRequest(String userId, String sessionId, String message) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.message = message;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
