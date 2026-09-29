package com.academy.lms.review;

import com.academy.lms.common.audit.AuditService;
import com.academy.lms.common.exception.ApiException;
import com.academy.lms.course.entity.Course;
import com.academy.lms.course.repository.CourseRepository;
import com.academy.lms.enrollment.repository.EnrollmentRepository;
import com.academy.lms.review.dto.request.CreateReviewRequest;
import com.academy.lms.review.dto.request.ModerateReviewRequest;
import com.academy.lms.review.dto.request.UpdateReviewRequest;
import com.academy.lms.review.entity.Review;
import com.academy.lms.review.entity.ReviewStatus;
import com.academy.lms.review.mapper.ReviewMapper;
import com.academy.lms.review.repository.ReviewRepository;
import com.academy.lms.review.security.authorization.ReviewAuthorizationService;
import com.academy.lms.review.service.ReviewService;
import com.academy.lms.review.validation.ReviewModerationValidator;
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
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceAuthorizationTest {
  @Mock ReviewRepository reviews;
  @Mock CourseRepository courses;
  @Mock UserRepository users;
  @Mock EnrollmentRepository enrollments;
  @Mock ReviewMapper mapper;
  @Spy ReviewAuthorizationService authorization = new ReviewAuthorizationService();
  @Spy ReviewModerationValidator validator = new ReviewModerationValidator();
  @Mock AuditService audit;
  @Mock HttpServletRequest http;
  @InjectMocks ReviewService service;

  @Test
  void nonEnrolledStudentCannotReview() {
    UUID actorId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    when(enrollments.existsByStudentIdAndCourseId(actorId, courseId)).thenReturn(false);

    ApiException error = assertThrows(ApiException.class,
        () -> service.create(actorId, new CreateReviewRequest(courseId, (short) 5, "Useful"), http));

    assertEquals(403, error.status().value());
    verifyNoInteractions(courses);
  }

  @Test
  void studentCannotEditAnotherStudentsReview() {
    UUID actorId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    Review review = mock(Review.class);
    User owner = mock(User.class);
    when(owner.getId()).thenReturn(UUID.randomUUID());
    when(review.getStudent()).thenReturn(owner);
    when(reviews.findById(reviewId)).thenReturn(Optional.of(review));

    ApiException error = assertThrows(ApiException.class,
        () -> service.update(actorId, reviewId,
            new UpdateReviewRequest((short) 1, "Tamper"), http));

    assertEquals(403, error.status().value());
    verify(review, never()).update(anyShort(), anyString());
  }

  @Test
  void instructorCannotModerateAnotherInstructorsCourse() {
    UUID actorId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    Review review = mock(Review.class);
    Course course = mock(Course.class);
    User owner = mock(User.class);
    when(owner.getId()).thenReturn(UUID.randomUUID());
    when(course.getInstructor()).thenReturn(owner);
    when(review.getCourse()).thenReturn(course);
    when(reviews.findById(reviewId)).thenReturn(Optional.of(review));

    ApiException error = assertThrows(ApiException.class,
        () -> service.moderate(actorId, false, reviewId,
            new ModerateReviewRequest(ReviewStatus.HIDDEN, "Spam"), http));

    assertEquals(403, error.status().value());
    verify(review, never()).moderate(any(), any(), any());
  }
}
