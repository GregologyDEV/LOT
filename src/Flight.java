import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Flight {
    private String flightNumber;
    private String originAirport;
    private String destinationAirport;
    private Timestamp departureTime;    // ISO 8601 YYYY-MM-DDThh:mm:ss
    private Timestamp estimatedArrivalTime;
    private int availableSeats;
    private int dbID;
    private Map<Passenger, Integer> passengersAndSeats = new HashMap<>();
    private List<Integer> availableSeatsNumbers;

    /**
     *
     * @param flightNo IATA Flight number (2-letter airline code and 1 to 4 digits)
     * @param originAirp IATA airport of origin code
     * @param destinationAirp IATA airport of destination code
     * @param depTime Timestamp in format YYYY-MM-DD hh:mm:ss
     * @param arrTime Timestamp in format YYYY-MM-DD hh:mm:ss
     * @param maxNoOfSeats Maximum number of seats
     * @throws IllegalArgumentException
     */
    public Flight(String flightNo, String originAirp, String destinationAirp, Timestamp depTime, Timestamp arrTime, int maxNoOfSeats) throws IllegalArgumentException {
        if (!isFlightNumberCorrect(flightNo.toUpperCase())) throw new IllegalArgumentException("Flight number is incorrect");
        if (Database.doesFlightExists(flightNo)) throw new IllegalArgumentException("Flight with this number already exists in database");
        if (maxNoOfSeats <= 0) throw new IllegalArgumentException("Incorrect number of seats");
        if (depTime.after(arrTime) || depTime.equals(arrTime)) throw new IllegalArgumentException("Incorrect departure or arrival datetime");

        this.originAirport = originAirp.trim().toUpperCase();
        this.destinationAirport = destinationAirp.trim().toUpperCase();
        this.flightNumber = flightNo.toUpperCase();
        this.departureTime = depTime;
        this.estimatedArrivalTime = arrTime;
        this.availableSeats = maxNoOfSeats;
        this.availableSeatsNumbers = IntStream.range(1, maxNoOfSeats).boxed().collect(Collectors.toList());

        this.dbID = Database.addFlightToDatabase(this);

        System.out.println("Flight " + this.flightNumber + " added to database");
    }

    /**
     * Only used to create Flight object pulled from database
     * @param id
     * @param flightNo
     * @param originAirp
     * @param destinationAirp
     * @param depTime
     * @param arrTime
     * @param maxNoOfSeats
     * @param passengersAndSeats
     */
    public Flight(int id, String flightNo, String originAirp, String destinationAirp, Timestamp depTime, Timestamp arrTime, int maxNoOfSeats, Map<Passenger, Integer> passengersAndSeats) {
        this.dbID = id;
        this.originAirport = originAirp.trim().toUpperCase();
        this.destinationAirport = destinationAirp.trim().toUpperCase();
        this.flightNumber = flightNo.toUpperCase();
        this.departureTime = depTime;
        this.estimatedArrivalTime = arrTime;
        this.availableSeats = maxNoOfSeats;
        this.passengersAndSeats = passengersAndSeats;
    }

    /**
     * Returns if given string matches IATA flight number (2-letter airline code and 1 to 4 digits)
     * @param flightNumber
     * @return <code>true</code> if string is in IATA format <p> <code>false</code> otherwise
     */
    static boolean isFlightNumberCorrect(String flightNumber) {
        String requiredFormatRegex = "[a-zA-Z]{2}[0-9]{1,4}";

        return flightNumber.matches(requiredFormatRegex);
    }

    public String getFlightNumber() {
        return this.flightNumber;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public String getFlightPath() {
        return this.originAirport + "-" + this.destinationAirport;
    }


    /**
     * Calculates estimated flight time based on previously assigned departure and est. arrival time
     * @return <code>long</code> array: <p> [0] - hours <p> [1] - minutes
     */
    public long[] getFlightDuration() {
        long miliseconds = estimatedArrivalTime.getTime() - departureTime.getTime();
        long hours = TimeUnit.MILLISECONDS.toHours(miliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(miliseconds) % 60;
        return new long[]{hours, minutes};
    }

    /**
     * Assigns given passenger to flight
     * @param passenger Passenger object
     * @param seatNo Seat number
     * @return booking id if successfully assigned <p> -1 if can not assign
     */
    public int assignPassenger(Passenger passenger, int seatNo) {
        if (this.availableSeats == 0) {
            System.out.println("No seats available, can't assign new passenger");
            return -1;
        }
        if (seatNo < 0 || seatNo > this.availableSeats) {
            System.out.println("Incorrect seat number");
            return -1;
        }
        if (this.passengersAndSeats.containsValue(seatNo)) {
            System.out.println("Selected seat is unavailable");
            return -1;
        }
        if (!this.passengersAndSeats.containsKey(passenger)) {
            this.passengersAndSeats.put(passenger, seatNo);
            this.availableSeats--;
            this.availableSeatsNumbers.remove(seatNo); // No longer needed?
            System.out.println("Passenger " + passenger.getFullName() + " assigned to flight " + this.flightNumber);
            Database.updateFlight(this);
            return Database.addPassengerToFlight(passenger, this, seatNo);
        } else {
            System.out.println("Passenger " + passenger.getFullName() + " already assigned to flight");
            return -1;
        }
    }

    /**
     * Removes given passenger from flight
     * @param passenger Passenger object
     */
    public void removePassenger(Passenger passenger) {
        // Due to fact that Passenger passenger may be the same, but different object it's necessary to find passenger in map using for each loop
        for (Map.Entry<Passenger, Integer> entry : this.passengersAndSeats.entrySet()) {
            if (entry.getKey().getFullName().equals(passenger.getFullName())) {
                this.passengersAndSeats.remove(entry.getKey());
                this.availableSeats++;
                Database.removePassengerFromFlight(passenger, this);
                return;
            }
            //System.out.println(entry.getKey().getFullName() + ", seat: " + entry.getValue());
        }

        System.out.println("Passenger " + passenger.getFullName() + " is not assigned to flight");

    }

    public String getOriginAirport() {
        return originAirport;
    }

    public String getDestinationAirport() {
        return destinationAirport;
    }

    /**
     *
     * @return Flight route in format: ORIGIN_AIRPORT-DESTINATION_AIRPORT (e.g WAW-LAX)
     */
    public String getRoute() {
        return this.originAirport + "-" + this.destinationAirport;
    }

    public Timestamp getDepartureTime() {
        return departureTime;
    }

    public Timestamp getEstimatedArrivalTime() {
        return estimatedArrivalTime;
    }

    public Map<Passenger, Integer> getPassengersAndSeats() {
        return passengersAndSeats;
    }

    public int getDbID() {
        return dbID;
    }

    /**
     *
     * @return List of Passengers assigned to this flight
     */
    public List<Passenger> getPassengersList() {
        return new ArrayList<>(passengersAndSeats.keySet());
    }

    /**
     *
      * @return List of occupied seats on flight
     */
    public List<Integer> getOccupiedSeatsList() {
        return new ArrayList<>(passengersAndSeats.values());
    }

    /**
     * Deletes flight from database
     */
    public void delete() {
        Database.deleteFlightFromDatabase(this.flightNumber);
    }

    public void setOriginAirport(String originAirport) {
        this.originAirport = originAirport;
        Database.updateFlight(this);
    }

    public void setDestinationAirport(String destinationAirport) {
        this.destinationAirport = destinationAirport;
        Database.updateFlight(this);
    }

    public void setDepartureTime(Timestamp departureTime) {
        this.departureTime = departureTime;
        Database.updateFlight(this);
    }

    public void setEstimatedArrivalTime(Timestamp estimatedArrivalTime) {
        this.estimatedArrivalTime = estimatedArrivalTime;
        Database.updateFlight(this);
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
        Database.updateFlight(this);
    }
}
