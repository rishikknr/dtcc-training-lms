package com.academy.lms.instructor.repository;

import com.academy.lms.instructor.entity.InstructorApplication;
import com.academy.lms.instructor.entity.InstructorApplicationStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructorApplicationRepository extends JpaRepository<InstructorApplication, UUID> {
  Optional<InstructorApplication> findByApplicantId(UUID applicantId);
  Page<InstructorApplication> findByStatus(InstructorApplicationStatus status, Pageable pageable);
  long countByStatus(InstructorApplicationStatus status);
}
