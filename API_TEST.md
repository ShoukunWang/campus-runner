# 校园跑腿系统接口测试文档

## 基础信息

| 项目 | 值 |
|------|-----|
| 基础URL | `http://localhost:8080/api` |
| Content-Type | `application/json` |

---

## 一、用户认证模块

### 1.1 用户注册

**接口信息**
- **URL**: `POST /user/register`
- **权限**: 公开接口

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 用户名，2-20字符 |
| password | string | 是 | 密码，6-20字符 |
| phone | string | 是 | 手机号，11位 |
| nickname | string | 否 | 昵称，默认使用用户名 |
| role | integer | 否 | 角色：0-发布者，1-跑腿员，2-两者皆可，默认0 |

**请求示例**
```http
POST http://localhost:8080/api/user/register
Content-Type: application/json

{
    "username": "testuser001",
    "password": "123456",
    "phone": "13800138001",
    "nickname": "测试用户",
    "role": 0
}
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "注册成功",
    "result": {
        "userId": "1234567890abcdef",
        "username": "testuser001",
        "nickname": "测试用户",
        "role": 0,
        "creditScore": 100,
        "token": "eyJhbGciOiJIUzI1NiIs...",
        "expireTime": "2025-03-18T15:30:00"
    },
    "timestamp": 1710145800000
}
```

**失败响应**
```json
{
    "success": false,
    "code": 1001,
    "message": "用户名已被注册",
    "timestamp": 1710145800000
}
```

---

### 1.2 用户登录

**接口信息**
- **URL**: `POST /user/login`
- **权限**: 公开接口

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| account | string | 是 | 用户名或手机号 |
| password | string | 是 | 密码 |

**请求示例**
```http
POST http://localhost:8080/api/user/login
Content-Type: application/json

{
    "account": "testuser001",
    "password": "123456"
}
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "登录成功",
    "result": {
        "userId": "1234567890abcdef",
        "username": "testuser001",
        "nickname": "测试用户",
        "avatar": null,
        "role": 0,
        "creditScore": 100,
        "completionRate": 100.00,
        "token": "eyJhbGciOiJIUzI1NiIs...",
        "expireTime": "2025-03-18T15:30:00"
    },
    "timestamp": 1710145800000
}
```

---

### 1.3 获取当前用户信息

**接口信息**
- **URL**: `GET /user/current`
- **权限**: 需要登录

**请求头**
```
Authorization: Bearer {token}
```

**请求示例**
```http
GET http://localhost:8080/api/user/current
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": {
        "id": "1234567890abcdef",
        "username": "testuser001",
        "nickname": "测试用户",
        "phone": "13800138001",
        "email": null,
        "avatar": null,
        "role": 0,
        "creditScore": 100,
        "completionRate": 100.00,
        "totalTasks": 0,
        "completedTasks": 0,
        "status": 1
    },
    "timestamp": 1710145800000
}
```

---

### 1.4 更新用户信息

**接口信息**
- **URL**: `PUT /user/profile`
- **权限**: 需要登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| nickname | string | 否 | 昵称 |
| avatar | string | 否 | 头像URL |
| email | string | 否 | 邮箱 |

**请求示例**
```http
PUT http://localhost:8080/api/user/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "nickname": "新昵称",
    "email": "test@example.com"
}
```

---

## 二、任务分类模块

### 2.1 获取任务分类列表

**接口信息**
- **URL**: `GET /category/list`
- **权限**: 公开接口

**请求示例**
```http
GET http://localhost:8080/api/category/list
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": [
        {
            "id": "1",
            "name": "代取快递",
            "icon": null,
            "sortOrder": 1
        },
        {
            "id": "2",
            "name": "代购",
            "icon": null,
            "sortOrder": 2
        },
        {
            "id": "3",
            "name": "跨校区送件",
            "icon": null,
            "sortOrder": 3
        },
        {
            "id": "4",
            "name": "文件传递",
            "icon": null,
            "sortOrder": 4
        },
        {
            "id": "5",
            "name": "其他",
            "icon": null,
            "sortOrder": 5
        }
    ],
    "timestamp": 1710145800000
}
```

---

## 三、任务大厅模块

### 3.1 任务大厅列表

**接口信息**
- **URL**: `GET /task/list`
- **权限**: 公开接口

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页数量，默认10 |
| categoryId | string | 否 | 分类ID |
| taskType | integer | 否 | 任务类型：1-代取快递，2-代购，3-跨校区送件，4-其他 |
| minReward | decimal | 否 | 最低酬金 |
| maxReward | decimal | 否 | 最高酬金 |
| area | string | 否 | 区域关键词 |
| sortField | string | 否 | 排序字段：createTime/rewardAsc/rewardDesc |

