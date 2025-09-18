package runtime.engine.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class AgentController {

    @GetMapping(value = "/", produces = "application/json")
    public String root() {
        return "{\"message\":\"Agent Service is running\"}";
    }


    @GetMapping(value = "${controller.endpoints.health:/health}", produces = "application/json")
    public String health() {
        return "{\"status\":\"healthy\",\"timestamp\":\"" + LocalDateTime.now() + "\",\"service\":\"agent-service\"}";
    }


    @GetMapping(value = "${controller.endpoints.readiness:/readiness}", produces = "application/json")
    public String readiness() {
        return "success";
    }

    @GetMapping(value = "${controller.endpoints.liveness:/liveness}", produces = "application/json")
    public String liveness() {
        return "success";
    }

}
