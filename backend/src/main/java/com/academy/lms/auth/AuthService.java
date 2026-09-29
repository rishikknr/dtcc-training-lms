package com.academy.lms.auth;
import com.academy.lms.audit.AuditService;
import com.academy.lms.common.exception.ApiException;
import com.academy.lms.security.AuthenticatedUser;
import com.academy.lms.user.*;
import jakarta.servlet.http.*;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service public class AuthService {
  private final UserRepository users; private final RoleRepository roles; private final PasswordEncoder passwords; private final UserMapper mapper; private final LoginThrottle throttle; private final AuditService audit;
  public AuthService(UserRepository u,RoleRepository r,PasswordEncoder p,UserMapper m,LoginThrottle t,AuditService a){users=u;roles=r;passwords=p;mapper=m;throttle=t;audit=a;}
  @Transactional public AuthDtos.AuthResponse register(AuthDtos.Register req,HttpServletRequest http){
    if(users.existsByEmailIgnoreCase(req.email()))throw ApiException.conflict("An account with that email already exists");
    Role role=roles.findByName(RoleName.STUDENT).orElseThrow(); User u=users.save(new User(req.email(),passwords.encode(req.password()),req.displayName().trim(),role));
    authenticateSession(u,http);audit.record(u.getId(),"USER_REGISTERED","USER",u.getId(),http.getRemoteAddr());return new AuthDtos.AuthResponse(mapper.toProfile(u));
  }
  @Transactional(noRollbackFor=ApiException.class) public AuthDtos.AuthResponse login(AuthDtos.Login req,HttpServletRequest http){
    String key=http.getRemoteAddr()+":"+req.email().toLowerCase();throttle.check(key);User u=users.findByEmailIgnoreCase(req.email()).orElse(null);
    if(u==null||!passwords.matches(req.password(),u.getPasswordHash())){throttle.fail(key);if(u!=null)u.loginFailed();throw new ApiException(HttpStatus.UNAUTHORIZED,"INVALID_CREDENTIALS","Invalid email or password");}
    if(u.getLockedUntil()!=null&&u.getLockedUntil().isAfter(Instant.now()))throw new ApiException(HttpStatus.LOCKED,"ACCOUNT_LOCKED","Account temporarily locked");
    u.loginSucceeded();throttle.success(key);authenticateSession(u,http);audit.record(u.getId(),"USER_LOGIN","USER",u.getId(),http.getRemoteAddr());return new AuthDtos.AuthResponse(mapper.toProfile(u));
  }
  private void authenticateSession(User u,HttpServletRequest req){HttpSession existing=req.getSession(false);if(existing!=null)req.changeSessionId();else req.getSession(true);var principal=AuthenticatedUser.from(u);var auth=new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());var context=SecurityContextHolder.createEmptyContext();context.setAuthentication(auth);SecurityContextHolder.setContext(context);req.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,context);}
}

