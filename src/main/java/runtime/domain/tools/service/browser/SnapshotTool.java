package runtime.domain.tools.service.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.SandboxTools;

import java.util.function.BiFunction;

public class SnapshotTool implements BiFunction<SnapshotTool.Request, ToolContext, SnapshotTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_snapshot();
        return new Response(result, "Browser snapshot completed");
    }

    public record Request() { }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}


