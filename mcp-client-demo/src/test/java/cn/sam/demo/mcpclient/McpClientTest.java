package cn.sam.demo.mcpclient;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.http.HttpClient;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MCP 客户端测试
 * 测试连接到 mcp-service-demo
 * 
 * 问题分析：
 * - HttpClientSseClientTransport 使用 SSE 传输，需要访问 /sse 端点
 * - 但服务端配置的是 streamable-http 协议，端点路径是 /mcp
 * - 应该使用 HttpClientStreamableHttpTransport 而不是 HttpClientSseClientTransport
 */
@SpringBootTest
class McpClientTest {

    @Test
    void testConnectToMcpServerStreamable() {
        // 使用 HttpClientStreamableHttpTransport（Streamable HTTP 传输）
        // 而不是 HttpClientSseClientTransport（SSE 传输）
        // 因为服务端配置的是 streamable-http 协议，端点路径是 /mcp
        String mcpServerUrl = "http://localhost:9899";

        System.out.println("============================================");
        System.out.println("MCP 客户端连接测试");
        System.out.println("============================================");
        System.out.println("服务器地址: " + mcpServerUrl);
        System.out.println("实际访问路径: " + mcpServerUrl + "/mcp");
        System.out.println();

        try {
            HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.NORMAL);

            // 使用 HttpClientStreamableHttpTransport，它会自动追加 /mcp 到 base URL
            HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport
                    .builder(mcpServerUrl)
                    .clientBuilder(clientBuilder)
                    .build();

            McpSyncClient client = McpClient.sync(transport).build();

            System.out.println("正在初始化 MCP 客户端...");
            client.initialize();
            System.out.println("初始化成功！");
            System.out.println("是否已经初始化: " + client.isInitialized());
            System.out.println();

            System.out.println("正在获取工具列表...");
            McpSchema.ListToolsResult listToolsResult = client.listTools();
            List<McpSchema.Tool> tools = listToolsResult.tools();

            if (tools.isEmpty()) {
                System.out.println("警告：未找到任何工具");
                return;
            }

            System.out.println("获取到的工具数量: " + tools.size());
            System.out.println("工具列表: " + tools.stream().map(McpSchema.Tool::name)
                    .collect(Collectors.joining(", ")));
            System.out.println();

            for (McpSchema.Tool tool : tools) {
                System.out.println("工具名称: " + tool.name());
                System.out.println("工具描述: " + tool.description());
                System.out.println("工具参数 Schema: " + tool.inputSchema());
                System.out.println();
            }

            System.out.println("============================================");
            System.out.println("测试完成！");
            System.out.println("============================================");

        } catch (io.modelcontextprotocol.spec.McpTransportException e) {
            System.err.println("============================================");
            System.err.println("MCP 传输错误！");
            System.err.println("============================================");
            System.err.println("错误信息: " + e.getMessage());

            if (e.getMessage() != null && e.getMessage().contains("404")) {
                System.err.println();
                System.err.println("这是 404 错误，说明 MCP 服务器端点不存在！");
                System.err.println();
                System.err.println("请确保：");
                System.err.println("1. mcp-service-demo 应用已启动（运行在 9899 端口）");
                System.err.println("2. MCP 服务器端点路径正确（/mcp）");
                System.err.println("3. 服务端配置了 streamable-http 协议");
            }
            System.err.println("============================================");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("============================================");
            System.err.println("发生错误！");
            System.err.println("============================================");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("错误类型: " + e.getClass().getName());
            System.err.println("============================================");
            e.printStackTrace();
        }
    }


    @Test
    void testConnectToMcpServerSse() {
        McpClientTransport transport = HttpClientSseClientTransport.builder("http://localhost:9899").build();
        var client = McpClient.sync(transport)
                .loggingConsumer(message -> System.out.println(">> Client Logging: " + message))
                .build();

        client.initialize();

        client.ping();

        // List and demonstrate tools
        McpSchema.ListToolsResult toolsList = client.listTools();

        System.out.println("Available Tools = " + toolsList);

        toolsList.tools().forEach(tool -> System.out.printf("Tool: %s, description: %s, schema: %s%n",
                tool.name(), tool.description(), tool.inputSchema()));

    }
}

