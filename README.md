# 校园跑腿系统

一个基于 Spring Boot + Vue3 的校园跑腿服务平台，支持任务发布、接单、支付、评价等完整业务流程。

## 项目结构

```
campus-runner3/
├── campus-runner/          # 后端服务（Spring Boot）
│   ├── src/main/java/      # Java 源代码
│   ├── src/main/resources/ # 配置文件
│   └── API_TEST.md         # API 接口测试文档
│
└── campus-runner-web/      # 前端应用（Vue3 + TypeScript）
    ├── src/
    │   ├── api/            # API 接口封装
    │   ├── components/     # 公共组件
    │   ├── router/         # 路由配置
    │   ├── stores/         # Pinia 状态管理
    │   ├── utils/          # 工具函数
    │   └── views/          # 页面视图
    └── package.json
```

## 技术栈

### 后端技术栈
- **框架**: Spring Boot 2.7.x
- **ORM**: MyBatis Plus
- **数据库**: MySQL 8.0
- **缓存**: Redis（可选）
- **安全**: JWT Token 认证
- **工具**: Lombok、Hutool

### 前端技术栈
- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite
- **UI 组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP 客户端**: Axios

## 功能模块

### 1. 用户认证模块
- 用户注册/登录
- JWT Token 认证
- 用户信息管理
- 管理员登录（独立接口）

### 2. 身份切换模块
- 发布者/跑腿者身份切换
- 不同身份主题色区分
- 发布任务界面权限控制
- 我的任务默认标签根据身份自动调整

### 3. 任务大厅模块
- 任务列表浏览
- 关键词搜索（标题+描述+地址）
- 分类筛选与排序
- 自己发布任务视觉区分（边框+标签+背景色）
- 任务进度状态显示

### 4. 任务管理模块
- 发布任务（重量等级选择）
- 接单/取消接单
- 任务状态流转（待接取→已接取→已取件→已完成）
- 修改任务信息（发布者）
- 任务进度条可视化

### 5. 收藏模块
- 添加/取消收藏
- 我的收藏列表

### 6. 评价模块
- 双向评价机制
- 评分统计
- 评价标签

### 7. 支付模块
- 创建支付订单
- 订单支付/退款
- 支付记录查询

### 8. 订单管理模块
- 任务完成自动创建订单
- 订单状态流转（待支付→已支付→已取消/已退款）
- 防重复支付机制
- 订单查询与统计
- 订单取消与退款

### 9. 管理员后台模块
- 管理员登录
- 数据概览（现代化统计卡片+图表）
  - 总用户数/订单数/收益统计
  - 今日新增数据
  - 任务状态分布图
  - 近7天任务趋势图
- 用户管理（查看、修改、禁用）
- 任务管理（查看、修改、删除、搜索）
- 管理员管理（添加、修改）
- 系统配置管理

## 快速开始

### 环境要求
- JDK 1.8+
- MySQL 8.0+
- Node.js 16+
- Maven 3.6+

### 后端启动

1. 创建数据库
```sql
CREATE DATABASE campus_runner DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改数据库配置
编辑 `campus-runner/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_runner?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

3. 运行项目
```bash
cd campus-runner
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8080`

### 前端启动

```bash
cd campus-runner-web
npm install
npm run dev
```

前端应用默认运行在 `http://localhost:5173`

## 默认账号

### 管理员账号
- 用户名: `admin`
- 密码: `admin123`
- 角色: 超级管理员

### 测试用户账号
- 需要在系统中注册
- 注册后可在导航栏切换"发布者"和"跑腿者"身份

## API 文档

详细的接口文档请参考：[API_TEST.md](./campus-runner/API_TEST.md)

主要接口模块：
- 用户认证: `/api/user/*`
- 任务管理: `/api/task/*`
- 收藏管理: `/api/favorite/*`
- 评价管理: `/api/review/*`
- 支付管理: `/api/payment/*`
- 系统配置: `/api/config/*`
- 管理员后台: `/api/admin/*`

## 任务状态说明

| 状态码 | 状态名 | 说明 |
|--------|--------|------|
| 0 | 待接单 | 任务已发布，等待跑腿员接单 |
| 1 | 进行中 | 已被接单，正在执行中（包含已接取、已取件） |
| 2 | 已完成 | 任务已完成（已送达） |
| 3 | 已取消 | 任务已取消 |

**任务进度字段：**
- `pickup_time`: 取件时间（确认取件后记录）
- `deliver_time`: 送达时间（确认送达后记录）
- `complete_time`: 完成时间（任务完成时记录）

**进度流转：**
```
待接取(0) → 已接取(1, pickup_time=null) → 已取件(1, pickup_time有值) → 已完成(2)
```

## 角色说明

### 普通用户角色
- `0` - 发布者：只能发布任务
- `1` - 跑腿员：只能接单
- `2` - 两者皆可：既可以发布也可以接单

### 管理员角色
- `0` - 普通管理员：基础管理权限
- `1` - 超级管理员：完整权限，可添加新管理员

## 开发规范

### 后端规范
- RESTful API 设计
- 统一返回格式: `Result<T>`
- 统一异常处理
- JWT Token 认证

### 前端规范
- 组件名使用 PascalCase
- API 函数使用 camelCase
- 使用 TypeScript 类型定义
- 使用 Composition API

## 项目截图

（可在此处添加项目运行截图）

## 许可证

MIT License

## 联系方式

如有问题或建议，欢迎提交 Issue 或 Pull Request。
