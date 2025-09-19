/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.agentscope.runtime.sandbox.manager;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import io.agentscope.runtime.sandbox.tools.SandboxTools;

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
