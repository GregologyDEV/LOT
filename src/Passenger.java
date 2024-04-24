public class Passenger {
    private final String name;
    private final String surname;
    private String phoneNumber;
    private final int dbID;

    /**
     *
     * @param name Passenger name
     * @param surname Passenger surname
     * @param phoneNumber Passenger phone number
     * @throws IllegalArgumentException
     */
    public Passenger(String name, String surname, String phoneNumber) throws IllegalArgumentException {
        if (!isPhoneNumberCorrect(phoneNumber)) throw new IllegalArgumentException("Incorrect phone number");
        if (Database.doesPassengerExists(name, surname)) throw new IllegalArgumentException("Such passenger already exists in database");
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;

        this.dbID = Database.addPassengerToDatabase(this);

        //System.out.println(this.dbID);
    }

    /**
     * Only used to create passenger object pulled from database
     */
    public Passenger(int id, String name, String surname, String phoneNumber) {
        this.dbID = id;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
    }

    /**
     *
     * @param phoneNumber
     * @return <code>true</code> if phone number is in correct format <p> <code>false</code> otherwise
     */
    private boolean isPhoneNumberCorrect(String phoneNumber) {
        String phoneNumberRegex = "^[+]?[(]?[0-9]{3}[)]?[-\\s.]?[0-9]{3}[-\\s.]?[0-9]{4,6}$";

        return phoneNumber.matches(phoneNumberRegex);
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getFullName() { return name + " " + surname; }

    public String getPhoneNumber() {
        return phoneNumber;
    }


    public void setPhoneNumber(String phoneNumber) throws IllegalArgumentException {
        if (isPhoneNumberCorrect(phoneNumber)) {
            this.phoneNumber = phoneNumber;
            Database.updatePassenger(this);
        } else throw new IllegalArgumentException("Incorrect phone number");
    }

    /**
     *
     * @return Database id of passenger
     */
    public int getDbID() {
        return dbID;
    }

    /**
     * Deletes passenger from database
     */
    public void delete() {
        Database.deletePassengerFromDatabase(this.dbID);
    }
}
