<div align="center">
  <h1>flowerpayment-ai 鲜花商店 + ai</h1>
  <h2>flowerpayment-ai：B2C 经营模式，一个花店卖家，多个买家。鲜花服务由店长、店员和客户组成。</h2>
  <h4>
    一个由 Spring Boot 3 + Vue 3 的前后端分离架构，中间件使用 Redis + nginx，主业务为鲜花礼品订单和支付的全栈系统和
    Spring AI（spring-ai-starter-model-openai + 阿里云），通过图像识别花材推荐相似花束，并支持 LLM 生成个性化贺卡文案。
  </h4>
</div>

<div align="center">
    <h1>
    <img src="https://img.shields.io/badge/Java-17+ -6DB33F?style=flat-square&logo=java&logoColor=white" alt="Java" />
    <img src="https://img.shields.io/badge/Spring%20Boot-3.+ -6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/MySQL-8.0+ -6DB33F?style=flat-square&logo=mysql&logoColor=white" alt="MySQL" />
    <img src="https://img.shields.io/badge/Redis-7.0+ -6DB33F?style=flat-square&logo=redis&logoColor=white" alt="Redis" />
    <img src="https://img.shields.io/badge/Spring%20AI-1.1.+ -6DB33F?style=flat-square&logo=spring&logoColor=white" alt="Spring AI" />
    <img src="https://img.shields.io/badge/Node.js-20.+-6DB33F?style=flat-square&logo=nodedotjs&logoColor=white" alt="Node.js" />
    <img src="https://img.shields.io/badge/Vue3-组合式API-6DB33F?style=flat-square&logo=vuedotjs&logoColor=white" alt="Vue3" />
    </h1>
</div>
------

# 架构图

| 数据流向     | <img src="说明/resource/design1.png" alt="架构" style="zoom: 50%;" /> |
| ------------ | ------------------------------------------------------------ |
| 总体设计     | <img src="说明/resource/design2.png" alt="架构" style="zoom:25%;" /> |
| 项目结构     | flower/<br/>├── spring-flower/            			  # 后端代码（Spring Boot 3 多模块）<br/>│   ├── common/                           # 公共模块<br/>│   ├── model/                            # 实体与数据传输对象<br/>│   ├── mapper/                           # 数据访问层（MyBatis-Plus）<br/>│   ├── service/                          # 业务逻辑模块<br/>│   ├── start/                            # 主业务启动模块<br/>│   └── ai/                           	  # AI服务启动模块<br/>│<br/>├── vue-flower/        					  # 前端管理端（Vue 3）<br/>│   ├── src/<br/>│   │   ├── api/                          # API接口封装（axios）<br/>│   │   ├── views/                        # 页面视图<br/>│   │   ├── layout/                       # 布局组件<br/>│   │   ├── router/                       # 路由配置(router)<br/>│   │   ├── stores/                       # 状态管理（Pinia）<br/>│   │   └── utils/                        # 工具函数<br/>│   └── package.json<br/>│<br/>├── database-sql/                  # 数据库脚本目录<br/>│   ├── sql.txt                    # 数据库create table<br/>│   ├── sql插入数据.txt              # 数据库初始化SQL<br/>│   └── 数据库设计文档.md             # 数据库设计说明<br/>│<br/>└── 说明/                          # 项目说明文档<br/>    ├── 原型功能/                   # 前端原型截图<br/>    ├── resource/                  # md文件使用<br/>    ├── 支付宝+qq/                  # 第三方应用注册和支付<br/>    ├── 并发测试                    # 使用jmeter测试<br/>    		├── flowr-category     使用springcache数据日志+结果<br/>    		├── flowr			   使用redis数据日志+结果<br/>    		├── festival		   使用redis数据日志+结果<br/>    ├── 运行日志.txt                # 运行日志<br/>    ├── admin接口文档.md            #admin接口详情<br/>    └── user接口文档.md             #user接口详情 |
| 启动步骤     | 1创建数据库并导入 `sql/` 目录脚本。 <br>2 修改 `start/src/main/resources/application-dev.yml` 中数据库与 Redis 配置。<br>3 `npm run dev ` 前端启动服务。 |
| **升级方案** | 使用nacos+gateway连接主业务+ai业务(两个服务)，灰度更新，分布式部署，故障转移等等。待后续开发。<br>当前使用mysql存储ai会话内容，可以改成redis的IO密集型，性能更强。待后续开发。 |

# 前端说明

技术栈：Vue 3 + Element Plus + Pinia + Vue Router + echarts

## 管理端界面

