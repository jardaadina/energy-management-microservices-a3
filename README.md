# DS2025_30645_Jarda_Adina_Assignment_3
# Energy Management System

# Overview
A comprehensive Distributed Energy Management System designed with a Microservices Architecture. The system monitors smart devices, tracks energy consumption in real-time, manages alerts, and provides intelligent customer support via a Chat System powered by AI and rule-based logic.

# Architecture

### Core Services
- **Authentication Service**: Secure access using JWT (JSON Web Tokens).
- **User Service**: Manages client and administrator accounts.
- **Device Service**: Handles smart device inventory and user mapping.
- **Monitoring Service**: Processes energy consumption, calculates hourly usage, and detects threshold checks.

### New Distributed Components
- **Load Balancer Service**: Distributes incoming sensor data using **Consistent Hashing** to ensure scalability.
- **WebSocket Service**: Manages real-time bi-directional communication for push notifications (alerts) and live chat.
- **Customer Support Service**: An intelligent support system featuring:
    - **Rule-Based Engine**: Handles common FAQs automatically.
    - **AI Integration**: Uses LLM for complex queries.
    - **Admin Handoff**: Routes messages to human administrators when needed.

### Infrastructure
- **Device Simulator**: Python application generating realistic smart meter readings.
- **RabbitMQ**: Message broker for asynchronous communication (sensor data, chat messages, system events).
- **PostgreSQL**: Database per service pattern.
- **Frontend**: React-based web interface with admin dashboards and client charts.

# Technologies

- **Backend**: Java 25 , Spring Boot 3.x
- **Communication**: REST APIs, WebSockets, RabbitMQ (AMQP)
- **Database**: PostgreSQL
- **Frontend**: React, TypeScript, Tailwind CSS, Shadcn/UI
- **AI/ML**: Python (Simulator), LLM Integration (Support Service)
- **Containerization**: Docker, Docker Compose

# Prerequisites

- Docker & Docker Compose
- Java 17+ (optional, for local dev)
- Node.js 18+ (optional, for local dev)
- Python 3.8+ (for device simulator)

# Build & Deployment

1. **Clone Repository**
   ```bash
   git clone <repository-url>
   cd energy-management-system