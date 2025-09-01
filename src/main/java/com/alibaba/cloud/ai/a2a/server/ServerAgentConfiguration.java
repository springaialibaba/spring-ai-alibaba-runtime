package com.alibaba.cloud.ai.a2a.server;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.lang.reflect.Method;

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
            invokeIfPresent(builder, "name", new Class[] { String.class }, new Object[] { def.getName() });
            invokeIfPresent(builder, "model", new Class[] { ChatModel.class }, new Object[] { chatModel });
            invokeIfPresent(builder, "description", new Class[] { String.class }, new Object[] { def.getDescription() });
            invokeIfPresent(builder, "instruction", new Class[] { String.class }, new Object[] { def.getInstruction() });
            invokeIfPresent(builder, "outputKey", new Class[] { String.class }, new Object[] { def.getOutputKey() });
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
            invokeIfPresent(builder, "name", new Class[] { String.class }, new Object[] { rootConfig.getName() });
            invokeIfPresent(builder, "model", new Class[] { ChatModel.class }, new Object[] { chatModel });
            invokeIfPresent(builder, "state", new Class[] { com.alibaba.cloud.ai.graph.KeyStrategyFactory.class }, new Object[] { (com.alibaba.cloud.ai.graph.KeyStrategyFactory) () -> keyStrategies });
            invokeIfPresent(builder, "inputKey", new Class[] { String.class }, new Object[] { rootConfig.getInputKey() });
            invokeIfPresent(builder, "outputKey", new Class[] { String.class }, new Object[] { rootConfig.getOutputKey() });
            invokeIfPresent(builder, "subAgents", new Class[] { List.class }, new Object[] { subAgents });
            Method buildMethod = builder.getClass().getMethod("build");
            return (BaseAgent) buildMethod.invoke(builder);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build root agent type=" + type, e);
        }
    }

    @Bean
    public AgentCard agentCard(AgentConfigProperties config) {
        AgentConfigProperties.AgentCardInfo cardConfig = config.getAgentCard();

        return new AgentCard.Builder().name(cardConfig.getName())
                .description(cardConfig.getDescription())
                .url(cardConfig.getUrl())
                .version(cardConfig.getVersion())
                .documentationUrl(cardConfig.getDocumentationUrl())
                .capabilities(new AgentCapabilities.Builder().streaming(cardConfig.getCapabilities().isStreaming())
                        .pushNotifications(cardConfig.getCapabilities().isPushNotifications())
                        .stateTransitionHistory(cardConfig.getCapabilities().isStateTransitionHistory())
                        .build())
                .defaultInputModes(cardConfig.getDefaultInputModes())
                .defaultOutputModes(cardConfig.getDefaultOutputModes())
                .skills(cardConfig.getSkills()
                        .stream()
                        .map(skill -> new AgentSkill.Builder().id(skill.getId())
                                .name(skill.getName())
                                .description(skill.getDescription())
                                .tags(skill.getTags())
                                .examples(skill.getExamples())
                                .build())
                        .collect(Collectors.toList()))
                .protocolVersion(cardConfig.getProtocolVersion())
                .build();
    }

}
