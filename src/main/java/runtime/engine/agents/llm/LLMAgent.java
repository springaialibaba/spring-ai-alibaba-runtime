package runtime.engine.agents.llm;

import reactor.core.publisher.Flux;
import runtime.engine.agents.BaseAgent;
import runtime.engine.agents.config.AgentConfig;
import runtime.engine.llms.BaseLLM;
import runtime.engine.schemas.context.Context;
import runtime.engine.schemas.agent.Event;
import runtime.engine.schemas.agent.Message;
import runtime.engine.schemas.agent.Content;
import runtime.engine.schemas.agent.TextContent;
import runtime.engine.memory.model.MessageType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM智能体
 */
public class LLMAgent extends BaseAgent {

    private final BaseLLM model;

    public LLMAgent(BaseLLM model) {
        this(model, "llm_agent", "A simple LLM agent for text generation", new AgentConfig());
    }

    public LLMAgent(BaseLLM model, String name, String description, AgentConfig config) {
        super(name, description, null, null, config);
        this.model = model;
    }

    @Override
    protected Flux<Event> execute(Context context) {
        return Flux.create(sink -> {
            try {
                // 使用完整的上下文消息，包括历史对话
                List<Map<String, Object>> openaiMessages = buildCompleteMessageHistory(context);
                List<Map<String, Object>> tools = convertToOpenAiTools(context.getRequest().getTools());

                Message message = new Message();
                message.setType(MessageType.MESSAGE.name());
                message.setRole("assistant");
                sink.next(message.inProgress());

                TextContent textDeltaContent = new TextContent();
                textDeltaContent.setDelta(true);

                model.chatStream(openaiMessages, tools)
                    .subscribe(
                        chunk -> {
                            try {
                                if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
                                    BaseLLM.ChatChunk.Delta delta = chunk.getChoices().get(0).getDelta();
                                    if (delta != null && delta.getContent() != null) {
                                        textDeltaContent.setText(delta.getContent());
                                        Content resultContent = message.addDeltaContent(textDeltaContent);
                                        sink.next(resultContent);
                                    }
                                }
                            } catch (Exception e) {
                                sink.error(e);
                            }
                        },
                        sink::error,
                        () -> {
                            message.completed();
                            sink.next(message);
                            sink.complete();
                        }
                    );

            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Override
    public LLMAgent copy() {
        return new LLMAgent(this.model, this.name, this.description, this.config);
    }

    private List<Map<String, Object>> convertToOpenAiMessages(List<Message> messages) {
        List<Map<String, Object>> openaiMessages = new ArrayList<>();

        for (Message message : messages) {
            Map<String, Object> openaiMessage = new HashMap<>();
            openaiMessage.put("role", message.getRole());

            List<Map<String, Object>> content = new ArrayList<>();
            if (message.getContent() != null) {
                for (Content msgContent : message.getContent()) {
                    Map<String, Object> contentItem = new HashMap<>();
                    if (msgContent instanceof TextContent) {
                        TextContent textContent = (TextContent) msgContent;
                        contentItem.put("type", "text");
                        contentItem.put("text", textContent.getText());
                    }
                    content.add(contentItem);
                }
            }
            openaiMessage.put("content", content);
            openaiMessages.add(openaiMessage);
        }
        return openaiMessages;
    }

    private List<Map<String, Object>> convertToOpenAiTools(List<Object> tools) {
        return new ArrayList<>();
    }
    
    /**
     * 构建完整的消息历史，包括会话中的历史消息和当前消息
     */
    private List<Map<String, Object>> buildCompleteMessageHistory(Context context) {
        List<Map<String, Object>> allMessages = new ArrayList<>();
        
        // 添加会话中的历史消息
        if (context.getSession() != null && context.getSession().getMessages() != null) {
            for (runtime.engine.schemas.agent.Message sessionMessage : context.getSession().getMessages()) {
                Map<String, Object> messageMap = new HashMap<>();
                messageMap.put("role", sessionMessage.getRole());
                
                List<Map<String, Object>> content = new ArrayList<>();
                if (sessionMessage.getContent() != null) {
                    for (Content msgContent : sessionMessage.getContent()) {
                        Map<String, Object> contentItem = new HashMap<>();
                        if (msgContent instanceof TextContent) {
                            TextContent textContent = (TextContent) msgContent;
                            contentItem.put("type", "text");
                            contentItem.put("text", textContent.getText());
                        }
                        content.add(contentItem);
                    }
                }
                messageMap.put("content", content);
                allMessages.add(messageMap);
            }
        }
        
        // 添加当前请求的消息
        if (context.getCurrentMessages() != null) {
            for (runtime.engine.schemas.agent.Message currentMessage : context.getCurrentMessages()) {
                Map<String, Object> messageMap = new HashMap<>();
                messageMap.put("role", currentMessage.getRole());
                
                List<Map<String, Object>> content = new ArrayList<>();
                if (currentMessage.getContent() != null) {
                    for (Content msgContent : currentMessage.getContent()) {
                        Map<String, Object> contentItem = new HashMap<>();
                        if (msgContent instanceof TextContent) {
                            TextContent textContent = (TextContent) msgContent;
                            contentItem.put("type", "text");
                            contentItem.put("text", textContent.getText());
                        }
                        content.add(contentItem);
                    }
                }
                messageMap.put("content", content);
                allMessages.add(messageMap);
            }
        }
        
        return allMessages;
    }
}