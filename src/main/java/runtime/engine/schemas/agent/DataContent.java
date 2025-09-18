package runtime.engine.schemas.agent;

import java.util.Map;

/**
 * 数据内容类
 * 对应Python版本的agent_schemas.py中的DataContent类
 */
public class DataContent extends Content {
    
    private Map<String, Object> data;
    
    public DataContent() {
        super(ContentType.DATA);
    }
    
    public DataContent(Map<String, Object> data) {
        super(ContentType.DATA);
        this.data = data;
    }
    
    public DataContent(Boolean delta, Map<String, Object> data, Integer index) {
        super(ContentType.DATA);
        this.setDelta(delta);
        this.data = data;
        this.setIndex(index);
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
