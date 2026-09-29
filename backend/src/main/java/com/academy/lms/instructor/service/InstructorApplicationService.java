package com.academy.lms.instructor.service;

import com.academy.lms.common.api.PageResponse;
import com.academy.lms.common.audit.AuditService;
import com.academy.lms.common.exception.ApiException;
import com.academy.lms.instructor.dto.request.InstructorApplicationRequest;
import com.academy.lms.instructor.dto.request.InstructorDecisionRequest;
import com.academy.lms.instructor.dto.response.InstructorApplicationResponse;
import com.academy.lms.instructor.entity.InstructorApplication;
import com.academy.lms.instructor.entity.InstructorApplicationStatus;
import com.academy.lms.instructor.mapper.InstructorApplicationMapper;
import com.academy.lms.instructor.repository.InstructorApplicationRepository;
import com.academy.lms.user.entity.RoleName;
import com.academy.lms.user.entity.User;
import com.academy.lms.user.repository.RoleRepository;
import com.academy.lms.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstructorApplicationService {
  private final InstructorApplicationRepository applications;
  private final UserRepository users;
  private final RoleRepository roles;
  private final InstructorApplicationMapper mapper;
  private final AuditService audit;

  public InstructorApplicationService(InstructorApplicationRepository applications,
                                      UserRepository users, RoleRepository roles,
                                      InstructorApplicationMapper mapper, AuditService audit) {
    this.applications = applications;
    this.users = users;
    this.roles = roles;
    this.mapper = mapper;
    this.audit = audit;
  }

  @Transactional
  public InstructorApplicationResponse submit(UUID applicantId,
                                              InstructorApplicationRequest request,
                                              HttpServletRequest http) {
    User applicant = requireUser(applicantId);
    if (applicant.hasRole(RoleName.INSTRUCTOR)) {
      throw ApiException.conflict("You are already an instructor");
    }
    InstructorApplication application = applications.findByApplicantId(applicantId).orElse(null);
    if (application == null) {
      application = applications.save(new InstructorApplication(applicant, request.expertise(),
          request.motivation(), request.portfolioUrl()));
    } else if (application.getStatus() == InstructorApplicationStatus.REJECTED) {
      application.resubmit(request.expertise(), request.motivation(), request.portfolioUrl());
    } else {
      throw ApiException.conflict("An instructor application already exists");
    }
    audit.record(applicantId, "INSTRUCTOR_APPLICATION_SUBMITTED", "INSTRUCTOR_APPLICATION",
        application.getId(), http);
    return mapper.toResponse(application);
  }

  @Transactional(readOnly = true)
  public InstructorApplicationResponse mine(UUID applicantId) {
    return applications.findByApplicantId(applicantId).map(mapper::toResponse).orElse(null);
  }

  @Transactional(readOnly = true)
  public PageResponse<InstructorApplicationResponse> list(InstructorApplicationStatus status,
                                                         int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
        Sort.by(Sort.Direction.ASC, "createdAt"));
    var result = status == null ? applications.findAll(pageable)
        : applications.findByStatus(status, pageable);
    return PageResponse.from(result.map(mapper::toResponse));
  }

  @Transactional
  public InstructorApplicationResponse decide(UUID adminId, UUID applicationId,
                                              InstructorDecisionRequest request,
                                              HttpServletRequest http) {
    if (request.status() == InstructorApplicationStatus.PENDING) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_DECISION",
          "Decision must be APPROVED or REJECTED");
    }
    if (request.status() == InstructorApplicationStatus.REJECTED
        && (request.note() == null || request.note().isBlank())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "REVIEW_NOTE_REQUIRED",
          "A reason is required when rejecting an application");
    }
    InstructorApplication application = applications.findById(applicationId)
        .orElseThrow(() -> ApiException.notFound("Instructor application"));
    if (application.getStatus() != InstructorApplicationStatus.PENDING) {
      throw ApiException.conflict("This application has already been reviewed");
    }
    User admin = requireUser(adminId);
    application.decide(request.status(), request.note(), admin);
    if (request.status() == InstructorApplicationStatus.APPROVED) {
      application.getApplicant().addRole(roles.findByName(RoleName.INSTRUCTOR)
          .orElseThrow(() -> new IllegalStateException("INSTRUCTOR role is not configured")));
    }
    audit.record(adminId, "INSTRUCTOR_APPLICATION_" + request.status().name(),
        "INSTRUCTOR_APPLICATION", applicationId, http);
    return mapper.toResponse(application);
  }

  private User requireUser(UUID id) {
    return users.findById(id).orElseThrow(() -> ApiException.notFound("User"));
  }
}
