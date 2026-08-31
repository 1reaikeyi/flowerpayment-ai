import request from '@/utils/admin/request.js'

/**
 * Excel 报表 API（按 admin 接口文档对齐）
 * 后端控制器根路径：/report/excel
 */

// 导出全部用户为 EasyExcel 文件 - POST /report/excel/write，响应 data = 文件绝对路径
export const writeExcel = () => {
  return request({
    url: '/report/excel/write',
    method: 'post'
  })
}

// 读取 Excel 为 UserExcel 列表 - POST /report/excel/read，响应 data = List<UserExcel>
export const readExcel = () => {
  return request({
    url: '/report/excel/read',
    method: 'post'
  })
}

// 下载 Excel - GET /report/excel/download，响应为二进制流，文件名"导出用户数据.xlsx"
export const downloadExcel = () => {
  return request({
    url: '/report/excel/download',
    method: 'get',
    responseType: 'blob'
  })
}


