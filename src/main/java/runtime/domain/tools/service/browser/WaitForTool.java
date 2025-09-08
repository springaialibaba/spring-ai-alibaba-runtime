package runtime.domain.tools.service.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.BaseSandboxTools;

import java.util.function.BiFunction;

public class WaitForTool implements BiFunction<WaitForTool.Request, ToolContext, WaitForTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new BaseSandboxTools().browser_wait_for(request.time, request.text, request.textGone);
        return new Response(result, "Browser wait_for completed");
    }

    public record Request(
            @JsonProperty("time") @JsonPropertyDescription("time in seconds") Double time,
            @JsonProperty("text") String text,
            @JsonProperty("textGone") String textGone
    ) { }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}


