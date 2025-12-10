# Energy Management System (Distributed Systems)

## Assignment Overview
This project extends the Distributed **Energy Management System** by introducing real-time communication capabilities and scalable data processing. The update adds a **Customer Support Chat**, **WebSocket-based notifications**, and a **Load Balancing Service** to distribute device data across multiple monitoring replicas.

## Architecture
The system architecture has been updated to support high scalability and real-time interaction.

## New Components:

 * **Customer Support Microservice:** Handles chat logic between clients and administrators. It includes a rule-based auto-responder and optional AI integration.
 * **WebSocket Microservice:** Manages the transport layer for real-time chat messages and pushes overconsumption alerts to the frontend.
 * **Load Balancing Service:** Acts as an ingestion pipeline. It consumes device data from RabbitMQ and distributes it to specific Ingest Queues based on a load-distribution strategy (e.g., Round Robin, Hashing).
 * **Monitoring Replicas:** Multiple instances of the Monitoring Service now process data in parallel, pulled from their dedicated ingest queues.

## Key Features
 **1. Real-Time Customer Support**
 * **Live Chat:** Authenticated users can initiate chat sessions with administrators.
 * **Rule-Based Bot:** Automatically answers common questions using keyword matching or conditional logic.
 * **AI Integration (Optional):** Fallback to LLM APIs (Gemini/OpenAI) for complex queries.
 * **Secure Transport:** Uses WebSockets for low-latency, bidirectional messaging.

 **2. Event-Driven Notifications**
 * **Overconsumption Alerts:** The monitoring pipeline detects energy spikes and instantly pushes alerts to the user's dashboard via WebSockets.
 * **Typing Indicators & Read Receipts:** (Optional) Real-time feedback in the chat interface.

 **3. Scalable Data Processing**
 * **Load Balancing:** A dedicated service sits between the Device Simulator and the Monitoring Services.
 * **Replica Management:** Deployed using Docker Swarm to manage multiple replicas of the monitoring microservice.
 * **Dedicated Queues:** Ensures data isolation and efficient processing by routing messages to specific replica queues.

## Tech Stack
 * **Communication:** RabbitMQ (STOMP & AMQP), WebSockets.
 * **Backend:** Java Spring Boot / .NET Web API.
 * **Frontend:** ReactJS / Angular.
 * **Orchestration:** Docker Swarm (Mandatory).
 * **Gateway:** Traefik Reverse Proxy.
 * **Database:** PostgreSQL / MySQL.
