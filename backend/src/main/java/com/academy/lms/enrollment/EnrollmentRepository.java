package com.academy.lms.enrollment;import java.util.*;import org.springframework.data.jpa.repository.JpaRepository;
public interface EnrollmentRepository extends JpaRepository<Enrollment,UUID>{boolean existsByStudentIdAndCourseId(UUID studentId,UUID courseId);Optional<Enrollment> findByStudentIdAndCourseId(UUID studentId,UUID courseId);List<Enrollment> findByStudentIdOrderByEnrolledAtDesc(UUID studentId);long countByCourseId(UUID courseId);}

