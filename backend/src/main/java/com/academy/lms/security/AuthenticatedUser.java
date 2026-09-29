package com.academy.lms.security;
import com.academy.lms.user.*;
import java.util.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
public record AuthenticatedUser(UUID id,String email,String password,boolean enabled,Collection<? extends GrantedAuthority> authorities) implements UserDetails {
  public static AuthenticatedUser from(User u){return new AuthenticatedUser(u.getId(),u.getEmail(),u.getPasswordHash(),u.isEnabled(),u.getRoles().stream().map(r->new SimpleGrantedAuthority("ROLE_"+r.getName())).toList());}
  public String getUsername(){return email;} public String getPassword(){return password;} public Collection<? extends GrantedAuthority> getAuthorities(){return authorities;} public boolean isAccountNonExpired(){return true;} public boolean isAccountNonLocked(){return true;} public boolean isCredentialsNonExpired(){return true;} public boolean isEnabled(){return enabled;}
}
