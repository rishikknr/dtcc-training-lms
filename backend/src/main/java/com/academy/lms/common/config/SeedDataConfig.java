package com.academy.lms.common.config;

import com.academy.lms.category.entity.Category;
import com.academy.lms.category.repository.CategoryRepository;
import com.academy.lms.course.entity.Course;
import com.academy.lms.course.entity.CourseLevel;
import com.academy.lms.course.entity.CourseStatus;
import com.academy.lms.course.repository.CourseRepository;
import com.academy.lms.curriculum.entity.CourseSection;
import com.academy.lms.curriculum.entity.Lesson;
import com.academy.lms.curriculum.repository.CourseSectionRepository;
import com.academy.lms.enrollment.entity.Enrollment;
import com.academy.lms.enrollment.repository.EnrollmentRepository;
import com.academy.lms.review.entity.Review;
import com.academy.lms.review.repository.ReviewRepository;
import com.academy.lms.user.entity.RoleName;
import com.academy.lms.user.entity.User;
import com.academy.lms.user.repository.RoleRepository;
import com.academy.lms.user.repository.UserRepository;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ConditionalOnProperty(name = "app.seed-enabled", havingValue = "true")
public class SeedDataConfig {
  @Bean
  ApplicationRunner seedData(
      UserRepository users, RoleRepository roles, CategoryRepository categories,
      CourseRepository courses, CourseSectionRepository sections,
      EnrollmentRepository enrollments, ReviewRepository reviews, PasswordEncoder passwords,
      TransactionTemplate transaction,
      @Value("${app.seed-password}") String seedPassword) {
    return args -> transaction.executeWithoutResult(status -> {
      if (users.count() > 0) return;
      if (seedPassword == null || seedPassword.length() < 12) {
        throw new IllegalStateException(
            "SEED_PASSWORD must contain at least 12 characters when seed data is enabled");
      }

      var studentRole = roles.findByName(RoleName.STUDENT).orElseThrow();
      var instructorRole = roles.findByName(RoleName.INSTRUCTOR).orElseThrow();
      var adminRole = roles.findByName(RoleName.ADMIN).orElseThrow();
      String passwordHash = passwords.encode(seedPassword);
      User student = users.save(new User("student@academy.local", passwordHash, "Maya Chen",
          studentRole));
      User instructor = users.save(new User("instructor@academy.local", passwordHash,
          "Dr. Alex Morgan", instructorRole));
      users.save(new User("admin@academy.local", passwordHash, "Platform Admin", adminRole));

      Category security = categories.save(new Category("Cybersecurity", "cybersecurity",
          "Defensive security, risk, and secure engineering"));
      categories.save(new Category("Cloud Engineering", "cloud-engineering",
          "Build resilient modern infrastructure"));
      categories.save(new Category("Leadership", "leadership",
          "Lead teams and shape healthy cultures"));

      Course course = new Course(instructor, security, "Security by Design",
          "security-by-design", "Build systems that are resilient from the first commit.",
          "Learn threat modeling, secure architecture, identity boundaries, and pragmatic "
              + "defense in depth through applied workshops.", CourseLevel.INTERMEDIATE,
          "https://images.unsplash.com/photo-1563013544-824ae1b704d3?auto=format&fit=crop&w=1200&q=80");
      course.changeStatus(CourseStatus.PUBLISHED);
      courses.save(course);
      CourseSection section = new CourseSection(course, "Foundations", 0);
      section.addLesson(new Lesson(section, "Welcome and course map",
          "This course turns security principles into repeatable engineering decisions.",
          null, 0, 8, true));
      section.addLesson(new Lesson(section, "Modeling trust boundaries",
          "Identify assets, entry points, actors, and data flows before choosing controls.",
          null, 1, 24, false));
      sections.save(section);
      course.addSection(section);

      enrollments.save(new Enrollment(student, course));
      reviews.save(new Review(student, course, (short) 5,
          "Clear, practical, and immediately useful for architecture reviews."));
      course.updateRating(BigDecimal.valueOf(5), 1);
    });
  }
}