| 功能页面  |                             截图                             |
| :-------: | :----------------------------------------------------------: |
| 登录页面  | <img src="说明/原型功能/admin1.png" alt="管理端登录" style="zoom: 25%;" /> |
|   分类    |                                                              |
| 单花销售  |                                                              |
| 组合销售  |                                                              |
| 订单管理  |                                                              |
|   店铺    |                                                              |
|   员工    |                                                              |
| 业务大屏1 |                                                              |
| 业务大屏2 |                                                              |
| 业务大屏3 |                                                              |

## 用户端界面

|     功能页面     | 截图 |
| :--------------: | ---- |
|     登录页面     |      |
|       分类       |      |
|     单花销售     |      |
|     组合销售     |      |
|       店铺       |      |
|      购物车      |      |
|       订单       |      |
| AI识别（多模态） |      |



# 后端说明

## 一、店长、店员和客户多端端登录认证模块

## **第三方授权登录流程图和支付流程**：支付宝

|                          支付宝授权                          |                           授权成功                           | 集成到订单                                                   | 支付过程                                                     |                           同步支付                           |                           异步检验                           |
| :----------------------------------------------------------: | :----------------------------------------------------------: | ------------------------------------------------------------ | ------------------------------------------------------------ | :----------------------------------------------------------: | :----------------------------------------------------------: |
| <img src="说明/支付宝+qq/ali1.png" alt="支付宝" style="zoom:25%;" /> | <img src="说明/支付宝+qq/ali2.png" alt="支付宝" style="zoom:50%;" /> | <img src="说明/支付宝+qq/1.png" alt="支付" style="zoom:25%;" /> | <img src="说明/支付宝+qq/2.png" alt="支付" style="zoom: 25%;" /> | <img src="说明/支付宝+qq/3.png" alt="支付" style="zoom: 25%;" /> | <img src="说明/支付宝+qq/4.png" alt="支付" style="zoom: 25%;" /> |

## **第三方授权登录流程图和支付流程**：qq

| 待完善 |      |      |      |      |
| ------ | ---- | ---- | ---- | ---- |

### 迭代过程

1. 早期版本：单过滤器 if-else 区分账号类型，新增 QQ / 支付宝第三方登录后逻辑爆炸；优化为过滤器链接力模式，解耦多端登录逻辑。
2. 早期只校验 JWT 签名，支持伪造永久 Token；新增 Redis 缓存校验，实现登录状态后端可控。
3. 最初使用固定 TTL，活跃用户频繁掉线；改造为滑动过期，留存提升明显。

```
Q：为什么不用一个过滤器统一解析两种 Token？

答：单过滤器会出现大量类型判断分支，后续扩展第三方登录、多角色账号时维护成本极高；拆分过滤器采用接力放行模式，每个过滤器只关心自己对应的账号类型，符合开闭原则。

Q：滑动过期会不会产生大量无效 Redis Key？

答：设置最大基础 TTL 兜底，即使用户长期不操作，缓存自动(expire:24小时)淘汰；登出接口主动删除对应 key，减少无效缓存堆积。

Q: BCrypt 密码加密存储优点？

不使用 MD5/SHA256 不可逆哈希，BCrypt 自带随机盐值，抗彩虹表暴力破解，数据库永不存储明文密码。
Q: 如何用户权限隔离？
 1.	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
 2.	@Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("EMP")
                .role("ADMIN").implies("USER")
                .role("EMP").implies("USER")
                .build();
    }

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }
   3. 	// 只拦截,对于测试使用的pay+auth+role，不拦截
.requestMatchers("/admin/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMP")
.requestMatchers("/user/**").hasAuthority("ROLE_USER")


```

------

## 二、flower-category模块

### model

1. 数据表 

   ```
   flower_category
   ```

   字段：id、分类名称、type 、排序、状态、删除标记；

   索引：type 普通索引，按类型快速筛选分类。

3. Redis 缓存结构，flower-category的并发量小，选择spring-cache缓存

   ```
   @CacheConfig(cacheNames = RedisPrefixConstant.CATEGORY_TYPE_PREFIX)
   ```

   key：type（1/2），value：全量分类列表 JSON；

   修改分类直接清空整个命名空间，规避无法枚举所有关联 key 的问题。

