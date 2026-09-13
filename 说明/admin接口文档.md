# Admin 管理端接口文档

### 统一返回结构

所有接口的基础返回结构统一为 `Result<T>`，其中 code=200 表示成功，500 表示失败。

```json
{
    "code": 200,       // 状态码：200成功，500失败
    "msg": "xxx",      // 错误信息（仅失败时返回）
    "data": {}         // 业务数据（泛型 T）
}
```

分页接口的 `data` 统一为 `PageResult<T>`：

```json
{
    "total": 100,      // 总记录数
    "list": [],        // 当前页数据
    "pageNum": 1,      // 当前页码
    "pageSize": 10     // 每页条数
}
```

> 注意：`/oss`、`/local`、`/report/excel/download` 为文件流/ResponseEntity 接口，**不使用** `Result<T>` 包装，见第 10、11 节。

### 统一错误处理

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

### 认证与权限

- **管理员登录**：`POST /admin/login`，返回 JWT Token
- **请求携带**：请求头 `Authorization: Bearer <token>`
- **Token 存储**：Redis key 前缀 `flower:admin:` + 管理员 ID（员工端 token 前缀为 `flower:emp:`）
- **权限控制**（Spring Security 配置）：
  - `/admin/login` 放行（permitAll）
  - `/admin/**` 需 `ROLE_ADMIN` 或 `ROLE_EMP` 角色
- **方法级权限**：由 Service 接口方法上的 `@PreAuthorize` 注解控制（`spring-flower/service` 模块），如 `hasAuthority('ROLE_ADMIN')`，未标注注解的接口仅受 URL 级配置约束。
- 所有写操作接口均有 `@OperationLogging` 操作日志切面记录。

---

## 1. 管理员账户 AdminController

**基础路径**: `/admin`

该控制器负责管理员登录、登出，以及后台员工账号的查询与删除（员工注册/资料修改接口在员工端 `/employee/**`，本文档不包含）。

### 1.1 管理员登录

- **方法**: `POST`
- **路径**: `/admin/login`
- **描述**: 管理员账号密码登录，返回 JWT Token
- **权限**: 无（放行）

