package service.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class LoginUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private String password;
    private final Collection<? extends GrantedAuthority> role;

    /**
     * 构造时把角色传进来，直接存
     */
    public LoginUserDetails(Long id, String username, String password,
                            Collection<? extends GrantedAuthority> role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public LoginUserDetails(Long id, String username, Collection<? extends GrantedAuthority> role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    // getters
    public Long getId() { return id; }
}