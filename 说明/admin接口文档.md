# Admin 管理端接口文档

---

## 统一返回结构

所有接口的基础返回结构统一为 `Result<T>`，其中 code=200 表示成功，500 表示失败。

```json
{
    "code": 200,       // 状态码：200成功，500失败
    "msg": "xxx",      // 错误信息（仅失败时返回）
    "data": {}         // 业务数据（泛型 T）
}
```

统一错误处理

```java
/**
 * 处理自定义业务异常 BaseException
 * 返回 200 状态码 + Result.error
 */
@ExceptionHandler(BaseException.class)
public Result exception(BaseException e) {
    return Result.error(e.getMessage() + ">>>>去联系管理员");
}
/**
 * @param e
 * @return
 */
@ExceptionHandler(Exception.class)
public Result handleException(Exception e) {
    log.error("未知异常: {}", e.getMessage(), e);  // 关键:打印堆栈,方便排查
    return Result.error("服务器开小差了,请稍后再试");
}

```

---

## 1. 员工管理 AdminEmployeeController

**基础路径**: `/admin/employee`

该控制器负责后台员工的注册、登录、登出及 CRUD 操作。

### 1.1 员工注册

- **方法**: `POST`
- **路径**: `/admin/employee/register`
- **描述**: 创建新的员工账号
- **权限**: 无

**请求体 (EmployeeDTO)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名（唯一） |
| password | String | 否 | 密码，默认为 123456 |
| work | String | 否 | 工作/职位 |
| avatar | String | 否 | 头像 URL |
| email | String | 否 | 邮箱 |
| phone | String | 否 | 手机号 |
| sex | String | 否 | 性别 |
| status | Long | 否 | 状态 0:禁用 1:启用（默认为 1） |

**业务逻辑**:

1. 根据用户名查询是否已存在，存在则抛注册异常
2. 将 DTO 转换为 Employee 实体，默认设置为启用状态，密码使用 BCrypt 加密
3. 设置 createUser/updateUser 为 0L（系统创建）
4. 保存到数据库

**响应**: `Result<String>` → `"register"`

---

### 1.2 员工登录

- **方法**: `POST`
- **路径**: `/admin/employee/login`
- **描述**: 员工登录，返回 JWT Token
- **权限**: 无

**请求体 (LoginDTO)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**业务逻辑**:
1. 使用 Spring Security 的 AuthenticationManager 进行身份认证
2. 认证成功后查询员工完整信息
3. 生成 JWT Token，载荷包含员工 ID、用户名、角色（ROLE_ADMIN）
4. 将 Token 存入 Redis（key 前缀 `emp:auth:` + 员工 ID），过期时间与 JWT 一致

**响应**: `Result<String>` → JWT Token 字符串

---

### 1.3 员工登出

- **方法**: `POST`
- **路径**: `/admin/employee/logout`
- **描述**: 清除当前登录状态
- **权限**: 需登录

**请求参数**: 无

**业务逻辑**:
1. 从 SecurityContext 获取当前登录员工 ID
2. 删除 Redis 中对应的 Token
3. 清空 SecurityContext

**响应**: `Result<String>` → `"logout"`

---

### 1.4 根据 ID 查询员工

- **方法**: `GET`
- **路径**: `/admin/employee`
- **描述**: 获取单个员工信息
- **权限**: 需 ADMIN 角色

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 员工主键 ID |

**业务逻辑**:
1. 根据 ID 查询员工，不存在则抛"账号不存在"异常
2. 将 Employee 实体转换为 EmployeeVO 返回

**响应**: `Result<EmployeeVO>`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| username | String | 用户名 |
| avatar | String | 头像 |
| work | String | 职位 |
| email | String | 邮箱 |
| phone | String | 手机号 |
| status | Long | 状态 0:禁用 1:启用 |

---

### 1.5 分页查询员工列表

- **方法**: `GET`
- **路径**: `/admin/employee/all`
- **描述**: 分页查询员工列表，支持按用户名模糊搜索
- **权限**: 需登录

