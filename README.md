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


##🧪 Intelligent Risk Scoring
VulnGuard calculates risk contextually rather than relying on static values. A "High" vulnerability on a Production Database is prioritized over the same bug on a Staging Server to ensure critical assets are addressed first.The Risk FormulaTo reflect real-world urgency, we use a dynamic time-decay factor:
$$Risk = (Severity \times Criticality) \times (1.0 + (\lfloor days/7 \rfloor \times 0.1))
$$Key Metrics:Severity: The inherent technical impact (mapped from Severity enum: Critical, High, Medium, Low).Criticality: The business value of the asset, rated on a scale of 1 to 5.
Age Factor: A 10% penalty is automatically added to the score for every 7 days the vulnerability remains unpatched.


---


##🚦 Getting Started
1. Database SetupEnsure your MySQL server is running, then create the project database:
      SQLCREATE DATABASE vulnguard;

2. ConfigurationUpdate your credentials in src/main/resources/application.properties:
Properties
      spring.datasource.username=YOUR_USERNAME
      spring.datasource.password=YOUR_PASSWORD
   
___


3. Run the ApplicationExecute the following commands in your terminal:Bash# Build the project and skip tests for a fast start
mvn clean install -DskipTests

# Run the Spring Boot application
mvn spring-boot:run

📖 API Documentation
Once the application has started, you can explore, test, and interact with the endpoints via the Swagger UI:
http://localhost:8080/swagger-ui.html

##🤝 Contact
Rohith A, Student, Computer Science & Engineering Department 
