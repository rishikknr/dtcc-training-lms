package com.academy.lms.enrollment.repository;

import com.academy.lms.enrollment.entity.Enrollment;
import com.academy.lms.enrollment.entity.EnrollmentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
  boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);
  boolean existsByStudentIdAndCourseIdAndStatus(UUID studentId, UUID courseId,
                                                 EnrollmentStatus status);
  Optional<Enrollment> findByStudentIdAndCourseId(UUID studentId, UUID courseId);
  Optional<Enrollment> findByStudentIdAndCourseIdAndStatus(UUID studentId, UUID courseId,
                                                            EnrollmentStatus status);
  List<Enrollment> findByStudentIdOrderByLastAccessedAtDesc(UUID studentId);
  List<Enrollment> findByStudentIdAndStatusOrderByLastAccessedAtDesc(UUID studentId,
                                                                     EnrollmentStatus status);
  Page<Enrollment> findByCourseId(UUID courseId, Pageable pageable);
  List<Enrollment> findByCourseId(UUID courseId);
  long countByStudentId(UUID studentId);
  long countByCourseId(UUID courseId);

  @Query("select count(e) from Enrollment e where e.course.instructor.id = :instructorId "
      + "and e.status = com.academy.lms.enrollment.entity.EnrollmentStatus.ACTIVE")
  long countByInstructor(@Param("instructorId") UUID instructorId);

  @Query("select coalesce(avg(e.progress), 0) from Enrollment e where e.course.instructor.id = :instructorId "
      + "and e.status = com.academy.lms.enrollment.entity.EnrollmentStatus.ACTIVE")
  double averageProgressByInstructor(@Param("instructorId") UUID instructorId);
}
