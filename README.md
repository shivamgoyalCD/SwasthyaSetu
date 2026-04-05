<p align="center">
  <img src="docs/logo.png" alt="SwasthyaSetu Logo" width="80" />
</p>

<h1 align="center">🏥 SwasthyaSetu</h1>

<p align="center">
  <strong>AI-Powered Telehealth & Doctor Consultation Platform</strong>
</p>

<p align="center">
  <em>Bridging healthcare gaps with real-time consultations, AI-powered live translation, and digital prescriptions.</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/WebRTC-Enabled-333333?style=for-the-badge&logo=webrtc&logoColor=white" alt="WebRTC" />
  <img src="https://img.shields.io/badge/AI-Live_Translation-FF6F00?style=for-the-badge&logo=openai&logoColor=white" alt="AI Translation" />
</p>

<p align="center">
  <a href="#-features">Features</a> •
  <a href="#-live-translation-engine">Translation Engine</a> •
  <a href="#%EF%B8%8F-system-architecture">Architecture</a> •
  <a href="#-getting-started">Getting Started</a> •
  <a href="#-roadmap">Roadmap</a>
</p>

---

## 📖 Overview

**SwasthyaSetu** (स्वास्थ्यसेतु — *"Bridge to Health"*) is a full-stack telehealth platform built to improve healthcare accessibility for rural and underserved communities. It enables patients to discover doctors, book appointments, join real-time chat or audio/video consultations, receive digital prescriptions, and communicate across language barriers through **live AI-powered translation**.

> Built with Java Spring Boot, React, MySQL, WebSocket, WebRTC, and LLM-based translation — designed to simulate a production-grade healthcare product.

---

## 🎯 Problem Statement

### For Patients
Patients in rural and semi-urban areas face critical barriers: limited access to specialists, long travel distances, language mismatches with doctors, inefficient scheduling, and poor continuity of prescriptions and consultation history.

### For Doctors
Doctors deal with manual slot management, fragmented patient context, inefficient follow-up workflows, and weak digital consultation tooling.

### The Solution
SwasthyaSetu provides a secure, real-time telemedicine platform with end-to-end appointment management, consultation support, prescription workflows, and multilingual communication — all in one place.

---

## ✨ Features

### 👤 Patient Portal
- 🔐 Secure registration & JWT-based login
- 🔍 Search doctors by specialization, language & availability
- 📅 Book, reschedule & cancel appointments
- 💬 Real-time chat with AI-translated messages
- 📹 Audio/video consultations via WebRTC
- 💊 Access e-prescriptions & consultation summaries
- 📋 Track appointment history & medical records
- 🔔 Appointment reminders & notifications

### 🩺 Doctor Dashboard
- 📝 Onboarding & profile management
- 🕐 Configurable availability slots
- ✅ Accept / reject appointment requests
- 👨‍⚕️ Join consultation rooms with full patient context
- 📄 Create digital prescriptions with treatment notes
- 📂 Maintain consultation records for continuity of care

### 🛡️ Admin Console
- ✔️ Verify doctor registrations
- 👥 Manage patient & doctor accounts
- 📊 Platform analytics & usage monitoring
- 🚩 Moderate flagged issues & misuse

---

## 🌐 Live Translation Engine

One of SwasthyaSetu's strongest differentiators — an **AI-powered translation layer** that breaks language barriers during consultations.

### How It Works

```mermaid
sequenceDiagram
    participant P as 🧑 Patient (Hindi)
    participant FE as 💻 Frontend
    participant WS as 🔌 WebSocket
    participant AI as 🤖 Translation API
    participant D as 👨‍⚕️ Doctor (English)

    P->>FE: Speaks / types in Hindi
    FE->>WS: Send message
    WS->>AI: Translate Hindi → English
    AI-->>WS: Translated text
    WS-->>D: Display in English

    D->>FE: Responds in English
    FE->>WS: Send message
    WS->>AI: Translate English → Hindi
    AI-->>WS: Translated text
    WS-->>P: Display in Hindi
```

### Capabilities
- ⚡ Real-time chat message translation
- 🎙️ Speech-to-text during live consultations
- 📝 Transcript translation into user's preferred language
- 🗂️ Translated communication history preserved for future reference

---

## 🛠️ Tech Stack

