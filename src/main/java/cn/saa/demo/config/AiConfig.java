package cn.saa.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI配置类 - 使用 Spring AI 1.1.0 新特性
 * 
 * @author Administrator
 */
@Configuration
public class AiConfig {

    /**
     * 配置 ChatClient，利用 Spring AI 1.1.0 的新特性
     * - 支持默认系统提示
     * - 支持提示缓存（通过配置）
     * - 改进的可观察性
     */
    @Bean
    public ChatClient chatClient(DeepSeekChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("你是一个有用的AI助手，能够理解上下文并提供准确的回答。")
                .build();
    }
}
