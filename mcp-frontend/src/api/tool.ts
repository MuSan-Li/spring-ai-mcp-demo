import request from '@/utils/request'

export interface McpToolData {
  id?: number
  name: string
  description?: string
  type: 'LOCAL' | 'REMOTE'
  status: 'ENABLED' | 'DISABLED'
  configJson?: string
  createTime?: string
  updateTime?: string
}

export interface ApiResponse<T = any> {
  success: boolean
  message?: string
  data?: T
  total?: number
}

// 获取工具列表
export function getToolList(params?: {
  type?: string
  status?: string
  keyword?: string
}) {
  return request.get<ApiResponse<McpToolData[]>>('/mcp/tools', { params })
}

// 获取工具详情
export function getToolById(id: number) {
  return request.get<ApiResponse<McpToolData>>(`/mcp/tools/${id}`)
}

// 创建工具
export function createTool(tool: McpToolData) {
  return request.post<ApiResponse<McpToolData>>('/mcp/tools', tool)
}

// 更新工具
export function updateTool(id: number, tool: McpToolData) {
  return request.put<ApiResponse<McpToolData>>(`/mcp/tools/${id}`, tool)
}

// 删除工具
export function deleteTool(id: number) {
  return request.delete<ApiResponse>(`/mcp/tools/${id}`)
}

// 批量删除工具
export function deleteToolsBatch(ids: string) {
  return request.delete<ApiResponse>('/mcp/tools/batch', { params: { ids } })
}

// 更新工具状态
export function updateToolStatus(id: number, status: string) {
  return request.put<ApiResponse>(`/mcp/tools/${id}/status`, null, {
    params: { status }
  })
}

// 测试工具
export function testTool(id: number, params?: Record<string, any>) {
  return request.post<ApiResponse>(`/mcp/tools/test/${id}`, params)
}

// 获取工具信息
export function getToolInfo(id: number) {
  return request.get<ApiResponse>(`/mcp/tools/info/${id}`)
}

