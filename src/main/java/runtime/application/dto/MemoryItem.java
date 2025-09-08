package runtime.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * 记忆项DTO
 */
public class MemoryItem {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("content")
    private List<ContentItem> content;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    public MemoryItem() {
        this.timestamp = System.currentTimeMillis();
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
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public List<ContentItem> getContent() {
        return content;
    }
    
    public void setContent(List<ContentItem> content) {
        this.content = content;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
