package runtime.engine.memory.service;

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
