package cn.saa.demo.service;

import cn.saa.demo.entity.McpMarket;
import cn.saa.demo.entity.McpMarketTool;

import java.util.List;

/**
 * MCP 市场服务接口
 *
 * @author Administrator
 */
public interface McpMarketService {

    /**
     * 保存或更新市场
     *
     * @param market 市场实体
     * @return 保存后的市场
     */
    McpMarket saveOrUpdateInfo(McpMarket market);

    /**
     * 根据ID查询市场
     *
     * @param id 市场ID
     * @return 市场实体
     */
    McpMarket getById(Long id);

    /**
     * 查询所有市场
     *
     * @return 市场列表
     */
    List<McpMarket> listAll();

    /**
     * 根据状态查询市场
     *
     * @param status 状态
     * @return 市场列表
     */
    List<McpMarket> listByStatus(String status);

    /**
     * 根据名称模糊查询
     *
     * @param name 市场名称
     * @return 市场列表
     */
    List<McpMarket> searchByName(String name);

    /**
     * 删除市场
     *
     * @param id 市场ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 更新市场状态
     *
     * @param id     市场ID
     * @param status 状态
     * @return 是否更新成功
     */
    boolean updateStatus(Long id, String status);

    /**
     * 从市场获取工具列表
     *
     * @param marketId 市场ID
     * @return 工具列表
     */
    List<McpMarketTool> getMarketTools(Long marketId);

    /**
     * 刷新市场工具列表
     *
     * @param marketId 市场ID
     * @return 是否刷新成功
     */
    boolean refreshMarketTools(Long marketId);

    /**
     * 加载市场工具到本地
     *
     * @param marketToolId 市场工具ID
     * @return 是否加载成功
     */
    boolean loadToolToLocal(Long marketToolId);

    /**
     * 批量加载市场工具到本地
     *
     * @param marketToolIds 市场工具ID列表
     * @return 成功加载的数量
     */
    int batchLoadToolsToLocal(List<Long> marketToolIds);
}

