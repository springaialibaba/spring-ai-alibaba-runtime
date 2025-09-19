package runtime.engine.llms;

import reactor.core.publisher.Flux;
import java.util.List;
import java.util.Map;

/**
 * LLM基础接口
 * 对应Python版本的base_llm.py中的BaseLLM类
 */
public interface BaseLLM {
    
    /**
     * 流式聊天
     * 对应Python版本的chat_stream方法
     * 
     * @param messages 消息列表
     * @param tools 工具列表
     * @return 流式响应
     */
    Flux<ChatChunk> chatStream(List<Map<String, Object>> messages, List<Map<String, Object>> tools);
    
    /**
     * 非流式聊天
     * 对应Python版本的chat方法
     * 
     * @param messages 消息列表
     * @param tools 工具列表
     * @return 聊天响应
     */
    ChatResponse chat(List<Map<String, Object>> messages, List<Map<String, Object>> tools);
    
    /**
     * 聊天响应类
     */
    class ChatResponse {
        private String content;
        private Map<String, Object> usage;
        
        public ChatResponse(String content, Map<String, Object> usage) {
            this.content = content;
            this.usage = usage;
        }
        
        public String getContent() { return content; }
        public Map<String, Object> getUsage() { return usage; }
    }
    
    /**
     * 聊天块类
     */
    class ChatChunk {
        private List<Choice> choices;
        
        public ChatChunk(List<Choice> choices) {
            this.choices = choices;
        }
        
        public List<Choice> getChoices() { return choices; }
        
        public static class Choice {
            private Delta delta;
            
            public Choice(Delta delta) {
                this.delta = delta;
            }
            
            public Delta getDelta() { return delta; }
        }
        
        public static class Delta {
            private String content;
            
            public Delta(String content) {
                this.content = content;
            }
            
            public String getContent() { return content; }
        }
    }
}
