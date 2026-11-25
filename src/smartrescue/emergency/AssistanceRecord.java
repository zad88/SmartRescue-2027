package smartrescue.emergency;

import java.time.LocalDateTime;

public class AssistanceRecord {

    // Attributes

    private int recordId;
    private String description;
    private LocalDateTime recordedAt;
    private int patientsTreated;

    // Constructor

    public AssistanceRecord(int recordId, String description, int patientsTreated) {
        this.recordId = recordId;
        this.description = description;
        this.patientsTreated = patientsTreated;
        this.recordedAt = null;
    }

    // Record assistance

    public void recordAssistance() {
        this.recordedAt = LocalDateTime.now();
    }

}