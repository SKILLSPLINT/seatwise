### Project Analysis & Roadmap: SeatWise Platform

The following report provides a detailed analysis of the **SeatWise** project, a microservices-based event ticketing platform. This analysis cross-references the current codebase with the **Software Requirements Specification (SRS)** found in the `docs` folder.

---

### 1. Cross-Referenced Overview (Code vs. SRS)

Below is the mapping of functional requirements from the SRS to their current implementation status in the codebase.

| Functional Area | SRS Requirement | Implementation Status | Code Location (Service) |
| :--- | :--- | :--- | :--- |
| **Authentication** | User registration (Email/Phone) | ✅ Fully Implemented | `user-service` (UserController/ServiceImpl) |
| **RBAC** | Role-based Access (USER, ADMIN) | ✅ Fully Implemented | `api-gateway` (SecurityConfig), `user-service` |
| **User Profile** | Profile management & picture upload | ✅ Fully Implemented | `user-service` (UserServiceImpl) |
| **Event MGMT** | Create, Update, Delete Events | ✅ Fully Implemented | `event-service` (EventController/ServiceImpl) |
| **Seat MGMT** | Real-time seat reservation & mapping | ⚠️ Partially Implemented | `event-service` (Seat/EventServiceImpl) |
| **Ticketing** | Booking creation (RESERVED state) | ✅ Fully Implemented | `booking-service` (BookingServiceImpl) |
| **Payments** | Payment gateway integration | ❌ Not Started | *N/A (Missing payment-service)* |
| **Notifications** | Async email notifications via RabbitMQ | ⚠️ Partially Implemented | `notification-service`, `user-service` (EmailProducer) |
| **File Storage** | MinIO-based image/file management | ✅ Fully Implemented | `file-service` (FileStorageServiceImpl) |
| **Service Mesh** | Discovery, Gateway, Routing | ✅ Fully Implemented | `eureka-server`, `api-gateway` |

---

### 2. Gap Analysis & Inconsistencies

#### Code Exists, Documentation Missing/Incomplete:
- **Timezone Handling**: The code includes sophisticated `TimeUtils` and headers for user-specific timezones, which is a great feature but not explicitly detailed in the SRS as a core requirement.
- **Seat Optimistic Locking**: The `Seat` entity uses `@Version` for concurrency control, a critical technical detail for high-traffic ticketing.

#### Documentation Exists, Code Missing/Incomplete:
- **Payment Integration**: The SRS likely specifies M-Pesa or card payments (given the East Africa focus). The `booking-service` has a placeholder for payment confirmation, but the `payment-service` itself is non-existent.
- **Automated Seat Release**: The SRS requires seats to be released after a timeout (e.g., 10-15 mins). While `EventServiceImpl` has a `releaseSeat` method, there is no **Scheduled Task** or **Redis-based TTL** mechanism to trigger it automatically.
- **Search & Filtering**: Comprehensive event filtering (by category, location, date) mentioned in the SRS is currently limited in the API implementation.
- **SMS Notifications**: The `notification-service` is structured for multiple types, but only Email (placeholder) is currently implemented.

---

### 3. Detailed Project Roadmap

This roadmap is prioritized to reach a Minimum Viable Product (MVP) for the East African market.

#### Phase 1: Core Flow Completion (High Priority)
1.  **Task: Implement Seat Release Scheduler**
    -   *Detail*: Create a `@Scheduled` task in `event-service` to scan for seats with `status = RESERVED` where `reserved_at` is older than 15 minutes and call `releaseSeat()`.
    -   *Checkpoint*: Reserved seats automatically return to `AVAILABLE` after timeout.
2.  **Task: Create Payment Service**
    -   *Detail*: Develop a new `payment-service` to handle transactions. Integrate with a mock gateway initially, then East African providers (e.g., Flutterwave or M-Pesa API).
    -   *Checkpoint*: Successful payment triggers a call to `booking-service` to change status to `BOOKED`.
3.  **Task: Notification Triggers**
    -   *Detail*: Add `EmailProducer` calls to `BookingServiceImpl` (on creation) and `EventServiceImpl` (on seat confirmation).
    -   *Checkpoint*: Users receive emails for every booking step.

#### Phase 2: Refinement & Scalability (Medium Priority)
1.**Task: Unit & Integration Testing**
    -   *Detail*: Add Mockito tests for `BookingService` and `EventService` logic, focusing on race conditions during seat reservation.
    -   *Checkpoint*: Test coverage reaches >70% for business logic.

#### Phase 3: Future Improvements
1.  **Task: QR Code Ticket Generation**
    -   *Detail*: Integrate a QR code library to generate unique ticket IDs upon successful payment.
2.  **Task: SMS Integration**
    -   *Detail*: Add an SMS provider (e.g., Africa's Talking) to `notification-service`.

---

### 4. Project Summary

*   **Current Status**: **65% Complete**. The core microservices architecture is solid, and the primary "happy path" (Register -> View Event -> Reserve Seat -> Create Booking) is mostly implemented.
*   **Completeness Breakdown**:
    *   **Infrastructure (Eureka, Gateway, Common)**: 95%
    *   **User & Auth**: 90%
    *   **Event & Seat Management**: 75% (Missing auto-release)
    *   **Booking**: 80% (Missing payment integration)
    *   **File & Notification**: 70% (Missing SMS and real mail server config)
    *   **Payment**: 0%

**Recommendations**:
- Prioritize the **Payment Service** and **Seat Expiration Logic** immediately; without these, the platform cannot function commercially.
- Formalize the API contracts between services using the existing OpenAPI/Swagger configurations to ensure frontend teams can work in parallel.
- Move hardcoded JWT secrets (e.g., in `JwtUtils`) to environment variables or a Config Server for security.