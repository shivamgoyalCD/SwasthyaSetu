SwasthyaSetu
<p align="center"> <img src="https://img.shields.io/badge/Backend-Java%2017%20%7C%20Spring%20Boot-brightgreen" alt="Backend" /> <img src="https://img.shields.io/badge/Frontend-React%20%7C%20TailwindCSS-61DAFB" alt="Frontend" /> <img src="https://img.shields.io/badge/Database-MySQL-orange" alt="Database" /> <img src="https://img.shields.io/badge/Realtime-WebSocket%20%7C%20WebRTC-blue" alt="Realtime" /> <img src="https://img.shields.io/badge/Auth-JWT%20%7C%20RBAC-red" alt="Auth" /> <img src="https://img.shields.io/badge/AI-LLM%20Live%20Translation-purple" alt="AI" /> <img src="https://img.shields.io/badge/Build-Maven-C71A36" alt="Build" /> </p> <p align="center"> <b>AI-powered telehealth and doctor consultation platform for rural and remote healthcare access</b> </p> <p align="center"> SwasthyaSetu enables doctor discovery, appointment booking, real-time chat, audio/video consultation, e-prescriptions, and multilingual communication through live AI translation. </p>
Table of Contents
Overview
Problem Statement
Why This Project Stands Out
Features
Patient Features
Doctor Features
Admin Features
Live Translation Engine
Tech Stack
System Architecture
Architecture Diagram
Appointment Booking Flow
Consultation Flow
Database Design
Entity Relationship Diagram
Module Breakdown
API Overview
Realtime Events
Project Structure
Getting Started
Environment Variables
Deployment Architecture
Security
Scalability
Testing Strategy
Roadmap
Screenshots
Resume Value
Author
Overview

SwasthyaSetu is a full-stack telehealth platform built to improve healthcare accessibility for rural and underserved users. It allows patients to search doctors, book appointments, join chat or audio/video consultations, receive digital prescriptions, and overcome language barriers through live AI translation.

The project combines Java Spring Boot, React, MySQL, WebSocket, WebRTC, and LLM-based translation to simulate a real-world healthcare product with production-grade workflows.

Problem Statement

Patients in rural and semi-urban areas often face major barriers in accessing quality healthcare:

limited access to nearby specialists
long travel distances
language mismatch between doctor and patient
inefficient appointment scheduling
poor continuity of prescriptions and consultation history

Doctors also face operational problems such as:

manual slot handling
fragmented patient context
inefficient follow-up communication
weak digital consultation workflows

SwasthyaSetu solves these challenges by providing a secure, real-time telemedicine platform with appointment management, consultation support, prescription workflows, and multilingual communication.

Why This Project Stands Out
Real-world healthcare use case with strong product relevance
End-to-end consultation lifecycle
Secure role-based architecture
WebRTC-based audio/video communication
WebSocket-powered real-time chat and signaling
LLM-powered multilingual translation layer
E-prescription and consultation history support
Modular architecture that can scale from monolith to microservices
Strong resume value for backend, full-stack, and system design roles
Features
Patient Features
Secure registration and login
Search doctors by specialization, language, and availability
View doctor profiles and consultation modes
Book, reschedule, and cancel appointments
Join chat, audio, or video consultations
Receive translated chat messages and call transcripts
Access e-prescriptions and consultation summaries
Track appointment history and medical records
Receive reminders and notifications
Doctor Features
Doctor onboarding and profile management
Set availability slots and consultation timings
Accept or reject appointments
Join consultation room with patient
Access patient history during consultation
Create digital prescriptions
Add treatment notes and follow-up guidance
Maintain consultation records for continuity of care
Admin Features
Verify doctor registrations
Manage patient and doctor accounts
Monitor consultations and appointments
View platform analytics
Track operational activity and usage
Moderate misuse and flagged issues
Live Translation Engine

One of the strongest differentiators of SwasthyaSetu is its AI-powered translation layer.

What it does
translates chat messages in real time
converts speech to text during consultations
translates transcripts into the user’s preferred language
preserves translated communication history for future reference
Example Use Case
patient speaks in Hindi
doctor prefers English
patient audio is transcribed
transcript is translated into English
doctor responds in English
response is translated into Hindi for the patient

This allows consultations to continue smoothly even when doctor and patient do not speak the same language.

