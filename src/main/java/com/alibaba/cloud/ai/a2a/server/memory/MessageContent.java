package com.alibaba.cloud.ai.a2a.server.memory;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 消息内容类
 */
public class MessageContent {
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("text")
    private String text;
    
    public MessageContent() {}
    
    public MessageContent(String type, String text) {
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
