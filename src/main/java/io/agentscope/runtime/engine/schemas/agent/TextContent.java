package io.agentscope.runtime.engine.schemas.agent;

/**
 * 文本内容类
 * 对应Python版本的agent_schemas.py中的TextContent类
 */
public class TextContent extends Content {
    
    private String text;
    
    public TextContent() {
        super(ContentType.TEXT);
    }
    
    public TextContent(String text) {
        super(ContentType.TEXT);
        this.text = text;
    }
    
    public TextContent(Boolean delta, String text, Integer index) {
        super(ContentType.TEXT);
        this.setDelta(delta);
        this.text = text;
        this.setIndex(index);
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
}
