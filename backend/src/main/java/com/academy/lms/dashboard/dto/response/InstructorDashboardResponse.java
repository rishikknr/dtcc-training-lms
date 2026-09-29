package com.academy.lms.dashboard.dto.response;

public record InstructorDashboardResponse(long courses, long publishedCourses, long learners,
                                          double averageRating, int averageCompletion,
                                          long reviews) {}
