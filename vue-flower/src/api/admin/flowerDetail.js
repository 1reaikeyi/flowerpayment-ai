import request from '@/utils/admin/request.js'

/**
 * 鲜花明细管理 API（按 admin 接口文档对齐）
 * 后端控制器根路径：/admin/flowerDetail
 * 管理鲜花的规格明细（送人对象、用途场景），支持单条 CRUD
 * 使用 Redis 缓存 + Redisson 分布式锁
 * 缓存 key 前缀: flowerDetail:{id}
 */

// 6.1 新增鲜花明细 - POST /admin/flowerDetail
// 请求体 FlowerDetailDTO: { flowerId(必填), specObject, specOption }
// specObject: 送人对象（如女友、母亲）；specOption: 用途场景（如表白、生日）
// 响应 Result<FlowerDetailDTO>（带自增 ID）
export const createFlowerDetail = (data) => {
  return request({
    url: '/admin/flowerDetail',
    method: 'post',
    data
  })
}

// 6.2 根据 ID 查询鲜花明细 - GET /admin/flowerDetail
// 请求参数: id (Long, 必填) 明细主键 ID
// 带 Redis 缓存 + Redisson 锁 + 逻辑过期，响应 Result<FlowerDetailVO>
export const getFlowerDetailById = (id) => {
  return request({
    url: '/admin/flowerDetail',
    method: 'get',
    params: { id }
  })
}

// 6.3 更新鲜花明细 - PUT /admin/flowerDetail
// 请求体 FlowerDetailDTO: { id(必填), flowerId, specObject, specOption }
// 只更新非空字段，更新后清除 Redis 缓存
// 响应 Result<FlowerDetailDTO>
export const updateFlowerDetail = (data) => {
  return request({
    url: '/admin/flowerDetail',
    method: 'put',
    data
  })
}

// 6.4 批量删除鲜花明细 - DELETE /admin/flowerDetail
// 请求参数: ids (List<Long>, 必填) 明细 ID 列表
// 删除后遍历清除 Redis 缓存，响应 Result<List<Long>>
export const deleteFlowerDetails = (ids) => {
  return request({
    url: '/admin/flowerDetail',
    method: 'delete',
    params: { ids }
  })
}
