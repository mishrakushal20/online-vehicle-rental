-- Schema for the Online Vehicle Rental (Car Rental System) database.
-- Create the database and run this whole file against it, e.g.:
--   mysql -u root -p -e "CREATE DATABASE vehicle_rental"
--   mysql -u root -p vehicle_rental < schema.sql

CREATE TABLE IF NOT EXISTS user (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100)  NOT NULL,
    email    VARCHAR(150)  NOT NULL UNIQUE,
    password VARCHAR(255)  NOT NULL, -- BCrypt hash, not plain text
    address  VARCHAR(255),
    city     VARCHAR(100),
    state    VARCHAR(100),
    pincode  VARCHAR(20),
    role     VARCHAR(20)   NOT NULL DEFAULT 'USER', -- 'USER' or 'ADMIN'
    mobno    VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS category (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    image VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS vehicle (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    title            VARCHAR(150) NOT NULL,
    vehicleNumber    VARCHAR(50)  NOT NULL UNIQUE,
    categoryId       INT          NOT NULL,
    availability     VARCHAR(20)  NOT NULL DEFAULT 'Available', -- 'Available' or 'Booked'
    perDay           DOUBLE       NOT NULL,
    insuranceStatus  VARCHAR(20),
    description      TEXT,
    ownerName        VARCHAR(100),
    contactNo        VARCHAR(20),
    image            VARCHAR(255) NOT NULL,
    CONSTRAINT fk_vehicle_category FOREIGN KEY (categoryId) REFERENCES category(id)
);

CREATE TABLE IF NOT EXISTS booking (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    userId      INT          NOT NULL,
    vehicleId   INT          NOT NULL,
    fromDate    VARCHAR(20)  NOT NULL,
    toDate      VARCHAR(20)  NOT NULL,
    day         VARCHAR(20)  NOT NULL,
    totalPrice  DOUBLE       NOT NULL,
    idCard      VARCHAR(100),
    orderId     VARCHAR(50)  NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'Booked',
    bookingDate VARCHAR(20)  NOT NULL,
    CONSTRAINT fk_booking_user    FOREIGN KEY (userId)    REFERENCES user(id),
    CONSTRAINT fk_booking_vehicle FOREIGN KEY (vehicleId) REFERENCES vehicle(id)
);
