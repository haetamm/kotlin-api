# 🚀 Warmakth API

Backend API built with Spring Boot (Kotlin).

---

# 🧩 Architecture Overview

This project supports two runtime modes:

| Mode        | Spring Boot Runtime | Database                 | Message Broker       | File Storage        | Configuration Source          |
|-------------|--------------------|--------------------------|----------------------|--------------------|------------------------------|
| Development | Run from IDE       | PostgreSQL (Docker)      | RabbitMQ (Docker)    | Supabase Storage   | IDE Environment Variables     |
| Production  | Docker Container   | Supabase (PostgreSQL)    | CloudAMQP (RabbitMQ) | Supabase Storage   | `.env` via Docker Compose     |

---

# 🛠 DEVELOPMENT MODE (Hybrid Setup)

Development Mode uses:

- 🐳 Docker → PostgreSQL, RabbitMQ, Ngrok
- 💻 IDE → Spring Boot Application
- 🌍 Ngrok → Webhook exposure for Midtrans

---

## 📦 1. Prerequisites

Ensure the following tools are installed on your machine:

- **JDK 17** (Microsoft OpenJDK 17.0.18 or equivalent)
- **Docker Desktop**
- **DBeaver** (Optional, for database management)

---

## 🧬 2. Clone Repository

```bash
git clone https://github.com/haetamm/kotlin-api.git
cd kotlin-api
```

---

## 🔐 3. Configure Required Environment Variables

### 3.1 Spring Boot (IDE)

Configure in:

IntelliJ → Run → Edit Configurations → Environment Variables

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/warmakth-db
SPRING_DATASOURCE_USERNAME=developer
SPRING_DATASOURCE_PASSWORD=rahasia

SMTP_HOST=your_smtp_host
SMTP_PORT=587
SMTP_USERNAME=your_email
SMTP_PASSWORD=your_password

MIDTRANS_API_KEY=your_midtrans_key

SUPABASE_URL=your_supabase_url
SUPABASE_API_KEY=your_supabase_api_key
SUPABASE_BUCKET=your_bucket_name

YOUR_GOOGLE_CLIENT_ID=your_google_client_id
YOUR_GOOGLE_CLIENT_SECRET=your_google_client_secret
```

---

### 3.2 RabbitMQ (Development Mode)

RabbitMQ is configured directly in `application.properties` for development:

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.rabbitmq.virtual-host=/
spring.rabbitmq.ssl.enabled=false
```

---

### 3.3 Ngrok (Docker)

Create a `.env` file in project root:

```env
NGROK_AUTHTOKEN=your_real_ngrok_token
```

Get token from:

https://dashboard.ngrok.com/get-started/your-authtoken

---

### 3.4 Storage (Supabase)

Supabase Storage is used for image upload in both:

- Development Mode
- Production Mode

#### Required Environment Variables

Configure the following variables in your IDE:

IntelliJ → Run → Edit Configurations → Environment Variables

```env
SUPABASE_URL=your_supabase_url
SUPABASE_API_KEY=your_supabase_api_key
SUPABASE_BUCKET=your_bucket_name
```

---

## 🐳 4. Start Infrastructure (Docker)

```bash
docker compose -f docker-compose.dev.yml up -d
```

Check:

```bash
docker ps
```

Expected:

- warmakth-postgres-dev
- warmakth-rabbitmq-dev
- warmakth-ngrok-dev

---

## ▶ 5. Run Spring Boot Application

Run:

```
Application.kt
```

App runs at:

```
http://localhost:8081
```
---

## 📚 6. API Documentation (Swagger)

Open the following URL in your browser to access the API documentation:

http://localhost:8081/swagger-ui/index.html#/

---

## 🗄 7. Configure Database via DBeaver

Open DBeaver → Create New PostgreSQL Connection

Use the following configuration:

- Host: `localhost`
- Port: `5432`
- Database: `warmakth-db`
- Username: `developer`
- Password: `rahasia`

Click **Test Connection** → then Finish.

Hibernate will automatically create tables on first application startup.

---

## 🐰 8. RabbitMQ Management Dashboard

Open in browser:

```
http://localhost:15672
```

Login credentials:

- Username: `guest`
- Password: `guest`

---

## 🌍 9. Ngrok (Webhook Testing)

Open Ngrok dashboard:

```
http://localhost:4040
```

Copy public URL:

```
https://xxxx.ngrok-free.app
```

Set in Midtrans Sandbox:

https://dashboard.sandbox.midtrans.com/settings/payment/notification

Notification URL:

```
https://xxxx.ngrok-free.app/api/payments/webhook
```

Click **Save**.


---

### 🔁 Flow Explanation

Midtrans → Ngrok Public URL → Localhost:8081 → `/api/payments/webhook`

---

⚠ Make sure:

- Spring Boot is running on port `8081`
- Ngrok container is running
- The webhook endpoint exists:

```
POST /api/payments/webhook
```

---

## 🔁 Development Workflow

1. Start Docker infrastructure
2. Run Spring Boot from IDE
3. Ngrok exposes the application
4. Test APIs
5. Debug & develop

---

## 🛑 Stop Development

```bash
docker compose -f docker-compose.dev.yml down
```

---

# 🏭 PRODUCTION MODE (Dockerized)

Production runs fully inside Docker.

Infrastructure:

- Supabase → PostgreSQL
- CloudAMQP → RabbitMQ
- Supabase → Storage
- Ngrok (optional)

---

## 🐳 1. Build & Run

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

Check:

```bash
docker ps
```

Expected containers:

- warmakth-api-prod
- warmakth-ngrok-prod

---

## 🔐 2. Environment Variables (Production)

Production uses:

```
docker-compose.prod.yml → env_file → .env
```

Create `.env` in project root.

You can copy from:

```
.env.example
```

Example:

```env
NGROK_AUTHTOKEN=your_ngrok_token

SPRING_DATASOURCE_URL=your_supabase_jdbc_url
SPRING_DATASOURCE_USERNAME=your_db_username
SPRING_DATASOURCE_PASSWORD=your_db_password

MIDTRANS_API_KEY=your_midtrans_key

RABBITMQ_HOST=your_cloudamqp_host
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=your_cloudamqp_username
RABBITMQ_PASS=your_cloudamqp_password
RABBITMQ_VIRTUAL_HOST=your_virtual_host

SMTP_HOST=your_smtp_host
SMTP_PORT=587
SMTP_USERNAME=your_email
SMTP_PASSWORD=your_password

SUPABASE_URL=your_supabase_url
SUPABASE_API_KEY=your_supabase_api_key
SUPABASE_BUCKET=your_bucket_name

YOUR_GOOGLE_CLIENT_ID=your_google_client_id
YOUR_GOOGLE_CLIENT_SECRET=your_google_client_secret
```

⚠ Do NOT commit `.env`.

---

## 🔧 Configuration Strategy

This project uses a single `application.properties`.

All environment-specific values are injected using:

```
${ENV_VARIABLE_NAME}
```

No modification to `application.properties` is required between environments.

---

## 🌍 3. Access Application

```
http://localhost:8081
```

Ngrok dashboard:

```
http://localhost:4040
```

---

## 🛑 Stop Production

```bash
docker compose -f docker-compose.prod.yml down
```

---

# 🎯 Summary

Development Mode:

✔ App from IDE  
✔ Infrastructure via Docker  
✔ Config via IDE ENV and `.env` file

Production Mode:

✔ App inside Docker  
✔ Supabase & CloudAMQP  
✔ Config via `.env`  
✔ Multi-stage optimized build

---