**请求参数 (EmployeePageDTO, Query 参数)**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Long | 否 | 1 | 页码，最小 1 |
| pageSize | Long | 否 | 10 | 每页条数，范围 1-20 |
| employeename | String | 否 | - | 用户名模糊搜索 |

**业务逻辑**:
1. 构建分页查询，支持用户名模糊匹配
2. 使用 MyBatis-Plus 的 Page 进行分页
3. 将查询结果转换为 EmployeeVO 列表

**响应**: `Result<List<EmployeeVO>>`

---

### 1.6 更新员工信息

- **方法**: `PUT`
- **路径**: `/admin/employee`
- **描述**: 根据 ID 更新员工信息（部分字段更新）
- **权限**: 需登录

**请求体 (EmployeeDTO)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 员工主键 ID |
| username | String | 否 | 用户名 |
| password | String | 否 | 密码（自动加密） |
| work | String | 否 | 职位 |
| avatar | String | 否 | 头像 |
| email | String | 否 | 邮箱 |
| phone | String | 否 | 手机号 |
| sex | String | 否 | 性别 |
| status | Long | 否 | 状态 |

**业务逻辑**:
1. 校验 ID 不为空
2. 构建 LambdaUpdateWrapper，只 set 非空字段
3. 密码字段使用 BCrypt 加密后存入
4. 执行数据库更新

**响应**: `Result<Long>` → 更新的员工 ID

---

### 1.7 批量删除员工

- **方法**: `DELETE`
- **路径**: `/admin/employee`
- **描述**: 根据 ID 列表批量删除员工
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 员工 ID 列表 |

**业务逻辑**:
1. 校验 ids 列表不为空
2. 调用 MyBatis-Plus 的 removeByIds 批量删除

**响应**: `Result<List<Long>>` → 删除的 ID 列表

---

## 2. 节日礼盒管理 AdminFestivalController

**基础路径**: `/admin/festival`

该控制器负责节日礼盒的增删改查，使用 Redis 缓存 + Redisson 分布式锁 + 逻辑过期策略。

### 2.1 新增节日礼盒

- **方法**: `POST`
- **路径**: `/admin/festival`
- **描述**: 创建新的节日礼盒
- **权限**: 需登录

**请求体 (FestivalDTO)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 礼盒名称 |
| categoryId | Long | 是 | 所属分类 ID |
| price | BigDecimal | 是 | 礼盒价格 |
| number | Long | 否 | 鲜花总数量 |
| status | Long | 否 | 售卖状态 0:下架 1:在售 |
| description | String | 否 | 礼盒描述 |
| image | String | 否 | 礼盒图片 URL |

**业务逻辑**:
1. 将 DTO 拷贝为 Festival 实体
2. 保存到数据库（自动回填 id、createTime、updateTime）
3. 将带自增 ID 的实体转回 DTO 返回

**响应**: `Result<FestivalDTO>` → 带 id 的 DTO

---

### 2.2 根据 ID 查询节日礼盒

- **方法**: `GET`
- **路径**: `/admin/festival`
- **描述**: 查询单个礼盒详情（带缓存）
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 礼盒主键 ID |

**业务逻辑**:
1. 优先查 Redis 缓存，key 前缀 `festival:` + ID
2. 缓存存在且未过期 → 直接返回；剩余 TTL < 3s 时提前续期
3. 缓存不存在 → 使用 Redisson 分布式锁保护，查询数据库并重建缓存（含空值防穿透）
4. 缓存逻辑过期 → 异步重建并立即返回旧数据（不阻塞用户）
5. Redis 异常时降级直查数据库

**响应**: `Result<FestivalVO>`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| categoryId | Long | 所属分类 ID |
| name | String | 礼盒名称 |
| price | BigDecimal | 价格 |
| number | Long | 鲜花总数量 |
| status | Long | 售卖状态 0:下架 1:在售 |
| statusDesc | String | 售卖状态描述 |
| description | String | 描述 |
| image | String | 图片 URL |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

