import request from '@/utils/admin/request.js'

/**
 * 多花礼盒明细管理 API（按 admin 接口文档对齐）
 * 后端控制器根路径：/admin/festivalDetail
 */

// 3.1 新增多花礼盒明细 - POST /admin/festivalDetail
// 请求体 FestivalDetailDTO: { festivalId(必填), flowerId(必填), specObject, specOption }
// specObject: 送人对象（如女友、母亲）；specOption: 用途场景（如表白、生日）
// 响应 Result<FestivalDetailDTO>（带自增 ID）
export const createFestivalDetail = (data) => {
  return request({
    url: '/admin/festivalDetail',
    method: 'post',
    data
  })
}

// 3.2 根据 ID 查询多花礼盒明细 - GET /admin/festivalDetail
// 请求参数: id (Long, 必填) 明细主键 ID
// 响应 Result<FestivalDetailVO>
export const getFestivalDetailById = (id) => {
  return request({
    url: '/admin/festivalDetail',
    method: 'get',
    params: { id }
  })
}

// 3.3 更新多花礼盒明细 - PUT /admin/festivalDetail
// 请求体 FestivalDetailDTO: { id(必填), festivalId, flowerId, specObject, specOption }
// 响应 Result<FestivalDetailDTO>
export const updateFestivalDetail = (data) => {
  return request({
    url: '/admin/festivalDetail',
    method: 'put',
    data
  })
}

// 3.4 批量删除多花礼盒明细 - DELETE /admin/festivalDetail
// 请求参数: ids (List<Long>, 必填) 明细 ID 列表
//响应 Result<List<Long>>
export const deleteFestivalDetails = (ids) => {
  return request({
    url: '/admin/festivalDetail',
    method: 'delete',
    params: { ids }
  })
}
