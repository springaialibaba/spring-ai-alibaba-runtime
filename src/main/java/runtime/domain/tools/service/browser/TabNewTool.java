package runtime.domain.tools.service.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.BaseSandboxTools;

import java.util.function.BiFunction;

public class TabNewTool implements BiFunction<TabNewTool.Request, ToolContext, TabNewTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new BaseSandboxTools().browser_tab_new(request.url);
        return new Response(result, "Browser tab_new completed");
    }

    public record Request(
            @JsonProperty("url")
            @JsonPropertyDescription("The URL to navigate to in the new tab")
            String url
    ) { }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}


