package com.alibaba.cloud.ai.a2a.server.sandbox;

public class Main {

    public static void main(String[] args) {


        try {
            SandboxManager sandboxManager = new SandboxManager();

            System.out.println("正在创建沙箱...");
            ContainerModel model = sandboxManager.getSandbox(SandboxType.BASE);
            sandboxManager.startSandbox(SandboxType.BASE);
            System.out.println("容器启动成功！");

            // 检查容器状态
            String status = sandboxManager.getSandboxStatus(SandboxType.BASE);
            System.out.println("容器状态: " + status);

        } catch (Exception e) {
            System.err.println("操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
