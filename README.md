
<h1 align="center">SwasthyaSetu</h1>

<p align="center">
  <strong>AI-Powered Telehealth & Doctor Consultation Platform</strong>
</p>

<p align="center">
  <em>Bridging healthcare gaps with real-time consultations, AI-powered live translation, and digital prescriptions.</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot 3.2.5" />
  <img src="https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React 18" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL 16" />
  <img src="https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis 7" />
  <img src="https://img.shields.io/badge/WebRTC-Enabled-333333?style=for-the-badge&logo=webrtc&logoColor=white" alt="WebRTC" />
  <img src="https://img.shields.io/badge/AI-Live_Translation-FF6F00?style=for-the-badge&logo=openai&logoColor=white" alt="AI Translation" />
</p>

<p align="center">
  <a href="#features">Features</a> &bull;
  <a href="#live-translation-engine">Translation Engine</a> &bull;
  <a href="#system-architecture">Architecture</a> &bull;
  <a href="#getting-started">Getting Started</a> &bull;
  <a href="#roadmap">Roadmap</a>
</p>

---

## Overview

**SwasthyaSetu** (&#2360;&#2381;&#2357;&#2366;&#2360;&#2381;&#2341;&#2381;&#2351;&#2360;&#2375;&#2340;&#2369; &mdash; *"Bridge to Health"*) is a full-stack telehealth platform built to improve healthcare accessibility for rural and underserved communities. It enables patients to discover doctors, book appointments, join real-time chat or audio/video consultations, receive digital prescriptions, and communicate across language barriers through **live AI-powered translation**.

> Built as a distributed microservices system with Java 21, Spring Boot, Spring Cloud Gateway, React, PostgreSQL, Redis, MinIO, WebSocket, and WebRTC &mdash; designed to simulate a production-grade healthcare product.

---

## Problem Statement

### For Patients
Patients in rural and semi-urban areas face critical barriers: limited access to specialists, long travel distances, language mismatches with doctors, inefficient scheduling, and poor continuity of prescriptions and consultation history.

### For Doctors
Doctors deal with manual slot management, fragmented patient context, inefficient follow-up workflows, and weak digital consultation tooling.

### The Solution
SwasthyaSetu provides a secure, real-time telemedicine platform with end-to-end appointment management, consultation support, prescription workflows, and multilingual communication &mdash; all in one place.

---

## Features

### Patient Portal
- Secure OTP-based phone authentication with JWT
- Search doctors by specialization, language & availability
- Book, reschedule & cancel appointments
- Real-time chat with AI-translated messages
- Audio/video consultations via WebRTC
- Access e-prescriptions & consultation summaries
- Track appointment history
- Appointment reminders & notifications

### Doctor Dashboard
- Onboarding & profile management with document upload
- Configurable weekly availability slots
- Patient queue management for the day
- Join consultation rooms with full patient context
- Create digital prescriptions with AI-generated summaries
- PDF prescription generation & download

### Admin Console
- Verify or reject doctor registrations
- View pending doctor applications
- Manage patient & doctor accounts

---

## Live Translation Engine

One of SwasthyaSetu's strongest differentiators &mdash; an **AI-powered translation layer** that breaks language barriers during consultations.

### How It Works

```mermaid
sequenceDiagram
    participant P as Patient (Hindi)
    participant FE as Frontend
    participant CS as Chat Service
    participant TS as Translation Service
    participant D as Doctor (English)

    P->>FE: Types message in Hindi
    FE->>CS: Send message via WebSocket
    CS->>TS: POST /translate/text (Hindi to English)
    TS-->>CS: Translated text
    CS-->>D: Deliver with original + translated content

    D->>FE: Responds in English
    FE->>CS: Send message via WebSocket
    CS->>TS: POST /translate/text (English to Hindi)
    TS-->>CS: Translated text
    CS-->>P: Deliver with original + translated content
```

### Capabilities
- Real-time chat message translation via LLM
- Both original and translated messages preserved
- Language preference per user
- Translated communication history for future reference

---

## Tech Stack

| Layer | Technology |
|:------|:-----------|
| **Frontend** | React 18, Vite 5, Tailwind CSS, React Router 6, Axios |
| **API Gateway** | Spring Cloud Gateway 2023.0.3 |
| **Backend** | Java 21, Spring Boot 3.2.5, Spring Data JPA, Hibernate, Maven |
| **Database** | PostgreSQL 16 with Flyway migrations |
| **Caching** | Redis 7 (auth tokens, appointment slots, RTC sessions) |
| **Object Storage** | MinIO (doctor documents, chat attachments, prescription PDFs) |
| **Real-time** | WebSocket (chat & signaling), WebRTC (audio/video) |
| **Auth** | OTP-based phone authentication, JWT (access + refresh tokens), RBAC |
| **AI Layer** | LLM-based Translation API |
| **PDF Generation** | Apache PDFBox 3.0.2 |
| **Message Broker** | Apache Kafka (provisioned) |
| **DevOps** | Docker, Docker Compose, Nginx, multi-stage builds |

---

## System Architecture

```mermaid
graph TB
    subgraph Client["Client Layer"]
        PA["Patient App<br/>(React + Vite)"]
        DA["Doctor App<br/>(React + Vite)"]
        AA["Admin Panel<br/>(React + Vite)"]
    end

    subgraph Gateway["API Gateway (Spring Cloud Gateway :8080)"]
        GW["Route & JWT Validation"]
    end

    subgraph Services["Microservices"]
        AUTH["Auth Service :8081<br/>OTP + JWT"]
        USER["User Service :8082<br/>Profiles + MinIO"]
        APT["Appointment Service :8083<br/>Booking + Redis"]
        CHAT["Chat Service :8084<br/>WebSocket + MinIO"]
        TRANS["Translation Service :8085<br/>LLM Translation"]
        RTC["RTC Signaling :8086<br/>WebRTC + Redis"]
        RX["Prescription Service :8087<br/>PDFBox + MinIO"]
        NOTIF["Notification Service :8088"]
    end

    subgraph Data["Data Layer"]
        PG[("PostgreSQL 16")]
        REDIS[("Redis 7")]
        MINIO[("MinIO<br/>Object Storage")]
    end

    subgraph Shared["Shared Libraries"]
        DTO["common-dtos"]
        SEC["common-security"]
    end

    subgraph External["External Services"]
        LLM["LLM / Translation API"]
        STUN["STUN/TURN Server"]
    end

    PA & DA & AA -->|HTTPS / WS| GW
    GW --> AUTH & USER & APT & CHAT & TRANS & RTC & RX & NOTIF
    CHAT -->|translate| TRANS
    RTC -->|translate| TRANS
    RX -->|fetch messages| CHAT
    TRANS --> LLM
    RTC -.->|peer-to-peer media| STUN
    AUTH & APT --> REDIS
    RTC --> REDIS
    AUTH & USER & APT & CHAT & RX & NOTIF --> PG
    USER & CHAT & RX --> MINIO

    style Client fill:#dbeafe,stroke:#3b82f6,stroke-width:2px
    style Services fill:#dcfce7,stroke:#22c55e,stroke-width:2px
    style Data fill:#fef3c7,stroke:#f59e0b,stroke-width:2px
    style External fill:#fce7f3,stroke:#ec4899,stroke-width:2px
    style Shared fill:#f3e8ff,stroke:#a855f7,stroke-width:2px
```

---

## Appointment Booking Flow

```mermaid
stateDiagram-v2
    [*] --> SearchDoctor: Patient searches
    SearchDoctor --> ViewProfile: Select doctor
    ViewProfile --> SelectSlot: Choose available slot
    SelectSlot --> Booked: Confirm booking

    Booked --> Online: Patient joins waiting room
    Online --> Consultation: Start consultation
    Consultation --> Prescription: Generate e-prescription
    Prescription --> [*]

    Booked --> Cancelled: Patient cancels
    Cancelled --> [*]
```

---

## Consultation Flow

```mermaid
sequenceDiagram
    participant P as Patient
    participant FE as Frontend
    participant GW as API Gateway
    participant CS as Chat Service
    participant RTC as RTC Signaling
    participant TS as Translation Service
    participant RX as Prescription Service
    participant D as Doctor

    P->>FE: Join consultation room
    FE->>GW: Start conversation (appointmentId)
    GW->>CS: POST /chat/conversations/start
    CS-->>FE: Conversation ID

    FE->>CS: Connect WebSocket /ws/chat
    D->>CS: Connect WebSocket /ws/chat

    Note over P,D: Chat Phase
    P->>CS: Send chat message
    CS->>TS: Translate message
    TS-->>CS: Translated text
    CS-->>D: Deliver original + translated message

    Note over P,D: Video Call Phase
    D->>RTC: Create RTC room
    D->>RTC: SDP Offer via WebSocket /ws/rtc
    RTC-->>P: Forward SDP Offer
    P->>RTC: SDP Answer
    RTC-->>D: Forward SDP Answer
    P->>RTC: ICE Candidates
    RTC-->>D: ICE Candidates

    Note over P,D: Peer-to-peer media stream established

    D->>RX: Start consultation session
    D->>RX: End consultation (generates summary)
    RX->>CS: Fetch chat messages for appointment
    CS-->>RX: Message history
    RX-->>D: Consultation summary
    D->>RX: Generate prescription PDF
    RX-->>P: Prescription available for download
```

---

## Database Design

```mermaid
erDiagram
    USERS ||--o| DOCTOR_PROFILE : "has"
    DOCTOR_PROFILE ||--|{ DOCTOR_DOCUMENTS : "uploads"

    USERS ||--|{ DOCTOR_AVAILABILITY : "sets"
    USERS ||--|{ APPOINTMENTS : "books/receives"

    APPOINTMENTS ||--o| CONVERSATIONS : "has"
    CONVERSATIONS ||--|{ MESSAGES : "contains"
    MESSAGES ||--o{ ATTACHMENTS : "has"

    APPOINTMENTS ||--o| CONSULTATIONS : "triggers"
    CONSULTATIONS ||--o| LLM_SUMMARY : "generates"
    CONSULTATIONS ||--o| PRESCRIPTIONS : "generates"

    USERS ||--|{ NOTIFICATIONS : "receives"

    USERS {
        uuid id PK
        varchar phone UK
        varchar role
        varchar name
        varchar language
        timestamp created_at
    }
    DOCTOR_PROFILE {
        uuid id PK
        uuid user_id FK, UK
        varchar specialization
        varchar license_no
        varchar status
        int experience_years
        timestamp created_at
    }
    DOCTOR_DOCUMENTS {
        uuid id PK
        uuid doctor_id FK
        varchar doc_type
        varchar s3_key
        varchar status
        timestamp created_at
    }
    DOCTOR_AVAILABILITY {
        uuid id PK
        uuid doctor_id FK
        int day_of_week
        time start_time
        time end_time
        int slot_minutes
        int buffer_minutes
    }
    APPOINTMENTS {
        uuid id PK
        uuid patient_id FK
        uuid doctor_id FK
        timestamp start_ts
        timestamp end_ts
        varchar status
        timestamp created_at
    }
    CONVERSATIONS {
        uuid id PK
        uuid appointment_id FK, UK
        uuid patient_id
        uuid doctor_id
        varchar status
        timestamp created_at
    }
    MESSAGES {
        uuid id PK
        uuid conversation_id FK
        uuid sender_id
        varchar type
        text content
        varchar original_lang
        text translated_content
        timestamp created_at
    }
    ATTACHMENTS {
        uuid id PK
        uuid message_id FK
        varchar s3_key
        varchar mime
        bigint size
        timestamp created_at
    }
    CONSULTATIONS {
        uuid id PK
        uuid appointment_id FK, UK
        varchar status
        timestamp started_at
        timestamp ended_at
    }
    LLM_SUMMARY {
        uuid id PK
        uuid consultation_id FK, UK
        text json_summary
        timestamp created_at
    }
    PRESCRIPTIONS {
        uuid id PK
        uuid consultation_id FK, UK
        varchar pdf_s3_key
        timestamp created_at
    }
    NOTIFICATIONS {
        uuid id PK
        uuid user_id
        varchar type
        text payload
        varchar status
        int retries
        timestamp created_at
    }
```

---

## Service Breakdown

| # | Service | Port | Responsibilities |
|:--|:--------|:-----|:-----------------|
| 1 | **API Gateway** | 8080 | Request routing, path rewriting, JWT validation, WebSocket proxy |
| 2 | **Auth Service** | 8081 | OTP request/verify, JWT generation, token refresh, Redis token cache |
| 3 | **User Service** | 8082 | Patient & doctor profiles, doctor onboarding, document uploads (MinIO), admin verification |
| 4 | **Appointment Service** | 8083 | Availability management, slot booking, cancellation, waiting room, doctor queue |
| 5 | **Chat Service** | 8084 | Real-time messaging via WebSocket/STOMP, message persistence, translation integration, attachments (MinIO) |
| 6 | **Translation Service** | 8085 | LLM-based text translation, language detection |
| 7 | **RTC Signaling Service** | 8086 | WebRTC SDP offer/answer relay, ICE candidate exchange, Redis session management |
| 8 | **Prescription Service** | 8087 | Consultation sessions, summary generation from chat history, PDF prescription generation (PDFBox), MinIO storage |
| 9 | **Notification Service** | 8088 | Appointment notifications, reminders, status updates |

### Shared Libraries

| Library | Purpose |
|:--------|:--------|
| **common-dtos** | Shared DTOs: `ApiResponse`, `ApiError`, `PageResponse` |
| **common-security** | JWT utilities (`JwtUtil`, `JwtAuthenticationFilter`), Role enum |

---

## API Reference

<details>
<summary><strong>Auth Service</strong> &mdash; <code>/api/v1/auth</code></summary>

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `POST` | `/request-otp` | Request OTP for phone number |
| `POST` | `/verify-otp` | Verify OTP and receive JWT tokens |
| `POST` | `/refresh` | Refresh access token |

</details>

<details>
<summary><strong>User Service</strong> &mdash; <code>/api/v1/users</code></summary>

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `GET` | `/me` | Get current user profile |
| `PATCH` | `/me` | Update current user profile |
| `POST` | `/doctor/onboard` | Submit doctor onboarding details |
| `POST` | `/doctor/docs/presign` | Get presigned URL for document upload |
| `GET` | `/admin/doctors/pending` | List pending doctor verifications |
| `POST` | `/admin/doctor/{doctorId}/verify` | Approve a doctor |
| `POST` | `/admin/doctor/{doctorId}/reject` | Reject a doctor |

</details>

<details>
<summary><strong>Appointment Service</strong> &mdash; <code>/api/v1/appointments</code></summary>

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `POST` | `/book` | Book an appointment |
| `POST` | `/{id}/cancel` | Cancel an appointment |
| `POST` | `/{id}/join-waiting-room` | Patient joins waiting room |
| `GET` | `/slots?doctorId=&date=` | Get available slots for a doctor on a date |
| `POST` | `/doctor/availability` | Set doctor's weekly availability |
| `GET` | `/doctor/availability` | Get doctor's availability config |
| `GET` | `/doctor/queue?doctorId=` | Get doctor's patient queue for today |

</details>

<details>
<summary><strong>Chat Service</strong> &mdash; <code>/api/v1/chat</code></summary>

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `POST` | `/conversations/start?appointmentId=` | Start a conversation |
| `GET` | `/conversations/{id}/messages` | Get paginated messages (cursor-based) |
| `POST` | `/conversations/{id}/messages` | Send a message |
| `GET` | `/appointments/{appointmentId}/messages` | Get all messages for an appointment |
| `POST` | `/attachments/presign` | Get presigned URL for attachment upload |

**WebSocket:** `ws://gateway:8080/ws/chat` (plain WebSocket, JSON messages)

</details>

<details>
<summary><strong>Translation Service</strong> &mdash; <code>/api/v1/translate</code></summary>

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `POST` | `/text` | Translate text between languages |

</details>

<details>
<summary><strong>RTC Signaling Service</strong> &mdash; <code>/api/v1/rtc</code></summary>

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `POST` | `/room/create?appointmentId=` | Create an RTC room |
| `GET` | `/room/{appointmentId}/status` | Get room status |

**WebSocket:** `ws://gateway:8080/ws/rtc` (SDP & ICE exchange)

</details>

<details>
<summary><strong>Prescription Service</strong> &mdash; <code>/api/v1/prescriptions</code></summary>

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `POST` | `/consultations/start?appointmentId=` | Start a consultation session |
| `POST` | `/consultations/end?consultationId=` | End consultation (generates summary from chat history) |
| `PATCH` | `/consultations/{id}/summary` | Update consultation summary |
| `POST` | `/consultations/{id}/prescription/generate` | Generate prescription PDF |
| `GET` | `/{id}/download` | Download a prescription |

</details>

<details>
<summary><strong>Notification Service</strong> &mdash; <code>/api/v1/notifications</code></summary>

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `POST` | `/notify` | Send a notification (internal) |

</details>

---

## Real-time Events

<details>
<summary><strong>WebSocket Event Reference</strong></summary>

**Chat Events** (`/ws/chat`)
| Event | Description |
|:------|:------------|
| `SEND_MESSAGE` | Client sends a chat message |
| `RECEIVE_MESSAGE` | Server broadcasts message to room |
| `MESSAGE_TRANSLATED` | Translated version delivered |
| `USER_TYPING` | Typing indicator on |
| `USER_STOPPED_TYPING` | Typing indicator off |

**RTC Signaling Events** (`/ws/rtc`)
| Event | Description |
|:------|:------------|
| `SDP_OFFER` | WebRTC SDP offer |
| `SDP_ANSWER` | WebRTC SDP answer |
| `ICE_CANDIDATE` | ICE candidate exchange |
| `CALL_INITIATED` | Call started |
| `CALL_ENDED` | Call terminated |

</details>

---

## Project Structure

```
swasthyasetu/
├── pom.xml                         # Parent Maven POM
├── services/
│   ├── pom.xml                     # Services parent POM
│   ├── api-gateway/                # Spring Cloud Gateway (port 8080)
│   ├── auth-service/               # OTP + JWT auth (port 8081)
│   ├── user-service/               # Profiles & onboarding (port 8082)
│   ├── appointment-service/        # Booking & availability (port 8083)
│   ├── chat-service/               # Real-time messaging (port 8084)
│   ├── translation-service/        # LLM translation (port 8085)
│   ├── rtc-signaling-service/      # WebRTC signaling (port 8086)
│   ├── prescription-service/       # E-prescriptions (port 8087)
│   └── notification-service/       # Notifications (port 8088)
├── libs/
│   ├── common-dtos/                # Shared DTOs
│   └── common-security/            # JWT utils & auth filters
├── frontend/
│   ├── src/
│   │   ├── api/                    # Axios API clients per service
│   │   ├── components/             # Reusable UI components
│   │   ├── hooks/                  # Custom React hooks
│   │   ├── layouts/                # Page layouts (AppShell)
│   │   ├── pages/                  # Route pages
│   │   ├── routes/                 # Route definitions & guards
│   │   ├── store/                  # Session state
│   │   └── utils/                  # Auth & navigation helpers
│   ├── Dockerfile                  # Multi-stage: Node 20 -> Nginx 1.27
│   ├── package.json
│   └── vite.config.js
├── infra/
│   ├── docker-compose.yml          # Full stack orchestration
│   └── .env                        # Environment configuration
└── README.md
```

---

## Getting Started

### Prerequisites

| Tool | Version |
|:-----|:--------|
| Docker | 20+ |
| Docker Compose | v2+ |
| Git | Latest |

> For local development without Docker, you also need: Java 21, Maven 3.9+, Node.js 20+, PostgreSQL 16, Redis 7, and MinIO.

### 1. Clone the Repository

```bash
git clone https://github.com/shivamgoyalCD/SwasthyaSetu.git
cd SwasthyaSetu
```

### 2. Start with Docker Compose

```bash
cd infra
docker compose up --build
```

This starts the entire stack: PostgreSQL, Redis, Kafka, MinIO, all 9 backend services, the API gateway, and the frontend.

| Component | URL |
|:----------|:----|
| Frontend | http://localhost:5173 |
| API Gateway | http://localhost:8080 |
| MinIO Console | http://localhost:9001 |

### 3. Environment Configuration

The default configuration in `infra/.env` works out of the box for local development. Key settings:

```env
# PostgreSQL
POSTGRES_USER=postgres
POSTGRES_DB=swasthyasetu

# JWT
JWT_ACCESS_TTL=PT15M          # 15-minute access tokens
JWT_REFRESH_TTL=P7D            # 7-day refresh tokens

# MinIO
MINIO_BUCKET=swasthyasetu

# Frontend
FRONTEND_PORT=5173
API_GATEWAY_PORT=8080
VITE_STUN_SERVER=stun:stun.l.google.com:19302
```

### 4. Local Development (without Docker)

<details>
<summary>Click to expand</summary>

**Start infrastructure services:**
```bash
# Start PostgreSQL, Redis, and MinIO manually or via:
cd infra
docker compose up postgres redis minio minio-init -d
```

**Build shared libraries:**
```bash
# From project root
mvn clean install -pl libs/common-dtos,libs/common-security
```

**Run a backend service:**
```bash
cd services/auth-service
mvn spring-boot:run
```

**Run the frontend:**
```bash
cd frontend
npm install
npm run dev
```

</details>

---

## API Gateway Routes

All client requests pass through the API Gateway at port 8080:

| Route Pattern | Target Service |
|:-------------|:---------------|
| `/api/v1/auth/**` | auth-service:8081 |
| `/api/v1/users/**` | user-service:8082 |
| `/api/v1/appointments/**` | appointment-service:8083 |
| `/api/v1/chat/**` | chat-service:8084 |
| `/api/v1/translate/**` | translation-service:8085 |
| `/api/v1/rtc/**` | rtc-signaling-service:8086 |
| `/api/v1/prescriptions/**` | prescription-service:8087 |
| `/api/v1/notifications/**` | notification-service:8088 |
| `/ws/chat` | chat-service:8084 (WebSocket) |
| `/ws/rtc` | rtc-signaling-service:8086 (WebSocket) |

---

## Security

| Layer | Implementation |
|:------|:---------------|
| **Authentication** | Phone-based OTP verification, no passwords stored |
| **Tokens** | JWT access tokens (15 min) + refresh tokens (7 days), Redis-backed |
| **Authorization** | Role-based access control (PATIENT, DOCTOR, ADMIN) |
| **API Gateway** | Centralized JWT validation on all protected routes |
| **Consultation** | Access restricted to valid appointment participants |
| **File Uploads** | Presigned URLs for secure direct-to-MinIO uploads |
| **Prescriptions** | Ownership verification before download |
| **Database** | Flyway-managed migrations, UUID primary keys |

---

## Deployment Architecture

```mermaid
graph LR
    subgraph Internet["Internet"]
        USER["Users"]
    end

    subgraph Infra["Docker Compose Stack"]
        FE["React<br/>Nginx :5173"]
        GW["Spring Cloud Gateway<br/>:8080"]
        subgraph Services["9 Microservices"]
            S1["Auth :8081"]
            S2["User :8082"]
            S3["Appointment :8083"]
            S4["Chat :8084"]
            S5["Translation :8085"]
            S6["RTC :8086"]
            S7["Prescription :8087"]
            S8["Notification :8088"]
        end
        PG[("PostgreSQL 16")]
        REDIS[("Redis 7")]
        MINIO[("MinIO")]
        KAFKA["Kafka"]
    end

    subgraph External["External"]
        LLM["LLM API"]
        STUN["STUN/TURN"]
    end

    USER -->|HTTPS| FE
    USER -->|API / WS| GW
    GW --> S1 & S2 & S3 & S4 & S5 & S6 & S7 & S8
    S1 & S3 & S6 --> REDIS
    S1 & S2 & S3 & S4 & S7 & S8 --> PG
    S2 & S4 & S7 --> MINIO
    S5 --> LLM
    USER -.->|WebRTC Media| STUN

    style Internet fill:#e0f2fe,stroke:#0284c7
    style Infra fill:#f0fdf4,stroke:#16a34a
    style External fill:#fff7ed,stroke:#ea580c
```

---

## Roadmap

- [x] OTP-based authentication & JWT token management
- [x] Doctor onboarding with document verification
- [x] Configurable weekly availability & slot management
- [x] Appointment booking with waiting room
- [x] Real-time chat via WebSocket/STOMP
- [x] AI-powered live translation
- [x] WebRTC-based audio/video consultations
- [x] AI-generated consultation summaries
- [x] PDF e-prescription generation & download
- [x] Admin doctor verification workflow
- [x] Notification service
- [ ] Kafka-based async event processing
- [ ] Medical report uploads
- [ ] Payment gateway integration
- [ ] Family profile support
- [ ] Emergency priority booking
- [ ] AI symptom pre-screening
- [ ] Prometheus + Grafana monitoring
- [ ] Regional voice assistant

---

## Author

**Shivam Goyal**

[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/shivamgoyalCD)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/shivamgoyal29)

---

<p align="center">
  <strong>If you found this project useful, consider giving it a star!</strong>
</p>
