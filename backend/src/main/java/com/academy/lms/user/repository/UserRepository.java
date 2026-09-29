package com.academy.lms.user.repository;

import com.academy.lms.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.academy.lms.user.entity.RoleName;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
  Optional<User> findByEmailIgnoreCase(String email);
  boolean existsByEmailIgnoreCase(String email);
  Page<User> findByEmailContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
      String email, String displayName, Pageable pageable);

  @Query("select count(distinct u) from User u join u.roles r where u.enabled = true and r.name = :role")
  long countEnabledByRole(@Param("role") RoleName role);
}
