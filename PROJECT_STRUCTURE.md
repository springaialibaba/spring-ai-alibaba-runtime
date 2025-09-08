# Spring AI Alibaba A2A Server - 项目结构

## 📁 目录结构

```
src/main/java/com/alibaba/cloud/ai/a2a/server/
├── 📱 application/                    # 应用层
│   ├── controller/                    # REST控制器
│   │   ├── A2aController.java        # A2A协议处理
│   │   ├── AgentController.java      # Agent基础接口
│   │   ├── AgentCardController.java  # Agent卡片管理
│   │   └── MemoryController.java     # 内存管理接口
│   ├── dto/                         # 数据传输对象
│   │   ├── ContentItem.java         # 内容项DTO
│   │   ├── MemoryItem.java          # 记忆项DTO
│   │   ├── MemoryRequest.java       # 记忆请求DTO
│   │   └── MemoryResponse.java      # 记忆响应DTO
│   └── service/                     # 应用服务
│       └── MemoryServiceStartupListener.java # 内存服务启动监听器
│
├── 🧠 domain/                       # 领域层
│   ├── memory/                      # 内存领域
│   │   ├── model/                   # 领域模型
│   │   │   ├── Message.java         # 消息模型
│   │   │   ├── MessageContent.java  # 消息内容模型
│   │   │   ├── MessageType.java     # 消息类型枚举
│   │   │   └── Session.java         # 会话模型
│   │   └── service/                 # 领域服务
│   │       ├── EmbeddingService.java     # 向量嵌入服务
│   │       ├── MemoryService.java        # 记忆服务
│   │       └── SessionHistoryService.java # 会话历史服务
│   │
│   └── tools/service/               # 工具服务
│       ├── BaseSandboxTools.java    # 基础沙箱工具
│       ├── MathCalculatorTools.java # 数学计算工具
│       ├── RunPythonTools.java      # Python执行工具
│       └── ToolsInit.java           # 工具初始化
│
├── 🏗️ infrastructure/              # 基础设施层
│   ├── config/                      # 配置管理
│   │   ├── agent/                   # Agent配置
│   │   │   ├── AgentConfig.java              # Agent配置类
│   │   │   ├── AgentConfigurationLoader.java # Agent配置加载器
│   │   │   ├── AgentHandlerConfiguration.java # Agent处理器配置
│   │   │   ├── AgentProperties.java          # Agent属性配置
│   │   │   └── ServerAgentConfiguration.java # 服务端Agent配置
│   │   ├── core/                    # 核心配置
│   │   │   ├── AgentRegistryConfiguration.java # Agent注册配置
│   │   │   ├── LocalKeyStrategyFactory.java    # 本地密钥策略工厂
│   │   │   ├── RuntimeConfigProperties.java    # 运行时配置属性
│   │   │   ├── RuntimeConfiguration.java       # 运行时配置
│   │   │   └── YamlPropertySourceFactory.java  # YAML属性源工厂
│   │   ├── memory/                  # 内存配置
│   │   │   ├── MemoryConfig.java    # 内存配置类
│   │   │   └── MemoryProperties.java # 内存属性配置
│   │   └── nacos/                   # Nacos配置
│   │       ├── NacosConfiguration.java # Nacos配置类
│   │       └── NacosProperties.java     # Nacos属性配置
│   │
│   ├── external/                    # 外部系统集成
│   │   ├── nacos/                   # Nacos集成
│   │   │   ├── NacosAgentRegistry.java # Nacos Agent注册
│   │   │   └── util/
│   │   │       └── AgentCardUtil.java # Agent卡片工具类
│   │   ├── registry/                # 注册中心
│   │   │   ├── AgentRegistry.java        # Agent注册接口
│   │   │   └── AgentRegistryService.java # Agent注册服务
│   │   └── sandbox/                 # 沙箱环境
│   │       ├── DockerManager.java       # Docker管理器
│   │       ├── SandboxManager.java      # 沙箱管理器
│   │       ├── model/                    # 沙箱模型
│   │       │   ├── ContainerModel.java  # 容器模型
│   │       │   ├── DockerProp.java      # Docker属性
│   │       │   ├── IpythonRequest.java  # IPython请求
│   │       │   ├── IpythonResponse.java # IPython响应
│   │       │   ├── SandboxType.java     # 沙箱类型枚举
│   │       │   └── VolumeBinding.java   # 卷绑定
│   │       └── util/                     # 工具类
│   │           ├── HttpClient.java           # HTTP客户端
│   │           ├── RandomStringGenerator.java # 随机字符串生成器
│   │           └── SimpleTest.java           # 简单测试
│   │
│   └── persistence/                 # 持久化层
│       ├── memory/                  # 内存存储
│       │   ├── entity/              # 实体类
│       │   │   ├── MemoryEntity.java        # 记忆实体
│       │   │   ├── SessionEntity.java       # 会话实体
│       │   │   └── SessionMessageEntity.java # 会话消息实体
│       │   ├── repository/          # 仓储接口
│       │   │   ├── MemoryRepository.java        # 记忆仓储
│       │   │   ├── SessionMessageRepository.java # 会话消息仓储
│       │   │   └── SessionRepository.java        # 会话仓储
│       │   └── service/             # 持久化服务实现
│       │       ├── InMemoryMemoryService.java # 内存记忆服务
│       │       ├── MySQLMemoryService.java    # MySQL记忆服务
│       │       ├── RedisMemoryService.java    # Redis记忆服务
│       │       └── SimpleEmbeddingService.java # 简单嵌入服务
│       └── session/                 # 会话存储
│           ├── InMemorySessionHistoryService.java # 内存会话历史服务
│           ├── MySQLSessionHistoryService.java    # MySQL会话历史服务
│           └── RedisSessionHistoryService.java    # Redis会话历史服务
│
├── 🔧 shared/                       # 共享组件
│   ├── common/                      # 通用组件
│   │   ├── Service.java             # 服务基类
│   │   ├── ServiceManager.java      # 服务管理器
│   │   └── ServiceWithLifecycleManager.java # 带生命周期的服务管理器
│   ├── context/                     # 上下文管理
│   │   ├── ContextComposer.java     # 上下文组合器
│   │   ├── ContextManager.java      # 上下文管理器
│   │   ├── ContextManagerFactory.java # 上下文管理器工厂
│   │   └── ServerCallContext.java   # 服务调用上下文
│   └── handler/                     # 处理器
│       ├── GraphAgentExecutor.java  # 图Agent执行器
│       └── JSONRPCHandler.java      # JSON-RPC处理器
│
└── 🚀 A2aServerApplication.java     # 应用启动类

src/main/resources/
├── sql/                             # 数据库脚本
│   └── schema.sql                   # 数据库架构
├── application.yml                  # 主配置文件
├── application-memory.yml           # 内存相关配置
├── application-nacos.yml            # Nacos配置
├── application-new-agent.yml        # 新Agent配置
└── application-runtime.yml          # 运行时配置
```