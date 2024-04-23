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

    public ResultSet executeQuery(String sqlQuery, String ... params) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setString(i+1, params[i]);
            }
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

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

    public static Flight getFlight(String flightNumber) { // TODO get Map<Passneger, Seat>
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

                    return new Flight(id, flightNumber, originAirport, destinationAirport, departureTime, estimatedArrivalTime, availableSeats);
                } else {
                    System.out.println("Flight not found");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static Map<Passenger, Integer> getPassengersOnFlight(int flightID) {
        Map<Passenger, Integer> result = new HashMap<>();
        String sql = "SELECT p.*, b.seat_number FROM passengers p JOIN bookings b on p.id = b.passenger_id WHERE b.flight_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, flightID);
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
     *
     * @param flight Flight object
     * @return Flight database id
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

    public static List<Flight> getAllFlights() { //TODO Change to use getFlight() ?
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT * FROM flights";
        try (PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Flight flight = new Flight(
                        resultSet.getInt("id"),
                        resultSet.getString("flight_number"),
                        resultSet.getString("origin_airport"),
                        resultSet.getString("destination_airport"),
                        resultSet.getTimestamp("departure_time"),
                        resultSet.getTimestamp("estimated_arrival_time"),
                        resultSet.getInt("available_seats")
                );
                flights.add(flight);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flights;
    }

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

    public static void removePassengerFromFlight(Passenger passenger, Flight flight) {

    }

    public void close() {
        if (this.connection != null) {
            try {
                this.connection.close();
                System.out.println("Database disconnected");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
