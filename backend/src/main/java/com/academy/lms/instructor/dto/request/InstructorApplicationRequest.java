package com.academy.lms.instructor.dto.request;

import com.academy.lms.common.validation.ValidHttpUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InstructorApplicationRequest(
    @NotBlank @Size(min = 20, max = 500) String expertise,
    @NotBlank @Size(min = 50, max = 2000) String motivation,
    @Size(max = 500) @ValidHttpUrl String portfolioUrl
) {}
