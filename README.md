# 🪙 Crypto Transfer API

A high-performance REST API for cryptocurrency transfers powered by the **Binance API**. Built with **Java 21** and **Spring Boot 3**, designed to be embedded in any platform that needs fast, reliable on-chain transactions.

---

## ✨ Features

- ⚡ **Fast transfers** — async withdrawal execution with real-time status tracking
- 🔐 **JWT Authentication** — stateless, secure token-based auth
- 💱 **Multi-currency** — BTC, ETH, BNB, USDT, SOL, TRX, XRP and more
- 📊 **Live prices** — real-time market data from Binance ticker
- 🔁 **Auto-retry & timeout** — scheduled sync jobs catch stuck transactions
- 📖 **Swagger UI** — interactive API docs at `/swagger-ui.html`
- 🩺 **Health endpoints** — Spring Actuator for monitoring

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 15+
- Binance account with API key & secret

### Environment Variables

```bash
export BINANCE_API_KEY=your_binance_api_key
export BINANCE_SECRET_KEY=your_binance_secret_key
export DB_USERNAME=postgres
export DB_PASSWORD=your_db_password
export JWT_SECRET=your_32_char_secret_here
```

### Run

```bash
git clone https://github.com/youruser/crypto-transfer-api.git
cd crypto-transfer-api
mvn spring-boot:run
```

API will start on `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 📡 API Reference

### Authentication

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "apiKey": "your_api_key",
  "apiSecret": "your_api_secret"
}
```

Returns a `Bearer` JWT token. Include it in all subsequent requests:
```
Authorization: Bearer <token>
```

---

### Transfer Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/transfer/send` | Initiate a transfer |
| `GET` | `/api/v1/transfer/status/{txId}` | Get transaction status |
| `GET` | `/api/v1/transfer/history` | Get transfer history |
| `GET` | `/api/v1/transfer/balance/{currency}` | Get wallet balance |
| `GET` | `/api/v1/transfer/price/{symbol}` | Get live market price |
| `POST` | `/api/v1/transfer/cancel/{txId}` | Cancel pending transfer |

---

### Send Crypto — Example

```http
POST /api/v1/transfer/send
Authorization: Bearer <token>
Content-Type: application/json

{
  "fromAddress": "0xYourWalletAddress",
  "toAddress": "0xRecipientAddress",
  "currency": "USDT",
  "amount": "50.00",
  "network": "BSC",
  "memo": null
}
```

**Response:**
```json
{
  "txId": "A1B2C3D4E5F6...",
  "status": "PENDING",
  "currency": "USDT",
  "amount": 50.00,
  "fee": 1.05,
  "createdAt": "2024-11-01T12:00:00"
}
```

---

## 🏗️ Project Structure

```
src/main/java/com/cryptoapi/
├── controller/        # REST controllers (Transfer, Auth)
├── service/           # Business logic (Transfer, Binance, Auth, JWT, Fee, Notification)
├── repository/        # Spring Data JPA repositories
├── entity/            # JPA entities (Transaction)
├── dto/               # Request/Response DTOs
├── config/            # Spring Security, JWT filter, AppConfig
├── scheduler/         # Async jobs (status sync, stuck tx cleanup)
├── exception/         # Custom exceptions + global handler
├── enums/             # TransactionStatus
└── util/              # HmacSignatureUtil, AddressValidator
```

---

## 🔒 Security

- All endpoints (except `/api/v1/auth/**` and `/swagger-ui/**`) require a valid JWT.
- Binance API calls are signed with HMAC-SHA256 per Binance's requirements.
- Sensitive config is injected via environment variables — never hardcoded.

---

## 📦 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JJWT |
| Database | PostgreSQL + Spring Data JPA |
| Docs | SpringDoc OpenAPI (Swagger) |
| Build | Maven |
| Exchange | Binance REST API |

---

## ⚠️ Disclaimer

This project is intended for **educational and portfolio purposes**. Always follow Binance's Terms of Service and applicable regulations when handling real funds.

---

## 📄 License

MIT License © 2024
