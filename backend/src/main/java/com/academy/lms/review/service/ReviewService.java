package com.academy.lms.review.service;

import com.academy.lms.common.api.PageResponse;
import com.academy.lms.common.audit.AuditService;
import com.academy.lms.common.exception.ApiException;
import com.academy.lms.course.entity.Course;
import com.academy.lms.course.repository.CourseRepository;
import com.academy.lms.enrollment.repository.EnrollmentRepository;
import com.academy.lms.enrollment.entity.EnrollmentStatus;
import com.academy.lms.review.dto.request.CreateReviewRequest;
import com.academy.lms.review.dto.request.ModerateReviewRequest;
import com.academy.lms.review.dto.request.UpdateReviewRequest;
import com.academy.lms.review.dto.response.ReviewResponse;
import com.academy.lms.review.dto.response.ReviewSummaryResponse;
import com.academy.lms.review.entity.Review;
import com.academy.lms.review.entity.ReviewStatus;
import com.academy.lms.review.mapper.ReviewMapper;
import com.academy.lms.review.repository.ReviewRepository;
import com.academy.lms.review.security.authorization.ReviewAuthorizationService;
import com.academy.lms.review.validation.ReviewModerationValidator;
import com.academy.lms.user.entity.User;
import com.academy.lms.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.UUID;
import java.util.LinkedHashMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
  private final ReviewRepository reviews;
  private final CourseRepository courses;
  private final UserRepository users;
  private final EnrollmentRepository enrollments;
  private final ReviewMapper mapper;
  private final ReviewAuthorizationService authorization;
  private final ReviewModerationValidator moderationValidator;
  private final AuditService audit;

  public ReviewService(ReviewRepository reviews, CourseRepository courses, UserRepository users,
                       EnrollmentRepository enrollments, ReviewMapper mapper,
                       ReviewAuthorizationService authorization,
                       ReviewModerationValidator moderationValidator, AuditService audit) {
    this.reviews = reviews;
    this.courses = courses;
    this.users = users;
    this.enrollments = enrollments;
    this.mapper = mapper;
    this.authorization = authorization;
    this.moderationValidator = moderationValidator;
    this.audit = audit;
  }

  @Transactional
  public ReviewResponse create(UUID actorId, CreateReviewRequest request, HttpServletRequest http) {
    if (!enrollments.existsByStudentIdAndCourseIdAndStatus(
        actorId, request.courseId(), EnrollmentStatus.ACTIVE)) {
      throw ApiException.forbidden();
    }
    if (reviews.existsByStudentIdAndCourseId(actorId, request.courseId())) {
      throw ApiException.conflict("You have already reviewed this course");
    }
    Course course = courses.findById(request.courseId())
        .orElseThrow(() -> ApiException.notFound("Course"));
    User student = users.findById(actorId).orElseThrow(() -> ApiException.notFound("User"));
    Review review = reviews.save(new Review(student, course, request.rating(), request.comment()));
    recalculate(course);
    audit.record(actorId, "REVIEW_CREATED", "REVIEW", review.getId(), http);
    return mapper.toResponse(review);
  }

  @Transactional(readOnly = true)
  public PageResponse<ReviewResponse> listPublished(UUID courseId, int page, int size,
                                                    String sort) {
    Sort ordering = switch (sort == null ? "newest" : sort) {
      case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
      case "highest" -> Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.DESC, "createdAt"));
      case "lowest" -> Sort.by(Sort.Direction.ASC, "rating").and(Sort.by(Sort.Direction.DESC, "createdAt"));
      default -> Sort.by(Sort.Direction.DESC, "createdAt");
    };
    var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
        ordering);
    return PageResponse.from(reviews.findByCourseIdAndStatus(courseId, ReviewStatus.PUBLISHED,
        pageable).map(mapper::toResponse));
  }

  @Transactional(readOnly = true)
  public PageResponse<ReviewResponse> mine(UUID studentId, int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
        Sort.by(Sort.Direction.DESC, "updatedAt"));
    return PageResponse.from(reviews.findByStudentId(studentId, pageable).map(mapper::toResponse));
  }

  @Transactional(readOnly = true)
  public ReviewSummaryResponse summary(UUID courseId) {
    Object[] aggregate = reviews.aggregate(courseId);
    Number average = aggregate != null && aggregate.length > 0 && aggregate[0] != null
        ? (Number) aggregate[0] : BigDecimal.ZERO;
    Number count = aggregate != null && aggregate.length > 1 && aggregate[1] != null
        ? (Number) aggregate[1] : 0;
    var distribution = new LinkedHashMap<Integer, Long>();
    for (int rating = 5; rating >= 1; rating--) distribution.put(rating, 0L);
    reviews.distribution(courseId).forEach(row ->
        distribution.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue()));
    return new ReviewSummaryResponse(
        BigDecimal.valueOf(average.doubleValue()).setScale(2, RoundingMode.HALF_UP),
        count.longValue(), distribution);
  }

  @Transactional(readOnly = true)
  public PageResponse<ReviewResponse> moderationQueue(UUID actorId, boolean admin, UUID courseId,
                                                     ReviewStatus status, int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
        Sort.by(Sort.Direction.DESC, "createdAt"));
    Specification<Review> specification = (root, query, builder) -> {
      var predicates = new ArrayList<Predicate>();
      if (!admin) predicates.add(builder.equal(root.get("course").get("instructor").get("id"), actorId));
      if (courseId != null) predicates.add(builder.equal(root.get("course").get("id"), courseId));
      if (status != null) predicates.add(builder.equal(root.get("status"), status));
      return builder.and(predicates.toArray(Predicate[]::new));
    };
    return PageResponse.from(reviews.findAll(specification, pageable).map(mapper::toResponse));
  }

  @Transactional
  public ReviewResponse update(UUID actorId, UUID reviewId, UpdateReviewRequest request,
                               HttpServletRequest http) {
    Review review = require(reviewId);
    authorization.requireOwner(review, actorId);
    review.update(request.rating(), request.comment());
    recalculate(review.getCourse());
    audit.record(actorId, "REVIEW_UPDATED", "REVIEW", reviewId, http);
    return mapper.toResponse(review);
  }

  @Transactional
  public void delete(UUID actorId, boolean admin, UUID reviewId, HttpServletRequest http) {
    Review review = require(reviewId);
    if (!admin) authorization.requireOwner(review, actorId);
    Course course = review.getCourse();
    reviews.delete(review);
    reviews.flush();
    recalculate(course);
    audit.record(actorId, "REVIEW_DELETED", "REVIEW", reviewId, http);
  }

  @Transactional
  public ReviewResponse moderate(UUID actorId, boolean admin, UUID reviewId,
                                 ModerateReviewRequest request, HttpServletRequest http) {
    moderationValidator.validate(request);
    Review review = require(reviewId);
    authorization.requireModerator(review, actorId, admin);
    User moderator = users.findById(actorId).orElseThrow(() -> ApiException.notFound("User"));
    review.moderate(request.status(), request.reason(), moderator);
    recalculate(review.getCourse());
    audit.record(actorId, "REVIEW_MODERATED", "REVIEW", reviewId,
        java.util.Map.of("status", request.status().name()), http);
    return mapper.toResponse(review);
  }

  private Review require(UUID id) {
    return reviews.findById(id).orElseThrow(() -> ApiException.notFound("Review"));
  }

  private void recalculate(Course course) {
    reviews.flush();
    Object[] aggregate = reviews.aggregate(course.getId());
    Number average = aggregate != null && aggregate.length > 0 && aggregate[0] != null
        ? (Number) aggregate[0] : BigDecimal.ZERO;
    Number count = aggregate != null && aggregate.length > 1 && aggregate[1] != null
        ? (Number) aggregate[1] : 0;
    course.updateRating(BigDecimal.valueOf(average.doubleValue()).setScale(2, RoundingMode.HALF_UP),
        count.intValue());
  }
}
