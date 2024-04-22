import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        Database db = new Database();

        //Flight fl = new Flight("LO948", "WAW", "WMM", Timestamp.valueOf("2022-04-14 12:30:45"), Timestamp.valueOf("2027-04-14 12:30:45"), 100);

        Passenger pa = new Passenger("Anna", "Stankiewicz", "+48694466866");

        //fl.assignPassenger(pa);

        //System.out.println(Database.doesFlightExists("LO948"));

        //Database db = new Database();
        //db.executeInsert("INSERT INTO ...", "adad", "asdfas", "4894848");

        //System.out.println(pa.isPhoneNumberCorrect("+48694466866"));


        Database.getAllFlights();

        //TODO Flights class
    }
}