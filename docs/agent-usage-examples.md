# Agent 使用示例

## 快速开始

### 1. 基本Agent创建

```java
import runtime.engine.agents.BaseAgent;
import runtime.engine.schemas.context.Context;
import runtime.engine.schemas.agent.Event;
import reactor.core.publisher.Flux;

public class SimpleAgent extends BaseAgent {
    
    public SimpleAgent() {
        super("simple-agent", "A simple agent example", null, null, new AgentConfig());
    }
    
    @Override
    protected Flux<Event> execute(Context context) {
        return Flux.create(sink -> {
            try {
                // 获取用户输入
                String userInput = context.getNewMessage().getTextContent();
                
                // 生成简单响应
                String response = "Hello! You said: " + userInput;
                
                // 创建响应消息
                Message responseMessage = new Message();
                responseMessage.setRole(Role.ASSISTANT);
                responseMessage.setType(MessageType.MESSAGE);
                
                TextContent textContent = new TextContent(response);
                responseMessage.addContent(textContent);
                
                // 发送事件流
                sink.next(responseMessage.inProgress());
                sink.next(responseMessage.completed());
                sink.complete();
                
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
}
```

### 2. 使用Agent

```java
@Service
public class AgentRunnerService {
    
    @Autowired
    private ContextManager contextManager;
    
    public Flux<Event> runSimpleAgent(String userId, String sessionId, String userInput) {
        // 创建上下文
        Context context = contextManager.createContext(userId, sessionId);
        
        // 创建用户消息
        Message userMessage = new Message();
        userMessage.setRole(Role.USER);
        userMessage.setType(MessageType.MESSAGE);
        userMessage.addContent(new TextContent(userInput));
        
        // 设置到上下文
        context.setNewMessage(userMessage);
        
        // 创建并运行Agent
        Agent agent = new SimpleAgent();
        context.setAgent(agent);
        
        return agent.runAsync(context)
            .thenApply(Flux::from)
            .orElse(Flux.empty());
    }
}
```

## 高级示例

### 1. 带工具集成的Agent

```java
public class ToolEnabledAgent extends BaseAgent {
    
    @Autowired
    private ToolManager toolManager;
    
    public ToolEnabledAgent() {
        super("tool-agent", "Agent with tool integration", null, null, new AgentConfig());
    }
    
    @Override
    protected Flux<Event> execute(Context context) {
        return Flux.create(sink -> {
            try {
                String userInput = context.getNewMessage().getTextContent();
                
                // 检查是否需要工具调用
                if (userInput.contains("calculate")) {
                    // 执行工具调用
                    FunctionCall functionCall = new FunctionCall(
                        "call_" + System.currentTimeMillis(),
                        "calculator",
                        "{\"expression\": \"" + extractExpression(userInput) + "\"}"
                    );
                    
                    toolManager.executeTool(functionCall)
                        .thenAccept(output -> {
                            // 创建工具调用消息
                            Message toolCallMessage = new Message();
                            toolCallMessage.setType(MessageType.PLUGIN_CALL);
                            toolCallMessage.setRole(Role.ASSISTANT);
                            
                            DataContent dataContent = new DataContent(functionCall.toMap());
                            toolCallMessage.addContent(dataContent);
                            
                            sink.next(toolCallMessage.completed());
                            
                            // 创建工具结果消息
                            Message toolResultMessage = new Message();
                            toolResultMessage.setType(MessageType.PLUGIN_CALL_OUTPUT);
                            toolResultMessage.setRole(Role.ASSISTANT);
                            
                            DataContent resultContent = new DataContent(output.toMap());
                            toolResultMessage.addContent(resultContent);
                            
                            sink.next(toolResultMessage.completed());
                        })
                        .exceptionally(throwable -> {
                            sink.error(throwable);
                            return null;
                        });
                } else {
                    // 普通响应
                    String response = "I can help you with calculations. Try asking me to calculate something!";
                    
                    Message responseMessage = new Message();
                    responseMessage.setRole(Role.ASSISTANT);
                    responseMessage.setType(MessageType.MESSAGE);
                    responseMessage.addContent(new TextContent(response));
                    
                    sink.next(responseMessage.inProgress());
                    sink.next(responseMessage.completed());
                    sink.complete();
                }
                
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
    
    private String extractExpression(String input) {
        // 简单的表达式提取逻辑
        return input.replaceAll(".*calculate\\s+", "").replaceAll("\\?.*", "");
    }
}
```

### 2. 流式响应Agent

