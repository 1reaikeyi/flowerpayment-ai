import request from '@/utils/admin/request.js'

/**
 * 营业数据统计 API（按 admin 接口文档对齐）
 * 后端控制器根路径：/admin/statistics，均为只读 GET 查询
 */

// 9.1 鲜花单品销量统计 - GET /admin/statistics/flower
// 响应 Result<List<StatisticsVO>>: { id, name, count, totalAccount }
export const getFlower = () => {
    return request({
        url: '/admin/statistics/flower',
        method: 'get'
    })
}

// 9.2 多花礼盒销量统计 - GET /admin/statistics/festival（按销量降序）
// 响应 Result<List<StatisticsVO>>
export const getFestival = () => {
    return request({
        url: '/admin/statistics/festival',
        method: 'get'
    })
}

// 9.3 鲜花热销榜 TopN - GET /admin/statistics/top1
// 响应 Result<List<TopStatisticsVO>>: { id, name, count }
export const getTop1 = () => {
    return request({
        url: '/admin/statistics/top1',
        method: 'get'
    })
}

// 9.4 多花礼盒热销榜 TopN - GET /admin/statistics/top2
// 响应 Result<List<TopStatisticsVO>>
export const getTop2 = () => {
    return request({
        url: '/admin/statistics/top2',
        method: 'get'
    })
}

// 9.5 订单状态分布统计 - GET /admin/statistics/order
// 响应 Result<List<OrderStatisticsVO>>: { status, name, count }
export const getOrder = () => {
    return request({
        url: '/admin/statistics/order',
        method: 'get'
    })
}

// 9.6 今日订单统计 - GET /admin/statistics/today
// 今日订单数、已支付订单数、今日营业额，响应 Result<List<OrderStatisticsVO>>
export const getTodayOrder = () => {
    return request({
        url: '/admin/statistics/today',
        method: 'get'
    })
}
