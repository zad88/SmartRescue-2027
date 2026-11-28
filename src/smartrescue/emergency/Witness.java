package smartrescue.emergency;

import smartrescue.Person;
import smartrescue.data.EmergencyRepository;

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

        if (location == null || location.isBlank()
                || description == null || description.isBlank()) {
            throw new IllegalArgumentException("Location and description are required.");
        }
        if (injuredCount <= 0) {
            throw new IllegalArgumentException("The number of injured people must be positive.");
        }

        Emergency emergency = new Emergency(
                location, description, injuredCount
        );
        emergency.setWitnessId(witnessId);

        EmergencyRepository repository = new EmergencyRepository();
        repository.saveEmergency(emergency);

        return emergency;
    }

    public String provideLocation() {
        return currentLocation;
    }
}
