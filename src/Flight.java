import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class Flight {
    private String flightNumber;
    private String originAirport;
    private String destinationAirport;
    private Timestamp departureTime;    // ISO 8601 YYYY-MM-DDThh:mm:ss
    private Timestamp estimatedArrivalTime;
    private int availableSeats;
    private int dbID;
    private List<Passenger> passengers = new ArrayList<>();

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

    private boolean isFlightNumberCorrect(String flightNumber) {
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

    public long getFlightDuration() {
        //TODO
        return 0;
    }

    public void assignPassenger(Passenger passenger) {
        if (this.availableSeats == 0) System.out.println("No seats available, can't assign new passenger");
        if (!this.passengers.contains(passenger)) {
            this.passengers.add(passenger);
            this.availableSeats--;
            System.out.println("Passenger " + passenger.getName() + " " + passenger.getSurname() + " assigned to flight " + this.flightNumber);
        } else System.out.println("Passenger " + passenger.getName() + " " + passenger.getSurname() + " already assigned to flight");
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
}
