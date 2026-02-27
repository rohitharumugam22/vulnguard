# 🛡️ VulnGuard: Intelligent Attack Surface Management (ASM) Simulator

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**VulnGuard** is a sophisticated Attack Surface Management (ASM) simulation platform. Built for security teams, it identifies, tracks, and prioritizes vulnerabilities across a digital estate using a **context-aware risk engine**.

---

## 🚀 Key Features

* **Multi-Vector Asset Management**: Inventory Domains, IPs, APIs, and Cloud Instances.
* **Dynamic Risk Scoring**: Prioritizes fixes based on business impact and vulnerability age.
* **Scan Simulator**: Generates realistic security flaws (SQLi, XSS, etc.) for testing workflows.
* **Secure by Design**: Stateless JWT authentication via `jjwt 0.12.6`.
* **Automated Reports**: Export executive-ready PDF summaries instantly.



---

## 🛠️ Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Backend** | Spring Boot 3.3.5 / Java 17 |
| **Security** | Spring Security 6 + JWT |
| **Database** | MySQL (Production) / H2 (Dev) |
| **Documentation** | SpringDoc OpenAPI (Swagger UI) |
| **Reporting** | iText PDF Library |

---

## 🏗️ Project Structure

```text
com.rohith.vulnguard
  ├── ⚙️ config/      # Security & OpenAPI configurations
  ├── 🎮 controller/  # REST Endpoints (Asset, Scan, Auth)
  ├── 📦 model/       # JPA Entities (Asset, Vulnerability)
  ├── 🛡️ security/    # JWT Filter & Auth Logic
  ├── 🧠 service/     # Risk Scoring & Scan Logic
  └── 🗄️ repository/  # Database Access Layer
