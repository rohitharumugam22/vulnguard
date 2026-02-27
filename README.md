# VulnGuard: Intelligent Attack Surface Management (ASM) Simulator
VulnGuard is a sophisticated Attack Surface Management (ASM) simulation platform designed to help security teams identify, track, and prioritize vulnerabilities across a diverse digital estate. Unlike traditional scanners, VulnGuard uses a context-aware risk engine to calculate the real-world impact of security flaws based on asset criticality and vulnerability age.
## 🚀 Core Features
Multi-Vector Asset Management: Track Domains, IPs, Cloud Instances, and APIs with custom Business Criticality (1-5) weighting.
Intelligent Risk Scoring: A dynamic engine that calculates risk using a time-decay and criticality algorithm.
Automated Scan Simulator: Generates realistic vulnerability data (SQLi, XSS, etc.) to simulate a growing attack surface.
Stateless JWT Security: Robust authentication using jjwt 0.12.6 with modern cryptographic signing.
Executive Reporting: Instant PDF export of security postures using the iText library.
Interactive API Docs: Full Swagger/OpenAPI 3.0 integration for seamless developer onboarding.
## 🛠 Tech Stack
LayerTechnologyFramework Spring Boot 3.3.5 (Java 17)SecuritySpring Security + JWT (jjwt 0.12.6)PersistenceSpring Data JPA + MySQL / H2DocumentationSpringDoc OpenAPI 2.6.0 (Swagger UI)ReportingiText 5.5.13.3ToolingLombok, Maven, Jakarta Validation## 🏗 ArchitectureThe project follows a Clean Layered Architecture to ensure maintainability and scalability:Plaintextcom.rohith.vulnguard
  ├── config/      # Security & OpenAPI configurations
  ├── controller/  # REST API Endpoints
  ├── model/       # JPA Entities & Enums (Asset, Vulnerability, User)
  ├── security/    # JWT Filters & Auth logic (Modern jjwt 0.12.6)
  ├── service/     # Business logic & Risk Scoring Engine
  └── repository/  # Data Access Layer
## 🚦 Getting StartedPrerequisitesJDK 17 or higherMaven 3.8+MySQL 8.0+Installation & RunClone the repositoryBashgit clone https://github.com/your-username/VulnGuard.git
cd VulnGuard
Configure DatabaseUpdate src/main/resources/application.properties with your MySQL credentials:Propertiesspring.datasource.url=jdbc:mysql://localhost:3306/vulnguard?createDatabaseIfNotExist=true
spring.datasource.username=your_user
spring.datasource.password=your_password
Build and RunBashmvn clean install
mvn spring-boot:run
Access Swagger UIOpen your browser and navigate to:http://localhost:8080/swagger-ui.html## 📊 Risk Scoring LogicThe Age Factor in our engine encourages prompt patching. For every 7 days a vulnerability remains open, the risk score increases by 10%, simulating the increased likelihood of exploitation over time.$$Risk = (Severity \times Criticality) \times (1.0 + (\lfloor days/7 \rfloor \times 0.1))$$## 🤝 ContactDeveloped by Rohith Computer Science & Engineering Specialist in AI & Cybersecurity
