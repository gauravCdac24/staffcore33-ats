Staffcore33 ATS — Phase 1 MVP Requirements

1. Objective

Build a web-based Applicant Tracking System (ATS) for Staffcore33 LLC to manage the complete recruiting process from Job Creation → Candidate Sourcing → Screening → Submission → Interview → Placement.

The system should be simple, fast, mobile-responsive, and designed specifically for a US staffing/recruiting business.

---

2. User Roles

Admin

- Full access to the system
- Create/edit/deactivate users
- Assign roles
- View all jobs, candidates, clients and reports
- Manage system settings

Recruiter

- Create/manage candidates
- Upload resumes
- Search candidates
- Manage assigned jobs
- Match candidates to jobs
- Submit candidates
- Track candidate status
- Add notes and activities

Account Manager / BD

- Create/manage clients
- Create/manage jobs
- View candidate submissions
- Track interviews and placements

---

3. Login & User Management

Login

- Email/username + password
- Secure authentication
- Forgot/reset password
- Logout

User Profile

- Name
- Email
- Phone
- Role
- Active/inactive status

Permissions

Users should only be able to access functionality permitted by their role.

---

4. Main Dashboard

Dashboard should show:

Jobs

- Total open jobs
- New jobs
- Jobs in progress
- Jobs on hold
- Filled jobs
- Closed jobs

Candidates

- Total candidates
- New candidates
- Candidates contacted
- Candidates in screening
- Candidates submitted
- Candidates interviewing
- Candidates placed

Recruiting Pipeline

Display:

Sourced → Contacted → Interested → Screening → Submitted → Interview → Selected → Onboarding → Placed

Also show:

- Today's tasks
- Overdue tasks
- Recent activities
- Recent submissions

Dashboard should support date filters such as:

- Today
- This week
- This month
- Custom date range

---

5. Client Management

Create a client database.

Client Fields

- Company name
- Website
- Industry
- Address
- City
- State
- ZIP
- Primary contact name
- Contact email
- Contact phone
- LinkedIn
- Account Manager
- MSA status
- Payment terms
- Notes
- Active/Inactive

Client Profile Should Show

- Active jobs
- Closed jobs
- Candidates submitted
- Interviews
- Placements
- Activity history

---

6. Job Management

Recruiter/Account Manager should be able to create a job manually.

Job Fields

- Job ID
- Client
- Job title
- Full Job Description
- Required skills
- Preferred skills
- Years of experience
- Location
- Remote / Hybrid / Onsite
- Work authorization requirement
- Employment type
- W2 / C2C / 1099
- Pay rate
- Bill rate
- Contract duration
- Start date
- Number of openings
- Priority
- Assigned recruiter
- Account manager
- Date received
- Job status

Job Status

- New
- Open
- In Progress
- On Hold
- Filled
- Cancelled
- Closed

JD Input

Allow recruiter to:

1. Paste JD text
2. Upload JD document
3. Save JD

The system should extract the important requirements automatically.

---

7. JD Parsing

When a recruiter adds a JD, AI should identify:

Required Skills

Example:

- Java
- Spring Boot
- AWS
- Kubernetes

Preferred Skills

- Kafka
- Docker

Experience

Example:

- 7+ years Java
- 5+ years backend development

Location

Example:

- Dallas, TX
- Must be within 50 miles

Work Authorization

Example:

- USC
- GC
- No H1B

Employment Type

- W2
- C2C
- Contract
- Contract-to-hire

Other Requirements

- Onsite requirements
- Education
- Certifications
- Industry experience
- Security clearance
- Travel requirements

Recruiter must be able to manually edit the AI-extracted information.

---

8. Candidate Management

Create a centralized candidate database.

Personal Information

- First name
- Last name
- Email
- Phone
- City
- State
- ZIP
- LinkedIn URL

Professional Information

- Current title
- Total years of experience
- Primary skills
- Secondary skills
- Previous employers
- Education
- Certifications

Staffing Information

- Work authorization
- Visa type
- W2 / C2C / 1099
- Desired rate
- Current rate
- Availability
- Notice period
- Relocation preference
- Remote preference
- Willingness to travel

Internal Information

- Recruiter
- Candidate source
- Tags
- Notes
- Date added
- Last contacted
- Next follow-up
- Candidate status

---

9. Resume Upload & Parsing

Allow recruiters to upload:

- PDF
- DOC
- DOCX
- TXT

After upload, AI should automatically extract:

- Candidate name
- Email
- Phone
- Location
- LinkedIn
- Current title
- Skills
- Technologies
- Employers
- Job titles
- Years of experience
- Education
- Certifications

Recruiter should be able to review and edit extracted information.

Resume Versions

