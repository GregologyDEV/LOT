public class Passenger {
    private String name;
    private String surname;
    private String phoneNumber;
    private int dbID;

    public Passenger(String name, String surname, String phoneNumber) throws IllegalArgumentException {
        if (!isPhoneNumberCorrect(phoneNumber)) throw new IllegalArgumentException("Incorrect phone number");
        if (Database.doesPassengerExists(name, surname)) throw new IllegalArgumentException("Such passenger already exists in database");
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;

        this.dbID = Database.addPassengerToDatabase(this);

        //System.out.println(this.dbID);
    }

    public Passenger(int id, String name, String surname, String phoneNumber) {
        this.dbID = id;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
    }

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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setPhoneNumber(String phoneNumber) throws IllegalArgumentException {
        if (isPhoneNumberCorrect(phoneNumber)) {
            this.phoneNumber = phoneNumber;
        } else throw new IllegalArgumentException("Incorrect phone number");
    }

    public int getDbID() {
        return dbID;
    }
}
