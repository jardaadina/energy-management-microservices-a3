# DS2025_30645_Jarda_Adina_Assignment_2
# Energy Management System 

# Overview
Distributed Energy Management System with asynchronous communication using RabbitMQ for device monitoring and real-time energy consumption tracking.

# Architecture

- User Service - User management and authentication
- Device Service - Device management and user-device assignments
- Authentication Service - JWT-based authentication
- Monitoring Service - Energy consumption data processing and aggregation
- Device Simulator - Python application simulating smart meter readings
- RabbitMQ - Message broker for asynchronous communication
- Traefik - Reverse proxy and API gateway
- Frontend - React-based web interface

# Technologies

- Backend: Java Spring Boot, REST APIs
- Database: PostgreSQL
- Message Broker: RabbitMQ
- Frontend: React, TypeScript, Chart.js
- Containerization: Docker, Docker Compose
- Reverse Proxy: Traefik v3.2

# Prerequisites

- Docker & Docker Compose
- Python 3.8+ (for device simulator)
- Git

# Build & Deployment
1. Clone Repository
   git clone <repository-url>
   cd energy-management-system
2. Build and Start Services
   docker-compose up --build

This will start all services:
User Service: http://localhost:8081
Device Service: http://localhost:8082
Auth Service: http://localhost:8083
Monitoring Service: http://localhost:8084
Frontend: http://localhost
RabbitMQ Management: http://localhost:15672 (admin/admin123)
Traefik Dashboard: http://localhost:8080

3. Run Device Simulator
   cd device-simulator
   pip install -r requirements.txt
   python simulator.py

- Stopping Services 
   docker-compose down
- To remove volumes:
   docker-compose down -v