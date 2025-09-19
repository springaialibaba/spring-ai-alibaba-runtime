package io.agentscope.runtime.engine.schemas.agent;

/**
 * 图像内容类
 * 对应Python版本的agent_schemas.py中的ImageContent类
 */
public class ImageContent extends Content {
    
    private String imageUrl;
    
    public ImageContent() {
        super(ContentType.IMAGE);
    }
    
    public ImageContent(String imageUrl) {
        super(ContentType.IMAGE);
        this.imageUrl = imageUrl;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
