package com.alibaba.cloud.ai.a2a.server;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.ai.chat.model.ChatModel;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.agent.BaseAgent;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.a2a.server.config.AgentConfigProperties;
import com.alibaba.cloud.ai.a2a.server.config.KeyStrategyFactory;

import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;

@Configuration
public class ServerAgentConfiguration {

    @Value("${server.port:10000}")
    private int port;

    @Bean
    public ChatModel chatModel(AgentConfigProperties config) {
        return DashScopeChatModel.builder()
                .dashScopeApi(DashScopeApi.builder().apiKey(config.getDashScope().getApiKey()).build())
                .build();
    }

    @Bean
    public Map<String, KeyStrategy> keyStrategies(AgentConfigProperties config, KeyStrategyFactory strategyFactory) {
        Map<String, String> strategyConfigs = config.getKeyStrategies()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getType()));
        return strategyFactory.createStrategies(strategyConfigs);
    }

    @Bean
    public Map<String, BaseAgent> agents(ChatModel chatModel, AgentConfigProperties config) throws GraphStateException {
        List<AgentConfigProperties.AgentDefinition> definitions = config.getAgents();
        if (definitions == null || definitions.isEmpty()) {
            return new LinkedHashMap<>();
        }
        return definitions.stream().collect(Collectors.toMap(
                AgentConfigProperties.AgentDefinition::getName,
                def -> createAgent(def, chatModel),
                (a, b) -> a,
                LinkedHashMap::new
        ));
    }

    private BaseAgent createAgent(AgentConfigProperties.AgentDefinition def, ChatModel chatModel) {
        String type = def.getType();
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Agent type must be provided for agent: " + def.getName());
        }
        if ("ReactAgent".equals(type)) {
            try {
                return ReactAgent.builder()
                        .name(def.getName())
                        .model(chatModel)
                        .description(def.getDescription())
                        .instruction(def.getInstruction())
                        .outputKey(def.getOutputKey())
                        .build();
            } catch (GraphStateException e) {
                throw new IllegalStateException("Failed to build ReactAgent: " + def.getName(), e);
            }
        }

        try {
            Class<?> clazz = resolveAgentClass(type);
            if (!BaseAgent.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Agent type does not extend BaseAgent: " + type);
            }
            Method builderMethod = clazz.getMethod("builder");
            Object builder = builderMethod.invoke(null);
            invokeIfPresent(builder, "name", new Class[]{String.class}, new Object[]{def.getName()});
            invokeIfPresent(builder, "model", new Class[]{ChatModel.class}, new Object[]{chatModel});
            invokeIfPresent(builder, "description", new Class[]{String.class}, new Object[]{def.getDescription()});
            invokeIfPresent(builder, "instruction", new Class[]{String.class}, new Object[]{def.getInstruction()});
            invokeIfPresent(builder, "outputKey", new Class[]{String.class}, new Object[]{def.getOutputKey()});
            Method buildMethod = builder.getClass().getMethod("build");
            return (BaseAgent) buildMethod.invoke(builder);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to reflectively build agent: " + def.getName() + " type=" + type, e);
        }
    }

    private static Class<?> resolveAgentClass(String type) throws ClassNotFoundException {
        if (type.indexOf('.') < 0) {
            String fqn = "com.alibaba.cloud.ai.graph.agent." + type;
            return Class.forName(fqn);
        }
        return Class.forName(type);
    }

    private static void invokeIfPresent(Object target, String methodName, Class<?>[] paramTypes, Object[] args) {
        try {
            Method m = target.getClass().getMethod(methodName, paramTypes);
            m.invoke(target, args);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke builder method: " + methodName, e);
        }
    }


    @Bean
    @Primary
    public BaseAgent rootAgent(ChatModel chatModel, Map<String, KeyStrategy> keyStrategies,
                               Map<String, BaseAgent> agents, AgentConfigProperties config) throws GraphStateException {
        AgentConfigProperties.RootAgent rootConfig = config.getRootAgent();
        String type = rootConfig.getType() == null || rootConfig.getType().isEmpty() ? "LlmRoutingAgent" : rootConfig.getType();

        List<String> subAgentNames = rootConfig.getSubAgentNames();
        List<BaseAgent> subAgents = subAgentNames == null ? List.of() : subAgentNames.stream()
                .map(name -> {
                    BaseAgent agent = agents.get(name);
                    if (agent == null) {
                        throw new IllegalArgumentException("Unknown sub-agent: " + name);
                    }
                    return agent;
                })
                .collect(Collectors.toList());

        if ("LlmRoutingAgent".equals(type)) {
            return LlmRoutingAgent.builder()
                    .name(rootConfig.getName())
                    .model(chatModel)
                    .state(() -> keyStrategies)
                    .inputKey(rootConfig.getInputKey())
                    .outputKey(rootConfig.getOutputKey())
                    .subAgents(subAgents)
                    .build();
        }

        try {
            Class<?> clazz = resolveAgentClass(type.indexOf('.') < 0 ? "com.alibaba.cloud.ai.graph.agent.flow." + type : type);
            if (!BaseAgent.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Root agent type does not extend BaseAgent: " + type);
            }
            Method builderMethod = clazz.getMethod("builder");
            Object builder = builderMethod.invoke(null);
            invokeIfPresent(builder, "name", new Class[]{String.class}, new Object[]{rootConfig.getName()});
            invokeIfPresent(builder, "model", new Class[]{ChatModel.class}, new Object[]{chatModel});
            invokeIfPresent(builder, "state", new Class[]{com.alibaba.cloud.ai.graph.KeyStrategyFactory.class}, new Object[]{(com.alibaba.cloud.ai.graph.KeyStrategyFactory) () -> keyStrategies});
            invokeIfPresent(builder, "inputKey", new Class[]{String.class}, new Object[]{rootConfig.getInputKey()});
            invokeIfPresent(builder, "outputKey", new Class[]{String.class}, new Object[]{rootConfig.getOutputKey()});
            invokeIfPresent(builder, "subAgents", new Class[]{List.class}, new Object[]{subAgents});
            Method buildMethod = builder.getClass().getMethod("build");
            return (BaseAgent) buildMethod.invoke(builder);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build root agent type=" + type, e);
        }
    }

    @Bean
    public AgentCard agentCard(AgentConfigProperties config) {
        AgentConfigProperties.RootAgent rootAgent = config.getRootAgent();
        List<AgentConfigProperties.AgentDefinition> agents = config.getAgents();
        List<AgentSkill> skills = generateSkillsFromAgents(agents);
        String description = generateDescriptionFromRootAgent(rootAgent, agents);
        String name = generateNameFromRootAgent(rootAgent);

        return new AgentCard.Builder()
                .name(name)
                .description(description)
                .url(String.format("http://localhost:%d/a2a/", port)) // 默认URL
                .version("1.0.0")
                .documentationUrl("")
                .capabilities(new AgentCapabilities.Builder()
                        .streaming(true)
                        .pushNotifications(true)
                        .stateTransitionHistory(true)
                        .build())
                .defaultInputModes(List.of("text"))
                .defaultOutputModes(List.of("text"))
                .skills(skills)
                .protocolVersion("0.2.5")
                .build();
    }

    private List<AgentSkill> generateSkillsFromAgents(List<AgentConfigProperties.AgentDefinition> agents) {
        if (agents == null || agents.isEmpty()) {
            return List.of();
        }

        return agents.stream()
                .map(agent -> new AgentSkill.Builder()
                        .id(agent.getName() + "_skill")
                        .name(agent.getName())
                        .description(agent.getDescription())
                        .tags(List.of(agent.getType().toLowerCase()))
                        .examples(generateExamplesFromAgent(agent))
                        .build())
                .collect(Collectors.toList());
    }

    private List<String> generateExamplesFromAgent(AgentConfigProperties.AgentDefinition agent) {
        List<String> examples = new ArrayList<>();

        examples.add("使用" + agent.getName() + "处理任务");

        return examples;
    }

    private String generateDescriptionFromRootAgent(AgentConfigProperties.RootAgent rootAgent,
                                                    List<AgentConfigProperties.AgentDefinition> agents) {
        if (rootAgent == null) {
            return "AI代理服务";
        }

        return rootAgent.getDescription();
    }

    private String generateNameFromRootAgent(AgentConfigProperties.RootAgent rootAgent) {
        if (rootAgent == null) {
            return "AI Agent Service";
        }

        return rootAgent.getName();
    }
}
