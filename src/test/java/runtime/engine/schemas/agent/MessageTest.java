package runtime.engine.schemas.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import runtime.engine.memory.model.MessageType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Message测试类
 */
class MessageTest {
    
    private Message message;
    
    @BeforeEach
    void setUp() {
        message = new Message();
    }
    
    @Test
    void testMessageCreation() {
        assertNotNull(message.getId());
        assertTrue(message.getId().startsWith("msg_"));
        assertEquals(MessageType.MESSAGE.name(), message.getType());
        assertEquals("message", message.getObject());
        assertEquals(RunStatus.CREATED, message.getStatus());
    }
    
    @Test
    void testMessageWithTypeAndRole() {
        Message msg = new Message(MessageType.PLUGIN_CALL.name(), "assistant");
        
        assertEquals(MessageType.PLUGIN_CALL.name(), msg.getType());
        assertEquals("assistant", msg.getRole());
    }
    
    @Test
    void testGetTextContent() {
        TextContent textContent = new TextContent("Hello, world!");
        message.setContent(List.of(textContent));
        
        String text = message.getTextContent();
        assertEquals("Hello, world!", text);
    }
    
    @Test
    void testGetTextContentWithNoText() {
        DataContent dataContent = new DataContent();
        message.setContent(List.of(dataContent));
        
        String text = message.getTextContent();
        assertNull(text);
    }
    
    @Test
    void testGetImageContent() {
        ImageContent imageContent = new ImageContent("https://example.com/image.jpg");
        message.setContent(List.of(imageContent));
        
        List<String> images = message.getImageContent();
        assertEquals(1, images.size());
        assertEquals("https://example.com/image.jpg", images.get(0));
    }
    
    @Test
    void testAddDeltaContent() {
        TextContent textContent = new TextContent(true, "Hello", null);
        
        Content result = message.addDeltaContent(textContent);
        
        assertNotNull(result);
        assertEquals(0, result.getIndex());
        assertEquals(message.getId(), result.getMsgId());
        assertEquals(RunStatus.IN_PROGRESS, result.getStatus());
        
        // 验证内容已添加到消息中
        assertEquals(1, message.getContent().size());
    }
    
    @Test
    void testAddContent() {
        TextContent textContent = new TextContent("Hello, world!");
        
        Content result = message.addContent(textContent);
        
        assertNotNull(result);
        assertEquals(0, result.getIndex());
        assertEquals(message.getId(), result.getMsgId());
        assertEquals(RunStatus.COMPLETED, result.getStatus());
        
        // 验证内容已添加到消息中
        assertEquals(1, message.getContent().size());
    }
    
    @Test
    void testContentCompleted() {
        TextContent textContent = new TextContent("Hello, world!");
        message.setContent(List.of(textContent));
        
        Content result = message.contentCompleted(0);
        
        assertNotNull(result);
        assertEquals(0, result.getIndex());
        assertEquals(message.getId(), result.getMsgId());
        assertEquals(RunStatus.COMPLETED, result.getStatus());
        assertFalse(result.getDelta());
    }
    
    @Test
    void testFromOpenAiMessage() {
        Map<String, Object> openAiMessage = Map.of(
            "role", "user",
            "content", "Hello, world!"
        );
        
        Message result = Message.fromOpenAiMessage(openAiMessage);
        
        assertNotNull(result);
        assertEquals("user", result.getRole());
        assertEquals(1, result.getContent().size());
        
        TextContent textContent = (TextContent) result.getContent().get(0);
        assertEquals("Hello, world!", textContent.getText());
    }
    
    @Test
    void testFromOpenAiMessageWithToolCall() {
        Map<String, Object> function = Map.of(
            "name", "test_function",
            "arguments", "{\"param\": \"value\"}"
        );
        
        Map<String, Object> toolCall = Map.of(
            "id", "call_123",
            "type", "function",
            "function", function
        );
        
        Map<String, Object> openAiMessage = Map.of(
            "role", "assistant",
            "tool_calls", List.of(toolCall)
        );
        
        Message result = Message.fromOpenAiMessage(openAiMessage);
        
        assertNotNull(result);
        assertEquals(MessageType.FUNCTION_CALL.name(), result.getType());
        assertEquals(1, result.getContent().size());
        
        DataContent dataContent = (DataContent) result.getContent().get(0);
        Map<String, Object> data = dataContent.getData();
        assertEquals("call_123", data.get("call_id"));
        assertEquals("test_function", data.get("name"));
        assertEquals("{\"param\": \"value\"}", data.get("arguments"));
    }
}
