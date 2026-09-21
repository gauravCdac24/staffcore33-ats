-- Staffcore33 ATS Phase 1 schema

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    phone           VARCHAR(50),
    role            VARCHAR(50) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE password_reset_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id),
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE clients (
    id                  BIGSERIAL PRIMARY KEY,
    company_name        VARCHAR(255) NOT NULL,
    website             VARCHAR(255),
    industry            VARCHAR(150),
    address             VARCHAR(500),
    city                VARCHAR(100),
    state               VARCHAR(50),
    zip                 VARCHAR(20),
    primary_contact_name VARCHAR(255),
    contact_email       VARCHAR(255),
    contact_phone       VARCHAR(50),
    linkedin            VARCHAR(255),
    account_manager_id  BIGINT REFERENCES users(id),
    msa_status          VARCHAR(50),
    payment_terms       VARCHAR(100),
    notes               TEXT,
    status              VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    deleted_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE jobs (
    id                      BIGSERIAL PRIMARY KEY,
    job_code                VARCHAR(50) NOT NULL UNIQUE,
    client_id               BIGINT NOT NULL REFERENCES clients(id),
    title                   VARCHAR(255) NOT NULL,
    description             TEXT,
    required_skills         TEXT,
    preferred_skills        TEXT,
    years_experience        INTEGER,
    location                VARCHAR(255),
    work_mode               VARCHAR(30),
    work_authorization      VARCHAR(100),
    employment_type         VARCHAR(50),
    pay_type                VARCHAR(30),
    pay_rate                NUMERIC(12,2),
    bill_rate               NUMERIC(12,2),
    contract_duration       VARCHAR(100),
    start_date              DATE,
    openings                INTEGER DEFAULT 1,
    priority                VARCHAR(30) DEFAULT 'MEDIUM',
    assigned_recruiter_id   BIGINT REFERENCES users(id),
    account_manager_id      BIGINT REFERENCES users(id),
    date_received           DATE,
    status                  VARCHAR(30) NOT NULL DEFAULT 'NEW',
    deleted_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE candidates (
    id                      BIGSERIAL PRIMARY KEY,
    candidate_code          VARCHAR(50) NOT NULL UNIQUE,
    first_name              VARCHAR(100) NOT NULL,
    last_name               VARCHAR(100) NOT NULL,
    email                   VARCHAR(255),
    phone                   VARCHAR(50),
    city                    VARCHAR(100),
    state                   VARCHAR(50),
    zip                     VARCHAR(20),
    linkedin_url            VARCHAR(255),
    current_title           VARCHAR(255),
    current_company         VARCHAR(255),
    total_experience_years  NUMERIC(5,1),
    primary_skills          TEXT,
    secondary_skills        TEXT,
    previous_employers      TEXT,
    education               TEXT,
    certifications          TEXT,
    work_authorization      VARCHAR(100),
    visa_type               VARCHAR(50),
    employment_pref         VARCHAR(30),
    desired_rate            NUMERIC(12,2),
    current_rate            NUMERIC(12,2),
    availability            VARCHAR(100),
    notice_period           VARCHAR(50),
    relocation_pref         VARCHAR(50),
    remote_pref             VARCHAR(50),
    willing_to_travel       BOOLEAN DEFAULT FALSE,
    recruiter_id            BIGINT REFERENCES users(id),
    source                  VARCHAR(100),
    notes                   TEXT,
    status                  VARCHAR(50) NOT NULL DEFAULT 'NEW',
    last_contacted_at       TIMESTAMPTZ,
    next_follow_up_at       TIMESTAMPTZ,
    deleted_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_candidates_email ON candidates(email);
CREATE INDEX idx_candidates_phone ON candidates(phone);
CREATE INDEX idx_candidates_skills ON candidates USING gin (to_tsvector('english', coalesce(primary_skills,'') || ' ' || coalesce(secondary_skills,'')));

CREATE TABLE resumes (
    id              BIGSERIAL PRIMARY KEY,
    candidate_id    BIGINT NOT NULL REFERENCES candidates(id),
    version_type    VARCHAR(50) NOT NULL,
    original_name   VARCHAR(255) NOT NULL,
    stored_name     VARCHAR(255) NOT NULL,
    content_type    VARCHAR(100),
    file_size       BIGINT,
    uploaded_by     BIGINT REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE tags (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE candidate_tags (
    candidate_id BIGINT NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    tag_id       BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (candidate_id, tag_id)
);

CREATE TABLE job_candidates (
    id              BIGSERIAL PRIMARY KEY,
    job_id          BIGINT NOT NULL REFERENCES jobs(id),
    candidate_id    BIGINT NOT NULL REFERENCES candidates(id),
    status          VARCHAR(50) NOT NULL DEFAULT 'SOURCED',
    notes           TEXT,
    created_by      BIGINT REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (job_id, candidate_id)
);

CREATE TABLE submissions (
    id                  BIGSERIAL PRIMARY KEY,
    candidate_id        BIGINT NOT NULL REFERENCES candidates(id),
    job_id              BIGINT NOT NULL REFERENCES jobs(id),
    client_id           BIGINT NOT NULL REFERENCES clients(id),
    resume_id           BIGINT REFERENCES resumes(id),
    submitted_by        BIGINT REFERENCES users(id),
    submission_date     DATE NOT NULL DEFAULT CURRENT_DATE,
    submitted_rate      NUMERIC(12,2),
    bill_rate           NUMERIC(12,2),
    notes               TEXT,
    status              VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_submissions_candidate_job ON submissions(candidate_id, job_id);

CREATE TABLE interviews (
    id              BIGSERIAL PRIMARY KEY,
    candidate_id    BIGINT NOT NULL REFERENCES candidates(id),
    job_id          BIGINT NOT NULL REFERENCES jobs(id),
    client_id       BIGINT NOT NULL REFERENCES clients(id),
    round_name      VARCHAR(100),
    interview_date  DATE,
    interview_time  TIME,
    duration_minutes INTEGER DEFAULT 60,
    interview_type  VARCHAR(50),
    interviewer     VARCHAR(255),
    recruiter_id    BIGINT REFERENCES users(id),
    notes           TEXT,
    feedback        TEXT,
    result          VARCHAR(50),
    status          VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE activities (
    id              BIGSERIAL PRIMARY KEY,
    activity_type   VARCHAR(50) NOT NULL,
    notes           TEXT,
    activity_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    user_id         BIGINT REFERENCES users(id),
    candidate_id    BIGINT REFERENCES candidates(id),
    job_id          BIGINT REFERENCES jobs(id),
    client_id       BIGINT REFERENCES clients(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE tasks (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    notes           TEXT,
    candidate_id    BIGINT REFERENCES candidates(id),
    job_id          BIGINT REFERENCES jobs(id),
    client_id       BIGINT REFERENCES clients(id),
    assigned_to     BIGINT REFERENCES users(id),
    due_date        DATE,
    priority        VARCHAR(30) DEFAULT 'MEDIUM',
    status          VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_by      BIGINT REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE documents (
    id              BIGSERIAL PRIMARY KEY,
    entity_type     VARCHAR(50) NOT NULL,
    entity_id       BIGINT NOT NULL,
    original_name   VARCHAR(255) NOT NULL,
    stored_name     VARCHAR(255) NOT NULL,
    content_type    VARCHAR(100),
    file_size       BIGINT,
    uploaded_by     BIGINT REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id),
    action          VARCHAR(100) NOT NULL,
    entity_type     VARCHAR(50),
    entity_id       BIGINT,
    details         TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_created ON audit_logs(created_at DESC);
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_job_candidates_status ON job_candidates(status);

-- Default admin: password = Admin@123 (BCrypt)
INSERT INTO users (email, password_hash, full_name, phone, role, active)
VALUES (
    'admin@staffcore33.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'System Admin',
    NULL,
    'ADMIN',
    TRUE
);

INSERT INTO tags (name) VALUES
 ('Java'), ('Python'), ('Hot Candidate'), ('USC'), ('GC'), ('W2'), ('C2C'), ('Immediate'), ('Local');
