package runtime.engine.agents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import runtime.engine.schemas.agent.Event;
import runtime.engine.schemas.context.Context;
import runtime.engine.agents.config.AgentConfig;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BaseAgent测试类
 */
@ExtendWith(MockitoExtension.class)
class BaseAgentTest {
    
    @Mock
    private Context mockContext;
    
    private TestAgent testAgent;
    
    @BeforeEach
    void setUp() {
        testAgent = new TestAgent("test-agent", "Test agent description");
    }
    
    @Test
    void testGetName() {
        assertEquals("test-agent", testAgent.getName());
    }
    
    @Test
    void testGetDescription() {
        assertEquals("Test agent description", testAgent.getDescription());
    }
    
    @Test
    void testRunAsync() {
        // 准备测试数据
        when(mockContext.getUserId()).thenReturn("user123");
        
        // 执行测试
        CompletableFuture<Flux<Event>> future = testAgent.runAsync(mockContext);
        
        // 验证结果
        assertNotNull(future);
        assertDoesNotThrow(() -> {
            Flux<Event> eventFlux = future.get();
            assertNotNull(eventFlux);
        });
    }
    
    @Test
    void testSetBeforeCallback() {
        AgentCallback callback = context -> System.out.println("Before callback");
        
        testAgent.setBeforeCallback(callback);
        
        // 验证回调已设置
        assertTrue(testAgent.getBeforeCallbacks().contains(callback));
    }
    
    @Test
    void testSetAfterCallback() {
        AgentCallback callback = context -> System.out.println("After callback");
        
        testAgent.setAfterCallback(callback);
        
        // 验证回调已设置
        assertTrue(testAgent.getAfterCallbacks().contains(callback));
    }
    
    @Test
    void testGetConfig() {
        AgentConfig config = testAgent.getConfig();
        assertNotNull(config);
    }
    
    @Test
    void testCopy() {
        Agent copiedAgent = testAgent.copy();
        
        assertNotNull(copiedAgent);
        assertEquals(testAgent.getName(), copiedAgent.getName());
        assertEquals(testAgent.getDescription(), copiedAgent.getDescription());
    }
    
    /**
     * 测试用的Agent实现
     */
    private static class TestAgent extends BaseAgent {
        
        public TestAgent(String name, String description) {
            super(name, description, null, null, new AgentConfig());
        }
        
        @Override
        protected Flux<Event> execute(Context context) {
            return Flux.just(new Event().inProgress(), new Event().completed());
        }
        
        @Override
        public TestAgent copy() {
            return new TestAgent(getName(), getDescription());
        }
        
        // 用于测试的getter方法
        public java.util.List<AgentCallback> getBeforeCallbacks() {
            return beforeCallbacks;
        }
        
        public java.util.List<AgentCallback> getAfterCallbacks() {
            return afterCallbacks;
        }
    }
}
