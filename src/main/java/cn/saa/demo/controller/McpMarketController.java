package cn.saa.demo.controller;

import cn.saa.demo.entity.McpMarket;
import cn.saa.demo.entity.McpMarketTool;
import cn.saa.demo.service.McpMarketService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * MCP 市场管理控制器
 *
 * @author Administrator
 */
@Controller
@RequestMapping("/mcp/markets")
public class McpMarketController {

    @Resource
    private McpMarketService mcpMarketService;

    /**
     * 市场列表页面
     */
    @GetMapping
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        var markets = keyword != null && !keyword.isEmpty()
                ? mcpMarketService.searchByName(keyword)
                : status != null && !status.isEmpty()
                ? mcpMarketService.listByStatus(status)
                : mcpMarketService.listAll();
        model.addAttribute("markets", markets);
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        return "mcp/markets/list";
    }

    /**
     * 添加市场页面
     */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("market", new McpMarket());
        return "mcp/markets/form";
    }

    /**
     * 编辑市场页面
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        McpMarket market = mcpMarketService.getById(id);
        if (market == null) {
            return "redirect:/mcp/markets";
        }
        model.addAttribute("market", market);
        return "mcp/markets/form";
    }

    /**
     * 市场详情页面（包含工具列表）
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        McpMarket market = mcpMarketService.getById(id);
        if (market == null) {
            return "redirect:/mcp/markets";
        }
        List<McpMarketTool> tools = mcpMarketService.getMarketTools(id);
        model.addAttribute("market", market);
        model.addAttribute("tools", tools);
        return "mcp/markets/detail";
    }

    /**
     * 保存市场
     */
    @PostMapping("/save")
    public String save(McpMarket market, RedirectAttributes redirectAttributes) {
        try {
            mcpMarketService.saveOrUpdateInfo(market);
            redirectAttributes.addFlashAttribute("success", "保存成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "保存失败: " + e.getMessage());
        }
        return "redirect:/mcp/markets";
    }

    /**
     * 删除市场
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            mcpMarketService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/mcp/markets";
    }

    /**
     * 更新市场状态
     */
    @PostMapping("/status/{id}")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        try {
            mcpMarketService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "状态更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "状态更新失败: " + e.getMessage());
        }
        return "redirect:/mcp/markets";
    }

    /**
     * 刷新市场工具列表
     */
    @PostMapping("/refresh/{id}")
    public String refreshTools(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean success = mcpMarketService.refreshMarketTools(id);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "刷新成功");
            } else {
                redirectAttributes.addFlashAttribute("error", "刷新失败，请检查市场URL和网络连接");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "刷新失败: " + e.getMessage());
        }
        return "redirect:/mcp/markets/" + id;
    }

    /**
     * 加载市场工具到本地
     */
    @PostMapping("/tools/load/{toolId}")
    public String loadTool(@PathVariable Long toolId,
                          @RequestParam Long marketId,
                          RedirectAttributes redirectAttributes) {
        try {
            boolean success = mcpMarketService.loadToolToLocal(toolId);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "工具加载成功");
            } else {
                redirectAttributes.addFlashAttribute("error", "工具加载失败");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "工具加载失败: " + e.getMessage());
        }
        return "redirect:/mcp/markets/" + marketId;
    }

    /**
     * 批量加载市场工具到本地
     * 和单个加载逻辑完全一致，只是循环处理多个工具
     */
    @PostMapping("/tools/batch-load")
    public String batchLoadTools(@RequestParam("toolIds") String toolIdsStr,
                                @RequestParam("marketId") Long marketId,
                                RedirectAttributes redirectAttributes) {
        try {
            // 验证参数
            if (toolIdsStr == null || toolIdsStr.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "请选择要加载的工具");
                return "redirect:/mcp/markets/" + marketId;
            }
            
            if (marketId == null) {
                redirectAttributes.addFlashAttribute("error", "市场ID不能为空");
                return "redirect:/mcp/markets";
            }
            
            // 解析工具ID字符串（逗号分隔）
            String[] toolIdArray = toolIdsStr.split(",");
            int successCount = 0;
            int failCount = 0;
            
            // 循环调用单个加载方法，和单个加载逻辑完全一致
            for (String toolIdStr : toolIdArray) {
                try {
                    Long toolId = Long.parseLong(toolIdStr.trim());
                    boolean success = mcpMarketService.loadToolToLocal(toolId);
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                }
            }
            
            if (successCount > 0) {
                String message = String.format("成功加载 %d 个工具", successCount);
                if (failCount > 0) {
                    message += String.format("，%d 个工具加载失败", failCount);
                }
                redirectAttributes.addFlashAttribute("success", message);
            } else {
                redirectAttributes.addFlashAttribute("error", "批量加载失败，请检查工具状态");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "批量加载失败: " + e.getMessage());
        }
        return "redirect:/mcp/markets/" + marketId;
    }
}

