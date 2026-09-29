package com.academy.lms.security;
import com.academy.lms.user.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
@Service public class DatabaseUserDetailsService implements UserDetailsService {
  private final UserRepository users; public DatabaseUserDetailsService(UserRepository users){this.users=users;}
  public UserDetails loadUserByUsername(String email){return users.findByEmailIgnoreCase(email).map(AuthenticatedUser::from).orElseThrow(()->new UsernameNotFoundException("Invalid credentials"));}
}

