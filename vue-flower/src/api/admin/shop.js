import request from '@/utils/admin/request.js'

/**
 * 店铺管理 API（按 admin 接口文档对齐）
 * 后端控制器根路径：/admin/shop
 */

// 查询营业状态 - GET /admin/shop，响应 data = ShopVO { status: "营业中"/"已打烊" }
export const getShopStatus = () => {
  return request({
    url: '/admin/shop',
    method: 'get'
  })
}

// 设置营业状态 - POST /admin/shop/{status}，路径参数 status 仅取 0 或 1：1=营业中，0=已打烊，响应 data = ShopVO
export const setShopStatus = (status) => {
  return request({
    url: `/admin/shop/${status}`,
    method: 'post'
  })
}
