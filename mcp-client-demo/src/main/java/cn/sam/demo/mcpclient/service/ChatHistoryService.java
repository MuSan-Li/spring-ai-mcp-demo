package cn.sam.demo.mcpclient.service;

import cn.sam.demo.mcpclient.entity.ChatHistory;

import java.util.List;

/**
 * 聊天历史记录服务接口
 * @author Administrator
 */
public interface ChatHistoryService {

    /**
     * 保存聊天记录
     * @param chatHistory 聊天记录
     * @return 保存后的聊天记录
     */
    ChatHistory saveInfo(ChatHistory chatHistory);

    /**
     * 根据会话ID查询历史记录，按创建时间升序排列
     * @param sessionId 会话ID
     * @return 历史记录列表
     */
    List<ChatHistory> getHistoryBySessionId(String sessionId);

    /**
     * 根据会话ID查询最近N条历史记录
     * @param sessionId 会话ID
     * @param limit 限制条数
     * @return 历史记录列表
     */
    List<ChatHistory> getRecentHistoryBySessionId(String sessionId, Integer limit);

    /**
     * 删除指定会话的所有历史记录
     * @param sessionId 会话ID
     * @return 是否删除成功
     */
    boolean deleteBySessionId(String sessionId);
}

