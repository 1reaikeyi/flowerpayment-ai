package start.security;

import common.constant.RoleConstant;
import common.exception.PasswordErrorException;
import model.entity.Employee;
import model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import service.EmployeeService;
import service.UserService;

import java.util.Collection;
import java.util.Collections;

@Component
public class RoleAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserService userService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.iterator().next().getAuthority();

        if (role.equals(RoleConstant.ROLE_ADMIN)) {
            Employee employee = employeeService.findEmployeename(username);
            if (employee == null || !passwordEncoder.matches(password, employee.getPassword())) {
                throw new PasswordErrorException("admin用户名或密码错误");
            }
            // 管理员认证成功，返回带有 ROLE_ADMIN 权限的认证对象
            return new UsernamePasswordAuthenticationToken(
                    new LoginUserDetails(employee.getId(), employee.getUsername(), employee.getPassword(),
                            Collections.singletonList(new SimpleGrantedAuthority(RoleConstant.ROLE_ADMIN))
                    ),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(RoleConstant.ROLE_ADMIN)));
        }
        if (role.equals(RoleConstant.ROLE_EMP)) {
            Employee employee = employeeService.findEmployeename(username);
            if (employee == null || !passwordEncoder.matches(password, employee.getPassword())) {
                throw new PasswordErrorException("employee用户名或密码错误");
            }
            // 管理员认证成功，返回带有 ROLE_ADMIN 权限的认证对象
            return new UsernamePasswordAuthenticationToken(
                    new LoginUserDetails(employee.getId(), employee.getUsername(), employee.getPassword(),
                            Collections.singletonList(new SimpleGrantedAuthority(RoleConstant.ROLE_EMP))
                    ),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(RoleConstant.ROLE_EMP)));
        }
//        if (role.equals(RoleConstant.ROLE_USER)) {}
        User user = userService.findUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new PasswordErrorException("用户名或密码错误");
        }
        // 普通用户认证成功，返回带有 ROLE_USER 权限的认证对象
        return new UsernamePasswordAuthenticationToken(
                new LoginUserDetails(user.getId(), user.getUsername(), user.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority(RoleConstant.ROLE_USER))
                ),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(RoleConstant.ROLE_USER)));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
