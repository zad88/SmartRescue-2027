package smartrescue.emergency;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import smartrescue.dispatch.Ambulance;
import smartrescue.transfer.Patient;

public class Emergency {

    private String incidentId;
    private String location;
    private String description;
    private LocalDateTime reportedAt;
    private String type;
    private Severity severity;
    private int patientCount;
    private EmergencyStatus status;
    private Ambulance ambulance;
    private List<Patient> patients;

    private static int nextIncidentNumber = 1;

    // Constructor used when the Witness reports an emergency

    public Emergency(String location, String description, int patientCount) {
        this(location, description, null, null, patientCount);
    }

    // Constructor with classification

    public Emergency(String location, String description, String type,
                     Severity severity, int patientCount) {

        this.incidentId = generateIncidentId();
        this.location = location;
        this.description = description;
        this.reportedAt = LocalDateTime.now();
        this.type = type;
        this.severity = severity;
        this.patientCount = patientCount;
        this.status = EmergencyStatus.REPORTED;
        this.ambulance = null;
        this.patients = new ArrayList<>();
    }

    public String generateIncidentId() {
        String generatedId = String.format("INC-%03d", nextIncidentNumber);
        nextIncidentNumber++;
        return generatedId;
    }

    public void changeStatus(EmergencyStatus newStatus) {
        this.status = newStatus;
    }

    public void assignAmbulance(Ambulance ambulance) {
        this.ambulance = ambulance;
    }

    public String getDetails() {
        return "Incident ID: " + incidentId +
               "\nLocation: " + location +
               "\nDescription: " + description +
               "\nReported At: " + reportedAt +
               "\nType: " + type +
               "\nSeverity: " + severity +
               "\nPatient Count: " + patientCount +
               "\nStatus: " + status;
    }

    public int getPatientCount() {
        return patientCount;
    }

    public void addPatient(Patient patient) {
        patients.add(patient);
    }

    public List<Patient> getPatients() {
        return new ArrayList<>(patients);
    }

    // Allows the Operator to classify the emergency

    public void setClassification(String type, Severity severity) {
        this.type = type;
        this.severity = severity;
    }

}