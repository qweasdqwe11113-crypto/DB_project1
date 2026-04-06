-- 导入地区数据（region.csv: name,code）
CREATE TEMP TABLE staging_region (
    region_name VARCHAR(100),
    region_code VARCHAR(10)
);

COPY staging_region(region_name, region_code)
FROM 'D:\code\ideaprogramms\DB_project1\data\region.csv'
WITH (FORMAT csv, HEADER true, DELIMITER ',');

-- 导入地区数据，统一地区名称并去重
INSERT INTO region(region_name, region_code)
SELECT DISTINCT
    CASE
        WHEN region_name = 'Hong Kong SAR of China' THEN 'Hong Kong'
        WHEN region_name = 'Republic of Korea' THEN 'South Korea'
        ELSE region_name
    END AS region_name,
    region_code
FROM staging_region
ON CONFLICT (region_name) DO NOTHING;

-- 清理临时表
DROP TABLE staging_region;









-- 导入航空公司数据（airline.csv: id,code,name,region）
CREATE TEMP TABLE staging_airline (
                                      id INT,
                                      airline_code VARCHAR(10),
                                      airline_name VARCHAR(100),
                                      region_name VARCHAR(100)
);

COPY staging_airline(id, airline_code, airline_name, region_name)
    FROM 'D:\code\ideaprogramms\DB_project1\data\airline.csv'
    WITH (FORMAT csv, HEADER true, DELIMITER ',');

-- 处理地区名称映射
CREATE TEMP TABLE staging_airline_processed AS
SELECT 
    airline_code,
    airline_name,
    CASE 
        WHEN region_name = 'Hong Kong SAR of China' THEN 'Hong Kong'
        WHEN region_name = 'Republic of Korea' THEN 'South Korea'
        WHEN region_name = 'DRAGON' THEN 'Hong Kong'
        ELSE region_name
    END AS region_name
FROM staging_airline;

-- 导入航空公司数据，处理外键关联
INSERT INTO airline(airline_code, airline_name, region_name)
SELECT DISTINCT airline_code, airline_name, region_name
FROM staging_airline_processed
ON CONFLICT (airline_code) DO NOTHING;

-- 清理临时表
DROP TABLE staging_airline;
DROP TABLE staging_airline_processed;







-- 导入机场数据（airport.csv: id,name,city,region,iata_code,latitude,longitude,altitude,timezone_offset,timezone_dst,timezone_region）
CREATE TEMP TABLE staging_airport (
    id INT,
    airport_name VARCHAR(100),
    city VARCHAR(100),
    region_name VARCHAR(100),
    airport_code VARCHAR(10),
    latitude DECIMAL(10, 6),
    longitude DECIMAL(10, 6),
    altitude INT,
    timezone_offset DECIMAL(3, 1),
    timezone_dst VARCHAR(1),
    timezone_region VARCHAR(50)
);

COPY staging_airport(id, airport_name, city, region_name, airport_code, latitude, longitude, altitude, timezone_offset, timezone_dst, timezone_region)
FROM 'D:\code\ideaprogramms\DB_project1\data\airport.csv'
WITH (FORMAT csv, HEADER true, DELIMITER ',');

-- 处理地区名称映射
CREATE TEMP TABLE staging_airport_processed AS
SELECT 
    airport_name,
    airport_code,
    city,
    CASE 
        WHEN region_name = 'Hong Kong SAR of China' THEN 'Hong Kong'
        WHEN region_name = 'Republic of Korea' THEN 'South Korea'
        ELSE region_name
    END AS region_name,
    latitude,
    longitude,
    altitude,
    timezone_offset,
    timezone_dst,
    timezone_region
FROM staging_airport;

-- 导入机场数据，处理外键关联
INSERT INTO airport(airport_name, airport_code, city, region_name, latitude, longitude, altitude, timezone_offset, timezone_dst, timezone_region)
SELECT DISTINCT airport_name, airport_code, city, region_name, latitude, longitude, altitude, timezone_offset, timezone_dst, timezone_region
FROM staging_airport_processed
ON CONFLICT (airport_name) DO NOTHING;

-- 清理临时表
DROP TABLE staging_airport;
DROP TABLE staging_airport_processed;






-- 导入乘客数据（passenger.csv: id,name,age,gender,mobile_number）
CREATE TEMP TABLE staging_passenger (
                                        passenger_id VARCHAR(10),
                                        passenger_name VARCHAR(100),
                                        age INT,
                                        gender VARCHAR(10),
                                        mobile_number VARCHAR(20)
);

COPY staging_passenger(passenger_id, passenger_name, age, gender, mobile_number)
    FROM 'D:\code\ideaprogramms\DB_project1\data\passenger.csv'
    WITH (FORMAT csv, HEADER true, DELIMITER ',');

-- 导入乘客数据
INSERT INTO passenger(passenger_id, passenger_name, age, gender, mobile_number)
SELECT DISTINCT passenger_id, passenger_name, age, gender, mobile_number
FROM staging_passenger
ON CONFLICT (passenger_id) DO NOTHING;

-- 清理临时表
DROP TABLE staging_passenger;







-- 导入航班数据（tickets.csv -> flight）
CREATE TEMP TABLE staging_ticket_flight (
    flight_number_raw VARCHAR(20),
    airline_name VARCHAR(100),
    airline_region VARCHAR(100),
    source_city VARCHAR(100),
    source_region VARCHAR(100),
    source_code VARCHAR(10),
    destination_city VARCHAR(100),
    destination_region VARCHAR(100),
    destination_code VARCHAR(10),
    flight_date_raw VARCHAR(20),
    departure_time_raw VARCHAR(20),
    arrival_time_raw VARCHAR(20),
    business_price_raw VARCHAR(20),
    business_remain_raw VARCHAR(20),
    economy_price_raw VARCHAR(20),
    economy_remain_raw VARCHAR(20)
);

