package com.academy.lms.user.entity;

import com.academy.lms.common.domain.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "users")
public class User extends AuditedEntity {
  @Column(nullable = false, unique = true, columnDefinition = "citext")
  private String email;

  @Column(name = "password_hash", nullable = false, length = 100)
  private String passwordHash;

  @Column(name = "display_name", nullable = false, length = 100)
  private String displayName;

  @Column(length = 500)
  private String bio;

  @Column(name = "avatar_url", length = 500)
  private String avatarUrl;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(name = "failed_login_attempts", nullable = false)
  private int failedLoginAttempts;

  @Column(name = "locked_until")
  private Instant lockedUntil;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  protected User() {}

  public User(String email, String passwordHash, String displayName, Role initialRole) {
    this.email = email.trim().toLowerCase(Locale.ROOT);
    this.passwordHash = passwordHash;
    this.displayName = displayName.trim();
    this.roles.add(initialRole);
  }

  public String getEmail() { return email; }
  public String getPasswordHash() { return passwordHash; }
  public String getDisplayName() { return displayName; }
  public String getBio() { return bio; }
  public String getAvatarUrl() { return avatarUrl; }
  public boolean isEnabled() { return enabled; }
  public Set<Role> getRoles() { return Set.copyOf(roles); }
  public int getFailedLoginAttempts() { return failedLoginAttempts; }
  public Instant getLockedUntil() { return lockedUntil; }

  public boolean hasRole(RoleName role) {
    return roles.stream().anyMatch(existing -> existing.getName() == role);
  }

  public void addRole(Role role) { roles.add(role); }
  public void removeRole(RoleName role) { roles.removeIf(existing -> existing.getName() == role); }
  public void setEnabled(boolean enabled) { this.enabled = enabled; }

  public void updateProfile(String displayName, String bio, String avatarUrl) {
    this.displayName = displayName.trim();
    this.bio = normalizeNullable(bio);
    this.avatarUrl = normalizeNullable(avatarUrl);
  }

  public void changePassword(String passwordHash) { this.passwordHash = passwordHash; }

  public void loginFailed() {
    failedLoginAttempts++;
    if (failedLoginAttempts >= 5) lockedUntil = Instant.now().plusSeconds(900);
  }

  public void loginSucceeded() {
    failedLoginAttempts = 0;
    lockedUntil = null;
  }

  private String normalizeNullable(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
