package cn.saa.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * MCP 服务器列表响应实体
 *
 * @author Administrator
 */
@Data
public class McpServerListResponse {
    
    @JsonProperty("success")
    private Boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("request_id")
    private String requestId;
    
    @JsonProperty("data")
    private McpServerListData data;
    
    @Data
    public static class McpServerListData {
        @JsonProperty("mcp_server_list")
        private List<McpServerInfo> mcpServerList;
        
        @JsonProperty("total_count")
        private Integer totalCount;
    }
    
    @Data
    public static class McpServerInfo {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("chinese_name")
        private String chineseName;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("publisher")
        private String publisher;
        
        @JsonProperty("logo_url")
        private String logoUrl;
        
        @JsonProperty("categories")
        private List<String> categories;
        
        @JsonProperty("tags")
        private List<String> tags;
        
        @JsonProperty("view_count")
        private Integer viewCount;
        
        @JsonProperty("locales")
        private Map<String, LocaleInfo> locales;
        
        @Data
        public static class LocaleInfo {
            @JsonProperty("name")
            private String name;
            
            @JsonProperty("description")
            private String description;
        }
    }
}

