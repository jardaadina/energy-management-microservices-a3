# DS2025_30645_Jarda_Adina_Assignment_1

Energy Management Project
This is a full-stack project for managing users and their devices. It uses a microservice system.

How it Works
The project has a simple flow:

Frontend (React) → API Gateway (Traefik) → Backend Services (Spring Boot) → Databases (PostgreSQL)

What's Inside
The system is made of a few main parts:

1. Frontend
   User Interface (React): A web app that the user sees and clicks on (from the image_60ae30.png file).

2. Gateway
   Traefik (API Gateway): A smart router. All API requests from the frontend go to Traefik first. It then sends the request to the correct backend service.

Nginx (Web Server): A simple server to send the React app's files to the user's browser.

3. Backend Services
   These are three separate Spring Boot apps. Each app has its own job and its own database.

authentication-service (Port 8083)

Handles user login and checks if a user is allowed to do things.

Creates and checks security tokens (JWT).

Connects to the auth_db database.

user-service (Port 8081)

Manages user information (create, read, update, delete users).

Talks to the authentication-service when a new user signs up.

Connects to the user_db database.

 device-service (Port 8082)

Manages the devices (create, read, update, delete devices).

Also manages which devices belong to which user.

Connects to the device_db database.

Technology Used
Backend: Java, Spring Boot 3, Spring Security (JWT).

Frontend: React.js.

Database: PostgreSQL (each service has its own).

System: Docker, Traefik (API Gateway).

API Docs: OpenAPI (Swagger) (all backend services have this).

How to Run
This whole application is built to run using Docker.

Each service has its own Dockerfile. You will use docker-compose to start and connect all the services at the same time.