package com.academy.lms.review;import java.math.BigDecimal;import java.util.*;import org.springframework.data.domain.*;import org.springframework.data.jpa.repository.*;import org.springframework.data.repository.query.Param;
public interface ReviewRepository extends JpaRepository<Review,UUID>{boolean existsByStudentIdAndCourseId(UUID s,UUID c);Page<Review> findByCourseIdAndStatus(UUID course,Review.Status status,Pageable p);
 @Query("select avg(r.rating),count(r) from Review r where r.course.id=:course and r.status=com.academy.lms.review.Review.Status.PUBLISHED")Object[] aggregate(@Param("course")UUID course);
}

