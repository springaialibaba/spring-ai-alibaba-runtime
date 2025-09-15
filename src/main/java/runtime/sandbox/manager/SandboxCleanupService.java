package runtime.sandbox.manager;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import runtime.sandbox.tools.SandboxTools;

/**
 * 沙箱清理服务
 * 在Spring Boot应用关闭时自动清理所有沙箱容器
 */
@Service
public class SandboxCleanupService implements DisposableBean {

    @Override
    public void destroy() throws Exception {
        System.out.println("Spring Boot应用正在关闭，开始清理沙箱容器...");
        try {
            // 通过SandboxTools获取共享的SandboxManager实例
            SandboxTools sandboxTools = new SandboxTools();
            SandboxManager sandboxManager = sandboxTools.getSandboxManager();
            sandboxManager.cleanupAllSandboxes();
        } catch (Exception e) {
            System.err.println("清理沙箱容器时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("沙箱清理服务执行完成");
    }
}