**请求示例**
```http
GET http://localhost:8080/api/task/list?pageNum=1&pageSize=10&categoryId=1&minReward=5&sortField=rewardAsc
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": {
        "records": [
            {
                "id": "task001",
                "taskNo": "TASK202503110001",
                "title": "代取快递",
                "description": "菜鸟驿站取件",
                "taskType": 1,
                "pickupAddress": "菜鸟驿站（北区）",
                "deliveryAddress": "南区宿舍3号楼",
                "reward": 5.00,
                "status": 0,
                "viewCount": 15,
                "favoriteCount": 3,
                "createTime": "2025-03-11T14:32:00"
            }
        ],
        "total": 100,
        "size": 10,
        "current": 1,
        "pages": 10
    },
    "timestamp": 1710145800000
}
```

---

### 3.2 智能推荐任务列表

**接口信息**
- **URL**: `GET /task/recommend`
- **权限**: 需要登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| longitude | decimal | 是 | 当前经度 |
| latitude | decimal | 是 | 当前纬度 |
| limit | integer | 否 | 推荐数量，默认5 |

**请求示例**
```http
GET http://localhost:8080/api/task/recommend?longitude=116.397428&latitude=39.90923&limit=5
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": [
        {
            "taskId": "task001",
            "taskNo": "TASK202503110001",
            "title": "代取快递",
            "reward": 5.00,
            "distance": 320,
            "distanceText": "320米",
            "matchScore": 92.5,
            "matchReason": "距离近、发布者信用高",
            "publisher": {
                "userId": "user001",
                "nickname": "张同学",
                "creditScore": 98
            }
        }
    ],
    "timestamp": 1710145800000
}
```

---

### 3.3 获取任务详情

**接口信息**
- **URL**: `GET /task/detail/{taskId}`
- **权限**: 公开接口

**请求示例**
```http
GET http://localhost:8080/api/task/detail/task001
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": {
        "id": "task001",
        "taskNo": "TASK202503110001",
        "title": "代取快递",
        "description": "菜鸟驿站取件，取件码3-2-5014",
        "categoryId": "1",
        "taskType": 1,
        "pickupAddress": "菜鸟驿站（北区）",
        "pickupLongitude": 116.397428,
        "pickupLatitude": 39.90923,
        "deliveryAddress": "南区宿舍3号楼 502室",
        "deliveryLongitude": 116.398000,
        "deliveryLatitude": 39.910000,
        "reward": 5.00,
        "weight": 0.50,
        "expectCompleteTime": "2025-03-11T16:00:00",
        "contactName": "张同学",
        "contactPhone": "13912345678",
        "remarks": "小件文件请轻拿轻放",
        "status": 0,
        "viewCount": 16,
        "favoriteCount": 3,
        "createTime": "2025-03-11T14:32:00"
    },
    "timestamp": 1710145800000
}
```

---

## 四、任务管理模块

### 4.1 发布任务

**接口信息**
- **URL**: `POST /task/publish`
- **权限**: 需要登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| categoryId | string | 是 | 分类ID |
| title | string | 是 | 任务标题，2-100字符 |
| description | string | 否 | 任务描述 |
| taskType | integer | 是 | 任务类型：1-代取快递，2-代购，3-跨校区送件，4-其他 |
| pickupAddress | string | 是 | 取件地址 |
| pickupLongitude | decimal | 否 | 取件经度 |
| pickupLatitude | decimal | 否 | 取件纬度 |
| deliveryAddress | string | 是 | 送达地址 |
| deliveryLongitude | decimal | 否 | 送达经度 |
| deliveryLatitude | decimal | 否 | 送达纬度 |
| reward | decimal | 是 | 任务酬金，最小0.1元 |
| weight | decimal | 否 | 物品重量(kg)，默认0 |
| expectCompleteTime | string | 是 | 期望完成时间，格式：yyyy-MM-dd HH:mm:ss |
| contactName | string | 否 | 联系人姓名 |
| contactPhone | string | 否 | 联系人电话 |
| remarks | string | 否 | 备注 |

**请求示例**
```http
POST http://localhost:8080/api/task/publish
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "categoryId": "1",
    "title": "代取一个中通快递",
    "description": "菜鸟驿站取件，取件码是3-2-5014，小件文件请轻拿轻放",
    "taskType": 1,
    "pickupAddress": "菜鸟驿站（北区）",
    "pickupLongitude": 116.397428,
    "pickupLatitude": 39.90923,
    "deliveryAddress": "南区宿舍3号楼 502室",
    "deliveryLongitude": 116.398,
    "deliveryLatitude": 39.91,
    "reward": 5.00,
    "weight": 0.5,
    "expectCompleteTime": "2025-03-11 16:00:00",
    "contactName": "张同学",
    "contactPhone": "13912345678",
    "remarks": "小件文件请轻拿轻放"
}
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "发布成功",
    "result": {
        "id": "task002",
        "taskNo": "TASK202503110002",
        "title": "代取一个中通快递",
        "status": 0,
        "statusName": "待接单",
        "createTime": "2025-03-11T15:30:00"
    },
    "timestamp": 1710145800000
}
```

