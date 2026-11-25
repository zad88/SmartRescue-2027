package smartrescue.emergency;

public class Witness {

    private int witnessId;
    private String fullName;
    private String phoneNumber;
    private String currentLocation;

    public Witness(int witnessId, String fullName,
                   String phoneNumber, String currentLocation) {

        this.witnessId = witnessId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.currentLocation = currentLocation;
    }

    // Report an emergency

    public Emergency reportEmergency(String location,
                                     String description,
                                     int injuredCount) {

        return new Emergency(location, description, injuredCount);
    }

    // Provide the witness's current location

    public String provideLocation() {
        return currentLocation;
    }

}