# E-Commerce Microservices

This repository contains two Spring Boot microservices for an E-Commerce platform: **User Service** (authentication & user management) and **Product Service** (product catalog with JWT-protected APIs).

---

## Repository Structure

```
E-Commerce/
├── UserService/
│   └── userservicemwfeve/     # User Service (signup, login, JWT validation)
├── ProductService/
│   └── productservicedecmwfeve/  # Product Service (CRUD, paging, filters)
└── README.md
```

---

## User Service

### Summary

User Service handles **user registration**, **login**, and **JWT token validation**. It is the auth authority for the platform: other microservices (e.g. Product Service) call this service to validate JWTs before serving protected resources.

- **Tech stack:** Spring Boot 3.2, Java 17, Spring Data JPA, MySQL, JWT (stateless).
- **Port:** `8181` (local).
- **Database:** Uses its own MySQL database (configured via `USER_SERVICE_DATABASE_*` env vars).
- **AWS Elastic Beanstalk:** [User Service](http://userserviceebs-env.eba-hyu9tihu.ap-southeast-2.elasticbeanstalk.com/)

### Features

| Feature | Description |
|--------|-------------|
| **Sign up** | Create user with default `USER` role or custom roles (`ADMIN`, `USER`). |
| **Login** | Email + password → returns JWT in response. No token stored in DB. |
| **Validate token** | `POST /users/validate/{token}` — used by other services to verify JWT and get user info (UserDto). |
| **Stateless JWT** | No logout endpoint; tokens are valid until expiry. |

### How to Run

1. Set environment variables (or use run configuration):
   - `USER_SERVICE_DATABASE_URL` (e.g. `jdbc:mysql://localhost:3306/userservicedb`)
   - `USER_SERVICE_DATABASE_USERNAME`
   - `USER_SERVICE_DATABASE_PASSWORD`
2. From project root:
   ```bash
   cd UserService/userservicemwfeve
   ./mvnw spring-boot:run
   ```
3. Service runs at **http://localhost:8181**.

| Environment | Base URL |
|-------------|----------|
| **Local** | `http://localhost:8181` |
| **AWS Elastic Beanstalk** | http://userserviceebs-env.eba-hyu9tihu.ap-southeast-2.elasticbeanstalk.com/ |

---

## Product Service

### Summary

Product Service provides **product CRUD** and **listing with paging, sorting, and filters**. All product endpoints require a valid JWT in the `Authorization: Bearer <token>` header; the service validates the token by calling User Service.

- **Tech stack:** Spring Boot 3.2, Java 17, Spring Data JPA, MySQL.
- **Port:** `8080` (local).
- **Database:** Own MySQL DB (e.g. `PRODUCT_SERVICE_DATABASE_URL`). Requires `USER_SERVICE_URL` for token validation.
- **AWS Elastic Beanstalk:** [Product Service](http://newproductservice-env.eba-9kmkaptr.ap-southeast-2.elasticbeanstalk.com)

### Features

| Feature | Description |
|--------|-------------|
| **List products** | Paginated, sortable (id, title, price, description), filterable by name, category, price range. |
| **Get single product** | By ID. |
| **Create product** | POST with title, price, description, imageUrl, optional category. |
| **Update product** | PATCH (partial) or PUT (full replace). |
| **Delete product** | By ID. |

### How to Run

1. Set environment variables:
   - `PRODUCT_SERVICE_DATABASE_URL` (default: `jdbc:mysql://localhost:3306/productservice`)
   - `PRODUCT_SERVICE_DATABASE_USERNAME`
   - `PRODUCT_SERVICE_DATABASE_PASSWORD`
   - `USER_SERVICE_URL` (e.g. `http://localhost:8181` for local User Service)
2. From project root:
   ```bash
   cd ProductService/productservicedecmwfeve
   ./mvnw spring-boot:run
   ```
3. Service runs at **http://localhost:8080**.

| Environment | Base URL |
|-------------|----------|
| **Local** | `http://localhost:8080` |
| **AWS Elastic Beanstalk** | http://newproductservice-env.eba-9kmkaptr.ap-southeast-2.elasticbeanstalk.com |

---

## Postman / cURL Commands

### 1. User Service

| Environment | Base URL |
|-------------|----------|
| **Local** | `http://localhost:8181` |
| **AWS Elastic Beanstalk** | http://userserviceebs-env.eba-hyu9tihu.ap-southeast-2.elasticbeanstalk.com/ |

Replace the base URL in the table below with either the local or AWS URL.

| # | Method | Path | Body / Notes |
|---|--------|------|--------------|
| 1 | **POST** | `/users/signup` | `{"name":"John Doe","email":"john@example.com","password":"secret123"}` — default USER role. |
| 2 | **POST** | `/users/signup` | `{"name":"Admin User","email":"admin@example.com","password":"admin123","roles":["ADMIN","USER"]}` — with roles. |
| 3 | **POST** | `/users/login` | `{"email":"john@example.com","password":"secret123"}` — returns `{"token":"<JWT>"}`. **Use this token for Product Service.** |
| 4 | **POST** | `/users/validate/{token}` | Path: replace `{token}` with JWT from login. Returns UserDto if valid. |

**Example cURL – Sign up (use local or AWS base URL):**
```bash
# Local
curl -X POST http://localhost:8181/users/signup -H "Content-Type: application/json" -d "{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"password\":\"secret123\"}"

# AWS Elastic Beanstalk
curl -X POST http://userserviceebs-env.eba-hyu9tihu.ap-southeast-2.elasticbeanstalk.com/users/signup -H "Content-Type: application/json" -d "{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"password\":\"secret123\"}"
```

**Example cURL – Login (save the `token` for Product Service):**
```bash
# Local
curl -X POST http://localhost:8181/users/login -H "Content-Type: application/json" -d "{\"email\":\"john@example.com\",\"password\":\"secret123\"}"

# AWS
curl -X POST http://userserviceebs-env.eba-hyu9tihu.ap-southeast-2.elasticbeanstalk.com/users/login -H "Content-Type: application/json" -d "{\"email\":\"john@example.com\",\"password\":\"secret123\"}"
```

**Example cURL – Validate token:**
```bash
# Local: curl -X POST "http://localhost:8181/users/validate/YOUR_JWT_HERE"
# AWS:   curl -X POST "http://userserviceebs-env.eba-hyu9tihu.ap-southeast-2.elasticbeanstalk.com/users/validate/YOUR_JWT_HERE"
```

---

### 2. Product Service

| Environment | Base URL |
|-------------|----------|
| **Local** | `http://localhost:8080` |
| **AWS Elastic Beanstalk** | http://newproductservice-env.eba-9kmkaptr.ap-southeast-2.elasticbeanstalk.com |

**Required header for all requests:** `Authorization: Bearer <JWT>` (get JWT from User Service login — use the same environment, e.g. AWS login token for AWS Product Service).

| Method | Path | Body / Query params |
|--------|------|----------------------|
| **GET** | `/products` | Optional: `pageNo`, `pageSize`, `sortBy` (id/title/price/description), `sortDir` (asc/desc), `name`, `category`, `minPrice`, `maxPrice`. |
| **GET** | `/products/{id}` | — |
| **POST** | `/products` | JSON: `{"title":"iPhone 15","price":999.99,"description":"Latest iPhone","imageUrl":"https://example.com/iphone.png"}` |
| **PATCH** | `/products/{id}` | Partial JSON, e.g. `{"title":"Updated Title"}` |
| **PUT** | `/products/{id}` | Full product JSON. |
| **DELETE** | `/products/{id}` | — |

**Example cURL – Get products (with JWT; use local or AWS base URL):**
```bash
# Local
curl -X GET "http://localhost:8080/products?pageNo=0&pageSize=10&sortBy=price&sortDir=asc" -H "Authorization: Bearer YOUR_JWT_HERE"

# AWS
curl -X GET "http://newproductservice-env.eba-9kmkaptr.ap-southeast-2.elasticbeanstalk.com/products?pageNo=0&pageSize=10&sortBy=price&sortDir=asc" -H "Authorization: Bearer YOUR_JWT_HERE"
```

**Example cURL – Create product:**
```bash
# Local: use http://localhost:8080/products
# AWS:   use http://newproductservice-env.eba-9kmkaptr.ap-southeast-2.elasticbeanstalk.com/products
curl -X POST <BASE_URL>/products -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_JWT_HERE" -d "{\"title\":\"iPhone 15\",\"price\":999.99,\"description\":\"Latest iPhone\",\"imageUrl\":\"https://example.com/iphone.png\"}"
```

**Postman – GET /products (paging, sort, filters):**  
Use base URL `http://localhost:8080` (local) or `http://newproductservice-env.eba-9kmkaptr.ap-southeast-2.elasticbeanstalk.com` (AWS).

| Description | Path | Query params |
|-------------|------|--------------|
| First page, 10 per page | `/products` | `?pageNo=0&pageSize=10` |
| Sort by price low→high | `/products` | `?sortBy=price&sortDir=asc` |
| Filter by name contains "phone" | `/products` | `?name=phone` |
| Filter by category | `/products` | `?category=Electronics` |
| Price range | `/products` | `?minPrice=500&maxPrice=1000` |
| Combined | `/products` | `?pageNo=0&pageSize=5&sortBy=price&sortDir=desc&name=phone&minPrice=500&maxPrice=1500` |

Use **Headers:** `Authorization: Bearer <your-jwt-token>` for every Product Service request.

---

## Recommended Flow (Postman)

**Local:** User Service base = `http://localhost:8181`, Product Service base = `http://localhost:8080`.  
**AWS:** User Service base = `http://userserviceebs-env.eba-hyu9tihu.ap-southeast-2.elasticbeanstalk.com`, Product Service base = `http://newproductservice-env.eba-9kmkaptr.ap-southeast-2.elasticbeanstalk.com`.

1. **User Service – Sign up:** `POST <user-service-base>/users/signup` with name, email, password.
2. **User Service – Login:** `POST <user-service-base>/users/login` with email and password → copy the `token` from the response.
3. **Product Service:** Set a Postman environment variable `token` = that JWT, then for each request add header `Authorization: Bearer {{token}}`.
4. Call Product Service endpoints (e.g. `GET <product-service-base>/products`, `POST <product-service-base>/products`, etc.). Use the same environment (local or AWS) for both services so token validation works.

---

## Prerequisites

- **Java 17**
- **Maven** (or use included `./mvnw` in each service)
- **MySQL** — separate databases for User Service and Product Service
- Run **User Service first** so Product Service can validate tokens

---

## Environment Summary

| Service | Port (local) | AWS Elastic Beanstalk | Database URL (env) | Other env |
|---------|---------------|------------------------|--------------------|-----------|
| User Service | 8181 | [userserviceebs-env](http://userserviceebs-env.eba-hyu9tihu.ap-southeast-2.elasticbeanstalk.com/) | `USER_SERVICE_DATABASE_URL` | `USER_SERVICE_DATABASE_USERNAME`, `USER_SERVICE_DATABASE_PASSWORD` |
| Product Service | 8080 | [newproductservice-env](http://newproductservice-env.eba-9kmkaptr.ap-southeast-2.elasticbeanstalk.com) | `PRODUCT_SERVICE_DATABASE_URL` | `PRODUCT_SERVICE_DATABASE_USERNAME`, `PRODUCT_SERVICE_DATABASE_PASSWORD`, `USER_SERVICE_URL` |

For more API detail and edge cases (404, 401), see:
- **User Service:** `UserService/userservicemwfeve/POSTMAN_API.md`
- **Product Service:** `ProductService/productservicedecmwfeve/POSTMAN_API.md`
