ALTER TABLE course_sections
  ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE lessons
  ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE enrollments
  ADD COLUMN last_accessed_at TIMESTAMPTZ,
  ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

UPDATE enrollments SET last_accessed_at = enrolled_at WHERE last_accessed_at IS NULL;
ALTER TABLE enrollments ALTER COLUMN last_accessed_at SET NOT NULL;

CREATE TABLE instructor_applications (
  id UUID PRIMARY KEY,
  applicant_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  expertise VARCHAR(500) NOT NULL,
  motivation VARCHAR(2000) NOT NULL,
  portfolio_url VARCHAR(500),
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
    CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
  reviewed_by UUID REFERENCES users(id) ON DELETE SET NULL,
  review_note VARCHAR(1000),
  reviewed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  version BIGINT NOT NULL DEFAULT 0,
  CHECK (status <> 'REJECTED' OR review_note IS NOT NULL)
);
CREATE INDEX idx_instructor_applications_status
  ON instructor_applications(status, created_at);

CREATE TABLE lesson_progress (
  id UUID PRIMARY KEY,
  enrollment_id UUID NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
  lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
  completed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(enrollment_id, lesson_id)
);
CREATE INDEX idx_lesson_progress_enrollment ON lesson_progress(enrollment_id);
CREATE INDEX idx_lesson_progress_lesson ON lesson_progress(lesson_id);

CREATE INDEX idx_enrollments_course ON enrollments(course_id);
CREATE INDEX idx_enrollments_last_accessed ON enrollments(student_id, last_accessed_at DESC);
