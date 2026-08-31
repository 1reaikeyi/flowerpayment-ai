import request from '@/utils/admin/request.js'

/**
 * 鲜花分类管理 API（按 admin 接口文档对齐）
 * 后端控制器根路径：/admin/category
 * 使用 Spring Cache（Redis）做全量缓存，增删改后自动清除缓存
 */

// 4.1 新增鲜花分类 - POST /admin/category
// 请求体 FlowerCategoryDTO: { name(必填), type(必填, 1=鲜花单只, 2=节日多只, 3=礼品), sort, status }
// 响应 Result<FlowerCategoryDTO>
export const createCategory = (data) => {
  return request({
    url: '/admin/category',
    method: 'post',
    data
  })
}

// 4.2 根据类型查询分类列表 - GET /admin/category
// 请求参数: type (Long, 必填) 分类类型（1/2/3）
// 使用 Spring Cache 按 type 缓存，响应 Result<List<FlowerCategoryVO>>
export const getCategoryByType = (type) => {
  return request({
    url: '/admin/category',
    method: 'get',
    params: { type }
  })
}

// 4.3 分页查询分类列表 - GET /admin/category/all
// 请求参数 FlowerCategoryPageDTO (Query): { page, pageSize, type }
// 支持按类型筛选，响应 Result<List<FlowerCategoryVO>>
export const pageCategoryList = (params) => {
  return request({
    url: '/admin/category/all',
    method: 'get',
    params
  })
}

// 4.4 更新鲜花分类 - PUT /admin/category
// 请求体 FlowerCategoryDTO: { id(必填), name, type, sort, status }
// 只更新非空字段，更新后清除 Spring Cache
// 响应 Result<FlowerCategoryDTO>
export const updateCategory = (data) => {
  return request({
    url: '/admin/category',
    method: 'put',
    data
  })
}

// 4.5 批量删除鲜花分类 - DELETE /admin/category
// 请求参数: ids (List<Long>, 必填)
// 响应 Result<List<Long>> → 删除的 ID 列表
export const deleteCategories = (ids) => {
  return request({
    url: '/admin/category',
    method: 'delete',
    params: { ids }
  })
}

// 4.6 查询分类下的所有鲜花 - GET /admin/category/of/flower
// 请求参数: id (Long, 必填) 分类主键 ID
// 在 flower 表按 category_id 查询，响应 Result<List<FlowerVO>>
export const getFlowersByCategoryId = (id) => {
  return request({
    url: '/admin/category/of/flower',
    method: 'get',
    params: { id }
  })
}

// 4.7 查询分类下的所有多花礼盒 - GET /admin/category/of/festival
// 请求参数: id (Long, 必填) 分类主键 ID
// 在 festival 表按 category_id 查询，响应 Result<List<FestivalVO>>
export const getFestivalsByCategoryId = (id) => {
  return request({
    url: '/admin/category/of/festival',
    method: 'get',
    params: { id }
  })
}
