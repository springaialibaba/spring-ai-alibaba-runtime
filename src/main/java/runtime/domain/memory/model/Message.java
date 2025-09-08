package runtime.domain.memory.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * 消息类，表示对话中的一条消息
 */
public class Message {
    
    @JsonProperty("type")
    private MessageType type;
    
    @JsonProperty("content")
    private List<MessageContent> content;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    public Message() {}
    
    public Message(MessageType type, List<MessageContent> content) {
        this.type = type;
        this.content = content;
    }
    
    public Message(MessageType type, List<MessageContent> content, Map<String, Object> metadata) {
        this.type = type;
        this.content = content;
        this.metadata = metadata;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public List<MessageContent> getContent() {
        return content;
    }
    
    public void setContent(List<MessageContent> content) {
        this.content = content;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
