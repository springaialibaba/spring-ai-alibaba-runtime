package runtime.infrastructure.config.agent;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.stream.Collectors;

import java.util.ArrayList;

import runtime.domain.tools.service.ToolsInit;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.ai.chat.model.ChatModel;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;

import com.alibaba.cloud.ai.graph.agent.BaseAgent;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import runtime.infrastructure.config.core.RuntimeConfigProperties;
import runtime.infrastructure.config.core.LocalKeyStrategyFactory;

import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;

@Configuration
public class ServerAgentConfiguration {

    @Value("${server.port:10000}")
    private int port;

    ToolsInit toolsInit;

    public ServerAgentConfiguration(ToolsInit toolsInit) {
        this.toolsInit = toolsInit;
    }

    @Bean
    public ChatModel defaultChatModel(RuntimeConfigProperties runtimeConfig) throws Exception {
        return generateChatModel(runtimeConfig.getModel());
    }

    private ChatModel generateChatModel(AgentProperties.Model modelConfig) throws Exception {
        if (modelConfig.getApiKey() == null) {
            throw new Exception("Api key is required");
        }
        String apiKey = modelConfig.getApiKey();
        DashScopeChatModel.Builder modelBuilder = DashScopeChatModel.builder().dashScopeApi(DashScopeApi.builder().apiKey(apiKey).build());
        DashScopeChatOptions.DashscopeChatOptionsBuilder dashScopeChatOptionsBuilder = DashScopeChatOptions.builder();
        if (modelConfig.getName() != null) {
            dashScopeChatOptionsBuilder.withModel(modelConfig.getName());
        }

        if (modelConfig.getOptions().containsKey("max_tokens")) {
            dashScopeChatOptionsBuilder.withMaxToken((Integer) modelConfig.getOptions().get("max_tokens"));
        }

        if (modelConfig.getOptions().containsKey("temperature")) {
            dashScopeChatOptionsBuilder.withTemperature((Double) modelConfig.getOptions().get("temperature"));
        }

        if (modelConfig.getOptions().containsKey("top_p")) {
            dashScopeChatOptionsBuilder.withTopP((Double) modelConfig.getOptions().get("top_p"));
        }

        if (modelConfig.getOptions().containsKey("top_k")) {
            dashScopeChatOptionsBuilder.withTopK((Integer) modelConfig.getOptions().get("top_k"));
        }

        DashScopeChatOptions dashScopeChatOptions = dashScopeChatOptionsBuilder.build();
        modelBuilder.defaultOptions(dashScopeChatOptions);

        return modelBuilder.build();
    }


