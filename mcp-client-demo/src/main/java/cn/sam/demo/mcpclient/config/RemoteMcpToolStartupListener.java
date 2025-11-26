package cn.sam.demo.mcpclient.config;

import cn.sam.demo.mcpclient.entity.McpToolData;
import cn.sam.demo.mcpclient.service.McpToolRegistryService;
import cn.sam.demo.mcpclient.service.McpToolService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 远程 MCP 工具启动监听器
 * 在应用启动时自动注册所有已启用的远程工具
 *
 * @author Administrator
 */
@Slf4j
@Component
public class RemoteMcpToolStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private McpToolService mcpToolService;

    @Resource
    private McpToolRegistryService mcpToolRegistryService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("开始注册远程 MCP 工具...");

        try {
            // 获取所有已启用的远程工具
            List<McpToolData> remoteTools = mcpToolService.listByType(McpToolData.Type.REMOTE)
                    .stream().filter(tool -> McpToolData.Status.ENABLED.equals(tool.getStatus())).toList();

            if (remoteTools.isEmpty()) {
                log.info("没有需要注册的远程 MCP 工具");
                return;
            }

            int successCount = 0;
            int failCount = 0;

            for (McpToolData tool : remoteTools) {
                try {
                    // 检查是否已注册
                    if (mcpToolRegistryService.isRegistered(tool.getId())) {
                        log.debug("工具已注册，跳过: {}", tool.getName());
                        successCount++;
                        continue;
                    }
                    // 注册工具
                    if (mcpToolRegistryService.registerTool(tool)) {
                        successCount++;
                        log.info("成功注册远程工具: {}", tool.getName());
                    } else {
                        failCount++;
                        log.warn("注册远程工具失败: {}", tool.getName());
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("注册远程工具异常: {}", tool.getName(), e);
                }
            }
            log.info("远程 MCP 工具注册完成，成功: {}/{}, 失败: {}", successCount, remoteTools.size(), failCount);
        } catch (Exception e) {
            log.error("注册远程 MCP 工具时发生异常", e);
        }
    }
}

