# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

校园跑腿系统是一个基于 Spring Boot + Vue3 的全栈应用，支持任务发布、接单、支付、评价等完整业务流程。

## 技术栈

### 后端 (campus-runner/)
- Spring Boot 2.7.18 + MyBatis Plus 3.5.3
- MySQL 8.0（逻辑删除支持）
- JWT Token 认证
- Java 1.8

### 前端 (campus-runner-web/)
- Vue 3 + TypeScript + Vite 7
- Element Plus UI 组件库
- Pinia 状态管理
- Vue Router 4

## 常用命令

### 后端启动
```bash
cd campus-runner
mvn spring-boot:run
```
服务地址: http://localhost:8080/api

### 前端启动
```bash
cd campus-runner-web
npm install
npm run dev
```
服务地址: http://localhost:5173

### 前端构建
```bash
npm run build        # 构建生产版本
npm run type-check  # TypeScript 类型检查
npm run lint        # ESLint 检查
```

## 架构说明

### 后端分层
```
controller/    # REST API 控制器
service/       # 业务逻辑接口
service/impl/  # 业务逻辑实现
mapper/        # MyBatis Plus Mapper 接口
entity/        # 数据实体类
utils/         # 工具类 (JwtUtil, Result, SnowflakeIdUtil)
config/        # 配置类 (MybatisPlusConfig, WebConfig)
interceptor/   # JWT 拦截器
```

### 前端结构
```
src/api/       # Axios API 接口封装
src/views/      # 页面组件
src/components/ # 公共组件
src/stores/     # Pinia 状态管理
src/router/     # Vue Router 路由配置
src/utils/      # 工具函数 (request.ts 封装 Axios)
```

### 重要约定

**后端:**
- MyBatis Plus 使用逻辑删除 (`is_deleted` 字段)
- ID 生成: entity 层用 `assign_uuid`，service 层用 SnowflakeIdUtil
- 统一返回格式: `Result<T>` 工具类
- JWT 拦截器在 `WebConfig` 中注册

**前端:**
- API 函数按模块拆分 (user.ts, task.ts, order.ts 等)
- request.ts 封装 Axios 实例，自动携带 Token
- 用户状态存储在 Pinia stores (user.ts)

## 数据库

- 数据库名: `campus_runner`
- 需要手动创建: `CREATE DATABASE campus_runner DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
- 配置文件: `campus-runner/src/main/resources/application.yml`

## 默认账号

- 管理员: admin / admin123
- 普通用户需在系统中注册

## API 前缀

所有后端接口以 `/api` 为前缀（context-path 配置）
