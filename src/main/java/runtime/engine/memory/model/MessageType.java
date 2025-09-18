package runtime.engine.memory.model;

/**
 * 消息类型枚举
 * 扩展了Agent相关的消息类型
 */
public enum MessageType {
    MESSAGE,
    SYSTEM,
    USER,
    ASSISTANT,
    FUNCTION_CALL,
    FUNCTION_CALL_OUTPUT,
    PLUGIN_CALL,
    PLUGIN_CALL_OUTPUT,
    COMPONENT_CALL,
    COMPONENT_CALL_OUTPUT,
    MCP_LIST_TOOLS,
    MCP_APPROVAL_REQUEST,
    MCP_TOOL_CALL,
    MCP_APPROVAL_RESPONSE,
    HEARTBEAT,
    ERROR
}
