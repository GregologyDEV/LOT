import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
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
    private List<Passenger> passengers = new ArrayList<>();
    private List<Integer> availableSeatsNumbers;

    /**
     *
     * @param flightNo IATA Flight number (2-letter airline code and 1 to 4 digits)
     * @param originAirp IATA airport of origin code
     * @param destinationAirp IATA airport of destination code
     * @param depTime
     * @param arrTime
     * @param maxNoOfSeats
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

    public Flight(int id, String flightNo, String originAirp, String destinationAirp, Timestamp depTime, Timestamp arrTime, int maxNoOfSeats) {
        this.dbID = id;
        this.originAirport = originAirp.trim().toUpperCase();
        this.destinationAirport = destinationAirp.trim().toUpperCase();
        this.flightNumber = flightNo.toUpperCase();
        this.departureTime = depTime;
        this.estimatedArrivalTime = arrTime;
        this.availableSeats = maxNoOfSeats;
    }

    static boolean isFlightNumberCorrect(String flightNumber) {
        String requiredFormatRegex = "[a-zA-Z]{2}[0-9]{1,4}";

        return flightNumber.matches(requiredFormatRegex);
    }

    public String getFlightNumber() {
        return this.flightNumber;
    }

    public void setDepartureTime(Timestamp departureTime) {
        this.departureTime = departureTime;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public String getFlightPath() {
        return this.originAirport + "-" + this.destinationAirport;
    }

    public void setFlightNumber(String flightNumber) {
        if (isFlightNumberCorrect(flightNumber.toUpperCase())) throw new IllegalArgumentException("Flight number is incorrect");
        this.flightNumber = flightNumber;
    }


    /**
     * Calculates estimated flight time based on previously assigned departure and est. arrival time
     * @return long array: <p> [0] - hours <p> [1] - minutes
     */
    public long[] getFlightDuration() {
        long miliseconds = estimatedArrivalTime.getTime() - departureTime.getTime();
        long hours = TimeUnit.MILLISECONDS.toHours(miliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(miliseconds) % 60;
        return new long[]{hours, minutes};
    }

    /**
     *
     * @param passenger Passenger object
     * @param seatNo Seat number
     * @return booking id if successfully assigned <p> -1 if can not assign
     */
    public int assignPassenger(Passenger passenger, int seatNo) {
        if (this.availableSeats == 0) {
            System.out.println("No seats available, can't assign new passenger");
            return -1;
        }
        if (!this.availableSeatsNumbers.contains(seatNo)) {
            System.out.println("Selected seat is unavailable");
            return -1;
        }
        if (!this.passengers.contains(passenger)) {
            this.passengers.add(passenger);
            this.availableSeats--;
            this.availableSeatsNumbers.remove(seatNo);
            System.out.println("Passenger " + passenger.getName() + " " + passenger.getSurname() + " assigned to flight " + this.flightNumber);
            return Database.addPassengerToFlight(passenger, this, seatNo);
        } else {
            System.out.println("Passenger " + passenger.getName() + " " + passenger.getSurname() + " already assigned to flight");
            return -1;
        }
    }

    public String getOriginAirport() {
        return originAirport;
    }

    public String getDestinationAirport() {
        return destinationAirport;
    }

    public Timestamp getDepartureTime() {
        return departureTime;
    }

    public Timestamp getEstimatedArrivalTime() {
        return estimatedArrivalTime;
    }

    public int getDbID() {
        return dbID;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void delete() {
        Database.deleteFlightFromDatabase(this.flightNumber);
    }
}
