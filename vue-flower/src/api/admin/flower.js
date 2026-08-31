import request from '@/utils/admin/request.js'

/**
 * 鲜花单品管理 API（按 admin 接口文档对齐）
 * 后端控制器根路径：/admin/flower
 * 使用 Redis 缓存 + Redisson 分布式锁 + 逻辑过期策略
 * 缓存 key 前缀: flower:{id}
 */

// 5.1 新增鲜花 - POST /admin/flower
// 请求体 FlowerDTO: { name(必填), color, categoryId(必填), price(必填), image, description, status }
// 响应 Result<FlowerDTO>
export const createFlower = (data) => {
  return request({
    url: '/admin/flower',
    method: 'post',
    data
  })
}

// 5.2 根据 ID 查询鲜花 - GET /admin/flower
// 请求参数: id (Long, 必填)
// 带 Redis 缓存，逻辑过期时异步重建，响应 Result<FlowerVO>
export const getFlowerById = (id) => {
  return request({
    url: '/admin/flower',
    method: 'get',
    params: { id }
  })
}

// 5.3 分页查询鲜花列表 - GET /admin/flower/all
// 请求参数 FlowerPageDTO (Query): { page, pageSize, name }
// 支持按名称模糊搜索，响应 Result<List<FlowerVO>>
export const pageFlowerList = (params) => {
  return request({
    url: '/admin/flower/all',
    method: 'get',
    params
  })
}

// 5.4 更新鲜花 - PUT /admin/flower
// 请求体 FlowerDTO: { id(必填), name, color, categoryId, price, image, description, status }
// 只更新非空字段，更新后清除 Redis 缓存
// 响应 Result<FlowerDTO>
export const updateFlower = (data) => {
  return request({
    url: '/admin/flower',
    method: 'put',
    data
  })
}

// 5.5 批量删除鲜花 - DELETE /admin/flower
// 请求参数: ids (List<Long>, 必填)
// 删除后遍历清除 Redis 缓存，响应 Result<List<Long>>
export const deleteFlowers = (ids) => {
  return request({
    url: '/admin/flower',
    method: 'delete',
    params: { ids }
  })
}

// 5.6 查询鲜花下的所有明细 - GET /admin/flower/of/flowerDetail
// 请求参数: id (Long, 必填) 鲜花主键 ID
// 在 flower_detail 表按 flower_id 查询，响应 Result<List<FlowerDetailVO>>
export const getFlowerDetailsByFlowerId = (id) => {
  return request({
    url: '/admin/flower/of/flowerDetail',
    method: 'get',
    params: { id }
  })
}
