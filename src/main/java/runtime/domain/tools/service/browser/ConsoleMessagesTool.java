package runtime.domain.tools.service.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.SandboxTools;

import java.util.function.BiFunction;

public class ConsoleMessagesTool implements BiFunction<ConsoleMessagesTool.Request, ToolContext, ConsoleMessagesTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_console_messages_tool();
        return new Response(result, "success");
    }

    public record Request() { }

    @JsonClassDescription("Returns all console messages")
    public record Response(String result, String message) {}
}
