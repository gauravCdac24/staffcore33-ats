# Staffcore33 ATS

**Staffcore33 ATS** is a web-based Applicant Tracking System built for **Staffcore33 LLC**, a US staffing and recruiting business.

---

## What this product is

Staffcore33 ATS helps recruiters and account managers run the full hiring pipeline in one place — from job creation through candidate sourcing, screening, client submission, interviews, and placement.

It is designed to be:

- Simple and fast for day-to-day recruiting work
- Role-based (Admin, Recruiter, Account Manager)
- Mobile-responsive for use on desktop and phone
- Self-hosted with data stored locally (PostgreSQL + filesystem uploads)

This repository contains the **Phase 1 MVP**.

---

## Description

Recruiting teams use Staffcore33 ATS to:

| Area | What you can do |
|------|-----------------|
| **Clients & jobs** | Maintain client companies and open job requisitions |
| **Candidates** | Add candidates, upload resume versions, search, and detect duplicates |
| **Pipeline** | Move candidates through statuses (sourced → screening → submitted → interview → placed) |
| **Submissions** | Submit candidates to jobs with duplicate-submission protection |
| **Interviews** | Schedule and track interviews |
| **Matching** | Rule-based job ↔ candidate matching (no AI/LLM in Phase 1) |
| **Tasks & activity** | Follow-ups, notes, and an audit trail |
| **Dashboard & reports** | Command Center metrics, CSV import/export |

**Default seeded admin (local):**

| Field | Value |
|-------|-------|
| Email | `admin@staffcore33.com` |
| Password | `Admin@123` |

Change this password before any shared or production use.

---

## Technology stack

| Layer | Technology | Notes |
|-------|------------|--------|
| **Frontend** | Next.js 15, React 19, TypeScript, Tailwind CSS 4 | UI on port `3000` |
| **Backend** | Spring Boot 3.4, Java 17, Maven | REST API on port `8080` |
| **Database** | PostgreSQL 14+ | Schema via Flyway migrations |
| **Auth** | Spring Security + JWT | Email/password login |
| **File storage** | Local filesystem | Resumes/uploads under `device\` locally |
| **Import / export** | Apache POI + CSV | Bulk data support |
| **Matching** | Rule-based (Phase 1) | No LLM / AI Copilot yet |

### Project layout

```
staffcore33-ats/
  backend/           Spring Boot API (:8080)
  frontend/          Next.js app (:3000)
  device/            Uploaded files (created at runtime)
  .tools/            Portable JDK/Maven/Node if auto-installed
  deploy/nginx/      nginx reverse-proxy (VM)
  deploy/systemd/    systemd units (VM)
  start-local.bat    Windows one-click starter
  run-local.ps1      Windows PowerShell runner (used by the .bat)
  requirement.md     Full product requirements
```

---

## How to run on a local Windows system

### Step 1 — Prerequisites

You need:

- Windows 10 / 11
- Internet connection (first run downloads tools and packages)
- Optional but recommended: **PostgreSQL 14+** already installed  
  (the script can attempt to install it via winget/Chocolatey)

If **JDK 17+**, **Maven**, or **Node.js / npm** are missing, `start-local.bat` will try to install them automatically in this order:

1. **winget**
2. **Chocolatey**
3. Portable zips under project folder `.tools\`

Frontend `npm` packages and backend Maven dependencies are also installed/resolved when missing.

### Step 2 — Get the project

Open the project folder in File Explorer or a terminal, for example:

```text
c:\Users\hp\staffcore33-ats
```

### Step 3 — Start everything (recommended)

**Option A — double-click**

1. Double-click `start-local.bat`
2. Allow UAC / installer prompts if Windows asks (JDK, Node, or PostgreSQL)
3. Wait until two new windows open (API + Web)

**Option B — Command Prompt or PowerShell**

```bat
cd c:\Users\hp\staffcore33-ats
start-local.bat
```

Or:

```powershell
cd c:\Users\hp\staffcore33-ats
.\run-local.ps1
```

If your PostgreSQL password is not `root`:

```powershell
.\run-local.ps1 -DbUser postgres -DbPassword your_password
```

### Step 4 — What the script does

1. Creates local folders: `device\` (uploads), `logs\`, `.tools\`
2. Checks for JDK, Maven, Node.js, npm — installs any that are missing, then **re-checks**
3. Tries to ensure PostgreSQL is available and creates database `staffcore33_ats` if needed
4. Installs frontend packages (`npm install`) when `node_modules` is missing
5. Resolves backend Maven dependencies
6. Frees ports **8080** and **3000** if they are already in use
7. Starts the Spring Boot API and Next.js frontend in separate windows

### Step 5 — Open the app

| Service | URL |
|---------|-----|
| **Web app** | http://localhost:3000 |
| **API** | http://127.0.0.1:8080 |

Sign in with:

- Email: `admin@staffcore33.com`
- Password: `Admin@123`

### Step 6 — Where local data is stored

| Data | Location |
|------|----------|
| Application database | PostgreSQL database `staffcore33_ats` on `127.0.0.1:5432` |
| Uploaded files (resumes, etc.) | `device\` at the project root |
| Portable tools (if auto-installed) | `.tools\` |

### Optional script flags

```powershell
.\run-local.ps1 -DbUser postgres -DbPassword your_password
.\run-local.ps1 -SkipToolInstall          # do not auto-install JDK / Maven / Node
.\run-local.ps1 -SkipPostgresInstall      # do not auto-install PostgreSQL
.\run-local.ps1 -SkipNpmInstall           # skip frontend npm install
```

### Restarting later

Run `start-local.bat` again anytime. It frees ports 3000/8080 if busy, then starts the services again.

---

## Manual start (any OS)

Use this if you prefer not to use the Windows script.

### 1. Database

```sql
CREATE DATABASE staffcore33_ats;
```

Default local credentials: user `postgres`, password `root` on `127.0.0.1:5432`.  
Override with env vars `DB_USER` / `DB_PASSWORD`, or edit `backend/src/main/resources/application-local.yml`.

### 2. Backend

```bash
cd backend
set FILE_UPLOAD_DIR=..\device
mvn spring-boot:run
```

- API: http://127.0.0.1:8080  
- Flyway applies schema on startup  

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

- App: http://localhost:3000  
- Next.js rewrites `/api/*` → Spring Boot `:8080`

---

## Features (Phase 1)

- Roles: Admin, Recruiter, Account Manager
- Clients, Jobs, Candidates, Resume versions (upload)
- Pipeline statuses, Submissions (duplicate protection), Interviews
- Activities, Tasks / follow-ups, Tags
- Candidate search + duplicate detection
- Rule-based JD → candidate matching (no LLM)
- Command Center dashboard + reports
- CSV import / export
- Soft delete + audit logging

---

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

Traffic flow on the VM:

```
Internet → nginx :80/:443 → Next.js :3000
                         → Spring Boot :8080 (/api)
```

---

## Production notes

- Set a strong `JWT_SECRET` (≥32 characters).
- Restrict CORS to your public URL.
- Back up PostgreSQL and the upload directory regularly.
- Keep the upload directory writable by the API process user.

---

## Out of scope (Phase 1)

Communication / Zoho Mail, AI Copilot, multi-workspace, Docker, client/candidate portals, payroll/CRM.

See `requirement.md` for the full Phase 1 requirements.
