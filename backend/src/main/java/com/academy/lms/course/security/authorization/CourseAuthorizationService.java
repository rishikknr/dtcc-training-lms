package com.academy.lms.course.security.authorization;

import com.academy.lms.common.exception.ApiException;
import com.academy.lms.course.entity.Course;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CourseAuthorizationService {
  public void requireManage(Course course, UUID actorId, boolean admin) {
    if (!admin && !course.getInstructor().getId().equals(actorId)) throw ApiException.forbidden();
  }

  public boolean canManage(Course course, UUID actorId, boolean admin) {
    return admin || (actorId != null && course.getInstructor().getId().equals(actorId));
  }
}
