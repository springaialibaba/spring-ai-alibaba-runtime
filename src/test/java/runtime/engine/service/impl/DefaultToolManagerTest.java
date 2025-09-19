package runtime.engine.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import runtime.engine.service.ToolAdapter;
import runtime.engine.service.ToolInfo;
import runtime.engine.service.ToolManager;
import runtime.engine.schemas.agent.FunctionCall;
import runtime.engine.schemas.agent.FunctionCallOutput;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DefaultToolManager测试类
 */
@ExtendWith(MockitoExtension.class)
class DefaultToolManagerTest {
    
    @Mock
    private ToolAdapter mockToolAdapter;
    
    private ToolManager toolManager;
    
    @BeforeEach
    void setUp() {
        toolManager = new DefaultToolManager();
        
        // 设置mock工具适配器
        when(mockToolAdapter.getName()).thenReturn("test_tool");
        when(mockToolAdapter.getDescription()).thenReturn("Test tool description");
        when(mockToolAdapter.getSchema()).thenReturn(Map.of(
            "type", "object",
            "properties", Map.of(
                "param1", Map.of("type", "string"),
                "param2", Map.of("type", "integer")
            )
        ));
    }
    
    @Test
    void testRegisterTool() {
        // 执行测试
        toolManager.registerTool("test_tool", mockToolAdapter);
        
        // 验证结果
        assertTrue(toolManager.toolExists("test_tool"));
        
        ToolInfo toolInfo = toolManager.getToolInfo("test_tool");
        assertNotNull(toolInfo);
        assertEquals("test_tool", toolInfo.getName());
        assertEquals("Test tool description", toolInfo.getDescription());
        assertEquals("mcp_server", toolInfo.getSource());
        assertEquals("basic", toolInfo.getGroup());
    }
    
    @Test
    void testRegisterToolWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            toolManager.registerTool(null, mockToolAdapter);
        });
    }
    
    @Test
    void testRegisterToolWithNullAdapter() {
        assertThrows(IllegalArgumentException.class, () -> {
            toolManager.registerTool("test_tool", null);
        });
    }
    
    @Test
    void testUnregisterTool() {
        // 先注册工具
        toolManager.registerTool("test_tool", mockToolAdapter);
        assertTrue(toolManager.toolExists("test_tool"));
        
        // 取消注册
        toolManager.unregisterTool("test_tool");
        
        // 验证结果
        assertFalse(toolManager.toolExists("test_tool"));
        assertNull(toolManager.getToolInfo("test_tool"));
    }
    
    @Test
    void testExecuteTool() {
        // 准备测试数据
        FunctionCall functionCall = new FunctionCall("call_123", "test_tool", "{\"param1\": \"value1\"}");
        FunctionCallOutput expectedOutput = new FunctionCallOutput("call_123", "Tool executed successfully");
        
        when(mockToolAdapter.execute(functionCall))
            .thenReturn(CompletableFuture.completedFuture(expectedOutput));
        
        // 注册工具
        toolManager.registerTool("test_tool", mockToolAdapter);
        
        // 执行测试
        CompletableFuture<FunctionCallOutput> future = toolManager.executeTool(functionCall);
        
        // 验证结果
        assertDoesNotThrow(() -> {
            FunctionCallOutput result = future.get();
            assertNotNull(result);
            assertEquals("call_123", result.getCallId());
            assertEquals("Tool executed successfully", result.getOutput());
        });
        
        verify(mockToolAdapter).execute(functionCall);
    }
    
    @Test
    void testExecuteToolNotFound() {
        FunctionCall functionCall = new FunctionCall("call_123", "nonexistent_tool", "{}");
        
        CompletableFuture<FunctionCallOutput> future = toolManager.executeTool(functionCall);
        
        assertThrows(Exception.class, () -> {
            future.get();
        });
    }
    
    @Test
    void testExecuteToolWithNullFunctionCall() {
        CompletableFuture<FunctionCallOutput> future = toolManager.executeTool(null);
        
        assertThrows(Exception.class, () -> {
            future.get();
        });
    }
    
    @Test
    void testGetAvailableTools() {
        // 注册多个工具
        toolManager.registerTool("tool1", mockToolAdapter);
        toolManager.registerTool("tool2", mockToolAdapter);
        
        List<ToolInfo> tools = toolManager.getAvailableTools();
        
        assertEquals(2, tools.size());
        assertTrue(tools.stream().anyMatch(t -> "tool1".equals(t.getName())));
        assertTrue(tools.stream().anyMatch(t -> "tool2".equals(t.getName())));
    }
    
    @Test
    void testGetToolSchema() {
        toolManager.registerTool("test_tool", mockToolAdapter);
        
        Map<String, Object> schema = toolManager.getToolSchema("test_tool");
        
        assertNotNull(schema);
        assertEquals("object", schema.get("type"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("param1"));
        assertTrue(properties.containsKey("param2"));
    }
    
    @Test
    void testGetAllToolSchemas() {
        toolManager.registerTool("tool1", mockToolAdapter);
        toolManager.registerTool("tool2", mockToolAdapter);
        
        Map<String, Map<String, Object>> schemas = toolManager.getAllToolSchemas();
        
        assertEquals(2, schemas.size());
        assertTrue(schemas.containsKey("tool1"));
        assertTrue(schemas.containsKey("tool2"));
    }
}
