package cn.saa.demo.service.impl;

import cn.saa.demo.dto.McpServerListResponse;
import cn.saa.demo.entity.McpMarket;
import cn.saa.demo.entity.McpMarketTool;
import cn.saa.demo.entity.McpToolData;
import cn.saa.demo.mapper.McpMarketMapper;
import cn.saa.demo.mapper.McpMarketToolMapper;
import cn.saa.demo.service.McpMarketService;
import cn.saa.demo.service.McpToolService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * MCP 市场服务实现类
 *
 * @author Administrator
 */
@Service
public class McpMarketServiceImpl extends ServiceImpl<McpMarketMapper, McpMarket> implements McpMarketService {

    @Resource
    private McpMarketToolMapper marketToolMapper;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ApplicationContext applicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public McpMarket saveOrUpdateInfo(McpMarket market) {
        if (market.getId() == null) {
            market.setCreateTime(LocalDateTime.now());
        }
        market.setUpdateTime(LocalDateTime.now());
        if (market.getStatus() == null) {
            market.setStatus(McpMarket.Status.ENABLED);
        }
        super.saveOrUpdate(market);
        return market;
    }

    @Override
    public McpMarket getById(Long id) {
        return super.getById(id);
    }

    @Override
    public List<McpMarket> listAll() {
        return super.list(new LambdaQueryWrapper<McpMarket>().orderByDesc(McpMarket::getCreateTime));
    }

    @Override
    public List<McpMarket> listByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    public List<McpMarket> searchByName(String name) {
        return baseMapper.selectByNameLike(name);
    }

    @Override
    public boolean deleteById(Long id) {
        return super.removeById(id);
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        McpMarket market = getById(id);
        if (market == null) {
            return false;
        }
        market.setStatus(status);
        market.setUpdateTime(LocalDateTime.now());
        return super.updateById(market);
    }

    @Override
    public List<McpMarketTool> getMarketTools(Long marketId) {
        return marketToolMapper.selectByMarketId(marketId);
    }

