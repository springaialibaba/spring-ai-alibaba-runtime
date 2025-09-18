package runtime.engine.memory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 记忆响应DTO
 */
public class MemoryResponse {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private Object data;
    
    @JsonProperty("memories")
    private List<MemoryItem> memories;
    
    public MemoryResponse() {}
    
    public MemoryResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public MemoryResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public static MemoryResponse success(String message) {
        return new MemoryResponse(true, message);
    }
    
    public static MemoryResponse success(String message, Object data) {
        return new MemoryResponse(true, message, data);
    }
    
    public static MemoryResponse error(String message) {
        return new MemoryResponse(false, message);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public List<MemoryItem> getMemories() {
        return memories;
    }
    
    public void setMemories(List<MemoryItem> memories) {
        this.memories = memories;
    }
}