```java
public class StreamingAgent extends BaseAgent {
    
    public StreamingAgent() {
        super("streaming-agent", "Agent with streaming response", null, null, new AgentConfig());
    }
    
    @Override
    protected Flux<Event> execute(Context context) {
        return Flux.create(sink -> {
            try {
                String userInput = context.getNewMessage().getTextContent();
                
                // 创建初始消息
                Message responseMessage = new Message();
                responseMessage.setRole(Role.ASSISTANT);
                responseMessage.setType(MessageType.MESSAGE);
                
                sink.next(responseMessage.inProgress());
                
                // 模拟流式生成响应
                String fullResponse = generateLongResponse(userInput);
                String[] words = fullResponse.split(" ");
                
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    if (i > 0) word = " " + word;
                    
                    TextContent deltaContent = new TextContent(true, word, null);
                    Content result = responseMessage.addDeltaContent(deltaContent);
                    
                    if (result != null) {
                        sink.next(result);
                    }
                    
                    // 模拟延迟
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                // 完成消息
                sink.next(responseMessage.completed());
                sink.complete();
                
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
    
    private String generateLongResponse(String input) {
        return "This is a long response to your input: " + input + 
               ". I'm generating this response word by word to demonstrate streaming capabilities. " +
               "Each word will be sent as a separate delta event, creating a smooth streaming experience.";
    }
}
```

### 3. 多模态Agent

```java
public class MultimodalAgent extends BaseAgent {
    
    public MultimodalAgent() {
        super("multimodal-agent", "Agent supporting text and images", null, null, new AgentConfig());
    }
    
    @Override
    protected Flux<Event> execute(Context context) {
        return Flux.create(sink -> {
            try {
                Message userMessage = context.getNewMessage();
                
                // 处理文本内容
                String textContent = userMessage.getTextContent();
                
                // 处理图像内容
                List<String> imageUrls = userMessage.getImageContent();
                
                Message responseMessage = new Message();
                responseMessage.setRole(Role.ASSISTANT);
                responseMessage.setType(MessageType.MESSAGE);
                
                if (textContent != null && !textContent.isEmpty()) {
                    String textResponse = "I received your text: " + textContent;
                    responseMessage.addContent(new TextContent(textResponse));
                }
                
                if (!imageUrls.isEmpty()) {
                    String imageResponse = "I also received " + imageUrls.size() + " image(s). " +
                                         "I can analyze images and provide descriptions or answer questions about them.";
                    responseMessage.addContent(new TextContent(imageResponse));
                    
                    // 这里可以添加图像分析逻辑
                    for (String imageUrl : imageUrls) {
                        String analysis = analyzeImage(imageUrl);
                        responseMessage.addContent(new TextContent("Image analysis: " + analysis));
                    }
                }
                
                sink.next(responseMessage.inProgress());
                sink.next(responseMessage.completed());
                sink.complete();
                
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
    
    private String analyzeImage(String imageUrl) {
        // 模拟图像分析
        return "This appears to be an image. I can see various elements and can provide detailed analysis.";
    }
}
```

## 工具开发示例

### 1. 自定义工具

```java
@Component
public class WeatherTool implements ToolAdapter {
    
    @Override
    public CompletableFuture<FunctionCallOutput> execute(FunctionCall functionCall) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 解析参数
                String arguments = functionCall.getArguments();
                Map<String, Object> params = parseArguments(arguments);
                
                String city = (String) params.get("city");
                
                // 模拟天气查询
                String weather = getWeatherForCity(city);
                
                return new FunctionCallOutput(functionCall.getCallId(), weather);
                
            } catch (Exception e) {
                return new FunctionCallOutput(functionCall.getCallId(), 
                    "Error getting weather: " + e.getMessage());
            }
        });
    }
    
    @Override
    public Map<String, Object> getSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "city", Map.of(
                    "type", "string",
                    "description", "The city to get weather for"
                )
            ),
            "required", List.of("city")
        );
    }
    
    @Override
    public String getName() {
        return "get_weather";
    }
    
    @Override
    public String getDescription() {
        return "Get current weather information for a specified city";
    }
    
    @Override
    public boolean validateParameters(Map<String, Object> parameters) {
        return parameters.containsKey("city") && 
               parameters.get("city") instanceof String;
    }
    
    private Map<String, Object> parseArguments(String arguments) {
        // 简单的JSON解析
        // 在实际应用中，应该使用JSON库
        Map<String, Object> params = new HashMap<>();
        // 这里简化处理
        params.put("city", "Beijing");
        return params;
    }
    
    private String getWeatherForCity(String city) {
        // 模拟天气查询
        return "The weather in " + city + " is sunny with a temperature of 25°C";
    }
}
```

