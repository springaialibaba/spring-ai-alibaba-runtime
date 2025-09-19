package io.agentscope.runtime.engine;

import reactor.core.publisher.Flux;
import io.agentscope.runtime.engine.agents.Agent;
import io.agentscope.runtime.engine.memory.context.ContextManager;
import io.agentscope.runtime.engine.schemas.agent.AgentRequest;
import io.agentscope.runtime.engine.schemas.agent.Event;
import io.agentscope.runtime.engine.schemas.context.Context;
import io.agentscope.runtime.engine.schemas.context.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 运行器类
 */
public class Runner implements AutoCloseable {

    private final Agent agent;
    private final ContextManager contextManager;
    private final String sessionId;
    private final String userId;

    public Runner(Agent agent, ContextManager contextManager) {
        this(agent, contextManager, "default_user", UUID.randomUUID().toString());
    }

    public Runner(Agent agent, ContextManager contextManager, String userId, String sessionId) {
        this.agent = agent;
        this.contextManager = contextManager;
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public Flux<Event> streamQuery(AgentRequest request) {
        return Flux.create(sink -> {
            try {
                // 获取或创建Session
                io.agentscope.runtime.engine.memory.model.Session memorySession = getOrCreateSession(userId, sessionId);

                Session session = new Session();
                session.setId(memorySession.getId());
                session.setUserId(memorySession.getUserId());
                // 转换历史消息类型
                List<io.agentscope.runtime.engine.schemas.agent.Message> convertedMessages = new ArrayList<>();
                if (memorySession.getMessages() != null) {
                    for (io.agentscope.runtime.engine.memory.model.Message memoryMsg : memorySession.getMessages()) {
                        io.agentscope.runtime.engine.schemas.agent.Message agentMsg = new io.agentscope.runtime.engine.schemas.agent.Message();
                        agentMsg.setRole(memoryMsg.getType() == io.agentscope.runtime.engine.memory.model.MessageType.USER ? "user" : "assistant");
                        
                        List<io.agentscope.runtime.engine.schemas.agent.Content> content = new ArrayList<>();
                        if (memoryMsg.getContent() != null) {
                            for (io.agentscope.runtime.engine.memory.model.MessageContent msgContent : memoryMsg.getContent()) {
                                io.agentscope.runtime.engine.schemas.agent.TextContent textContent = new io.agentscope.runtime.engine.schemas.agent.TextContent();
                                textContent.setText(msgContent.getText());
                                content.add(textContent);
                            }
                        }
                        agentMsg.setContent(content);
                        convertedMessages.add(agentMsg);
                    }
                }
                session.setMessages(convertedMessages);

                Context context = new Context();
                context.setUserId(userId);
                context.setSession(session);
                context.setRequest(request);
                context.setAgent(agent);

                if (request.getInput() != null && !request.getInput().isEmpty()) {
                    context.setCurrentMessages(request.getInput());
                }

                CompletableFuture<Flux<Event>> agentFuture = agent.runAsync(context);

                agentFuture.thenAccept(eventFlux -> {
                    StringBuilder aiResponse = new StringBuilder();
                    eventFlux.subscribe(
                        event -> {
                            sink.next(event);
                            // 收集AI的回复内容
                            if (event instanceof io.agentscope.runtime.engine.schemas.agent.Message) {
                                io.agentscope.runtime.engine.schemas.agent.Message message = (io.agentscope.runtime.engine.schemas.agent.Message) event;
                                if (io.agentscope.runtime.engine.memory.model.MessageType.MESSAGE.name().equals(message.getType()) && 
                                    "completed".equals(message.getStatus())) {
                                    if (message.getContent() != null && !message.getContent().isEmpty()) {
                                        io.agentscope.runtime.engine.schemas.agent.Content content = message.getContent().get(0);
                                        if (content instanceof io.agentscope.runtime.engine.schemas.agent.TextContent) {
                                            io.agentscope.runtime.engine.schemas.agent.TextContent textContent = (io.agentscope.runtime.engine.schemas.agent.TextContent) content;
                                            String text = textContent.getText();
                                            // 提取纯文本内容，去除ChatResponse包装
                                            String cleanText = extractCleanText(text);
                                            aiResponse.append(cleanText);
                                        }
                                    }
                                }
                            }
                        },
                        sink::error,
                        () -> {
                            // 对话完成后，保存历史消息到ContextManager
                            saveConversationHistory(context, aiResponse.toString());
                            sink.complete();
                        }
                    );
                }).exceptionally(throwable -> {
                    sink.error(throwable);
                    return null;
                });

            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    /**
     * 从ChatResponse对象中提取纯文本内容
     */
    private String extractCleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // 如果文本包含ChatResponse的完整对象信息，尝试提取其中的textContent
        if (text.contains("textContent=")) {
            int start = text.indexOf("textContent=") + 12;
            int end = text.indexOf(",", start);
            if (end == -1) end = text.indexOf("}", start);
            if (end == -1) end = text.length();
            
            String extracted = text.substring(start, end).trim();
            // 移除可能的引号
            if (extracted.startsWith("\"") && extracted.endsWith("\"")) {
                extracted = extracted.substring(1, extracted.length() - 1);
            }
            return extracted;
        }
        
        // 如果已经是纯文本，直接返回
        return text;
    }

    /**
     * 获取或创建Session
     */
    private io.agentscope.runtime.engine.memory.model.Session getOrCreateSession(String userId, String sessionId) {
        try {
            return contextManager.composeSession(userId, sessionId).join();
        } catch (Exception e) {
            // 如果Session不存在，通过ContextManager创建
            try {
                return contextManager.getSessionHistoryService().createSession(userId, Optional.of(sessionId)).join();
            } catch (Exception ex) {
                // 如果创建失败，返回一个临时Session
                return new io.agentscope.runtime.engine.memory.model.Session(sessionId, userId, new ArrayList<>());
            }
        }
    }

    /**
     * 保存对话历史到ContextManager
     */
    private void saveConversationHistory(Context context, String aiResponse) {
        try {
            // 获取当前会话
            io.agentscope.runtime.engine.memory.model.Session memorySession = getOrCreateSession(userId, sessionId);
            
            // 创建要保存的消息列表
            List<io.agentscope.runtime.engine.memory.model.Message> messagesToSave = new ArrayList<>();
            
            // 添加用户消息
            if (context.getCurrentMessages() != null) {
                for (io.agentscope.runtime.engine.schemas.agent.Message userMessage : context.getCurrentMessages()) {
                    io.agentscope.runtime.engine.memory.model.Message memoryMessage = new io.agentscope.runtime.engine.memory.model.Message();
                    memoryMessage.setType(io.agentscope.runtime.engine.memory.model.MessageType.USER);
                    
                    List<io.agentscope.runtime.engine.memory.model.MessageContent> content = new ArrayList<>();
                    if (userMessage.getContent() != null) {
                        for (io.agentscope.runtime.engine.schemas.agent.Content msgContent : userMessage.getContent()) {
                            if (msgContent instanceof io.agentscope.runtime.engine.schemas.agent.TextContent) {
                                io.agentscope.runtime.engine.schemas.agent.TextContent textContent = (io.agentscope.runtime.engine.schemas.agent.TextContent) msgContent;
                                content.add(new io.agentscope.runtime.engine.memory.model.MessageContent("text", textContent.getText()));
                            }
                        }
                    }
                    memoryMessage.setContent(content);
                    messagesToSave.add(memoryMessage);
                }
            }
            
            // 添加AI回复消息
            if (aiResponse != null && !aiResponse.isEmpty()) {
                io.agentscope.runtime.engine.memory.model.Message aiMessage = new io.agentscope.runtime.engine.memory.model.Message();
                aiMessage.setType(io.agentscope.runtime.engine.memory.model.MessageType.ASSISTANT);
                
                List<io.agentscope.runtime.engine.memory.model.MessageContent> content = new ArrayList<>();
                content.add(new io.agentscope.runtime.engine.memory.model.MessageContent("text", aiResponse));
                aiMessage.setContent(content);
                messagesToSave.add(aiMessage);
            }
            
            // 保存到ContextManager
            contextManager.append(memorySession, messagesToSave).join();
            
        } catch (Exception e) {
            // 记录错误但不中断流程
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
        } catch (Exception e) {
        }
    }
}