    @Bean
    public Map<String, BaseAgent> agents(ChatModel defaultChatModel, AgentProperties config, RuntimeConfigProperties runtimeConfig, LocalKeyStrategyFactory strategyFactory) throws GraphStateException {
        List<AgentProperties.AgentDefinition> definitions = config.getAgents();
        if (definitions == null || definitions.isEmpty()) {
            return new LinkedHashMap<>();
        }

        // 创建agents Map和待处理定义Map
        Map<String, BaseAgent> agents = new LinkedHashMap<>();
        Map<String, AgentProperties.AgentDefinition> remainingDefinitions = new LinkedHashMap<>();

        // 初始化待处理定义Map
        for (AgentProperties.AgentDefinition def : definitions) {
            remainingDefinitions.put(def.getName(), def);
        }

        // 多轮构建：每轮都尝试构建所有可以构建的agent
        int round = 1;
        while (!remainingDefinitions.isEmpty()) {
            System.out.println("=== 第 " + round + " 轮构建开始 ===");
            System.out.println("待构建的agents: " + remainingDefinitions.keySet());
            System.out.println("已构建的agents: " + agents.keySet());

            boolean progress = false;
            List<String> agentsToBuildThisRound = new ArrayList<>();

            // 检查每个待构建的agent是否可以构建
            for (Map.Entry<String, AgentProperties.AgentDefinition> entry : remainingDefinitions.entrySet()) {
                String agentName = entry.getKey();
                AgentProperties.AgentDefinition def = entry.getValue();

                // 检查依赖是否满足
                if (canBuildAgent(def, agents)) {
                    agentsToBuildThisRound.add(agentName);
                    progress = true;
                } else {
                    // 显示缺失的依赖
                    List<String> missingDependencies = getMissingDependencies(def, agents);
                    System.out.println("Agent '" + agentName + "' 无法构建，缺失依赖: " + missingDependencies);
                }
            }

            // 构建这一轮可以构建的所有agent
            for (String agentName : agentsToBuildThisRound) {
                AgentProperties.AgentDefinition def = remainingDefinitions.get(agentName);
                System.out.println("正在构建agent: " + agentName);

                try {
                    BaseAgent agent = createAgent(def, defaultChatModel, runtimeConfig, strategyFactory, agents);
                    agents.put(agentName, agent);
                    remainingDefinitions.remove(agentName);
                    System.out.println("成功构建agent: " + agentName);
                } catch (Exception e) {
                    System.err.println("构建agent '" + agentName + "' 失败: " + e.getMessage());
                    throw new IllegalStateException("Failed to build agent: " + agentName, e);
                }
            }

            System.out.println("第 " + round + " 轮构建完成，本轮构建了 " + agentsToBuildThisRound.size() + " 个agent");
            System.out.println();

            // 如果没有进展，说明存在循环依赖或无法解决的依赖
            if (!progress) {
                System.err.println("无法继续构建，剩余agents: " + remainingDefinitions.keySet());
                for (String agentName : remainingDefinitions.keySet()) {
                    AgentProperties.AgentDefinition def = remainingDefinitions.get(agentName);
                    List<String> missingDependencies = getMissingDependencies(def, agents);
                    System.err.println("Agent '" + agentName + "' 的缺失依赖: " + missingDependencies);
                }
                throw new IllegalStateException("Circular dependency or unresolved dependencies detected in agent definitions: " + remainingDefinitions.keySet());
            }

            round++;
        }

        System.out.println("=== 所有agents构建完成，共构建了 " + agents.size() + " 个agent ===");

        return agents;
    }

    /**
     * 检查agent是否可以构建（所有依赖都已满足）
     */
    private boolean canBuildAgent(AgentProperties.AgentDefinition def, Map<String, BaseAgent> existingAgents) {
        if (def.getSubAgentNames() == null || def.getSubAgentNames().isEmpty()) {
            return true; // 没有依赖，可以构建
        }

        // 检查所有subAgents是否都已构建
        for (String subAgentName : def.getSubAgentNames()) {
            if (!existingAgents.containsKey(subAgentName)) {
                return false; // 有依赖未满足
            }
        }

        return true; // 所有依赖都已满足
    }

    /**
     * 获取agent缺失的依赖列表
     */
    private List<String> getMissingDependencies(AgentProperties.AgentDefinition def, Map<String, BaseAgent> existingAgents) {
        List<String> missingDependencies = new ArrayList<>();

        if (def.getSubAgentNames() != null && !def.getSubAgentNames().isEmpty()) {
            for (String subAgentName : def.getSubAgentNames()) {
                if (!existingAgents.containsKey(subAgentName)) {
                    missingDependencies.add(subAgentName);
                }
            }
        }

        return missingDependencies;
    }

    private List<ToolCallback> convertToolCallbacks(List<String> toolNames) {
        return List.of();
    }

    private ToolCallbackResolver convertToolCallbackResolver(String resolverName) {
        return null;
    }

    //    Todo: 几个hook还没有添加，有一些转换的函数当前是空实现，compileConfig也没有实现
    private BaseAgent createAgent(AgentProperties.AgentDefinition def, ChatModel defaultChatModel, RuntimeConfigProperties runtimeConfig, LocalKeyStrategyFactory strategyFactory, Map<String, BaseAgent> existingAgents) {
        String type = def.getType();
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Agent type must be provided for agent: " + def.getName());
        }

        // 为这个agent创建专用的ChatModel
        ChatModel agentChatModel = createAgentChatModel(def, defaultChatModel, runtimeConfig);

        KeyStrategyFactory agentKeyStrategies = createAgentKeyStrategies(def, strategyFactory);

