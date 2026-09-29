package com.academy.lms;

import com.academy.lms.course.repository.CourseRepository;
import com.academy.lms.curriculum.repository.LessonRepository;
import com.academy.lms.review.repository.ReviewRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ApiSecurityIntegrationTest {
  private static final String TEST_PASSWORD = "Integration-Test-Password1!";

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("lms_test").withUsername("lms").withPassword("lms");

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("app.seed-enabled", () -> "true");
    registry.add("app.seed-password", () -> TEST_PASSWORD);
  }

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired CourseRepository courses;
  @Autowired ReviewRepository reviews;
  @Autowired LessonRepository lessons;

  @Test
  void unauthenticatedMutationIsRejected() throws Exception {
    mvc.perform(post("/api/courses").with(csrf()).contentType("application/json").content("{}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void studentCannotAccessAdminOrCreateCourse() throws Exception {
    MockHttpSession session = login("student@academy.local", TEST_PASSWORD);
    mvc.perform(get("/api/dashboard/admin").session(session)).andExpect(status().isForbidden());
    mvc.perform(post("/api/courses").session(session).with(csrf())
            .contentType("application/json").content(validCourse("Unauthorized course")))
        .andExpect(status().isForbidden());
  }

  @Test
  void clientCannotGrantItselfInstructorOrAdminRole() throws Exception {
    String email = "role-spoof-" + UUID.randomUUID() + "@example.test";
    String body = """
        {"email":"%s","password":"%s","displayName":"Attacker",
         "roles":["ADMIN","INSTRUCTOR"],"enabled":true}
        """.formatted(email, TEST_PASSWORD);
    MvcResult result = mvc.perform(post("/api/auth/register").with(csrf())
            .contentType("application/json").content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.user.roles.length()").value(1))
        .andExpect(jsonPath("$.user.roles[0]").value("STUDENT"))
        .andReturn();
    MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
    mvc.perform(get("/api/admin/users").session(session)).andExpect(status().isForbidden());
    mvc.perform(post("/api/courses").session(session).with(csrf())
            .contentType("application/json").content(validCourse("Spoofed course")))
        .andExpect(status().isForbidden());
  }

  @Test
  void instructorRoleRequiresAdminApproval() throws Exception {
    String email = "teacher-" + UUID.randomUUID() + "@example.test";
    MockHttpSession applicant = register(email, "Aspiring Teacher");
    String application = """
        {"expertise":"Secure software engineering and architecture reviews",
         "motivation":"I want to teach engineers how to apply secure design decisions in realistic delivery environments.",
         "portfolioUrl":"https://example.test/portfolio","status":"APPROVED"}
        """;
    MvcResult submitted = mvc.perform(post("/api/instructor-applications")
            .session(applicant).with(csrf()).contentType("application/json").content(application))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andReturn();
    String applicationId = json.readTree(submitted.getResponse().getContentAsString()).get("id").asText();

    mvc.perform(post("/api/courses").session(applicant).with(csrf())
            .contentType("application/json").content(validCourse("Before approval")))
        .andExpect(status().isForbidden());

    MockHttpSession admin = login("admin@academy.local", TEST_PASSWORD);
    mvc.perform(patch("/api/instructor-applications/{id}/decision", applicationId)
            .session(admin).with(csrf()).contentType("application/json")
            .content("{\"status\":\"APPROVED\",\"note\":\"Qualified\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));

    mvc.perform(post("/api/auth/refresh").session(applicant).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.roles").isArray());
    mvc.perform(post("/api/courses").session(applicant).with(csrf())
            .contentType("application/json").content(validCourse("After approval")))
        .andExpect(status().isCreated());
  }

  @Test
  void nonEnrolledUserCannotSpoofIdentityToReviewOrLearn() throws Exception {
    MockHttpSession outsider = register("outsider-" + UUID.randomUUID() + "@example.test", "Outsider");
    String courseId = reviews.findAll().getFirst().getCourse().getId().toString();
    String seededStudentId = reviews.findAll().getFirst().getStudent().getId().toString();
    mvc.perform(post("/api/reviews").session(outsider).with(csrf())
            .contentType("application/json")
            .content("{\"courseId\":\"%s\",\"rating\":5,\"comment\":\"spoof\",\"studentId\":\"%s\"}"
                .formatted(courseId, seededStudentId)))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/learning/courses/{id}", courseId).session(outsider))
        .andExpect(status().isForbidden());
  }

  @Test
  void csrfIsRequiredForAuthenticatedMutation() throws Exception {
    MockHttpSession session = login("student@academy.local", TEST_PASSWORD);
    mvc.perform(post("/api/courses/{id}/enroll", courses.findAll().getFirst().getId())
            .session(session))
        .andExpect(status().isForbidden());
  }

  @Test
  void duplicateReviewDatabaseRuleIsSurfacedAsConflict() throws Exception {
    MockHttpSession session = login("student@academy.local", TEST_PASSWORD);
    String courseId = reviews.findAll().getFirst().getCourse().getId().toString();
    mvc.perform(post("/api/reviews").session(session).with(csrf())
            .contentType("application/json")
            .content("{\"courseId\":\"%s\",\"rating\":4,\"comment\":\"duplicate\"}"
                .formatted(courseId)))
        .andExpect(status().isConflict());
  }

  @Test
  void sqlInjectionShapedSearchIsHandledAsData() throws Exception {
    mvc.perform(get("/api/courses").param("q", "' OR 1=1 --"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void lessonCompletionUpdatesEnrollmentProgress() throws Exception {
    MockHttpSession student = login("student@academy.local", TEST_PASSWORD);
    var lesson = lessons.findAll().getFirst();
    String courseId = reviews.findAll().getFirst().getCourse().getId().toString();
    mvc.perform(post("/api/auth/refresh").session(student).with(csrf()))
        .andExpect(status().isOk());
    mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
            "/api/learning/lessons/{id}/completion", lesson.getId())
            .session(student).with(csrf()).contentType("application/json")
            .content("{\"completed\":true}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.completed").value(true))
        .andExpect(jsonPath("$.courseProgress").value(50));
    mvc.perform(get("/api/learning/courses/{id}", courseId).session(student))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.progress").value(50));
  }

  @Test
  void disablingUserRevokesAnExistingSession() throws Exception {
    String email = "disabled-" + UUID.randomUUID() + "@example.test";
    String registration = "{\"email\":\"%s\",\"password\":\"%s\",\"displayName\":\"Disabled User\"}"
        .formatted(email, TEST_PASSWORD);
    MvcResult registered = mvc.perform(post("/api/auth/register").with(csrf())
            .contentType("application/json").content(registration))
        .andExpect(status().isCreated()).andReturn();
    MockHttpSession userSession = (MockHttpSession) registered.getRequest().getSession();
    JsonNode payload = json.readTree(registered.getResponse().getContentAsString());
    String userId = payload.get("user").get("id").asText();
    MockHttpSession admin = login("admin@academy.local", TEST_PASSWORD);
    mvc.perform(patch("/api/admin/users/{id}/status", userId).session(admin).with(csrf())
            .contentType("application/json").content("{\"enabled\":false}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.enabled").value(false));
    mvc.perform(get("/api/profile").session(userSession)).andExpect(status().isUnauthorized());
  }

  private MockHttpSession register(String email, String displayName) throws Exception {
    String body = "{\"email\":\"%s\",\"password\":\"%s\",\"displayName\":\"%s\"}"
        .formatted(email, TEST_PASSWORD, displayName);
    return (MockHttpSession) mvc.perform(post("/api/auth/register").with(csrf())
            .contentType("application/json").content(body))
        .andExpect(status().isCreated()).andReturn().getRequest().getSession();
  }

  private MockHttpSession login(String email, String password) throws Exception {
    return (MockHttpSession) mvc.perform(post("/api/auth/login").with(csrf())
            .contentType("application/json")
            .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
        .andExpect(status().isOk()).andReturn().getRequest().getSession();
  }

  private String validCourse(String title) {
    return """
        {"title":"%s","shortDescription":"A valid course promise",
         "description":"A sufficiently useful and detailed course description",
         "level":"BEGINNER"}
        """.formatted(title);
  }
}
