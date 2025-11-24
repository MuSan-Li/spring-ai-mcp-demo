# Spring AI MCP 管理系统

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.0-blue.svg)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)

一个基于 Spring Boot 3.5.7 和 Spring AI 1.1.0 的 MCP (Model Context Protocol) 管理系统，提供完整的 MCP 工具管理、市场集成和 AI 聊天功能。

## 📋 目录

- [项目简介](#项目简介)
- [核心特性](#核心特性)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [快速开始](#快速开始)
- [功能模块](#功能模块)
- [API 文档](#api-文档)
- [配置说明](#配置说明)
- [数据库设计](#数据库设计)
- [开发指南](#开发指南)
- [部署指南](#部署指南)
- [远程 MCP 服务使用](#远程-mcp-服务使用)
- [常见问题](#常见问题)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

## 🎯 项目简介

Spring AI MCP 管理系统是一个企业级的 MCP 工具管理平台，旨在简化 MCP 工具的发现、加载、配置和使用。系统支持本地工具和远程工具的统一管理，并提供从 MCP 市场自动加载工具的能力。

### 主要应用场景

- **工具管理**：统一管理本地和远程 MCP 工具
- **市场集成**：从多个 MCP 市场发现和加载工具
- **AI 对话**：集成 Spring AI，支持工具调用的智能对话
- **历史记录**：完整的对话历史记录和会话管理

## ✨ 核心特性

### 1. MCP 工具管理
- ✅ **本地工具管理**：添加、编辑、删除本地实现的 MCP 工具
- ✅ **远程工具管理**：配置和管理远程 MCP 服务器连接
- ✅ **工具状态控制**：支持工具的启用/禁用操作
- ✅ **批量操作**：支持批量删除工具
- ✅ **工具搜索**：按名称、类型、状态搜索工具
- ✅ **动态注册**：工具加载后自动注册到 Spring AI 系统

### 2. MCP 市场管理
- ✅ **市场配置**：添加和管理多个 MCP 市场
- ✅ **工具发现**：从市场获取可用工具列表
- ✅ **一键加载**：将市场工具快速加载到本地系统
- ✅ **工具刷新**：支持从市场重新获取工具列表
- ✅ **认证支持**：支持市场 API 的认证配置

### 3. AI 聊天功能
- ✅ **智能对话**：基于 DeepSeek 模型的 AI 对话
- ✅ **上下文记忆**：支持多轮对话的上下文理解
- ✅ **流式响应**：支持流式输出，提升用户体验
- ✅ **会话管理**：支持多会话隔离和历史记录
- ✅ **工具调用**：AI 可以自动调用已注册的 MCP 工具

### 4. 系统特性
- ✅ **前后端不分离**：使用 Thymeleaf 模板引擎
- ✅ **响应式设计**：基于 Bootstrap 5 的现代化 UI
- ✅ **数据持久化**：使用 MyBatis Plus 进行数据访问
- ✅ **逻辑删除**：支持数据的软删除
- ✅ **配置管理**：支持多环境配置（dev/prod）

## 🛠 技术栈

### 后端技术
- **框架**：Spring Boot 3.5.7
- **AI 框架**：Spring AI 1.1.0
  - `spring-ai-starter-model-deepseek`：DeepSeek 模型集成
  - `spring-ai-starter-mcp-client-webflux`：MCP 客户端
  - `spring-ai-starter-mcp-server-webflux`：MCP 服务端
- **ORM**：MyBatis Plus 3.5.7
- **数据库**：MySQL 8.0+
- **模板引擎**：Thymeleaf
- **工具库**：Lombok 1.18.28

### 前端技术
- **UI 框架**：Bootstrap 5
- **模板引擎**：Thymeleaf
- **JavaScript**：原生 JavaScript（ES6+）

### 开发工具
- **构建工具**：Maven
- **JDK 版本**：Java 17
- **IDE 推荐**：IntelliJ IDEA / Eclipse

## 🏗 系统架构

### 架构图

```
┌─────────────────────────────────────────────────────────┐
│                      Web 层 (Controller)                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐│
│  │ChatCtrl  │  │McpTool   │  │McpMarket │  │Index    ││
│  │          │  │Ctrl      │  │Ctrl      │  │Ctrl     ││
│  └──────────┘  └──────────┘  └──────────┘  └─────────┘│
└─────────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────────┐
│                     服务层 (Service)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐│
│  │Chat      │  │McpTool   │  │McpMarket │  │McpTool  ││
│  │History   │  │Service   │  │Service   │  │Registry ││
│  │Service   │  │          │  │          │  │Service  ││
│  └──────────┘  └──────────┘  └──────────┘  └─────────┘│
└─────────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────────┐
│                     数据访问层 (Mapper)                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐│
│  │Chat      │  │McpTool   │  │McpMarket │  │McpMarket││
│  │History   │  │Mapper    │  │Mapper    │  │Tool     ││
│  │Mapper    │  │          │  │          │  │Mapper   ││
│  └──────────┘  └──────────┘  └──────────┘  └─────────┘│
└─────────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────────┐
│                     数据库层 (MySQL)                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐│
│  │chat_     │  │mcp_tool  │  │mcp_      │  │mcp_     ││
│  │history   │  │          │  │market    │  │market_  ││
│  │          │  │          │  │          │  │tool     ││
│  └──────────┘  └──────────┘  └──────────┘  └─────────┘│
└─────────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────────┐
│                 Spring AI 集成层                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │ChatClient│  │MCP Client│  │MCP Server│             │
│  │          │  │          │  │          │             │
│  └──────────┘  └──────────┘  └──────────┘             │
└─────────────────────────────────────────────────────────┘
```

### 核心组件说明

1. **Controller 层**：处理 HTTP 请求，返回视图或 JSON
2. **Service 层**：业务逻辑处理，包括工具注册、市场管理等
3. **Mapper 层**：数据访问接口，使用 MyBatis Plus
4. **Entity 层**：数据实体类，对应数据库表
5. **Config 层**：配置类，包括 AI 配置、MCP 配置等

## 🚀 快速开始

### 环境要求

- JDK 17 或更高版本
- Maven 3.6+
- MySQL 8.0+ 或更高版本
- DeepSeek API Key（用于 AI 对话功能）

### 1. 克隆项目

```bash
git clone <repository-url>
cd spring-ai-mcp-demo
```

### 2. 数据库准备

#### 2.1 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS spring_ai_demo 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

#### 2.2 执行数据库脚本

执行 `src/main/resources/db/mcp_schema.sql` 创建表结构：

```bash
mysql -u root -p spring_ai_demo < src/main/resources/db/mcp_schema.sql
```

或者直接在 MySQL 客户端中执行 SQL 文件内容。

### 3. 配置文件

#### 3.1 修改数据库配置

编辑 `src/main/resources/application-dev.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/spring_ai_demo?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password  # 修改为你的数据库密码
```

#### 3.2 配置 DeepSeek API Key

在 `application-dev.yaml` 中配置：

```yaml
spring:
  ai:
    deepseek:
      api-key: sk-your-deepseek-api-key  # 修改为你的 DeepSeek API Key
```

### 4. 编译项目

```bash
mvn clean compile
```

### 5. 运行项目

#### 方式一：使用 Maven

```bash
mvn spring-boot:run
```

#### 方式二：使用 Java 命令

```bash
mvn clean package
java -jar target/spring-ai-mcp-demo-0.0.1-SNAPSHOT.jar
```

### 6. 访问系统

启动成功后，访问：

- **首页**：http://localhost:9898
- **MCP 工具管理**：http://localhost:9898/mcp/tools
- **MCP 市场管理**：http://localhost:9898/mcp/markets
- **AI 聊天接口**：http://localhost:9898/ai/generate?message=你好

## 📦 功能模块

### 1. MCP 工具管理模块

#### 1.1 功能列表

- **工具列表**：查看所有已注册的工具
- **添加工具**：添加本地或远程 MCP 工具
- **编辑工具**：修改工具配置信息
- **删除工具**：删除不再需要的工具
- **批量删除**：支持批量删除多个工具
- **状态切换**：启用/禁用工具
- **工具搜索**：按名称、类型、状态搜索

#### 1.2 使用流程

**添加本地工具**：
1. 进入"MCP工具管理"页面
2. 点击"添加工具"按钮
3. 填写工具信息：
   - 工具名称（必填）
   - 工具描述
   - 工具类型：选择"LOCAL"
   - 状态：选择"ENABLED"或"DISABLED"
   - 配置信息：JSON 格式的配置（见配置说明）
4. 点击"保存"

**添加远程工具**：
1. 进入"MCP工具管理"页面
2. 点击"添加工具"按钮
3. 填写工具信息：
   - 工具名称（必填）
   - 工具描述
   - 工具类型：选择"REMOTE"
   - 远程URL：工具的访问地址
   - 配置信息：包含连接信息的 JSON 配置
4. 点击"保存"

### 2. MCP 市场管理模块

#### 2.1 功能列表

- **市场列表**：查看所有已配置的市场
- **添加市场**：添加新的 MCP 市场
- **编辑市场**：修改市场配置
- **删除市场**：删除市场配置
- **市场详情**：查看市场信息和可用工具
- **刷新工具列表**：从市场重新获取工具列表
- **加载工具**：将市场工具加载到本地系统

#### 2.2 使用流程

**添加市场**：
1. 进入"MCP市场管理"页面
2. 点击"添加市场"按钮
3. 填写市场信息：
   - 市场名称（必填）
   - 市场URL（必填）：市场的 API 地址
   - 市场描述
   - 状态：选择"ENABLED"或"DISABLED"
   - 认证配置：如果需要认证，填写 JSON 格式的认证信息
4. 点击"保存"

**从市场加载工具**：
1. 进入"MCP市场管理"页面
2. 点击市场的"查看"按钮
3. 如果工具列表为空，点击"刷新工具列表"按钮
4. 选择要加载的工具，点击"加载"按钮
5. 工具加载成功后，会自动出现在"MCP工具管理"页面

### 3. AI 聊天模块

#### 3.1 功能列表

- **普通对话**：支持单次问答
- **上下文对话**：支持多轮对话，保持上下文
- **流式响应**：支持流式输出，实时显示 AI 回复
- **历史记录**：保存和查看对话历史
- **会话管理**：支持多会话隔离

#### 3.2 API 接口

**生成 AI 回复**：
```
GET /ai/generate?message=你好&sessionId=session-123
```

**流式生成 AI 回复**：
```
GET /ai/generateStream?message=你好&sessionId=session-123
```

**获取历史记录**：
```
GET /ai/history?sessionId=session-123
```

**删除历史记录**：
```
DELETE /ai/history?sessionId=session-123
```

## 📚 API 文档

### MCP 工具管理 API

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/mcp/tools` | 工具列表页面 | `type`, `status`, `keyword` |
| GET | `/mcp/tools/add` | 添加工具页面 | - |
| GET | `/mcp/tools/edit/{id}` | 编辑工具页面 | `id` |
| POST | `/mcp/tools/save` | 保存工具 | `McpToolData` |
| POST | `/mcp/tools/delete/{id}` | 删除工具 | `id` |
| POST | `/mcp/tools/delete/batch` | 批量删除工具 | `ids` |
| POST | `/mcp/tools/status/{id}` | 更新工具状态 | `id`, `status` |

### MCP 市场管理 API

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/mcp/markets` | 市场列表页面 | - |
| GET | `/mcp/markets/add` | 添加市场页面 | - |
| GET | `/mcp/markets/edit/{id}` | 编辑市场页面 | `id` |
| GET | `/mcp/markets/{id}` | 市场详情页面 | `id` |
| POST | `/mcp/markets/save` | 保存市场 | `McpMarket` |
| POST | `/mcp/markets/delete/{id}` | 删除市场 | `id` |
| POST | `/mcp/markets/status/{id}` | 更新市场状态 | `id`, `status` |
| POST | `/mcp/markets/refresh/{id}` | 刷新市场工具列表 | `id` |
| POST | `/mcp/markets/tools/load/{toolId}` | 加载市场工具到本地 | `toolId` |

### AI 聊天 API

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/ai/generate` | 生成 AI 回复 | `message`, `sessionId` |
| GET | `/ai/generateStream` | 流式生成 AI 回复 | `message`, `sessionId` |
| GET | `/ai/history` | 获取历史记录 | `sessionId` |
| DELETE | `/ai/history` | 删除历史记录 | `sessionId` |

## ⚙️ 配置说明

### 应用配置

#### application.yaml

```yaml
server:
  port: 9898  # 服务端口

spring:
  application:
    name: spring-ai-agents-demo
  profiles:
    active: prod  # 激活的配置文件：dev 或 prod
```

#### application-dev.yaml

开发环境配置：

```yaml
spring:
  # Thymeleaf 配置
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    mode: HTML
    encoding: UTF-8
    cache: false  # 开发环境关闭缓存
  
  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/spring_ai_demo?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root
  
  # DeepSeek AI 配置
  ai:
    deepseek:
      api-key: sk-xx  # 替换为你的 API Key
      chat:
        options:
          temperature: 0.7  # 温度参数，控制回复的随机性
          max-tokens: 2000  # 最大 token 数

# MyBatis Plus 配置
mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: cn.saa.demo.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 开发环境开启 SQL 日志
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 工具配置格式

#### 本地工具配置（LOCAL）

```json
{
  "function": {
    "name": "get_weather",
    "description": "获取天气信息",
    "parameters": {
      "type": "object",
      "properties": {
        "city": {
          "type": "string",
          "description": "城市名称"
        }
      },
      "required": ["city"]
    }
  },
  "implementation": {
    "type": "http",
    "url": "https://api.weather.com/weather",
    "method": "GET"
  }
}
```

#### 远程工具配置（REMOTE）

```json
{
  "id": "@modelcontextprotocol/fetch",
  "name": "Fetch网页内容抓取",
  "transport": {
    "type": "sse",
    "url": "https://mcp-server.example.com/sse",
    "headers": {
      "Authorization": "Bearer token"
    }
  },
  "capabilities": {
    "tools": true,
    "resources": true
  }
}
```

### 市场认证配置格式

```json
{
  "apiKey": "your-api-key",
  "type": "bearer"
}
```

## 🗄️ 数据库设计

### 表结构说明

#### 1. mcp_tool（MCP 工具表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID，自增 |
| name | VARCHAR(100) | 工具名称，唯一 |
| description | TEXT | 工具描述 |
| type | VARCHAR(20) | 工具类型：LOCAL/REMOTE |
| status | VARCHAR(20) | 状态：ENABLED/DISABLED |
| config_json | TEXT | 配置信息（JSON格式） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### 2. mcp_market（MCP 市场表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID，自增 |
| name | VARCHAR(100) | 市场名称，唯一 |
| url | VARCHAR(500) | 市场URL |
| description | TEXT | 市场描述 |
| auth_config | TEXT | 认证配置（JSON格式） |
| status | VARCHAR(20) | 状态：ENABLED/DISABLED |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### 3. mcp_market_tool（MCP 市场工具表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID，自增 |
| market_id | BIGINT | 市场ID，外键 |
| tool_name | VARCHAR(100) | 工具名称 |
| tool_description | TEXT | 工具描述 |
| tool_version | VARCHAR(50) | 工具版本 |
| tool_metadata | TEXT | 工具元数据（JSON格式） |
| is_loaded | TINYINT(1) | 是否已加载到本地：0-未加载，1-已加载 |
| local_tool_id | BIGINT | 关联的本地工具ID |
| create_time | DATETIME | 创建时间 |

#### 4. chat_history（聊天历史表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID，自增 |
| session_id | VARCHAR(100) | 会话ID |
| user_message | TEXT | 用户消息 |
| ai_response | TEXT | AI回复 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 数据库关系图

```
mcp_market (1) ────< (N) mcp_market_tool
                              │
                              │ (可选)
                              │
                              ↓
                        mcp_tool (N)
                              │
                              │ (使用)
                              ↓
                        chat_history (N)
```

## 💻 开发指南

### 项目结构

```
spring-ai-mcp-demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── cn/saa/demo/
│   │   │       ├── config/          # 配置类
│   │   │       │   ├── AiConfig.java
│   │   │       │   ├── McpToolStartupListener.java
│   │   │       │   ├── MyBatisPlusConfig.java
│   │   │       │   └── WebConfig.java
│   │   │       ├── controller/      # 控制器
│   │   │       │   ├── ChatController.java
│   │   │       │   ├── IndexController.java
│   │   │       │   ├── McpMarketController.java
│   │   │       │   ├── McpToolController.java
│   │   │       │   └── McpToolTestController.java
│   │   │       ├── dto/             # 数据传输对象
│   │   │       │   └── McpServerListResponse.java
│   │   │       ├── entity/          # 实体类
│   │   │       │   ├── ChatHistory.java
│   │   │       │   ├── McpMarket.java
│   │   │       │   ├── McpMarketTool.java
│   │   │       │   └── McpToolData.java
│   │   │       ├── mapper/          # Mapper 接口
│   │   │       │   ├── ChatHistoryMapper.java
│   │   │       │   ├── McpMarketMapper.java
│   │   │       │   ├── McpMarketToolMapper.java
│   │   │       │   └── McpToolMapper.java
│   │   │       ├── mcp/            # MCP 相关
│   │   │       │   └── tool/
│   │   │       │       └── LocalMcpTools.java
│   │   │       ├── service/        # 服务接口
│   │   │       │   ├── ChatHistoryService.java
│   │   │       │   ├── McpMarketService.java
│   │   │       │   ├── McpToolInvokeService.java
│   │   │       │   ├── McpToolRegistryService.java
│   │   │       │   ├── McpToolService.java
│   │   │       │   └── impl/       # 服务实现
│   │   │       │       ├── ChatHistoryServiceImpl.java
│   │   │       │       ├── McpMarketServiceImpl.java
│   │   │       │       └── McpToolServiceImpl.java
│   │   │       └── SpringAiAgentsDemoApplication.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── application-dev.yaml
│   │       ├── application-prod.yaml
│   │       ├── db/                 # 数据库脚本
│   │       │   ├── mcp_schema.sql
│   │       │   └── schema.sql
│   │       ├── mapper/             # MyBatis XML
│   │       │   ├── ChatHistoryMapper.xml
│   │       │   ├── McpMarketMapper.xml
│   │       │   ├── McpMarketToolMapper.xml
│   │       │   └── McpToolMapper.xml
│   │       └── templates/         # Thymeleaf 模板
│   │           ├── layout/
│   │           │   └── base.html
│   │           └── mcp/
│   │               ├── markets/
│   │               │   ├── detail.html
│   │               │   ├── form.html
│   │               │   └── list.html
│   │               └── tool/
│   │                   ├── form.html
│   │                   └── list.html
│   └── test/                      # 测试代码
│       └── java/
│           └── cn/saa/demo/
│               └── SpringAiAgentsDemoApplicationTests.java
├── docs/                          # 文档
│   ├── MCP注解与系统集成分析.md
│   └── MCP集成需求分析.md
├── pom.xml                        # Maven 配置
├── README.md                      # 项目说明
├── MCP管理系统-使用说明.md
└── MCP管理系统-需求分析与实施方案.md
```

### 核心类说明

#### 1. SpringAiAgentsDemoApplication

主启动类，配置了 `@MapperScan` 注解扫描 Mapper 接口。

#### 2. AiConfig

AI 配置类，配置了 `ChatClient` Bean，使用 DeepSeek 模型。

#### 3. McpToolRegistryService

MCP 工具注册服务，负责将工具注册到 Spring AI 系统中。

#### 4. McpToolService

MCP 工具服务，提供工具的 CRUD 操作。

#### 5. McpMarketService

MCP 市场服务，提供市场的管理和工具加载功能。

### 开发规范

1. **代码风格**：遵循 Java 编码规范，使用 Lombok 简化代码
2. **命名规范**：
   - 类名：大驼峰命名（PascalCase）
   - 方法名：小驼峰命名（camelCase）
   - 常量：全大写下划线分隔（UPPER_SNAKE_CASE）
3. **注释规范**：类和方法必须添加 JavaDoc 注释
4. **异常处理**：使用统一的异常处理机制

### 扩展开发

#### 添加新的工具类型

1. 在 `McpToolData.Type` 中添加新的类型常量
2. 在 `McpToolRegistryService` 中添加对应的注册逻辑
3. 更新工具配置格式文档

#### 集成新的 AI 模型

1. 在 `pom.xml` 中添加对应的 Spring AI Starter
2. 在 `AiConfig` 中配置新的 ChatModel
3. 更新配置文件

## 🌐 远程 MCP 服务使用

系统支持三种方式使用远程 MCP 服务：

1. **通过市场加载**：从 MCP 市场自动发现和加载工具
2. **手动添加**：手动配置远程 MCP 服务器的连接信息
3. **Spring AI MCP Client**：使用 Spring AI 的 MCP Client 直接连接（推荐）

### 快速开始

1. **从市场加载工具**：
   - 访问"MCP市场管理"页面
   - 添加市场并刷新工具列表
   - 选择工具并加载到本地

2. **手动添加远程工具**：
   - 访问"MCP工具管理"页面
   - 点击"添加工具"
   - 选择类型为"REMOTE"
   - 填写远程服务器连接信息

3. **配置远程连接**：
   ```json
   {
     "transport": {
       "type": "sse",
       "url": "https://mcp-server.example.com/sse",
       "headers": {
         "Authorization": "Bearer your-token"
       }
     }
   }
   ```

### 详细文档

📖 **完整使用方案请参考**：[远程 MCP 服务使用方案](./docs/远程MCP服务使用方案.md)

该文档包含：
- 三种使用方式的详细步骤
- 配置示例和最佳实践
- 测试方法和故障排查
- 在 AI 对话中使用远程工具的方法

## 🚢 部署指南

### 开发环境部署

1. 确保 JDK 17 已安装
2. 配置数据库连接
3. 配置 DeepSeek API Key
4. 运行 `mvn spring-boot:run`

#### Docker 部署

创建 `Dockerfile`：

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/spring-ai-mcp-demo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9898

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

构建和运行：

```bash
docker build -t spring-ai-mcp:latest .
docker run -d -p 9898:9898 --name spring-ai-mcp spring-ai-mcp:latest
```

## ❓ 常见问题

### 1. 工具无法加载

**问题**：从市场加载工具后，工具无法使用。

**解决方案**：
- 检查工具配置是否正确
- 检查网络连接（远程工具）
- 查看应用日志
- 确认工具状态为"ENABLED"

### 2. 市场工具列表为空

**问题**：从市场获取工具列表时返回空。

**解决方案**：
- 检查市场 URL 是否正确
- 检查认证配置是否正确
- 点击"刷新工具列表"按钮
- 查看应用日志中的错误信息

### 3. AI 对话无响应

**问题**：调用 AI 接口时没有响应。

**解决方案**：
- 检查 DeepSeek API Key 是否正确配置
- 检查网络连接
- 查看应用日志
- 确认 API Key 有足够的余额

### 4. 数据库连接失败

**问题**：应用启动时数据库连接失败。

**解决方案**：
- 检查数据库服务是否启动
- 检查数据库连接配置是否正确
- 检查数据库用户权限
- 检查防火墙设置

### 5. 端口被占用

**问题**：启动时提示端口 9898 被占用。

**解决方案**：
- 修改 `application.yaml` 中的端口号
- 或者停止占用端口的进程

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 贡献规范

- 代码必须通过编译
- 添加必要的单元测试
- 更新相关文档
- 遵循代码风格规范

## 📄 许可证

本项目采用 MIT 许可证。详情请参阅 [LICENSE](LICENSE) 文件。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 Issue
- 发送邮件
- 提交 Pull Request

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot) - 优秀的 Java 框架
- [Spring AI](https://spring.io/projects/spring-ai) - AI 集成框架
- [MyBatis Plus](https://baomidou.com/) - 强大的 ORM 框架
- [Bootstrap](https://getbootstrap.com/) - 前端 UI 框架

---

**版本**: 0.0.1-SNAPSHOT  
**最后更新**: 2024年  
**维护者**: Administrator

