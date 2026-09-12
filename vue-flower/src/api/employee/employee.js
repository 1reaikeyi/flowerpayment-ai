import request from '@/utils/admin/request.js'

/**
 * 员工端账户 API（后端 EmployeeController，根路径 /employee）
 * admin 接口文档第 1 节明确：员工注册 / 资料修改接口在员工端 /employee/**，不属于 /admin 控制器
 */

// 员工注册 - POST /employee/register（Security 配置 permitAll 放行）
// 请求体 EmployeeDTO: { username, password, work, avatar, email, phone, sex, status }
// 响应 Result<String> → "register"
export const registerEmployee = (data) => {
  return request({
    url: '/employee/register',
    method: 'post',
    data
  })
}

// 更新当前登录员工资料 - PUT /employee（需 ROLE_EMP，按登录态更新本人信息）
// 请求体 EmployeeDTO: { id(必填), username, password, work, avatar, email, phone, sex, status }
// 只更新非空字段，密码自动 BCrypt 加密
// 响应 Result<Long> → 更新的员工 ID
export const updateEmployee = (data) => {
  return request({
    url: '/employee',
    method: 'put',
    data
  })
}