---

### 4.2 接单

**接口信息**
- **URL**: `POST /task/accept/{taskId}`
- **权限**: 需要登录

**请求示例**
```http
POST http://localhost:8080/api/task/accept/task002
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "接单成功",
    "result": {
        "id": "task002",
        "taskNo": "TASK202503110002",
        "status": 1,
        "statusName": "进行中",
        "runnerId": "runner001",
        "acceptTime": "2025-03-11T15:35:00"
    },
    "timestamp": 1710145800000
}
```

**失败响应**
```json
{
    "success": false,
    "code": 2007,
    "message": "来晚一步，任务已被其他跑腿员接单",
    "timestamp": 1710145800000
}
```

---

### 4.3 更新任务状态

**接口信息**
- **URL**: `PUT /task/status/{taskId}`
- **权限**: 需要登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| action | string | 是 | 操作类型：pickup-已取件，deliver-确认送达，cancel-取消任务 |
| cancelReason | string | 否 | 取消原因（action=cancel时必填） |

**请求示例 - 确认送达**
```http
PUT http://localhost:8080/api/task/status/task002
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "action": "deliver"
}
```

**请求示例 - 取消任务**
```http
PUT http://localhost:8080/api/task/status/task002
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "action": "cancel",
    "cancelReason": "临时有事，无法完成"
}
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "状态更新成功",
    "result": {
        "id": "task002",
        "status": 2,
        "statusName": "已完成",
        "completeTime": "2025-03-11T15:40:00"
    },
    "timestamp": 1710145800000
}
```

---

### 4.4 获取我的任务列表

**接口信息**
- **URL**: `GET /task/my-list`
- **权限**: 需要登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleType | string | 是 | 角色类型：publisher-我发布的，runner-我接单的 |
| status | integer | 否 | 任务状态筛选 |
| pageNum | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页数量，默认10 |

