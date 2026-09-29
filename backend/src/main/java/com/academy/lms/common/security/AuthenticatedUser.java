package com.academy.lms.common.security;

import com.academy.lms.user.entity.Role;
import com.academy.lms.user.entity.User;
import java.util.Collection;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record AuthenticatedUser(
    UUID id,
    String email,
    String password,
    boolean enabled,
    Collection<? extends GrantedAuthority> authorities
) implements UserDetails {
  public static AuthenticatedUser from(User user) {
    return new AuthenticatedUser(
        user.getId(),
        user.getEmail(),
        user.getPasswordHash(),
        user.isEnabled(),
        user.getRoles().stream()
            .map(Role::getName)
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
            .toList());
  }

  @Override public String getUsername() { return email; }
  @Override public String getPassword() { return password; }
  @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
  @Override public boolean isAccountNonExpired() { return true; }
  @Override public boolean isAccountNonLocked() { return true; }
  @Override public boolean isCredentialsNonExpired() { return true; }
  @Override public boolean isEnabled() { return enabled; }
}
