package smartrescue.transfer;

public class Patient {

    // Attributes

    private int patientId;
    private String name;
    private int age;
    private String condition;
    private String treatmentStatus;

    // Constructor

    public Patient(int patientId, String name, int age,
                   String condition, String treatmentStatus) {

        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.condition = condition;
        this.treatmentStatus = treatmentStatus;
    }

    // Update treatment status

    public void updateTreatmentStatus(String newStatus) {
        this.treatmentStatus = newStatus;
    }

}