| Layer | Technology |
|:------|:-----------|
| **Frontend** | React 18, Tailwind CSS, React Router, Axios, Zustand / Context API |
| **Backend** | Java 17, Spring Boot 3, Spring Security, Spring Data JPA, Hibernate, Maven |
| **Database** | MySQL 8 |
| **Real-time** | WebSocket / STOMP (chat & signaling), WebRTC (audio/video) |
| **Auth** | JWT, BCrypt, Role-Based Access Control (RBAC) |
| **AI Layer** | LLM / Translation API, Speech-to-Text, optional Text-to-Speech |
| **DevOps** | Docker, Nginx, Railway / Render / AWS / VPS |

---

## ⚙️ System Architecture

```mermaid
graph TB
    subgraph Client["🖥️ Client Layer"]
        PA["👤 Patient App<br/>(React)"]
        DA["🩺 Doctor App<br/>(React)"]
        AA["🛡️ Admin Panel<br/>(React)"]
    end

    subgraph API["⚡ API Gateway / Nginx"]
        NG["Nginx Reverse Proxy"]
    end

    subgraph Backend["☕ Spring Boot Backend"]
        AUTH["🔐 Auth Service<br/>JWT + RBAC"]
        DOC["👨‍⚕️ Doctor Service"]
        APT["📅 Appointment Service"]
        CHAT["💬 Chat Service<br/>WebSocket/STOMP"]
        CONSULT["📹 Consultation Service<br/>WebRTC Signaling"]
        RX["💊 Prescription Service"]
        NOTIF["🔔 Notification Service"]
        TRANS["🌐 Translation Service"]
    end

    subgraph Data["🗄️ Data Layer"]
        DB[("MySQL<br/>Database")]
    end

    subgraph External["☁️ External Services"]
        LLM["🤖 LLM / Translation API"]
        STT["🎙️ Speech-to-Text"]
        STUN["📡 STUN/TURN Server"]
    end

    PA & DA & AA -->|HTTPS| NG
    NG --> AUTH & DOC & APT & CHAT & CONSULT & RX & NOTIF
    CHAT -->|translate| TRANS
    CONSULT -->|signaling| CHAT
    TRANS --> LLM
    TRANS --> STT
    CONSULT -.->|peer-to-peer media| STUN
    AUTH & DOC & APT & CHAT & RX & NOTIF --> DB

    style Client fill:#dbeafe,stroke:#3b82f6,stroke-width:2px
    style Backend fill:#dcfce7,stroke:#22c55e,stroke-width:2px
    style Data fill:#fef3c7,stroke:#f59e0b,stroke-width:2px
    style External fill:#fce7f3,stroke:#ec4899,stroke-width:2px
```

---

## 📅 Appointment Booking Flow

```mermaid
stateDiagram-v2
    [*] --> SearchDoctor: Patient searches
    SearchDoctor --> ViewProfile: Select doctor
    ViewProfile --> SelectSlot: Choose time slot
    SelectSlot --> BookingRequested: Confirm booking

    BookingRequested --> Accepted: Doctor accepts
    BookingRequested --> Rejected: Doctor rejects

    Accepted --> Reminder: Send reminder
    Reminder --> ConsultationReady: Join room
    ConsultationReady --> InProgress: Start consultation
    InProgress --> Completed: End consultation
    Completed --> Prescription: Generate e-prescription
    Prescription --> [*]

    Rejected --> [*]

    BookingRequested --> Cancelled: Patient cancels
    Cancelled --> [*]

    Accepted --> Rescheduled: Reschedule
    Rescheduled --> BookingRequested
```

---

## 📹 Consultation Flow

```mermaid
sequenceDiagram
    participant P as 🧑 Patient
    participant FE as 💻 Frontend
    participant BE as ☕ Backend
    participant WS as 🔌 WebSocket
    participant AI as 🤖 Translation
    participant D as 👨‍⚕️ Doctor

    P->>FE: Join consultation room
    FE->>BE: Validate appointment access
    BE-->>FE: Access granted ✅

    FE->>WS: Connect to room
    D->>WS: Connect to room

    Note over P,D: 💬 Chat Phase
    P->>WS: Send chat message
    WS->>AI: Translate message
    AI-->>WS: Translated text
    WS-->>D: Deliver translated message

    Note over P,D: 📹 Video Call Phase
    D->>WS: SDP Offer
    WS-->>P: Forward SDP Offer
    P->>WS: SDP Answer
    WS-->>D: Forward SDP Answer
    P->>WS: ICE Candidates
    WS-->>D: ICE Candidates

    Note over P,D: 🔗 Peer-to-peer media stream established

    D->>BE: Create e-prescription
    BE-->>P: Prescription available 💊
```

