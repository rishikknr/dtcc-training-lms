package com.academy.lms.user;
import com.academy.lms.common.domain.AuditedEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
@Entity @Table(name="users")
public class User extends AuditedEntity {
  @Column(nullable=false,unique=true,columnDefinition="citext") private String email;
  @Column(name="password_hash",nullable=false) private String passwordHash;
  @Column(name="display_name",nullable=false,length=100) private String displayName;
  @Column(length=500) private String bio; @Column(name="avatar_url",length=500) private String avatarUrl;
  @Column(nullable=false) private boolean enabled=true;
  @Column(name="failed_login_attempts",nullable=false) private int failedLoginAttempts;
  @Column(name="locked_until") private Instant lockedUntil;
  @ManyToMany(fetch=FetchType.EAGER) @JoinTable(name="user_roles",joinColumns=@JoinColumn(name="user_id"),inverseJoinColumns=@JoinColumn(name="role_id")) private Set<Role> roles=new HashSet<>();
  protected User(){} public User(String email,String hash,String name,Role role){this.email=email.toLowerCase(Locale.ROOT);passwordHash=hash;displayName=name;roles.add(role);}
  public String getEmail(){return email;} public String getPasswordHash(){return passwordHash;} public String getDisplayName(){return displayName;} public String getBio(){return bio;} public String getAvatarUrl(){return avatarUrl;} public boolean isEnabled(){return enabled;} public Set<Role> getRoles(){return Set.copyOf(roles);} public int getFailedLoginAttempts(){return failedLoginAttempts;} public Instant getLockedUntil(){return lockedUntil;}
  public void updateProfile(String name,String bio,String avatar){displayName=name;this.bio=bio;avatarUrl=avatar;}
  public void changePassword(String hash){passwordHash=hash;}
  public void loginFailed(){failedLoginAttempts++;if(failedLoginAttempts>=5)lockedUntil=Instant.now().plusSeconds(900);}
  public void loginSucceeded(){failedLoginAttempts=0;lockedUntil=null;}
}