        if ("ReactAgent".equals(type)) {
            try {
                ReactAgent.Builder builder = ReactAgent.builder().name(def.getName()).state(agentKeyStrategies).model(agentChatModel);

                if (def.getDescription() != null) {
                    builder.description(def.getDescription());
                }
                if (def.getInstruction() != null) {
                    builder.instruction(def.getInstruction());
                }
                if (def.getOutputKey() != null) {
                    builder.outputKey(def.getOutputKey());
                }
                if (def.getMaxIterations() != null) {
                    builder.maxIterations(def.getMaxIterations());
                }
                if (def.getTools() != null) {
                    builder.tools(convertToolCallbacks(def.getTools()));
                }
                if (def.getResolver() != null) {
                    builder.resolver(convertToolCallbackResolver(def.getResolver()));
                }
                if (def.getInputKey() != null) {
                    builder.inputKey(def.getInputKey());
                }
                if (def.getMaxIterations() != null) {
                    builder.maxIterations(def.getMaxIterations());
                }
                builder.tools(toolsInit.getTools());
                return builder.build();
            } catch (GraphStateException e) {
                throw new IllegalStateException("Failed to build ReactAgent: " + def.getName(), e);
            }
        } else if ("LlmRoutingAgent".equals(type)) {
            try {
                LlmRoutingAgent.LlmRoutingAgentBuilder builder = LlmRoutingAgent.builder().name(def.getName()).state(agentKeyStrategies).model(agentChatModel);

                if (def.getDescription() != null) {
                    builder.description(def.getDescription());
                }
                if (def.getOutputKey() != null) {
                    builder.outputKey(def.getOutputKey());
                }
                if (def.getInputKey() != null) {
                    builder.inputKey(def.getInputKey());
                }
                if (def.getSubAgentNames() != null && !def.getSubAgentNames().isEmpty()) {
                    List<BaseAgent> subAgents = def.getSubAgentNames().stream().map(name -> {
                        BaseAgent agent = existingAgents.get(name);
                        if (agent == null) {
                            throw new IllegalArgumentException("Unknown sub-agent: " + name);
                        }
                        return agent;
                    }).collect(Collectors.toList());
                    builder.subAgents(subAgents);
                }
                return builder.build();
            } catch (GraphStateException e) {
                throw new IllegalStateException("Failed to build LlmRoutingAgent: " + def.getName(), e);
            }
        } else if ("SequentialAgent".equals(type)) {
            try {
                SequentialAgent.SequentialAgentBuilder builder = SequentialAgent.builder().name(def.getName()).state(agentKeyStrategies);

                if (def.getDescription() != null) {
                    builder.description(def.getDescription());
                }
                if (def.getOutputKey() != null) {
                    builder.outputKey(def.getOutputKey());
                }
                if (def.getInputKey() != null) {
                    builder.inputKey(def.getInputKey());
                }
                if (def.getSubAgentNames() != null && !def.getSubAgentNames().isEmpty()) {
                    List<BaseAgent> subAgents = def.getSubAgentNames().stream().map(name -> {
                        BaseAgent agent = existingAgents.get(name);
                        if (agent == null) {
                            throw new IllegalArgumentException("Unknown sub-agent: " + name);
                        }
                        return agent;
                    }).collect(Collectors.toList());
                    builder.subAgents(subAgents);
                }
                return builder.build();
            } catch (GraphStateException e) {
                throw new IllegalStateException("Failed to build SequentialAgent: " + def.getName(), e);
            }
        }

//        Todo: 这个ParallelAgent的逻辑还没有看懂
        else if ("ParallelAgent".equals(type)) {
            try {
                ParallelAgent.ParallelAgentBuilder builder = ParallelAgent.builder().name(def.getName()).state(agentKeyStrategies);

                if (def.getDescription() != null) {
                    builder.description(def.getDescription());
                }
                if (def.getOutputKey() != null) {
                    builder.outputKey(def.getOutputKey());
                }
                if (def.getInputKey() != null) {
                    builder.inputKey(def.getInputKey());
                }
                if (def.getSubAgentNames() != null && !def.getSubAgentNames().isEmpty()) {
                    List<BaseAgent> subAgents = def.getSubAgentNames().stream().map(name -> {
                        BaseAgent agent = existingAgents.get(name);
                        if (agent == null) {
                            throw new IllegalArgumentException("Unknown sub-agent: " + name);
                        }
                        return agent;
                    }).collect(Collectors.toList());
                    builder.subAgents(subAgents);
                }
                return builder.build();
            } catch (GraphStateException e) {
                throw new IllegalStateException("Failed to build ParallelAgent: " + def.getName(), e);
            }
        } else {
            throw new IllegalStateException("Unsupported agent type: " + type);
        }
    }

    private ChatModel createAgentChatModel(AgentProperties.AgentDefinition def, ChatModel defaultChatModel, RuntimeConfigProperties runtimeConfig) {
        if (def.getModel() != null) {
            System.out.println("为agent '" + def.getName() + "' 使用自定义模型配置");
            try {
                return generateChatModel(def.getModel());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create ChatModel for agent: " + def.getName(), e);
            }
        }

        // 使用默认的ChatModel
        return defaultChatModel;
    }

    private KeyStrategyFactory createAgentKeyStrategies(AgentProperties.AgentDefinition def, LocalKeyStrategyFactory strategyFactory) {
        Map<String, String> strategyConfigs = new LinkedHashMap<>();

        if (def.getState() != null && def.getState().getStrategies() != null) {
            strategyConfigs.putAll(def.getState().getStrategies());
        }

        return strategyFactory.createStrategies(strategyConfigs);
    }

    @Bean
    public BaseAgent rootAgent(Map<String, BaseAgent> agents, AgentProperties config) throws GraphStateException {
        // 从配置中查找根agent
        AgentProperties.AgentDefinition rootAgentDef = config.getAgents().stream().filter(agent -> Boolean.TRUE.equals(agent.getIsRoot())).findFirst().orElse(null);

        if (rootAgentDef == null) {
            throw new IllegalStateException("No root agent found in configuration");
        }

        // 根agent已经在agents Map中创建了，直接返回
        BaseAgent rootAgent = agents.get(rootAgentDef.getName());
        if (rootAgent == null) {
            throw new IllegalStateException("Root agent not found in agents map: " + rootAgentDef.getName());
        }

        return rootAgent;
    }

    @Bean
    public AgentCard agentCard(AgentProperties config) {
        List<AgentProperties.AgentDefinition> agents = config.getAgents();
        AgentProperties.AgentDefinition rootAgent = agents.stream().filter(agent -> Boolean.TRUE.equals(agent.getIsRoot())).findFirst().orElse(null);

        List<AgentSkill> skills = generateSkillsFromAgents(agents);
        String description = generateDescriptionFromRootAgent(rootAgent, agents);
        String name = generateNameFromRootAgent(rootAgent);

        return new AgentCard.Builder().name(name).description(description).url(String.format("http://localhost:%d/a2a/", port)) // 默认URL
                .version("1.0.0").documentationUrl("").capabilities(new AgentCapabilities.Builder().streaming(true).pushNotifications(true).stateTransitionHistory(true).build()).defaultInputModes(List.of("text")).defaultOutputModes(List.of("text")).skills(skills).protocolVersion("0.2.5").build();
    }

    private List<AgentSkill> generateSkillsFromAgents(List<AgentProperties.AgentDefinition> agents) {
        if (agents == null || agents.isEmpty()) {
            return List.of();
        }

        return agents.stream().filter(agent -> !Boolean.TRUE.equals(agent.getIsRoot())) // 排除根agent
                .map(agent -> new AgentSkill.Builder().id(agent.getName() + "_skill").name(agent.getName()).description(agent.getDescription()).tags(List.of(agent.getType().toLowerCase())).examples(generateExamplesFromAgent(agent)).build()).collect(Collectors.toList());
    }

    private List<String> generateExamplesFromAgent(AgentProperties.AgentDefinition agent) {
        List<String> examples = new ArrayList<>();

        examples.add("使用" + agent.getName() + "处理任务");

        return examples;
    }

    private String generateDescriptionFromRootAgent(AgentProperties.AgentDefinition rootAgent, List<AgentProperties.AgentDefinition> agents) {
        if (rootAgent == null) {
            return "AI代理服务";
        }

        return rootAgent.getDescription();
    }

    private String generateNameFromRootAgent(AgentProperties.AgentDefinition rootAgent) {
        if (rootAgent == null) {
            return "AI Agent Service";
        }

        return rootAgent.getName();
    }
}
