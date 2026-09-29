package com.academy.lms.dashboard.dto.response;

public record AdminDashboardResponse(long users, long courses, long enrollments, long reviews,
                                     long pendingInstructorApplications, long hiddenReviews) {}
