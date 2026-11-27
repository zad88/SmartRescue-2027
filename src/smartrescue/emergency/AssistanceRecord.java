package smartrescue.emergency;

import java.time.LocalDateTime;

public class AssistanceRecord {

    private int recordId;
    private String description;
    private LocalDateTime recordedAt;
    private int patientsTreated;

    public AssistanceRecord(int recordId, String description,
                            int patientsTreated) {
        this(recordId, description, null, patientsTreated);
    }

    public AssistanceRecord(int recordId, String description,
                            LocalDateTime recordedAt, int patientsTreated) {
        this.recordId = recordId;
        this.description = description;
        this.recordedAt = recordedAt;
        this.patientsTreated = patientsTreated;
    }

    public void recordAssistance() {
        this.recordedAt = LocalDateTime.now();
    }

    public int getRecordId() { return recordId; }
    public String getDescription() { return description; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public int getPatientsTreated() { return patientsTreated; }
}
