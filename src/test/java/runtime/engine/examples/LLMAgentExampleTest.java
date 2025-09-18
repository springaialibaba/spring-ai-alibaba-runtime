package runtime.engine.examples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import runtime.engine.Runner;
import runtime.engine.agents.llm.LLMAgent;
import runtime.engine.agents.config.AgentConfig;
import runtime.engine.llms.QwenLLM;
import runtime.engine.memory.context.ContextManager;
import runtime.engine.schemas.agent.AgentRequest;
import runtime.engine.schemas.agent.Content;
import runtime.engine.schemas.agent.Event;
import runtime.engine.schemas.agent.Message;
import runtime.engine.schemas.agent.TextContent;
import runtime.engine.memory.model.MessageType;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * LLM智能体示例测试
 */
public class LLMAgentExampleTest {
    
    private LLMAgent llmAgent;
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        // 创建LLM实例
        QwenLLM model = new QwenLLM("qwen-turbo", "test_api_key");
        
        // 创建LLM智能体
        llmAgent = new LLMAgent(model, "test_agent", "Test LLM agent", new AgentConfig());
        
        // 创建上下文管理器
        contextManager = new ContextManager();
    }
    
    @Test
    void testLLMAgentCreation() {
        assertNotNull(llmAgent);
        assertEquals("test_agent", llmAgent.getName());
        assertEquals("Test LLM agent", llmAgent.getDescription());
    }
    
    @Test
    void testRunnerCreation() {
        try (Runner runner = new Runner(llmAgent, contextManager)) {
            assertNotNull(runner);
        }
    }
    
    @Test
    void testStreamQuery() {
        try (Runner runner = new Runner(llmAgent, contextManager)) {
            // 创建请求
            AgentRequest request = createTestRequest();
            
            // 执行流式查询
            List<Event> events = runner.streamQuery(request)
                .collectList()
                .block();
            
            assertNotNull(events);
            assertFalse(events.isEmpty());
            
            // 检查最后一个事件是否是完成的消息
            Event lastEvent = events.get(events.size() - 1);
            assertTrue(lastEvent instanceof Message);
            
            Message lastMessage = (Message) lastEvent;
            assertEquals(MessageType.MESSAGE.name(), lastMessage.getType());
            assertEquals("completed", lastMessage.getStatus());
        }
    }
    
    @Test
    void testQuery() {
        try (Runner runner = new Runner(llmAgent, contextManager)) {
            // 创建请求
            AgentRequest request = createTestRequest();
            
            // 执行查询
            List<Event> events = runner.streamQuery(request).collectList().block();
            Event result = events != null && !events.isEmpty() ? events.get(events.size() - 1) : null;
            
            assertNotNull(result);
            assertTrue(result instanceof Message);
            
            Message message = (Message) result;
            assertEquals(MessageType.MESSAGE.name(), message.getType());
            assertEquals("completed", message.getStatus());
        }
    }
    
    @Test
    void testAgentCopy() {
        LLMAgent copiedAgent = llmAgent.copy();
        
        assertNotNull(copiedAgent);
        assertEquals(llmAgent.getName(), copiedAgent.getName());
        assertEquals(llmAgent.getDescription(), copiedAgent.getDescription());
        assertNotSame(llmAgent, copiedAgent);
    }
    
    /**
     * 创建测试请求
     */
    private AgentRequest createTestRequest() {
        Message userMessage = new Message();
        userMessage.setRole("user");
        
        List<Content> content = new ArrayList<>();
        TextContent textContent = new TextContent();
        textContent.setText("你好");
        content.add(textContent);
        
        userMessage.setContent(content);
        
        AgentRequest request = new AgentRequest();
        request.setInput(List.of(userMessage));
        
        return request;
    }
}
