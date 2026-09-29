package com.academy.lms.user.service;

import com.academy.lms.common.api.PageResponse;
import com.academy.lms.common.audit.AuditService;
import com.academy.lms.common.exception.ApiException;
import com.academy.lms.user.dto.request.UpdateUserStatusRequest;
import com.academy.lms.user.dto.request.UpdateUserRolesRequest;
import com.academy.lms.user.dto.response.AdminUserResponse;
import com.academy.lms.user.entity.RoleName;
import com.academy.lms.user.entity.User;
import com.academy.lms.user.mapper.UserMapper;
import com.academy.lms.user.repository.UserRepository;
import com.academy.lms.user.repository.RoleRepository;
import com.academy.lms.course.repository.CourseRepository;
import com.academy.lms.instructor.repository.InstructorApplicationRepository;
import com.academy.lms.instructor.entity.InstructorApplicationStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {
  private final UserRepository users;
  private final UserMapper mapper;
  private final AuditService audit;
  private final RoleRepository roles;
  private final CourseRepository courses;
  private final InstructorApplicationRepository applications;

  public AdminUserService(UserRepository users, UserMapper mapper, AuditService audit,
                          RoleRepository roles, CourseRepository courses,
                          InstructorApplicationRepository applications) {
    this.users = users;
    this.mapper = mapper;
    this.audit = audit;
    this.roles = roles;
    this.courses = courses;
    this.applications = applications;
  }

  @Transactional(readOnly = true)
  public PageResponse<AdminUserResponse> list(String query, RoleName role, Boolean enabled,
                                               int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
        Sort.by(Sort.Direction.DESC, "createdAt"));
    Specification<User> specification = (root, cq, builder) -> {
      var predicates = new ArrayList<Predicate>();
      if (query != null && !query.isBlank()) {
        String term = "%" + query.trim().toLowerCase() + "%";
        predicates.add(builder.or(builder.like(builder.lower(root.get("email")), term),
            builder.like(builder.lower(root.get("displayName")), term)));
      }
      if (enabled != null) predicates.add(builder.equal(root.get("enabled"), enabled));
      if (role != null) predicates.add(builder.equal(root.join("roles").get("name"), role));
      cq.distinct(true);
      return builder.and(predicates.toArray(Predicate[]::new));
    };
    var result = users.findAll(specification, pageable);
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

  @Transactional
  public AdminUserResponse updateRoles(UUID actorId, UUID targetId, UpdateUserRolesRequest request,
                                       HttpServletRequest http) {
    User target = users.findById(targetId).orElseThrow(() -> ApiException.notFound("User"));
    var requested = request.roles();
    boolean removingAdmin = target.hasRole(RoleName.ADMIN) && !requested.contains(RoleName.ADMIN);
    if (actorId.equals(targetId) && removingAdmin) {
      throw ApiException.conflict("Administrators cannot remove their own admin role");
    }
    if (removingAdmin && users.countEnabledByRole(RoleName.ADMIN) <= 1) {
      throw ApiException.conflict("The last enabled admin cannot lose the admin role");
    }
    if (requested.contains(RoleName.INSTRUCTOR) && !target.hasRole(RoleName.INSTRUCTOR)) {
      boolean approved = applications.findByApplicantId(targetId)
          .map(application -> application.getStatus() == InstructorApplicationStatus.APPROVED)
          .orElse(false);
      if (!approved) throw ApiException.conflict(
          "Instructor access requires an approved instructor application");
    }
    if (target.hasRole(RoleName.INSTRUCTOR) && !requested.contains(RoleName.INSTRUCTOR)
        && courses.countByInstructorId(targetId) > 0) {
      throw ApiException.conflict("Reassign or archive this instructor's courses before removing the role");
    }
    for (RoleName role : RoleName.values()) {
      if (!requested.contains(role)) target.removeRole(role);
    }
    requested.forEach(role -> {
      if (!target.hasRole(role)) target.addRole(roles.findByName(role)
          .orElseThrow(() -> new IllegalStateException(role + " role is not configured")));
    });
    audit.record(actorId, "USER_ROLES_UPDATED", "USER", targetId,
        java.util.Map.of("roles", requested.stream().map(Enum::name).sorted().toList()), http);
    return mapper.toAdmin(target);
  }
}
