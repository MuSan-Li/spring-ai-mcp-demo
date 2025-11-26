import request from '@/utils/request'

export interface ChatHistory {
  id?: number
  sessionId: string
  userMessage: string
  aiResponse: string
  createTime?: string
  updateTime?: string
}

// 生成AI回复
export function generateAI(message: string, sessionId?: string) {
  return request.get<string>('/ai/generate', {
    params: { message, sessionId },
    responseType: 'text'
  })
}

// 流式生成AI回复 - 使用 EventSource (SSE)
export function generateAIStream(
  message: string,
  sessionId: string | undefined,
  onChunk: (chunk: string) => void,
  onComplete: () => void,
  onError: (error: Error) => void
): () => void {
  const params = new URLSearchParams()
  params.append('message', message)
  if (sessionId) {
    params.append('sessionId', sessionId)
  }

  const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9898/api'
  const url = `${baseURL}/ai/generateStream?${params.toString()}`

  const eventSource = new EventSource(url)

  eventSource.onmessage = (event) => {
    if (event.data) {
      onChunk(event.data)
    }
  }

  eventSource.addEventListener('done', () => {
    eventSource.close()
    onComplete()
  })

  eventSource.onerror = (error) => {
    eventSource.close()
    onError(new Error('流式响应错误'))
  }

  // 返回关闭函数
  return () => {
    eventSource.close()
  }
}

// 获取历史记录
export function getChatHistory(sessionId: string) {
  return request.get<ChatHistory[]>('/ai/history', {
    params: { sessionId }
  })
}

// 删除历史记录
export function deleteChatHistory(sessionId: string) {
  return request.delete<string>('/ai/history', {
    params: { sessionId }
  })
}
