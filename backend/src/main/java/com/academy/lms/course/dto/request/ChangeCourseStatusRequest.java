package com.academy.lms.course.dto.request;

import com.academy.lms.course.entity.CourseStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeCourseStatusRequest(@NotNull CourseStatus status) {}