---

## 🗄️ Database Design

```mermaid
erDiagram
    USERS ||--o| PATIENTS : "is a"
    USERS ||--o| DOCTORS : "is a"
    USERS }o--|| ROLES : "has"

    DOCTORS ||--|{ DOCTOR_AVAILABILITY : "sets"
    DOCTORS ||--|{ APPOINTMENTS : "receives"
    PATIENTS ||--|{ APPOINTMENTS : "books"

    APPOINTMENTS ||--o| CONSULTATION_SESSIONS : "triggers"
    APPOINTMENTS ||--|{ CHAT_MESSAGES : "contains"
    APPOINTMENTS ||--|{ PRESCRIPTIONS : "generates"

    PRESCRIPTIONS ||--|{ PRESCRIPTION_ITEMS : "includes"
    PATIENTS ||--|{ MEDICAL_RECORDS : "owns"
    USERS ||--|{ NOTIFICATIONS : "receives"
    CHAT_MESSAGES ||--o{ TRANSLATIONS : "translated to"

    USERS {
        bigint id PK
        string email
        string password_hash
        string phone
        enum role
        timestamp created_at
    }
    DOCTORS {
        bigint id PK
        bigint user_id FK
        string specialization
        string languages
        boolean verified
    }
    APPOINTMENTS {
        bigint id PK
        bigint patient_id FK
        bigint doctor_id FK
        datetime slot_time
        enum status
        enum consultation_mode
    }
    PRESCRIPTIONS {
        bigint id PK
        bigint appointment_id FK
        text diagnosis
        text notes
        timestamp created_at
    }
```

---

## 📦 Module Breakdown

```mermaid
graph LR
    subgraph Core["🔐 Core"]
        A[Auth Module]
        U[User Management]
    end

    subgraph Discovery["🔍 Discovery"]
        DD[Doctor Discovery]
        AP[Appointment Module]
    end

    subgraph Consultation["💬 Consultation"]
        CH[Chat Module]
        CO[Consultation Module]
        TR[Translation Module]
    end

    subgraph PostCare["📋 Post-Care"]
        RX[Prescription Module]
        NO[Notification Module]
    end

    subgraph Admin["🛡️ Admin"]
        AD[Admin Module]
    end

    A --> U --> DD --> AP --> CH --> CO --> TR --> RX --> NO
    AD -.-> U & DD & AP

    style Core fill:#dbeafe,stroke:#3b82f6
    style Discovery fill:#dcfce7,stroke:#22c55e
    style Consultation fill:#fef3c7,stroke:#f59e0b
    style PostCare fill:#fce7f3,stroke:#ec4899
    style Admin fill:#f3e8ff,stroke:#a855f7
```

| # | Module | Responsibilities |
|:--|:-------|:-----------------|
| 1 | **Auth** | Signup / login, JWT creation & validation, role-based authorization |
| 2 | **User Management** | Patient & doctor profiles, admin verification, account lifecycle |
| 3 | **Doctor Discovery** | Search, specialization & language filters, availability lookup |
| 4 | **Appointment** | Slot creation, booking, cancellation, rescheduling, status transitions |
| 5 | **Chat** | Real-time messaging via WebSocket, message persistence, translation storage |
| 6 | **Consultation** | WebRTC signaling, call join/leave, session lifecycle management |
| 7 | **Translation** | Chat & transcript translation, language mapping, history persistence |
| 8 | **Prescription** | E-prescription creation, medicine instructions, consultation summary linkage |
| 9 | **Notification** | Confirmations, reminders, cancellation alerts, consultation notifications |
| 10 | **Admin** | Doctor approval, user oversight, analytics, moderation |

---

## 🔌 API Reference

<details>
<summary><strong>Auth</strong> — <code>/api/v1/auth</code></summary>

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `POST` | `/register` | Register new user |
| `POST` | `/login` | Authenticate & get JWT |
| `POST` | `/refresh` | Refresh access token |
| `GET` | `/me` | Get current user profile |

</details>

<details>
<summary><strong>Doctors</strong> — <code>/api/v1/doctors</code></summary>

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `GET` | `/` | List / search doctors |
| `GET` | `/{id}` | Get doctor profile |
| `PUT` | `/profile` | Update own profile |
| `POST` | `/availability` | Set availability slots |
| `GET` | `/availability/{doctorId}` | Get doctor availability |

</details>

