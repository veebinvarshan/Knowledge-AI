# Enterprise AI Knowledge Management Platform

## 1. Project Overview
This repository contains the source code for the **Enterprise AI Knowledge Management Platform**, a production-grade SaaS system designed to unify fragmented organizational knowledge. It features a Next.js 15 client and a Spring Boot 3 Modular Monolith backend, running pre-filtered vector semantic search via Qdrant and Retrieval-Augmented Generation (RAG) powered by Google Gemini 2.5 Flash.

---

## 2. Directory Structure
```
/ (Repository Root)
├── .github/             # GitHub templates and CI/CD pipelines
├── assets/              # Branding resources and media
├── backend/             # Spring Boot 3 Java Maven project
├── database/            # Relational database schema migrations
├── docker/              # Local environment containers & Dockerfiles
├── docs/                # Architectural blueprints and functional specifications
├── frontend/            # Next.js 15 App Router web client
└── scripts/             # Local bootstrapping automation scripts
```

---

## 3. Technology Stack & Prerequisites
Make sure you have the following installed:
* **Java:** JDK 21
* **Node.js:** v20.x or higher (npm v10+)
* **Docker:** Engine v20.10+ with Compose support

---

## 4. Local Quick Start

### Step 1: Clone and Set Up Environment Variables
Duplicate the environment template and configure actual values:
```bash
cp .env.example .env
```

### Step 2: Spin Up Infrastructure Containers
Start PostgreSQL, Redis, and Qdrant locally using Docker Compose:
```bash
# From the repository root
docker compose -f docker/docker-compose.yml up -d postgres redis qdrant
```

### Step 3: Run the Backend Monolith
Open a terminal in the `/backend` directory:
* **Windows (PowerShell):**
  ```powershell
  mvn spring-boot:run
  ```
* **macOS / Linux:**
  ```bash
  ./mvnw spring-boot:run
  ```
The REST API starts at `http://localhost:8080/api/v1`. Access health metrics at `http://localhost:8080/api/v1/actuator/health`.

### Step 4: Run the Next.js Web Client
Open a terminal in the `/frontend` directory:
```bash
npm install
npm run dev
```
The client starts at `http://localhost:3000`.

---

## 5. Development Guidelines
* **Module Encapsulation:** Modules under `/backend/src/main/java/com/enterprise/platform/modules` are logically isolated. Cross-module queries must rely on Java services, not database joins.
* **Coding Style:** Run `mvn lint` or ESLint checkers before committing feature changes. Ensure all tests pass.

---

## 6. Secure Gemini API Key Configuration
The project integrates with Google Spring AI Gemini models. Set the `GEMINI_API_KEY` environment variable prior to starting up the backend application:

* **Command Line (Windows PowerShell):**
  ```powershell
  $env:GEMINI_API_KEY="your-gemini-key"
  ```
* **Command Line (Bash):**
  ```bash
  export GEMINI_API_KEY="your-gemini-key"
  ```
* **Local .env File:** Add the variable inside `/backend/.env` (which is excluded from Git via `.gitignore`):
  ```env
  GEMINI_API_KEY=your-gemini-key
  ```

### IntelliJ Run Configurations
To run inside IntelliJ IDEA:
1. Open **Edit Run Configurations...**
2. In the Environment Variables input field, add: `GEMINI_API_KEY=your-gemini-key`
3. Click Apply and run `AppMonolithApplication`.

### Docker Configuration
When running via Docker, define the environment variable inside `docker-compose.yml` or container configurations:
```yaml
environment:
  - GEMINI_API_KEY=${GEMINI_API_KEY}
```

