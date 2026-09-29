package com.academy.lms.common.audit;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
  private final AuditLogRepository repository;

  public AuditService(AuditLogRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public void record(UUID actorId, String action, String resourceType, Object resourceId,
                     HttpServletRequest request) {
    record(actorId, action, resourceType, resourceId, Map.of(), request);
  }

  @Transactional
  public void record(UUID actorId, String action, String resourceType, Object resourceId,
                     Map<String, Object> details, HttpServletRequest request) {
    repository.save(new AuditLog(actorId, action, resourceType, resourceId, details,
        request == null ? null : clientIp(request)));
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",", 2)[0].trim();
    return request.getRemoteAddr();
  }
}
