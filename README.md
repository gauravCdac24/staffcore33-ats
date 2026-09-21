# Staffcore33 ATS — Phase 1 MVP

Web-based Applicant Tracking System for Staffcore33 LLC.

## Stack

| Layer | Technology |
|-------|------------|
| Frontend | Next.js 15 + TypeScript + Tailwind CSS |
| Backend | Spring Boot 3.4 + Java 17 |
| Database | PostgreSQL |
| Auth | Spring Security + JWT |
| Files | Local filesystem |
| AI | Not included in Phase 1 (manual fields + rule-based matching) |

## Project layout

```
ATS/
  backend/          Spring Boot API (:8080)
  frontend/         Next.js app (:3000)
  deploy/nginx/     nginx reverse-proxy config
  deploy/systemd/   systemd unit files for VM
  requirement.md    Product requirements
```

## Prerequisites (local)

- JDK 17+
- Maven 3.9+
- Node.js 20+
- PostgreSQL 14+

## Database setup

```sql
CREATE DATABASE staffcore33_ats;
```

Update credentials in `backend/src/main/resources/application-local.yml` or set:

```
DB_USER=postgres
DB_PASSWORD=your_password
```

Default local config expects user `postgres` / password `root` on `127.0.0.1:5432`.

## Run locally

### 1. Backend

```bash
cd backend
mvn spring-boot:run
```

API: http://127.0.0.1:8080  
Flyway migrates schema on startup. Default admin is seeded:

- Email: `admin@staffcore33.com`
- Password: `Admin@123`

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

App: http://localhost:3000  
Next.js rewrites `/api/*` → Spring Boot `:8080`.

## Default login

| Field | Value |
|-------|-------|
| Email | admin@staffcore33.com |
| Password | Admin@123 |

**Change this password after first login in production.**

## Features (Phase 1)

- Roles: Admin, Recruiter, Account Manager
- Clients, Jobs, Candidates, Resume versions (upload)
- Pipeline statuses, Submissions (duplicate protection), Interviews
- Activities, Tasks/follow-ups, Tags
- Candidate search + duplicate detection
- Rule-based JD → candidate matching (no LLM)
- Command Center dashboard + reports
- CSV import/export
- Soft delete + audit logging

## VM deployment (nginx)

1. Install JDK 17, Node.js, PostgreSQL, nginx on the VM.
2. Create DB user/database and upload directory:

```bash
sudo mkdir -p /opt/staffcore33 /var/staffcore33/uploads /etc/staffcore33
sudo useradd -r -s /bin/false staffcore || true
sudo chown -R staffcore:staffcore /opt/staffcore33 /var/staffcore33
```

3. Build artifacts:

```bash
cd backend && mvn -DskipTests package
# copy target/ats-1.0.0.jar → /opt/staffcore33/backend/

cd frontend && npm ci && npm run build
# copy frontend folder → /opt/staffcore33/frontend/
```

4. Copy env + services:

```bash
sudo cp deploy/ats-api.env.example /etc/staffcore33/ats-api.env
# edit secrets
sudo cp deploy/systemd/*.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now staffcore-ats-api staffcore-ats-web
```

5. nginx:

```bash
sudo cp deploy/nginx/staffcore33-ats.conf /etc/nginx/sites-available/staffcore33-ats
sudo ln -s /etc/nginx/sites-available/staffcore33-ats /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

6. (Recommended) TLS with certbot for HTTPS.

On the VM, traffic flow:

```
Internet → nginx :80/:443 → Next.js :3000
                         → Spring Boot :8080 (/api)
```

Disable Next.js API rewrites in production if nginx already routes `/api` (optional: set frontend to call same-origin `/api`).

## Production notes

- Set a strong `JWT_SECRET` (≥32 chars).
- Restrict CORS to your public URL.
- Back up PostgreSQL and `/var/staffcore33/uploads` regularly.
- Keep upload directory writable by the API process user.

## Out of scope (Phase 1)

Communication / Zoho Mail, AI Copilot, multi-workspace, Docker, client/candidate portals, payroll/CRM.
