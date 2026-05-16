# 用户信誉分系统设计

## 1. 概述

为校园跑腿系统引入信誉分机制，通过渐进式惩戒 + 渐进式恢复成本，约束用户行为，保护交易双方金钱利益。替代注册保证金方案，降低学生使用门槛。

- 初始信誉分：100
- 违规扣分 → 功能受限 → 支付恢复 → 资金进入赔偿金池 → 赔偿受损方

## 2. 违规扣分规则

| 违规类型 | 扣分 | 触发方式 |
|---------|------|---------|
| PAYMENT_OVERDUE（发布者拒不支付） | -15 | 定时任务自动：订单创建7天未支付 |
| FAKE_DELIVERY（跑腿员虚假送达） | -20 | 管理员审核申诉通过 |
| MALICIOUS_REFUND（发布者恶意退款） | -10 | 管理员审核申诉通过 |
| ITEM_DAMAGE（跑腿员损坏/丢失物品） | -20 | 管理员审核申诉通过 |
| FAKE_CONFIRM（发布者虚假确认） | -15 | 管理员审核申诉通过 |

取消发布任务和取消接单不扣分。

## 3. 功能限制（分级）

| 分数区间 | 发任务 | 接任务 | 说明 |
|---------|--------|--------|------|
| 80-100 | ✅ | ✅ | 正常 |
| 60-79 | ✅ | ✅ | 页面顶部显示信誉分警告 |
| 40-59 | ❌ | ✅ | 禁止发任务 |
| 20-39 | ❌ | ❌ | 禁止发任务和接任务 |
| 0-19 | ❌ | ❌ | 账号冻结，禁止登录 |

拦截位置：
- publishTask：40分以下拒绝
- acceptTask：20分以下拒绝
- login：status 改为冻结（信用分过低导致）

## 4. 信誉分恢复

### 4.1 支付恢复（阶梯单价）

| 分数区间 | 单价 |
|---------|------|
| 80-100 | ¥0.5/分 |
| 60-79 | ¥2/分 |
| 40-59 | ¥5/分 |
| 20-39 | ¥10/分 |
| 0-19 | ¥20/分 |

计算公式：从当前分数恢复到目标分数，按各区间的分数差 × 对应单价累加。

### 4.2 自然恢复

连续30天无违规，自动 +5 分（不超过100）。

## 5. 赔偿机制

### 5.1 赔偿规则

| 违规场景 | 受损方 | 赔偿金额 |
|---------|--------|---------|
| 发布者拒不支付 | 跑腿员 | 订单金额全额 |
| 跑腿员虚假送达 | 发布者 | 订单金额全额 |
| 发布者恶意退款 | 跑腿员 | 订单金额全额 |
| 跑腿员损坏/丢失物品 | 发布者 | 订单金额 + 物品损失（上限¥200） |
| 发布者虚假确认 | 跑腿员 | 订单金额全额 |

### 5.2 赔偿金池

- 收入：用户支付的信誉分恢复费用
- 支出：受损方赔付
- 金池余额不足时平台先行垫付
- 管理员后台可查看余额

### 5.3 申诉流程

1. 受损方在订单详情页发起申诉（订单完成后7天内可申诉）
2. 管理员后台审核
3. 驳回 → 关闭；受理 → 扣违规方信誉分 + 赔付受损方

## 6. 数据模型

### 6.1 新增 credit_record 表

```sql
CREATE TABLE `credit_record` (
    `id` VARCHAR(32) NOT NULL,
    `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
    `change_amount` INT NOT NULL COMMENT '变化量（负数为扣分，正数为恢复）',
    `before_score` INT NOT NULL COMMENT '变化前分数',
    `after_score` INT NOT NULL COMMENT '变化后分数',
    `type` TINYINT NOT NULL COMMENT '类型：1-违规扣分，2-支付恢复，3-自然恢复，4-管理员调整',
    `reason` VARCHAR(255) DEFAULT NULL COMMENT '原因描述',
    `related_order_id` VARCHAR(32) DEFAULT NULL COMMENT '关联订单ID',
    `related_task_id` VARCHAR(32) DEFAULT NULL COMMENT '关联任务ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信誉分变动记录表';
```

### 6.2 新增 appeal 表

```sql
CREATE TABLE `appeal` (
    `id` VARCHAR(32) NOT NULL,
    `order_id` VARCHAR(32) NOT NULL COMMENT '关联订单ID',
    `task_id` VARCHAR(32) DEFAULT NULL COMMENT '关联任务ID',
    `applicant_id` VARCHAR(32) NOT NULL COMMENT '申诉人ID',
    `respondent_id` VARCHAR(32) NOT NULL COMMENT '被申诉方ID',
    `appeal_type` TINYINT NOT NULL COMMENT '1-拒不支付，2-虚假送达，3-恶意退款，4-物品损坏，5-虚假确认',
    `status` TINYINT DEFAULT 0 COMMENT '0-待审核，1-已受理，2-已驳回',
    `compensation_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '赔付金额',
    `evidence` VARCHAR(500) DEFAULT NULL COMMENT '申诉描述/证据',
    `admin_id` VARCHAR(32) DEFAULT NULL COMMENT '审核管理员ID',
    `audit_remark` VARCHAR(255) DEFAULT NULL COMMENT '审核意见',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_applicant_id` (`applicant_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申诉表';
```

## 7. API 设计

| 接口 | 方法 | 说明 |
|------|------|------|
| `/credit/my-records` | GET | 用户查看自己的信誉分变动记录 |
| `/credit/restore-price` | GET | 计算恢复到指定分数所需金额 |
| `/credit/restore` | POST | 支付恢复信誉分 |
| `/appeal` | POST | 用户发起申诉 |
| `/appeal/my` | GET | 用户查看自己的申诉列表 |
| `/admin/appeal/list` | GET | 管理员查看申诉列表 |
| `/admin/appeal/{id}/audit` | POST | 管理员审核申诉 |
| `/admin/credit-pool` | GET | 查看赔偿金池余额 |

## 8. 前端改动

| 页面 | 改动 |
|------|------|
| ProfileView.vue | 信誉分展示增强，入口到明细页 |
| CreditScoreView.vue（新增） | 信誉分明细列表 + 弥补信誉分支付 |
| OrderDetailView.vue | 新增"我要申诉"按钮（订单完成7天内） |
| AppLayout.vue | 60-79分时显示信誉分警告条 |
| AdminView.vue | 新增申诉管理Tab + 赔偿金池卡片 |

## 9. 代码改动清单

### 后端新增
- `entity/CreditRecord.java`
- `entity/Appeal.java`
- `mapper/CreditRecordMapper.java`
- `mapper/AppealMapper.java`
- `service/CreditService.java` + `impl/CreditServiceImpl.java`
- `controller/AppealController.java`
- `controller/CreditController.java`

### 后端修改
- `entity/User.java` — 已有 creditScore，无需改动
- `service/impl/TaskServiceImpl.java` — publishTask / acceptTask 加信誉分校验
- `service/impl/UserServiceImpl.java` — login 加冻结拦截
- `service/impl/AdminServiceImpl.java` — 新增申诉审核、赔偿金池方法
- `controller/AdminController.java` — 新增申诉管理、赔偿金池接口
- `config/ScheduleTask.java` — 扣分改为通过 CreditService，写入 credit_record

### 前端新增
- `views/CreditScoreView.vue`

### 前端修改
- `views/ProfileView.vue`
- `views/OrderDetailView.vue`
- `components/AppLayout.vue`
- `views/AdminView.vue`

### 数据库
- 新增 `credit_record` 表
- 新增 `appeal` 表
- 修改 `init.sql`
