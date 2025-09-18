package runtime.sandbox.tools.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.sandbox.tools.SandboxTools;

import java.util.function.BiFunction;

/**
 * 浏览器按键工具
 */
public class PressKeyTool implements BiFunction<PressKeyTool.Request, ToolContext, PressKeyTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_press_key(request.key);
        return new Response(result, "Browser press key completed");
    }

    public record Request(
            @JsonProperty(required = true, value = "key")
            @JsonPropertyDescription("Name of the key to press or a character to generate")
            String key
    ) { }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}
