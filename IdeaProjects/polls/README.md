# 📊 Polling Platform

A feature-rich, reactive polling backend application This platform enables secure user authentication, interactive poll creation

---

## 🛠️ Architecture & Tech Stack

The application relies on the following core dependencies configured in `pom.xml`:

* **Java Version:** 17
* **Framework:** Spring Boot 3.2.4
* **Security & Auth:** Spring Security, OAuth2 Resource Server, OAuth2 Client, JWT
* **Identity Provider:** Keycloak (OAuth2 / OpenID Connect)
* **Reactive Engine:** Project Reactor for non-blocking SSE streaming
* **Database & Persistence:** Spring Data JPA, MySQL


---

## 🚀 Key Features

### 🔐 1. Authentication & Identity Management
* **Keycloak Integration:** Centralized identity management. User creation and role assignments (`ROLE_USER`) are delegated directly to Keycloak Admin Service.
* **JWT Authorization:** Secured REST endpoints leveraging OAuth2 JWT tokens and Spring Security `@PreAuthorize` method protection.
* **User Profile & Metrics:** Public user profile retrieval including account registration timestamp, total created polls, and total cast votes.

### ⚡ 2. Reactive Real-Time Vote Streaming
* **Server-Sent Events (SSE):** Dedicated `/api/polls/votes/stream` endpoint emitting `text/event-stream` using Project Reactor (`Flux`). Real-time vote updates are pushed asynchronously to connec[...]

### 📊 3. Poll Management & Voting
* **Create & Browse Polls:** Authenticated users can publish new polls with custom expiration periods (days/hours) and browse paginated poll lists.
* **Interactive Voting:** Submit votes on specific choices with immediate live updates across subscribers.
* **User Activity Feeds:** Retrieve paginated lists of polls created or voted on by a specific user.

---

## 🔑 Keycloak Setup (PKCE Authorization Code Flow)

Follow these steps to set up Keycloak and configure a realm and client for **PKCE (Proof Key for Code Exchange)**.

### Step 1: Run Keycloak Container
```bash
docker run -d \
  --name keycloak \
  -p 8080:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest start-dev
```
Access the Admin Console at `http://localhost:8080` (credentials: `admin` / `admin`).

### Step 2: Create a Realm
1. Open the realm dropdown menu (top-left) and select **Create Realm**.
2. Name the realm `polls-realm` (or your configured realm name).
3. Click **Create**.

### Step 3: Register Public Client for PKCE
1. Go to **Clients** > **Create client**.
2. **Client Type:** `OpenID Connect` | **Client ID:** `polls-app`.
3. **Capability Config:**
   * Set **Client Authentication** to `OFF` (marks client as Public, enforcing PKCE).
   * Enable **Standard Flow** (Authorization Code Grant).
   * Disable Direct Access Grants / Implicit Flow.
4. **Login Settings:**
   * **Root URL:** `http://localhost:3000` (or frontend URL)
   * **Valid Redirect URIs:** `http://localhost:3000/*`
   * **Web Origins:** `http://localhost:3000` (for CORS)
5. Save changes. In **Advanced Settings**, ensure **Code Challenge Method** is set to `S256`.

---
## 📚  Quick Start — Run locally (MySQL + Maven)

Prerequisites: Java 17, Maven, Docker (or local MySQL).

1) Start MySQL (Docker):
```bash
docker run -d --name polls-mysql -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=rootpwd \
  -e MYSQL_DATABASE=polling_app \
  -e MYSQL_USER=polls_user \
  -e MYSQL_PASSWORD=change_me_strong_password \
  mysql:8.0
```

2) Configure datasource (add to src/main/resources/application.properties or set env vars):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/polling_app?useSSL=false&serverTimezone=UTC&useLegacyDatetimeCode=falseuseSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=polls_user
spring.datasource.password=change_me_strong_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
```

3) Build & run:
- Run in dev: mvn spring-boot:run
- Or package and run jar:
  mvn -DskipTests package
  java -jar target/*.jar

4) Verify: curl http://localhost:8080/api/polls or check app logs for successful datasource initialization.

Notes:
- Do not commit credentials; use environment variables for production.
- If the app starts before MySQL is ready, retry or use a wait-for script.
---

## 🔌 API Reference & Endpoints

### 1. Authentication

#### `POST /api/auth/register`
Registers a new user in Keycloak and local DB.

* **Auth:** None
* **Request:**
  ```json
  {
    "name": "user1",
    "username": "user1@gmail.com",
    "email": "user1@gmail.com",
    "password": "12345678"
  }
  ```
* **Response (201 Created):**
  ```json
  {
    "success": true,
    "message": "User registered successfully"
  }
  ```

---

### 2. Poll Management

#### `POST /api/polls/new`
Creates a new interactive poll.

* **Auth:** Bearer Token (`ROLE_USER`)
* **Request:**
  ```json
  {
    "question": "do you smoke?",
    "choices": [
      { "text": "yes" },
      { "text": "no" }
    ],
    "pollLength": {
      "days": 2,
      "hours": 2
    }
  }
  ```

#### `GET /api/polls`
Fetches a paginated list of all polls.

* **Auth:** Bearer Token (`ROLE_USER`)

#### `GET /api/polls/{pollId}`
Retrieves details for a specific poll by ID.

* **Auth:** Bearer Token

#### `POST /api/polls/{pollId}/votes`
Submits a vote for a choice in a poll.

* **Auth:** Bearer Token (`ROLE_USER`)
* **Request:**
  ```json
  {
    "choiceId": 10
  }
  ```

#### `GET /api/polls/votes/stream`
Reactive Server-Sent Event (SSE) stream for real-time vote updates.

* **Auth:** Bearer Token
* **Produces:** `text/event-stream`
* **Sample Stream Event:**
  ```text
  data:{"voteId":5,"PollId":4,"voter":{"name":"user1","id":"c5ce3a03-d859-4b44-81fa-e6a4c3894931","username":"user1@gmail.com"},"choice":"no"}
  ```

---

### 3. User Profile & Metrics

#### `GET /api/users/{username}`
Fetches user summary profile and aggregate metrics.

#### `GET /api/users/{username}/polls`
Fetches a paginated list of polls created by the specified user.

#### `GET /api/users/{username}/votes`
Fetches a paginated list of polls voted on by the specified user.

---
