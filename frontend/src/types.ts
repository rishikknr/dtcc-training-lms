export type Role = 'STUDENT' | 'INSTRUCTOR' | 'ADMIN';
export type CourseLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
export type CourseStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
export type ReviewStatus = 'PUBLISHED' | 'HIDDEN';
export type InstructorApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
export type EnrollmentStatus = 'ACTIVE' | 'CANCELLED';

export type User = {
  id: string;
  email: string;
  displayName: string;
  bio?: string;
  avatarUrl?: string;
  roles: Role[];
};

export type AdminUser = User & { enabled: boolean; createdAt: string };

export type Category = {
  id: string;
  name: string;
  slug: string;
  description?: string;
};

export type CourseSummary = {
  id: string;
  title: string;
  slug: string;
  shortDescription: string;
  level: CourseLevel;
  status: CourseStatus;
  thumbnailUrl?: string;
  averageRating: number;
  ratingCount: number;
  categoryId?: string;
  categoryName?: string;
  instructorId: string;
  instructorName: string;
};

export type Lesson = {
  id: string;
  title: string;
  description?: string;
  content?: string;
  videoUrl?: string;
  resourceUrl?: string;
  position: number;
  durationMinutes: number;
  preview: boolean;
  published: boolean;
};

export type CourseSection = {
  id: string;
  title: string;
  position: number;
  lessons: Lesson[];
};

export type Course = CourseSummary & {
  description: string;
  prerequisites?: string;
  learningObjectives?: string;
  tags?: string;
  language: string;
  durationMinutes: number;
  category?: Category;
  instructor: { id: string; displayName: string };
  sections: CourseSection[];
  enrolled: boolean;
  manageable: boolean;
  createdAt: string;
  updatedAt: string;
};

export type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
};

export type Review = {
  id: string;
  courseId: string;
  courseTitle: string;
  studentId: string;
  studentName: string;
  rating: number;
  comment: string;
  status: ReviewStatus;
  moderationReason?: string;
  createdAt: string;
  updatedAt: string;
};

export type ReviewSummary = {
  averageRating: number;
  totalReviews: number;
  distribution: Record<string, number>;
};

export type Enrollment = {
  id: string;
  course: CourseSummary;
  progress: number;
  enrolledAt: string;
  completedAt?: string;
  lastAccessedAt: string;
  status: EnrollmentStatus;
  cancelledAt?: string;
};

export type CourseStudent = {
  enrollmentId: string;
  studentId: string;
  studentName: string;
  studentEmail: string;
  progress: number;
  status: EnrollmentStatus;
  enrolledAt: string;
  completedAt?: string;
  lastAccessedAt: string;
};

export type LearningCourse = {
  courseId: string;
  title: string;
  progress: number;
  sections: Array<{
    id: string;
    title: string;
    position: number;
    lessons: Array<Lesson & { content: string; completed: boolean }>;
  }>;
};

export type InstructorApplication = {
  id: string;
  applicantId: string;
  applicantName: string;
  applicantEmail: string;
  expertise: string;
  motivation: string;
  portfolioUrl?: string;
  status: InstructorApplicationStatus;
  reviewNote?: string;
  createdAt: string;
  reviewedAt?: string;
};

export type StudentStats = { enrolledCourses: number; completedCourses: number; averageProgress: number };
export type InstructorStats = { courses: number; publishedCourses: number; learners: number; averageRating: number; averageCompletion: number; reviews: number };
export type AdminStats = { users: number; courses: number; enrollments: number; reviews: number; pendingInstructorApplications: number; hiddenReviews: number };