### 迭代过程
排除冷启动的（第一次，第1000次)达到稳定，进行统计
| jmeter每次1000次                                             | <img src="说明/并发测试/flower-category.png" style="zoom: 25%;" /> |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| 没有缓存，并发1000次                                         | ![](说明/并发测试/flower-category-运行日志-没有缓存1.png)    |
| 没有缓存，并发1000次                                         | ![](说明/并发测试/flower-category-运行日志-没有缓存2.png)    |
| 没有缓存，并发1000次                                         | ![](说明/并发测试/flower-category-运行日志-没有缓存3.png)    |
| 无缓存的情况是全程没有使用 redis 的稳定情况                  | 说明/并发测试/flower-category-运行日志-没有缓存日志.txt      |
| 有spring-cache缓存，再并发1000次                             | ![](说明/并发测试/flower-category-运行日志-缓存1.png)        |
| 有spring-cache缓存，再并发1000次                             | ![](说明/并发测试/flower-category-运行日志-缓存2.png)        |
| 有spring-cache缓存，再并发1000次                             | ![](说明/并发测试/flower-category-运行日志-缓存3.png)        |
| 有缓存的情况是全程有 redis 的稳定情况                        | 说明/并发测试/flower-category-运行日志-缓存日志.txt          |
| 计算说明：性能提升百分比 =(无缓存值‑有缓存值)/ 无缓存值 ×100%；吞吐量提升百分比 =(有缓存‑无缓存)/ 无缓存 ×100%。 | **响应时间**：开启缓存后接口全量响应指标得到极大优化。无缓存场景平均响应时间 600ms，开启缓存后平均响应仅 10ms，平均响应性能提升**98.3%**。90%、95%、99% 分位耗时下降尤为明显；无缓存场景高百分位接近 1800ms，开启缓存 99 分位仅 98ms。无缓存时需要完整执行业务逻辑、访问数据库；命中缓存直接读取缓存数据，极大降低接口处理时延。 **吞吐量**：无缓存吞吐量 21.8 请求 / 秒；开启缓存吞吐量提升至 29.7 请求 / 秒，吞吐量提升**36.2%**，系统整体并发处理能力增强。 **网络流量**：接收速率从 43.46KB/sec 提升至 59.09KB/sec，发送速率从 7.93KB/sec 提升至 10.78KB/sec，单位时间网络数据处理能力随吞吐量同步上涨。 |

## 三、flower模块，festival模块

1. 鲜花单品模块

- 分类：鲜花单品 = 1 : N（`flower.category_id`关联分类表）
- 鲜花单品：鲜花规格`flower_detail` = 1 : N

> - spec_object：送人对象（恋人 / 朋友 / 长辈）
> - spec_options：用途（表白、生日、道歉）

2. 节日礼盒模块

- `festival`节日礼盒主表，代表一个成品礼盒商品（比如 “520 热恋礼盒”）

- `festival_detail`礼盒明细：一个礼盒包含多朵鲜花，一条明细代表礼盒里面的一款鲜花，同时携带该鲜花的送人对象、用途规格。

- 关系：`festival` : `festival_detail` = 1 : N

  >- spec_object：送人对象（恋人 / 朋友 / 长辈）
  >- spec_options：用途（表白、生日、道歉）

3. 索引

index idx_festival_id (festival_id)、index idx_flower_id (flower_id).

 1 大幅提升数据检索速度（避免全表扫描） 

 2 优化 ORDER BY 和 GROUP BY 操作

---

**缓存设计：因为flower模块 festival模块的价格存在因为节日，花的保质期限制不能直接返回旧数据，异步更新后返回新数据**



```mermaid
%%{init: {'theme':'neutral','themeVariables':{'fontSize':'8px','nodeBorder':'2px'},'flowchart':{'nodeSpacing':8,'rankSpacing':32,'useMaxWidth':false,'curve':'basis'}}}%%
flowchart TD
    S([开始]) --> A[从 Redis 读取缓存]
    A -->|Redis 宕机 / value 为空| B[加redisson + 查 DB + 写缓存]
    A -->|命中| C[解析 LogicData]
    C -->|解析失败 / data 为空| B
    C -->|成功| D{逻辑时间未过期?}
    D -->|是 未过期| E[剩余 TTL 不足则延长<br/>返回缓存数据]
    D -->|否 已过期| F[异步线程池重建缓存<br/>加redisson + 查 DB + 写缓存<br/>返回缓存数据]
    B --> R([返回结果])
    E --> R
    F --> R
```



**DB查询**

