package runtime.infrastructure.external.sandbox.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * HTTP客户端工具类，用于发送HTTP请求
 */
public class HttpClient {
    
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public HttpClient() {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 发送POST请求
     * 
     * @param url 请求URL
     * @param headers 请求头
     * @param requestBody 请求体
     * @return 响应内容
     * @throws IOException 请求异常
     */
    public String post(String url, Map<String, String> headers, String requestBody) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        
        // 设置请求头
        if (headers != null) {
            headers.forEach(httpPost::setHeader);
        }
        
        // 设置请求体
        if (requestBody != null) {
            StringEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
        }
        
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            
            if (statusCode >= 200 && statusCode < 300) {
                return responseBody;
            } else {
                throw new IOException("HTTP请求失败，状态码: " + statusCode + ", 响应: " + responseBody);
            }
        }
    }
    
    /**
     * 发送JSON POST请求
     * 
     * @param url 请求URL
     * @param headers 请求头
     * @param requestData 请求数据对象
     * @return 响应内容
     * @throws IOException 请求异常
     */
    public String postJson(String url, Map<String, String> headers, Object requestData) throws IOException {
        String jsonBody = objectMapper.writeValueAsString(requestData);
        return post(url, headers, jsonBody);
    }

    /**
     * 发送GET请求
     *
     * @param url 请求URL
     * @param headers 请求头
     * @return 响应内容
     * @throws IOException 请求异常
     */
    public String get(String url, Map<String, String> headers) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        if (headers != null) {
            headers.forEach(httpGet::setHeader);
        }
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = response.getEntity() != null ? new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8) : "";
            if (statusCode >= 200 && statusCode < 300) {
                return responseBody;
            } else {
                throw new IOException("HTTP请求失败，状态码: " + statusCode + ", 响应: " + responseBody);
            }
        }
    }
    
    /**
     * 关闭HTTP客户端
     */
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
