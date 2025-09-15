package runtime.sandbox.manager.model;

/**
 * 卷挂载配置类
 * 用于表示Docker容器的卷挂载配置
 */
public class VolumeBinding {
    private String hostPath;
    private String containerPath;
    private String mode;

    public VolumeBinding(String hostPath, String containerPath, String mode) {
        this.hostPath = hostPath;
        this.containerPath = containerPath;
        this.mode = mode;
    }

    public VolumeBinding(String hostPath, String containerPath) {
        this(hostPath, containerPath, "rw");
    }

    public String getHostPath() {
        return hostPath;
    }

    public void setHostPath(String hostPath) {
        this.hostPath = hostPath;
    }

    public String getContainerPath() {
        return containerPath;
    }

    public void setContainerPath(String containerPath) {
        this.containerPath = containerPath;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "manager.VolumeBinding{" +
                "hostPath='" + hostPath + '\'' +
                ", containerPath='" + containerPath + '\'' +
                ", mode='" + mode + '\'' +
                '}';
    }
}
