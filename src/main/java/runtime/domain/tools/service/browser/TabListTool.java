package runtime.domain.tools.service.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.SandboxTools;

import java.util.function.BiFunction;

/**
 * 浏览器标签页列表工具
 */
public class TabListTool implements BiFunction<TabListTool.Request, ToolContext, TabListTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_tab_list();
        return new Response(result, "Browser tab list completed");
    }

    public record Request() { }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}
