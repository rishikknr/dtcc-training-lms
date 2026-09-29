package com.academy.lms.common.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditedEntity {
  @Id protected UUID id;
  @CreatedDate @Column(nullable=false, updatable=false) protected Instant createdAt;
  @LastModifiedDate @Column(nullable=false) protected Instant updatedAt;
  @Version protected long version;
  @PrePersist void assignId() { if (id == null) id = UUID.randomUUID(); }
  public UUID getId(){return id;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}

