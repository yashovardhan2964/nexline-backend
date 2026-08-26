⚡ NexLine — AI-Powered Queue Management Platform

> Replacing physical queues with intelligent digital token management for hospitals, banks, and government offices.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Redis](https://img.shields.io/badge/Redis-7.0-red)
![License](https://img.shields.io/badge/license-MIT-brightgreen)

---

📌 Problem Statement

Physical queue systems in hospitals, banks, and government offices cause:
- Long wait times with no transparency
- Poor handling of priority cases (elderly, emergency)
- No wait-time estimation
- Idle or overloaded counters
- Zero analytics for staff optimization

NexLine replaces this with a smart digital token system — backed by real-time scheduling, AI prediction, and live updates.

---

🏗️ System Architecture
┌─────────────────────────────────┐
│ React Frontend (Port 3000) │
│ Customer Portal + Admin Panel │
└──────────────┬──────────────────┘
│ REST + WebSocket
┌──────────────▼──────────────────┐
│ Spring Boot Backend (8080) │
│ Controllers → Services → Repos │
└───────┬──────────┬──────────┬───┘
│ │ │
┌────▼───┐ ┌───▼──┐ ┌───▼──────────┐
│ MySQL │ │Redis │ │ Python AI │
│History │ │Live │ │ FastAPI 8000 │
│Analytics│ │Queue │ │ ML Predict │
└────────┘ └──────┘ └─────────────┘


---

✨ Key Features

### Core Queue Engine
- **Token Generation** — Daily per-service sequences with prefix display (B-1, C-3, P-2)
- **Priority Scheduling** — EMERGENCY > PRIORITY > NORMAL with weighted aging algorithm
- **Starvation Prevention** — Aging bonus ensures NORMAL tokens aren't indefinitely skipped
- **Counter Management** — Multi-service-type counters with capability and active assignment

### Engineering Highlights
- **Concurrency Safe** — Pessimistic locking (`SELECT FOR UPDATE`) prevents race conditions when multiple admins call "Next Token" simultaneously
- **Real-Time Updates** — WebSocket (STOMP protocol) pushes queue changes to all clients instantly — no polling
- **AI Wait Prediction** — Python ML microservice predicts wait time from queue features (R²=0.97)
- **Graceful Degradation** — AI failure never breaks token creation; system degrades cleanly
- **JWT Security** — Stateless auth with role-based access (ADMIN / STAFF / CUSTOMER)
- **Redis Queue State** — Sorted sets for O(log n) live queue operations, decoupled from MySQL

---

🛠️ Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Backend | Java 17, Spring Boot 3.3.5 | REST APIs, business logic |
| Database | MySQL 8.0 | Persistent storage, analytics |
| Cache | Redis 7.0 | Live queue state, fast reads |
| AI Service | Python 3.13, FastAPI | ML inference microservice |
| ML Model | scikit-learn GradientBoosting | Wait-time prediction |
| Frontend | React 18, STOMP.js | Customer + admin UI |
| Auth | JWT (jjwt 0.12.6) | Stateless authentication |
| Build | Maven | Dependency management |

---

🧠 Key Engineering Decisions

### 1. Pessimistic Locking for Concurrency
**Problem:** Two admins clicking "Next Token" simultaneously could assign the same token to two counters — a classic read-modify-write race condition.

**Solution:** `SELECT ... FOR UPDATE` locks all WAITING tokens for a service type within a single `@Transactional` boundary. The second concurrent request blocks until the first commits.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT t FROM QueueToken t WHERE t.serviceType = :serviceType AND t.status = 'WAITING'")
List<QueueToken> findWaitingTokensForUpdate(@Param("serviceType") ServiceType serviceType);
```

### 2. Weighted Aging Algorithm (Starvation Prevention)
**Problem:** Pure strict-priority queues starve NORMAL customers when EMERGENCY/PRIORITY tokens keep arriving.

**Solution:** Dynamic scoring with aging bonus — wait time increases effective priority gradually:

score = priorityWeight + (minutesWaited × agingFactor)
EMERGENCY=1000, PRIORITY=500, NORMAL=0, agingFactor=2/min

Inspired by OS CPU scheduling algorithms (multi-level priority queue with aging).

### 3. Redis vs MySQL for Live Queue State
**Problem:** Querying MySQL on every "what's my position?" request from hundreds of clients is expensive.

**Solution:** Redis sorted sets mirror live queue state. Score = priority+aging weight. `ZREVRANK` gives position in O(log n). MySQL stays as source of truth for history and analytics.

### 4. AI Microservice Isolation
**Problem:** Tight coupling to AI service would break the entire system if the ML model crashes.

**Solution:** Python FastAPI runs as a separate process. Spring Boot calls it via REST with a try/catch — returns -1 on failure, frontend shows "Calculating..." instead of crashing. AI is enhancement, not dependency.

---

📁 Project Structure

src/main/java/com/yash/smartqueue/
├── controller/ # REST endpoints
│ ├── AuthController
│ ├── QueueController
│ ├── QueueTokenController
│ ├── ServiceTypeController
│ └── AdminController
├── service/ # Business logic
│ ├── QueueTokenService
│ ├── QueueAdvancementService
│ ├── TokenScoringService
│ ├── RedisQueueService
│ ├── AiPredictionService
│ └── AuthService
├── repository/ # JPA + Redis
├── model/ # JPA entities
├── dto/ # Request/response shapes
├── security/ # JWT filter + util
├── config/ # Security, Redis, CORS, WebSocket
├── websocket/ # WebSocket event publisher
└── exception/ # Global exception handler


---

🚀 Local Setup

### Prerequisites
- Java 17+
- MySQL 8.0
- Redis (WSL2 on Windows or Docker)
- Python 3.13+
- Node.js 20+

### 1. Clone all repos
```bash
git clone https://github.com/yashovardhan2964/nexline-backend
git clone https://github.com/yashovardhan2964/nexline-ai
git clone https://github.com/yashovardhan2964/nexline-frontend
```

### 2. Database setup
```sql
CREATE DATABASE smartqueue;
```

### 3. Configure backend
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smartqueue
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
jwt.secret=YourSecretKeyHere
```

### 4. Start Redis (WSL2)
```bash
sudo service redis-server start
```

### 5. Start AI service
```bash
cd nexline-ai
pip install -r requirements.txt
python data/generate_data.py
python model/train.py
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

### 6. Start backend
```bash
cd nexline-backend
./mvnw spring-boot:run
```

### 7. Start frontend
```bash
cd nexline-frontend
npm install
npm start
```

### Default Admin Credentials
- Phone: `9999999999`
- Password: `admin123`

### Access
- Customer Portal: `http://localhost:3000`
- Admin Dashboard: `http://localhost:3000/admin`
- AI Service Docs: `http://localhost:8000/docs`
- Backend API: `http://localhost:8080`

---

🔗 Related Repositories

| Repo | Description |
|------|-------------|
| [nexline-ai](https://github.com/yashovardhan2964/nexline-ai) | Python FastAPI ML microservice |
| [nexline-frontend](https://github.com/yashovardhan2964/nexline-frontend) | React customer + admin UI |

---

👨‍💻 Author

**Yashovardhan Singh Rathore**
- B.Tech Student | ServiceNow CSA & CAD Certified
- GitHub: [@yashovardhan2964](https://github.com/yashovardhan2964)
