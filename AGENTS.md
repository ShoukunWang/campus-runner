# AGENTS.md - 校园跑腿系统

## 项目结构

```
campus-runner/
├── campus-runner/          # Spring Boot 后端 (端口 8080)
│   └── src/main/java/com/campusrunner/
│       ├── controller/     # REST API 控制器
│       ├── service/impl/   # 业务逻辑实现
│       ├── mapper/         # MyBatis Plus Mapper
│       ├── entity/         # 数据实体
│       ├── utils/          # 工具类 (JwtUtil, Result, SnowflakeIdUtil)
│       ├── config/         # 配置类 (WebConfig, MybatisPlusConfig)
│       └── interceptor/     # JWT 拦截器
│
└── campus-runner-web/      # Vue 3 前端 (端口 5173)
    └── src/
        ├── api/            # API 函数 (按模块拆分)
        ├── views/          # 页面组件
        ├── components/     # 公共组件
        ├── stores/         # Pinia 状态管理
        ├── router/         # Vue Router 配置
        └── utils/          # request.ts (Axios 封装)
```

## 启动命令

### 后端
```bash
cd campus-runner
mvn spring-boot:run
# 服务地址: http://localhost:8080/api
```

### 前端
```bash
cd campus-runner-web
npm install
npm run dev
# 服务地址: http://localhost:5173
```

### 前端构建
```bash
npm run build        # 构建生产版本
npm run type-check  # TypeScript 类型检查
npm run lint        # ESLint 检查
```

## 数据库

**必须手动创建:**
```sql
CREATE DATABASE campus_runner DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

配置文件: `campus-runner/src/main/resources/application.yml`

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |

普通用户需在系统中注册。

## 关键约定 (易错点)

### 后端
- **ID 生成**: entity 层用 `assign_uuid`，service 层用 `SnowflakeIdUtil`
- **逻辑删除**: MyBatis Plus 使用 `is_deleted` 字段 (值 1=删除, 0=未删除)
- **统一返回格式**: `Result<T>` 工具类
- **API 前缀**: `/api` (context-path 配置)
- **JWT Token**: 包含 `userType` 字段区分 USER/ADMIN，拦截器据此校验权限

### 前端
- **API 封装**: `src/utils/request.ts` 封装 Axios，自动携带 Token
- **用户状态**: Pinia store (`stores/user.ts`)，需调用 `initUserInfo()` 从 localStorage 恢复
- **路由守卫**: 在路由守卫中必须先调用 `initUserInfo()` 确保用户信息已初始化
- **动态导入**: 使用 `@/views/xxx.vue`，不能用相对路径 `./views/xxx.vue`
- **响应式数据**: `<script setup>` 中必须用 `ref()` 或 `reactive()` 声明

## 任务状态流转

```
待接单(0) → 进行中(1) → 已完成(2)
    ↓            ↓
  已取消(3)    已取消(3)
```

**进行中(1) 的细分:**
- `pickup_time = null`: 已接取，未取件
- `pickup_time 有值`: 已取件，未送达

## 角色说明

| 值 | 角色 | 说明 |
|----|------|------|
| 0 | 发布者 | 只能发布任务 |
| 1 | 跑腿员 | 只能接单 |
| 2 | 两者皆可 | 既可发布也可接单 |

管理员角色: 0=普通管理员, 1=超级管理员

## 前后端联调注意

1. **CORS**: 后端 WebConfig 已配置允许 `http://localhost:5173`
2. **业务码判断**: 前端需判断 `response.data.code === 200`，而非仅 HTTP 状态码
3. **嵌套数据结构**: 后端返回 `{ result: { overview: {...}, today: {...} } }`，前端需正确解析
4. **时间格式**: 统一使用 `yyyy-MM-dd HH:mm:ss`

## 关键文件

| 功能 | 文件路径 |
|------|---------|
| JWT 工具 | `campus-runner/src/main/java/com/campusrunner/utils/JwtUtil.java` |
| JWT 拦截器 | `campus-runner/src/main/java/com/campusrunner/interceptor/JwtInterceptor.java` |
| Web 配置 | `campus-runner/src/main/java/com/campusrunner/config/WebConfig.java` |
| 用户状态 | `campus-runner-web/src/stores/user.ts` |
| HTTP 封装 | `campus-runner-web/src/utils/request.ts` |
| 路由配置 | `campus-runner-web/src/router/index.ts` |
| 管理员后台 | `campus-runner-web/src/views/AdminView.vue` |

## API 模块

- `/api/user/*` - 用户认证
- `/api/task/*` - 任务管理
- `/api/category/*` - 任务分类
- `/api/favorite/*` - 收藏
- `/api/review/*` - 评价
- `/api/payment/*` - 支付
- `/api/admin/*` - 管理员后台
- `/api/config/*` - 系统配置

## 新增业务模块标准流程

1. **后端**: Entity → Mapper → Service接口 → ServiceImpl → Controller
2. **前端**: API文件 → 页面组件 → 路由配置 → 菜单配置
3. **联调**: 验证接口权限、数据结构、错误码处理

## 常见错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 401 | 未登录或 Token 过期 |
| 403 | 无权访问 |
| 1001 | 用户名已被注册 |
| 1004 | 用户名或密码错误 |
| 2007 | 任务已被其他跑腿员接单 |
| 2008 | 不能接自己发布的任务 |
| 2010 | 当前状态不允许此操作 |
| 4001 | 管理员账号或密码错误 |
