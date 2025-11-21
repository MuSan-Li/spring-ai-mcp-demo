package cn.saa.demo.mapper;

import cn.saa.demo.entity.ChatHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天历史记录Mapper
 * @author Administrator
 */
@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

    /**
     * 根据会话ID查询历史记录，按创建时间升序排列
     * @param sessionId 会话ID
     * @return 历史记录列表
     */
    List<ChatHistory> selectBySessionIdOrderByCreateTimeAsc(@Param("sessionId") String sessionId);

    /**
     * 根据会话ID查询最近N条历史记录
     * @param sessionId 会话ID
     * @param limit 限制条数
     * @return 历史记录列表
     */
    List<ChatHistory> selectRecentBySessionId(@Param("sessionId") String sessionId, @Param("limit") Integer limit);

    /**
     * 删除指定会话的所有历史记录
     * @param sessionId 会话ID
     * @return 删除的记录数
     */
    int deleteBySessionId(@Param("sessionId") String sessionId);
}

