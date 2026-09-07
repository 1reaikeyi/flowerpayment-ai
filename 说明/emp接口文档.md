##  1. 员工管理 EmployeeController

**基础路径**: `/employee`

该控制器负责后台员工的注册、登录、登出及 CRUD 操作。

### 1.1 员工注册

- **方法**: `POST`
- **路径**: `/employee/register`
- **描述**: 创建新的员工账号
- **权限**: 无

**请求体 (EmployeeDTO)**:

| 字段     | 类型   | 必填 | 说明                           |
| -------- | ------ | ---- | ------------------------------ |
| username | String | 是   | 用户名（唯一）                 |
| password | String | 否   | 密码，默认为 123456            |
| work     | String | 否   | 工作/职位                      |
| avatar   | String | 否   | 头像 URL                       |
| email    | String | 否   | 邮箱                           |
| phone    | String | 否   | 手机号                         |
| sex      | String | 否   | 性别                           |
| status   | Long   | 否   | 状态 0:禁用 1:启用（默认为 1） |

**响应**: `Result<String>` → `"register"`

------

### 1.2 员工登录

- **方法**: `POST`
- **路径**: `/employee/login`
- **描述**: 员工登录，返回 JWT Token
- **权限**: 无

**请求体 (LoginDTO)**:

| 字段     | 类型   | 必填 | 说明   |
| -------- | ------ | ---- | ------ |
| username | String | 是   | 用户名 |
| password | String | 是   | 密码   |

**响应**: `Result<String>` → JWT Token 字符串

------

### 1.3 员工登出

- **方法**: `POST`
- **路径**: `/employee/logout`
- **描述**: 清除当前登录状态
- **权限**: 需登录

**请求参数**: 无

**响应**: `Result<String>` → `"logout"`

整体同user文档