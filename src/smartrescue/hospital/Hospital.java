package smartrescue.hospital;

import java.util.ArrayList;
import java.util.List;

public class Hospital {

    private int hospitalId;
    private String name;
    private String location;
    private int availableBeds;
    private int emergencyCapacity;
    private List<String> medicalSpecialties;

    public Hospital(int hospitalId, String name, String location,
                    int availableBeds, int emergencyCapacity,
                    List<String> medicalSpecialties) {

        this.hospitalId = hospitalId;
        this.name = name;
        this.location = location;
        this.availableBeds = availableBeds;
        this.emergencyCapacity = emergencyCapacity;
        this.medicalSpecialties = new ArrayList<>(medicalSpecialties);
    }

    public boolean hasCapacityFor(String specialty) {

        return availableBeds > 0
                && emergencyCapacity > 0
                && medicalSpecialties.contains(specialty);
    }

    public void updateAvailability(int beds, int capacity,
                                   List<String> specialties) {

        this.availableBeds = beds;
        this.emergencyCapacity = capacity;
        this.medicalSpecialties = new ArrayList<>(specialties);
    }

    public void receivePatients(int count) {
        this.availableBeds -= count;
        this.emergencyCapacity -= count;
    }

    // Getters needed by AmbulanceTeam

    public int getAvailableBeds() {
        return availableBeds;
    }

    public int getEmergencyCapacity() {
        return emergencyCapacity;
    }

    public List<String> getMedicalSpecialties() {
        return new ArrayList<>(medicalSpecialties);
    }

}