package smartrescue.dispatch;

import smartrescue.emergency.Emergency;

public class Ambulance {

    // Attributes

    private int ambulanceId;
    private String licensePlate;
    private AmbulanceStatus status;
    private String currentLocation;

    // Constructor

    public Ambulance(int ambulanceId, String licensePlate,
                     AmbulanceStatus status, String currentLocation) {

        this.ambulanceId = ambulanceId;
        this.licensePlate = licensePlate;
        this.status = status;
        this.currentLocation = currentLocation;
    }

    // Check ambulance availability

    public boolean checkAvailability() {
        return status == AmbulanceStatus.AVAILABLE;
    }

    // Assign ambulance to emergency

    public void assignToEmergency(Emergency emergency) {
        emergency.assignAmbulance(this);
        this.status = AmbulanceStatus.DISPATCHED;
    }

    // Release ambulance

    public void release() {
        this.status = AmbulanceStatus.AVAILABLE;
    }

}