### 2. 工具注册

```java
@Configuration
public class ToolConfiguration {
    
    @Bean
    public ToolManager toolManager() {
        return new DefaultToolManager();
    }
    
    @PostConstruct
    public void registerTools(ToolManager toolManager, WeatherTool weatherTool) {
        toolManager.registerTool("get_weather", weatherTool);
    }
}
```

## 配置示例

### 1. Agent配置

```java
@Configuration
public class AgentConfiguration {
    
    @Bean
    public AgentConfig defaultAgentConfig() {
        AgentConfig config = new AgentConfig();
        config.setName("default-agent");
        config.set("temperature", 0.7);
        config.set("max_tokens", 1000);
        config.set("stream", true);
        return config;
    }
    
    @Bean
    public AgentConfig creativeAgentConfig() {
        AgentConfig config = new AgentConfig();
        config.setName("creative-agent");
        config.set("temperature", 1.0);
        config.set("max_tokens", 2000);
        config.set("top_p", 0.9);
        return config;
    }
}
```

### 2. 服务配置

```java
@Configuration
public class ServiceConfiguration {
    
    @Bean
    public EnvironmentManager environmentManager() {
        return new DefaultEnvironmentManager();
    }
    
    @Bean
    public ContextManager contextManager() {
        return new DefaultContextManager();
    }
    
    @Bean
    public ToolManager toolManager() {
        return new DefaultToolManager();
    }
}
```

## 错误处理示例

### 1. 异常处理

```java
public class RobustAgent extends BaseAgent {
    
    public RobustAgent() {
        super("robust-agent", "Agent with robust error handling", null, null, new AgentConfig());
    }
    
    @Override
    protected Flux<Event> execute(Context context) {
        return Flux.create(sink -> {
            try {
                // 验证输入
                if (context.getNewMessage() == null) {
                    Event errorEvent = new Event();
                    errorEvent.failed(new Error("INVALID_INPUT", "No message provided"));
                    sink.next(errorEvent);
                    sink.complete();
                    return;
                }
                
                String userInput = context.getNewMessage().getTextContent();
                if (userInput == null || userInput.trim().isEmpty()) {
                    Event errorEvent = new Event();
                    errorEvent.failed(new Error("EMPTY_INPUT", "Empty message provided"));
                    sink.next(errorEvent);
                    sink.complete();
                    return;
                }
                
                // 处理输入
                String response = processInput(userInput);
                
                // 创建响应
                Message responseMessage = new Message();
                responseMessage.setRole(Role.ASSISTANT);
                responseMessage.setType(MessageType.MESSAGE);
                responseMessage.addContent(new TextContent(response));
                
                sink.next(responseMessage.inProgress());
                sink.next(responseMessage.completed());
                sink.complete();
                
            } catch (Exception e) {
                // 记录错误
                System.err.println("Agent execution error: " + e.getMessage());
                
                // 返回错误事件
                Event errorEvent = new Event();
                errorEvent.failed(new Error("EXECUTION_ERROR", e.getMessage()));
                sink.next(errorEvent);
                sink.complete();
            }
        });
    }
    
    private String processInput(String input) {
        // 模拟处理逻辑
        if (input.length() > 1000) {
            throw new IllegalArgumentException("Input too long");
        }
        return "Processed: " + input;
    }
}
```

## 测试示例

### 1. 单元测试

```java
@ExtendWith(MockitoExtension.class)
class AgentTest {
    
    @Mock
    private Context mockContext;
    
    @Mock
    private Message mockMessage;
    
    @Test
    void testAgentExecution() {
        // 准备测试数据
        when(mockContext.getNewMessage()).thenReturn(mockMessage);
        when(mockMessage.getTextContent()).thenReturn("Hello, world!");
        
        // 创建Agent
        Agent agent = new SimpleAgent();
        
        // 执行测试
        CompletableFuture<Flux<Event>> future = agent.runAsync(mockContext);
        
        // 验证结果
        assertDoesNotThrow(() -> {
            Flux<Event> events = future.get();
            List<Event> eventList = events.collectList().block();
            
            assertNotNull(eventList);
            assertFalse(eventList.isEmpty());
            
            // 验证事件类型
            Event lastEvent = eventList.get(eventList.size() - 1);
            assertEquals(RunStatus.COMPLETED, lastEvent.getStatus());
        });
    }
}
```

这些示例展示了如何使用Java版本的Agent API来创建各种类型的Agent，包括基本Agent、工具集成Agent、流式响应Agent和多模态Agent。每个示例都包含了完整的代码和说明，可以作为开发的参考。
