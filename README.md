<div align="center">

# AgentScope Runtime Java

[![License](https://img.shields.io/badge/license-Apache%202.0-red.svg?logo=apache&label=Liscnese)](LICENSE)
[![GitHub Stars](https://img.shields.io/github/stars/agentscope-ai/agentscope-runtime?style=flat&logo=github&color=yellow&label=Stars)](https://github.com/agentscope-ai/agentscope-runtime-java/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/agentscope-ai/agentscope-runtime?style=flat&logo=github&color=purple&label=Forks)](https://github.com/agentscope-ai/agentscope-runtime-java/network)
[![MCP](https://img.shields.io/badge/MCP-Model_Context_Protocol-purple.svg?logo=plug&label=MCP)](https://modelcontextprotocol.io/)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/io.agentscope/agentscope-runtime/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.agentscope/agentscope-runtime)
[![DingTalk](https://img.shields.io/badge/DingTalk-Join_Us-orange.svg)](https://qr.dingtalk.com/action/joingroup?code=v1,k1,OmDlBXpjW+I2vWjKDsjvI9dhcXjGZi3bQiojOq3dlDw=&_dt_no_comment=1&origin=11)

[[Cookbook]](https://runtime.agentscope.io/)
[[中文README]](README_zh.md)

**Java implementation for AgentScope Runtime**

*AgentScope Runtime tackles two critical challenges in agent development: secure sandboxed tool execution and scalable agent deployment. Built with a dual-core architecture, it provides framework-agnostic infrastructure for deploying agents with full observability and safe tool interactions.*

</div>

---

## ✨ Key Features

- **🏗️ Deployment Infrastructure**: Built-in services for session management, memory, and sandbox environment control
- **🔒 Sandboxed Tool Execution**: Isolated sandboxes ensure safe tool execution without system compromise

- **🔧 Framework Agnostic**: Not tied to any specific framework. Works seamlessly with popular open-source agent frameworks and custom implementations

- ⚡ **Developer Friendly**: Simple deployment with powerful customization options

- **📊 Observability**: Comprehensive tracing and monitoring for runtime operations

---

## 💬 Contact

Welcome to join our community on

| [Discord](https://discord.gg/eYMpfnkG8h)                     | DingTalk                                                     |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| <img src="https://gw.alicdn.com/imgextra/i1/O1CN01hhD1mu1Dd3BWVUvxN_!!6000000000238-2-tps-400-400.png" width="100" height="100"> | <img src="https://img.alicdn.com/imgextra/i1/O1CN01LxzZha1thpIN2cc2E_!!6000000005934-2-tps-497-477.png" width="100" height="100"> |

---

## 📋 Table of Contents

- [🚀 Quick Start](#-quick-start)
- [🔌 Agent Framework Integration](#-agent-framework-integration)
- [🏗️ Deployment](#️-deployment)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)

---

## 🚀 Quick Start

### Prerequisites
- Java 17+

### Dependency

```xml
<dependency>
	<groupId>io.agentscope</groupId>
	<artifactId>agentscope-runtime</artifactId>
	<version>0.1.0</version>
</dependency>
```

### Basic Agent Usage Example

This example demonstrates how to create a simple LLM agent using AgentScope Runtime and stream responses from the Qwen model.

```java

```

### Basic Sandbox Usage Example

This example demonstrates how to create sandboxed and execute tool within the sandbox.

```java

```

> [!NOTE]
>
> Current version requires Docker or Kubernetes to be installed and running on your system. Please refer to [this tutorial](https://runtime.agentscope.io/en/sandbox.html) for more details.

## 🔌 Agent Framework Integration

### AgentScope Integration

```java

```

### Spring AI Alibaba Integration

```java

```


> [!NOTE]
>
> More agent framework interations are comming soon!

---

## 🏗️ Deployment

The agent runner exposes a `deploy` method that takes a `DeployManager` instance and deploys the agent. The service port is set as the parameter `port` when creating the `LocalDeployManager`. The service endpoint path is set as the parameter `endpoint_path` when deploying the agent. In this example, we set the endpoint path to `/process`. After deployment, you can access the service at `http://localhost:8090/process`.

```python
from agentscope_runtime.engine.deployers import LocalDeployManager

# Create deployment manager
deploy_manager = LocalDeployManager(
    host="localhost",
    port=8090,
)

# Deploy the agent as a streaming service
deploy_result = await runner.deploy(
    deploy_manager=deploy_manager,
    endpoint_path="/process",
    stream=True,  # Enable streaming responses
)
```

---

## 🤝 Contributing

We welcome contributions from the community! Here's how you can help:

### 🐛 Bug Reports
- Use GitHub Issues to report bugs
- Include detailed reproduction steps
- Provide system information and logs

### 💡 Feature Requests
- Discuss new ideas in GitHub Discussions
- Follow the feature request template
- Consider implementation feasibility

### 🔧 Code Contributions
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

For detailed contributing guidelines, please see  [CONTRIBUTE](CONTRIBUTING.md).

---

## 📄 License

AgentScope Runtime is released under the [Apache License 2.0](LICENSE).

```
Copyright 2025 Tongyi Lab

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
