package cn.saa.demo.service.impl;

import cn.saa.demo.entity.ChatHistory;
import cn.saa.demo.mapper.ChatHistoryMapper;
import cn.saa.demo.service.ChatHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天历史记录服务实现类
 * @author Administrator
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Override
    public ChatHistory saveInfo(ChatHistory chatHistory) {
        if (chatHistory.getCreateTime() == null) {
            chatHistory.setCreateTime(LocalDateTime.now());
        }
        if (chatHistory.getUpdateTime() == null) {
            chatHistory.setUpdateTime(LocalDateTime.now());
        }
        // 使用 MyBatis Plus 的 saveInfo 方法
        super.save(chatHistory);
        return chatHistory;
    }

    @Override
    public List<ChatHistory> getHistoryBySessionId(String sessionId) {
        return baseMapper.selectBySessionIdOrderByCreateTimeAsc(sessionId);
    }

    @Override
    public List<ChatHistory> getRecentHistoryBySessionId(String sessionId, Integer limit) {
        return baseMapper.selectRecentBySessionId(sessionId, limit);
    }

    @Override
    public boolean deleteBySessionId(String sessionId) {
        int count = baseMapper.deleteBySessionId(sessionId);
        return count > 0;
    }
}

