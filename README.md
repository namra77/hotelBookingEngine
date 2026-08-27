# Hotel Booking Engine

A full-stack hotel booking and management system built with Java and Spring Boot. Supports room browsing, availability checking, bookings, and role-based access for Admins, Managers, and Guests, with secure JWT-based authentication.

## Features

- User registration and login with JWT authentication
- Role-based access control (Admin, Manager, Guest)
- Browse rooms and check availability by date
- Create, view, and cancel bookings
- Payment processing on booking creation
- Admin endpoints for managing all bookings

## Tech Stack

- **Backend:** Java, Spring Boot, Spring Security (JWT)
- **Database:** MySQL
- **Architecture:** Layered (Controller → Service → Repository)
- **API:** REST

## Prerequisites

Before running this project, make sure you have installed:

- Java 17+ (or whichever JDK version the project targets)
- Maven (or Gradle, if that's what the project uses)
- MySQL Server (running locally or accessible remotely)

## Setup & Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/namra77/hotelBookingEngine.git
   cd hotelBookingEngine
   ```

2. **Create the MySQL database**
   ```sql
   CREATE DATABASE hotel_booking_db;
   ```

3. **Configure database connection**

   Open `src/main/resources/application.properties` and update it with your local MySQL credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/hotel_booking_db
   spring.datasource.username=your_mysql_username
   spring.datasource.password=your_mysql_password
   spring.jpa.hibernate.ddl-auto=update
   ```

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   The API will start on `http://localhost:8080` by default.

## Default Test Accounts

Once the app is running, you can log in with:

- **Admin:** `admin@hotel.com` / `admin123`
- **Customer:** `customer@hotel.com` / `cust123`

> Note: adjust this section if these accounts are seeded automatically vs. need to be created manually — see setup notes below if applicable.

## API Testing

For a full list of endpoints, example requests, and a step-by-step test sequence (using curl or Postman), see [`API_TESTING_GUIDE.md`](./API_TESTING_GUIDE.md).

## Project Structure

```
src/main/java/.../
├── controller/     # REST API endpoints
├── service/        # Business logic
├── repository/     # Database access layer
├── model/          # Entity classes
├── security/       # JWT & Spring Security config
```

## Related Repository

Manual QA test cases for this project (functional, regression, and API testing) are documented separately here:
[hotelBookingEngine_Test_Cases](https://github.com/namra77/hotelBookingEngine_Test_Cases)

## Author

Namra Mahmood — [GitHub](https://github.com/namra77)
