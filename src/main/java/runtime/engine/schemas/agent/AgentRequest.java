package runtime.engine.schemas.agent;

import java.util.List;
import java.util.Map;

/**
 * Agent请求类
 * 对应Python版本的agent_schemas.py中的AgentRequest类
 */
public class AgentRequest {
    
    private List<Message> input;
    private boolean stream = true;
    private String model;
    private Double topP;
    private Double temperature;
    private Double frequencyPenalty;
    private Double presencePenalty;
    private Integer maxTokens;
    private Object stop; // String or List<String>
    private Integer n = 1;
    private Integer seed;
    private List<Object> tools; // List<Tool> or List<Map>
    private String sessionId;
    private String responseId;
    
    public AgentRequest() {}
    
    public AgentRequest(List<Message> input) {
        this.input = input;
    }
    
    public List<Message> getInput() {
        return input;
    }
    
    public void setInput(List<Message> input) {
        this.input = input;
    }
    
    public boolean isStream() {
        return stream;
    }
    
    public void setStream(boolean stream) {
        this.stream = stream;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public Double getTopP() {
        return topP;
    }
    
    public void setTopP(Double topP) {
        this.topP = topP;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }
    
    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }
    
    public Double getPresencePenalty() {
        return presencePenalty;
    }
    
    public void setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
    }
    
    public Integer getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public Object getStop() {
        return stop;
    }
    
    public void setStop(Object stop) {
        this.stop = stop;
    }
    
    public Integer getN() {
        return n;
    }
    
    public void setN(Integer n) {
        this.n = n;
    }
    
    public Integer getSeed() {
        return seed;
    }
    
    public void setSeed(Integer seed) {
        this.seed = seed;
    }
    
    public List<Object> getTools() {
        return tools;
    }
    
    public void setTools(List<Object> tools) {
        this.tools = tools;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getResponseId() {
        return responseId;
    }
    
    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }
}
