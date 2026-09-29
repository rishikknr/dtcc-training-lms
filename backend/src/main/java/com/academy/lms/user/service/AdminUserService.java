package com.academy.lms.user.service;

import com.academy.lms.common.api.PageResponse;
import com.academy.lms.common.audit.AuditService;
import com.academy.lms.common.exception.ApiException;
import com.academy.lms.user.dto.request.UpdateUserStatusRequest;
import com.academy.lms.user.dto.response.AdminUserResponse;
import com.academy.lms.user.entity.RoleName;
import com.academy.lms.user.entity.User;
import com.academy.lms.user.mapper.UserMapper;
import com.academy.lms.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {
  private final UserRepository users;
  private final UserMapper mapper;
  private final AuditService audit;

  public AdminUserService(UserRepository users, UserMapper mapper, AuditService audit) {
    this.users = users;
    this.mapper = mapper;
    this.audit = audit;
  }

  @Transactional(readOnly = true)
  public PageResponse<AdminUserResponse> list(String query, int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
        Sort.by(Sort.Direction.DESC, "createdAt"));
    var result = query == null || query.isBlank()
        ? users.findAll(pageable)
        : users.findByEmailContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
            query.trim(), query.trim(), pageable);
    return PageResponse.from(result.map(mapper::toAdmin));
  }

  @Transactional
  public AdminUserResponse updateStatus(UUID actorId, UUID targetId, UpdateUserStatusRequest request,
                                        HttpServletRequest http) {
    if (actorId.equals(targetId) && !request.enabled()) {
      throw ApiException.conflict("Administrators cannot disable their own account");
    }
    User target = users.findById(targetId).orElseThrow(() -> ApiException.notFound("User"));
    if (target.hasRole(RoleName.ADMIN) && !request.enabled()) {
      long enabledAdmins = users.countEnabledByRole(RoleName.ADMIN);
      if (enabledAdmins <= 1) throw ApiException.conflict("The last enabled admin cannot be disabled");
    }
    target.setEnabled(request.enabled());
    audit.record(actorId, request.enabled() ? "USER_ENABLED" : "USER_DISABLED", "USER", targetId,
        http);
    return mapper.toAdmin(target);
  }
}
