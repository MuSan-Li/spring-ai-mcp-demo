package cn.sam.demo.mcpclient;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MCP 客户端应用
 * 提供管理界面和 MCP 客户端功能
 * 
 * @author Administrator
 */
@Slf4j
@SpringBootApplication
@MapperScan("cn.sam.demo.mcpclient.mapper")
public class McpClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpClientApplication.class, args);
        log.info("MCP Client started successfully!");
    }
}

