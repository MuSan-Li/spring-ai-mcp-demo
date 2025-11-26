<template>
  <div class="chat-view">
    <el-card>
      <template #header>
        <span>AI 聊天</span>
      </template>

      <div class="chat-container">
        <div class="chat-messages" ref="messagesRef">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message', msg.type]"
          >
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(msg.content)"></div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>
          <!-- 流式响应中的消息 -->
          <div v-if="streamingMessage" class="message ai">
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(streamingMessage)"></div>
              <div class="message-time">{{ formatTime(new Date()) }}</div>
              <span class="streaming-indicator">▋</span>
            </div>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入消息..."
            @keydown.ctrl.enter="handleSend"
            :disabled="sending"
          />
          <div class="input-actions">
            <el-button type="primary" @click="handleSend" :loading="sending">
              发送 (Ctrl+Enter)
            </el-button>
            <el-button @click="handleClear" :disabled="sending">清空历史</el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { generateAIStream, getChatHistory, deleteChatHistory } from '@/api/chat'

const messagesRef = ref<HTMLElement>()
const inputMessage = ref('')
const sending = ref(false)
const sessionId = ref('')
const streamingMessage = ref('')
let streamController: (() => void) | null = null

interface Message {
  type: 'user' | 'ai'
  content: string
  time: string
}

const messages = ref<Message[]>([])

const formatTime = (date: Date) => {
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const formatMessage = (content: string) => {
  // 简单的文本格式化，将换行转换为 <br>
  return content.replace(/\n/g, '<br>')
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const handleSend = async () => {
  if (!inputMessage.value.trim()) {
    ElMessage.warning('请输入消息')
    return
  }

  if (sending.value) {
    ElMessage.warning('正在发送中，请稍候...')
    return
  }

  const userMessage = inputMessage.value
  inputMessage.value = ''
  streamingMessage.value = ''

  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: userMessage,
    time: formatTime(new Date())
  })
  scrollToBottom()

  sending.value = true
  const currentTime = formatTime(new Date())

  try {
    // 使用 SSE 流式接口
    streamController = generateAIStream(
      userMessage,
      sessionId.value || undefined,
      // onChunk: 接收到数据块
      (chunk: string) => {
        streamingMessage.value += chunk
        scrollToBottom()
      },
      // onComplete: 流式响应完成
      () => {
        // 将流式消息添加到消息列表
        if (streamingMessage.value.trim()) {
          messages.value.push({
            type: 'ai',
            content: streamingMessage.value,
            time: currentTime
          })
        }
        streamingMessage.value = ''
        sending.value = false
        streamController = null
        scrollToBottom()
      },
      // onError: 错误处理
      (error: Error) => {
        console.error('流式响应错误:', error)
        // 即使出错，也保存已接收的内容
        if (streamingMessage.value.trim()) {
          messages.value.push({
            type: 'ai',
            content: streamingMessage.value + '\n[响应中断: ' + error.message + ']',
            time: currentTime
          })
        } else {
          ElMessage.error('发送失败: ' + error.message)
        }
        streamingMessage.value = ''
        sending.value = false
        streamController = null
        scrollToBottom()
      }
    )
  } catch (error: any) {
    console.error('发送失败:', error)
    ElMessage.error(error?.message || '发送失败')
    sending.value = false
    streamingMessage.value = ''
    streamController = null
  }
}

const handleClear = async () => {
  if (sending.value) {
    ElMessage.warning('正在发送中，无法清空')
    return
  }

  if (sessionId.value) {
    try {
      await ElMessageBox.confirm('确定要清空历史记录吗？', '提示', {
        type: 'warning'
      })
      await deleteChatHistory(sessionId.value)
      messages.value = []
      streamingMessage.value = ''
      ElMessage.success('清空成功')
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error('清空失败')
      }
    }
  } else {
    messages.value = []
    streamingMessage.value = ''
  }
}

const loadHistory = async () => {
  if (!sessionId.value) return
  try {
    const history = await getChatHistory(sessionId.value)
    // 将历史记录转换为消息格式
    const newMessages: Message[] = []
    history.forEach(item => {
      newMessages.push({
        type: 'user',
        content: item.userMessage,
        time: item.createTime ? new Date(item.createTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : ''
      })
      newMessages.push({
        type: 'ai',
        content: item.aiResponse,
        time: item.createTime ? new Date(item.createTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : ''
      })
    })
    messages.value = newMessages
    scrollToBottom()
  } catch (error) {
    // 忽略错误，可能是新会话
  }
}

onMounted(() => {
  sessionId.value = localStorage.getItem('chatSessionId') || ''
  if (sessionId.value) {
    loadHistory()
  } else {
    sessionId.value = Date.now().toString()
    localStorage.setItem('chatSessionId', sessionId.value)
  }
})

onUnmounted(() => {
  // 组件卸载时关闭流式连接
  if (streamController) {
    streamController()
  }
})
</script>

<style scoped>
.chat-view {
  height: 100%;
  margin: 20px;
}

:deep(.el-card) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 4px;
  margin-bottom: 20px;
}

.message {
  margin-bottom: 20px;
}

.message.user {
  text-align: right;
}

.message.ai {
  text-align: left;
}

.message-content {
  display: inline-block;
  max-width: 70%;
  padding: 10px 15px;
  border-radius: 8px;
  background: white;
  position: relative;
}

.message.user .message-content {
  background: #409eff;
  color: white;
}

.message-text {
  word-wrap: break-word;
  white-space: pre-wrap;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.message.user .message-time {
  color: rgba(255, 255, 255, 0.8);
}

.streaming-indicator {
  display: inline-block;
  animation: blink 1s infinite;
  margin-left: 2px;
  color: #409eff;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

.chat-input {
  border-top: 1px solid #e6e6e6;
  padding-top: 20px;
}

.input-actions {
  margin-top: 10px;
  text-align: right;
}
</style>
