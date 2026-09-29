package com.academy.lms.course;

import com.academy.lms.category.service.CategoryService;
import com.academy.lms.common.audit.AuditService;
import com.academy.lms.common.exception.ApiException;
import com.academy.lms.course.dto.request.AssignInstructorRequest;
import com.academy.lms.course.dto.request.CourseUpsertRequest;
import com.academy.lms.course.entity.Course;
import com.academy.lms.course.entity.CourseLevel;
import com.academy.lms.course.mapper.CourseMapper;
import com.academy.lms.course.repository.CourseRepository;
import com.academy.lms.course.security.authorization.CourseAuthorizationService;
import com.academy.lms.course.service.CourseService;
import com.academy.lms.course.service.SlugService;
import com.academy.lms.course.validation.CoursePublishingValidator;
import com.academy.lms.enrollment.repository.EnrollmentRepository;
import com.academy.lms.user.entity.RoleName;
import com.academy.lms.user.entity.User;
import com.academy.lms.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceAuthorizationTest {
  @Mock CourseRepository courses;
  @Mock CategoryService categories;
  @Mock UserRepository users;
  @Mock EnrollmentRepository enrollments;
  @Mock CourseMapper mapper;
  @Mock SlugService slugs;
  @Spy CourseAuthorizationService authorization = new CourseAuthorizationService();
  @Mock CoursePublishingValidator publishing;
  @Mock AuditService audit;
  @Mock HttpServletRequest http;
  @InjectMocks CourseService service;

  @Test
  void instructorCannotUpdateCourseTheyDoNotOwn() {
    UUID actorId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    Course course = mock(Course.class);
    User owner = mock(User.class);
    when(owner.getId()).thenReturn(UUID.randomUUID());
    when(course.getInstructor()).thenReturn(owner);
    when(courses.findById(courseId)).thenReturn(Optional.of(course));
    var request = new CourseUpsertRequest("Title", "Short", "Description",
        CourseLevel.BEGINNER, null, null, null, null, null, "English");

    ApiException error = assertThrows(ApiException.class,
        () -> service.update(actorId, false, courseId, request, http));

    assertEquals(403, error.status().value());
    verify(course, never()).update(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void adminCannotAssignCourseToUnapprovedUser() {
    UUID courseId = UUID.randomUUID();
    UUID candidateId = UUID.randomUUID();
    Course course = mock(Course.class);
    User candidate = mock(User.class);
    when(courses.findById(courseId)).thenReturn(Optional.of(course));
    when(users.findById(candidateId)).thenReturn(Optional.of(candidate));
    when(candidate.hasRole(RoleName.INSTRUCTOR)).thenReturn(false);

    ApiException error = assertThrows(ApiException.class,
        () -> service.assignInstructor(UUID.randomUUID(), courseId,
            new AssignInstructorRequest(candidateId), http));

    assertEquals(409, error.status().value());
    verify(course, never()).assignInstructor(any());
  }
}
