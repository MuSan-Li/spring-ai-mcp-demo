import request from '@/utils/request'

export interface McpMarket {
  id?: number
  name: string
  url: string
  description?: string
  authConfig?: string
  status: 'ENABLED' | 'DISABLED'
  createTime?: string
  updateTime?: string
}

export interface McpMarketTool {
  id?: number
  marketId: number
  toolName: string
  toolDescription?: string
  toolVersion?: string
  toolMetadata?: string
  isLoaded: boolean
  localToolId?: number
  createTime?: string
}

export interface ApiResponse<T = any> {
  success: boolean
  message?: string
  data?: T
  total?: number
  page?: number
  size?: number
  pages?: number
}

// 获取市场列表
export function getMarketList(params?: {
  status?: string
  keyword?: string
}) {
  return request.get<ApiResponse<McpMarket[]>>('/mcp/markets', { params })
}

// 获取市场详情
export function getMarketById(id: number) {
  return request.get<ApiResponse<McpMarket>>(`/mcp/markets/${id}`)
}

// 获取市场工具列表（支持分页）
export function getMarketTools(marketId: number, page?: number, size?: number) {
  return request.get<ApiResponse<McpMarketTool[]>>(`/mcp/markets/${marketId}/tools`, {
    params: { page, size }
  })
}

// 创建市场
export function createMarket(market: McpMarket) {
  return request.post<ApiResponse<McpMarket>>('/mcp/markets', market)
}

// 更新市场
export function updateMarket(id: number, market: McpMarket) {
  return request.put<ApiResponse<McpMarket>>(`/mcp/markets/${id}`, market)
}

// 删除市场
export function deleteMarket(id: number) {
  return request.delete<ApiResponse>(`/mcp/markets/${id}`)
}

// 更新市场状态
export function updateMarketStatus(id: number, status: string) {
  return request.put<ApiResponse>(`/mcp/markets/${id}/status`, null, {
    params: { status }
  })
}

// 刷新市场工具列表
export function refreshMarketTools(id: number) {
  return request.post<ApiResponse>(`/mcp/markets/${id}/refresh`)
}

// 加载市场工具到本地
export function loadMarketTool(toolId: number) {
  return request.post<ApiResponse>(`/mcp/markets/tools/${toolId}/load`)
}

// 批量加载市场工具到本地
export function batchLoadMarketTools(toolIds: number[]) {
  return request.post<ApiResponse>(`/mcp/markets/tools/batch-load`, { toolIds })
}

