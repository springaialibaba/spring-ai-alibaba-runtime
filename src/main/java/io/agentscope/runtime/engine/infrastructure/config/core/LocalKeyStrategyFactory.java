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
package io.agentscope.runtime.engine.infrastructure.config.core;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.MergeStrategy;
import org.springframework.stereotype.Component;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;

@Component
public class LocalKeyStrategyFactory {

    private static final Map<String, Class<? extends KeyStrategy>> STRATEGY_MAP = new HashMap<>();

    static {
        STRATEGY_MAP.put("replace", ReplaceStrategy.class);
        STRATEGY_MAP.put("append", AppendStrategy.class);
        STRATEGY_MAP.put("merge", MergeStrategy.class);
    }

    public KeyStrategy createStrategy(String strategyType) {
        Class<? extends KeyStrategy> strategyClass = STRATEGY_MAP.get(strategyType);
        if (strategyClass == null) {
            throw new IllegalArgumentException("Unknown strategy type: " + strategyType);
        }

        try {
            return strategyClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create strategy instance: " + strategyType, e);
        }
    }

    public com.alibaba.cloud.ai.graph.KeyStrategyFactory createStrategies(Map<String, String> strategyConfigs) {
        Map<String, KeyStrategy> strategies = new HashMap<>();
        for (Map.Entry<String, String> entry : strategyConfigs.entrySet()) {
            String key = entry.getKey();
            String strategyType = entry.getValue();
            strategies.put(key, createStrategy(strategyType));
        }
        return () -> strategies;
    }

}
