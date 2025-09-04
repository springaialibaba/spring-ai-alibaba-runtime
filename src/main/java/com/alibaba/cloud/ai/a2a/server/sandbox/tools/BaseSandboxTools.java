package com.alibaba.cloud.ai.a2a.server.sandbox.tools;

import com.alibaba.cloud.ai.a2a.server.sandbox.ContainerModel;
import com.alibaba.cloud.ai.a2a.server.sandbox.SandboxManager;
import com.alibaba.cloud.ai.a2a.server.sandbox.SandboxType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 沙箱工具类，提供各种沙箱操作功能
 */
public class BaseSandboxTools {
    
    private final SandboxManager sandboxManager;
    private final HttpClient httpClient;

    public BaseSandboxTools() {
        this.sandboxManager = new SandboxManager();
        this.httpClient = new HttpClient();
    }
    
    /**
     * 执行IPython代码
     * 
     * @param code 要执行的Python代码
     * @return 执行结果
     */
    public String run_ipython_cell(String code) {
        try {
            // 获取沙箱
            ContainerModel sandbox = sandboxManager.getSandbox(SandboxType.BASE);
            
            // 确保沙箱正在运行
            if (!isSandboxRunning(sandbox)) {
                System.out.println("沙箱未运行，正在启动...");
                sandboxManager.startSandbox(SandboxType.BASE);
                // 等待沙箱启动
                Thread.sleep(3000);
            }
            
            // 构建请求URL
            String baseUrl = sandbox.getBaseUrl();
            String authToken = sandbox.getAuthToken();
            String requestUrl = baseUrl + "/tools/run_ipython_cell";

            System.out.println(authToken);
            System.out.println("运行代码: " + code);
            
            // 构建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + authToken);
            headers.put("Content-Type", "application/json");
            headers.put("Host", "localhost:" + sandbox.getPorts()[0]);

            // 构建请求体
            IpythonRequest request = new IpythonRequest(code);
            
            // 发送请求
            String response = httpClient.postJson(requestUrl, headers, request);
            
            System.out.println("IPython执行成功，响应: " + response);
            return response;
            
        } catch (Exception e) {
            String errorMsg = "执行IPython代码失败: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return errorMsg;
        }
    }
    
    /**
     * 检查沙箱是否正在运行
     * 
     * @param sandbox 沙箱模型
     * @return 是否正在运行
     */
    private boolean isSandboxRunning(ContainerModel sandbox) {
        try {
            String status = sandboxManager.getSandboxStatus(SandboxType.BASE);
            return "running".equals(status);
        } catch (Exception e) {
            System.err.println("检查沙箱状态失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 关闭资源
     */
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            System.err.println("关闭HTTP客户端失败: " + e.getMessage());
        }
    }
}
