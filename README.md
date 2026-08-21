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
| **升级方案** | 使用nacos+gateway连接主业务+ai业务(两个服务)，灰度更新，分布式部署，故障转移等等。待后续开发。<br>当前使用mysql存储ai会话内容，高消耗和延迟，可以改成redis的IO密集型，性能更强。待后续开发。 |

**订单状态流转**：

```
1 待支付 → 2 已付款 → 3 制作中 → 4 骑手待出发 → 5 配送中 → 6 已送达 → 7 已完成 
        → 8 已取消（未接单退款、商家拒单、超时取消、售后全额退款）
```

**第三方授权登录流程图和支付流程**：支付宝

| 支付宝授权 | 授权成功 | 集成到订单 | 支付过程 | 同步支付 | 异步检验 |
| :--: | :--: | ---- | ---- | :--: | :--: |
| <img src="说明/支付宝+qq/ali1.png" alt="支付宝" style="zoom:25%;" /> | <img src="说明/支付宝+qq/ali2.png" alt="支付宝" style="zoom:50%;" /> | <img src="说明/支付宝+qq/1.png" alt="支付" style="zoom:25%;" /> | <img src="说明/支付宝+qq/2.png" alt="支付" style="zoom: 25%;" /> | <img src="说明/支付宝+qq/3.png" alt="支付" style="zoom: 25%;" /> | <img src="说明/支付宝+qq/4.png" alt="支付" style="zoom: 25%;" /> |

**第三方授权登录流程图和支付流程**：qq

| 待完善 |      |      |      |      |
| ------ | ---- | ---- | ---- | ---- |
|        |      |      |      |      |

# 启动步骤

1. 创建数据库并导入 `sql/` 目录脚本。
2. 修改 `start/src/main/resources/application-dev.yml` 中数据库与 Redis 配置。
3. `npm run dev ` 前端启动服务。

# 项目结构

```
flower/
├── spring-flower/            			  # 后端代码（Spring Boot 3 多模块）
│   ├── common/                           # 公共模块
│   ├── model/                            # 实体与数据传输对象
│   ├── mapper/                           # 数据访问层（MyBatis-Plus）
│   ├── service/                          # 业务逻辑模块
│   ├── start/                            # 主业务启动模块
│   └── ai/                           	  # AI服务启动模块
│
├── vue-flower/        					  # 前端管理端（Vue 3）
│   ├── src/
│   │   ├── api/                          # API接口封装（axios）
│   │   ├── views/                        # 页面视图
│   │   ├── layout/                       # 布局组件
│   │   ├── router/                       # 路由配置(router)
│   │   ├── stores/                       # 状态管理（Pinia）
│   │   └── utils/                        # 工具函数
│   └── package.json
│
├── database-sql/                  # 数据库脚本目录
│   ├── sql.txt                    # 数据库create table
│   ├── sql插入数据.txt              # 数据库初始化SQL
│   └── 数据库设计文档.md             # 数据库设计说明
│
└── 说明/                          # 项目说明文档
    ├── 原型功能/                   # 前端原型截图
    ├── 支付宝+qq/                  # 第三方应用注册和支付
    ├── 运行日志.txt                # 运行日志
    ├── admin接口文档.md            #admin接口详情
    └── user接口文档.md             #user接口详情
```

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