package cn.saa.demo.controller;

import cn.saa.demo.entity.McpToolData;
import cn.saa.demo.service.McpToolService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MCP 工具管理控制器
 *
 * @author Administrator
 */
@Controller
@RequestMapping("/mcp/tools")
public class McpToolController {

    @Resource
    private McpToolService mcpToolService;

    /**
     * 工具列表页面
     */
    @GetMapping
    public String list(@RequestParam(required = false) String type,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        List<McpToolData> tools;
        if (keyword != null && !keyword.isEmpty()) {
            tools = mcpToolService.searchByName(keyword);
        } else if (type != null && !type.isEmpty()) {
            tools = mcpToolService.listByType(type);
        } else if (status != null && !status.isEmpty()) {
            tools = mcpToolService.listByStatus(status);
        } else {
            tools = mcpToolService.listAll();
        }
        model.addAttribute("tools", tools);
        model.addAttribute("type", type);
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        return "mcp/tool/list";
    }

    /**
     * 添加工具页面
     */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("tool", new McpToolData());
        return "mcp/tool/form";
    }

    /**
     * 编辑工具页面
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        McpToolData tool = mcpToolService.getById(id);
        if (tool == null) {
            return "redirect:/mcp/tools";
        }
        model.addAttribute("tool", tool);
        return "mcp/tool/form";
    }

    /**
     * 保存工具
     */
    @PostMapping("/save")
    public String save(McpToolData tool, RedirectAttributes redirectAttributes) {
        try {
            mcpToolService.saveOrUpdateInfo(tool);
            redirectAttributes.addFlashAttribute("success", "保存成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "保存失败: " + e.getMessage());
        }
        return "redirect:/mcp/tools";
    }

    /**
     * 删除工具
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            mcpToolService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/mcp/tools";
    }

    /**
     * 批量删除工具
     */
    @PostMapping("/delete/batch")
    public String deleteBatch(@RequestParam String ids, RedirectAttributes redirectAttributes) {
        try {
            List<Long> idList = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            mcpToolService.deleteBatch(idList);
            redirectAttributes.addFlashAttribute("success", "批量删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "批量删除失败: " + e.getMessage());
        }
        return "redirect:/mcp/tools";
    }

    /**
     * 更新工具状态
     */
    @PostMapping("/status/{id}")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        try {
            mcpToolService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "状态更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "状态更新失败: " + e.getMessage());
        }
        return "redirect:/mcp/tools";
    }
}

