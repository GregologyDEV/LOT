CREATE TABLE `flights` (
    `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    `flight_number` VARCHAR(10) NOT NULL DEFAULT '',
    `origin_airport` VARCHAR(5) NOT NULL DEFAULT '',
    `destination_airport` VARCHAR(5) NOT NULL DEFAULT '',
    `departure_time` DATETIME NOT NULL,
    `estimated_arrival_time` DATETIME NOT NULL,
    `available_seats` SMALLINT(6) UNSIGNED NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_route` (`origin_airport`, `destination_airport`) USING BTREE,
    INDEX `idx_flight_number` (`flight_number`) USING BTREE
);


CREATE TABLE `passengers` (
    `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL DEFAULT '',
    `surname` VARCHAR(50) NOT NULL DEFAULT '',
    `phone_number` VARCHAR(50) NULL DEFAULT '',
    PRIMARY KEY (`id`) USING BTREE
);


CREATE TABLE `bookings` (
    `booking_id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    `flight_id` INT(10) UNSIGNED NOT NULL DEFAULT '0',
    `passenger_id` INT(10) UNSIGNED NOT NULL DEFAULT '0',
    `seat_number` VARCHAR(5) NOT NULL DEFAULT '0',
    PRIMARY KEY (`booking_id`) USING BTREE,
    UNIQUE INDEX `idx_flight_passenger_seat` (`flight_id`, `passenger_id`, `seat_number`) USING BTREE,
    INDEX `passenger_id_FK` (`passenger_id`) USING BTREE,
    CONSTRAINT `flight_id_FK` FOREIGN KEY (`flight_id`) REFERENCES `flights` (`id`) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT `passenger_id_FK` FOREIGN KEY (`passenger_id`) REFERENCES `passengers` (`id`) ON UPDATE CASCADE ON DELETE CASCADE
);