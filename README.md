# Online Vehicle Rental (Car Rental System)

A Java EE web app for renting cars/bikes, built with **Servlets + JSP** and **MySQL**, with no external MVC framework. It has a public-facing browsing/booking flow for customers and a separate admin console for managing categories, vehicles, and bookings.

> Originally a plain servlet/JSP tutorial-style project; hardened for real deployment (connection pooling, password hashing, access control, safe file uploads — see [Security notes](#security-notes-what-was-fixed) below).

## Features

**Customer**
- Browse vehicles by category, view details and daily rate
- Sign up / log in
- Book a vehicle for a date range with an auto-calculated total price
- View "My Bookings"
- Forgot / reset password (via email + mobile number verification)

**Admin**
- Dashboard with vehicle/category/booking counts
- Add, edit, delete vehicle categories (with image)
- Add, edit, delete vehicles (with image, per-day rate, insurance status, owner/contact info)
- View all bookings across all customers

## Tech stack

| Layer       | Technology                                   |
|-------------|-----------------------------------------------|
| Views       | JSP, JSTL, Bootstrap                          |
| Controllers | Java Servlets (`javax.servlet`, annotation-based routing) |
| Data access | Plain JDBC with `PreparedStatement` (no ORM)  |
| Connection pool | HikariCP                                  |
| Auth        | BCrypt (jBCrypt) password hashing             |
| Database    | MySQL 8                                       |
| Build       | Maven (packaged as a `.war`)                  |
| Target runtime | Java 8, any Servlet 4.0-compatible container (e.g. Apache Tomcat 9) |

## Project structure

```
src/main/java/com/
  entites/   Plain data classes (User, Vehicle, Category, Booking)
  dao/       JDBC data access objects, one per entity
  db/        DBConnect – HikariCP-backed connection pool
  filter/    DBConnectionFilter – returns pooled connections after each request
  util/      AuthUtil (session/role checks), FileUploadUtil (safe image uploads)
  servlet/
    user/    Login, RegisterUser, BookVehicle, Logout
    admin/   AddVehicle, UpdateVehicle, DeleteVehicle, AddCategory, UpdateCategory, DeleteCategory
src/main/webapp/
  *.jsp          Public/customer-facing pages
  admin/*.jsp    Admin console pages
  component/     Shared JSP includes (navbar, footer, css)
  img/           Static images + vehicle_img/ and category_img/ upload targets
src/main/resources/
  db.properties.example   Copy to db.properties and fill in your local DB credentials
schema.sql       MySQL schema (tables + foreign keys)
```

## Prerequisites

- JDK 8+
- Maven 3.6+
- MySQL 8.x
- A Servlet 4.0 container, e.g. Apache Tomcat 9

## Setup

**1. Create the database**

```bash
mysql -u root -p -e "CREATE DATABASE vehicle_rental"
mysql -u root -p vehicle_rental < schema.sql
```

**2. Configure your local DB credentials**

```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

Edit `db.properties` with your MySQL username/password. This file is gitignored on purpose — never commit real credentials to it. In staging/production, prefer setting environment variables instead, which override anything in the file:

```bash
export DB_URL="jdbc:mysql://<host>:3306/vehicle_rental?useSSL=false&serverTimezone=UTC"
export DB_USERNAME="<user>"
export DB_PASSWORD="<password>"
```

**3. Build**

```bash
mvn clean package
```

This produces `target/Online_Vehicle_Rental.war`.

**4. Deploy**

Drop the `.war` into your servlet container's deploy directory (e.g. Tomcat's `webapps/`), or run it from your IDE's server tooling. Once deployed, visit:

```
http://localhost:8080/Online_Vehicle_Rental/
```

**5. Create the first admin account**

Every self-registration is created as a regular `USER` (this is intentional — see [Security notes](#security-notes-what-was-fixed)). To get an admin account:

1. Sign up normally through the app.
2. Promote that account to admin directly in the database:
   ```sql
   UPDATE user SET role = 'ADMIN' WHERE email = 'you@example.com';
   ```
3. Log out and log back in — you'll land on the admin dashboard.

## Security notes (what was fixed)

This project started as a typical tutorial-style CRUD app and has since been hardened:

- **Connection handling**: replaced a single shared `static Connection` (unsafe under concurrent requests) with a HikariCP pool, one connection per request thread.
- **Passwords**: hashed with BCrypt on registration/reset and verified with `BCrypt.checkpw` — never stored or compared as plain text.
- **Broken access control**: every admin servlet (`add/update/deleteVehicle`, `add/update/deleteCategory`) now checks for an authenticated admin session server-side, not just at the JSP-rendering layer.
- **IDOR**: `BookVehicle` now takes the user ID from the session instead of a client-editable hidden form field.
- **Privilege escalation**: `RegisterUser` no longer trusts a client-supplied `role` field — self-registration always creates a `USER`.
- **File uploads**: vehicle/category images are validated against an image-extension allow-list and stored under a server-generated file name, not the client-submitted one (was a path-traversal / arbitrary-file-type risk).
- **Query bugs**: fixed `UserDao.getAllUsers()` (was always returning an empty list) and `VehicleDao.checkVehicleNumber()` (was querying the wrong column, so duplicate vehicle numbers were never caught).

### Known limitations / follow-ups

- If you're migrating an existing database that predates this hardening, any passwords stored as plain text will not work with the new BCrypt-based login — those users need a password reset.
- JSP output (e.g. vehicle titles, user names) is not yet HTML-escaped, so stored XSS is possible if malicious input reaches those fields. Worth adding `<c:out>` / JSTL escaping or a templating layer.
- No CSRF protection on state-changing forms (add/update/delete, booking).
- No automated tests.

## License

Add a license of your choice (e.g. MIT) here before making the repository public.
