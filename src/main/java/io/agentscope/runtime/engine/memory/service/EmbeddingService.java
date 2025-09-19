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
package io.agentscope.runtime.engine.memory.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 文本嵌入服务接口
 */
public interface EmbeddingService {
    
    /**
     * 将文本转换为向量嵌入
     * 
     * @param text 输入文本
     * @return 向量嵌入
     */
    CompletableFuture<List<Double>> embedText(String text);
    
    /**
     * 计算两个向量之间的余弦相似度
     * 
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 余弦相似度 (0-1之间，1表示完全相同)
     */
    double cosineSimilarity(List<Double> vector1, List<Double> vector2);
    
    /**
     * 计算两个向量之间的欧几里得距离
     * 
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 欧几里得距离
     */
    double euclideanDistance(List<Double> vector1, List<Double> vector2);
    
    /**
     * 获取嵌入向量的维度
     * 
     * @return 向量维度
     */
    int getEmbeddingDimension();
}