<details>
<summary><strong>Appointments</strong> — <code>/api/v1/appointments</code></summary>

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `POST` | `/` | Create appointment |
| `GET` | `/my` | List my appointments |
| `PUT` | `/{id}/reschedule` | Reschedule appointment |
| `PUT` | `/{id}/cancel` | Cancel appointment |
| `PUT` | `/{id}/accept` | Doctor accepts |
| `PUT` | `/{id}/reject` | Doctor rejects |

</details>

<details>
<summary><strong>Chat</strong> — <code>/api/v1/chats</code></summary>

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `GET` | `/{appointmentId}/messages` | Get chat history |

</details>

<details>
<summary><strong>Prescriptions</strong> — <code>/api/v1/prescriptions</code></summary>

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `POST` | `/` | Create prescription |
| `GET` | `/{appointmentId}` | Get by appointment |
| `GET` | `/patient/{patientId}` | Get patient history |

</details>

<details>
<summary><strong>Admin</strong> — <code>/api/v1/admin</code></summary>

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `GET` | `/doctors/pending` | List pending verifications |
| `PUT` | `/doctors/{id}/verify` | Verify a doctor |
| `GET` | `/analytics/overview` | Platform analytics |
| `GET` | `/users` | List all users |

</details>

---

## 📡 Real-time Events

<details>
<summary><strong>WebSocket Event Reference</strong></summary>

**💬 Chat Events**
| Event | Description |
|:------|:------------|
| `SEND_MESSAGE` | Client sends a chat message |
| `RECEIVE_MESSAGE` | Server broadcasts message to room |
| `MESSAGE_TRANSLATED` | Translated version delivered |
| `USER_TYPING` | Typing indicator on |
| `USER_STOPPED_TYPING` | Typing indicator off |

**📹 Consultation Events**
| Event | Description |
|:------|:------------|
| `CALL_INITIATED` | Doctor/patient starts call |
| `CALL_ACCEPTED` | Callee accepts |
| `CALL_REJECTED` | Callee rejects |
| `CALL_ENDED` | Call terminated |
| `SDP_OFFER` | WebRTC SDP offer |
| `SDP_ANSWER` | WebRTC SDP answer |
| `ICE_CANDIDATE` | ICE candidate exchange |

**📅 Notification Events**
| Event | Description |
|:------|:------------|
| `APPOINTMENT_BOOKED` | New appointment created |
| `APPOINTMENT_UPDATED` | Appointment modified |
| `APPOINTMENT_CANCELLED` | Appointment cancelled |
| `CONSULTATION_REMINDER` | Upcoming consultation alert |

</details>

---

## 📁 Project Structure

```
swasthyasetu/
├── backend/
│   ├── src/main/java/com/swasthyasetu/
│   │   ├── config/           # App & WebSocket configuration
│   │   ├── controller/       # REST controllers
│   │   ├── dto/              # Request/Response DTOs
│   │   ├── entity/           # JPA entities
│   │   ├── enums/            # Status & role enums
│   │   ├── exception/        # Global exception handling
│   │   ├── mapper/           # Entity ↔ DTO mappers
│   │   ├── repository/       # Spring Data repositories
│   │   ├── security/         # JWT filters & security config
│   │   ├── service/          # Business logic layer
│   │   ├── websocket/        # WebSocket handlers & config
│   │   └── SwasthyaSetuApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/               # Migration scripts
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── api/              # Axios API clients
│   │   ├── assets/           # Static assets
│   │   ├── components/       # Reusable UI components
│   │   ├── features/         # Feature-specific modules
│   │   ├── hooks/            # Custom React hooks
│   │   ├── layouts/          # Page layouts
│   │   ├── pages/            # Route pages
│   │   ├── routes/           # Route definitions & guards
│   │   ├── store/            # Zustand / Context state
│   │   ├── utils/            # Helpers & utilities
│   │   └── main.jsx
│   ├── public/
│   ├── package.json
│   └── vite.config.js
├── docs/
│   ├── diagrams/
│   └── screenshots/
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version |
|:-----|:--------|
| Java | 17+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Git | Latest |

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/your-username/swasthyasetu.git
cd swasthyasetu
```

### 2️⃣ Backend Setup

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 3️⃣ Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

### 4️⃣ Environment Variables

Create `.env` files with the following:

**Backend** (`backend/application.yml` or env vars)
```env
DB_URL=jdbc:mysql://localhost:3306/swasthyasetu
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_secret_key
JWT_EXPIRY=86400000
TRANSLATION_API_KEY=your_translation_api_key
```

