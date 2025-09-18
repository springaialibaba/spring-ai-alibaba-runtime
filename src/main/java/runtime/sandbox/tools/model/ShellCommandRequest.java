package runtime.sandbox.tools.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShellCommandRequest {
    @JsonProperty("command")
    private String command;

    public ShellCommandRequest() {
    }

    public ShellCommandRequest(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "ShellCommandRequest{" +
                "command='" + command + '\'' +
                '}';
    }
}

