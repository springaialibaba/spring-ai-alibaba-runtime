package runtime.engine.agents;

import reactor.core.publisher.Flux;
import runtime.engine.schemas.context.Context;
import runtime.engine.schemas.agent.Event;
import runtime.engine.agents.config.AgentConfig;

import java.util.concurrent.CompletableFuture;

/**
 * Agent接口定义
 * 对应Python版本的base_agent.py中的Agent类
 */
public interface Agent {
    
    /**
     * 获取Agent名称
     */
    String getName();
    
    /**
     * 获取Agent描述
     */
    String getDescription();
    
    /**
     * 异步执行Agent
     * 对应Python版本的run_async方法
     * 
     * @param context 执行上下文
     * @return 事件流
     */
    CompletableFuture<Flux<Event>> runAsync(Context context);
    
    /**
     * 设置执行前回调
     * 对应Python版本的before_agent_callback
     */
    void setBeforeCallback(AgentCallback callback);
    
    /**
     * 设置执行后回调
     * 对应Python版本的after_agent_callback
     */
    void setAfterCallback(AgentCallback callback);
    
    /**
     * 获取Agent配置
     * 对应Python版本的agent_config
     */
    AgentConfig getConfig();
    
    /**
     * 复制Agent实例
     * 对应Python版本的copy方法
     */
    Agent copy();
}