---

### 2.3 分页查询礼盒列表

- **方法**: `GET`
- **路径**: `/admin/festival/all`
- **描述**: 分页查询礼盒列表，支持按名称模糊搜索
- **权限**: 需登录

**请求参数 (FestivalPageDTO, Query 参数)**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Long | 否 | 1 | 页码 |
| pageSize | Long | 否 | 10 | 每页条数（1-20） |
| name | String | 否 | - | 礼盒名称模糊搜索 |

**业务逻辑**:
1. 构建 LambdaQueryWrapper，支持名称模糊匹配
2. 使用 MyBatis-Plus Page 分页查询
3. 将 Festival 实体转换为 FestivalVO 列表

**响应**: `Result<List<FestivalVO>>`

---

### 2.4 更新节日礼盒

- **方法**: `PUT`
- **路径**: `/admin/festival`
- **描述**: 根据 ID 更新礼盒，并清除缓存
- **权限**: 需登录

**请求体 (FestivalDTO)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 主键 ID |
| name | String | 否 | 礼盒名称 |
| categoryId | Long | 否 | 分类 ID |
| price | BigDecimal | 否 | 价格 |
| number | Long | 否 | 鲜花数量 |
| status | Long | 否 | 状态 |
| description | String | 否 | 描述 |
| image | String | 否 | 图片 |

**业务逻辑**:
1. 校验 ID 不为空
2. 构建 LambdaUpdateWrapper，只 set 非空字段
3. 执行数据库更新
4. 删除 Redis 中对应的缓存 key

**响应**: `Result<FestivalDTO>` → 更新后的 DTO

---

### 2.5 批量删除节日礼盒

- **方法**: `DELETE`
- **路径**: `/admin/festival`
- **描述**: 根据 ID 列表批量删除礼盒，并清除缓存
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 礼盒 ID 列表 |

**业务逻辑**:
1. 调用 removeByIds 批量删除数据库记录
2. 遍历 ids，逐个删除 Redis 缓存 key

**响应**: `Result<List<Long>>` → 删除的 ID 列表

---

### 2.6 查询礼盒下的明细列表

- **方法**: `GET`
- **路径**: `/admin/festival/of/festivalDetail`
- **描述**: 根据礼盒 ID 查询其包含的所有礼盒明细（关联鲜花）
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 礼盒主键 ID |

**业务逻辑**:
1. 在 festival_detail 表中按 festival_id 查询所有关联记录
2. 转换为 FestivalDetailVO 返回

**响应**: `Result<List<FestivalDetailVO>>`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 明细主键 |
| festivalId | Long | 关联礼盒 ID |
| flowerId | Long | 关联鲜花 ID |
| specObject | String | 送人对象（如女友、母亲） |
| specOption | String | 用途场景（如表白、生日） |

---

### 2.7 查询某鲜花被哪些礼盒包含

- **方法**: `GET`
- **路径**: `/admin/festival/of/flower`
- **描述**: 根据鲜花 ID 查询它被哪些礼盒关联
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 鲜花主键 ID |

**业务逻辑**:
1. 在 festival_detail 表中按 flower_id 查询所有记录
2. 转换为 FestivalDetailVO 返回

**响应**: `Result<List<FestivalDetailVO>>`

---

## 3. 礼盒明细管理 AdminFestivalDetailController

**基础路径**: `/admin/festivalDetail`

管理礼盒与鲜花的关联明细，支持单条 CRUD。使用 Redis 缓存 + Redisson 分布式锁。

### 3.1 新增礼盒明细

- **方法**: `POST`
- **路径**: `/admin/festivalDetail`
- **描述**: 在某个礼盒中添加一条鲜花明细
- **权限**: 需登录

**请求体 (FestivalDetailDTO)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| festivalId | Long | 是 | 关联礼盒 ID |
| flowerId | Long | 是 | 关联鲜花 ID |
| specObject | String | 否 | 送人对象（如女友） |
| specOption | String | 否 | 用途场景（如表白） |

