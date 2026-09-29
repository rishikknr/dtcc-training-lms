ALTER TABLE courses
  ADD COLUMN prerequisites TEXT,
  ADD COLUMN learning_objectives TEXT,
  ADD COLUMN tags VARCHAR(500),
  ADD COLUMN language VARCHAR(80) NOT NULL DEFAULT 'English';

ALTER TABLE lessons
  ADD COLUMN description VARCHAR(500),
  ADD COLUMN resource_url VARCHAR(500),
  ADD COLUMN published BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE enrollments
  ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
    CHECK (status IN ('ACTIVE', 'CANCELLED')),
  ADD COLUMN cancelled_at TIMESTAMPTZ;

CREATE INDEX idx_enrollments_course_status ON enrollments(course_id, status);
CREATE INDEX idx_enrollments_student_status ON enrollments(student_id, status, last_accessed_at DESC);

ALTER TABLE enrollments ADD CONSTRAINT chk_enrollment_cancelled_state
  CHECK ((status = 'CANCELLED' AND cancelled_at IS NOT NULL)
      OR (status = 'ACTIVE' AND cancelled_at IS NULL));
