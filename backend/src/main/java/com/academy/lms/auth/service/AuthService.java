package com.academy.lms.auth.service;

import com.academy.lms.auth.dto.request.LoginRequest;
import com.academy.lms.auth.dto.request.RegisterRequest;
import com.academy.lms.auth.dto.response.AuthResponse;
import com.academy.lms.auth.security.authorization.LoginThrottle;
import com.academy.lms.common.audit.AuditService;
import com.academy.lms.common.exception.ApiException;
import com.academy.lms.common.security.AuthenticatedUser;
import com.academy.lms.user.entity.Role;
import com.academy.lms.user.entity.RoleName;
import com.academy.lms.user.entity.User;
import com.academy.lms.user.mapper.UserMapper;
import com.academy.lms.user.repository.RoleRepository;
import com.academy.lms.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final UserRepository users;
  private final RoleRepository roles;
  private final PasswordEncoder passwords;
  private final UserMapper mapper;
  private final LoginThrottle throttle;
  private final AuditService audit;

  public AuthService(UserRepository users, RoleRepository roles, PasswordEncoder passwords,
                     UserMapper mapper, LoginThrottle throttle, AuditService audit) {
    this.users = users;
    this.roles = roles;
    this.passwords = passwords;
    this.mapper = mapper;
    this.throttle = throttle;
    this.audit = audit;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request, HttpServletRequest http) {
    if (users.existsByEmailIgnoreCase(request.email())) {
      throw ApiException.conflict("An account with that email already exists");
    }
    Role student = roles.findByName(RoleName.STUDENT)
        .orElseThrow(() -> new IllegalStateException("STUDENT role is not configured"));
    User user = users.save(new User(request.email(), passwords.encode(request.password()),
        request.displayName(), student));
    authenticateSession(user, http);
    audit.record(user.getId(), "USER_REGISTERED", "USER", user.getId(), http);
    return new AuthResponse(mapper.toProfile(user));
  }

  @Transactional(noRollbackFor = ApiException.class)
  public AuthResponse login(LoginRequest request, HttpServletRequest http) {
    String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
    String throttleKey = clientIp(http) + ":" + normalizedEmail;
    throttle.check(throttleKey);
    User user = users.findByEmailIgnoreCase(normalizedEmail).orElse(null);

    if (user == null || !passwords.matches(request.password(), user.getPasswordHash())) {
      throttle.failure(throttleKey);
      if (user != null) user.loginFailed();
      throw invalidCredentials();
    }
    if (!user.isEnabled()) {
      throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED",
          "This account has been disabled");
    }
    if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
      throw new ApiException(HttpStatus.LOCKED, "ACCOUNT_LOCKED",
          "Account temporarily locked");
    }

    user.loginSucceeded();
    throttle.success(throttleKey);
    authenticateSession(user, http);
    audit.record(user.getId(), "USER_LOGIN", "USER", user.getId(), http);
    return new AuthResponse(mapper.toProfile(user));
  }

  @Transactional
  public void logout(java.util.UUID userId, HttpServletRequest http) {
    audit.record(userId, "USER_LOGOUT", "USER", userId, http);
    HttpSession session = http.getSession(false);
    if (session != null) session.invalidate();
    SecurityContextHolder.clearContext();
  }

  @Transactional(readOnly = true)
  public AuthResponse refresh(java.util.UUID userId, HttpServletRequest http) {
    User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
    if (!user.isEnabled()) {
      throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED",
          "This account has been disabled");
    }
    authenticateSession(user, http);
    return new AuthResponse(mapper.toProfile(user));
  }

  private ApiException invalidCredentials() {
    return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
        "Invalid email or password");
  }

  private void authenticateSession(User user, HttpServletRequest request) {
    HttpSession existing = request.getSession(false);
    if (existing != null) request.changeSessionId();
    else request.getSession(true);
    var principal = AuthenticatedUser.from(user);
    var authentication = new UsernamePasswordAuthenticationToken(
        principal, null, principal.getAuthorities());
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
    request.getSession(true).setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    return forwarded == null || forwarded.isBlank()
        ? request.getRemoteAddr()
        : forwarded.split(",", 2)[0].trim();
  }
}