**请求示例 - 我发布的任务**
```http
GET http://localhost:8080/api/task/my-list?roleType=publisher&status=0&pageNum=1&pageSize=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**请求示例 - 我接的任务**
```http
GET http://localhost:8080/api/task/my-list?roleType=runner&status=1&pageNum=1&pageSize=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": {
        "records": [
            {
                "id": "task002",
                "taskNo": "TASK202503110002",
                "title": "代取一个中通快递",
                "reward": 5.00,
                "status": 1,
                "pickupAddress": "菜鸟驿站（北区）",
                "deliveryAddress": "南区宿舍3号楼",
                "expectCompleteTime": "2025-03-11T16:00:00"
            }
        ],
        "total": 5,
        "size": 10,
        "current": 1,
        "pages": 1
    },
    "timestamp": 1710145800000
}
```

---

## 五、收藏模块

### 5.1 添加/取消收藏

**接口信息**
- **URL**: `POST /favorite/toggle`
- **权限**: 需要登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | string | 是 | 任务ID |

**请求示例**
```http
POST http://localhost:8080/api/favorite/toggle
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "taskId": "task001"
}
```

**成功响应 - 收藏**
```json
{
    "success": true,
    "code": 200,
    "message": "收藏成功",
    "result": {
        "taskId": "task001",
        "isFavorite": true,
        "favoriteCount": 4
    },
    "timestamp": 1710145800000
}
```

**成功响应 - 取消收藏**
```json
{
    "success": true,
    "code": 200,
    "message": "取消收藏成功",
    "result": {
        "taskId": "task001",
        "isFavorite": false,
        "favoriteCount": 3
    },
    "timestamp": 1710145800000
}
```

---

### 5.2 获取我的收藏列表

**接口信息**
- **URL**: `GET /favorite/list`
- **权限**: 需要登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页数量，默认10 |

**请求示例**
```http
GET http://localhost:8080/api/favorite/list?pageNum=1&pageSize=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": {
        "total": 5,
        "pages": 1,
        "pageNum": 1,
        "pageSize": 10,
        "list": [
            {
                "favoriteId": "fav001",
                "taskId": "task001",
                "taskNo": "TASK202503110001",
                "title": "代取快递",
                "reward": 5.00,
                "status": 0,
                "pickupAddress": "菜鸟驿站（北区）",
                "deliveryAddress": "南区宿舍3号楼",
                "createTime": "2025-03-11T14:32:00"
            }
        ]
    },
    "timestamp": 1710145800000
}
```

---

### 5.3 检查是否已收藏

**接口信息**
- **URL**: `GET /favorite/check/{taskId}`
- **权限**: 需要登录

**请求示例**
```http
GET http://localhost:8080/api/favorite/check/task001
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": {
        "taskId": "task001",
        "isFavorite": true
    },
    "timestamp": 1710145800000
}
```

---

## 六、评价模块

### 6.1 提交评价

**接口信息**
- **URL**: `POST /review/submit`
- **权限**: 需要登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | string | 是 | 任务ID |
| rating | integer | 是 | 评分：1-5星 |
| content | string | 否 | 评价内容 |
| tags | array | 否 | 评价标签数组 |
| isAnonymous | boolean | 否 | 是否匿名，默认false |

**请求示例**
```http
POST http://localhost:8080/api/review/submit
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "taskId": "task001",
    "rating": 5,
    "content": "服务态度很好，送货准时",
    "tags": ["准时送达", "服务态度好"],
    "isAnonymous": false
}
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "评价成功",
    "result": {
        "id": "review001",
        "taskId": "task001",
        "rating": 5,
        "content": "服务态度很好，送货准时",
        "tags": "准时送达,服务态度好",
        "isAnonymous": 0,
        "createTime": "2025-03-11T15:10:00"
    },
    "timestamp": 1710145800000
}
```

**失败响应**
```json
{
    "success": false,
    "code": 3001,
    "message": "任务未完成，暂不能评价",
    "timestamp": 1710145800000
}
```

---

### 6.2 获取评价列表

**接口信息**
- **URL**: `GET /review/list`
- **权限**: 公开接口

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | string | 否 | 任务ID |
| userId | string | 否 | 被评价用户ID |
| reviewType | integer | 否 | 评价类型：1-发布者对跑腿员，2-跑腿员对发布者 |
| pageNum | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页数量，默认10 |

**请求示例 - 查看任务评价**
```http
GET http://localhost:8080/api/review/list?taskId=task001&pageNum=1&pageSize=10
```

**请求示例 - 查看用户评价（带统计）**
```http
GET http://localhost:8080/api/review/list?userId=user001&pageNum=1&pageSize=10
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": {
        "total": 20,
        "pages": 2,
        "pageNum": 1,
        "pageSize": 10,
        "avgRating": 4.8,
        "totalReviews": 20,
        "ratingDistribution": {
            "1": 0,
            "2": 0,
            "3": 1,
            "4": 4,
            "5": 15
        },
        "list": [
            {
                "reviewId": "review001",
                "taskId": "task001",
                "rating": 5,
                "content": "服务态度很好，送货准时",
                "tags": ["准时送达", "服务态度好"],
                "isAnonymous": 0,
                "reviewer": {
                    "userId": "user002",
                    "nickname": "小王",
                    "avatar": null
                },
                "createTime": "2025-03-11T15:10:00"
            }
        ]
    },
    "timestamp": 1710145800000
}
```

---

### 6.3 获取用户评分统计

**接口信息**
- **URL**: `GET /review/stats/{userId}`
- **权限**: 公开接口

**请求示例**
```http
GET http://localhost:8080/api/review/stats/user001
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": {
        "avgRating": 4.8,
        "totalReviews": 20,
        "ratingDistribution": {
            "1": 0,
            "2": 0,
            "3": 1,
            "4": 4,
            "5": 15
        }
    },
    "timestamp": 1710145800000
}
```

---

## 七、系统配置模块

### 7.1 获取智能匹配权重配置

**接口信息**
- **URL**: `GET /config/match-weights`
- **权限**: 公开接口

**请求示例**
```http
GET http://localhost:8080/api/config/match-weights
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": {
        "distance": "0.4",
        "credit": "0.3",
        "reward": "0.2",
        "completion": "0.1"
    },
    "timestamp": 1710145800000
}
```

---

### 7.2 获取所有系统配置

**接口信息**
- **URL**: `GET /config/list`
- **权限**: 需要管理员登录

**请求示例**
```http
GET http://localhost:8080/api/config/list
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": [
        {
            "id": "1",
            "configKey": "match.weight.distance",
            "configValue": "0.4",
            "description": "距离权重"
        },
        {
            "id": "2",
            "configKey": "match.weight.credit",
            "configValue": "0.3",
            "description": "信用积分权重"
        }
    ],
    "timestamp": 1710145800000
}
```

---

### 7.3 更新系统配置

**接口信息**
- **URL**: `PUT /config/update`
- **权限**: 需要管理员登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| configKey | string | 是 | 配置键 |
| configValue | string | 是 | 配置值 |

**请求示例**
```http
PUT http://localhost:8080/api/config/update
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "configKey": "match.weight.distance",
    "configValue": "0.5"
}
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "更新成功",
    "result": {
        "id": "1",
        "configKey": "match.weight.distance",
        "configValue": "0.5",
        "description": "距离权重"
    },
    "timestamp": 1710145800000
}
```

---

## 八、支付模块

### 8.1 创建支付订单

**接口信息**
- **URL**: `POST /payment/create`
- **权限**: 需要登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | string | 是 | 任务ID |
| payeeId | string | 是 | 收款人ID |
| amount | decimal | 是 | 支付金额 |
| paymentType | integer | 否 | 支付类型：1-任务酬金，2-平台服务费，默认1 |

**请求示例**
```http
POST http://localhost:8080/api/payment/create
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "taskId": "task001",
    "payeeId": "user002",
    "amount": 5.00,
    "paymentType": 1
}
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "创建支付订单成功",
    "result": {
        "id": "pay001",
        "paymentNo": "PAY202503111230451234",
        "taskId": "task001",
        "payerId": "user001",
        "payeeId": "user002",
        "amount": 5.00,
        "paymentType": 1,
        "status": 0,
        "statusName": "待支付",
        "createTime": "2025-03-11T12:30:45"
    },
    "timestamp": 1710145800000
}
```

---

### 8.2 支付订单

**接口信息**
- **URL**: `POST /payment/pay/{paymentId}`
- **权限**: 需要登录

**请求示例**
```http
POST http://localhost:8080/api/payment/pay/pay001
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "支付成功",
    "result": {
        "id": "pay001",
        "paymentNo": "PAY202503111230451234",
        "status": 1,
        "statusName": "已支付",
        "payTime": "2025-03-11T12:31:00"
    },
    "timestamp": 1710145800000
}
```

**失败响应**
```json
{
    "success": false,
    "code": 6002,
    "message": "该订单已支付或已退款",
    "timestamp": 1710145800000
}
```

---

### 8.3 退款

**接口信息**
- **URL**: `POST /payment/refund/{paymentId}`
- **权限**: 需要登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | string | 是 | 退款原因 |

**请求示例**
```http
POST http://localhost:8080/api/payment/refund/pay001
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "reason": "任务取消，协商退款"
}
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "退款成功",
    "result": {
        "id": "pay001",
        "paymentNo": "PAY202503111230451234",
        "status": 2,
        "statusName": "已退款",
        "refundTime": "2025-03-11T14:00:00",
        "refundReason": "任务取消，协商退款"
    },
    "timestamp": 1710145800000
}
```

---

### 8.4 获取我的支付记录

**接口信息**
- **URL**: `GET /payment/list`
- **权限**: 需要登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | integer | 否 | 支付状态：0-待支付，1-已支付，2-已退款 |
| pageNum | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页数量，默认10 |

**请求示例**
```http
GET http://localhost:8080/api/payment/list?status=1&pageNum=1&pageSize=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": {
        "records": [
            {
                "id": "pay001",
                "paymentNo": "PAY202503111230451234",
                "taskId": "task001",
                "amount": 5.00,
                "status": 1,
                "payTime": "2025-03-11T12:31:00"
            }
        ],
        "total": 10,
        "size": 10,
        "current": 1,
        "pages": 1
    },
    "timestamp": 1710145800000
}
```

---

### 8.5 获取支付统计

**接口信息**
- **URL**: `GET /payment/statistics`
- **权限**: 需要登录

**请求示例**
```http
GET http://localhost:8080/api/payment/statistics
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": {
        "totalIncomeCount": 15,
        "totalIncomeAmount": 125.50,
        "totalExpenseCount": 8,
        "totalExpenseAmount": 68.00
    },
    "timestamp": 1710145800000
}
```

---

## 九、管理员后台模块

### 5.1 管理员登录

**接口信息**
- **URL**: `POST /admin/login`
- **权限**: 公开接口

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 管理员账号 |
| password | string | 是 | 密码 |

**请求示例**
```http
POST http://localhost:8080/api/admin/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "登录成功",
    "result": {
        "adminId": "1",
        "username": "admin",
        "realName": "超级管理员",
        "role": 1,
        "roleName": "超级管理员",
        "token": "eyJhbGciOiJIUzI1NiIs...",
        "expireTime": "2025-03-18T15:30:00"
    },
    "timestamp": 1710145800000
}
```

---

### 5.2 获取用户管理列表

**接口信息**
- **URL**: `GET /admin/user/list`
- **权限**: 需要管理员登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页数量，默认10 |
| keyword | string | 否 | 关键词（用户名/昵称/手机号） |
| role | integer | 否 | 角色筛选 |
| status | integer | 否 | 状态筛选 |

**请求示例**
```http
GET http://localhost:8080/api/admin/user/list?pageNum=1&pageSize=10&keyword=test&status=1
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": {
        "records": [
            {
                "id": "user001",
                "username": "testuser001",
                "nickname": "测试用户",
                "phone": "13800138001",
                "role": 0,
                "creditScore": 100,
                "status": 1,
                "createTime": "2025-03-11T14:32:00"
            }
        ],
        "total": 100,
        "size": 10,
        "current": 1,
        "pages": 10
    },
    "timestamp": 1710145800000
}
```

---

### 5.3 修改用户状态

**接口信息**
- **URL**: `PUT /admin/user/status/{userId}`
- **权限**: 需要管理员登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | integer | 是 | 状态：0-禁用，1-正常 |

**请求示例**
```http
PUT http://localhost:8080/api/admin/user/status/user001
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "status": 0
}
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "操作成功",
    "result": {
        "id": "user001",
        "username": "testuser001",
        "status": 0,
        "statusName": "禁用"
    },
    "timestamp": 1710145800000
}
```

---

### 5.4 修改用户信息

**接口信息**
- **URL**: `PUT /admin/user/{userId}`
- **权限**: 需要管理员登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| nickname | string | 否 | 昵称 |
| phone | string | 否 | 手机号 |
| email | string | 否 | 邮箱 |
| creditScore | integer | 否 | 信用积分 |

**请求示例**
```http
PUT http://localhost:8080/api/admin/user/user001
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "nickname": "新昵称",
    "phone": "13800138000",
    "email": "newemail@example.com",
    "creditScore": 95
}
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "修改成功",
    "result": {
        "id": "user001",
        "username": "testuser001",
        "nickname": "新昵称",
        "phone": "13800138000",
        "email": "newemail@example.com",
        "creditScore": 95
    },
    "timestamp": 1710145800000
}
```

---

### 5.5 获取任务管理列表

**接口信息**
- **URL**: `GET /admin/task/list`
- **权限**: 需要管理员登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页数量，默认10 |
| taskNo | string | 否 | 任务编号 |
| title | string | 否 | 任务标题 |
| status | integer | 否 | 任务状态 |
| publisherName | string | 否 | 发布者用户名 |
| runnerName | string | 否 | 跑腿员用户名 |

**请求示例**
```http
GET http://localhost:8080/api/admin/task/list?pageNum=1&pageSize=10&status=0
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": {
        "records": [
            {
                "id": "task001",
                "taskNo": "TASK202503110001",
                "title": "代取快递",
                "reward": 5.00,
                "status": 0,
                "publisherId": "user001",
                "runnerId": null,
                "createTime": "2025-03-11T14:32:00"
            }
        ],
        "total": 100,
        "size": 10,
        "current": 1,
        "pages": 10
    },
    "timestamp": 1710145800000
}
```

---

### 5.6 管理员修改任务

**接口信息**
- **URL**: `PUT /admin/task/{taskId}`
- **权限**: 需要管理员登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | string | 否 | 任务标题 |
| description | string | 否 | 任务描述 |
| reward | decimal | 否 | 任务酬金 |
| status | integer | 否 | 任务状态 |
| cancelReason | string | 否 | 取消原因（status=3时必填） |

**请求示例**
```http
PUT http://localhost:8080/api/admin/task/task001
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "title": "修改后的标题",
    "reward": 10.00,
    "status": 3,
    "cancelReason": "管理员强制取消"
}
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "修改成功",
    "result": {
        "id": "task001",
        "taskNo": "TASK202503110001",
        "title": "修改后的标题",
        "reward": 10.00,
        "status": 3,
        "cancelReason": "管理员强制取消"
    },
    "timestamp": 1710145800000
}
```

---

### 5.7 管理员删除任务

**接口信息**
- **URL**: `DELETE /admin/task/{taskId}`
- **权限**: 需要管理员登录

**请求示例**
```http
DELETE http://localhost:8080/api/admin/task/task001
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "删除成功",
    "result": null,
    "timestamp": 1710145800000
}
```

---

### 5.8 获取当前管理员信息

**接口信息**
- **URL**: `GET /admin/current`
- **权限**: 需要管理员登录

**请求示例**
```http
GET http://localhost:8080/api/admin/current
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": {
        "id": "1",
        "username": "admin",
        "realName": "超级管理员",
        "phone": "13800138000",
        "email": "admin@campus.com",
        "role": 1,
        "status": 1,
        "createTime": "2025-03-01T10:00:00"
    },
    "timestamp": 1710145800000
}
```

---

### 5.9 获取管理员列表

**接口信息**
- **URL**: `GET /admin/list`
- **权限**: 需要管理员登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页数量，默认10 |
| keyword | string | 否 | 关键词（用户名/真实姓名） |

**请求示例**
```http
GET http://localhost:8080/api/admin/list?pageNum=1&pageSize=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": {
        "records": [
            {
                "id": "1",
                "username": "admin",
                "realName": "超级管理员",
                "phone": "13800138000",
                "email": "admin@campus.com",
                "role": 1,
                "roleName": "超级管理员",
                "status": 1,
                "createTime": "2025-03-01T10:00:00"
            }
        ],
        "total": 5,
        "size": 10,
        "current": 1,
        "pages": 1
    },
    "timestamp": 1710145800000
}
```

---

### 5.10 添加新管理员

**接口信息**
- **URL**: `POST /admin/add`
- **权限**: 需要超级管理员登录（role=1）

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |
| realName | string | 否 | 真实姓名 |
| phone | string | 否 | 手机号 |
| email | string | 否 | 邮箱 |
| role | integer | 否 | 角色：0-普通管理员，1-超级管理员，默认0 |

**请求示例**
```http
POST http://localhost:8080/api/admin/add
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "username": "newadmin",
    "password": "admin123",
    "realName": "新管理员",
    "phone": "13800138001",
    "email": "newadmin@campus.com",
    "role": 0
}
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "添加成功",
    "result": {
        "id": "2",
        "username": "newadmin",
        "realName": "新管理员",
        "phone": "13800138001",
        "email": "newadmin@campus.com",
        "role": 0,
        "status": 1,
        "createTime": "2025-03-11T15:30:00"
    },
    "timestamp": 1710145800000
}
```

**失败响应**
```json
{
    "success": false,
    "code": 403,
    "message": "只有超级管理员才能添加新管理员",
    "timestamp": 1710145800000
}
```

---

### 5.11 修改管理员信息

**接口信息**
- **URL**: `PUT /admin/{adminId}`
- **权限**: 需要管理员登录

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| realName | string | 否 | 真实姓名 |
| phone | string | 否 | 手机号 |
| email | string | 否 | 邮箱 |
| role | integer | 否 | 角色：0-普通管理员，1-超级管理员 |
| status | integer | 否 | 状态：0-禁用，1-正常 |

**请求示例**
```http
PUT http://localhost:8080/api/admin/2
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "realName": "修改后的姓名",
    "phone": "13800138002",
    "status": 1
}
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "修改成功",
    "result": {
        "id": "2",
        "username": "newadmin",
        "realName": "修改后的姓名",
        "phone": "13800138002",
        "email": "newadmin@campus.com",
        "role": 0,
        "status": 1
    },
    "timestamp": 1710145800000
}
```

---

### 5.12 获取系统统计数据

**接口信息**
- **URL**: `GET /admin/statistics`
- **权限**: 需要管理员登录

**请求示例**
```http
GET http://localhost:8080/api/admin/statistics
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "result": {
        "overview": {
            "totalUsers": 1234,
            "totalTasks": 5678,
            "totalCompletedTasks": 4321
        },
        "today": {
            "newUsers": 12,
            "newTasks": 56,
            "completedTasks": 45
        },
        "taskStatusStats": {
            "pending": 123,
            "ongoing": 45,
            "completed": 4321,
            "cancelled": 189
        }
    },
    "timestamp": 1710145800000
}
```

---

## 十、任务管理模块（用户端）

### 10.1 修改任务信息

**接口信息**
- **URL**: `PUT /task/update/{taskId}`
- **权限**: 需要登录（仅任务发布者可修改）

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | string | 否 | 任务标题 |
| description | string | 否 | 任务描述 |
| pickupAddress | string | 否 | 取件地址 |
| deliveryAddress | string | 否 | 送达地址 |
| reward | decimal | 否 | 任务酬金 |
| weight | decimal | 否 | 物品重量(kg) |
| expectCompleteTime | string | 否 | 期望完成时间，格式：yyyy-MM-dd HH:mm:ss |
| contactName | string | 否 | 联系人姓名 |
| contactPhone | string | 否 | 联系人电话 |
| remarks | string | 否 | 备注 |

**请求示例**
```http
PUT http://localhost:8080/api/task/update/task001
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
    "title": "修改后的任务标题",
    "description": "修改后的任务描述",
    "reward": 8.00,
    "contactPhone": "13912345679"
}
```

**成功响应**
```json
{
    "success": true,
    "code": 200,
    "message": "修改成功",
    "result": {
        "id": "task001",
        "taskNo": "TASK202503110001",
        "title": "修改后的任务标题",
        "description": "修改后的任务描述",
        "reward": 8.00,
        "contactPhone": "13912345679",
        "status": 0,
        "statusName": "待接单"
    },
    "timestamp": 1710145800000
}
```

**失败响应**
```json
{
    "success": false,
    "code": 2011,
    "message": "只有任务发布者可以修改任务",
    "timestamp": 1710145800000
}
```

---

## 八、错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录或Token过期 |
| 403 | 无权访问 |
| 1001 | 用户名已被注册 |
| 1002 | 手机号已被注册 |
| 1004 | 用户名或密码错误 |
| 1005 | 账号已被禁用 |
| 2002 | 当前角色不允许发布任务 |
| 2005 | 任务不存在 |
| 2007 | 任务已被其他跑腿员接单 |
| 2008 | 不能接自己发布的任务 |
| 2010 | 当前状态不允许此操作 |
| 2011 | 取消任务请填写原因 |
| 3001 | 评价失败（任务未完成/无权评价/已评价） |
| 3002 | 无权评价此任务 |
| 3003 | 已评价过此任务 |
| 3004 | 评分必须在1-5星之间 |
| 4001 | 管理员账号或密码错误 |
| 4002 | 用户不存在 |
| 4003 | 任务修改失败 |
| 4004 | 任务删除失败 |
| 4005 | 用户信息修改失败 |
| 4006 | 添加管理员失败 |
| 4007 | 修改管理员信息失败 |
| 5001 | 配置项不存在 |
| 6001 | 创建支付订单失败 |
| 6002 | 支付失败 |
| 6003 | 退款失败 |

---

## 六、测试流程

### 6.1 完整业务流程测试

```
1. 注册两个用户
   POST /user/register
   - 用户A（发布者）
   - 用户B（跑腿员）

