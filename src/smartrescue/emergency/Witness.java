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

        Emergency emergency = new Emergency(
                location, description, injuredCount
        );

        EmergencyRepository repository = new EmergencyRepository();
        repository.saveEmergency(emergency);

        return emergency;
    }

    public String provideLocation() {
        return currentLocation;
    }
}