**业务逻辑**:
1. 将 DTO 拷贝为 FestivalDetail 实体
2. 保存到数据库
3. 转换回 DTO 返回（含自增 ID）

**响应**: `Result<FestivalDetailDTO>`

---

### 3.2 根据 ID 查询礼盒明细

- **方法**: `GET`
- **路径**: `/admin/festivalDetail`
- **描述**: 查询单条明细详情（带缓存）
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 明细主键 ID |

**业务逻辑**:
1. 优先查 Redis 缓存，key 前缀 `festivalDetail:` + ID
2. 缓存未过期直接返回；剩余 TTL < 3s 时续期
3. 缓存不存在 → 分布式锁保护，查库并重建缓存
4. 缓存过期 → 异步重建，立即返回旧数据

**响应**: `Result<FestivalDetailVO>`（字段见 2.6 节）

---

### 3.3 更新礼盒明细

- **方法**: `PUT`
- **路径**: `/admin/festivalDetail`
- **描述**: 更新单条礼盒明细，并清除缓存
- **权限**: 需登录

**请求体 (FestivalDetailDTO)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 主键 ID |
| festivalId | Long | 否 | 礼盒 ID |
| flowerId | Long | 否 | 鲜花 ID |
| specObject | String | 否 | 送人对象 |
| specOption | String | 否 | 用途场景 |

**业务逻辑**:
1. 校验 ID 不为空
2. 构建 LambdaUpdateWrapper，只 set 非空字段
3. 更新数据库
4. 删除 Redis 缓存

**响应**: `Result<FestivalDetailDTO>`

---

### 3.4 批量删除礼盒明细

- **方法**: `DELETE`
- **路径**: `/admin/festivalDetail`
- **描述**: 根据 ID 列表批量删除明细
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 明细 ID 列表 |

**业务逻辑**:
1. removeByIds 批量删除
2. 遍历删除 Redis 缓存 key

**响应**: `Result<List<Long>>`

---

## 4. 鲜花分类管理 AdminFlowerCategoryController

**基础路径**: `/admin/category`

管理鲜花商品的分类，支持按类型查询、分页查询、增删改。使用 Spring Cache（Redis）做全量缓存。

### 4.1 新增鲜花分类

- **方法**: `POST`
- **路径**: `/admin/category`
- **描述**: 创建新的鲜花分类
- **权限**: 需登录

**请求体 (FlowerCategoryDTO)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 分类名称 |
| type | Long | 是 | 分类类型：1=鲜花商品单只，2=节日商品多只，3=礼品 |
| sort | Long | 否 | 排序序号 |
| status | Long | 否 | 状态 0:禁用 1:启用 |

**业务逻辑**:
1. DTO 拷贝为 FlowerCategory 实体
2. 保存数据库（自动填充 id、createTime、updateTime、createUser、updateUser）
3. 转换回 DTO 返回
4. 清除 Spring Cache 中所有分类缓存（@CacheEvict allEntries）

**响应**: `Result<FlowerCategoryDTO>`

---

### 4.2 根据类型查询分类列表

- **方法**: `GET`
- **路径**: `/admin/category`
- **描述**: 按分类类型查询所有分类
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | Long | 是 | 分类类型（1/2/3） |

**业务逻辑**:
1. 使用 Spring Cache（@Cacheable）按 type 做缓存
2. 缓存未命中则查询数据库 `WHERE type = ?`
3. 结果为空时抛"分类不存在"异常

**响应**: `Result<List<FlowerCategoryVO>>`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 分类名称 |
| type | Long | 分类类型（1/2/3） |
| sort | Long | 排序序号 |
| status | Long | 状态 0:禁用 1:启用 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| createUser | Long | 创建人 ID |
| updateUser | Long | 修改人 ID |

---

### 4.3 分页查询分类列表

- **方法**: `GET`
- **路径**: `/admin/category/all`
- **描述**: 分页查询所有分类，支持按类型筛选
- **权限**: 需登录

