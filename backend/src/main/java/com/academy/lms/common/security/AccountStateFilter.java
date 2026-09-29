package com.academy.lms.common.security;

import com.academy.lms.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AccountStateFilter extends OncePerRequestFilter {
  private final UserRepository users;

  public AccountStateFilter(UserRepository users) { this.users = users; }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser current) {
      var databaseUser = users.findById(current.id()).orElse(null);
      if (databaseUser == null || !databaseUser.isEnabled()) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
      } else {
        var refreshed = AuthenticatedUser.from(databaseUser);
        var replacement = new UsernamePasswordAuthenticationToken(
            refreshed, null, refreshed.getAuthorities());
        replacement.setDetails(authentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(replacement);
      }
    }
    chain.doFilter(request, response);
  }
}
