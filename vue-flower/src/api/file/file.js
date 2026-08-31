import request from '@/utils/admin/request.js'

/**
 * 文件 API - 仅使用本地存储（后端 FileController，根路径 /local）
 */

// 本地文件上传 - POST /local，multipart/form-data，字段 file
// 后端返回 data = "{绝对路径}::{保存文件名}"，前端一般只用"::"后的文件名写入数据库
export const uploadFile = (data) => {
  return request({
    url: '/local',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 本地文件下载 - GET /local?fileName={fileName}，响应为二进制流
// 说明：前端展示图片一般不直接调用此函数，而是拼成 /image/xxx.png 走静态资源映射
export const downloadFile = (fileName) => {
  return request({
    url: '/local',
    method: 'get',
    params: { fileName },
    responseType: 'blob'
  })
}
