package runtime.infrastructure.external.sandbox.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * IPython执行响应的数据模型
 */
public class IpythonResponse {
    
    @JsonProperty("result")
    private String result;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("success")
    private boolean success;
    
    public IpythonResponse() {}
    
    public IpythonResponse(String result, String error, boolean success) {
        this.result = result;
        this.error = error;
        this.success = success;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    @Override
    public String toString() {
        return "IpythonResponse{" +
                "result='" + result + '\'' +
                ", error='" + error + '\'' +
                ", success=" + success +
                '}';
    }
}
