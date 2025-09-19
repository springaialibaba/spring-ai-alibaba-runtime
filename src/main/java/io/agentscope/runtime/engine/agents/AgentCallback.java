package io.agentscope.runtime.engine.agents;

import io.agentscope.runtime.engine.schemas.context.Context;

/**
 * Agent回调接口
 * 对应Python版本中的before_agent_callback和after_agent_callback
 */
@FunctionalInterface
public interface AgentCallback {
    
    /**
     * 执行回调
     * 
     * @param context 执行上下文
     */
    void execute(Context context);
}