Tech Stack
Frontend
React
Tailwind CSS
React Router
Axios
Zustand / Context API
WebSocket/STOMP client
WebRTC integration
Backend
Java 17
Spring Boot
Spring Security
Spring Data JPA
Hibernate
Spring WebSocket
Maven
Database
MySQL
Realtime
WebSocket / STOMP
WebRTC
Authentication & Authorization
JWT Authentication
BCrypt Password Hashing
Role-Based Access Control
AI / Integrations
LLM / Translation API
Speech-to-Text integration
Optional Text-to-Speech integration
DevOps / Deployment
Docker
Nginx
Railway / Render / AWS / VPS
System Architecture

SwasthyaSetu follows a modular architecture:

React frontend for patient, doctor, and admin interfaces
Spring Boot backend for REST APIs, business logic, scheduling, and authorization
WebSocket/STOMP for real-time chat and signaling
WebRTC for peer-to-peer audio/video consultation
MySQL for persistence
LLM / Translation service for multilingual consultation support
Architecture Diagram
Appointment Booking Flow
Consultation Flow
Database Design

The database is centered around users, doctors, patients, appointments, consultations, prescriptions, chat messages, and notifications.

Main Tables
users
roles
patients
doctors
doctor_availability
appointments
consultation_sessions
chat_messages
prescriptions
prescription_items
medical_records
notifications
translations
Entity Relationship Diagram
Module Breakdown
1. Authentication Module

Handles:

signup/login
JWT creation and validation
role-based authorization
secured route access
2. User Management Module

Handles:

patient profile
doctor profile
admin verification flow
account lifecycle basics
3. Doctor Discovery Module

Handles:

doctor search
specialization filter
language filter
availability lookup
4. Appointment Module

Handles:

slot creation
appointment booking
cancellation and rescheduling
status transitions
5. Chat Module

Handles:

real-time text messaging
room-specific chat
message persistence
translated message storage
6. Consultation Module

Handles:

call join/leave logic
WebRTC signaling
consultation room state
session lifecycle management
7. Translation Module

Handles:

chat translation
transcript translation
language mapping
translated history persistence
8. Prescription Module

Handles:

e-prescription creation
medicine instructions
prescription history
consultation summary linkage
9. Notification Module

Handles:

confirmations
reminders
cancellations
consultation alerts
10. Admin Module

Handles:

doctor approval
user oversight
analytics
moderation
API Overview
Auth
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
GET  /api/v1/auth/me
Doctors
GET  /api/v1/doctors
GET  /api/v1/doctors/{id}
PUT  /api/v1/doctors/profile
POST /api/v1/doctors/availability
GET  /api/v1/doctors/availability/{doctorId}
Appointments
POST /api/v1/appointments
GET  /api/v1/appointments/my
PUT  /api/v1/appointments/{id}/reschedule
PUT  /api/v1/appointments/{id}/cancel
PUT  /api/v1/appointments/{id}/accept
PUT  /api/v1/appointments/{id}/reject
Chat
GET /api/v1/chats/{appointmentId}/messages
Prescriptions
POST /api/v1/prescriptions
GET  /api/v1/prescriptions/{appointmentId}
GET  /api/v1/prescriptions/patient/{patientId}
Admin
GET /api/v1/admin/doctors/pending
PUT /api/v1/admin/doctors/{id}/verify
GET /api/v1/admin/analytics/overview
GET /api/v1/admin/users
Realtime Events
Chat Events
SEND_MESSAGE
RECEIVE_MESSAGE
MESSAGE_TRANSLATED
USER_TYPING
USER_STOPPED_TYPING
Consultation Events
CALL_INITIATED
CALL_ACCEPTED
CALL_REJECTED
CALL_ENDED
SDP_OFFER
SDP_ANSWER
ICE_CANDIDATE
Appointment / Notification Events
APPOINTMENT_BOOKED
APPOINTMENT_UPDATED
APPOINTMENT_CANCELLED
CONSULTATION_REMINDER
Project Structure
swasthyasetu/
│
├── backend/
│   ├── src/main/java/com/swasthyasetu/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── enums/
│   │   ├── exception/
│   │   ├── mapper/
│   │   ├── repository/
│   │   ├── security/
│   │   ├── service/
│   │   ├── websocket/
│   │   └── SwasthyaSetuApplication.java
│   │
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/
│   │
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── api/
│   │   ├── assets/
│   │   ├── components/
│   │   ├── features/
│   │   ├── hooks/
│   │   ├── layouts/
│   │   ├── pages/
│   │   ├── routes/
│   │   ├── store/
│   │   ├── utils/
│   │   └── main.jsx
│   │
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
├── docs/
│   ├── diagrams/
│   └── screenshots/
│
└── README.md
Getting Started
Prerequisites
Java 17+
Maven
Node.js 18+
MySQL
Git
Clone Repository
git clone https://github.com/your-username/swasthyasetu.git
cd swasthyasetu
Backend Setup
cd backend
mvn clean install
mvn spring-boot:run
Example application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/swasthyasetu
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