**请求参数 (FlowerCategoryPageDTO, Query 参数)**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Long | 否 | 1 | 页码 |
| pageSize | Long | 否 | 10 | 每页条数（1-20） |
| type | Long | 否 | - | 分类类型筛选 |

**业务逻辑**:
1. 构建 LambdaQueryWrapper，type 非空时添加条件
2. MyBatis-Plus Page 分页查询
3. 转换为 FlowerCategoryVO 列表返回

**响应**: `Result<List<FlowerCategoryVO>>`

---

### 4.4 更新鲜花分类

- **方法**: `PUT`
- **路径**: `/admin/category`
- **描述**: 根据 ID 更新分类，并清除缓存
- **权限**: 需登录

**请求体 (FlowerCategoryDTO)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 主键 ID |
| name | String | 否 | 分类名称 |
| type | Long | 否 | 分类类型 |
| sort | Long | 否 | 排序 |
| status | Long | 否 | 状态 |

**业务逻辑**:
1. 校验 ID 不为空
2. LambdaUpdateWrapper 只 set 非空字段
3. 执行更新
4. 清除 Spring Cache（@CacheEvict）

**响应**: `Result<FlowerCategoryDTO>`

---

### 4.5 批量删除鲜花分类

- **方法**: `DELETE`
- **路径**: `/admin/category`
- **描述**: 根据 ID 列表批量删除分类
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 分类 ID 列表 |

**业务逻辑**:
1. 校验 ids 不为空
2. removeByIds 批量删除
3. 清除 Spring Cache

**响应**: `Result<List<Long>>`

---

### 4.6 查询分类下的所有鲜花

- **方法**: `GET`
- **路径**: `/admin/category/of/flower`
- **描述**: 根据分类 ID 查询其下所有鲜花
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 分类主键 ID |

**业务逻辑**:
1. 在 flower 表中按 category_id 查询
2. 转换为 FlowerVO 返回

**响应**: `Result<List<FlowerVO>>`（字段见 5.2 节）

---

### 4.7 查询分类下的所有礼盒

- **方法**: `GET`
- **路径**: `/admin/category/of/festival`
- **描述**: 根据分类 ID 查询其下所有节日礼盒
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 分类主键 ID |

**业务逻辑**:
1. 在 festival 表中按 category_id 查询
2. 转换为 FestivalVO 返回

**响应**: `Result<List<FestivalVO>>`（字段见 2.2 节）

---

## 5. 鲜花单品管理 AdminFlowerController

**基础路径**: `/admin/flower`

管理鲜花单品，使用 Redis 缓存 + Redisson 分布式锁 + 逻辑过期策略，结构与 Festival 类似。

### 5.1 新增鲜花

- **方法**: `POST`
- **路径**: `/admin/flower`
- **描述**: 创建新的鲜花单品
- **权限**: 需登录

**请求体 (FlowerDTO)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 鲜花名称 |
| color | String | 否 | 颜色 |
| categoryId | Long | 是 | 所属分类 ID |
| price | BigDecimal | 是 | 单价 |
| image | String | 否 | 商品图片 |
| description | String | 否 | 花语/描述 |
| status | Long | 否 | 售卖状态 0:下架 1:在售 |

**业务逻辑**:
1. DTO 拷贝为 Flower 实体
2. 保存数据库（回填自增 ID）
3. 转回 DTO 返回

**响应**: `Result<FlowerDTO>`

---

### 5.2 根据 ID 查询鲜花

- **方法**: `GET`
- **路径**: `/admin/flower`
- **描述**: 查询单个鲜花详情（带缓存）
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 鲜花主键 ID |

**业务逻辑**:
1. 优先查 Redis 缓存，key 前缀 `flower:` + ID
2. 缓存存在且未过期直接返回；剩余 TTL < 3s 续期
3. 缓存不存在 → Redisson 分布式锁保护，查库重建缓存
4. 缓存过期 → 异步重建，立即返回旧数据
5. Redis 异常降级直查数据库

