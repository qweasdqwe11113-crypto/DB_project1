-- Create region table
CREATE TABLE IF NOT EXISTS region (
    region_name VARCHAR(100) PRIMARY KEY,
    region_code VARCHAR(10)
);

-- Create airline table
CREATE TABLE IF NOT EXISTS airline (
    airline_code VARCHAR(10) PRIMARY KEY,
    airline_name VARCHAR(100) NOT NULL,
    region_name VARCHAR(100),
    FOREIGN KEY (region_name) REFERENCES region(region_name)
);

-- Create airport table
CREATE TABLE IF NOT EXISTS airport (
    airport_name VARCHAR(100) PRIMARY KEY,
    airport_code VARCHAR(10),
    city VARCHAR(100) NOT NULL,
    region_name VARCHAR(100),
    latitude DECIMAL(10, 6),
    longitude DECIMAL(10, 6),
    altitude INT,
    timezone_offset DECIMAL(3, 1),
    timezone_dst VARCHAR(1),
    timezone_region VARCHAR(50),
    FOREIGN KEY (region_name) REFERENCES region(region_name)
);

-- Create passenger table
CREATE TABLE IF NOT EXISTS passenger (
    passenger_id VARCHAR(10) PRIMARY KEY,
    passenger_name VARCHAR(100) NOT NULL,
    age INT,
    gender VARCHAR(10),
    mobile_number VARCHAR(20)
);

-- Create passenger contact relationship table
CREATE TABLE IF NOT EXISTS passenger_contact (
    owner_passenger_id VARCHAR(10),
    contact_passenger_id VARCHAR(10),
    PRIMARY KEY (owner_passenger_id, contact_passenger_id),
    FOREIGN KEY (owner_passenger_id) REFERENCES passenger(passenger_id),
    FOREIGN KEY (contact_passenger_id) REFERENCES passenger(passenger_id),
    CHECK (owner_passenger_id <> contact_passenger_id)
);

-- Create flight table
CREATE TABLE IF NOT EXISTS flight (
    flight_number VARCHAR(20) PRIMARY KEY,
    source_airport VARCHAR(100),
    destination_airport VARCHAR(100),
    flight_date DATE,
    departure_time TIME,
    arrival_time TIME,
    arrival_day_offset SMALLINT NOT NULL DEFAULT 0,
    FOREIGN KEY (source_airport) REFERENCES airport(airport_name),
    FOREIGN KEY (destination_airport) REFERENCES airport(airport_name)
);

-- Backfill arrival_day_offset for existing flight table
ALTER TABLE flight
ADD COLUMN IF NOT EXISTS arrival_day_offset SMALLINT NOT NULL DEFAULT 0;

-- Create ticket table
CREATE TABLE IF NOT EXISTS ticket (
    ticket_id SERIAL PRIMARY KEY,
    flight_number VARCHAR(20),
    business_price DECIMAL(10, 2),
    business_remain INT,
    economy_price DECIMAL(10, 2),
    economy_remain INT,
    FOREIGN KEY (flight_number) REFERENCES flight(flight_number)
);

-- Create ticket order table
CREATE TABLE IF NOT EXISTS ticket_order (
    order_id SERIAL PRIMARY KEY,
    passenger_id VARCHAR(10),
    ticket_id INT,
    cabin_class VARCHAR(20) NOT NULL,
    order_time TIMESTAMP NOT NULL,
    FOREIGN KEY (passenger_id) REFERENCES passenger(passenger_id),
    FOREIGN KEY (ticket_id) REFERENCES ticket(ticket_id)
);