COPY staging_ticket_flight(
    flight_number_raw,
    airline_name,
    airline_region,
    source_city,
    source_region,
    source_code,
    destination_city,
    destination_region,
    destination_code,
    flight_date_raw,
    departure_time_raw,
    arrival_time_raw,
    business_price_raw,
    business_remain_raw,
    economy_price_raw,
    economy_remain_raw
)
FROM 'D:\code\ideaprogramms\DB_project1\data\tickets.csv'
WITH (FORMAT csv, HEADER true, DELIMITER ',');

-- 当前 flight 表以 flight_number 为主键，为避免同一航班号不同日期冲突，拼接日期生成唯一 flight_number
INSERT INTO flight(
    flight_number,
    source_airport,
    destination_airport,
    flight_date,
    departure_time,
    arrival_time,
    arrival_day_offset
)
SELECT DISTINCT
    t.flight_number_raw || '_' || TO_CHAR(TO_DATE(t.flight_date_raw, 'YYYY/MM/DD'), 'YYYYMMDD') AS flight_number,
    src.airport_name AS source_airport,
    dst.airport_name AS destination_airport,
    TO_DATE(t.flight_date_raw, 'YYYY/MM/DD') AS flight_date,
    TO_TIMESTAMP(t.departure_time_raw, 'HH24:MI')::TIME AS departure_time,
    TO_TIMESTAMP(SPLIT_PART(t.arrival_time_raw, '(+1)', 1), 'HH24:MI')::TIME AS arrival_time,
    CASE
        WHEN t.arrival_time_raw LIKE '%(+1)%' THEN 1
        ELSE 0
    END AS arrival_day_offset
FROM staging_ticket_flight t
JOIN airport src ON src.airport_code = t.source_code
JOIN airport dst ON dst.airport_code = t.destination_code
ON CONFLICT (flight_number) DO NOTHING;

-- 清理临时表
DROP TABLE staging_ticket_flight;










-- 导入机票数据（tickets.csv -> ticket）
CREATE TEMP TABLE staging_ticket_ticket (
    flight_number_raw VARCHAR(20),
    airline_name VARCHAR(100),
    airline_region VARCHAR(100),
    source_city VARCHAR(100),
    source_region VARCHAR(100),
    source_code VARCHAR(10),
    destination_city VARCHAR(100),
    destination_region VARCHAR(100),
    destination_code VARCHAR(10),
    flight_date_raw VARCHAR(20),
    departure_time_raw VARCHAR(20),
    arrival_time_raw VARCHAR(20),
    business_price_raw VARCHAR(20),
    business_remain_raw VARCHAR(20),
    economy_price_raw VARCHAR(20),
    economy_remain_raw VARCHAR(20)
);

COPY staging_ticket_ticket(
    flight_number_raw,
    airline_name,
    airline_region,
    source_city,
    source_region,
    source_code,
    destination_city,
    destination_region,
    destination_code,
    flight_date_raw,
    departure_time_raw,
    arrival_time_raw,
    business_price_raw,
    business_remain_raw,
    economy_price_raw,
    economy_remain_raw
)
FROM 'D:\code\ideaprogramms\DB_project1\data\tickets.csv'
WITH (FORMAT csv, HEADER true, DELIMITER ',');

INSERT INTO ticket(flight_number, business_price, business_remain, economy_price, economy_remain)
SELECT DISTINCT
    t.flight_number_raw || '_' || TO_CHAR(TO_DATE(t.flight_date_raw, 'YYYY/MM/DD'), 'YYYYMMDD') AS flight_number,
    t.business_price_raw::DECIMAL(10, 2) AS business_price,
    t.business_remain_raw::INT AS business_remain,
    t.economy_price_raw::DECIMAL(10, 2) AS economy_price,
    t.economy_remain_raw::INT AS economy_remain
FROM staging_ticket_ticket t
JOIN flight f ON f.flight_number = t.flight_number_raw || '_' || TO_CHAR(TO_DATE(t.flight_date_raw, 'YYYY/MM/DD'), 'YYYYMMDD')
LEFT JOIN ticket tk ON tk.flight_number = f.flight_number
    AND tk.business_price = t.business_price_raw::DECIMAL(10, 2)
    AND tk.business_remain = t.business_remain_raw::INT
    AND tk.economy_price = t.economy_price_raw::DECIMAL(10, 2)
    AND tk.economy_remain = t.economy_remain_raw::INT
WHERE tk.ticket_id IS NULL;

-- 清理临时表
DROP TABLE staging_ticket_ticket;







-- 导入订单数据（passenger + ticket -> ticket_order）
WITH ranked_passenger AS (
    SELECT passenger_id, ROW_NUMBER() OVER (ORDER BY passenger_id) AS rn
    FROM passenger
),
ranked_ticket AS (
    SELECT ticket_id, ROW_NUMBER() OVER (ORDER BY ticket_id) AS rn
    FROM ticket
),
paired_orders AS (
    SELECT
        p.passenger_id,
        t.ticket_id,
        p.rn
    FROM ranked_passenger p
    JOIN ranked_ticket t ON p.rn = t.rn
)
INSERT INTO ticket_order(passenger_id, ticket_id, cabin_class, order_time)
SELECT
    po.passenger_id,
    po.ticket_id,
    CASE WHEN po.rn % 2 = 0 THEN 'Economy' ELSE 'Business' END AS cabin_class,
    NOW() - (po.rn || ' minutes')::INTERVAL AS order_time
FROM paired_orders po
WHERE NOT EXISTS (
    SELECT 1
    FROM ticket_order o
    WHERE o.passenger_id = po.passenger_id
      AND o.ticket_id = po.ticket_id
);














