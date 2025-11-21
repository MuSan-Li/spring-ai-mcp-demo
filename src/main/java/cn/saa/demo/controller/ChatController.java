package cn.saa.demo.controller;

import cn.saa.demo.entity.ChatHistory;
import cn.saa.demo.service.ChatHistoryService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 聊天控制器
 *
 * @author Administrator
 */
@RestController
public class ChatController {

    @Resource
    private ChatClient chatClient;

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 生成AI回复（带历史记录）
     * 使用 Spring AI 1.1.0 的新特性优化：
     * - 改进的 API 调用方式
     * - 记忆压缩（限制历史记录数量，避免上下文过长）
     *
     * @param message   用户消息
     * @param sessionId 会话ID，如果不提供则自动生成
     * @return AI回复
     */
    @GetMapping("/ai/generate")
    public String generate(
            @RequestParam(value = "message", defaultValue = "Tell me a joke") String message,
            @RequestParam(value = "sessionId", required = false, defaultValue = "1") String sessionId) {

        // 如果没有提供sessionId，则生成一个新的
        if (sessionId == null || sessionId.isEmpty() || "1".equals(sessionId)) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }

        // 获取历史记录（使用记忆压缩，只保留最近20条对话）
        List<ChatHistory> histories = chatHistoryService.getRecentHistoryBySessionId(sessionId, 20);
        List<Message> messageList = buildContext(histories);
        
        // 使用 Spring AI 1.1.0 改进的 API 调用
        String aiResponse = chatClient.prompt()
                .messages(messageList)
                .user(message)
                .call()
                .content();

        // 保存历史记录
        ChatHistory chatHistory = ChatHistory.builder()
                .sessionId(sessionId)
                .userMessage(message)
                .aiResponse(aiResponse)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        chatHistoryService.saveInfo(chatHistory);

        return aiResponse;
    }

    /**
     * 流式生成AI回复（带历史记录）
     * 使用 Spring AI 1.1.0 的新特性优化：
     * - 改进的流式处理
     * - 正确保存流式响应的完整内容
     *
     * @param message   用户消息
     * @param sessionId 会话ID，如果不提供则自动生成
     * @return AI回复流
     */
    @GetMapping(value = "/ai/generateStream", produces = "text/html;charset=UTF-8")
    public Flux<String> generateStream(
            @RequestParam(value = "message", defaultValue = "Tell me a joke") String message,
            @RequestParam(value = "sessionId", required = false, defaultValue = "1") String sessionId) {

        // 如果没有提供sessionId，则生成一个新的
        if (sessionId == null || sessionId.isEmpty() || "1".equals(sessionId)) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }

        // 获取历史记录（使用记忆压缩，只保留最近20条对话）
        List<ChatHistory> histories = chatHistoryService.getRecentHistoryBySessionId(sessionId, 20);
        List<Message> messageList = buildContext(histories);

        // 用于收集完整的AI响应
        AtomicReference<StringBuilder> fullResponse = new AtomicReference<>(new StringBuilder());
        final String finalSessionId = sessionId;
        final String finalMessage = message;

        // 流式返回并收集完整响应
        return chatClient.prompt()
                .messages(messageList)
                .user(message)
                .stream()
                .content()
                .doOnNext(chunk -> {
                    // 收集每个流式片段
                    fullResponse.get().append(chunk);
                })
                .doOnComplete(() -> {
                    // 流式响应完成后保存历史记录
                    String completeResponse = fullResponse.get().toString();
                    if (!completeResponse.isEmpty()) {
                        ChatHistory chatHistory = ChatHistory.builder()
                                .sessionId(finalSessionId)
                                .userMessage(finalMessage)
                                .aiResponse(completeResponse)
                                .createTime(LocalDateTime.now())
                                .updateTime(LocalDateTime.now())
                                .build();
                        chatHistoryService.saveInfo(chatHistory);
                    }
                })
                .doOnError(error -> {
                    // 错误处理：即使出错也尝试保存已收集的内容
                    String partialResponse = fullResponse.get().toString();
                    if (!partialResponse.isEmpty()) {
                        ChatHistory chatHistory = ChatHistory.builder()
                                .sessionId(finalSessionId)
                                .userMessage(finalMessage)
                                .aiResponse(partialResponse + "\n[响应中断: " + error.getMessage() + "]")
                                .createTime(LocalDateTime.now())
                                .updateTime(LocalDateTime.now())
                                .build();
                        chatHistoryService.saveInfo(chatHistory);
                    }
                });
    }

    /**
     * 获取指定会话的历史记录
     *
     * @param sessionId 会话ID
     * @return 历史记录列表
     */
    @GetMapping("/ai/history")
    public List<ChatHistory> getHistory(@RequestParam("sessionId") String sessionId) {
        return chatHistoryService.getHistoryBySessionId(sessionId);
    }

    /**
     * 删除指定会话的历史记录
     *
     * @param sessionId 会话ID
     * @return 删除结果
     */
    @DeleteMapping("/ai/history")
    public String deleteHistory(@RequestParam("sessionId") String sessionId) {
        boolean success = chatHistoryService.deleteBySessionId(sessionId);
        return success ? "删除成功" : "删除失败或记录不存在";
    }

    /**
     * 构建对话上下文
     * 使用 Spring AI 1.1.0 改进的消息处理方式
     *
     * @param histories 历史记录列表
     * @return 构建好的消息列表
     */
    private List<Message> buildContext(List<ChatHistory> histories) {
        List<Message> messageList = new ArrayList<>();
        
        // 添加系统提示（可以根据需要自定义）
        messageList.add(new SystemMessage("你是一个有用的AI助手，能够理解上下文并提供准确的回答。"));
        
        // 添加历史对话记录
        histories.forEach(item -> {
            messageList.add(new UserMessage(item.getUserMessage()));
            messageList.add(new AssistantMessage(item.getAiResponse()));
        });
        
        return messageList;
    }
}