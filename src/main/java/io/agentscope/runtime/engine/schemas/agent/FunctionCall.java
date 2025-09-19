package io.agentscope.runtime.engine.schemas.agent;

import java.util.HashMap;
import java.util.Map;

/**
 * 函数调用类
 * 对应Python版本的agent_schemas.py中的FunctionCall类
 */
public class FunctionCall {
    
    private String callId;
    private String name;
    private String arguments;
    
    public FunctionCall() {}
    
    public FunctionCall(String callId, String name, String arguments) {
        this.callId = callId;
        this.name = name;
        this.arguments = arguments;
    }
    
    public String getCallId() {
        return callId;
    }
    
    public void setCallId(String callId) {
        this.callId = callId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getArguments() {
        return arguments;
    }
    
    public void setArguments(String arguments) {
        this.arguments = arguments;
    }
    
    /**
     * 转换为Map格式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("call_id", callId);
        map.put("name", name);
        map.put("arguments", arguments);
        return map;
    }
    
    /**
     * 从Map创建FunctionCall
     */
    public static FunctionCall fromMap(Map<String, Object> map) {
        FunctionCall functionCall = new FunctionCall();
        functionCall.setCallId((String) map.get("call_id"));
        functionCall.setName((String) map.get("name"));
        functionCall.setArguments((String) map.get("arguments"));
        return functionCall;
    }
}
