# Java Memory Module

这是Python services目录的Java版本实现，提供了完整的会话历史和内存管理功能。

## 功能特性

- **会话管理**: 创建、检索、更新和删除对话会话
- **内存管理**: 存储和检索长期记忆，支持按用户和会话组织
- **上下文组合**: 自动组合会话历史和记忆信息
- **多种存储后端**: 支持内存存储和Redis存储
- **异步操作**: 所有操作都是异步的，基于CompletableFuture
- **Spring Boot集成**: 提供自动配置和依赖注入支持

## 核心组件

### 基础接口
- `Service`: 服务基础接口，定义生命周期方法
- `ServiceWithLifecycleManager`: 具有生命周期管理功能的服务基类

### 数据模型
- `Message`: 消息类，包含类型、内容和元数据
- `MessageContent`: 消息内容类，支持文本等类型
- `MessageType`: 消息类型枚举
- `Session`: 会话类，包含用户ID、会话ID和消息列表

### 服务接口
- `MemoryService`: 内存服务接口，用于存储和检索长期记忆
- `SessionHistoryService`: 会话历史服务接口，用于管理对话会话

### 实现类
- `InMemoryMemoryService`: 内存实现的内存服务
- `InMemorySessionHistoryService`: 内存实现的会话历史服务
- `RedisMemoryService`: Redis实现的内存服务
- `RedisSessionHistoryService`: Redis实现的会话历史服务

### 管理器
- `ServiceManager`: 服务管理器抽象基类
- `ContextManager`: 上下文管理器，组合会话历史和内存服务
- `ContextComposer`: 上下文组合器，负责组合上下文信息

### 工厂和配置
- `ContextManagerFactory`: 上下文管理器工厂类
- `MemoryConfiguration`: Spring Boot自动配置类

## 使用方法

### 1. 基本使用

```java
// 创建默认的上下文管理器（使用内存实现）
ContextManager contextManager = ContextManagerFactory.createDefault();

// 启动服务
contextManager.start().get();

// 创建会话
Session session = contextManager.composeSession("user123", "session456").get();

// 创建消息
MessageContent content = new MessageContent("text", "Hello, AI!");
Message message = new Message(MessageType.USER, List.of(content));

// 组合上下文
contextManager.composeContext(session, List.of(message)).get();

// 关闭服务
contextManager.close();
```

### 2. 使用Redis存储

```java
// 配置Redis
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setDefaultSerializer(new StringRedisSerializer());
        return template;
    }
}

// 创建使用Redis的上下文管理器
ContextManager contextManager = ContextManagerFactory.createWithRedis(redisTemplate);
```

### 3. Spring Boot集成

在`application.yml`中配置：

```yaml
# 使用内存存储（默认）
memory:
  service:
    type: memory
session:
  service:
    type: memory

# 或使用Redis存储
memory:
  service:
    type: redis
session:
  service:
    type: redis

spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

然后直接注入使用：

```java
@Service
public class ChatService {
    @Autowired
    private ContextManager contextManager;
    
    public CompletableFuture<String> processMessage(String userId, String sessionId, String message) {
        // 处理消息逻辑
    }
}
```

## 配置选项

### 内存服务配置
- `memory.service.type`: 内存服务类型 (`memory` 或 `redis`)

### 会话服务配置
- `session.service.type`: 会话服务类型 (`memory` 或 `redis`)

### Redis配置
- `spring.data.redis.host`: Redis主机地址
- `spring.data.redis.port`: Redis端口
- `spring.data.redis.database`: Redis数据库编号

## 示例代码

参考`MemoryServiceExample`类查看完整的使用示例，包括：
- 处理用户消息
- 搜索相关记忆
- 列出用户会话
- 清除用户记忆

## 注意事项

1. 所有操作都是异步的，需要使用`CompletableFuture`处理结果
2. 服务需要先启动才能使用
3. 使用完毕后应该调用`close()`方法关闭服务
4. Redis实现需要确保Redis服务可用
5. 内存实现的数据在服务重启后会丢失
