# Agent API 文档

## 概述

本文档描述了Spring AI Alibaba Runtime中Agent相关的API，这些API对应Python版本的agentscope-runtime中的Agent功能。

## 核心接口

### Agent接口

`Agent`接口是所有Agent实现的基础接口，定义了Agent的基本行为。

```java
public interface Agent {
    String getName();
    String getDescription();
    CompletableFuture<Flux<Event>> runAsync(Context context);
    void setBeforeCallback(AgentCallback callback);
    void setAfterCallback(AgentCallback callback);
    AgentConfig getConfig();
    Agent copy();
}
```

### BaseAgent抽象类

`BaseAgent`提供了Agent的通用实现，包括回调机制和配置管理。

```java
public abstract class BaseAgent implements Agent {
    // 构造函数
    public BaseAgent(String name, String description, 
                    List<AgentCallback> beforeCallbacks, 
                    List<AgentCallback> afterCallbacks, 
                    AgentConfig config);
    
    // 子类必须实现的抽象方法
    protected abstract Flux<Event> execute(Context context);
}
```

## 数据模型

### Event类

`Event`是所有事件的基类，包含状态管理功能。

```java
public class Event {
    private Integer sequenceNumber;
    private String object;
    private String status;
    private Error error;
    
    // 状态管理方法
    public Event created();
    public Event inProgress();
    public Event completed();
    public Event failed(Error error);
    public Event rejected();
    public Event canceled();
}
```

### Message类

`Message`继承自`Event`，表示消息事件。

```java
public class Message extends Event {
    private String id;
    private String type;
    private String role;
    private List<Content> content;
    
    // 内容管理方法
    public String getTextContent();
    public List<String> getImageContent();
    public Content addDeltaContent(Content newContent);
    public Content addContent(Content newContent);
    public Content contentCompleted(int contentIndex);
    
    // 静态工厂方法
    public static Message fromOpenAiMessage(Map<String, Object> message);
}
```

### Content类

`Content`是消息内容的基类，支持多种内容类型。

```java
public abstract class Content extends Event {
    private String type;
    private Integer index;
    private Boolean delta;
    private String msgId;
}

// 具体实现
public class TextContent extends Content {
    private String text;
}

public class DataContent extends Content {
    private Map<String, Object> data;
}

public class ImageContent extends Content {
    private String imageUrl;
}
```

## 服务层

### EnvironmentManager

环境管理器负责管理运行时环境。

```java
public interface EnvironmentManager {
    String getEnvironmentVariable(String key);
    void setEnvironmentVariable(String key, String value);
    Map<String, String> getAllEnvironmentVariables();
    boolean isEnvironmentAvailable();
    CompletableFuture<Void> initializeEnvironment();
    CompletableFuture<Void> cleanupEnvironment();
    Map<String, Object> getEnvironmentInfo();
}
```

### ContextManager

上下文管理器负责管理Agent执行上下文。

```java
public interface ContextManager {
    Context createContext(String userId, String sessionId);
    Context getContext(String sessionId);
    CompletableFuture<Void> updateContext(Context context);
    CompletableFuture<Void> deleteContext(String sessionId);
    CompletableFuture<Void> addMessageToContext(String sessionId, Message message);
    List<Message> getContextMessages(String sessionId);
    CompletableFuture<Void> clearContext(String sessionId);
    boolean contextExists(String sessionId);
    List<String> getActiveContexts();
}
```

### ToolManager

工具管理器负责管理Agent可用的工具。

```java
public interface ToolManager {
    void registerTool(String name, ToolAdapter adapter);
    void unregisterTool(String name);
    CompletableFuture<FunctionCallOutput> executeTool(FunctionCall functionCall);
    List<ToolInfo> getAvailableTools();
    ToolInfo getToolInfo(String name);
    boolean toolExists(String name);
    Map<String, Object> getToolSchema(String name);
    Map<String, Map<String, Object>> getAllToolSchemas();
}
```

## 使用示例

### 创建自定义Agent

```java
public class MyCustomAgent extends BaseAgent {
    
    public MyCustomAgent(String name, String description) {
        super(name, description, null, null, new AgentConfig());
    }
    
    @Override
    protected Flux<Event> execute(Context context) {
        return Flux.create(sink -> {
            try {
                // 处理用户输入
                String userInput = context.getNewMessage().getTextContent();
                
                // 生成响应
                String response = generateResponse(userInput);
                
                // 创建响应消息
                Message responseMessage = new Message();
                responseMessage.setRole(Role.ASSISTANT);
                responseMessage.setType(MessageType.MESSAGE);
                
                TextContent textContent = new TextContent(response);
                responseMessage.addContent(textContent);
                
                // 发送事件
                sink.next(responseMessage.inProgress());
                sink.next(responseMessage.completed());
                sink.complete();
                
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
    
    private String generateResponse(String input) {
        // 实现响应生成逻辑
        return "Response to: " + input;
    }
}
```