```mermaid
%%{init: {'theme':'neutral','themeVariables':{'fontSize':'8px','nodeBorder':'2px'},'flowchart':{'nodeSpacing':8,'rankSpacing':32,'useMaxWidth':false,'curve':'basis'}}}%%
flowchart TD
    S([getMysql 开始]) --> A[super.getById id 查数据库]
    A --> B{flower 是否为 null?}
    B -->|是 缓存穿透| C[构造空 LogicData data=null 设逻辑过期时间]
    B -->|否 有数据| D[构造 LogicData data=flower 设逻辑过期时间]
    C --> E[SET Redis 空值缓存 TTL=REDIS_EXIST_TTL]
    D --> F[SET Redis 正常缓存 TTL=REDIS_EXIST_TTL]
    E --> G([返回 null])
    F --> H([返回 flower])
```



### 迭代过程

|           早期问题           |                           我的方案                           |
| :--------------------------: | :----------------------------------------------------------: |
|           缓存穿透           |          空值缓存 + 短 TTL60s，查询为空也写入 Redis          |
|           缓存击穿           | 逻辑过期 + Redisson 分布式锁 + 异步重建；持锁线程异步更新，其余请求返回旧缓存 |
|           缓存雪崩           | 基础 TTL 叠加 ±10% 随机偏移；列表、详情缓存设置不同过期周期  |
|       缓存数据库一致性       |                       兜底物理 TTL24h                        |
| 悲观锁不能解决集群和并发问题 | Redisson 可重入分布式锁 + 看门狗自动续期；加锁后双重检查缓存 |
|    redis宕机的突发性问题     | Redis 不可用：降级直查数据库<br> log.info("Redis 宕机:{}", e.getMessage()); Flower flower = this.getMysql(id); return BeanUtil.toBean(flower, FlowerVO.class); |

排除冷启动的（第一次，第100次)达到稳定，进行统计

| flower                       | jmeter的100次并发                               |
| ---------------------------- | ----------------------------------------------- |
| 没有缓存，再开启100次并发    | ![](说明/并发测试/flower-运行日志-没有缓存.png) |
| 没有缓存                     | 说明/并发测试/flower-运行日志-没有缓存.txt      |
| 有redis缓存，再开启100次并发 | ![](说明/并发测试/flower-运行日志-缓存.png)     |
| 有redis缓存                  | 说明/并发测试/flower-运行日志-缓存.txt          |

