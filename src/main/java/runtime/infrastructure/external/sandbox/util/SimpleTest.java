package runtime.infrastructure.external.sandbox.util;

import runtime.domain.tools.service.SandboxTools;

/**
 * 简化的IPython功能测试
 */
public class SimpleTest {
    
    public static void main(String[] args) {
        System.out.println("=== 开始测试IPython功能 ===");
        
        SandboxTools tools = new SandboxTools();
        
        try {
            // 测试简单的Python代码
            String code = "print('Hello World from IPython!')";
            System.out.println("执行代码: " + code);
            
            String result = tools.run_ipython_cell(code);
            System.out.println("执行结果: " + result);
            
        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭资源
            tools.close();
            System.out.println("=== 测试完成 ===");
        }
    }
}