### 使用Agent

```java
@Service
public class AgentService {
    
    @Autowired
    private ContextManager contextManager;
    
    @Autowired
    private EnvironmentManager environmentManager;
    
    public Flux<Event> runAgent(String userId, String sessionId, String userInput) {
        // 创建或获取上下文
        Context context = contextManager.createContext(userId, sessionId);
        
        // 创建用户消息
        Message userMessage = new Message();
        userMessage.setRole(Role.USER);
        userMessage.setType(MessageType.MESSAGE);
        userMessage.addContent(new TextContent(userInput));
        
        // 设置新消息
        context.setNewMessage(userMessage);
        
        // 创建Agent
        Agent agent = new MyCustomAgent("my-agent", "My custom agent");
        
        // 设置Agent到上下文
        context.setAgent(agent);
        
        // 运行Agent
        return agent.runAsync(context)
            .thenApply(Flux::from)
            .orElse(Flux.empty());
    }
}
```

### 注册和使用工具

```java
@Component
public class ToolRegistrationService {
    
    @Autowired
    private ToolManager toolManager;
    
    @PostConstruct
    public void registerTools() {
        // 注册计算器工具
        ToolAdapter calculatorTool = new ToolAdapter() {
            @Override
            public CompletableFuture<FunctionCallOutput> execute(FunctionCall functionCall) {
                return CompletableFuture.supplyAsync(() -> {
                    // 解析参数
                    String arguments = functionCall.getArguments();
                    // 执行计算逻辑
                    String result = performCalculation(arguments);
                    
                    return new FunctionCallOutput(functionCall.getCallId(), result);
                });
            }
            
            @Override
            public Map<String, Object> getSchema() {
                return Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "expression", Map.of("type", "string", "description", "Mathematical expression to evaluate")
                    ),
                    "required", List.of("expression")
                );
            }
            
            @Override
            public String getName() {
                return "calculator";
            }
            
            @Override
            public String getDescription() {
                return "Performs mathematical calculations";
            }
            
            @Override
            public boolean validateParameters(Map<String, Object> parameters) {
                return parameters.containsKey("expression");
            }
        };
        
        toolManager.registerTool("calculator", calculatorTool);
    }
    
    private String performCalculation(String expression) {
        // 实现计算逻辑
        return "Result: " + expression;
    }
}
```

## 配置

### AgentConfig

Agent配置类用于管理Agent的各种配置参数。

```java
public class AgentConfig {
    private Map<String, Object> config;
    
    // 常用配置方法
    public String getName();
    public void setName(String name);
    public Object getMemory();
    public void setMemory(Object memory);
    public Object getFormatter();
    public void setFormatter(Object formatter);
    public Object getToolkit();
    public void setToolkit(Object toolkit);
    
    // 通用配置方法
    public Object get(String key);
    public Object get(String key, Object defaultValue);
    public void set(String key, Object value);
    public Map<String, Object> getAll();
}
```

## 错误处理

### Error类

错误信息类用于表示执行过程中的错误。

```java
public class Error {
    private String code;
    private String message;
    
    public Error(String code, String message);
    // getter和setter方法
}
```

### 异常处理

Agent执行过程中的异常会被包装为`Event`并设置失败状态：

```java
try {
    // Agent执行逻辑
    Flux<Event> events = agent.execute(context);
    return events;
} catch (Exception e) {
    Event errorEvent = new Event();
    errorEvent.failed(new Error("EXECUTION_ERROR", e.getMessage()));
    return Flux.just(errorEvent);
}
```

## 最佳实践

1. **异步处理**：所有Agent操作都是异步的，使用`CompletableFuture`和`Flux`进行响应式编程。

2. **状态管理**：使用`Event`的状态管理方法来跟踪执行状态。

3. **错误处理**：始终处理可能的异常，并返回适当的错误事件。

4. **资源清理**：在Agent执行完成后及时清理资源。

5. **配置管理**：使用`AgentConfig`来管理Agent的配置参数。

6. **工具集成**：通过`ToolManager`注册和管理工具，确保工具的正确性和安全性。

## 与Python版本的对应关系

| Java类/接口 | Python类 | 说明 |
|------------|----------|------|
| `Agent` | `Agent` | Agent基础接口 |
| `BaseAgent` | `Agent` | Agent抽象基类 |
| `AgentScopeAgent` | `AgentScopeAgent` | AgentScope实现 |
| `Message` | `Message` | 消息类 |
| `Event` | `Event` | 事件基类 |
| `Context` | `Context` | 上下文类 |
| `ToolManager` | 工具管理功能 | 工具管理器 |
| `EnvironmentManager` | `EnvironmentManager` | 环境管理器 |
| `ContextManager` | `ContextManager` | 上下文管理器 |
