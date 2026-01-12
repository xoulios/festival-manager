# Festival Manager (Τεχνολογία Λογισμικού)

Festival Management App Spring Boot (REST API) and React frontend.

### Backend
- Java 21
- Maven Wrapper (`./mvnw`)
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=dev

### Frontend
- npm
npm run dev

Backend at: http://localhost:8080

H2 Console (dev)    http://localhost:8080/h2

Tests from folder backend/festival-manager with ./mvnw test

Authentication (Basic Auth) curl -i -u programmer1:pass123 http://localhost:8080/api/festivals

programmer1:pass123
artist1:pass123
staff1:pass123