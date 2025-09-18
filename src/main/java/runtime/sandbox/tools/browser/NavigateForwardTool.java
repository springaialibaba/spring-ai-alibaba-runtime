package runtime.sandbox.tools.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.sandbox.tools.SandboxTools;

import java.util.function.BiFunction;

/**
 * 浏览器前进导航工具
 */
public class NavigateForwardTool implements BiFunction<NavigateForwardTool.Request, ToolContext, NavigateForwardTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_navigate_forward();
        return new Response(result, "Browser navigate forward completed");
    }

    public record Request() { }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}
