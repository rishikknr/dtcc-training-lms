package com.academy.lms.course;
import java.util.UUID;import org.springframework.data.jpa.repository.*;import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface CourseRepository extends JpaRepository<Course,UUID>,JpaSpecificationExecutor<Course>{boolean existsBySlug(String slug);}

