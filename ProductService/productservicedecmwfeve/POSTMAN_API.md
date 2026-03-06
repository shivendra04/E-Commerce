# ProductService API – Postman Reference

## Base URLs

| Environment | Base URL |
|-------------|----------|
| **Local** | `http://localhost:8080` |
| **AWS Elastic Beanstalk** | `http://newproductservice-env.eba-9kmkaptr.ap-southeast-2.elasticbeanstalk.com` |

All requests require a valid JWT in the `Authorization` header (obtain from UserService login).

---

## API Endpoints (use with either base URL above)

| Method | Path | Headers | Body |
|--------|------|---------|------|
| GET | `/products` | `Authorization: Bearer <token>` | — (supports paging/sorting query params) |
| GET | `/products/{id}` | `Authorization: Bearer <token>` | — |
| POST | `/products` | `Authorization: Bearer <token>` | `Content-Type: application/json` — e.g. `{"title":"Product","price":99.99,"description":"Desc","imageUrl":"http://..."}` |
| PATCH | `/products/{id}` | `Authorization: Bearer <token>` | `Content-Type: application/json` — partial fields e.g. `{"title":"Updated Title"}` |
| PUT | `/products/{id}` | `Authorization: Bearer <token>` | `Content-Type: application/json` — full product |
| DELETE | `/products/{id}` | `Authorization: Bearer <token>` | — |

### GET /products – paging, sorting, and filters (query params)

| Param       | Default | Description |
|-------------|---------|-------------|
| `pageNo`    | `0`     | Page number (0-based). |
| `pageSize`  | `10`    | Number of products per page. |
| `sortBy`    | `id`    | Field to sort by: `id`, `title`, `price`, `description`. |
| `sortDir`   | `asc`   | Sort direction: `asc` or `desc`. |
| `name`      | —       | Optional. Filter by product title (contains, case-insensitive). |
| `category`  | —       | Optional. Filter by category name (exact match, case-insensitive). |
| `minPrice`  | —       | Optional. Minimum price (inclusive). |
| `maxPrice`  | —       | Optional. Maximum price (inclusive). |

You can combine filters in any way (e.g. `name` + `category` + `minPrice`/`maxPrice`) together with paging and sorting.

Response body shape:

```json
{
  "content": [
    { "id": 1, "title": "Product A", "price": 99.99, "category": "Electronics", "description": "...", "imageUrl": "..." }
  ],
  "pageNo": 0,
  "pageSize": 10,
  "totalElements": 25,
  "totalPages": 3,
  "sort": "id: ASC"
}
```

---

### Full URLs for local & Elastic Beanstalk

| Method | Description | Local URL | Elastic Beanstalk URL |
|--------|-------------|-----------|------------------------|
| GET | Get products (supports paging/sorting/filters) | `http://localhost:8080/products` | `http://newproductservice-env.eba-9kmkaptr.ap-southeast-2.elasticbeanstalk.com/products` |
| GET | Get single product | `http://localhost:8080/products/{id}` | `http://newproductservice-env.eba-9kmkaptr.ap-southeast-2.elasticbeanstalk.com/products/{id}` |
| POST | Add product | `http://localhost:8080/products` | `http://newproductservice-env.eba-9kmkaptr.ap-southeast-2.elasticbeanstalk.com/products` |
| PATCH | Partially update product | `http://localhost:8080/products/{id}` | `http://newproductservice-env.eba-9kmkaptr.ap-southeast-2.elasticbeanstalk.com/products/{id}` |
| PUT | Replace product | `http://localhost:8080/products/{id}` | `http://newproductservice-env.eba-9kmkaptr.ap-southeast-2.elasticbeanstalk.com/products/{id}` |
| DELETE | Delete product | `http://localhost:8080/products/{id}` | `http://newproductservice-env.eba-9kmkaptr.ap-southeast-2.elasticbeanstalk.com/products/{id}` |

## Example JSON body (POST / PUT)

```json
{
  "title": "iPhone 15",
  "price": 999.99,
  "description": "Latest iPhone",
  "imageUrl": "https://example.com/iphone.png",
  "numberOfSales": 0
}
```

For POST with category (optional):

```json
{
  "title": "iPhone 15",
  "price": 999.99,
  "description": "Latest iPhone",
  "imageUrl": "https://example.com/iphone.png",
  "numberOfSales": 0,
  "category": {
    "name": "Electronics",
    "description": "Electronic devices",
    "imageUrl": "https://example.com/cat.png"
  }
}
```

## Notes

- Replace `<token>` with the JWT returned by UserService (login endpoint: local `http://localhost:8181` or your UserService URL in production).
- Replace `{id}` with the product ID (e.g. `1`, `2`).
- **Elastic Beanstalk:** Your app must be configured with the correct `USER_SERVICE_URL` (or similar) so token validation calls your UserService (e.g. your UserService Elastic Beanstalk URL) instead of localhost.
- In Postman, you can define a `baseUrl` variable in an environment:
  - **Local environment**: `baseUrl = http://localhost:8080`
  - **Elastic Beanstalk environment**: `baseUrl = http://newproductservice-env.eba-9kmkaptr.ap-southeast-2.elasticbeanstalk.com`
  - Then use `{{baseUrl}}` in all request URLs (examples below).

---

## Postman examples – GET /products (paging, sorting & filters)

| Description | Method | URL | Query params |
|-------------|--------|-----|--------------|
| First page, 10 per page, sort by id asc | GET | `{{baseUrl}}/products` | (defaults) or `?pageNo=0&pageSize=10&sortBy=id&sortDir=asc` |
| Second page, 5 per page | GET | `{{baseUrl}}/products` | `?pageNo=1&pageSize=5` |
| Sort by title A–Z | GET | `{{baseUrl}}/products` | `?sortBy=title&sortDir=asc` |
| Sort by title Z–A | GET | `{{baseUrl}}/products` | `?sortBy=title&sortDir=desc` |
| Sort by price low to high | GET | `{{baseUrl}}/products` | `?sortBy=price&sortDir=asc` |
| Sort by price high to low | GET | `{{baseUrl}}/products` | `?sortBy=price&sortDir=desc` |
| Sort by name (same as title) | GET | `{{baseUrl}}/products` | `?sortBy=title&sortDir=asc` |
| Filter by name contains "phone" | GET | `{{baseUrl}}/products` | `?name=phone` |
| Filter by category "Electronics" | GET | `{{baseUrl}}/products` | `?category=Electronics` |
| Filter by price range 500–1000 | GET | `{{baseUrl}}/products` | `?minPrice=500&maxPrice=1000` |
| Name + category + price range | GET | `{{baseUrl}}/products` | `?name=phone&category=Electronics&minPrice=500&maxPrice=1500` |
| All filters + paging + sort | GET | `{{baseUrl}}/products` | `?pageNo=0&pageSize=5&sortBy=price&sortDir=desc&name=phone&category=Electronics&minPrice=500&maxPrice=1500` |

**Headers for all:** `Authorization: Bearer <your-jwt-token>`.
