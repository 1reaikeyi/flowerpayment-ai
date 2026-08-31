import request from '@/utils/admin/request.js'

/**
 * 店铺管理 API
 * 包含查询状态、设置状态等操作
 */

// 查询状态 - 查询店铺营业状态
export const getShopStatus = () => {
  return request({
    url: '/admin/shop/status',
    method: 'get'
  })
}

// 设置状态 - 设置店铺营业/打烊
export const setShopStatus = (status) => {
  return request({
    url: `/admin/shop/status/${status}`,
    method: 'put'
  })
}
