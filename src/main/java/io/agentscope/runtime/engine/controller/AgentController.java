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
package io.agentscope.runtime.engine.controller;

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