Maintain multiple resume versions:

- Original Resume
- Updated Resume
- Client Submission Resume

Do not overwrite the original resume.

---

10. Candidate Search

Provide a powerful candidate search.

Search/filter by:

- Name
- Skill
- Technology
- Location
- State
- ZIP
- Job title
- Employer
- Experience
- Work authorization
- Visa
- W2/C2C
- Rate
- Availability
- Recruiter
- Candidate status
- Tags

Boolean Search

Support searches such as:

"Java AND Spring Boot AND AWS"

"Python OR Java"

"("Data Engineer" OR "ETL Developer") AND Snowflake"

Search results should be ranked by relevance.

---

11. JD → Candidate Matching

This is a critical Phase 1 feature.

From a Job page, recruiter should be able to click:

"Find Matching Candidates"

The system should search the existing candidate database and rank candidates against the JD.

Match Criteria

Compare:

- Required technical skills
- Preferred skills
- Years of experience
- Job titles
- Industry experience
- Location
- Work authorization
- Employment type
- Rate
- Availability
- Other mandatory requirements

Match Score

Display a score from 0–100%.

Example:

John Smith — 94% Match

- Technical Skills: 96%
- Experience: 95%
- Location: 100%
- Work Authorization: 100%
- Rate: 85%

AI Explanation

Show:

Why this candidate matches

- 8 years Java experience
- 6 years Spring Boot
- 5 years AWS
- Local to Dallas
- Meets work authorization requirement

Missing / Weak Requirements

- Kafka experience not found
- Rate is $5/hr above target

AI should explain the score instead of providing only a percentage.

Recruiter must be able to override the AI recommendation.

---

12. Candidate Profile

Candidate profile should contain:

Overview

- Candidate information
- Skills
- Experience
- Location
- Work authorization
- Rate
- Availability

Resume

- View uploaded resumes
- Upload new version
- Download resume

Jobs

Show every job associated with the candidate.

Example:

Job| Client| Status| Submitted| Result
Java Developer| ABC Corp| Interview| Aug 25| Pending
Sr. Engineer| XYZ Corp| Submitted| Aug 28| Pending

Activity Timeline

Show:

- Calls
- Emails
- Notes
- Screening
- Submission
- Interview
- Status changes

Everything should be timestamped and associated with the user.

---

13. Candidate Pipeline

Each candidate should have a job-specific status.

Pipeline

Sourced
↓
Contacted
↓
Interested
↓
Screening
↓
Submitted
↓
Client Review
↓
Interview
↓
Selected
↓
Onboarding
↓
Placed

Additional statuses:

- Not Interested
- Rejected by Recruiter
- Rejected by Client
- Withdrawn
- Position Closed
- Duplicate

A candidate can be associated with multiple jobs simultaneously, with a separate status for each job.

---

14. Candidate Submission

Recruiter should be able to submit a candidate to a job.

Submission should record:

- Candidate
- Job
- Client
- Submission date
- Submitted rate
- Bill rate
- Resume version
- Recruiter
- Submission notes
- Current status

Duplicate Submission Protection

If the candidate was already submitted to the same job, show:

"This candidate was already submitted to this job on [date]."

Do not create another submission unless the recruiter explicitly confirms.

---

15. Interview Tracking

Track:

- Candidate
- Job
- Client
- Interview round
- Date
- Time
- Interview type
- Interviewer
- Notes
- Feedback
- Result

Interview statuses:

- Scheduled
- Confirmed
- Completed
- Rescheduled
- Cancelled
- Selected
- Rejected

---

16. Activities & Notes

Recruiter should be able to add activities to candidates, jobs and clients.

Activity types:

- Phone call
- Email
- LinkedIn
- Screening
- Follow-up
- Resume request
- Client contact
- Submission
- Interview
- Other

Each activity should contain:

- Activity type
- Date/time
- User
- Notes

---

17. Tasks & Follow-Ups

Recruiters should be able to create tasks.

Example:

"Call John tomorrow regarding Java Developer position."

Task fields:

- Task name
- Candidate / Job / Client
- Assigned user
- Due date
- Priority
- Status
- Notes

Dashboard should display:

- Today's tasks
- Upcoming tasks
- Overdue tasks

---

18. Tags

Allow recruiters to add customizable candidate tags.

Examples:

- Java
- Python
- Data Engineer
- Local
- GC
- USC
- Immediate
- Hot Candidate
- W2
- C2C
- Available

Tags must be searchable/filterable.

---

19. Duplicate Candidate Detection

System should identify possible duplicate candidates using:

- Email
- Phone
- LinkedIn
- Name + location
- Resume similarity

When adding a candidate, show:

"Possible duplicate candidate found."