**Frontend** (`frontend/.env`)
```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_WS_URL=http://localhost:8080/ws
VITE_STUN_SERVER=stun:stun.l.google.com:19302
```

---

## 🚢 Deployment Architecture

```mermaid
graph LR
    subgraph Internet["🌍 Internet"]
        USER["👤 Users"]
    end

    subgraph Infra["☁️ Cloud / VPS"]
        NG["🔀 Nginx<br/>Reverse Proxy<br/>+ SSL"]
        subgraph App["Application"]
            FE["⚛️ React<br/>Static Build"]
            BE["☕ Spring Boot<br/>API Server"]
        end
        DB[("🗄️ MySQL")]
    end

    subgraph ExtServices["🔗 External"]
        TAPI["🤖 Translation API"]
        STUN["📡 STUN/TURN"]
    end

    USER -->|HTTPS| NG
    NG -->|/| FE
    NG -->|/api, /ws| BE
    BE --> DB
    BE --> TAPI
    USER -.->|WebRTC Media| STUN

    style Internet fill:#e0f2fe,stroke:#0284c7
    style Infra fill:#f0fdf4,stroke:#16a34a
    style ExtServices fill:#fff7ed,stroke:#ea580c
```

---

## 🔒 Security

| Layer | Implementation |
|:------|:---------------|
| **Authentication** | JWT-secured APIs with token refresh |
| **Password** | BCrypt hashing |
| **Authorization** | Role-based (Patient / Doctor / Admin) |
| **Consultation** | Access restricted to valid participants only |
| **Input** | Request payload validation & chat sanitization |
| **Prescriptions** | Ownership verification checks |
| **Transport** | HTTPS-ready deployment structure |
| **Audit** | Access control flow designed for audit trails |

---

## 📈 Scalability

### Current: Modular Monolith
The application is designed as a **modular monolith** — clean module boundaries that can be split into microservices when scale demands it.

### Future Scaling Path

```mermaid
graph TB
    subgraph Current["📦 Current — Modular Monolith"]
        MONO["Single Spring Boot App"]
        SQLDB[("MySQL")]
        MONO --> SQLDB
    end

    subgraph Future["🚀 Future — Microservices"]
        GW["API Gateway"]
        AS["Auth Service"]
        APS["Appointment Service"]
        CS["Consultation Service"]
        NS["Notification Service"]
        TS["Translation Service"]
        REDIS[("Redis Cache")]
        KAFKA["Kafka / RabbitMQ"]
        S3["Object Storage"]
        MON["Prometheus + Grafana"]
        
        GW --> AS & APS & CS & NS & TS
        AS & APS & CS & NS --> KAFKA
        CS --> REDIS
        TS --> REDIS
        NS --> S3
        GW --> MON
    end

    Current -->|"Scale when needed"| Future

    style Current fill:#fef3c7,stroke:#f59e0b
    style Future fill:#dbeafe,stroke:#3b82f6
```

---

## 🧪 Testing Strategy

| Layer | Tests |
|:------|:------|
| **Backend** | Service unit tests, controller integration tests, repository tests, auth/authz tests |
| **Frontend** | Component tests, form validation, route guards, state management |
| **Real-time** | Chat delivery, consultation room access, WebRTC signaling flow |
| **E2E** | Patient booking → doctor acceptance → consultation → e-prescription → translation flow |

---

## 🗺️ Roadmap

- [x] User authentication & role management
- [x] Doctor onboarding & availability management
- [x] Appointment booking lifecycle
- [x] Real-time chat via WebSocket
- [x] WebRTC-based audio/video consultations
- [x] E-prescription generation
- [x] Admin verification flow
- [x] AI-powered live translation
- [ ] Medical report uploads
- [ ] Payment gateway integration
- [ ] Family profile support
- [ ] Emergency priority booking
- [ ] AI symptom pre-screening
- [ ] Regional voice assistant
- [ ] Analytics dashboard with deeper healthcare insights

---

## 💼 Resume-Ready Description

> Built **SwasthyaSetu**, an AI-powered telehealth platform enabling doctor discovery, appointment booking, real-time chat, WebRTC-based audio/video consultation, multilingual live translation, and e-prescriptions — using **Java 17, Spring Boot, React, Tailwind CSS, WebSocket, WebRTC, JWT, and MySQL**.

---

## 👤 Author

**Shivam Goyal**

[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/your-username)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/your-profile)

---

<p align="center">
  <strong>⭐ If you found this project useful, consider giving it a star!</strong>
</p>
