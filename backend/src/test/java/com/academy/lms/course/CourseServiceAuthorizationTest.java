package com.academy.lms.course;
import com.academy.lms.audit.AuditService;import com.academy.lms.category.CategoryRepository;import com.academy.lms.common.exception.ApiException;import com.academy.lms.enrollment.EnrollmentRepository;import com.academy.lms.user.*;import jakarta.servlet.http.HttpServletRequest;import java.util.*;import org.junit.jupiter.api.*;import org.junit.jupiter.api.extension.ExtendWith;import org.mockito.*;import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class) class CourseServiceAuthorizationTest {
 @Mock CourseRepository courses;@Mock CategoryRepository categories;@Mock UserRepository users;@Mock CourseSectionRepository sections;@Mock EnrollmentRepository enrollments;@Mock CourseMapper mapper;@Mock SlugService slugs;@Mock AuditService audit;@Mock HttpServletRequest request;@InjectMocks CourseService service;
 @Test void instructorCannotUpdateCourseTheyDoNotOwn(){UUID actor=UUID.randomUUID(),courseId=UUID.randomUUID();Course c=mock(Course.class);User owner=mock(User.class);when(owner.getId()).thenReturn(UUID.randomUUID());when(c.getInstructor()).thenReturn(owner);when(courses.findById(courseId)).thenReturn(Optional.of(c));var input=new CourseDtos.Upsert("Title","Short","Description",Course.Level.BEGINNER,null,null);assertEquals(403,assertThrows(ApiException.class,()->service.update(actor,false,courseId,input,request)).status().value());verify(c,never()).update(any(),any(),any(),any(),any(),any(),any());}
}

