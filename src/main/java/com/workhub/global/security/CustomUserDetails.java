package com.workhub.global.security;

import com.workhub.userTable.entity.UserTable;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final String userName;
    private final String phone;
    private final GrantedAuthority authority;

    public CustomUserDetails(UserTable userTable) {
        this.userId = userTable.getUserId();
        this.username = userTable.getLoginId();
        this.password = userTable.getPassword();
        this.userName = userTable.getUserName();
        this.phone = userTable.getPhone();
        this.authority = new SimpleGrantedAuthority("ROLE_" + userTable.getRole().name());
    }

    public String getUserName() {
        return userName;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(authority);
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
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