**响应**: `Result<FlowerVO>`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 鲜花名称 |
| categoryId | Long | 所属分类 ID |
| categoryName | String | 分类名称（关联查询） |
| color | String | 颜色 |
| price | BigDecimal | 单价 |
| image | String | 图片 URL |
| description | String | 花语描述 |
| status | Long | 状态 0:下架 1:在售 |
| statusDesc | String | 状态描述 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

---

### 5.3 分页查询鲜花列表

- **方法**: `GET`
- **路径**: `/admin/flower/all`
- **描述**: 分页查询鲜花列表，支持按名称模糊搜索
- **权限**: 需登录

**请求参数 (FlowerPageDTO, Query 参数)**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Long | 否 | 1 | 页码 |
| pageSize | Long | 否 | 10 | 每页条数（1-20） |
| name | String | 否 | - | 鲜花名称模糊搜索 |

**业务逻辑**:
1. 构建 LambdaQueryWrapper，支持名称 like 匹配
2. Page 分页查询
3. 转换为 FlowerVO 列表

**响应**: `Result<List<FlowerVO>>`

---

### 5.4 更新鲜花

- **方法**: `PUT`
- **路径**: `/admin/flower`
- **描述**: 根据 ID 更新鲜花，并清除缓存
- **权限**: 需登录

**请求体 (FlowerDTO)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 主键 ID |
| name | String | 否 | 名称 |
| color | String | 否 | 颜色 |
| categoryId | Long | 否 | 分类 ID |
| price | BigDecimal | 否 | 单价 |
| image | String | 否 | 图片 |
| description | String | 否 | 描述 |
| status | Long | 否 | 状态 |

**业务逻辑**:
1. 校验 ID
2. LambdaUpdateWrapper 只更新非空字段
3. 执行数据库更新
4. 删除 Redis 缓存 key

**响应**: `Result<FlowerDTO>`

---

### 5.5 批量删除鲜花

- **方法**: `DELETE`
- **路径**: `/admin/flower`
- **描述**: 根据 ID 列表批量删除鲜花
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 鲜花 ID 列表 |

**业务逻辑**:
1. removeByIds 批量删除
2. 遍历删除 Redis 缓存

**响应**: `Result<List<Long>>`

---

### 5.6 查询鲜花下的所有明细

- **方法**: `GET`
- **路径**: `/admin/flower/of/flowerDetail`
- **描述**: 根据鲜花 ID 查询其所有规格明细（送人对象/用途场景）
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 鲜花主键 ID |

**业务逻辑**:
1. 在 flower_detail 表按 flower_id 查询
2. 转换为 FlowerDetailVO 返回

**响应**: `Result<List<FlowerDetailVO>>`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 明细主键 |
| flowerId | Long | 关联鲜花 ID |
| specObject | String | 送人对象（如女友、母亲） |
| specOption | String | 用途/场景（如表白、生日） |

---

## 6. 鲜花明细管理 AdminFlowerDetailController

**基础路径**: `/admin/flowerDetail`

管理鲜花的规格明细（送人对象、用途场景），支持单条 CRUD，使用 Redis 缓存 + Redisson 分布式锁。

### 6.1 新增鲜花明细

- **方法**: `POST`
- **路径**: `/admin/flowerDetail`
- **描述**: 为鲜花添加一条规格明细
- **权限**: 需登录

**请求体 (FlowerDetailDTO)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| flowerId | Long | 是 | 关联鲜花 ID |
| specObject | String | 否 | 送人对象（如女友） |
| specOption | String | 否 | 用途场景（如表白） |

**业务逻辑**:
1. DTO 拷贝为 FlowerDetail 实体
2. 保存数据库，回填自增 ID
3. 转回 DTO 返回

**响应**: `Result<FlowerDetailDTO>`

---

### 6.2 根据 ID 查询鲜花明细

- **方法**: `GET`
- **路径**: `/admin/flowerDetail`
- **描述**: 查询单条鲜花明细详情（带缓存）
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 明细主键 ID |

