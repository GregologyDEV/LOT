CREATE TABLE `flights` (
    `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    `flight_number` VARCHAR(10) NOT NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
    `origin_airport` VARCHAR(5) NOT NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
    `destination_airport` VARCHAR(5) NOT NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
    `departure_time` DATETIME NOT NULL,
    `estimated_arrival_time` DATETIME NOT NULL,
    `available_seats` SMALLINT(6) UNSIGNED NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_route` (`origin_airport`, `destination_airport`) USING BTREE,
    INDEX `idx_flight_number` (`flight_number`) USING BTREE
) COLLATE='utf8mb4_general_ci' ENGINE=InnoDB;


CREATE TABLE `passengers` (
    `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
    `surname` VARCHAR(50) NOT NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
    `phone_number` VARCHAR(50) NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
    PRIMARY KEY (`id`) USING BTREE
) COLLATE='utf8mb4_general_ci' ENGINE=InnoDB;


CREATE TABLE `bookings` (
    `booking_id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    `flight_id` INT(10) UNSIGNED NOT NULL DEFAULT '0',
    `passenger_id` INT(10) UNSIGNED NOT NULL DEFAULT '0',
    `seat_number` VARCHAR(5) NOT NULL DEFAULT '0' COLLATE 'utf8mb4_general_ci',
    PRIMARY KEY (`booking_id`) USING BTREE,
    UNIQUE INDEX `idx_flight_passenger_seat` (`flight_id`, `passenger_id`, `seat_number`) USING BTREE,
    INDEX `passenger_id_FK` (`passenger_id`) USING BTREE,
    CONSTRAINT `flight_id_FK` FOREIGN KEY (`flight_id`) REFERENCES `flights` (`id`) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT `passenger_id_FK` FOREIGN KEY (`passenger_id`) REFERENCES `passengers` (`id`) ON UPDATE CASCADE ON DELETE CASCADE
) COLLATE='utf8mb4_general_ci' ENGINE=InnoDB;