    @Override
    public boolean refreshMarketTools(Long marketId) {
        try {
            McpMarket market = getById(marketId);
            if (market == null) {
                return false;
            }

            // 调用市场API获取工具列表
            String url = market.getUrl();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 如果有认证配置，添加到请求头
            if (market.getAuthConfig() != null && !market.getAuthConfig().isEmpty()) {
                try {
                    Map<String, String> authConfig = objectMapper.readValue(market.getAuthConfig(),
                            new TypeReference<>() {
                            });
                    if (authConfig.containsKey("apiKey")) {
                        headers.set("Authorization", "Bearer " + authConfig.get("apiKey"));
                    }
                } catch (Exception e) {
                    // 忽略认证配置解析错误
                }
            }

            // 分页获取所有数据，循环请求直到返回为空
            int pageSize = 20; // 每页大小
            int pageNumber = 1;
            Random random = new Random();
            boolean hasMore = true;

            while (hasMore) {
                // 构建请求体
                String requestBody = String.format("""
                        {
                          "page_number": %d,
                          "page_size": %d,
                          "search": ""
                        }""", pageNumber, pageSize);

                HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
                
                // 使用 PUT 请求
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    // 解析响应
                    McpServerListResponse responseData = objectMapper.readValue(
                            response.getBody(), 
                            McpServerListResponse.class
                    );

                    if (responseData.getSuccess() != null && responseData.getSuccess() 
                        && responseData.getData() != null 
                        && responseData.getData().getMcpServerList() != null) {
                        
                        List<McpServerListResponse.McpServerInfo> serverList = 
                                responseData.getData().getMcpServerList();
                        
                        // 如果返回的列表为空，说明没有更多数据了
                        if (serverList.isEmpty()) {
                            break;
                        }

                        // 保存或更新当前页的工具
                        for (McpServerListResponse.McpServerInfo serverInfo : serverList) {
                            // 获取服务器ID作为唯一标识
                            String serverId = serverInfo.getId();
                            if (serverId == null || serverId.isEmpty()) {
                                continue; // 跳过没有ID的服务器
                            }
                            
                            // 检查是否已存在（根据 marketId 和 serverId）
                            McpMarketTool existingTool = marketToolMapper.selectByMarketIdAndServerId(marketId, serverId);
                            
                            // 获取中文名称或英文名称
                            String toolName = serverInfo.getChineseName() != null && !serverInfo.getChineseName().isEmpty()
                                    ? serverInfo.getChineseName()
                                    : serverInfo.getName();
                            
                            // 获取描述（优先中文）
                            String description = serverInfo.getDescription();
                            if (serverInfo.getLocales() != null && serverInfo.getLocales().containsKey("zh")) {
                                McpServerListResponse.McpServerInfo.LocaleInfo zhLocale = 
                                        serverInfo.getLocales().get("zh");
                                if (zhLocale != null && zhLocale.getDescription() != null) {
                                    description = zhLocale.getDescription();
                                }
                            }
                            
                            // 构建完整的元数据
                            Map<String, Object> toolMetadata = new HashMap<>();
                            toolMetadata.put("id", serverId);
                            toolMetadata.put("name", serverInfo.getName() != null ? serverInfo.getName() : "");
                            toolMetadata.put("chinese_name", serverInfo.getChineseName() != null ? serverInfo.getChineseName() : "");
                            toolMetadata.put("description", description != null ? description : "");
                            toolMetadata.put("publisher", serverInfo.getPublisher() != null ? serverInfo.getPublisher() : "");
                            toolMetadata.put("logo_url", serverInfo.getLogoUrl() != null ? serverInfo.getLogoUrl() : "");
                            toolMetadata.put("categories", serverInfo.getCategories() != null ? serverInfo.getCategories() : List.of());
                            toolMetadata.put("tags", serverInfo.getTags() != null ? serverInfo.getTags() : List.of());
                            toolMetadata.put("view_count", serverInfo.getViewCount() != null ? serverInfo.getViewCount() : 0);

                            String metadataJson = objectMapper.writeValueAsString(toolMetadata);
                            
                            if (existingTool != null) {
                                // 更新已存在的工具（保留加载状态）
                                existingTool.setToolName(toolName);
                                existingTool.setToolDescription(description);
                                existingTool.setToolMetadata(metadataJson);
                                // 不更新 isLoaded 和 localToolId，保留原有状态
                                marketToolMapper.updateById(existingTool);
                            } else {
                                // 添加新工具
                                McpMarketTool tool = McpMarketTool.builder()
                                        .marketId(marketId)
                                        .toolName(toolName)
                                        .toolDescription(description)
                                        .toolVersion(null) // API 响应中没有版本信息
                                        .toolMetadata(metadataJson)
                                        .isLoaded(false)
                                        .createTime(LocalDateTime.now())
                                        .build();
                                marketToolMapper.insert(tool);
                            }
                        }

                        // 判断是否还有更多数据
                        int currentPageSize = serverList.size();
                        if (currentPageSize < pageSize) {
                            // 返回的数据少于每页大小，说明已经是最后一页了
                            hasMore = false;
                        } else {
                            // 还有更多数据，继续请求下一页
                            pageNumber++;
                            // 随机等待10-20秒
                            int waitSeconds = 10 + random.nextInt(11); // 10-20秒
                            try {
                                Thread.sleep(waitSeconds * 1000L);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    } else {
                        // 响应不成功或数据为空，停止请求
                        hasMore = false;
                    }
                } else {
                    // 请求失败，停止请求
                    hasMore = false;
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean loadToolToLocal(Long marketToolId) {
        try {
            McpMarketTool marketTool = marketToolMapper.selectById(marketToolId);
            if (marketTool == null || marketTool.getIsLoaded()) {
                return false;
            }

            // 创建本地工具
            McpToolData localTool = McpToolData.builder()
                    .name(marketTool.getToolName())
                    .description(marketTool.getToolDescription())
                    .type(McpToolData.Type.REMOTE)
                    .status(McpToolData.Status.ENABLED)
                    .configJson(marketTool.getToolMetadata())
                    .build();

            // 保存本地工具
            McpToolService mcpToolService = applicationContext.getBean(McpToolService.class);
            McpToolData savedTool = mcpToolService.saveOrUpdateInfo(localTool);

            // 更新市场工具的加载状态
            marketToolMapper.updateLoadedStatus(marketToolId, true, savedTool.getId());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int batchLoadToolsToLocal(List<Long> marketToolIds) {
        if (marketToolIds == null || marketToolIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        McpToolService mcpToolService = applicationContext.getBean(McpToolService.class);

        for (Long marketToolId : marketToolIds) {
            try {
                McpMarketTool marketTool = marketToolMapper.selectById(marketToolId);
                if (marketTool == null || marketTool.getIsLoaded()) {
                    continue; // 跳过已加载或不存在的工具
                }

                // 创建本地工具
                McpToolData localTool = McpToolData.builder()
                        .name(marketTool.getToolName())
                        .description(marketTool.getToolDescription())
                        .type(McpToolData.Type.REMOTE)
                        .status(McpToolData.Status.ENABLED)
                        .configJson(marketTool.getToolMetadata())
                        .build();

                // 保存本地工具
                McpToolData savedTool = mcpToolService.saveOrUpdateInfo(localTool);

                // 更新市场工具的加载状态
                marketToolMapper.updateLoadedStatus(marketToolId, true, savedTool.getId());
                successCount++;
            } catch (Exception e) {
                e.printStackTrace();
                // 继续处理下一个工具，不中断批量操作
            }
        }

        return successCount;
    }
}