**业务逻辑**:
1. 优先查 Redis 缓存，key 前缀 `flowerDetail:` + ID
2. 缓存未过期直接返回；剩余 TTL < 3s 续期
3. 缓存不存在 → Redisson 锁保护，查库重建
4. 缓存过期 → 异步重建，返回旧数据

**响应**: `Result<FlowerDetailVO>`（字段见 5.6 节）

---

### 6.3 更新鲜花明细

- **方法**: `PUT`
- **路径**: `/admin/flowerDetail`
- **描述**: 更新单条鲜花明细，并清除缓存
- **权限**: 需登录

**请求体 (FlowerDetailDTO)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 主键 ID |
| flowerId | Long | 否 | 鲜花 ID |
| specObject | String | 否 | 送人对象 |
| specOption | String | 否 | 用途场景 |

**业务逻辑**:
1. 校验 ID 不为空
2. LambdaUpdateWrapper 只更新非空字段
3. 执行更新
4. 删除 Redis 缓存

**响应**: `Result<FlowerDetailDTO>`

---

### 6.4 批量删除鲜花明细

- **方法**: `DELETE`
- **路径**: `/admin/flowerDetail`
- **描述**: 根据 ID 列表批量删除
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 明细 ID 列表 |

**业务逻辑**:
1. removeByIds 批量删除
2. 遍历删除 Redis 缓存 key

**响应**: `Result<List<Long>>`

---

## 7. 鲜花订单管理 AdminFlowerOrderController

**基础路径**: `/admin/flowerOrder`

管理鲜花订单的状态流转。订单状态枚举（OrderStatusEnum）如下：

| 状态码 | 说明 |
|--------|------|
| 1 | 用户下单 |
| 2 | 用户确认支付 |
| 3 | 商家制作（COOKING） |
| 4 | 工作人员取货（GO） |
| 5 | 工作人员开始配送（DELIVERING） |
| 6 | 工作人员已到达（ARRIVED） |
| 7 | 系统自动确认完成（COMPLETED） |
| 8 | 已取消（CANCELLED） |

### 7.1 根据 ID 查询订单

- **方法**: `GET`
- **路径**: `/admin/flowerOrder`
- **描述**: 查询单个订单详情（实现为空）
- **权限**: 需登录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**业务逻辑**: （当前返回 null，待实现）

**响应**: `Result<FlowerOrderVO>`

---

### 7.2 分页查询订单列表

- **方法**: `GET`
- **路径**: `/admin/flowerOrder/all`
- **描述**: 分页查询订单列表（当前实现返回空列表）
- **权限**: 需登录

**请求参数 (FlowerOrderPageDTO, Query 参数)**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Long | 否 | 1 | 页码 |
| pageSize | Long | 否 | 10 | 每页条数（1-20） |
| startTime | String | 否 | - | 起始时间 |
| endTime | String | 否 | - | 结束时间 |

**业务逻辑**: （当前返回空 List，待实现完整查询逻辑）

**响应**: `Result<List<FlowerOrderVO>>`

---

### 7.3 订单状态流转 → 商家制作

- **方法**: `PUT`
- **路径**: `/admin/flowerOrder/cooking/{id}`
- **描述**: 将订单状态更新为"商家制作"（状态码 3）
- **权限**: 需登录

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**业务逻辑**:
1. 根据 ID 定位订单
2. 更新 status 为 COOKING（3）

**响应**: `Result<OrderStatusEnum>` → 返回 COOKING 枚举

---

### 7.4 订单状态流转 → 工作人员取货

- **方法**: `PUT`
- **路径**: `/admin/flowerOrder/go/{id}`
- **描述**: 将订单状态更新为"工作人员取货"（状态码 4）
- **权限**: 需登录

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**业务逻辑**:
1. 根据 ID 定位订单
2. 更新 status 为 GO（4）

**响应**: `Result<OrderStatusEnum>` → 返回 GO 枚举

---

