package smartrescue.transfer;

import smartrescue.data.PatientRepository;

public class Patient {

    private int patientId;
    private String name;
    private int age;
    private String condition;
    private String treatmentStatus;
    private String emergencyId;

    public Patient(int patientId, String name, int age,
                   String condition, String treatmentStatus) {
        this(patientId, name, age, condition, treatmentStatus, null);
    }

    public Patient(int patientId, String name, int age,
                   String condition, String treatmentStatus,
                   String emergencyId) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.condition = condition;
        this.treatmentStatus = treatmentStatus;
        this.emergencyId = emergencyId;
    }

    public void updateTreatmentStatus(String newStatus) {
        this.treatmentStatus = newStatus;

        if (emergencyId != null) {
            new PatientRepository().savePatient(this, emergencyId);
        }
    }

    public int getPatientId() { return patientId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getCondition() { return condition; }
    public String getTreatmentStatus() { return treatmentStatus; }
    public String getEmergencyId() { return emergencyId; }

    public void setEmergencyId(String emergencyId) {
        this.emergencyId = emergencyId;
    }
}
