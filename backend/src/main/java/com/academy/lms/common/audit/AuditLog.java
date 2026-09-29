package com.academy.lms.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "actor_id")
  private UUID actorId;

  @Column(nullable = false, length = 80)
  private String action;

  @Column(name = "resource_type", nullable = false, length = 80)
  private String resourceType;

  @Column(name = "resource_id", length = 100)
  private String resourceId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> details = new LinkedHashMap<>();

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  protected AuditLog() {}

  public AuditLog(UUID actorId, String action, String resourceType, Object resourceId,
                  Map<String, Object> details, String ipAddress) {
    this.actorId = actorId;
    this.action = action;
    this.resourceType = resourceType;
    this.resourceId = resourceId == null ? null : resourceId.toString();
    this.details = details == null ? new LinkedHashMap<>() : new LinkedHashMap<>(details);
    this.ipAddress = ipAddress;
  }

  public Long getId() { return id; }
  public UUID getActorId() { return actorId; }
  public String getAction() { return action; }
  public String getResourceType() { return resourceType; }
  public String getResourceId() { return resourceId; }
  public Map<String, Object> getDetails() { return Map.copyOf(details); }
  public String getIpAddress() { return ipAddress; }
  public Instant getCreatedAt() { return createdAt; }
}
