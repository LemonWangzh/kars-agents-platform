# langchain4j-01-demo

基于 LangChain4j + Spring Boot 3 的智能对话服务，支持流式聊天、会话持久化（Redis / MySQL）及 Druid 数据库监控。

## 技术栈

- **Java 17** / **Spring Boot 3.5.0**
- **LangChain4j 1.0.1** — LLM 抽象与对话链
- **langchain4j-community-dashscope** — 阿里云通义千问集成
- **Spring WebFlux (Reactor)** — 流式响应
- **Redis** — 会话缓存（`RedisChatMemoryStore`）
- **MySQL + MyBatis + Druid** — 会话持久化（`MysqlChatMemoryStore`）
- **Druid 1.2.28** — 连接池与 SQL 监控

## 快速开始

### 前置依赖

| 服务 | 地址 | 说明 |
|------|------|------|
| Redis | `localhost:6379` | 会话缓存 |
| MySQL | `localhost:3306` | 会话持久化 |
| 通义千问 API | `QWEN_API_KEY` 环境变量 | LLM 调用密钥 |

### 数据库初始化

```bash
mysql -u username -p db < demo/src/main/resources/schema/chat_memory.sql
```

### 启动

```bash
# 设置 API Key
export QWEN_API_KEY=your-api-key

# 启动服务（端口 9001）
cd demo && mvn spring-boot:run
```

## 核心模块

| 包路径 | 说明 |
|--------|------|
| `com.kars.config` | 配置类（LLM、Redis、Druid、MyBatis、ChatMemoryStore） |
| `com.kars.controller` | REST 接口（流式对话、持久化管理） |
| `com.kars.assistant` | AI Assistant 接口定义 |
| `com.kars.prompt` | Prompt 模板 |
| `com.kars.listener` | 事件监听器 |
| `com.kars.entity` | 数据库实体 |
| `com.kars.mapper` | MyBatis Mapper |

## Druid 监控

访问 `http://localhost:9001/druid/`

| 账号 | 密码 |
|------|------|
| druid | druid |

## API 示例

```bash
# 流式对话
curl -N "http://localhost:9001/chat?userId=1&msg=你好"
```
