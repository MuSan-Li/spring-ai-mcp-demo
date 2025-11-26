package cn.sam.demo.mcpclient.config;

import cn.sam.demo.mcpclient.entity.McpToolData;
import cn.sam.demo.mcpclient.service.McpToolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 工具启动监听器
 * 在应用启动时自动注册本地 MCP 工具到数据库
 *
 * @author Administrator
 */
@Slf4j
@Component
public class McpToolStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private McpToolService mcpToolService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("开始注册远程 MCP 工具到数据库（从 mcp-service-demo 获取）...");

        // 定义要注册的工具列表（这些工具在 mcp-service-demo 中）
        // 类型改为 REMOTE，指向 mcp-service-demo (http://127.0.0.1:9899)
        List<ToolDefinition> toolDefinitions = List.of(
                new ToolDefinition(
                        "get_current_time",
                        "获取当前时间",
                        "获取当前系统时间，支持多种时间格式（datetime、date、time）",
                        createTimeToolConfig()
                ),
                new ToolDefinition(
                        "calculator_add",
                        "计算器-加法",
                        "计算两个数字的和",
                        createCalculatorAddConfig()
                ),
                new ToolDefinition(
                        "calculator_multiply",
                        "计算器-乘法",
                        "计算两个数字的乘积",
                        createCalculatorMultiplyConfig()
                ),
                new ToolDefinition(
                        "string_reverse",
                        "字符串反转",
                        "反转字符串",
                        createStringReverseConfig()
                ),
                new ToolDefinition(
                        "string_uppercase",
                        "字符串转大写",
                        "将字符串转换为大写",
                        createStringUppercaseConfig()
                )
        );

        // 注册每个工具
        for (ToolDefinition toolDef : toolDefinitions) {
            registerOrUpdateTool(toolDef);
        }

        log.info("远程 MCP 工具注册完成，共注册 {} 个工具（指向 mcp-service-demo）", toolDefinitions.size());
    }

    /**
     * 注册或更新工具
     */
    private void registerOrUpdateTool(ToolDefinition toolDef) {
        try {
            // 根据名称查找是否已存在
            List<McpToolData> existingTools = mcpToolService.searchByName(toolDef.name);
            McpToolData tool = null;

            // 查找完全匹配的工具（LOCAL 或 REMOTE 类型都可以）
            for (McpToolData t : existingTools) {
                if (toolDef.name.equals(t.getName())) {
                    tool = t;
                    break;
                }
            }

            if (tool == null) {
                // 创建新工具 - 类型改为 REMOTE，指向 mcp-service-demo
                tool = McpToolData.builder()
                        .name(toolDef.name)
                        .description(toolDef.description)
                        .type(McpToolData.Type.REMOTE)  // 改为 REMOTE 类型
                        .status(McpToolData.Status.ENABLED)
                        .configJson(toolDef.configJson)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                log.info("创建新远程工具: {} (指向 mcp-service-demo)", toolDef.name);
            } else {
                // 更新现有工具 - 如果原来是 LOCAL，改为 REMOTE
                if (McpToolData.Type.LOCAL.equals(tool.getType())) {
                    tool.setType(McpToolData.Type.REMOTE);
                    log.info("将工具类型从 LOCAL 改为 REMOTE: {}", toolDef.name);
                }
                tool.setDescription(toolDef.description);
                tool.setConfigJson(toolDef.configJson);
                tool.setUpdateTime(LocalDateTime.now());
                tool.setStatus(McpToolData.Status.ENABLED); // 确保启用
                log.info("更新现有工具: {}", toolDef.name);
            }

            mcpToolService.saveOrUpdateInfo(tool);
        } catch (Exception e) {
            log.error("注册工具失败: {}", toolDef.name, e);
        }
    }

    /**
     * 创建时间工具配置
     */
    private String createTimeToolConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("tool_name", "get_current_time");
            config.put("tool_type", "REMOTE");
            // 添加 transport 配置，指向 mcp-service-demo
            config.put("transport", Map.of(
                    "type", "http",
                    "url", "http://127.0.0.1:9899"
            ));
            config.put("function", Map.of(
                    "name", "get_current_time",
                    "description", "获取当前系统时间，支持多种时间格式（datetime、date、time）",
                    "parameters", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "format", Map.of(
                                            "type", "string",
                                            "description", "时间格式，可选值：datetime(默认), date, time",
                                            "enum", List.of("datetime", "date", "time")
                                    )
                            )
                    )
            ));
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            log.error("创建时间工具配置失败", e);
            return "{}";
        }
    }

    /**
     * 创建加法计算器配置
     */
    private String createCalculatorAddConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("tool_name", "calculator_add");
            config.put("tool_type", "REMOTE");
            // 添加 transport 配置，指向 mcp-service-demo
            config.put("transport", Map.of(
                    "type", "http",
                    "url", "http://127.0.0.1:9899"
            ));
            config.put("function", Map.of(
                    "name", "calculator_add",
                    "description", "计算两个数字的和",
                    "parameters", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "a", Map.of("type", "number", "description", "第一个数字"),
                                    "b", Map.of("type", "number", "description", "第二个数字")
                            ),
                            "required", List.of("a", "b")
                    )
            ));
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            log.error("创建加法计算器配置失败", e);
            return "{}";
        }
    }

    /**
     * 创建乘法计算器配置
     */
    private String createCalculatorMultiplyConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("tool_name", "calculator_multiply");
            config.put("tool_type", "REMOTE");
            // 添加 transport 配置，指向 mcp-service-demo
            config.put("transport", Map.of(
                    "type", "http",
                    "url", "http://127.0.0.1:9899"
            ));
            config.put("function", Map.of(
                    "name", "calculator_multiply",
                    "description", "计算两个数字的乘积",
                    "parameters", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "a", Map.of("type", "number", "description", "第一个数字"),
                                    "b", Map.of("type", "number", "description", "第二个数字")
                            ),
                            "required", List.of("a", "b")
                    )
            ));
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            log.error("创建乘法计算器配置失败", e);
            return "{}";
        }
    }

    /**
     * 创建字符串反转配置
     */
    private String createStringReverseConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("tool_name", "string_reverse");
            config.put("tool_type", "REMOTE");
            // 添加 transport 配置，指向 mcp-service-demo
            config.put("transport", Map.of(
                    "type", "http",
                    "url", "http://127.0.0.1:9899"
            ));
            config.put("function", Map.of(
                    "name", "string_reverse",
                    "description", "反转字符串",
                    "parameters", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "text", Map.of("type", "string", "description", "要反转的字符串")
                            ),
                            "required", List.of("text")
                    )
            ));
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            log.error("创建字符串反转配置失败", e);
            return "{}";
        }
    }

    /**
     * 创建字符串转大写配置
     */
    private String createStringUppercaseConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("tool_name", "string_uppercase");
            config.put("tool_type", "REMOTE");
            // 添加 transport 配置，指向 mcp-service-demo
            config.put("transport", Map.of(
                    "type", "http",
                    "url", "http://127.0.0.1:9899"
            ));
            config.put("function", Map.of(
                    "name", "string_uppercase",
                    "description", "将字符串转换为大写",
                    "parameters", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "text", Map.of("type", "string", "description", "要转换的字符串")
                            ),
                            "required", List.of("text")
                    )
            ));
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            log.error("创建字符串转大写配置失败", e);
            return "{}";
        }
    }

    /**
     * 工具定义内部类
     */
    private record ToolDefinition(String name, String displayName, String description, String configJson) {
    }
}

