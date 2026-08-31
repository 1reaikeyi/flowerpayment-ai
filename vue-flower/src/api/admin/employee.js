import request from '@/utils/admin/request.js'

/**
 * 员工管理 API（按 admin 接口文档对齐）
 * 后端控制器根路径：/admin/employee
 * 认证方式：POST /admin/employee/login 返回 JWT Token，后续请求需携带 Authorization: Bearer {token}
 */

// 1.1 员工注册 - POST /admin/employee/register
// 请求体 EmployeeDTO: { username, password, work, avatar, email, phone, sex, status }
// 响应 Result<String> → "register"
export const registerEmployee = (data) => {
  return request({
    url: '/admin/employee/register',
    method: 'post',
    data
  })
}

// 1.2 员工登录 - POST /admin/employee/login
// 请求体 LoginDTO: { username, password }
// 响应 Result<String> → JWT Token 字符串
export const loginEmployee = (data) => {
  return request({
    url: '/admin/employee/login',
    method: 'post',
    data
  })
}

// 1.3 员工登出 - POST /admin/employee/logout
// 清除 Redis 中的 token，响应 Result<String> → "logout"
export const logoutEmployee = () => {
  return request({
    url: '/admin/employee/logout',
    method: 'post'
  })
}

// 1.4 根据 ID 查询员工 - GET /admin/employee
// 请求参数: id (Long, 必填)
// 响应 Result<EmployeeVO>
export const getEmployeeById = (id) => {
  return request({
    url: '/admin/employee',
    method: 'get',
    params: { id }
  })
}

// 1.5 分页查询员工列表 - GET /admin/employee/all
// 请求参数 EmployeePageDTO (Query): { page, pageSize, employeename }
// 支持按用户名模糊搜索
// 响应 Result<List<EmployeeVO>>
export const pageEmployeeList = (params) => {
  return request({
    url: '/admin/employee/all',
    method: 'get',
    params
  })
}

// 1.6 更新员工信息 - PUT /admin/employee
// 请求体 EmployeeDTO: { id(必填), username, password, work, avatar, email, phone, sex, status }
// 只更新非空字段，密码自动 BCrypt 加密
// 响应 Result<Long> → 更新的员工 ID
export const updateEmployee = (data) => {
  return request({
    url: '/admin/employee',
    method: 'put',
    data
  })
}

// 1.7 批量删除员工 - DELETE /admin/employee
// 请求参数: ids (List<Long>, 必填)
// 响应 Result<List<Long>> → 删除的 ID 列表
export const deleteEmployees = (ids) => {
  return request({
    url: '/admin/employee',
    method: 'delete',
    params: { ids }
  })
}

// 1.8 修改密码 - DELETE /admin/employee/password
// @RequestBody PasswordDTO: { newPassword, confirmPassword }
// 后端从 token 取当前用户 id，前端无需传 id
// 注意：后端 HTTP 方法是 DELETE + RequestBody，虽然不常见但必须严格对齐
export const updateEmployeePassword = (data) => {
  return request({
    url: '/admin/employee/password',
    method: 'delete',
    data
  })
}
