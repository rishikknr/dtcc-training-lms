package com.academy.lms.audit;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="audit_logs") public class AuditLog {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="actor_id") private UUID actorId; @Column(nullable=false) private String action;
  @Column(name="resource_type",nullable=false) private String resourceType; @Column(name="resource_id") private String resourceId;
  @JdbcTypeCode(SqlTypes.JSON) @Column(nullable=false,columnDefinition="jsonb") private Map<String,Object> details=new LinkedHashMap<>(); @Column(name="ip_address",length=45) private String ipAddress;
  @Column(name="created_at",nullable=false) private Instant createdAt=Instant.now(); protected AuditLog(){}
  public AuditLog(UUID actorId,String action,String type,String resourceId,String ip){this.actorId=actorId;this.action=action;resourceType=type;this.resourceId=resourceId;ipAddress=ip;}
}
