# User 用户端接口文档

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

收货地址滚动查询的 `data` 为 `ScrollResult<T>`：

```json
{
    "list": [],        // 数据列表
    "minTime": 123,    // 本次最后一条数据的 ID（下一次查询的游标）
    "offset": 10       // 下一次查询的偏移量
}
```

### 认证与权限

- **用户登录**：`POST /user/login`
- **请求携带**：请求头 `Authorization: Bearer <token>`
- **Token 存储**：Redis key 前缀 `flower:user:` + 用户 ID
- **权限控制**（Spring Security 配置）：
  - `/user/register`、`/user/login` 放行（permitAll）
  - 其余 `/user/**` 均需 `ROLE_USER` 角色
- 所有写操作接口均有 `@OperationLogging` 操作日志切面记录。

---

## 1. 用户账户 UserController

**基础路径**: `/user`

该控制器负责用户注册、登录、登出及资料修改。

### 1.1 用户注册

- **方法**: `POST`
- **路径**: `/user/register`
- **描述**: 创建新的用户账号
- **权限**: 无（放行）

**请求体 (UserDTO, JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名（唯一，已存在则抛注册异常） |
| password | String | 否 | 密码，不传时默认 123456（BCrypt 加密存储） |
| phone | String | 否 | 手机号 |
| sex | String | 否 | 性别 |
| idNumber | String | 否 | 身份证号 |
| avatar | String | 否 | 头像 URL |
| status | Long | 否 | 状态 0:禁用 1:启用 |

**响应**: `Result<String>` → `"register"`

---

### 1.2 用户登录

- **方法**: `POST`
- **路径**: `/user/login`
- **描述**: 用户账号密码登录
- **权限**: 无（放行）

**请求体 (LoginDTO, JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**响应**: `Result<String>` → 仅返回 `code=200`, data 为 token

---

### 1.3 用户登出

- **方法**: `POST`
- **路径**: `/user/logout`
- **描述**: 清除当前登录状态
- **权限**: ROLE_USER（ROLE_USER）

**请求参数**: 无

**响应**: `Result<String>` → `"logout"`

---

### 1.4 修改用户信息

- **方法**: `PUT`
- **路径**: `/user`
- **描述**: 根据 ID 更新用户资料
- **权限**: ROLE_USER

**请求体 (UserDTO, JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户主键 ID |
| username | String | 否 | 用户名 |
| password | String | 否 | 密码 |
| phone | String | 否 | 手机号 |
| sex | String | 否 | 性别 |
| idNumber | String | 否 | 身份证号 |
| avatar | String | 否 | 头像 |
| status | Long | 否 | 状态 |

**响应**: `Result<Long>` → 返回传入的用户 ID

---

## 2. 收货地址 UserAddressController

**基础路径**: `/user/address`

管理用户的收花地址簿（对应 user_address 表），所有地址均按当前登录用户 ID 隔离。

### 2.1 新增收货地址

- **方法**: `POST`
- **路径**: `/user/address`
- **描述**: 添加一条收货地址；若设为默认地址，会自动将原默认地址改为非默认
- **权限**: ROLE_USER

**请求参数 (UserAddressDTO,  JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| consignee | String | 是 | 收花人姓名 |
| phone | String | 是 | 手机号 |
| sex | String | 否 | 收花人性别 |
| provinceCode | String | 否 | 省级区划编号 |
| provinceName | String | 否 | 省级名称 |
| cityCode | String | 否 | 市级区划编号 |
| cityName | String | 否 | 市级名称 |
| districtCode | String | 否 | 区级区划编号 |
| districtName | String | 否 | 区级名称 |
| detail | String | 否 | 详细地址 |
| label | String | 否 | 标签（如家、公司、学校） |
| isDefault | Long | 否 | 是否默认：0=否，1=是 |

**响应**: `Result<UserAddressDTO>` → 带 id 的地址 DTO

---

### 2.2 查询默认收货地址

- **方法**: `GET`
- **路径**: `/user/address/default`
- **描述**: 查询当前用户的默认收货地址
- **权限**: ROLE_USER

**请求参数**: 无

**响应**: `Result<UserAddress>`（字段见 2.1 节请求参数，另含 `id`、`userId`）

---

### 2.3 滚动查询全部地址

- **方法**: `GET`
- **路径**: `/user/address/all`
- **描述**: 滚动分页查询当前用户的全部收货地址
- **权限**: ROLE_USER

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| offset | Long | 否 | 固定默认条数 | 本次查询的条数上限（limit） |
| current | Long | 否 | - | 当前游标（预留参数） |

**响应**: `Result<ScrollResult<UserAddress>>`

| 字段 | 类型 | 说明 |
|------|------|------|
| list | List\<UserAddress\> | 地址列表（字段见 2.1 节） |
| minTime | Long | 本次最后一条地址 ID（下次查询游标） |
| offset | Long | 下次查询偏移量 |

---

### 2.4 设置默认地址

- **方法**: `PUT`
- **路径**: `/user/address/default/{id}`
- **描述**: 将指定地址设为默认地址（原默认地址自动取消）
- **权限**: ROLE_USER

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 地址主键 ID |

**响应**: `Result<Void>` → `code=200`

---

### 2.5 修改收货地址

- **方法**: `PUT`
- **路径**: `/user/address`
- **描述**: 根据 ID 修改地址（部分字段更新）；若 isDefault=1 会同步切换默认地址
- **权限**: ROLE_USER

**请求参数 (UserAddressDTO,  JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 地址主键 ID（不传抛操作异常） |
| consignee | String | 否 | 收花人姓名 |
| phone | String | 否 | 手机号 |
| sex | String | 否 | 性别 |
| provinceCode / provinceName | String | 否 | 省级区划编号 / 名称 |
| cityCode / cityName | String | 否 | 市级区划编号 / 名称 |
| districtCode / districtName | String | 否 | 区级区划编号 / 名称 |
| detail | String | 否 | 详细地址 |
| label | String | 否 | 标签 |
| isDefault | Long | 否 | 是否默认：0=否，1=是 |

**响应**: `Result<Void>` → `code=200`

---

### 2.6 批量删除收货地址

- **方法**: `DELETE`
- **路径**: `/user/address`
- **描述**: 根据 ID 列表批量删除地址
- **权限**: ROLE_USER

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 地址 ID 列表，如 `ids=1&ids=2`（为空抛操作异常） |

**响应**: `Result<Void>` → `code=200`

---

## 3. 购物车 UserShoppingController

**基础路径**: `/user/shopping`

购物车数据存储在 Redis Hash 中（key 为 `shopping:cart` + 用户 ID），不落数据库。

### 3.1 加入购物车

- **方法**: `POST`
- **路径**: `/user/shopping`
- **描述**: 将一个商品（鲜花单品或多花礼盒）加入购物车
- **权限**: ROLE_USER
- **Content-Type**: `application/json`

**请求体 (UserShoppingDTO, JSON)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 购物车记录 ID（作为 Redis Hash 的 field） |
| name | String | 否 | 商品名称 |
| image | String | 否 | 商品图片 |
| flowerId | Long | 否 | 鲜花单品 ID（买单品时赋值） |
| festivalId | Long | 否 | 节日多花 ID（买礼盒时赋值） |
| number | Long | 是 | 选购数量，最小 1（@Min(1)） |
| amount | BigDecimal | 否 | 小计金额 |
| createTime | LocalDateTime | 否 | 加入时间（格式 yyyy-MM-dd HH:mm:ss，通常由服务端处理） |

**响应**: `Result<UserShoppingDTO>` → 回显传入的 DTO

---

### 3.2 查询购物车

- **方法**: `GET`
- **路径**: `/user/shopping`
- **描述**: 查询当前用户购物车全部商品
- **权限**: ROLE_USER

**请求参数**: 无

**响应**: `Result<List<UserShoppingVO>>`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 购物车记录 ID |
| name | String | 商品名称 |
| image | String | 商品图片 |
| flowerId | Long | 鲜花单品 ID |
| festivalId | Long | 节日多花 ID |
| number | Long | 选购数量 |
| amount | BigDecimal | 小计金额 |
| createTime | LocalDateTime | 加入购物车时间（yyyy-MM-dd HH:mm:ss） |

---

### 3.3 删除购物车单个商品

- **方法**: `DELETE`
- **路径**: `/user/shopping`
- **描述**: 根据记录 ID 删除购物车中的一个商品
- **权限**: ROLE_USER

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 购物车记录 ID |

**响应**: `Result<Long>` → 被删除的记录 ID

---

### 3.4 清空购物车

- **方法**: `DELETE`
- **路径**: `/user/shopping/all`
- **描述**: 清空当前用户的整个购物车
- **权限**: ROLE_USER

**请求参数**: 无

**响应**: `Result<Void>` → `code=200`

---

## 4. 鲜花分类 FlowerCategoryController

**基础路径**: `/user/category`

用户端分类浏览接口（只读），数据与管理端共用，带 Spring Cache 缓存。

### 4.1 根据类型查询分类列表

- **方法**: `GET`
- **路径**: `/user/category`
- **描述**: 按分类类型查询所有启用分类
- **权限**: ROLE_USER

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | Long | 是 | 分类类型：1=鲜花商品单只，2=节日商品多只，3=礼品 |

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

### 4.2 分页查询分类列表

- **方法**: `GET`
- **路径**: `/user/category/all`
- **描述**: 分页查询分类，支持按类型筛选
- **权限**: ROLE_USER

**请求参数 (FlowerCategoryPageDTO, Query)**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Long | 否 | 1 | 页码（最小 1） |
| pageSize | Long | 否 | 10 | 每页条数（1~20） |
| type | Long | 否 | - | 分类类型筛选 |

**响应**: `Result<PageResult<FlowerCategoryVO>>`（字段见 4.1 节）

---

### 4.3 查询分类下的鲜花

- **方法**: `GET`
- **路径**: `/user/category/of/flower`
- **描述**: 根据分类 ID 查询其下所有鲜花单品
- **权限**: ROLE_USER

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 分类主键 ID |

**响应**: `Result<List<FlowerVO>>`（字段见 5.1 节）

---

### 4.4 查询分类下的多花礼盒

- **方法**: `GET`
- **路径**: `/user/category/of/festival`
- **描述**: 根据分类 ID 查询其下所有节日多花礼盒
- **权限**: ROLE_USER

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 分类主键 ID |

**响应**: `Result<List<FestivalVO>>`（字段见 6.1 节）

---

## 5. 鲜花单品 FlowerController

**基础路径**: `/user/flower`

用户端鲜花单品浏览接口（只读），带 Redis 缓存 + Redisson 分布式锁 + 逻辑过期。

### 5.1 根据 ID 查询鲜花

- **方法**: `GET`
- **路径**: `/user/flower`
- **描述**: 查询单个鲜花详情（带缓存）
- **权限**: ROLE_USER

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

### 5.2 分页查询鲜花列表

- **方法**: `GET`
- **路径**: `/user/flower/all`
- **描述**: 分页查询鲜花列表，支持按名称模糊搜索
- **权限**: ROLE_USER

**请求参数 (FlowerPageDTO, Query)**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Long | 否 | 1 | 页码（最小 1） |
| pageSize | Long | 否 | 10 | 每页条数（1~20） |
| name | String | 否 | - | 鲜花名称模糊搜索 |

**响应**: `Result<PageResult<FlowerVO>>`（字段见 5.1 节）

---

### 5.3 查询鲜花的规格明细

- **方法**: `GET`
- **路径**: `/user/flower/of/flowerDetail`
- **描述**: 根据鲜花 ID 查询其所有规格明细（送人对象/用途场景）
- **权限**: ROLE_USER

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

## 6. 节日多花 FestivalController

**基础路径**: `/user/festival`

用户端节日多花礼盒浏览接口（只读），缓存策略与鲜花一致。

### 6.1 根据 ID 查询多花礼盒

- **方法**: `GET`
- **路径**: `/user/festival`
- **描述**: 查询单个多花礼盒详情（带缓存）
- **权限**: ROLE_USER

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
| number | Long | 鲜花总数量 |
| status | Long | 售卖状态 0:下架 1:在售 |
| statusDesc | String | 售卖状态描述 |
| description | String | 描述 |
| image | String | 图片 URL |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

---

### 6.2 分页查询多花礼盒列表

- **方法**: `GET`
- **路径**: `/user/festival/all`
- **描述**: 分页查询多花礼盒列表，支持按名称模糊搜索
- **权限**: ROLE_USER

**请求参数 (FestivalPageDTO, Query)**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Long | 否 | 1 | 页码（最小 1） |
| pageSize | Long | 否 | 10 | 每页条数（1~20） |
| name | String | 否 | - | 礼盒名称模糊搜索 |

**响应**: `Result<PageResult<FestivalVO>>`（字段见 6.1 节）

---

### 6.3 查询礼盒下的明细列表

- **方法**: `GET`
- **路径**: `/user/festival/of/festivalDetail`
- **描述**: 根据多花礼盒 ID 查询其包含的所有明细（关联鲜花）
- **权限**: ROLE_USER

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
| specObject | String | 送人对象（如女友、母亲） |
| specOption | String | 用途场景（如表白、生日） |

---

### 6.4 查询某鲜花被哪些礼盒包含

- **方法**: `GET`
- **路径**: `/user/festival/of/flower`
- **描述**: 根据鲜花 ID 查询它被哪些多花礼盒关联
- **权限**: ROLE_USER

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 鲜花主键 ID |

**响应**: `Result<List<FestivalDetailVO>>`（字段见 6.3 节）

---

## 7. 鲜花明细 FlowerDetailController

**基础路径**: `/user/flowerDetail`

### 7.1 根据 ID 查询鲜花明细

- **方法**: `GET`
- **路径**: `/user/flowerDetail`
- **描述**: 查询单条鲜花规格明细详情（带缓存）
- **权限**: ROLE_USER

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 明细主键 ID |

**响应**: `Result<FlowerDetailVO>`（字段见 5.3 节）

---

## 8. 多花明细 FestivalDetailController

**基础路径**: `/user/festivalDetail`

### 8.1 根据 ID 查询多花明细

- **方法**: `GET`
- **路径**: `/user/festivalDetail`
- **描述**: 查询单条多花礼盒明细详情（带缓存）
- **权限**: ROLE_USER

**请求参数 (Query)**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 明细主键 ID |

**响应**: `Result<FestivalDetailVO>`（字段见 6.3 节）

---

## 9. 鲜花订单 OrderController

**基础路径**: `/user/order`

用户侧订单查询与状态流转。订单状态枚举（OrderStatusEnum）如下：

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

配送方式枚举（DeliveryStatusEnum）：`0`=预约配送（BOOK_TIME），`1`=立即送出（NOW）。

### 9.1 根据 ID 查询订单

- **方法**: `GET`
- **路径**: `/user/order`
- **描述**: 查询单个订单详情
- **权限**: ROLE_USER

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

### 9.2 分页查询历史订单

- **方法**: `GET`
- **路径**: `/user/order/history`
- **描述**: 按状态分页查询订单列表
- **权限**: ROLE_USER

**请求参数 (FlowerOrderPageDTO, Query)**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Long | 否 | 1 | 页码（最小 1） |
| pageSize | Long | 否 | 10 | 每页条数（1~20） |
| status | Long | 否 | 3 | 订单状态（1~8） |

**响应**: `Result<PageResult<FlowerOrderVO>>`（字段见 9.1 节）

---

### 9.3 用户下单（订单状态流转）

- **方法**: `POST`
- **路径**: `/user/order/order/{id}`
- **描述**: 用户对订单执行下单确认操作
- **权限**: ROLE_USER

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**响应**: `Result<Void>` → `code=200`

---

### 9.4 确认支付（订单状态流转）

- **方法**: `PUT`
- **路径**: `/user/order/payment/{id}`
- **描述**: 用户确认支付订单
- **权限**: ROLE_USER

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**响应**: `Result<Void>` → `code=200`

---

### 9.5 确认收货（订单状态流转）

- **方法**: `PUT`
- **路径**: `/user/order/complete/{id}`
- **描述**: 用户确认收货，订单完成
- **权限**: ROLE_USER

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

**响应**: `Result<Void>` → `code=200`

---

### 9.6 取消订单（含退款）

- **方法**: `PUT`
- **路径**: `/user/order/canceled/{id}`
- **描述**: 用户取消订单，同时触发支付宝退款
- **权限**: ROLE_USER

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单主键 ID |

1. 查询订单及订单明细
2. 构建 RefundDTO：outTradeNo、outRefundNo 均为订单 ID，退款金额取自订单明细
3. 调用 ZhifubaoService 执行退款（失败抛 RuntimeException）
4. 更新支付表 pay_status 为 REFUNDED
5. 更新订单 status 为 CANCELLED（8）

**响应**: `Result<Void>` → `code=200`

---

## 10. 店铺状态 ShopController

**基础路径**: `/user/shop`

### 10.1 查询店铺营业状态

- **方法**: `GET`
- **路径**: `/user/shop`
- **描述**: 获取当前店铺营业状态（用户下单前展示）
- **权限**: ROLE_USER

**请求参数**: 无

**响应**: `Result<ShopVO>`

| 字段 | 类型 | 说明 |
|------|------|------|
| status | String | 店铺状态文案（"营业中" / "已打烊"） |

