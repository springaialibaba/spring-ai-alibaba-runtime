package runtime.domain.tools.service.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.SandboxTools;

import java.util.function.BiFunction;

/**
 * 浏览器后退导航工具
 */
public class NavigateBackTool implements BiFunction<NavigateBackTool.Request, ToolContext, NavigateBackTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_navigate_back();
        return new Response(result, "Browser navigate back completed");
    }

    public record Request() { }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}
