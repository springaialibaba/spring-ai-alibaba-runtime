package runtime.sandbox.tools.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.sandbox.tools.SandboxTools;

import java.util.function.BiFunction;

public class WaitForTool implements BiFunction<WaitForTool.Request, ToolContext, WaitForTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_wait_for(request.time, request.text, request.textGone);
        return new Response(result, "Browser wait_for completed");
    }

    public record Request(
            @JsonProperty("time") @JsonPropertyDescription("time in seconds") Double time,
            @JsonProperty("text") String text,
            @JsonProperty("textGone") String textGone
    ) { 
        public Request {
            // 为可选参数提供默认值处理
            if (time == null) {
                time = 0.0;
            }
            if (text == null) {
                text = "";
            }
            if (textGone == null) {
                textGone = "";
            }
        }
    }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}


