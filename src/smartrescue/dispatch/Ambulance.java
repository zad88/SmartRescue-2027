package smartrescue.dispatch;

import smartrescue.emergency.Emergency;

public class Ambulance {

    private int ambulanceId;
    private String licensePlate;
    private AmbulanceStatus status;
    private String currentLocation;

    public Ambulance(int ambulanceId, String licensePlate,
                     AmbulanceStatus status, String currentLocation) {

        this.ambulanceId = ambulanceId;
        this.licensePlate = licensePlate;
        this.status = status;
        this.currentLocation = currentLocation;
    }

    public boolean checkAvailability() {
        return status == AmbulanceStatus.AVAILABLE;
    }

    public void assignToEmergency(Emergency emergency) {
        emergency.assignAmbulance(this);
        this.status = AmbulanceStatus.DISPATCHED;
    }

    public void release() {
        this.status = AmbulanceStatus.AVAILABLE;
    }

    // Getters for repository and GUI access

    public int getAmbulanceId() {
        return ambulanceId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public AmbulanceStatus getStatus() {
        return status;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }
}