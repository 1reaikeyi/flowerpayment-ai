import request from '@/utils/admin/request.js'

/**
 * 文件 API - 仅使用本地存储（后端 FileController，根路径 /local）
 */

// 本地文件上传 - POST /local，multipart/form-data，字段 file
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
export const downloadFile = (fileName) => {
  return request({
    url: '/local',
    method: 'get',
    params: { fileName },
    responseType: 'blob'
  })
}
