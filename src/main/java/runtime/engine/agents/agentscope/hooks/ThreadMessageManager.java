package runtime.engine.agents.agentscope.hooks;

import reactor.core.publisher.Flux;
import runtime.engine.schemas.agent.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 线程消息管理器
 * 对应Python版本的hooks.py中的消息管理功能
 */
public class ThreadMessageManager {
    
    private static final ThreadMessageManager INSTANCE = new ThreadMessageManager();
    private static final int TIMEOUT_SECONDS = 30;
    
    private final ConcurrentHashMap<String, BlockingQueue<Message>> messageQueues;
    private final ConcurrentHashMap<String, Object> locks;
    
    private ThreadMessageManager() {
        this.messageQueues = new ConcurrentHashMap<>();
        this.locks = new ConcurrentHashMap<>();
    }
    
    public static ThreadMessageManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册线程
     * 对应Python版本的clear_msg_instances
     */
    public void registerThread(String threadId) {
        locks.putIfAbsent(threadId, new Object());
        messageQueues.putIfAbsent(threadId, new LinkedBlockingQueue<>());
    }
    
    /**
     * 添加消息
     * 对应Python版本的pre_speak_msg_buffer_hook
     */
    public void addMessage(String threadId, Message message) {
        BlockingQueue<Message> queue = messageQueues.get(threadId);
        if (queue != null) {
            queue.offer(message);
        }
    }
    
    /**
     * 获取消息流
     * 对应Python版本的get_msg_instances
     */
    public Flux<Message> getMessageStream(String threadId) {
        return Flux.create(sink -> {
            BlockingQueue<Message> queue = messageQueues.get(threadId);
            if (queue == null) {
                sink.error(new IllegalArgumentException("Thread not registered: " + threadId));
                return;
            }
            
            Thread consumerThread = new Thread(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    
                    while (!Thread.currentThread().isInterrupted()) {
                        Message message = queue.poll(100, TimeUnit.MILLISECONDS);
                        
                        if (message != null) {
                            sink.next(message);
                        } else {
                            // 检查超时
                            if (System.currentTimeMillis() - startTime > TIMEOUT_SECONDS * 1000) {
                                sink.error(new TimeoutException("Message stream timeout"));
                                break;
                            }
                            
                            // 检查队列是否为空且线程已结束
                            if (queue.isEmpty()) {
                                // 这里可以添加更复杂的逻辑来检查线程是否已结束
                                // 暂时简单处理
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    sink.complete();
                }
            });
            
            consumerThread.setName("MessageConsumer-" + threadId);
            consumerThread.start();
            
            sink.onCancel(() -> {
                consumerThread.interrupt();
            });
        });
    }
    
    /**
     * 清理消息
     * 对应Python版本的clear_msg_instances
     */
    public void clearMessages(String threadId) {
        BlockingQueue<Message> queue = messageQueues.get(threadId);
        if (queue != null) {
            queue.clear();
        }
    }
    
    /**
     * 移除线程
     */
    public void removeThread(String threadId) {
        messageQueues.remove(threadId);
        locks.remove(threadId);
    }
    
    /**
     * 获取队列大小
     */
    public int getQueueSize(String threadId) {
        BlockingQueue<Message> queue = messageQueues.get(threadId);
        return queue != null ? queue.size() : 0;
    }
}
