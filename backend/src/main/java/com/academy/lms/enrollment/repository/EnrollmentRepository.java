package com.academy.lms.enrollment.repository;

import com.academy.lms.enrollment.entity.Enrollment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
  boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);
  Optional<Enrollment> findByStudentIdAndCourseId(UUID studentId, UUID courseId);
  List<Enrollment> findByStudentIdOrderByLastAccessedAtDesc(UUID studentId);
  long countByStudentId(UUID studentId);
  long countByCourseId(UUID courseId);

  @Query("select count(e) from Enrollment e where e.course.instructor.id = :instructorId")
  long countByInstructor(@Param("instructorId") UUID instructorId);

  @Query("select coalesce(avg(e.progress), 0) from Enrollment e where e.course.instructor.id = :instructorId")
  double averageProgressByInstructor(@Param("instructorId") UUID instructorId);
}