**请求体 (LoginDTO, JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 管理员用户名 |
| password | String | 是 | 密码 |

**响应**: `Result<String>` → JWT Token 字符串

---

### 1.2 管理员登出

- **方法**: `POST`
- **路径**: `/admin/logout`
- **描述**: 清除当前登录状态
- **权限**: 需登录（ROLE_ADMIN / ROLE_EMP）

**请求参数**: 无

**响应**: `Result<String>` → `"logout"`

---

### 1.3 根据 ID 查询员工

- **方法**: `GET`
- **路径**: `/admin`
- **描述**: 获取单个员工信息
- **权限**: ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 员工主键 ID |

**响应**: `Result<EmployeeVO>`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| username | String | 用户名 |
| avatar | String | 头像 |
| work | String | 职位 |
| sex | String | 性别（男/女） |
| email | String | 邮箱 |
| phone | String | 手机号 |
| status | Long | 状态 0:禁用 1:启用 |

---

### 1.4 分页查询员工列表

- **方法**: `GET`
- **路径**: `/admin/all`
- **描述**: 分页查询员工列表，支持按用户名模糊搜索
- **权限**: ROLE_ADMIN

**请求参数 (EmployeePageDTO, Query 参数)**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Long | 否 | 1 | 页码，最小 1 |
| pageSize | Long | 否 | 10 | 每页条数（1~20） |
| employeename | String | 否 | - | 用户名模糊搜索 |

**响应**: `Result<PageResult<EmployeeVO>>`（EmployeeVO 字段见 1.3 节）

---

### 1.5 修改密码

- **方法**: `PUT`
- **路径**: `/admin/password`
- **描述**: 修改当前登录管理员的密码
- **权限**: ROLE_ADMIN / ROLE_EMP

**请求体 (PasswordDTO, JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| newPassword | String | 是（@NotBlank） | 新密码 |
| confirmPassword | String | 是（@NotBlank） | 确认密码 |

**响应**: `Result<String>` → `"logout"`

---

### 1.6 批量删除员工

- **方法**: `DELETE`
- **路径**: `/admin`
- **描述**: 根据 ID 列表批量删除员工
- **权限**: ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 员工 ID 列表，如 `ids=1&ids=2` |

**响应**: `Result<List<Long>>` → 删除的 ID 列表

---

## 2. 节日多花管理 AdminFestivalController

**基础路径**: `/admin/festival`

该控制器负责节日多花礼盒的增删改查，使用 Redis 缓存 + Redisson 分布式锁 + 逻辑过期策略。

### 2.1 新增节日多花礼盒

- **方法**: `POST`
- **路径**: `/admin/festival`
- **描述**: 创建新的节日多花礼盒
- **权限**: ROLE_ADMIN

**请求体 (FestivalDTO, JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 多花礼盒名称 |
| categoryId | Long | 是 | 所属分类 ID |
| price | BigDecimal | 是 | 多花礼盒价格 |
| status | Long | 否 | 售卖状态 0:下架 1:在售 |
| description | String | 否 | 多花礼盒描述 |
| image | String | 否 | 多花礼盒图片 URL |

**响应**: `Result<FestivalDTO>` → 带 id 的 DTO

---

### 2.2 根据 ID 查询节日多花礼盒

- **方法**: `GET`
- **路径**: `/admin/festival`
- **描述**: 查询单个多花礼盒详情（带缓存）
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 多花礼盒主键 ID |

**响应**: `Result<FestivalVO>`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| categoryId | Long | 所属分类 ID |
| name | String | 多花礼盒名称 |
| price | BigDecimal | 价格 |
| status | Long | 售卖状态 0:下架 1:在售 |
| statusDesc | String | 售卖状态描述 |
| description | String | 描述 |
| image | String | 图片 URL |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

---

### 2.3 分页查询多花礼盒列表

- **方法**: `GET`
- **路径**: `/admin/festival/all`
- **描述**: 分页查询多花礼盒列表，支持按名称模糊搜索
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (FestivalPageDTO, Query 参数)**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Long | 否 | 1 | 页码（最小 1） |
| pageSize | Long | 否 | 10 | 每页条数（1~20） |
| name | String | 否 | - | 多花礼盒名称模糊搜索 |

**响应**: `Result<PageResult<FestivalVO>>`（FestivalVO 字段见 2.2 节）

---

### 2.4 更新节日多花礼盒

- **方法**: `PUT`
- **路径**: `/admin/festival`
- **描述**: 根据 ID 更新多花礼盒，并清除缓存
- **权限**: ROLE_ADMIN

**请求体 (FestivalDTO, JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 主键 ID |
| name | String | 否 | 多花礼盒名称 |
| categoryId | Long | 否 | 分类 ID |
| price | BigDecimal | 否 | 价格 |
| status | Long | 否 | 状态 |
| description | String | 否 | 描述 |
| image | String | 否 | 图片 |

**响应**: `Result<FestivalDTO>` → 更新后的 DTO

---

### 2.5 批量删除节日多花礼盒

- **方法**: `DELETE`
- **路径**: `/admin/festival`
- **描述**: 根据 ID 列表批量删除多花礼盒，并清除缓存
- **权限**: ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 多花礼盒 ID 列表 |

**响应**: `Result<List<Long>>` → 删除的 ID 列表

---

### 2.6 查询多花礼盒下的明细列表

- **方法**: `GET`
- **路径**: `/admin/festival/of/festivalDetail`
- **描述**: 根据多花礼盒 ID 查询其包含的所有多花礼盒明细（关联鲜花）
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 多花礼盒主键 ID |

**响应**: `Result<List<FestivalDetailVO>>`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 明细主键 |
| festivalId | Long | 关联多花礼盒 ID |
| flowerId | Long | 关联鲜花 ID |
| specNumber | Long | 鲜花数量（该鲜花在礼盒中的数量） |
| specObject | String | 送人对象（如女友、母亲） |
| specOption | String | 用途场景（如表白、生日） |

---

### 2.7 查询某鲜花被哪些多花礼盒包含

- **方法**: `GET`
- **路径**: `/admin/festival/of/flower`
- **描述**: 根据鲜花 ID 查询它被哪些多花礼盒关联
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 鲜花主键 ID |

**响应**: `Result<List<FestivalDetailVO>>`（字段见 2.6 节）

---

### 2.8 按送人对象查询多花礼盒明细

- **方法**: `GET`
- **路径**: `/admin/festival/of/object`
- **描述**: 按送人对象（specObject）模糊查询多花礼盒明细（`spec_object LIKE %object%`）
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| object | String | 是 | 送人对象关键词，如 `女友` |

**响应**: `Result<List<FestivalDetailVO>>`（字段见 2.6 节）

---

### 2.9 按用途场景查询多花礼盒明细

- **方法**: `GET`
- **路径**: `/admin/festival/of/option`
- **描述**: 按用途场景（specOption）模糊查询多花礼盒明细（`spec_option LIKE %option%`）
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| option | String | 是 | 用途场景关键词，如 `生日` |

**响应**: `Result<List<FestivalDetailVO>>`（字段见 2.6 节）

---

### 2.10 按用途场景查询多花礼盒明细

- **方法**: `GET`
- **路径**: `/admin/festival/of/nunmber`
- **描述**: 按用途数量（number）查询多花礼盒明细
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (Query)**:

| 参数   | 类型 | 必填 | 说明     |
| ------ | ---- | ---- | -------- |
| number | int  | 是   | 用途数量 |

**响应**: `Result<List<FestivalDetailVO>>`（字段见 2.6 节）

---

## 3. 多花明细管理 AdminFestivalDetailController

**基础路径**: `/admin/festivalDetail`

管理多花礼盒与鲜花的关联明细，支持单条 CRUD。使用 Redis 缓存 + Redisson 分布式锁。

### 3.1 新增多花礼盒明细

- **方法**: `POST`
- **路径**: `/admin/festivalDetail`
- **描述**: 在某个多花礼盒中添加一条鲜花明细
- **权限**: ROLE_ADMIN

**请求体 (FestivalDetailDTO, JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| festivalId | Long | 是 | 关联多花礼盒 ID |
| flowerId | Long | 是 | 关联鲜花 ID |
| specNumber | Long | 否 | 鲜花数量（该鲜花在礼盒中的数量） |
| specObject | String | 否 | 送人对象（如女友） |
| specOption | String | 否 | 用途场景（如表白） |

**响应**: `Result<FestivalDetailDTO>`

---

### 3.2 根据 ID 查询多花礼盒明细

- **方法**: `GET`
- **路径**: `/admin/festivalDetail`
- **描述**: 查询单条明细详情（带缓存）
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 明细主键 ID |

**响应**: `Result<FestivalDetailVO>`（字段见 2.6 节）

---

### 3.3 更新多花礼盒明细

- **方法**: `PUT`
- **路径**: `/admin/festivalDetail`
- **描述**: 更新单条多花礼盒明细，并清除缓存
- **权限**: ROLE_ADMIN

**请求体 (FestivalDetailDTO, JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 主键 ID |
| festivalId | Long | 否 | 多花礼盒 ID |
| flowerId | Long | 否 | 鲜花 ID |
| specNumber | Long | 否 | 鲜花数量 |
| specObject | String | 否 | 送人对象 |
| specOption | String | 否 | 用途场景 |

**响应**: `Result<FestivalDetailDTO>`

---

### 3.4 批量删除多花礼盒明细

- **方法**: `DELETE`
- **路径**: `/admin/festivalDetail`
- **描述**: 根据 ID 列表批量删除明细
- **权限**: ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 明细 ID 列表 |

**响应**: `Result<List<Long>>`

---

## 4. 鲜花分类管理 AdminFlowerCategoryController

**基础路径**: `/admin/category`

管理鲜花商品的分类，支持按类型查询、分页查询、增删改。使用 Spring Cache（Redis）做全量缓存。

### 4.1 新增鲜花分类

- **方法**: `POST`
- **路径**: `/admin/category`
- **描述**: 创建新的鲜花分类
- **权限**: ROLE_ADMIN

**请求体 (FlowerCategoryDTO, JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 分类名称 |
| type | Long | 是 | 分类类型：1=鲜花商品单只，2=节日商品多只，3=礼品 |
| sort | Long | 否 | 排序序号 |
| status | Long | 否 | 状态 0:禁用 1:启用 |

**响应**: `Result<FlowerCategoryDTO>`

---

### 4.2 根据类型查询分类列表

- **方法**: `GET`
- **路径**: `/admin/category`
- **描述**: 按分类类型查询所有分类
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | Long | 是 | 分类类型（1/2/3） |

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
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (FlowerCategoryPageDTO, Query 参数)**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Long | 否 | 1 | 页码（最小 1） |
| pageSize | Long | 否 | 10 | 每页条数（1~20） |
| type | Long | 否 | - | 分类类型筛选 |

**响应**: `Result<PageResult<FlowerCategoryVO>>`（字段见 4.2 节）

---

### 4.4 更新鲜花分类

- **方法**: `PUT`
- **路径**: `/admin/category`
- **描述**: 根据 ID 更新分类，并清除缓存
- **权限**: ROLE_ADMIN

**请求体 (FlowerCategoryDTO, JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 主键 ID |
| name | String | 否 | 分类名称 |
| type | Long | 否 | 分类类型 |
| sort | Long | 否 | 排序 |
| status | Long | 否 | 状态 |

**响应**: `Result<FlowerCategoryDTO>`

---

### 4.5 批量删除鲜花分类

- **方法**: `DELETE`
- **路径**: `/admin/category`
- **描述**: 根据 ID 列表批量删除分类
- **权限**: ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 分类 ID 列表 |

**响应**: `Result<List<Long>>`

---

### 4.6 查询分类下的所有鲜花

- **方法**: `GET`
- **路径**: `/admin/category/of/flower`
- **描述**: 根据分类 ID 查询其下所有鲜花
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 分类主键 ID |

**响应**: `Result<List<FlowerVO>>`（字段见 5.2 节）

---

### 4.7 查询分类下的所有多花礼盒

- **方法**: `GET`
- **路径**: `/admin/category/of/festival`
- **描述**: 根据分类 ID 查询其下所有节日多花礼盒
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 分类主键 ID |

**响应**: `Result<List<FestivalVO>>`（字段见 2.2 节）

---

## 5. 鲜花单品管理 AdminFlowerController

**基础路径**: `/admin/flower`

管理鲜花单品，使用 Redis 缓存 + Redisson 分布式锁 + 逻辑过期策略，结构与 Festival 类似。

### 5.1 新增鲜花

- **方法**: `POST`
- **路径**: `/admin/flower`
- **描述**: 创建新的鲜花单品
- **权限**: ROLE_ADMIN

**请求体 (FlowerDTO, JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 鲜花名称 |
| color | String | 否 | 颜色 |
| categoryId | Long | 是 | 所属分类 ID |
| price | BigDecimal | 是 | 单价 |
| image | String | 否 | 商品图片 |
| description | String | 否 | 花语/描述 |
| status | Long | 否 | 售卖状态 0:下架 1:在售 |

**响应**: `Result<FlowerDTO>`

---

### 5.2 根据 ID 查询鲜花

- **方法**: `GET`
- **路径**: `/admin/flower`
- **描述**: 查询单个鲜花详情（带缓存）
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 鲜花主键 ID |

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
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (FlowerPageDTO, Query 参数)**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Long | 否 | 1 | 页码（最小 1） |
| pageSize | Long | 否 | 10 | 每页条数（1~20） |
| name | String | 否 | - | 鲜花名称模糊搜索 |

**响应**: `Result<PageResult<FlowerVO>>`（字段见 5.2 节）

---

### 5.4 更新鲜花

- **方法**: `PUT`
- **路径**: `/admin/flower`
- **描述**: 根据 ID 更新鲜花，并清除缓存
- **权限**: ROLE_ADMIN

**请求体 (FlowerDTO, JSON)**:

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

**响应**: `Result<FlowerDTO>`

---

### 5.5 批量删除鲜花

- **方法**: `DELETE`
- **路径**: `/admin/flower`
- **描述**: 根据 ID 列表批量删除鲜花
- **权限**: ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 鲜花 ID 列表 |

**响应**: `Result<List<Long>>`

---

### 5.6 查询鲜花下的所有明细

- **方法**: `GET`
- **路径**: `/admin/flower/of/flowerDetail`
- **描述**: 根据鲜花 ID 查询其所有规格明细（送人对象/用途场景）
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 鲜花主键 ID |

**响应**: `Result<List<FlowerDetailVO>>`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 明细主键 |
| flowerId | Long | 关联鲜花 ID |
| specObject | String | 送人对象（如女友、母亲） |
| specOption | String | 用途/场景（如表白、生日） |

---

### 5.7 按送人对象查询鲜花明细

- **方法**: `GET`
- **路径**: `/admin/flower/of/object`
- **描述**: 按送人对象（specObject）模糊查询鲜花规格明细（`spec_object LIKE %object%`）
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| object | String | 是 | 送人对象关键词，如 `女友` |

**响应**: `Result<List<FlowerDetailVO>>`（字段见 5.6 节）

---

### 5.8 按用途场景查询鲜花明细

- **方法**: `GET`
- **路径**: `/admin/flower/of/option`
- **描述**: 按用途场景（specOption）模糊查询鲜花规格明细（`spec_option LIKE %option%`）
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| option | String | 是 | 用途场景关键词，如 `生日` |

**响应**: `Result<List<FlowerDetailVO>>`（字段见 5.6 节）

---

## 6. 鲜花明细管理 AdminFlowerDetailController

**基础路径**: `/admin/flowerDetail`

管理鲜花的规格明细（送人对象、用途场景），支持单条 CRUD，使用 Redis 缓存 + Redisson 分布式锁。

### 6.1 新增鲜花明细

- **方法**: `POST`
- **路径**: `/admin/flowerDetail`
- **描述**: 为鲜花添加一条规格明细
- **权限**: ROLE_ADMIN

**请求体 (FlowerDetailDTO, JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| flowerId | Long | 是 | 关联鲜花 ID |
| specObject | String | 否 | 送人对象（如女友） |
| specOption | String | 否 | 用途场景（如表白） |

**响应**: `Result<FlowerDetailDTO>`

---

### 6.2 根据 ID 查询鲜花明细

- **方法**: `GET`
- **路径**: `/admin/flowerDetail`
- **描述**: 查询单条鲜花明细详情（带缓存）
- **权限**: ROLE_USER / ROLE_EMP / ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 明细主键 ID |

**响应**: `Result<FlowerDetailVO>`（字段见 5.6 节）

---

### 6.3 更新鲜花明细

- **方法**: `PUT`
- **路径**: `/admin/flowerDetail`
- **描述**: 更新单条鲜花明细，并清除缓存
- **权限**: ROLE_ADMIN

**请求体 (FlowerDetailDTO, JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 主键 ID |
| flowerId | Long | 否 | 鲜花 ID |
| specObject | String | 否 | 送人对象 |
| specOption | String | 否 | 用途场景 |

**响应**: `Result<FlowerDetailDTO>`

---

### 6.4 批量删除鲜花明细

- **方法**: `DELETE`
- **路径**: `/admin/flowerDetail`
- **描述**: 根据 ID 列表批量删除明细
- **权限**: ROLE_ADMIN

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 明细 ID 列表 |

**响应**: `Result<List<Long>>`

---

## 7. 鲜花订单管理 AdminFlowerOrderController

**基础路径**: `/admin/flowerOrder`

管理鲜花订单的状态流转。订单状态枚举（OrderStatusEnum）如下：

| 状态码 | 枚举名 | 说明 |
|--------|--------|------|
| 1 | ORDER | 用户下单 |
| 2 | PAYMENT | 用户确认支付 |
| 3 | COOKING | 商家制作 |
| 4 | GO | 工作人员取货 |
| 5 | DELIVERING | 工作人员开始配送 |
| 6 | ARRIVED | 工作人员已到达 |
| 7 | COMPLETED | 系统自动确认完成 |
| 8 | CANCELLED | 已取消 |

### 7.1 根据 ID 查询订单

- **方法**: `GET`
- **路径**: `/admin/flowerOrder`
- **描述**: 查询单个订单详情
- **权限**: 需登录（ROLE_ADMIN / ROLE_EMP，Service 无方法级注解）

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**响应**: `Result<FlowerOrderVO>`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单主键 |
| status | OrderStatusEnum | 订单状态（枚举，序列化含 code/text） |
| userId | Long | 下单用户 ID |
| userName | String | 用户名称（冗余） |
| consignee | String | 收花人 |
| phone | String | 收花人手机号 |
| addressId | Long | 地址 ID |
| address | String | 完整配送地址（冗余） |
| remark | String | 备注（可存 AI 生成的贺卡文案） |
| deliveryType | DeliveryStatusEnum | 配送方式：0=预约配送，1=立即送出 |
| deliveryDate | LocalDate | 预约配送日期（yyyy-MM-dd） |
| startDeliveryTime | LocalDateTime | 配送开始时间 |
| estimatedDeliveryTime | LocalDateTime | 预计送达时间 |
| deliveryTime | LocalDateTime | 实际送达时间 |
| flowerOrderDetailList | List\<FlowerOrderDetail\> | 订单明细列表 |

订单明细 FlowerOrderDetail 字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 明细主键 |
| orderId | Long | 主订单 ID |
| name | String | 商品名称（冗余） |
| image | String | 商品图片（冗余） |
| flowerId | Long | 鲜花单品 ID（购买单品时赋值） |
| festivalId | Long | 节日多花 ID（购买礼盒时赋值） |
| number | Long | 购买数量，默认 1 |
| amount | BigDecimal | 单条明细金额 |
| wrapFee | Long | 多花礼盒包装费 |

---

### 7.2 分页查询订单列表

- **方法**: `GET`
- **路径**: `/admin/flowerOrder/all`
- **描述**: 分页查询订单列表，按订单状态筛选
- **权限**: 需登录（ROLE_ADMIN / ROLE_EMP，Service 无方法级注解）

**请求参数 (FlowerOrderPageDTO, Query 参数)**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Long | 否 | 1 | 页码（最小 1） |
| pageSize | Long | 否 | 10 | 每页条数（1~20） |
| status | Long | 否 | 无（null） | 订单状态（1~8）；不传时不加状态过滤，返回全部状态订单 |

**响应**: `Result<PageResult<FlowerOrderVO>>`（字段见 7.1 节）

---

### 7.3 订单状态流转 → 商家制作

- **方法**: `PUT`
- **路径**: `/admin/flowerOrder/cooking/{id}`
- **描述**: 将订单状态更新为"商家制作"（状态码 3）
- **权限**: 需登录（ROLE_ADMIN / ROLE_EMP）

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**响应**: `Result<OrderStatusEnum>` → 返回 COOKING 枚举

---

### 7.4 订单状态流转 → 工作人员取货

- **方法**: `PUT`
- **路径**: `/admin/flowerOrder/go/{id}`
- **描述**: 将订单状态更新为"工作人员取货"（状态码 4）
- **权限**: 需登录（ROLE_ADMIN / ROLE_EMP）

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**响应**: `Result<OrderStatusEnum>` → 返回 GO 枚举

---

### 7.5 订单状态流转 → 配送中

- **方法**: `PUT`
- **路径**: `/admin/flowerOrder/delivering/{id}`
- **描述**: 将订单状态更新为"配送中"（状态码 5）
- **权限**: 需登录（ROLE_ADMIN / ROLE_EMP）

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**响应**: `Result<OrderStatusEnum>` → 返回 DELIVERING 枚举

---

### 7.6 订单状态流转 → 已到达

- **方法**: `PUT`
- **路径**: `/admin/flowerOrder/arrived/{id}`
- **描述**: 将订单状态更新为"工作人员已到达"（状态码 6）
- **权限**: 需登录（ROLE_ADMIN / ROLE_EMP）

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**响应**: `Result<OrderStatusEnum>` → 返回 ARRIVED 枚举

---

### 7.7 订单状态流转 → 完成

- **方法**: `PUT`
- **路径**: `/admin/flowerOrder/complete/{id}`
- **描述**: 将订单状态更新为"已完成"（状态码 7）
- **权限**: 需登录（ROLE_ADMIN / ROLE_EMP）

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**响应**: `Result<OrderStatusEnum>` → 返回 COMPLETED 枚举

---

### 7.8 订单状态流转 → 已取消（含退款）

- **方法**: `PUT`
- **路径**: `/admin/flowerOrder/canceled/{id}`
- **描述**: 取消订单，同时触发支付宝退款
- **权限**: 需登录（ROLE_ADMIN / ROLE_EMP）

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**响应**: `Result<OrderStatusEnum>` → 返回 CANCELLED 枚举

---

## 8. 店铺状态管理 AdminShopController

**基础路径**: `/admin/shop`

管理店铺的营业状态，数据存储在 Redis 中（key 为常量 `店铺状态`）。

### 8.1 更新店铺营业状态

- **方法**: `POST`
- **路径**: `/admin/shop/{status}`
- **描述**: 设置店铺营业/打烊状态
- **权限**: 需登录（ROLE_ADMIN / ROLE_EMP，无 Service 层权限注解）

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Long | 是 | 取值 0 或 1（@Min(0) @Max(1)）：1=营业中，0=已打烊 |

**响应**: `Result<ShopVO>`

| 字段 | 类型 | 说明 |
|------|------|------|
| status | String | 店铺状态文案（"营业中" / "已打烊"） |

---

### 8.2 查询店铺营业状态

- **方法**: `GET`
- **路径**: `/admin/shop`
- **描述**: 获取当前店铺的营业状态
- **权限**: 需登录（ROLE_ADMIN / ROLE_EMP，无 Service 层权限注解）

**请求参数**: 无

**响应**: `Result<ShopVO>`

| 字段 | 类型 | 说明 |
|------|------|------|
| status | String | 店铺状态文案（"营业中" / "已打烊"） |

---

## 9. 营业数据统计 Statistics

**基础路径**: `/admin/statistics`

面向管理端首页看板的销量与订单统计接口，数据来源于订单明细表 flower_order_detail。

### 9.1 鲜花单品销量统计

- **方法**: `GET`
- **路径**: `/admin/statistics/flower`
- **描述**: 统计所有鲜花单品的累计销售数量与销售金额
- **权限**: ROLE_ADMIN / ROLE_EMP

**请求参数**: 无

**响应**: `Result<List<StatisticsVO>>`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 鲜花 ID |
| name | String | 鲜花名称 |
| count | Long | 累计销售数量 |
| totalAccount | BigDecimal | 累计销售金额 |

---

### 9.2 多花礼盒销量统计

- **方法**: `GET`
- **路径**: `/admin/statistics/festival`
- **描述**: 统计所有节日多花礼盒的累计销售数量与销售金额（按销量降序）
- **权限**: ROLE_ADMIN / ROLE_EMP

**请求参数**: 无

**响应**: `Result<List<StatisticsVO>>`（字段见 9.1 节）

---

### 9.3 鲜花热销榜 TopN

- **方法**: `GET`
- **路径**: `/admin/statistics/top1`
- **描述**: 鲜花单品销量排行榜（取前 TOP_NUMBER 条）
- **权限**: ROLE_ADMIN / ROLE_EMP

**请求参数**: 无

**响应**: `Result<List<TopStatisticsVO>>`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 鲜花 ID |
| name | String | 鲜花名称 |
| count | Long | 销售数量 |

---

### 9.4 多花礼盒热销榜 TopN

- **方法**: `GET`
- **路径**: `/admin/statistics/top2`
- **描述**: 节日多花礼盒销量排行榜
- **权限**: ROLE_ADMIN / ROLE_EMP

**请求参数**: 无

**响应**: `Result<List<TopStatisticsVO>>`（字段见 9.3 节）

---

### 9.5 订单状态分布统计

- **方法**: `GET`
- **路径**: `/admin/statistics/order`
- **描述**: 按订单状态分组统计各状态订单数量
- **权限**: ROLE_ADMIN / ROLE_EMP

**请求参数**: 无

**响应**: `Result<List<OrderStatisticsVO>>`

| 字段 | 类型 | 说明 |
|------|------|------|
| status | Long | 订单状态码（1~8） |
| name | String | 状态名称 |
| count | Long | 该状态订单数量 |

---

### 9.6 今日订单统计

- **方法**: `GET`
- **路径**: `/admin/statistics/today`
- **描述**: 今日订单数、已支付订单数、今日营业额
- **权限**: ROLE_ADMIN / ROLE_EMP

**请求参数**: 无

**响应**: `Result<List<OrderStatisticsVO>>`（字段见 9.5 节）

---

## 10. 文件上传下载

### 10.1 阿里云 OSS 上传 OSSFileController

- **方法**: `POST`
- **路径**: `/oss`
- **描述**: 上传文件到阿里云 OSS，返回可访问 URL（公共读）
- **权限**: 无（Security 配置 anyRequest 放行）
- **Content-Type**: `multipart/form-data`

**请求参数 (form-data)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 是 | 上传的文件 |

**响应**: **直接返回 ResponseEntity，不走 Result 包装**

- 成功（200）：

```json
{
    "url": "https://java-web-ai-wangnian.oss-cn-beijing.aliyuncs.com/xxxx.gif",
    "filename": "原始文件名.gif"
}
```

- 失败（400/500）：`{ "error": "错误信息" }`

---

### 10.2 OSS 文件下载

- **方法**: `GET`
- **路径**: `/oss`
- **描述**: 根据文件 URL 下载 OSS 上的文件（以附件形式返回二进制流）
- **权限**: 无

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| url | String | 是 | 文件的完整访问 URL |

**响应**: `200 OK`，响应体为文件字节流（Content-Type 为原文件类型）；失败返回 500。

---

### 10.3 本地文件上传 FileController

- **方法**: `POST`
- **路径**: `/local`
- **描述**: 上传文件到服务器本地目录（私有存储，目录 `ku/image`）
- **权限**: 无
- **Content-Type**: `multipart/form-data`

**请求参数 (form-data)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 是 | 上传的文件 |

**响应**: `Result<String>` → `"绝对路径::保存文件名"`

---

### 10.4 本地文件下载

- **方法**: `GET`
- **路径**: `/local`
- **描述**: 按文件名下载本地存储的文件
- **权限**: 无

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileName | String | 是 | 保存后的文件名 |

**响应**: 直接写入响应体的文件流（`application/octet-stream`），无 Result 包装。

---

## 11. Excel 报表 ExcelReportController

**基础路径**: `/report/excel`

基于 EasyExcel 的用户数据导出/导入，文件固定路径为服务器本地 `ku/excel/report.xlsx`。

### 11.1 导出用户数据到 Excel

- **方法**: `POST`
- **路径**: `/report/excel/write`
- **描述**: 查询全部用户并写入本地 Excel 文件（sheet 名：用户信息表）
- **权限**: 无

**请求参数**: 无

**响应**: `Result<String>` → 生成文件的绝对路径

UserExcel 列定义：

| 列序 | 表头 | 字段 | 说明 |
|------|------|------|------|
| 0 | 用户ID | id | Long |
| 1 | 姓名 | name | String |
| 2 | 电话 | phone | String |
| 3 | 邮箱 | email | String（表头第 3~4 列合并） |

---

### 11.2 读取 Excel 用户数据

- **方法**: `POST`
- **路径**: `/report/excel/read`
- **描述**: 读取本地 Excel 文件内容并返回
- **权限**: 无

**请求参数**: 无

**响应**: `Result<List<UserExcel>>`（字段见 11.1 节）

---

### 11.3 下载用户数据 Excel

- **方法**: `GET`
- **路径**: `/report/excel/download`
- **描述**: 下载导出的用户数据 Excel 文件
- **权限**: 无

**请求参数**: 无

**响应**: 成功为 xlsx 文件流；文件不存在时返回 `Result<String>`（code=500）。

