package com.academy.lms.course.repository;

import com.academy.lms.course.entity.Course;
import com.academy.lms.course.entity.CourseStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course> {
  boolean existsBySlug(String slug);
  long countByInstructorId(UUID instructorId);
  long countByInstructorIdAndStatus(UUID instructorId, CourseStatus status);
  Page<Course> findByInstructorId(UUID instructorId, Pageable pageable);

  @Query("select coalesce(avg(c.averageRating), 0) from Course c where c.instructor.id = :instructorId and c.ratingCount > 0")
  double averageRatingByInstructor(@Param("instructorId") UUID instructorId);
}
