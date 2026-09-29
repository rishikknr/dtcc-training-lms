package com.academy.lms.dashboard.dto.response;

public record StudentDashboardResponse(long enrolledCourses, long completedCourses,
                                       int averageProgress) {}
