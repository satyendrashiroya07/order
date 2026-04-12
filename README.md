# 📦 Order Service (Spring Boot Microservice)

## 📌 Overview

Order Service is responsible for managing order-related operations in the E-Commerce system.
It validates product availability, interacts with User & Product services, and publishes order events to Kafka for downstream processing (like Email Service).

---

## 🚀 Features

* 🛒 Create new orders
* 🔍 Validate product availability (via Product Service)
* 👤 Validate user (via User Service / future security)
* 📉 Reduce product quantity after order success
* 📩 Publish order-created events to Kafka
* 📧 Trigger email notifications (via Email Service)
* ⚠️ Proper exception handling
* 🔄 Microservice communication using REST & Kafka

---

## 🛠️ Tech Stack

* Java 17+
* Spring Boot
* Spring Web (REST APIs)
* Spring Data JPA
* PostgreSQL
* Apache Kafka
* RestTemplate (for inter-service calls)
* Lombok

---

## 🧱 Project Structure

```id="n4h72f"
orderService
│
├── controller        # REST APIs
├── service           # Business logic
├── repository        # JPA repositories
├── entity            # Order entity
├── event             # Kafka event models
├── client            # Product/User service calls
└── exception         # Custom exceptions
```

---

## 📦 API Endpoints

### 🔹 Create Order

```id="sqsn41"
POST /orders
```

### 📥 Request Body

```json id="t5pm6k"
{
  "userId": "USR1001",
  "productId": "P1001",
  "quantity": 1,
  "email": "test@gmail.com"
}
```

---

### 📤 Response

```json id="d4e0rc"
{
  "orderId": "2a5f6db2-b875-45a9-abdd-e4425382e8ee",
  "status": "CREATED"
}
```

---

## 🔄 Order Flow

```id="7xtpxe"
1. Receive order request
2. Validate user (User Service / future security)
3. Validate product & quantity (Product Service)
4. Reduce product quantity
5. Save order in DB
6. Publish event to Kafka
7. Email Service consumes event → sends email
```

---

## 🔗 Inter-Service Communication

### 📌 Product Service Call

* Endpoint: `/product/validateAndReduce`
* Method: POST

```json id="jgj0fe"
{
  "productId": "P1001",
  "quantity": 1
}
```

---

### 📌 User Validation (Future Scope)

* Can be done via:

    * REST call (User Service)
    * OR Security (JWT)

---

## 🧵 Kafka Integration

### 📌 Topic Used

```id="m8r9oa"
order-created
```

### 📤 Event Published

```json id="tw85k0"
{
  "orderId": "2a5f6db2-b875-45a9-abdd-e4425382e8ee",
  "userId": "USR1001",
  "productId": "P1001",
  "quantity": 1,
  "email": "test@gmail.com",
  "status": "CREATED"
}
```

---

## ⚙️ Configuration

### application.yml / properties

```yaml id="4a2sm1"
server:
  port: 8081

spring:
  application:
    name: orderService

  datasource:
    url: jdbc:postgresql://localhost:5432/shiroyadb
    username: postgres
    password: shiroya

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

  kafka:
    producer:
      bootstrap-servers: localhost:9092
```

---

## ❗ Error Handling

Handled using:

* Custom Exception → `OrderException`
* Global Handler → `@RestControllerAdvice`

### Example Errors

#### 🔸 Product Not Found

```json id="s7h5sn"
{
  "status": 404,
  "message": "Product not found"
}
```

#### 🔸 Insufficient Quantity

```json id="v9oc6t"
{
  "status": 409,
  "message": "Insufficient product quantity"
}
```

---

## 🧪 How to Run

### 1️⃣ Start Kafka & Zookeeper

### 2️⃣ Start PostgreSQL

### 3️⃣ Start Dependent Services

* Product Service
* User Service

### 4️⃣ Run Application

```id="5ur4rd"
mvn spring-boot:run
```

---

## 🔗 Dependencies

* spring-boot-starter-web
* spring-boot-starter-data-jpa
* spring-kafka
* postgresql
* lombok

---

## 📈 Future Enhancements

* 🔐 JWT-based authentication (User validation)
* 🔁 Retry mechanism for failed orders
* 📦 Order status tracking (CREATED, FAILED, COMPLETED)
* ⚡ Feign Client instead of RestTemplate
* 📬 Dead Letter Queue (Kafka)
* 📊 Monitoring & logging

---

## 👨‍💻 Author

**Satyendra Chaurasiya**

---

## ⭐ Notes

* This service is part of a **microservices-based E-Commerce system**
* Works with:

    * User Service
    * Product Service
    * Email Service
* Demonstrates real-world event-driven architecture

---
