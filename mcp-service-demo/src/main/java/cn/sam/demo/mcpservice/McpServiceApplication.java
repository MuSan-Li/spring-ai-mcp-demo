package cn.sam.demo.mcpservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MCP 服务器应用
 * 提供 MCP 服务器端点，支持工具调用
 * 
 * @author Administrator
 */
@Slf4j
@SpringBootApplication
public class McpServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServiceApplication.class, args);
        log.info("MCP Service started successfully!");
    }
}

