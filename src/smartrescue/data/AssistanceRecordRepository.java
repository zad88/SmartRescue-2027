package smartrescue.data;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import smartrescue.emergency.AssistanceRecord;

public class AssistanceRecordRepository {

    private static final Path FILE =
            Path.of("data", "assistance_records.csv");

    private static final String HEADER =
            "recordId,emergencyId,description,recordedAt,patientsTreated,teamId";
    private static final String LEGACY_HEADER =
            "recordId,emergencyId,description,recordedAt,patientsTreated";

    public void saveAssistanceRecord(AssistanceRecord record,
                                     String emergencyId) {
        if (record == null || emergencyId == null || emergencyId.isBlank()) {
            throw new IllegalArgumentException(
                "Assistance record and emergency ID are required."
            );
        }

        CsvSupport.ensureColumns(FILE, HEADER, LEGACY_HEADER);
        String row =
            record.getRecordId() + "," +
            CsvSupport.escape(emergencyId) + "," +
            CsvSupport.escape(record.getDescription()) + "," +
            CsvSupport.escape(record.getRecordedAt() == null
                ? null : record.getRecordedAt().toString()) + "," +
            record.getPatientsTreated() + "," +
            CsvSupport.escape(record.getTeamId() == null
                ? null : record.getTeamId().toString());

        CsvSupport.upsert(
            FILE, HEADER, 0, String.valueOf(record.getRecordId()), row
        );
    }

    public List<AssistanceRecord> loadByEmergencyId(String emergencyId) {
        CsvSupport.ensureColumns(FILE, HEADER, LEGACY_HEADER);
        List<AssistanceRecord> records = new ArrayList<>();
        List<String> lines = CsvSupport.readLines(FILE, HEADER);

        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) continue;

            List<String> fields = CsvSupport.parse(lines.get(i));
            if (fields.size() != 6) {
                throw new IllegalStateException(
                    "Invalid assistance CSV record: " + lines.get(i)
                );
            }

            if (fields.get(1).equals(emergencyId)) {
                LocalDateTime recordedAt = fields.get(3).isBlank()
                    ? null : LocalDateTime.parse(fields.get(3));

                records.add(new AssistanceRecord(
                    Integer.parseInt(fields.get(0)),
                    fields.get(2),
                    recordedAt,
                    Integer.parseInt(fields.get(4)),
                    fields.get(5).isBlank() ? null : Integer.parseInt(fields.get(5))
                ));
            }
        }

        return records;
    }
}
