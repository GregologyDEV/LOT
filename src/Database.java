import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/lot";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private static Connection connection;

    public Database() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Database connected successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param flightNumber IATA flight number
     * @return <code>true</code> if flight of given number exists in database <p> <code>false</code> if not
     */
    public static boolean doesFlightExists(String flightNumber) {
        String sql = "SELECT COUNT(*) FROM flights WHERE flight_number = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, flightNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    /**
     *
     * @param flightNumber IATA flight number
     * @return Flight object if such exists in database <p> null otherwise
     */
    public static Flight getFlight(String flightNumber) {
        if (!Flight.isFlightNumberCorrect(flightNumber.toUpperCase())) throw new IllegalArgumentException("Flight number is incorrect");
        String sql = "SELECT * FROM flights WHERE flight_number = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, flightNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String originAirport = resultSet.getString("origin_airport");
                    String destinationAirport = resultSet.getString("destination_airport");
                    Timestamp departureTime = resultSet.getTimestamp("departure_time");
                    Timestamp estimatedArrivalTime = resultSet.getTimestamp("estimated_arrival_time");
                    int availableSeats = resultSet.getInt("available_seats");

                    Map<Passenger, Integer> passengersAndSeats = getPassengersOnFlight(id);

                    return new Flight(id, flightNumber, originAirport, destinationAirport, departureTime, estimatedArrivalTime, availableSeats, passengersAndSeats);
                } else {
                    System.out.println("Flight not found");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     *
     * @param id Flight database id
     * @return Map: key -> Passenger object <p> value -> booked seat number
     */
    public static Map<Passenger, Integer> getPassengersOnFlight(int id) {
        Map<Passenger, Integer> result = new HashMap<>();
        String sql = "SELECT p.*, b.seat_number FROM passengers p JOIN bookings b on p.id = b.passenger_id WHERE b.flight_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Passenger p = new Passenger(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("surname"),
                            resultSet.getString("phone_number")
                    );

                    result.put(p, resultSet.getInt("seat_number"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     *
     * @param passenger Passenger object
     * @return Map: key -> Flight object <p> value -> booked seat number
     */
    public static Map<Flight, Integer> getAllPassengerFlights(Passenger passenger) {
        Map<Flight, Integer> flights = new HashMap<>();
        String sql = "SELECT f.*, b.seat_number FROM flights f JOIN bookings b on f.id = b.flight_id WHERE b.passenger_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, passenger.getDbID());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    Map<Passenger, Integer> passengersAndSeats = getPassengersOnFlight(id);
                    Flight f = new Flight(
                            id,
                            resultSet.getString("flight_number"),
                            resultSet.getString("origin_airport"),
                            resultSet.getString("destination_airport"),
                            resultSet.getTimestamp("departure_time"),
                            resultSet.getTimestamp("estimated_arrival_time"),
                            resultSet.getInt("available_seats"),
                            passengersAndSeats
                    );

                    flights.put(f, resultSet.getInt("seat_number"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return flights;
    }

    /**
     *
     * @param name Passenger name
     * @param surname Passenger surname
     * @return <code>true</code> if passenger exists in database <p> <code>false</code> otherwise
     */
    public static boolean doesPassengerExists(String name, String surname) {
        String sql = "SELECT COUNT(*) FROM passengers WHERE name = ? AND surname = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, surname);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * 
     * @param name Searched passenger name
     * @param surname Searched passenger surname
     * @return Passenger object if found <p> <code>null</code> if passenger not found
     */
    public static Passenger getPassenger(String name, String surname) {
        String sql = "SELECT * FROM passengers WHERE name = ? AND surname = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, surname);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String phoneNumber = resultSet.getString("phone_number");

                    return new Passenger(id, name, surname, phoneNumber);
                } else {
                    System.out.println("Passenger not found");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Deletes passenger from database
     * @param id Passenger database id
     */
    public static void deletePassengerFromDatabase(int id) {
        String sql = "DELETE FROM passengers WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Passenger with id: " + id + " deleted successfully");
            } else {
                System.out.println("No passenger with id: " + id + " found in database");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add flight to database
     * @param flight Flight object
     * @return Added flight database id
     */
    public static int addFlightToDatabase(Flight flight) {
        String sqlQuery = "INSERT INTO flights (flight_number, origin_airport, destination_airport, departure_time, estimated_arrival_time, available_seats) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, flight.getFlightNumber());
            preparedStatement.setString(2, flight.getOriginAirport());
            preparedStatement.setString(3, flight.getDestinationAirport());
            preparedStatement.setTimestamp(4, flight.getDepartureTime());
            preparedStatement.setTimestamp(5, flight.getEstimatedArrivalTime());
            preparedStatement.setInt(6, flight.getAvailableSeats());

            preparedStatement.executeUpdate();

            ResultSet lastInsertedID = connection.createStatement().executeQuery("SELECT LAST_INSERT_ID()");
            lastInsertedID.next();
            return lastInsertedID.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Deletes flight from database
     * @param flightNumber IATA flight number
     */
    public static void deleteFlightFromDatabase(String flightNumber) {
        String sql = "DELETE FROM flights WHERE flight_number = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, flightNumber);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Flight " + flightNumber + " deleted successfully");
            } else {
                System.out.println("No flight " + flightNumber + " found in database");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates all flight info in database
     * @param flight Flight object
     */
    public static void updateFlight(Flight flight) {
        String sqlQuery = "UPDATE flights SET origin_airport = ?, destination_airport = ?, departure_time = ?, estimated_arrival_time = ?, available_seats = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, flight.getOriginAirport());
            preparedStatement.setString(2, flight.getDestinationAirport());
            preparedStatement.setTimestamp(3, flight.getDepartureTime());
            preparedStatement.setTimestamp(4, flight.getEstimatedArrivalTime());
            preparedStatement.setInt(5, flight.getAvailableSeats());
            preparedStatement.setInt(6, flight.getDbID());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Updated flight " + flight.getFlightNumber() + " info");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates all passenger info in database
     * @param passenger Passenger object
     */
    public static void updatePassenger(Passenger passenger) {
        String sqlQuery = "UPDATE passengers SET phone_number = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, passenger.getPhoneNumber());
            preparedStatement.setInt(2, passenger.getDbID());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Updated passenger " + passenger.getFullName() + " info");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts passenger info in database
     * @param passenger Passenger object
     * @return Passenger database id
     */
    public static int addPassengerToDatabase(Passenger passenger) {
        String sqlQuery = "INSERT INTO passengers (name, surname, phone_number) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, passenger.getName());
            preparedStatement.setString(2, passenger.getSurname());
            preparedStatement.setString(3, passenger.getPhoneNumber());

            preparedStatement.executeUpdate();

            ResultSet lastInsertedID = connection.createStatement().executeQuery("SELECT LAST_INSERT_ID()");
            lastInsertedID.next();
            return lastInsertedID.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     *
     * @return List of all flights stored in database
     */
    public static List<Flight> getAllFlights() {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT * FROM flights";
        try (PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int flightID = resultSet.getInt("id");
                Map<Passenger, Integer> passengersAndSeats = getPassengersOnFlight(flightID);
                Flight flight = new Flight(
                        flightID,
                        resultSet.getString("flight_number"),
                        resultSet.getString("origin_airport"),
                        resultSet.getString("destination_airport"),
                        resultSet.getTimestamp("departure_time"),
                        resultSet.getTimestamp("estimated_arrival_time"),
                        resultSet.getInt("available_seats"),
                        passengersAndSeats
                );
                flights.add(flight);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flights;
    }

    /**
     *
     * @param r Flight route in format: ORIGIN_AIRPORT-DESTINATION_AIRPORT (e.g. WAW-LAX)
     * @param includeOppositeDirection Should return also flights going in opposite direction, (e.g. WAW-LAX and LAX-WAW)
     * @return List of flights that matches route / routes
     */
    public static List<Flight> getAllFlightsOnRoute(String r, boolean includeOppositeDirection) {
        String route = r.trim().toUpperCase();
        String[] routeAirports = route.split("-");
        String reverseRoute = routeAirports[1] + "-" + routeAirports[0];
        List<Flight> list = getAllFlights();
        if (includeOppositeDirection) {
            list.removeIf(fl -> !fl.getRoute().equals(route.trim().toUpperCase()) && !fl.getRoute().equals(reverseRoute));
        } else {
            list.removeIf(fl -> !fl.getRoute().equals(route.trim().toUpperCase()));
        }
        return list;
    }


    /**
     *
     * @param hours
     * @return List of flights departing in next hours specified in parameters
     */
    public static List<Flight> getFlightsDepartingInNextHours(int hours) {
        List<Flight> list = new ArrayList<>();
        String sql = "SELECT * FROM flights WHERE departure_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL ? HOUR)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, hours);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int flightID = resultSet.getInt("id");
                Map<Passenger, Integer> passengersAndSeats = getPassengersOnFlight(flightID);
                Flight flight = new Flight(
                        flightID,
                        resultSet.getString("flight_number"),
                        resultSet.getString("origin_airport"),
                        resultSet.getString("destination_airport"),
                        resultSet.getTimestamp("departure_time"),
                        resultSet.getTimestamp("estimated_arrival_time"),
                        resultSet.getInt("available_seats"),
                        passengersAndSeats
                );

                list.add(flight);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    public static List<Flight> getFlightsWithAvailableSeats(int minimumSeats) {
        List<Flight> list = new ArrayList<>();
        String sql = "SELECT * FROM flights WHERE available_seats >= ? ORDER BY available_seats DESC";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, minimumSeats);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int flightID = resultSet.getInt("id");
                Map<Passenger, Integer> passengersAndSeats = getPassengersOnFlight(flightID);
                Flight flight = new Flight(
                        flightID,
                        resultSet.getString("flight_number"),
                        resultSet.getString("origin_airport"),
                        resultSet.getString("destination_airport"),
                        resultSet.getTimestamp("departure_time"),
                        resultSet.getTimestamp("estimated_arrival_time"),
                        resultSet.getInt("available_seats"),
                        passengersAndSeats
                );

                list.add(flight);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     *
     * @return List of all passengers stored in database
     */
    public static List<Passenger> getAllPassengers() {
        List<Passenger> passengers = new ArrayList<>();
        String sql = "SELECT * FROM passengers";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Passenger passenger = new Passenger(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("surname"),
                        resultSet.getString("phone_number")
                );
                passengers.add(passenger);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return passengers;
    }

    /**
     *
     * @param passenger Passenger object assigned to flight
     * @param flight Flight object passenger is assigned to
     * @param seatNo Booked seat number
     * @return booking id from database
     */
    public static int addPassengerToFlight(Passenger passenger, Flight flight, int seatNo) {
        //System.out.println(passenger.getDbID() + " " + flight.getDbID());
        String sqlQuery = "INSERT INTO bookings (flight_id, passenger_id, seat_number) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setInt(1, flight.getDbID());
            preparedStatement.setInt(2, passenger.getDbID());
            preparedStatement.setInt(3, seatNo);

            preparedStatement.executeUpdate();

            ResultSet lastInsertedID = connection.createStatement().executeQuery("SELECT LAST_INSERT_ID()");
            lastInsertedID.next();
            return lastInsertedID.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     *
     * @param passenger Passenger object removed from flight
     * @param flight Flight object passenger is removed from
     */
    public static void removePassengerFromFlight(Passenger passenger, Flight flight) {
        String sqlQuery = "DELETE FROM bookings WHERE passenger_id = ? AND flight_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setInt(1, passenger.getDbID());
            preparedStatement.setInt(2, flight.getDbID());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closing database connection
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database disconnected");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
