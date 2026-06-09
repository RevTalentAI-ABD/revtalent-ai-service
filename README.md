# RevTalent AI Assistant & Document RAG Service

The **AI Service** is the cognitive layer of the **RevTalent** microservices ecosystem. It provides intelligent HR assistance, resumes screening match scoring, policy generation, performance appraisal summarization, and context-aware document queries utilizing Retrieval-Augmented Generation (RAG).

---

## Core AI Engines & Models

The service interfaces directly with local or remote AI components to run inference and vector operations:

### 1. Large Language Model (Inference)
- **Model Engine**: **Ollama** (`http://localhost:11434`)
- **LLM Model**: **`mistral`** (Mistral-7B)
- **Use Cases**:
  - **HR Chatbot (`askHR`)**: Answers HR questions.
  - **Resume Screening (`screenResume`)**: Evaluates raw candidate resume text against job requirements, returning a structured summary and scoring format (e.g. `SCORE: 85`).
  - **Performance Summary (`performanceSummary`)**: Aggregates employee review details to determine highlight strengths and improvement milestones.
  - **Policy Architect (`generatePolicy`)**: Automatically drafts HR compliant handbooks.

### 2. High-Dimensional Vector Embeddings
- **Model Engine**: **Ollama Embeddings API** (`/api/embeddings`)
- **Embedding Model**: **`nomic-embed-text`**
- **Use Case**: Converts document text chunks and search prompts into 768-dimensional dense vectors to support similarity search.

### 3. Vector Database (ChromaDB)
- **Engine**: **ChromaDB** (`http://localhost:8000`)
- **Collection Name**: `hr_documents`
- **Use Case**: Indexes text chunks alongside their vector embeddings. Enables prompt querying with custom metadata filtering (e.g., scoped by `userId`) to retrieve the top 4 most relevant text segments for injecting context into prompt requests (RAG).

---

## Document Processing & Parsing

The service utilizes Java libraries to parse unstructured data for indexing:
- **Apache PDFBox** (`pdfbox`): Reads and extracts raw text segments from PDF manuals, contracts, or forms.
- **Apache POI** (`poi-ooxml`): Parses Microsoft Word (`.docx`) and Excel (`.xlsx`) templates.
- **Text Splitter Service**: Groups extracted text blocks into clean chunks, generating vector maps dynamically to write to ChromaDB.

---

## Chat History Persistence

Conversations between employees and the AI HR Assistant are recorded inside **MongoDB** (`chat_history` collection), maintaining session logs across devices and logins.

---

## Dependencies Added

The following packages are declared in the service's `pom.xml`:

- **Apache PDFBox** (`org.apache.pdfbox:pdfbox`): Parses PDF text content.
- **Apache POI OOXML** (`org.apache.poi:poi-ooxml`): Parses Excel and Word files.
- **Spring Data MongoDB** (`spring-boot-starter-data-mongodb`): Saves user chat conversations in MongoDB.
- **Spring Data JPA** (`spring-boot-starter-data-jpa`): Maps database tables in MySQL.
- **MySQL Driver** (`mysql-connector-j`): SQL driver runtime client.
- **Spring Boot Security & JJWT**: Decrypts user identities from bearer token headers.
- **Spring Boot Web** (`spring-boot-starter-web`): Exposes AI controller API routes.
- **Netflix Eureka Client** (`spring-cloud-starter-netflix-eureka-client`): Registers with Eureka server.
- **Spring Cloud Config Client** (`spring-cloud-starter-config`): Loads external Ollama and ChromaDB server URLs.
- **Lombok** (`lombok`): Eliminates boilerplate Java code.
- **Jacoco Testing Quality Gate** (`jacoco-maven-plugin`): Monitors unit testing coverage.
