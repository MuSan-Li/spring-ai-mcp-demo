package cn.saa.demo.service;

import cn.saa.demo.entity.McpToolData;
import cn.saa.demo.service.McpToolRegistryService.RemoteMcpClientWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 远程 MCP 工具调用服务
 * 负责调用已注册的远程 MCP 工具
 *
 * @author Administrator
 */
@Slf4j
@Service
public class RemoteMcpToolInvokeService {

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private McpToolRegistryService mcpToolRegistryService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 调用远程 MCP 工具
     *
     * @param tool     工具实体
     * @param toolName 工具名称（MCP 协议中的工具名称）
     * @param params   工具参数
     * @return 调用结果
     */
    public Object invokeRemoteTool(McpToolData tool, String toolName, Map<String, Object> params) {
        try {
            // 检查工具是否已注册
            if (!mcpToolRegistryService.isRegistered(tool.getId())) {
                throw new IllegalStateException("工具未注册: " + tool.getName());
            }

            // 获取远程客户端包装器
            String beanName = "mcpClient_" + tool.getId();
            RemoteMcpClientWrapper clientWrapper = applicationContext.getBean(beanName, RemoteMcpClientWrapper.class);

            // 根据传输类型调用不同的方法
            String transportType = clientWrapper.transportType();
            String url = clientWrapper.url();
            Map<String, String> headers = clientWrapper.headers();

            switch (transportType.toLowerCase()) {
                case "http":
                case "sse":
                    return invokeHttpTool(url, toolName, params, headers);
                case "websocket":
                    // WebSocket 调用需要特殊处理，这里先返回提示
                    throw new UnsupportedOperationException("WebSocket 传输方式暂未实现");
                default:
                    throw new IllegalArgumentException("不支持的传输类型: " + transportType);
            }
        } catch (Exception e) {
            log.error("调用远程工具失败: {} -> {}", tool.getName(), toolName, e);
            throw new RuntimeException("调用远程工具失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过 HTTP/SSE 调用工具
     */
    private Object invokeHttpTool(String baseUrl, String toolName, Map<String, Object> params, Map<String, String> headers) {
        try {
            // 构建请求 URL（根据 MCP 协议规范）
            // 注意：这里需要根据实际的 MCP 服务器 API 规范来调整
            String requestUrl = baseUrl;
            if (!requestUrl.endsWith("/")) {
                requestUrl += "/";
            }
            requestUrl += "tools/" + toolName + "/invoke";

            // 构建请求头
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            if (headers != null) {
                headers.forEach(httpHeaders::set);
            }

            // 构建请求体（MCP 协议格式）
            Map<String, Object> requestBody = Map.of(
                    "name", toolName,
                    "arguments", params != null ? params : Map.of()
            );

            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, httpHeaders);

            // 发送 POST 请求
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("远程工具调用失败，状态码: " + response.getStatusCode());
            }

            // 解析响应
            String responseBody = response.getBody();
            if (responseBody == null || responseBody.isEmpty()) {
                return Map.of("success", true, "result", "");
            }

            // 尝试解析为 JSON
            try {
                return objectMapper.readValue(responseBody, Map.class);
            } catch (Exception e) {
                // 如果不是 JSON，直接返回字符串
                return responseBody;
            }
        } catch (Exception e) {
            log.error("HTTP 工具调用失败: {} -> {}", baseUrl, toolName, e);
            throw new RuntimeException("HTTP 工具调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查远程工具是否可用
     *
     * @param tool 工具实体
     * @return 是否可用
     */
    public boolean isToolAvailable(McpToolData tool) {
        try {
            if (!mcpToolRegistryService.isRegistered(tool.getId())) {
                return false;
            }

            String beanName = "mcpClient_" + tool.getId();
            RemoteMcpClientWrapper clientWrapper = applicationContext.getBean(beanName, RemoteMcpClientWrapper.class);
            String url = clientWrapper.url();

            // 发送健康检查请求
            HttpHeaders headers = new HttpHeaders();
            Map<String, String> clientHeaders = clientWrapper.headers();
            if (clientHeaders != null) {
                clientHeaders.forEach(headers::set);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url + "/health",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("工具健康检查失败: {}", tool.getName(), e);
            return false;
        }
    }
}


