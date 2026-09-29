package com.academy.lms.user;
import com.academy.lms.audit.AuditService;
import com.academy.lms.common.exception.ApiException;
import com.academy.lms.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/profile") public class ProfileController {
  private final UserRepository users;private final UserMapper mapper;private final PasswordEncoder encoder;private final AuditService audit;
  public ProfileController(UserRepository u,UserMapper m,PasswordEncoder e,AuditService a){users=u;mapper=m;encoder=e;audit=a;}
  @PutMapping @Transactional UserDtos.Profile update(@Valid @RequestBody UserDtos.UpdateProfile r,Authentication a){User u=users.findById(CurrentUser.id(a)).orElseThrow();u.updateProfile(r.displayName().trim(),r.bio(),r.avatarUrl());return mapper.toProfile(u);}
  @PostMapping("/password") @Transactional void password(@Valid @RequestBody UserDtos.ChangePassword r,Authentication a,HttpServletRequest h){User u=users.findById(CurrentUser.id(a)).orElseThrow();if(!encoder.matches(r.currentPassword(),u.getPasswordHash()))throw ApiException.forbidden();u.changePassword(encoder.encode(r.newPassword()));audit.record(u.getId(),"PASSWORD_CHANGED","USER",u.getId(),h.getRemoteAddr());}
}

