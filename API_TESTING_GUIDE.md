# Hotel Booking API Testing Guide

###  Default Credentials
After fixing, use these credentials:
- **Admin**: `admin@hotel.com` / `admin123`
- **Customer**: `customer@hotel.com` / `cust123`

## Testing with Postman/Curl

### 1. Test Authentication (Login)
```bash
# Test admin login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@hotel.com","password":"admin123"}'

# Test customer login  
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"customer@hotel.com","password":"cust123"}'
```

### 2. Test Public Endpoints (No Auth Required)
```bash
# Get all rooms
curl -X GET http://localhost:8080/api/rooms

# Get available rooms
curl -X GET http://localhost:8080/api/rooms/available

# Get available rooms for specific dates
curl -X GET "http://localhost:8080/api/rooms/available?checkIn=2024-12-01&checkOut=2024-12-05"

# Check room availability
curl -X GET "http://localhost:8080/api/bookings/check-availability?roomId=1&checkIn=2024-12-01&checkOut=2024-12-05"

# Register new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"John Doe","email":"john@example.com","password":"password123"}'
```

### 3. Test Protected Endpoints (With Basic Auth)

#### Admin Endpoints
```bash
# Get all bookings (Admin only)
curl -X GET http://localhost:8080/api/bookings \
  -u "admin@hotel.com:admin123"

# Cancel booking as admin
curl -X DELETE http://localhost:8080/api/bookings/1/admin \
  -u "admin@hotel.com:admin123"
```

#### Customer Endpoints
```bash
# Get my bookings
curl -X GET http://localhost:8080/api/bookings/my \
  -u "customer@hotel.com:cust123"

# Create a booking
curl -X POST http://localhost:8080/api/bookings \
  -u "customer@hotel.com:cust123" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "roomId": 1,
    "checkIn": "2024-12-01",
    "checkOut": "2024-12-05",
    "paymentMethod": "CREDIT_CARD"
  }'

# Cancel my booking
curl -X DELETE http://localhost:8080/api/bookings/my/1 \
  -u "customer@hotel.com:cust123"
```

## Postman Setup

### 1. Set Up Basic Authentication
1. Go to the **Authorization** tab
2. Select **Basic Auth** from the dropdown
3. Enter:
   - Username: `admin@hotel.com` (or `customer@hotel.com`)
   - Password: `admin123` (or `cust123`)

### 2. Common Headers
Add these headers to your requests:
```
Content-Type: application/json
Accept: application/json
```


3. **Test Authentication Separately**
   ```bash
   # This should return user info if auth works
   curl -X GET http://localhost:8080/api/users/profile \
     -u "admin@hotel.com:admin123"
   ```

### Common HTTP Status Codes:
- `200 OK` - Success
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Authentication failed
- `403 Forbidden` - Access denied (wrong role)
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Example Test Sequence

1. Register a new user
2. Login with the new user
3. Get available rooms
4. Create a booking
5. Get my bookings
6. Cancel the booking

This should give you a complete test of your API functionality!
