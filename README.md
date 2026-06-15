# 🚀 Agentic Trade Reconciliation Engine

## 📌 Overview

An automated, AI-driven full-stack system designed to reconcile internal trading records against external broker statements. By leveraging multimodal Large Language Models (LLMs), this engine eliminates manual data entry, reduces human error, and accelerates the daily financial reconciliation pipeline.

## ✨ Key Features

* **Multimodal AI Extraction:** Utilizes the Gemini API to intelligently extract structured trade data from unstructured visual broker statements (images).
* **Automated Reconciliation:** Cross-references extracted external broker data against internal trade ledgers to find exact matches and flag discrepancies.
* **Interactive Dashboard:** A React-based user interface for uploading statements, monitoring processing status, and reviewing the final reconciliation report.
* **Robust API Orchestration:** A Spring Boot backend handling file processing, agentic AI workflows, and API routing.

## 🏗️ Tech Stack

* **Backend:** Java, Spring Boot, RESTful APIs
* **Frontend:** React.js, Vite
* **AI / Agentic Workflow:** Google Gemini Multimodal API
* **Database & Infrastructure:** Docker, Docker Compose

## ⚙️ Local Setup & Installation

### Prerequisites

* Java 17+
* Node.js (v18+)
* API Key for Gemini
* Docker & Docker Compose

### 1. Database Setup

This project uses a containerized database to ensure a seamless setup environment.

1. Make sure Docker Desktop is running on your machine.
2. From the root directory containing the `docker-compose.yml` file, start the database container:

```bash
docker compose up -d
```

### 2. Backend Setup (`/wedbush-backend`)

Navigate to the backend directory:

```bash
cd wedbush-backend
```

Configure your environment variables in your application properties to include your Gemini API Key and match the Docker database credentials.

Run the Spring Boot application:

```bash
./mvnw spring-boot:run
```

### 3. Frontend Setup (`/wedbush-frontend`)

Open a new terminal and navigate to the frontend directory:

```bash
cd wedbush-frontend
```

Install the necessary dependencies:

```bash
npm install
```

Start the Vite development server:

```bash
npm run dev
```

The application will be available locally after both backend and frontend services are running.
