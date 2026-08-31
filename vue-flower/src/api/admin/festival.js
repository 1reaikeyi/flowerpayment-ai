import request from '@/utils/admin/request.js'

/**
 * 节日多花礼盒管理 API（按 admin 接口文档对齐）
 * 后端控制器根路径：/admin/festival
 * 使用 Redis 缓存 + Redisson 分布式锁 + 逻辑过期策略
 * 缓存 key 前缀: festival:{id}
 */

// 2.1 新增节日多花礼盒 - POST /admin/festival
// 请求体 FestivalDTO: { name(必填), categoryId(必填), price(必填), number, status, description, image }
// 响应 Result<FestivalDTO>（带自增 ID）
export const createFestival = (data) => {
  return request({
    url: '/admin/festival',
    method: 'post',
    data
  })
}

// 2.2 根据 ID 查询节日多花礼盒 - GET /admin/festival
// 请求参数: id (Long, 必填)
// 带 Redis 缓存 + Redisson 锁 + 逻辑过期，响应 Result<FestivalVO>
export const getFestivalById = (id) => {
  return request({
    url: '/admin/festival',
    method: 'get',
    params: { id }
  })
}

// 2.3 分页查询多花礼盒列表 - GET /admin/festival/all
// 请求参数 FestivalPageDTO (Query): { page, pageSize, name }
// 支持按名称模糊搜索，响应 Result<List<FestivalVO>>
export const pageFestivalList = (params) => {
  return request({
    url: '/admin/festival/all',
    method: 'get',
    params
  })
}

// 2.4 更新节日多花礼盒 - PUT /admin/festival
// 请求体 FestivalDTO: { id(必填), name, categoryId, price, number, status, description, image }
// 只更新非空字段，更新后清除 Redis 缓存
// 响应 Result<FestivalDTO>
export const updateFestival = (data) => {
  return request({
    url: '/admin/festival',
    method: 'put',
    data
  })
}

// 2.5 批量删除节日多花礼盒 - DELETE /admin/festival
// 请求参数: ids (List<Long>, 必填)
// 删除后遍历清除 Redis 缓存，响应 Result<List<Long>>
export const deleteFestivals = (ids) => {
  return request({
    url: '/admin/festival',
    method: 'delete',
    params: { ids }
  })
}

// 2.6 查询多花礼盒下的明细列表 - GET /admin/festival/of/festivalDetail
// 请求参数: id (Long, 必填) 多花礼盒主键 ID
// 在 festival_detail 表按 festival_id 查询，响应 Result<List<FestivalDetailVO>>
export const getFestivalDetailsByFestivalId = (id) => {
  return request({
    url: '/admin/festival/of/festivalDetail',
    method: 'get',
    params: { id }
  })
}

// 2.7 查询某鲜花被哪些多花礼盒包含 - GET /admin/festival/of/flower
// 请求参数: id (Long, 必填) 鲜花主键 ID
// 在 festival_detail 表按 flower_id 查询，响应 Result<List<FestivalDetailVO>>
export const getFestivalsByFlowerId = (id) => {
  return request({
    url: '/admin/festival/of/flower',
    method: 'get',
    params: { id }
  })
}
