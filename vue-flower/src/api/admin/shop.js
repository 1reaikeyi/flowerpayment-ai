import request from '@/utils/admin/request.js'

/**
 * 店铺管理 API（按 admin 接口文档对齐）
 * 后端控制器根路径：/admin/shop
 * 营业状态存储于 Redis，key 为 ShopConstant.SHOP_STATUS
 */

// 查询营业状态 - GET /admin/shop，响应 data = ShopVO { status: "营业中"/"已打烊" }
export const getShopStatus = () => {
  return request({
    url: '/admin/shop',
    method: 'get'
  })
}

// 设置营业状态 - POST /admin/shop/{status}，1=营业中，其它=已打烊，响应 data = ShopVO
export const setShopStatus = (status) => {
  return request({
    url: `/admin/shop/${status}`,
    method: 'post'
  })
}
