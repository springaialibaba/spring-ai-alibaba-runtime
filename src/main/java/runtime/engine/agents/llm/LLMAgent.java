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

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * LLM智能体
 * 对应Python版本的llm_agent.py中的LLMAgent类
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
                // 转换消息格式
                List<Map<String, Object>> openaiMessages = convertToOpenAiMessages(context.getCurrentMessages());
                List<Map<String, Object>> tools = convertToOpenAiTools(context.getRequest().getTools());
                
                // 创建初始消息
                Message message = new Message();
                message.setType(MessageType.MESSAGE.name());
                message.setRole("assistant");
                sink.next(message.inProgress());
                
                // 流式处理LLM响应
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
                        error -> sink.error(error),
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
    
    /**
     * 转换消息格式为OpenAI格式
     */
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
    
    /**
     * 转换工具格式为OpenAI格式
     */
    private List<Map<String, Object>> convertToOpenAiTools(List<Object> tools) {
        List<Map<String, Object>> openaiTools = new ArrayList<>();
        
        if (tools != null) {
            for (Object tool : tools) {
                // 这里可以根据实际的工具格式进行转换
                Map<String, Object> openaiTool = new HashMap<>();
                openaiTool.put("type", "function");
                openaiTool.put("function", tool);
                openaiTools.add(openaiTool);
            }
        }
        
        return openaiTools;
    }
    
    @Override
    public LLMAgent copy() {
        return new LLMAgent(this.model, this.name, this.description, this.config);
    }
}