2. 用户A登录获取Token
   POST /user/login

3. 用户A发布任务
   POST /task/publish
   记录返回的taskId

4. 用户B登录获取Token
   POST /user/login

5. 用户B查看任务列表
   GET /task/list

6. 用户B接单
   POST /task/accept/{taskId}

7. 用户B确认送达
   PUT /task/status/{taskId}
   Body: {"action": "deliver"}

8. 用户A查看我的任务
   GET /task/my-list?roleType=publisher

9. 用户B查看我的任务
   GET /task/my-list?roleType=runner
```

### 6.2 管理员后台测试流程

```
1. 管理员登录
   POST /admin/login
   - 用户名: admin
   - 密码: admin123

2. 获取当前管理员信息
   GET /admin/current

3. 查看系统统计数据
   GET /admin/statistics

4. 查看用户列表
   GET /admin/user/list

5. 修改用户信息
   PUT /admin/user/{userId}
   Body: {"nickname": "新昵称", "creditScore": 95}

6. 禁用某个用户
   PUT /admin/user/status/{userId}
   Body: {"status": 0}

7. 查看任务列表
   GET /admin/task/list

8. 修改任务信息
   PUT /admin/task/{taskId}
   Body: {"title": "新标题", "reward": 10.00}

9. 删除违规任务
   DELETE /admin/task/{taskId}

