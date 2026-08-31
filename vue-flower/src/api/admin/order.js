import request from '@/utils/admin/request.js'

/**
 * 鲜花订单管理 API（按 admin 接口文档对齐）
 * 后端控制器根路径：/admin/flowerOrder
 * 订单状态枚举：1-用户下单, 2-已支付, 3-商家制作, 4-工作人员取货, 5-配送中, 6-已到达, 7-已完成, 8-已取消
 */

// 7.1 根据 ID 查询订单 - GET /admin/flowerOrder
// 请求参数: id (Long, 必填) 订单主键 ID
// 响应 Result<FlowerOrderVO>（当前后端实现待完善）
export const getOrderById = (id) => {
  return request({
    url: '/admin/flowerOrder',
    method: 'get',
    params: { id }
  })
}

// 7.2 分页查询订单列表 - GET /admin/flowerOrder/all
// 请求参数 FlowerOrderPageDTO (Query): { page, pageSize, startTime, endTime }
// 支持按时间范围筛选，响应 Result<List<FlowerOrderVO>>
export const pageOrderList = (params) => {
  return request({
    url: '/admin/flowerOrder/all',
    method: 'get',
    params
  })
}

// 7.3 订单状态流转 → 商家制作 - PUT /admin/flowerOrder/cooking/{id}
// 路径参数: id (Long, 必填) 订单主键 ID
// 更新状态码为 3 (COOKING)，响应 Result<OrderStatusEnum>
export const updateOrderToCooking = (id) => {
  return request({
    url: `/admin/flowerOrder/cooking/${id}`,
    method: 'put'
  })
}

// 7.4 订单状态流转 → 工作人员取货 - PUT /admin/flowerOrder/go/{id}
// 路径参数: id (Long, 必填) 订单主键 ID
// 更新状态码为 4 (GO)，响应 Result<OrderStatusEnum>
export const updateOrderToGo = (id) => {
  return request({
    url: `/admin/flowerOrder/go/${id}`,
    method: 'put'
  })
}

// 7.5 订单状态流转 → 配送中 - PUT /admin/flowerOrder/delivering/{id}
// 路径参数: id (Long, 必填) 订单主键 ID
// 更新状态码为 5 (DELIVERING)，响应 Result<OrderStatusEnum>
export const updateOrderToDelivering = (id) => {
  return request({
    url: `/admin/flowerOrder/delivering/${id}`,
    method: 'put'
  })
}

// 7.6 订单状态流转 → 已到达 - PUT /admin/flowerOrder/arrived/{id}
// 路径参数: id (Long, 必填) 订单主键 ID
// 更新状态码为 6 (ARRIVED)，响应 Result<OrderStatusEnum>
export const updateOrderToArrived = (id) => {
  return request({
    url: `/admin/flowerOrder/arrived/${id}`,
    method: 'put'
  })
}

// 7.7 订单状态流转 → 已完成 - PUT /admin/flowerOrder/complete/{id}
// 路径参数: id (Long, 必填) 订单主键 ID
// 更新状态码为 7 (COMPLETED)，响应 Result<OrderStatusEnum>
export const updateOrderToComplete = (id) => {
  return request({
    url: `/admin/flowerOrder/complete/${id}`,
    method: 'put'
  })
}

// 7.8 订单状态流转 → 已取消（含退款） - PUT /admin/flowerOrder/canceled/{id}
// 路径参数: id (Long, 必填) 订单主键 ID
// 同时触发支付宝退款，更新状态码为 8 (CANCELLED)
// 响应 Result<OrderStatusEnum>
export const cancelOrder = (id) => {
  return request({
    url: `/admin/flowerOrder/canceled/${id}`,
    method: 'put'
  })
}
