
export const cleanToken = (token) => {
    if (!token) return ''
    // 直接返回 token（后端已修改为直接返回 JWT）
    return token
}

export const parseJWT = (token) => {
    if (!token) return null
    try {
        // 先清理 token，移除可能的前缀
        const cleanedToken = cleanToken(token)
        const base64Payload = cleanedToken.split('.')[1]
        const base64 = base64Payload.replace(/-/g, '+').replace(/_/g, '/')
        const padding = '='.repeat((4 - base64.length % 4) % 4)
        const decoded = atob(base64 + padding)
        const payload = decodeURIComponent(decoded.split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''))
        return JSON.parse(payload)
    } catch (e) {
        console.error('JWT 解析失败:', e)
        return null
    }
}

export const getUserIdFromToken = (token) => {
    const payload = parseJWT(token)
    // 优先匹配后端定义的 adminId 字段
    return payload?.adminId || payload?.ADMIN_ID || null
}

export const getUserNameFromToken = (token) => {
    const payload = parseJWT(token)
    // 优先匹配后端定义的 adminName 字段
    return payload?.adminName || payload?.ADMIN_NAME || null
}
