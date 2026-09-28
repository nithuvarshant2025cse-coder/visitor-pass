# VisitorPass - Gated Community Visitor Pre-Approval with OTP
Spring Boot 3 (Java 17) + Spring Data JPA + MySQL + HTML/CSS/JS frontend. Build tool: Maven.

## Run
1. Install JDK 17+, Maven, MySQL (running on port 3306).
2. Edit `src/main/resources/application.properties` -> set your MySQL username/password. The database `visitorpass_db` and all tables are created automatically.
3. `mvn spring-boot:run` (or run `VisitorPassApplication` in IntelliJ; enable Lombok annotation processing).
4. Open http://localhost:8080

## Flow
Flats -> Residents -> Pre-Approvals (OTP generated) -> Security Gate (validate OTP, entry logged).

## REST API
| Method | URL | Purpose |
|---|---|---|
| GET/POST | /api/flats | list / create flat |
| GET/PUT/DELETE | /api/flats/{id} | read / update / delete flat |
| GET/POST | /api/residents | list / create resident |
| GET/PUT/DELETE | /api/residents/{id} | read / update / delete resident |
| POST | /api/approvals | pre-approve visitor + generate OTP |
| GET | /api/approvals[?residentId=] | view approvals |
| GET/PUT/DELETE | /api/approvals/{id} | read / update (pending only) / delete |
| PUT | /api/approvals/{id}/revoke | revoke pending approval |
| POST | /api/gate/validate  `{"otp":"123456"}` | security validates OTP, logs entry time |
| GET | /api/gate/entries | entry log |

## Business rules (enforced in service layer)
- OTP can be validated for entry only once (409).
- Revoked / expired approvals are rejected even with a valid OTP (403); entry before the visit window is rejected.
- Unused passes auto-expire after the window (scheduler every minute + checked at validation).
- Only PENDING approvals can be edited or revoked.
- Bad input -> 400 with clear message via `@ControllerAdvice`.

## ER model
Flat 1---* Resident 1---* VisitorPreApproval 1---1 OTPPass