10. 查看管理员列表（需要超级管理员权限）
    GET /admin/list

11. 添加新管理员（需要超级管理员权限）
    POST /admin/add
    Body: {"username": "newadmin", "password": "admin123", "realName": "新管理员"}

12. 修改管理员信息
    PUT /admin/{adminId}
    Body: {"realName": "修改后的姓名", "status": 1}
```

### 6.3 完整业务流程测试（含收藏评价）

```
1. 用户A发布任务
   POST /task/publish

2. 用户B查看任务并收藏
   POST /favorite/toggle
   Body: {"taskId": "task001"}

3. 用户B接单
   POST /task/accept/task001

4. 用户B完成任务
   PUT /task/status/task001
   Body: {"action": "deliver"}

5. 用户A对用户B进行评价
   POST /review/submit
   Body: {
       "taskId": "task001",
       "rating": 5,
       "content": "服务态度很好",
       "tags": ["准时送达"]
   }

6. 用户B对用户A进行评价
   POST /review/submit
   Body: {
       "taskId": "task001",
       "rating": 5,
       "content": "发布者很友好"
   }

7. 查看任务评价
   GET /review/list?taskId=task001

8. 查看用户B的评分统计
   GET /review/stats/userB_id

9. 用户B查看我的收藏
   GET /favorite/list
