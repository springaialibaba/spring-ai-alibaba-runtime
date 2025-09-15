package runtime.sandbox.tools.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * IPython执行请求的数据模型
 */
public class IpythonRequest {
    
    @JsonProperty("code")
    private String code;
    
    public IpythonRequest() {}
    
    public IpythonRequest(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    @Override
    public String toString() {
        return "IpythonRequest{" +
                "code='" + code + '\'' +
                '}';
    }
}
