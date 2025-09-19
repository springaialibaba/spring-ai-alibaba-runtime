package io.agentscope.runtime.engine.schemas.agent;

import java.lang.Error;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 事件基类
 * 对应Python版本的agent_schemas.py中的Event类
 */
public class Event {
    
    private Integer sequenceNumber;
    protected String object;
    protected String status;
    private java.lang.Error error;
    
    public Event() {
        this.status = RunStatus.CREATED;
    }
    
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }
    
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    public String getObject() {
        return object;
    }
    
    public void setObject(String object) {
        this.object = object;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public java.lang.Error getError() {
        return error;
    }
    
    public void setError(java.lang.Error error) {
        this.error = error;
    }
    
    /**
     * 设置为创建状态
     */
    public Event created() {
        this.status = RunStatus.CREATED;
        return this;
    }
    
    /**
     * 设置为进行中状态
     */
    public Event inProgress() {
        this.status = RunStatus.IN_PROGRESS;
        return this;
    }
    
    /**
     * 设置为完成状态
     */
    public Event completed() {
        this.status = RunStatus.COMPLETED;
        return this;
    }
    
    /**
     * 设置为失败状态
     */
    public Event failed(Error error) {
        this.status = RunStatus.FAILED;
        this.error = error;
        return this;
    }
    
    /**
     * 设置为拒绝状态
     */
    public Event rejected() {
        this.status = RunStatus.REJECTED;
        return this;
    }
    
    /**
     * 设置为取消状态
     */
    public Event canceled() {
        this.status = RunStatus.CANCELED;
        return this;
    }
}
