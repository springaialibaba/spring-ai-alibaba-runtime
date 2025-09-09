package runtime.domain.tools.service.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.SandboxTools;

import java.util.function.BiFunction;

/**
 * 浏览器对话框处理工具
 */
public class HandleDialogTool implements BiFunction<HandleDialogTool.Request, ToolContext, HandleDialogTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_handle_dialog(request.accept, request.promptText);
        return new Response(result, "Browser handle dialog completed");
    }

    public record Request(
            @JsonProperty(required = true, value = "accept")
            @JsonPropertyDescription("Whether to accept the dialog")
            Boolean accept,
            @JsonProperty(value = "promptText")
            @JsonPropertyDescription("The text of the prompt in case of a prompt dialog")
            String promptText
    ) { 
        public Request {
            // 为可选参数提供默认值处理
            if (promptText == null) {
                promptText = "";
            }
        }
    }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}