```

---

### 6.4 完整业务流程测试（含支付）

```
1. 用户A发布任务
   POST /task/publish

2. 用户B接单并完成任务
   POST /task/accept/{taskId}
   PUT /task/status/{taskId} (deliver)

3. 用户A创建支付订单
   POST /payment/create
   Body: {
       "taskId": "task001",
       "payeeId": "userB_id",
       "amount": 5.00
   }

4. 用户A支付订单
   POST /payment/pay/{paymentId}

5. 查看支付记录
   GET /payment/list

6. 查看支付统计
   GET /payment/statistics
```

---

## 十、注意事项

1. **Token使用**: 登录成功后获取token，在需要登录的接口请求头中添加 `Authorization: Bearer {token}`

2. **Token类型区分**:
   - 用户Token: 用于普通用户接口，userType为"USER"
   - 管理员Token: 用于管理员接口，userType为"ADMIN"
   - 管理员Token不能访问普通用户接口，反之亦然

3. **任务状态流转**:
   - 待接单(0) → 进行中(1) → 已完成(2)
   - 待接单(0) → 已取消(3)
   - 进行中(1) → 已取消(3)

4. **智能推荐**: 需要提供当前位置的经纬度，系统会根据距离、信用分、酬金计算匹配分数

5. **并发控制**: 接单操作使用乐观锁，防止超卖

6. **管理员权限**: 管理员接口需要额外的权限校验，普通用户Token无法访问

7. **超级管理员权限**: 只有role=1的超级管理员才能添加新管理员

8. **任务修改限制**: 
   - 用户只能修改自己发布的任务
   - 只有待接单(status=0)状态的任务可以被修改
   - 管理员可以修改任意任务
