package runtime.sandbox.tools.browser;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import runtime.sandbox.tools.SandboxTools;

import java.util.function.BiFunction;

/**
 * 浏览器文件上传工具
 */
public class FileUploadTool implements BiFunction<FileUploadTool.Request, ToolContext, FileUploadTool.Response> {

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String result = new SandboxTools().browser_file_upload(request.paths);
        return new Response(result, "Browser file upload completed");
    }

    public record Request(
            @JsonProperty(required = true, value = "paths")
            @JsonPropertyDescription("The absolute paths to the files to upload")
            String[] paths
    ) { }

    @JsonClassDescription("The result contains browser tool output and message")
    public record Response(String result, String message) {}
}
