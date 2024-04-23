import javax.xml.crypto.Data;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Database db = new Database();

        System.out.println("===== CREATING NEW FLIGHTS =====");

        // Creating new flights and inserting into database
        Flight f0 = new Flight("LO777", "WAW", "VNO", Timestamp.valueOf("2022-04-14 12:30:00"), Timestamp.valueOf("2022-04-14 13:50:00"), 100);
        Flight f1 = new Flight("LO1", "WAW", "ORD", Timestamp.valueOf("2023-05-01 15:30:00"), Timestamp.valueOf("2023-05-01 18:45:00"), 150);
        Flight f2 = new Flight("BA456", "LHR", "JFK", Timestamp.valueOf("2023-06-15 10:00:00"), Timestamp.valueOf("2023-06-15 14:25:00"), 200);
        Flight f3 = new Flight("AF789", "WAW", "WMM", Timestamp.valueOf("2023-07-20 09:20:00"), Timestamp.valueOf("2023-07-20 12:35:00"), 180);
        Flight f4 = new Flight("UA101", "ORD", "LAS", Timestamp.valueOf("2023-08-05 16:10:00"), Timestamp.valueOf("2023-08-05 18:30:00"), 165);
        Flight f5 = new Flight("DL202", "ATL", "MIA", Timestamp.valueOf("2023-09-01 13:45:00"), Timestamp.valueOf("2023-09-01 15:50:00"), 175);
        Flight f6 = new Flight("EK303", "DXB", "LHR", Timestamp.valueOf("2023-10-15 23:00:00"), Timestamp.valueOf("2023-10-16 03:15:00"), 250);
        Flight f7 = new Flight("QR404", "WMM", "WAW", Timestamp.valueOf("2023-11-10 01:30:00"), Timestamp.valueOf("2023-11-10 06:45:00"), 215);
        Flight f8 = new Flight("NH505", "NRT", "SYD", Timestamp.valueOf("2023-12-05 19:00:00"), Timestamp.valueOf("2023-12-06 08:20:00"), 190);
        Flight f9 = new Flight("LH606", "FRA", "BOM", Timestamp.valueOf("2024-04-25 22:40:00"), Timestamp.valueOf("2024-04-26 10:50:00"), 160);
        Flight f10 = new Flight("SQ707", "SIN", "DXB", Timestamp.valueOf("2024-02-28 14:15:00"), Timestamp.valueOf("2024-02-28 17:45:00"), 230);

        System.out.println("===== CREATING NEW PASSENGERS =====");

        // Creating new passengers and inserting into database
        Passenger p1 = new Passenger("James", "Smith", "+12025550101");
        Passenger p2 = new Passenger("Maria", "Garcia", "+13475550342");
        Passenger p3 = new Passenger("John", "Johnson", "+19253329988");
        Passenger p4 = new Passenger("Emma", "Martinez", "+18175551234");
        Passenger p5 = new Passenger("William", "Brown", "+14159998877");
        Passenger p6 = new Passenger("Olivia", "Davis", "+15852226644");
        Passenger p7 = new Passenger("Michael", "Miller", "+17013334455");
        Passenger p8 = new Passenger("Sophia", "Wilson", "+13215567890");
        Passenger p9 = new Passenger("Benjamin", "Moore", "+19876543210");
        Passenger p10 = new Passenger("Isabella", "Taylor", "+16047788999");


        System.out.println("===== ASSIGNING PASSENGERS TO FLIGHTS =====");
        // Assigning passengers to flights

        f0.assignPassenger(p10, 48);
        f0.assignPassenger(p5, 78);
        f0.assignPassenger(p6, 29);
        f0.assignPassenger(p1, 99);
        f0.assignPassenger(p2, 100);


        f1.assignPassenger(p6, 48);
        f1.assignPassenger(p4, 78);
        f1.assignPassenger(p10, 29);
        f1.assignPassenger(p1, 99);
        f1.assignPassenger(p9, 100);

        f2.assignPassenger(p3, 88);
        f2.assignPassenger(p5, 77);
        f2.assignPassenger(p7, 44);
        f2.assignPassenger(p1, 55);
        f2.assignPassenger(p9, 33);

        System.out.println("===== TESTING AVAILABLE SEATS =====");

        System.out.println(f1.getFlightNumber() + ", seats available: " + f1.getAvailableSeats());
        System.out.println(f2.getFlightNumber() + ", seats available: " + f2.getAvailableSeats());

        System.out.println("===== DOES FLIGHT EXISTS =====");

        System.out.println(Database.doesFlightExists("LO948"));

        System.out.println("===== OBTAINING FLIGHTS BY GIVEN DATA =====");

        List<Flight> flights = Database.getFlightsDepartingInNextHours(72);
        List<Passenger> passengers = Database.getAllPassengers();
        List<Flight> flightsWAW_WMM = Database.getAllFlightsOnRoute("WAW-WMM", false);

        for (Flight f : flights) {
            System.out.println(f.getFlightNumber());
        }

        System.out.println("===== OBTAINING ALL PASSENGERS FROM DATABASE =====");

        for (Passenger p : passengers) {
            System.out.println(p.getFullName());
        }

        System.out.println("===== OBTAINING ALL PASSENGER FLIGHTS=====");

        Flight test = Database.getFlight("LO777");
        Passenger passengerTest = Database.getPassenger("Isabella", "Taylor");
        Map<Flight, Integer> passengerFlightsTest = Database.getAllPassengerFlights(passengerTest);

        //test = Database.getFlight("LO123");

        for (Map.Entry<Flight, Integer> entry : passengerFlightsTest.entrySet()) {
            System.out.println(entry.getKey().getFlightNumber() + ", seat: " + entry.getValue());
        }

        //Database.getPassengersOnFlight(test.getDbID());

        System.out.println("===== UPDATING FLIGHT DATA =====");

        test.setDestinationAirport("LAX");

        //Passenger p1 = Database.getPassenger("Anna", "Janowska");
        //p1.setPhoneNumber("+48888777999");

        System.out.println("===== OBTAINING FLIGHT DURATION =====");

        System.out.println(test.getFlightNumber() + " duration: " + test.getFlightDuration()[0] + " hours, " + test.getFlightDuration()[1] + " minutes");
    }
}