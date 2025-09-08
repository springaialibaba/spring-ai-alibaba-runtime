package runtime.domain.tools.service.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.BaseSandboxTools;

import java.util.function.BiFunction;

public class TabCloseTool implements BiFunction<TabCloseTool.Request, ToolContext, TabCloseTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new BaseSandboxTools().browser_tab_close(request.index);
        return new Response(result, "Browser tab_close completed");
    }

    public record Request(
            @JsonProperty("index")
            @JsonPropertyDescription("The index of the tab to close")
            Integer index
    ) { }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}


