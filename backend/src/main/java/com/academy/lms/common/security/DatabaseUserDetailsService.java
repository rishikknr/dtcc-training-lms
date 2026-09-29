package com.academy.lms.common.security;

import com.academy.lms.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
  private final UserRepository users;

  public DatabaseUserDetailsService(UserRepository users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String email) {
    return users.findByEmailIgnoreCase(email)
        .map(AuthenticatedUser::from)
        .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
  }
}
