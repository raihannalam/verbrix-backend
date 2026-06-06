# 📘 Verbrix Backend

Backend service for **Verbrix**, a platform designed to facilitate communication between international patients and professional interpreters.

Built with **Java 21**, **Spring Boot 3**, **PostgreSQL**, and **MongoDB**, the platform provides secure authentication, interpreter onboarding, relationship management, real-time communication, and payment processing.

---

## Overview

Verbrix is a backend system that enables healthcare communication across language barriers by connecting clients with verified interpreters.

The application provides:

* Secure authentication and authorization
* Interpreter onboarding and verification workflows
* Client–interpreter relationship management
* Real-time messaging
* Video communication
* Payment processing and transaction management
* Administrative moderation tools

---

## Features

### Authentication & Authorization

* JWT authentication (Access & Refresh Tokens)
* Email OTP verification
* Google OAuth login
* Device and session management
* Role-based access control (RBAC)

### User Management

* Multi-role architecture
* Client and Interpreter roles
* Admin-controlled verification process

### Interpreter Onboarding

* Interpreter application workflow
* Document uploads via Cloudinary
* Verification and approval system

### Relationship Management

Relationship lifecycle:

```text
REQUESTED → ACCEPTED → ACTIVE → TERMINATED
```

Features:

* Controlled access to platform services
* Relationship validation
* Duplicate request prevention

### Real-Time Chat

* WebSocket + STOMP messaging
* Persistent message storage using MongoDB
* Relationship-aware access control

### Video Calling

* LiveKit integration
* Secure session token generation
* Relationship-based session access

### Payments

* Razorpay integration
* Idempotent payment order creation
* Signature verification
* Automated refund workflow

### Administration

* Interpreter verification
* Document review
* User monitoring and management

---

## Architecture

The project follows a layered architecture.

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

### Layers

#### Controller Layer

* REST API endpoints
* Request validation
* Response handling

#### Service Layer

* Business logic
* Workflow orchestration
* Domain rules

#### Repository Layer

* PostgreSQL access via Spring Data JPA
* MongoDB access via Spring Data MongoDB

#### Security Layer

* JWT authentication
* Spring Security integration
* Role-based authorization

---

## Technology Stack

### Backend

* Java 21
* Spring Boot 3
* Spring Security
* Spring Data JPA
* Spring Data MongoDB
* WebSocket (STOMP)

### Databases

* PostgreSQL
* MongoDB

### External Services

* Razorpay
* LiveKit
* Cloudinary
* Firebase Authentication
* Resend

---

## Database Design

### PostgreSQL

Stores:

* Users
* Roles
* Interpreter Profiles
* Relationships
* Payments

### MongoDB

Stores:

* Chat Messages
* Activity Logs

---

## API Modules

| Module       | Base Endpoint    |
| ------------ | ---------------- |
| Auth         | `/auth`          |
| Interpreter  | `/interpreters`  |
| Client       | `/clients`       |
| Relationship | `/relationships` |
| Chat         | `/chat`          |
| Payment      | `/payments`      |
| Video        | `/video`         |

All APIs are protected using JWT-based authentication and role-based authorization.

---

## Real-Time Communication

### WebSocket Endpoint

```text
/ws
```

### Chat Topic

```text
/topic/chat/{relationshipId}
```

### Message Flow

1. Validate relationship status
2. Persist message
3. Broadcast to subscribers

---

## Security

* JWT authentication
* BCrypt password hashing
* Role-based authorization
* Session tracking
* Secure endpoint protection

---

## Prerequisites

* Java 21
* Maven or Gradle
* PostgreSQL
* MongoDB

---

## Configuration

Configure services inside:

```text
src/main/resources/application.yml
```

Example:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/verbrix
    username: postgres
    password: password

  data:
    mongodb:
      uri: mongodb://localhost:27017/verbrix

jwt:
  secret: your-secret-key
```

---

## Running Locally

```bash
./mvnw spring-boot:run
```

or

```bash
mvn spring-boot:run
```

Application URL:

```text
http://localhost:8080
```

---

## Build

```bash
mvn clean package
```

---

## Docker

```bash
docker build -t verbrix-backend .
docker run -p 8080:8080 verbrix-backend
```

---

## Deployment

* Docker
* GitHub Actions CI/CD
* Render

---

## Testing

### API Testing

* Postman Collections
* Unit Tests

### Performance

* Optimized JPA queries
* Efficient persistence strategy
* Idempotent payment workflows

---

## Roadmap

* Multi-factor authentication
* Subscription plans
* Fraud detection
* Analytics dashboard
* Multi-currency support

---

## Author

**Raihan Alam**
