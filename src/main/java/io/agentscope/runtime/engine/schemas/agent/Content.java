package io.agentscope.runtime.engine.schemas.agent;

import java.util.Map;

/**
 * 内容基类
 * 对应Python版本的agent_schemas.py中的Content类
 */
public abstract class Content extends Event {
    
    private String type;
    private String object = "content";
    private Integer index;
    private Boolean delta = false;
    private String msgId;
    
    public Content() {
        super();
    }
    
    public Content(String type) {
        super();
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public String getObject() {
        return object;
    }
    
    @Override
    public void setObject(String object) {
        this.object = object;
    }
    
    public Integer getIndex() {
        return index;
    }
    
    public void setIndex(Integer index) {
        this.index = index;
    }
    
    public Boolean getDelta() {
        return delta;
    }
    
    public void setDelta(Boolean delta) {
        this.delta = delta;
    }
    
    public String getMsgId() {
        return msgId;
    }
    
    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
    
    /**
     * 从聊天完成块创建内容
     * 对应Python版本的from_chat_completion_chunk方法
     */
    public static Content fromChatCompletionChunk(Map<String, Object> chunk, Integer index) {
        // 这里需要根据具体的聊天完成块格式来实现
        // 暂时返回null，需要根据实际需求实现
        return null;
    }
}
