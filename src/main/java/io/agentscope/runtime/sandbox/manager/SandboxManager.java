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

import io.agentscope.runtime.sandbox.manager.model.ContainerModel;
import io.agentscope.runtime.sandbox.manager.model.SandboxType;
import io.agentscope.runtime.sandbox.manager.model.VolumeBinding;
import io.agentscope.runtime.sandbox.manager.util.RandomStringGenerator;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

public class SandboxManager {
    DockerManager dockerManager = new DockerManager();
    private Map<SandboxType, ContainerModel> sandboxMap = new HashMap<>();
    String BROWSER_SESSION_ID="123e4567-e89b-12d3-a456-426614174000";
    public DockerClient client;

    public SandboxManager() {
        client = dockerManager.connectDocker();
    }

    private Map<SandboxType, String> typeNameMap= new HashMap<SandboxType, String>() {{
        put(SandboxType.BASE, "agentscope/runtime-manager-base");
        put(SandboxType.FILESYSTEM, "agentscope/runtime-manager-filesystem");
        put(SandboxType.BROWSER, "agentscope/runtime-manager-browser");
    }};

    public ContainerModel getSandbox(SandboxType sandboxType){
        if(sandboxMap.containsKey(sandboxType)){
            return sandboxMap.get(sandboxType);
        } else {
            DockerManager dockerManager = new DockerManager();
            String workdir = "/workspace";
            String default_mount_dir = "sessions_mount_dir";
            String[] portsArray = {"80/tcp"};
            List<String> ports = Arrays.asList(portsArray);
            
            // 创建端口映射
            Map<String, Integer> portMapping = createPortMapping(ports);
            System.out.println("端口映射: " + portMapping);

            String imageName = "agentscope/runtime-manager-base";
            if(typeNameMap.containsKey(sandboxType)){
                imageName = typeNameMap.get(sandboxType);
            }

            Map<String, String> environment = new HashMap<>();
            String secretToken = RandomStringGenerator.generateRandomString(16);
            environment.put("SECRET_TOKEN", secretToken);

            String sessionId = secretToken;
            // 使用绝对路径
            String currentDir = System.getProperty("user.dir");
            String mountDir = currentDir + "/" + default_mount_dir + "/" + sessionId;
            java.io.File file = new java.io.File(mountDir);
            if (!file.exists()) {
                file.mkdirs();
            }

            List<VolumeBinding> volumeBindings = new ArrayList<>();
            volumeBindings.add(new VolumeBinding(
                    mountDir,
                    workdir,
                    "rw"
            ));

            String runtimeConfig = "runc"; // 或者 "nvidia" 等
            String containerName = "sandbox_" + sandboxType.name().toLowerCase() + "_" + sessionId;

            CreateContainerResponse container = dockerManager.createContainers(
                    this.client,
                    containerName,
                    imageName,
                    ports,
                    portMapping,
                    volumeBindings,
                    environment,
                    runtimeConfig
            );

            // 将映射后的主机端口转换为字符串数组
            String[] mappedPorts = portMapping.values().stream()
                    .map(String::valueOf)
                    .toArray(String[]::new);

            ContainerModel containerModel = ContainerModel.builder()
                    .sessionId(sessionId)
                    .containerId(container.getId())
                    .containerName(containerName)
                    .baseUrl(String.format("http://localhost:%s/fastapi", mappedPorts[0]))
                    .browserUrl(String.format("http://localhost:%s/steel-api/%s", mappedPorts[0], secretToken))
                    .frontBrowserWS(String.format("ws://localhost:%s/steel-api/%s/v1/sessions/cast", mappedPorts[0], secretToken))
                    .clientBrowserWS(String.format("ws://localhost:%s/steel-api/%s/&sessionId=%s", mappedPorts[0], secretToken, BROWSER_SESSION_ID))
                    .artifactsSIO(String.format("http://localhost:%s/v1", mappedPorts[0]))
                    .ports(mappedPorts)
                    .mountDir(workdir)
                    .authToken(secretToken)
                    .build();

            sandboxMap.put(sandboxType, containerModel);
            
            return containerModel;
        }
    }

    /**
     * 启动沙箱容器
     * @param sandboxType 沙箱类型
     */
    public void startSandbox(SandboxType sandboxType) {
        ContainerModel containerModel = sandboxMap.get(sandboxType);
        if (containerModel != null) {
            dockerManager.startContainer(this.client, containerModel.getContainerId());
            System.out.println("容器状态更新为: running");
            sandboxMap.put(sandboxType, containerModel);
        }
    }

    /**
     * 停止沙箱容器
     * @param sandboxType 沙箱类型
     */
    public void stopSandbox(SandboxType sandboxType) {
        ContainerModel containerModel = sandboxMap.get(sandboxType);
        if (containerModel != null) {
            dockerManager.stopContainer(this.client, containerModel.getContainerId());
            System.out.println("容器状态更新为: stopped");
            sandboxMap.put(sandboxType, containerModel);
        }
    }