### 7.5 订单状态流转 → 配送中

- **方法**: `PUT`
- **路径**: `/admin/flowerOrder/delivering/{id}`
- **描述**: 将订单状态更新为"配送中"（状态码 5）
- **注意**: 代码中返回了 CANCELLED，但实际更新的是 DELIVERING（5）

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**业务逻辑**: 更新 status 为 DELIVERING（5）

**响应**: `Result<OrderStatusEnum>` →（代码返回 CANCELLED，疑似 Bug）

---

### 7.6 订单状态流转 → 已到达

- **方法**: `PUT`
- **路径**: `/admin/flowerOrder/arrived/{id}`
- **描述**: 将订单状态更新为"工作人员已到达"（状态码 6）
- **权限**: 需登录

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**业务逻辑**: 更新 status 为 ARRIVED（6）

**响应**: `Result<OrderStatusEnum>` → 返回 ARRIVED 枚举

---

### 7.7 订单状态流转 → 完成

- **方法**: `PUT`
- **路径**: `/admin/flowerOrder/complete/{id}`
- **描述**: 将订单状态更新为"已完成"（状态码 7）
- **权限**: 需登录

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**业务逻辑**: 更新 status 为 COMPLETED（7）

**响应**: `Result<OrderStatusEnum>` → 返回 COMPLETED 枚举

---

### 7.8 订单状态流转 → 已取消（含退款）

- **方法**: `PUT`
- **路径**: `/admin/flowerOrder/canceled/{id}`
- **描述**: 取消订单，同时触发支付宝退款
- **权限**: 需登录

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**业务逻辑**:
1. 查询订单详情和订单明细
2. 构建 RefundDTO：outTradeNo、outRefundNo 均为订单 ID，退款金额取自订单明细
3. 调用 ZhifubaoService 执行退款
4. 更新支付表的 pay_status 为 REFUNDED
5. 更新订单 status 为 CANCELLED（8）
6. 退款失败时抛 RuntimeException

**响应**: `Result<OrderStatusEnum>` → 返回 CANCELLED 枚举

---

## 8. 店铺状态管理 AdminShopController

**基础路径**: `/admin/shop`

管理店铺的营业状态，数据存储在 Redis 中，简单的开关控制器。

### 8.1 更新店铺营业状态

- **方法**: `POST`
- **路径**: `/admin/shop/{status}`
- **描述**: 设置店铺营业/打烊状态
- **权限**: 需登录

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Long | 是 | 1=营业中，其他值=已打烊 |

**业务逻辑**:
1. 根据 status 判断文案：1 → "营业中"，其他 → "已打烊"
2. 将状态文案存入 Redis（key 为 ShopConstant.SHOP_STATUS）
3. 构建 ShopVO 返回

**响应**: `Result<ShopVO>`

| 字段 | 类型 | 说明 |
|------|------|------|
| status | String | 店铺状态文案（"营业中" / "已打烊"） |

---

### 8.2 查询店铺营业状态

- **方法**: `GET`
- **路径**: `/admin/shop`
- **描述**: 获取当前店铺的营业状态
- **权限**: 需登录

**请求参数**: 无

**业务逻辑**:
1. 从 Redis 读取 ShopConstant.SHOP_STATUS 对应的值
2. 构建 ShopVO 返回

**响应**: `Result<ShopVO>`

| 字段 | 类型 | 说明 |
|------|------|------|
| status | String | 店铺状态文案（"营业中" / "已打烊"） |

---

## 附录

**异常处理**

spring-flower/start/src/main/java/start/exceptionhandle

**认证方式**

- **员工登录**：POST `/admin/employee/login`，返回 JWT Token
- **Token 存储**：Redis key 前缀 `emp:auth:` + 员工 ID
- **请求携带**：需要在请求头中携带 JWT Token 进行身份认证
- **权限注解**：`@PreAuthorize("hasAuthority(T(common.constant.RoleConstant).ROLE_ADMIN)")` 用于 ADMIN 角色校验