| festival                     | jmeter的100次并发                                            |
| ---------------------------- | ------------------------------------------------------------ |
| 没有缓存，再开启100次并发    | ![img](file://D:/a.github/flowerpayment-ai/%E8%AF%B4%E6%98%8E/%E5%B9%B6%E5%8F%91%E6%B5%8B%E8%AF%95/flower-%E8%BF%90%E8%A1%8C%E6%97%A5%E5%BF%97-%E6%B2%A1%E6%9C%89%E7%BC%93%E5%AD%98.png?lastModify=1787466751) |
| 没有缓存                     | 说明/并发测试/flower-运行日志-没有缓存.txt                   |
| 有redis缓存，再开启100次并发 | ![img](file://D:/a.github/flowerpayment-ai/%E8%AF%B4%E6%98%8E/%E5%B9%B6%E5%8F%91%E6%B5%8B%E8%AF%95/flower-%E8%BF%90%E8%A1%8C%E6%97%A5%E5%BF%97-%E7%BC%93%E5%AD%98.png?lastModify=1787466751) |
| 有redis缓存                  | 说明/并发测试/flower-运行日志-缓存.txt                       |

------

##  四、flower-detial模块，festival-detail模块

**缓存设计**

```mermaid
%%{init: {'theme':'neutral','themeVariables':{'fontSize':'8px','nodeBorder':'2px'},'flowchart':{'nodeSpacing':8,'rankSpacing':32,'useMaxWidth':false,'curve':'basis'}}}%%
flowchart TD
    S([查询]) --> A[直接查询Redis]
    A -->|redis宕机| B[降级查 DB]
    A -->|正常| C{value为空?}
    C -->|是| D[加锁查 DB + 写缓存]
    C -->|否| E[解析 LogicData]
    D --> R[返回结果]
    E -->|json解析失败?| D
    E -->|成功| F{data 为空?}
    F -->|返回null,DB防穿透| R
    F -->|否| G[保存旧数据 old]
    G --> H{已逻辑过期?}
    H -->|否| I[返回缓存数据]
    H -->|是| J[异步提交重建]
    J --> K[立即返回 old]
    J --> L[后台线程查 DB 写缓存]
```

**DB查询**

```mermaid
%%{init: {'theme':'neutral','themeVariables':{'fontSize':'8px','nodeBorder':'2px'},'flowchart':{'nodeSpacing':8,'rankSpacing':32,'useMaxWidth':false,'curve':'basis'}}}%%
flowchart TD
    S([getMysql 开始]) --> A[super.getById id 查数据库]
    A --> B{flower 是否为 null?}
    B -->|是 缓存穿透| C[构造空 LogicData data=null 设逻辑过期时间]
    B -->|否 有数据| D[构造 LogicData data=flower 设逻辑过期时间]
    C --> E[SET Redis 空值缓存 TTL=REDIS_EXIST_TTL]
    D --> F[SET Redis 正常缓存 TTL=REDIS_EXIST_TTL]
    E --> G([返回 null])
    F --> H([返回 flower])
```



## 五、订单状态流转

```
1 待支付 → 2 已付款 → 3 制作中 → 4 骑手待出发 → 5 配送中 → 6 已送达 → 7 已完成 
        → 8 已取消（未接单退款、商家拒单、超时取消、售后全额退款）
```

## 六、user模块

### user-address

|        业务难点        |                         场景                          |                           解决方案                           |                           选型理由                           |
| :--------------------: | :---------------------------------------------------: | :----------------------------------------------------------: | :----------------------------------------------------------: |
|   多默认地址数据违规   |       新增 / 修改地址勾选默认，旧默认地址未取消       | 设为默认前先批量更新该用户所有地址 isDefault=0，两步操作绑定业务逻辑 | 数据库无法直接约束单用户唯一默认，代码层前置清理旧默认，保证业务数据合规 |
| 传统分页深分页性能衰减 | 用户地址数量较多时，pageNum=100 需要扫描前 100 页数据 |  游标滚动分页，以上一页最后一条 id 作为游标，直接走主键索引  | 游标分页性能稳定不随页码增长衰减，统一项目分页返回结构 ScrollResult |

### user-shopping

```
Q:放弃 MySQL 持久化，采用 Redis Hash 存储?
购物车是临时会话数据，用户退出、下单完成即可清空，不需要持久化；Redis 读写 O (1)，支撑高并发增减购物车操作，集群多实例数据共享。
Q:Redis Hash 结构
   外层 key：shopping_cart:{userId}
   内层 field：购物项唯一 id，value：商品完整信息 JSON（菜品 / 套餐 id、名称、数量、口味、金额）
   优势：单用户购物车聚合存储，增删单项无需操作整条数据，性能优于 String 序列化列表。。

```

### shop店铺

仅两个状态值，高频读写、无需持久化报表，存入 Redis 读写 O (1)；多实例共享同一缓存，状态实时同步，无需事务、数据表，轻量化实现。

|       业务难点       |                     场景                     |                解决方案                 |                    选型理由                    |
| :------------------: | :------------------------------------------: | :-------------------------------------: | :--------------------------------------------: |
| 高频查询店铺营业状态 | 每个用户进店、下单前都校验状态，并发访问频繁 | Redis 单 key 存储状态，查询无数据库 IO  |  相比 MySQL 查询延迟大幅降低，减轻数据库压力   |
|    集群状态不同步    |      单实例内存变量存储，多节点状态独立      | 统一 Redis 集中存储店铺状态，全实例共享 | 分布式环境全局状态标准存储方案，一致性实时保障 |

## 七、文件管理，数据分析

1 使用excel分许

POST /report/excel/read EasyExcel流式逐行读取解析，不加载全表到内存
GET /report/excel/download 流式写入Response输出流，边写边返回，不占用堆内存

2 折线图，条形图，块图分析

3 文件上传
本地：写入项目image目录，返回本地访问路径
OSS：上传云端，返回CDN加速访问URL

1. 双存储环境隔离
   使用硬盘存储，对于内部的用户数据，敏感数据和重要文档。切换 OSS 加速、多实例共享文件、无限扩容，存储公共数据。
2. UUID 重命名策略
   丢弃原始文件名，UUID + 后缀生成全新文件名，解决重名覆盖、路径遍历攻击、中文乱码三大问题。

## 八、AI模块



## 九、aop日志

采用注解 + AOP 切面实现日志统一收集，自定义注解区分增删改查操作类型，切面统一采集上下文登录人、请求参数、耗时。

|       业务难点       |                场景                |                      解决方案                      |                          选型理由                           |
| :------------------: | :--------------------------------: | :------------------------------------------------: | :---------------------------------------------------------: |
| 日志逻辑侵入业务代码 | 每个 CRUD 方法手动写日志，代码冗余 | AOP 切面统一拦截，注解标记即可自动记录，无业务侵入 | 符合 AOP 面向切面设计思想，日志属于横向通用能力，与业务解耦 |

## 十、websocket

<img src="说明\resource\websocket.png" alt="websocket" style="zoom:50%;" />
