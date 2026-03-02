# 🛡️ VulnGuard — Attack Surface Management Simulator

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen?style=flat-square&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![JWT](https://img.shields.io/badge/Auth-JWT-purple?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

> A full-stack cybersecurity tool that simulates how real enterprise security teams track, scan, and manage vulnerabilities across their entire digital infrastructure.

---

## 📸 Preview

| Dashboard | Vulnerabilities |
|---|---|
| Real-time risk metrics, severity chart, 14-day trend | Filter by severity, CVE detail panel, remediation |

---

## ✨ Features

- 🔐 **JWT Authentication** — Secure register/login with stateless token-based auth
- 🖥️ **Asset Inventory** — Track Domains, IPs, APIs, Cloud Resources, Web Applications
- ⚡ **Vulnerability Scanner** — Simulates real CVE-style scans with 20 attack templates
- 📊 **Live Dashboard** — Severity breakdown chart, top-10 risk table, 14-day trend graph
- 🗺️ **Asset Risk Map** — Per-asset risk scoring sorted by criticality and vulnerability age
- 📄 **Report Export** — Download full reports as JSON or professionally formatted PDF
- 🔢 **Risk Scoring Engine** — `Severity × Asset Criticality × Age Factor`
- 🌐 **Swagger UI** — Full interactive API documentation with Bearer token support

---

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.3, Spring Security, Spring Data JPA |
| Auth | JWT (JJWT), BCrypt password encoding |
| Database | MySQL 8.0 (Hibernate ORM) |
| PDF Generation | iText 5 |
| API Docs | SpringDoc OpenAPI / Swagger UI |
| Frontend | Vanilla JS SPA (single `index.html`, Chart.js) |
| Build | Maven |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0 running on `localhost:3306`

### Step 1 — Clone the repository

```bash
git clone https://github.com/rohitharumugam22/vulnguard.git
cd vulnguard
```

### Step 2 — Configure MySQL

Make sure MySQL is running. The app will **auto-create** the `vulnguard` database on first run.

Default credentials in `application.properties`:
```
Username: root
Password: root
```

To use different credentials, edit:
```
src/main/resources/application.properties
```

### Step 3 — Run the application

```bash
mvn spring-boot:run
```

### Step 4 — Open in browser

```
http://localhost:8080
```

Register a new account and you're in!

---

## 📖 How to Use

### 1️⃣ Dashboard
Your security command center. Shows real-time stats, severity distribution, top risks, and trend graph.

### 2️⃣ Assets
Register everything you want to monitor:

| Type | Example |
|---|---|
| Domain | `example.com` |
| IP Address | `192.168.1.100` |
| API Endpoint | `api.example.com/v1` |
| Cloud Resource | `AWS S3 Bucket` |
| Web Application | `app.example.com` |

Set **Criticality 1–5** — higher criticality amplifies the risk score of vulnerabilities found on that asset.

### 3️⃣ Scans & Scanner
- **Single Scan** — Select an asset, click ⚡ Scan → finds 3–5 CVE-style vulnerabilities
- **Batch Scan** — Scans all active assets at once with aggregated results

### 4️⃣ Vulnerabilities
Filter by severity, search by CVE ID or asset name, view full details, and mark as remediated.

### 5️⃣ Reports & Export
- **JSON Report** — Full structured data export
- **PDF Report** — Executive summary ready to share with management

---

## 🔢 Risk Score Formula

```
Risk Score = Severity Weight × Asset Criticality × Age Factor

Where:
  Severity Weight  → CRITICAL=10, HIGH=7, MEDIUM=4, LOW=1, INFO=0.5
  Asset Criticality → 1 (low) to 5 (critical)
  Age Factor       → 1 + (ageInDays / 30)

Normalized to 0–100 scale
```

---

## 🌐 API Endpoints

Full interactive docs available at:
```
http://localhost:8080/swagger-ui.html
```

| Group | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` |
| Assets | `GET/POST /api/assets`, `PUT/DELETE /api/assets/{id}` |
| Scans | `POST /api/scans/asset/{id}`, `POST /api/scans/all`, `GET /api/scans/open` |
| Vulnerabilities | `PATCH /api/scans/{id}/remediate`, `GET /api/scans/stats` |
| Dashboard | `GET /api/dashboard`, `GET /api/dashboard/trend`, `GET /api/dashboard/asset-risks` |
| Reports | `GET /api/reports/json`, `GET /api/reports/pdf` |

---

## 📁 Project Structure

```
vulnguard/
├── src/main/java/com/rohith/vulnguard/
│   ├── config/          # OpenAPI, CORS config
│   ├── controller/      # REST controllers
│   ├── model/           # JPA entities (Asset, Vulnerability, User)
│   ├── repository/      # Spring Data JPA repositories
│   ├── security/        # JWT filter, SecurityConfig, UserDetailsService
│   └── service/         # Business logic, risk scoring, report generation
├── src/main/resources/
│   ├── static/
│   │   └── index.html   # Complete single-page frontend
│   └── application.properties
└── pom.xml
```

---

## 🔄 Typical Workflow

```
Register → Add Assets → Run Scans → Review Dashboard
        → Prioritize by Risk Score → Remediate
        → Export PDF Report → Track Progress
```

---

## 🎯 Real-World Use Cases

| Who | How |
|---|---|
| Security Analyst | Monitor company assets, track exposure |
| Penetration Tester | Simulate scans and document findings |
| DevSecOps | Track APIs and cloud resources |
| IT Manager | Generate compliance/management reports |
| Students | Learn how real vulnerability management platforms work |

---

## 🛠️ Key Engineering Decisions

- **`@Transactional(readOnly = true)`** on `DashboardService` and `ReportService` — prevents `LazyInitializationException` when accessing JPA lazy collections with `open-in-view=false`
- **`@JsonIgnore`** on `Asset.vulnerabilities` — prevents Jackson from triggering lazy loads during serialization
- **`Promise.allSettled`** in the frontend — one failing API endpoint never freezes the entire dashboard
- **`UsernameNotFoundException` catch** in `JwtRequestFilter` — prevents mysterious 403 errors after a database reset
- **No Lombok** — zero annotation processors, explicit Java throughout

---

## 📜 License

MIT License — free to use, modify, and distribute.

---

---

## 👨‍💻 Author

**Rohith Arumugam**

[![GitHub](https://img.shields.io/badge/GitHub-rohitharumugam22-181717?style=flat-square&logo=github)](https://github.com/rohitharumugam22)

---

<p align="center">Built with ☕ Java + 🛡️ Spring Security + 💙 passion for cybersecurity</p>
<p align="center">© 2026 Rohith Arumugam</p>
