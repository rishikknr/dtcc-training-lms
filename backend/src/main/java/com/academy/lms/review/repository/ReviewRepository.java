package com.academy.lms.review.repository;

import com.academy.lms.review.entity.Review;
import com.academy.lms.review.entity.ReviewStatus;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID>, JpaSpecificationExecutor<Review> {
  boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);
  Page<Review> findByCourseIdAndStatus(UUID courseId, ReviewStatus status, Pageable pageable);
  Page<Review> findByStudentId(UUID studentId, Pageable pageable);
  long countByCourseInstructorId(UUID instructorId);
  long countByStatus(ReviewStatus status);

  @Query("select avg(r.rating), count(r) from Review r where r.course.id = :courseId "
      + "and r.status = com.academy.lms.review.entity.ReviewStatus.PUBLISHED")
  Object[] aggregate(@Param("courseId") UUID courseId);

  @Query("select r.rating, count(r) from Review r where r.course.id = :courseId "
      + "and r.status = com.academy.lms.review.entity.ReviewStatus.PUBLISHED group by r.rating")
  List<Object[]> distribution(@Param("courseId") UUID courseId);
}
