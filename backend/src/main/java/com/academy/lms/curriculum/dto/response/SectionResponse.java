package com.academy.lms.curriculum.dto.response;

import java.util.List;
import java.util.UUID;

public record SectionResponse(UUID id, String title, int position, List<LessonResponse> lessons) {}
