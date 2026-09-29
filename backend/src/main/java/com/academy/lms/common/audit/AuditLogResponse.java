package com.academy.lms.common.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(Long id, UUID actorId, String action, String resourceType,
                               String resourceId, Map<String, Object> details,
                               String ipAddress, Instant createdAt) {
  static AuditLogResponse from(AuditLog log) {
    return new AuditLogResponse(log.getId(), log.getActorId(), log.getAction(),
        log.getResourceType(), log.getResourceId(), log.getDetails(), log.getIpAddress(),
        log.getCreatedAt());
  }
}