jwt:
  secret: your_jwt_secret_key
  expiration: 86400000
Frontend Setup
cd frontend
npm install
npm run dev
Example .env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_WS_URL=http://localhost:8080/ws
VITE_STUN_SERVER=stun:stun.l.google.com:19302
Environment Variables
Backend
DB_URL=jdbc:mysql://localhost:3306/swasthyasetu
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_secret_key
JWT_EXPIRY=86400000
TRANSLATION_API_KEY=your_translation_api_key
Frontend
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_WS_URL=http://localhost:8080/ws
VITE_STUN_SERVER=stun:stun.l.google.com:19302
Deployment Architecture
Suggested Deployment
Frontend: Vercel / Netlify / Nginx static hosting
Backend: Railway / Render / AWS EC2 / VPS
Database: Managed MySQL or cloud VM
Media Connectivity: STUN/TURN server for reliable call setup
Reverse Proxy: Nginx for API routing and WebSocket upgrades
Security
JWT-secured APIs
BCrypt password hashing
role-based access for Patient / Doctor / Admin
protected consultation access for valid participants only
request payload validation
chat input sanitization
secure prescription ownership checks
audit-friendly access control flow
HTTPS-ready deployment structure
Scalability
Current Architecture
single Spring Boot application
single MySQL database
modular monolith design
WebSocket for signaling and chat
WebRTC for media exchange
direct AI translation integration
Future Scaling Path
split into microservices:
auth-service
appointment-service
consultation-service
notification-service
translation-service
Redis for caching and transient session state
Kafka / RabbitMQ for event-driven workflows
object storage for reports and prescription files
Prometheus + Grafana for monitoring
API gateway and rate limiting
dedicated media infrastructure for high-volume consultations
Testing Strategy
Backend
service layer unit tests
controller integration tests
repository tests
authentication and authorization tests
Frontend
component tests
form validation tests
route guard tests
state management tests
Realtime
chat delivery tests
consultation room access tests
WebRTC signaling flow tests
End-to-End
patient booking flow
doctor acceptance flow
consultation completion flow
e-prescription generation flow
translated consultation flow
Roadmap
 User authentication and roles
 Doctor onboarding and availability
 Appointment booking flow
 Real-time chat
 WebRTC-based consultation
 E-prescription generation
 Admin verification flow
 AI-powered live translation
 Medical report uploads
 Payment gateway integration
 Family profile support
 Emergency priority booking
 AI symptom pre-screening
 Regional voice assistant
 Analytics dashboard with deeper healthcare insights
Screenshots

Add images in these paths if you want screenshots to show in GitHub:

docs/screenshots/landing-page.png
docs/screenshots/doctor-search.png
docs/screenshots/booking-page.png
docs/screenshots/consultation-room.png
docs/screenshots/prescription-view.png
docs/screenshots/admin-dashboard.png

Example usage:

## Screenshots

### Landing Page
![Landing Page](docs/screenshots/landing-page.png)

### Doctor Search
![Doctor Search](docs/screenshots/doctor-search.png)

### Booking Page
![Booking Page](docs/screenshots/booking-page.png)

### Consultation Room
![Consultation Room](docs/screenshots/consultation-room.png)

### Prescription View
![Prescription View](docs/screenshots/prescription-view.png)

### Admin Dashboard
![Admin Dashboard](docs/screenshots/admin-dashboard.png)
Resume Value

SwasthyaSetu is a strong resume project because it demonstrates:

full-stack product development
backend architecture with Spring Boot
secure authentication and authorization
real-time systems with WebSocket and WebRTC
AI/LLM integration for user-facing functionality
appointment lifecycle and healthcare workflow design
database modeling and production-style modular architecture
Resume-Ready Description

Built SwasthyaSetu, an AI-powered telehealth platform enabling doctor discovery, appointment booking, real-time chat, WebRTC-based audio/video consultation, multilingual live translation, and e-prescriptions using Java, Spring Boot, React, Tailwind, WebSocket, WebRTC, JWT, and MySQL.

Author

Shivam Goyal

GitHub: https://github.com/your-username
LinkedIn: https://linkedin.com/in/your-profile
<p align="center"> <b>SwasthyaSetu brings accessibility, real-time care, and multilingual consultation into one modern digital healthcare platform.</b> </p>
