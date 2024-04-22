import javax.xml.crypto.Data;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Database db = new Database();

        Flight fl = new Flight("LO777", "WAW", "WMM", Timestamp.valueOf("2022-04-14 12:30:45"), Timestamp.valueOf("2027-04-14 12:30:45"), 100);

        Passenger pa = new Passenger("Anna", "Muczynska", "+48694466866");

        //System.out.println(fl.assignPassenger(pa, 3));

        //System.out.println(Database.doesFlightExists("LO948"));

        //Database db = new Database();
        //db.executeInsert("INSERT INTO ...", "adad", "asdfas", "4894848");

        //System.out.println(pa.isPhoneNumberCorrect("+48694466866"));


        List<Flight> flights = Database.getAllFlights();

        Flight test = Database.getFlight("LO777");
        //test.delete();

        //System.out.println(test.getFlightDuration()[0] + " " + test.getFlightDuration()[1]);

        //TODO Flights class
    }
}