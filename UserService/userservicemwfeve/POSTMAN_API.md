# User Service – Postman / cURL Verification

Base URL: `http://userserviceebs-env.eba-hyu9tihu.ap-southeast-2.elasticbeanstalk.com/` (ensure the application is running).

for localhost as mentioned below.

---

## 1. Sign Up (default USER role)

| Method | URL | Body | Description |
|--------|-----|------|--------------|
| POST | `http://localhost:8181/users/signup` | Raw JSON: `{"name":"John Doe","email":"john@example.com","password":"secret123"}` | Creates user with default role **USER**. Expect **201 Created** and `UserDto` in response. |

**cURL:**
```bash
curl -X POST http://localhost:8181/users/signup -H "Content-Type: application/json" -d "{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"password\":\"secret123\"}"
```

---

## 2. Sign Up (with roles)

| Method | URL | Body | Description |
|--------|-----|------|--------------|
| POST | `http://localhost:8181/users/signup` | Raw JSON: `{"name":"Admin User","email":"admin@example.com","password":"admin123","roles":["ADMIN","USER"]}` | Creates user with roles **ADMIN** and **USER**. Expect **201 Created**. |

**cURL:**
```bash
curl -X POST http://localhost:8181/users/signup -H "Content-Type: application/json" -d "{\"name\":\"Admin User\",\"email\":\"admin@example.com\",\"password\":\"admin123\",\"roles\":[\"ADMIN\",\"USER\"]}"
```

---

## 3. Login (success) – stateless JWT

| Method | URL | Body | Description |
|--------|-----|------|--------------|
| POST | `http://localhost:8181/users/login` | Raw JSON: `{"email":"john@example.com","password":"secret123"}` | Returns **200 OK** and `{"token":"<JWT>"}`. No token is stored in DB. Use `token` for validate. |

**cURL:**
```bash
curl -X POST http://localhost:8181/users/login -H "Content-Type: application/json" -d "{\"email\":\"john@example.com\",\"password\":\"secret123\"}"
```

---

## 4. Login (user not found – 404)

| Method | URL | Body | Description |
|--------|-----|------|--------------|
| POST | `http://localhost:8181/users/login` | Raw JSON: `{"email":"nobody@example.com","password":"any"}` | Expect **404 Not Found** and body `{"message":"User not found with email: nobody@example.com"}`. |

**cURL:**
```bash
curl -X POST http://localhost:8181/users/login -H "Content-Type: application/json" -d "{\"email\":\"nobody@example.com\",\"password\":\"any\"}"
```

---

## 5. Login (wrong password – 401)

| Method | URL | Body | Description |
|--------|-----|------|--------------|
| POST | `http://localhost:8181/users/login` | Raw JSON: `{"email":"john@example.com","password":"wrongpassword"}` | Expect **401 Unauthorized** and body `{"message":"Invalid password"}`. |

**cURL:**
```bash
curl -X POST http://localhost:8181/users/login -H "Content-Type: application/json" -d "{\"email\":\"john@example.com\",\"password\":\"wrongpassword\"}"
```

---

## 6. Validate token (for other microservices)

| Method | URL | Headers / Path | Description |
|--------|-----|----------------|--------------|
| POST | `http://localhost:8181/users/validate/{token}` | Path: replace `{token}` with the JWT string from login response (`value` field) | Returns **200 OK** and **UserDto** (name, email, roles, isEmailVerified) if token is valid. Use this so other microservices can validate the JWT. |

**Example (replace `YOUR_JWT_HERE` with actual token from login):**
```bash
curl -X POST "http://localhost:8181/users/validate/YOUR_JWT_HERE"
```

---

## 7. Validate token (invalid/expired – 404 or 401)

| Method | URL | Description |
|--------|-----|-------------|
| POST | `http://localhost:8181/users/validate/invalid-token` | Expect **404** `{"message":"Invalid or expired token"}` or **401** for expired JWT. |

**cURL:**
```bash
curl -X POST "http://localhost:8181/users/validate/invalid-token"
```

---

## Quick flow for microservices (stateless JWT)

1. **Sign up:** `POST /users/signup` with name, email, password (and optional `roles`).
2. **Login:** `POST /users/login` with email and password → get JWT in response `token` (no DB persistence).
3. **Validate (from another service):** `POST /users/validate/{jwt}` → JWT is verified; user is loaded by userId from token; returns UserDto.

There is no logout endpoint; JWTs are stateless and valid until expiry.

All endpoints return JSON. Errors use `{"message":"..."}` with appropriate HTTP status (404, 401, etc.).
