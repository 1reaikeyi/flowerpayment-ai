package start.security;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Security 用户信息工具类
 *
 */
public class SecurityContextParam {
    /**
     * 获取当前登录用户主体信息
     *
     * @return LoginUserDetails，如果未登录返回null
     */
    public static LoginUserDetails getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUserDetails) {
            return (LoginUserDetails) principal;
        }
        return null;
    }


    /**
     * 获取当前登录用户的ID
     *
     * @return 用户ID，如果未登录返回null
     */
    public static Long getCurrentUserId() {
        LoginUserDetails details = getPrincipal();
        return details != null ? details.getId() : null;
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名，如果未登录返回null
     */
    public static String getCurrentUsername() {
        LoginUserDetails details = getPrincipal();
        return details != null ? details.getUsername() : null;
    }
    /**
     * 获取当前登录用户角色
     *
     * @return 角色，例如"ROLE_USER"或"ROLE_ADMIN"前缀，如果未登录返回null
     */
    public static String getCurrentType() {
        LoginUserDetails details = getPrincipal();
        return details != null ? details.getAuthorities().toString() : null;
    }
    


}