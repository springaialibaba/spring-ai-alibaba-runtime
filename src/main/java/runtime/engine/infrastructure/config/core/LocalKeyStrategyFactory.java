package runtime.engine.infrastructure.config.core;

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