    /**
     * 删除沙箱容器
     * @param sandboxType 沙箱类型
     */
    public void removeSandbox(SandboxType sandboxType) {
        ContainerModel containerModel = sandboxMap.get(sandboxType);
        if (containerModel != null) {
            DockerManager dockerManager = new DockerManager();
            dockerManager.removeContainer(this.client, containerModel.getContainerId());
            sandboxMap.remove(sandboxType);
        }
    }


    public void stopAndRemoveSandbox(SandboxType sandboxType){
        ContainerModel containerModel = sandboxMap.get(sandboxType);
        if (containerModel != null) {
            try {
                DockerManager dockerManager = new DockerManager();
                String containerId = containerModel.getContainerId();
                String containerName = containerModel.getContainerName();
                
                System.out.println("正在停止并删除 " + sandboxType + " 沙箱 (容器ID: " + containerId + ", 名称: " + containerName + ")");
                
                // 先尝试停止容器
                dockerManager.stopContainer(this.client, containerId);
                
                // 等待一小段时间确保容器完全停止
                Thread.sleep(1000);
                
                // 强制删除容器
                dockerManager.removeContainer(this.client, containerId);
                
                // 从映射中移除
                sandboxMap.remove(sandboxType);
                
                System.out.println(sandboxType + " 沙箱已成功删除");
            } catch (Exception e) {
                System.err.println("删除 " + sandboxType + " 沙箱时出错: " + e.getMessage());
                e.printStackTrace();
                // 即使删除失败，也要从映射中移除，避免内存泄漏
                sandboxMap.remove(sandboxType);
            }
        } else {
            System.out.println("未找到 " + sandboxType + " 沙箱，可能已经被删除");
        }
    }

    /**
     * 获取沙箱状态
     * @param sandboxType 沙箱类型
     * @return 沙箱状态
     */
    public String getSandboxStatus(SandboxType sandboxType) {
        ContainerModel containerModel = sandboxMap.get(sandboxType);
        if (containerModel != null) {
            DockerManager dockerManager = new DockerManager();
            return dockerManager.getContainerStatus(this.client, containerModel.getContainerId());
        }
        return "not_found";
    }

    /**
     * 获取所有沙箱信息
     * @return 沙箱映射
     */
    public Map<SandboxType, ContainerModel> getAllSandboxes() {
        return new HashMap<>(sandboxMap);
    }

    /**
     * 清理所有沙箱容器
     */
    public void cleanupAllSandboxes() {
        if (sandboxMap == null || sandboxMap.isEmpty()) {
            System.out.println("没有需要清理的沙箱容器");
            return;
        }

        System.out.println("开始清理所有沙箱容器...");
        System.out.println("当前沙箱数量: " + sandboxMap.size());
        System.out.println("沙箱类型: " + sandboxMap.keySet());

        for (SandboxType sandboxType : new HashSet<>(sandboxMap.keySet())) {
            try {
                System.out.println("正在清理 " + sandboxType + " 沙箱...");
                stopAndRemoveSandbox(sandboxType);
                System.out.println(sandboxType + " 沙箱清理完成");
            } catch (Exception e) {
                System.err.println("清理 " + sandboxType + " 沙箱时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }

        sandboxMap.clear();
        System.out.println("所有沙箱容器清理完成！");
    }

    /**
     * 查找可用的端口
     * @param count 需要查找的端口数量
     * @return 可用端口列表
     */
    private List<Integer> findFreePorts(int count) {
        List<Integer> freePorts = new ArrayList<>();
        int startPort = 8000; // 从8000开始查找
        int maxAttempts = 1000; // 最多尝试1000个端口
        
        for (int i = 0; i < count && freePorts.size() < count; i++) {
            for (int port = startPort + i; port < startPort + maxAttempts; port++) {
                if (isPortAvailable(port)) {
                    freePorts.add(port);
                    break;
                }
            }
        }
        
        return freePorts;
    }

    /**
     * 检查端口是否可用
     * @param port 端口号
     * @return 是否可用
     */
    private boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // 端口可用
            return true;
        } catch (IOException e) {
            // 端口被占用
            return false;
        }
    }

    /**
     * 创建端口映射
     * @param containerPorts 容器端口列表
     * @return 端口映射Map
     */
    private Map<String, Integer> createPortMapping(List<String> containerPorts) {
        Map<String, Integer> portMapping = new HashMap<>();
        
        if (containerPorts != null && !containerPorts.isEmpty()) {
            List<Integer> freePorts = findFreePorts(containerPorts.size());
            
            for (int i = 0; i < containerPorts.size() && i < freePorts.size(); i++) {
                String containerPort = containerPorts.get(i);
                Integer hostPort = freePorts.get(i);
                portMapping.put(containerPort, hostPort);
            }
        }
        
        return portMapping;
    }

}
