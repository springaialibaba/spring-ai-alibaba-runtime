package runtime.sandbox.tools.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.sandbox.tools.SandboxTools;

import java.util.function.BiFunction;

public class TabCloseTool implements BiFunction<TabCloseTool.Request, ToolContext, TabCloseTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_tab_close(request.index);
        return new Response(result, "Browser tab_close completed");
    }

    public record Request(
            @JsonProperty("index")
            @JsonPropertyDescription("The index of the tab to close")
            Integer index
    ) { 
        public Request {
            if (index == null) {
                index = -1;
            }
        }
    }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}