Display existing matching candidates before creating a new record.

---

20. Reports

Phase 1 should include basic reports.

Recruiter Report

- Candidates added
- Candidates contacted
- Candidates screened
- Candidates submitted
- Interviews
- Placements

Job Report

- Open jobs
- Candidates per job
- Submissions
- Interviews
- Placements
- Days open

Client Report

- Jobs received
- Candidates submitted
- Interviews
- Placements

Conversion Report

- Contact → Interested
- Interested → Screening
- Screening → Submission
- Submission → Interview
- Interview → Placement

Reports should be filterable by date, recruiter and client.

---

21. Import / Export

Candidate Import

Allow CSV/Excel upload.

Fields should be mappable during import.

Example:

First Name
Last Name
Email
Phone
Location
LinkedIn
Skills
Experience
Work Authorization
Rate
Recruiter

Export

Allow export of:

- Candidates
- Jobs
- Submissions
- Interviews
- Placements
- Reports

Export format:

- CSV
- Excel

---

22. Security

The following are mandatory:

- Secure authentication
- Role-based access control
- Password hashing
- Secure sessions
- HTTPS
- Secure database access
- Secure file storage
- Regular backups
- Audit logging
- User access controls

Candidate information must not be accessible to unauthorized users.

---

23. Audit Log

Track major system actions.

Examples:

- Candidate created
- Candidate edited
- Resume uploaded
- Candidate deleted
- Job created
- Job edited
- Candidate submitted
- Status changed
- User created
- Permission changed

Each audit record should contain:

- User
- Action
- Record
- Date/time

---

24. Soft Delete

Do not permanently delete important records immediately.

Use soft deletion for:

- Candidates
- Jobs
- Clients
- Users

Admin should have the ability to restore deleted records where appropriate.

---

25. AI Architecture

AI functionality should be modular so the AI provider can be changed later.

Phase 1 AI functionality:

1. JD parsing
2. Resume parsing
3. JD-to-candidate matching
4. Resume summary
5. Candidate strengths
6. Candidate gaps/missing requirements

AI output should always be editable/overridable by recruiters.

The system should not automatically reject candidates based solely on AI output.

---

26. Responsive Web Application

The ATS should work on:

- Desktop
- Laptop
- Tablet
- Mobile browser

A separate native Android/iOS application is not required for Phase 1.

---

27. Basic System Architecture

Recommended architecture:

Frontend

- React / Next.js or equivalent

Backend

- Node.js / Python or equivalent

Database

- PostgreSQL preferred

File Storage

- S3-compatible cloud storage

AI

- LLM API through a separate AI service/module

Authentication

- Secure role-based authentication

The technology team can choose the exact framework, provided the system remains scalable and maintainable.

---

28. Core Database Entities

The database should at minimum contain:

- Users
- Clients
- Jobs
- Candidates
- Resumes
- Candidate Skills
- Job Requirements
- Submissions
- Interviews
- Activities
- Tasks
- Tags
- Candidate Tags
- Documents
- Audit Logs

Key Relationships

Client → Jobs

Job → Submissions

Candidate → Submissions

Candidate → Resumes

Candidate → Activities

Candidate → Interviews

Job → Interviews

Recruiter → Candidates

Recruiter → Jobs

---

29. Phase 1 End-to-End Workflow

The completed system must support this workflow:

1. Receive JD

↓

2. Create Job

↓

3. AI parses JD

↓

4. Search existing candidates

↓

5. Upload candidate resume

↓

6. AI parses resume

↓

7. AI calculates JD/Candidate match

↓

8. Recruiter reviews candidate

↓

9. Recruiter contacts/screens candidate

↓

10. Candidate moves through pipeline

↓

11. Submit candidate to client

↓

12. Track client response

↓

13. Schedule interview

↓

14. Track interview result

↓

15. Track selection/onboarding

↓

16. Mark candidate as placed

---

30. Phase 1 Acceptance Criteria

Phase 1 will be considered complete when a recruiter can perform the entire workflow above without requiring another ATS.

The system must allow the recruiter to:

- Create a client
- Create a job
- Parse the JD
- Add candidates
- Upload and parse resumes
- Search candidates
- Find candidates matching a JD
- Review AI match reasoning
- Edit candidate information
- Track candidate status
- Submit candidates
- Prevent duplicate submissions
- Track interviews
- Add notes and activities
- Create follow-up tasks
- Track placements
- Search/filter records
- Generate basic reports
- Import/export candidate data
- Manage users and permissions

Priority: Build the simplest reliable MVP first. Avoid adding CRM, payroll, timesheets, invoicing, client portal, candidate portal, or automated outreach functionality in Phase 1.