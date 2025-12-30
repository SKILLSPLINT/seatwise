
# SeatWise Backend Microservices

## Project Overview

SeatWise is a microservices-based backend system for event ticket booking. The system manages users, events, bookings, and notifications using Spring Boot services with PostgreSQL databases. Each service is independent, allowing scalability, maintainability, and easy future extension.

## Services

| Service Name         | Port | Database         | Description                                               |
| -------------------- | ---- | ---------------- | --------------------------------------------------------- |
| User Service         | 9090 | `userdb`         | Manages user registration, login, and profiles.           |
| Event Service        | 9091 | `eventdb`        | Manages events creation, listing, and details.            |
| Booking Service      | 9092 | `bookingdb`      | Handles seat selection, reservations, and bookings.       |
| Notification Service | 9093 | `notificationdb` | Sends notifications to users (async, future integration). |

## Tech Stack

* Java 17
* Spring Boot 3.x
* PostgreSQL
* Maven
* Lombok
* JPA/Hibernate

## Folder Structure

```
seatwise-backend/
├── common/
├── user-service/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   └── UserServiceApplication.java
│   └── application.yml
├── event-service/          # same structure as user-service
├── booking-service/        # same structure as user-service
├── notification-service/   # same structure as user-service
├── api-gateway/            # empty for now
├── eureka-server/          # empty for now
└── README.md
```

## Prerequisites

* Java 17 or higher
* Maven
* PostgreSQL

## Setup Database

```sql
CREATE DATABASE userdb;
CREATE DATABASE eventdb;
CREATE DATABASE bookingdb;
CREATE DATABASE notificationdb;
```

User: `jodos`
Password: `jodos2006`

## Running Services

Each service can be started independently using Maven:

```bash
cd user-service
mvn spring-boot:run
```

Default ports:

* User Service: 9090
* Event Service: 9091
* Booking Service: 9092
* Notification Service: 9093

## Configuration

* All services have `application.yml` configured for database, logging, and ports.
* Future configurations for Eureka, API Gateway, Redis, and async messaging will be added.

## Future Improvements

* Eureka Server for service discovery
* API Gateway for routing and load balancing
* Redis caching for performance
* Async notifications using RabbitMQ or Kafka
* Stripe API integration for payments
* Unit and integration tests
* Role-based access control (Admin/User)

## Project Goals

1. Learn microservices architecture hands-on
2. Understand service decomposition and inter-service communication
3. Practice Spring Boot, PostgreSQL, async processing, and logging
4. Gradually extend the system with real-world features like API Gateway, Eureka, Redis, and external integrations
