# Food Delivery Backend — Day 1

Core CRUD for `Restaurant` and `MenuItem`, with a clean Controller → Service → Repository structure, DTOs, validation, and global exception handling.

## How to run

1. **Create a Postgres database:**
   ```sql
   CREATE DATABASE food_delivery;
   ```

2. **Update `src/main/resources/application.properties`** with your local Postgres username/password if different from `postgres`/`postgres`.

3. **Run it:**
   ```bash
   mvn spring-boot:run
   ```
   The app starts on `http://localhost:8080`. Tables are auto-created by Hibernate (`ddl-auto=update`).

## Try it out (with curl or Postman)

**Create a restaurant:**
```bash
curl -X POST http://localhost:8080/api/restaurants \
  -H "Content-Type: application/json" \
  -d '{"name": "Spice Route", "address": "MG Road, Jamshedpur"}'
```

**Add a menu item to it (use the id returned above):**
```bash
curl -X POST http://localhost:8080/api/menu-items \
  -H "Content-Type: application/json" \
  -d '{"name": "Paneer Butter Masala", "price": 220.00, "restaurantId": 1}'
```

**Get all restaurants:**
```bash
curl http://localhost:8080/api/restaurants
```

**Get menu for a restaurant:**
```bash
curl http://localhost:8080/api/menu-items/restaurant/1
```

**Try a validation error (empty name):**
```bash
curl -X POST http://localhost:8080/api/restaurants \
  -H "Content-Type: application/json" \
  -d '{"name": "", "address": "Test"}'
```
You should get a 400 with a clean `fieldErrors` map — this is the `GlobalExceptionHandler` at work.

**Try a not-found error:**
```bash
curl http://localhost:8080/api/restaurants/999
```
Returns a 404 with a proper JSON error body instead of a stack trace.

## What's next (Day 2)
`Order` and `OrderItem` entities, the order-creation flow, and status transitions — see the full build guide.
