# Food Delivery Backend

A RESTful backend API for a food delivery platform, built with Spring Boot. Supports restaurant and menu management, JWT-based authentication, and order placement.

## Features

- **Authentication** — JWT-based registration and login, with role support (`CUSTOMER`, `RESTAURANT_OWNER`, `ADMIN`)
- **Restaurants** — Full CRUD for restaurant listings
- **Menu Items** — Full CRUD for menu items, linked to their restaurant
- **Orders** — Authenticated customers can place orders across multiple menu items; prices are snapshotted at order time so future menu changes don't affect past orders

## Tech Stack

- Java 17
- Spring Boot 3.3.2
- Spring Security + JWT (jjwt)
- Spring Data JPA / Hibernate
- H2 (in-memory database)
- Lombok
- Maven

## Architecture

Layered architecture per module (`entity` → `repository` → `service` → `controller`), with DTOs used for all request/response payloads to keep the API contract decoupled from persistence models.

src/main/java/com/fooddelivery/
├── controller/ # REST endpoints
├── dto/ # Request/response objects
├── entity/ # JPA entities
├── exception/ # Custom exceptions + global handler
├── repository/ # Spring Data JPA repositories
├── security/ # JWT filter, security config, user details service
└── service/ # Business logic
## API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Log in and receive a JWT |

### Restaurants
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/restaurants` | List all restaurants |
| GET | `/api/restaurants/{id}` | Get a restaurant by id |
| POST | `/api/restaurants` | Create a restaurant |
| PUT | `/api/restaurants/{id}` | Update a restaurant |
| DELETE | `/api/restaurants/{id}` | Delete a restaurant |

### Menu Items
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/menu-items/{id}` | Get a menu item by id |
| GET | `/api/menu-items/restaurant/{restaurantId}` | List menu items for a restaurant |
| POST | `/api/menu-items` | Create a menu item |
| PUT | `/api/menu-items/{id}` | Update a menu item |
| DELETE | `/api/menu-items/{id}` | Delete a menu item |

### Orders
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders` | Place an order (authenticated) |
| GET | `/api/orders/{id}` | Get an order by id |
| GET | `/api/orders/my-orders` | Get the logged-in customer's orders |
| GET | `/api/orders/restaurant/{restaurantId}` | Get orders for a restaurant |
| PUT | `/api/orders/{id}/status` | Update order status |

All endpoints except `/api/auth/**` require a JWT sent as `Authorization: Bearer <token>`.

## Running Locally

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`. It uses an in-memory H2 database, so data resets on every restart.

## Example Usage

**Register:**
```json
POST /api/auth/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "CUSTOMER"
}
```

**Place an order:**
```json
POST /api/orders
Authorization: Bearer <token>
{
  "restaurantId": 1,
  "items": [
    { "menuItemId": 1, "quantity": 2 }
  ]
}
```

## Status

Core CRUD and authentication complete. Planned next: role-based authorization (restaurant owners restricted to managing their own restaurants), order status workflow, and API documentation.