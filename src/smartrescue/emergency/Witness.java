package smartrescue.emergency;

import smartrescue.Person;

public class Witness extends Person {

    private int witnessId;
    private String phoneNumber;
    private String currentLocation;

    public Witness(int witnessId, String fullName,
                   String phoneNumber, String currentLocation) {

        super(fullName);

        this.witnessId = witnessId;
        this.phoneNumber = phoneNumber;
        this.currentLocation = currentLocation;
    }

    public Emergency reportEmergency(String location,
                                     String description,
                                     int injuredCount) {

        return new Emergency(location, description, injuredCount);
    }

    public String provideLocation() {
        return currentLocation;
    }
}