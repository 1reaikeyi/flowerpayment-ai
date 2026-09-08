import request from '@/utils/admin/request.js'

/**
 * 统计 API（按 admin 接口文档对齐）
 * 后端控制器根路径：/admin/statistics，均为只读 GET 查询
 */

export const getFlower = () => {
    return request({
        url: '/admin/statistics/flower',
        method: 'get'
    })
}


export const getFestival = () => {
    return request({
        url: '/admin/statistics/festival',
        method: 'get'
    })
}
export const getTop1 = () => {
    return request({
        url: '/admin/statistics/flower',
        method: 'get'
    })
}

export const getTop2 = () => {
    return request({
        url: '/admin/statistics/festival',
        method: 'get'
    })
}

export const getOrder = () => {
    return request({
        url: '/admin/statistics/order',
        method: 'get'
    })
}

// 今日统计 - GET /statistics/today，今日下单数/已支付数/营业额
export const getTodayOrder = () => {
    return request({
        url: '/admin/statistics/today',
        method: 'get'
    })
}



