package runtime.engine.memory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 内容项DTO
 */
public class ContentItem {
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("text")
    private String text;
    
    public ContentItem() {}
    
    public ContentItem(String type, String text) {
        this.type = type;
        this.text = text;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
}
