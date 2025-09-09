package runtime.domain.tools.service.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.domain.tools.service.SandboxTools;

import java.util.function.BiFunction;

/**
 * 浏览器PDF保存工具
 */
public class PdfSaveTool implements BiFunction<PdfSaveTool.Request, ToolContext, PdfSaveTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_pdf_save(request.filename);
        return new Response(result, "Browser PDF save completed");
    }

    public record Request(
            @JsonProperty(value = "filename")
            @JsonPropertyDescription("File name to save the pdf to")
            String filename
    ) { 
        public Request {
            // 为可选参数提供默认值处理
            if (filename == null) {
                filename = "";
            }
        }
    }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}
