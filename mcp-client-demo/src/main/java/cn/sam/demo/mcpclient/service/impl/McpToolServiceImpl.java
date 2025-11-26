package cn.sam.demo.mcpclient.service.impl;

import cn.sam.demo.mcpclient.entity.McpToolData;
import cn.sam.demo.mcpclient.mapper.McpToolMapper;
import cn.sam.demo.mcpclient.service.McpToolService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MCP 工具服务实现类
 *
 * @author Administrator
 */
@Service
public class McpToolServiceImpl extends ServiceImpl<McpToolMapper, McpToolData> implements McpToolService {

    @Override
    public McpToolData saveOrUpdateInfo(McpToolData tool) {
        if (tool.getId() == null) {
            tool.setCreateTime(LocalDateTime.now());
        }
        tool.setUpdateTime(LocalDateTime.now());
        if (tool.getStatus() == null) {
            tool.setStatus(McpToolData.Status.ENABLED);
        }
        super.saveOrUpdate(tool);
        return tool;
    }

    @Override
    public McpToolData getById(Long id) {
        return super.getById(id);
    }

    @Override
    public List<McpToolData> listAll() {
        return super.list(new LambdaQueryWrapper<McpToolData>().orderByDesc(McpToolData::getCreateTime));
    }

    @Override
    public List<McpToolData> listByType(String type) {
        return baseMapper.selectByType(type);
    }

    @Override
    public List<McpToolData> listByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    public List<McpToolData> searchByName(String name) {
        return baseMapper.selectByNameLike(name);
    }

    @Override
    public boolean deleteById(Long id) {
        return super.removeById(id);
    }

    @Override
    public boolean deleteBatch(List<Long> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        McpToolData tool = getById(id);
        if (tool == null) {
            return false;
        }
        tool.setStatus(status);
        tool.setUpdateTime(LocalDateTime.now());
        return super.updateById(tool);
    }
}

