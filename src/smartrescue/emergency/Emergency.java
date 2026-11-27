package smartrescue.emergency;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import smartrescue.data.IdGenerator;
import smartrescue.dispatch.Ambulance;
import smartrescue.transfer.Patient;
import smartrescue.transfer.HospitalTransfer;

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
    private List<AssistanceRecord> assistanceRecords;
    private List<HospitalTransfer> hospitalTransfers;

    public Emergency(String location, String description, int patientCount) {
        this(location, description, null, null, patientCount);
    }

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
        this.assistanceRecords = new ArrayList<>();
        this.hospitalTransfers = new ArrayList<>();
    }

    public String generateIncidentId() {
        return IdGenerator.nextIncidentId();
    }

    public void changeStatus(EmergencyStatus newStatus) {

        if (newStatus == null) {
            return;
        }

        boolean validTransition =
                (status == EmergencyStatus.REPORTED
                        && newStatus == EmergencyStatus.VERIFIED)
                ||
                (status == EmergencyStatus.VERIFIED
                        && newStatus == EmergencyStatus.RESPONDING)
                ||
                (status == EmergencyStatus.RESPONDING
                        && newStatus == EmergencyStatus.ON_SCENE)
                ||
                (status == EmergencyStatus.ON_SCENE
                        && newStatus == EmergencyStatus.RESOLVED)
                ||
                (status == EmergencyStatus.RESOLVED
                        && newStatus == EmergencyStatus.CLOSED);

        if (validTransition) {
            this.status = newStatus;
        }
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

    public void addAssistanceRecord(AssistanceRecord record) {
        assistanceRecords.add(record);
    }

    public List<AssistanceRecord> getAssistanceRecords() {
        return new ArrayList<>(assistanceRecords);
    }

    public void addHospitalTransfer(HospitalTransfer transfer) {
        hospitalTransfers.add(transfer);
    }

    public List<HospitalTransfer> getHospitalTransfers() {
        return new ArrayList<>(hospitalTransfers);
    }

    public void setClassification(String type, Severity severity) {
        this.type = type;
        this.severity = severity;
    }

    public EmergencyStatus getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public Severity getSeverity() {
        return severity;
    }
}