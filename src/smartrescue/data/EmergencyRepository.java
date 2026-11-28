package smartrescue.data;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import smartrescue.dispatch.Ambulance;
import smartrescue.emergency.AssistanceRecord;
import smartrescue.emergency.Emergency;
import smartrescue.emergency.EmergencyStatus;
import smartrescue.emergency.Severity;
import smartrescue.transfer.HospitalTransfer;
import smartrescue.transfer.Patient;

public class EmergencyRepository {

    private static final Path FILE =
            Path.of("data", "emergencies.csv");

    private static final String LEGACY_HEADER =
            "incidentId,location,description,reportedAt,type,severity,patientCount,status";

    private static final String HEADER =
            "incidentId,location,description,reportedAt,type,severity,patientCount,status,ambulanceId,witnessId,operatorId";

    private static final String AMBULANCE_HEADER =
            "incidentId,location,description,reportedAt,type,severity,patientCount,status,ambulanceId";

    public void saveEmergency(Emergency emergency) {

        if (emergency == null) {
            throw new IllegalArgumentException(
                    "Emergency cannot be null."
            );
        }

        ensureSchema();

        String ambulanceId = emergency.getAmbulance() == null
                ? null
                : String.valueOf(
                        emergency.getAmbulance().getAmbulanceId()
                );

        String row =
                CsvSupport.escape(emergency.getIncidentId()) + "," +
                CsvSupport.escape(emergency.getLocation()) + "," +
                CsvSupport.escape(emergency.getDescription()) + "," +
                CsvSupport.escape(emergency.getReportedAt().toString()) + "," +
                CsvSupport.escape(emergency.getType()) + "," +
                CsvSupport.escape(
                        emergency.getSeverity() == null
                                ? null
                                : emergency.getSeverity().name()
                ) + "," +
                emergency.getPatientCount() + "," +
                CsvSupport.escape(emergency.getStatus().name()) + "," +
                CsvSupport.escape(ambulanceId) + "," +
                CsvSupport.escape(emergency.getWitnessId() == null
                        ? null : emergency.getWitnessId().toString()) + "," +
                CsvSupport.escape(emergency.getOperatorId() == null
                        ? null : emergency.getOperatorId().toString());

        CsvSupport.upsert(
                FILE,
                HEADER,
                0,
                emergency.getIncidentId(),
                row
        );

        PatientRepository patientRepository =
                new PatientRepository();

        for (Patient patient : emergency.getPatients()) {
            patientRepository.savePatient(
                    patient, emergency.getIncidentId()
            );
        }

        AssistanceRecordRepository assistanceRepository =
                new AssistanceRecordRepository();

        for (AssistanceRecord record : emergency.getAssistanceRecords()) {
            assistanceRepository.saveAssistanceRecord(
                    record, emergency.getIncidentId()
            );
        }

        HospitalTransferRepository transferRepository =
                new HospitalTransferRepository();

        for (HospitalTransfer transfer : emergency.getHospitalTransfers()) {
            transferRepository.saveTransfer(
                    transfer, emergency.getIncidentId()
            );
        }
    }

    public List<Emergency> loadAllEmergencies() {

        ensureSchema();

        List<Emergency> emergencies = new ArrayList<>();
        List<String> lines = CsvSupport.readLines(FILE, HEADER);

        for (int i = 1; i < lines.size(); i++) {

            if (lines.get(i).isBlank()) {
                continue;
            }

            Emergency emergency = convertFromCsv(lines.get(i));

            restoreRelatedRecords(emergency);

            emergencies.add(emergency);
        }

        return emergencies;
    }

    public Emergency findEmergencyById(String incidentId) {

        for (Emergency emergency : loadAllEmergencies()) {

            if (emergency.getIncidentId().equals(incidentId)) {
                return emergency;
            }
        }

        return null;
    }

    private void ensureSchema() {
        CsvSupport.ensureColumns(FILE, HEADER, LEGACY_HEADER, AMBULANCE_HEADER);
    }

    private void restoreRelatedRecords(Emergency emergency) {

        String emergencyId = emergency.getIncidentId();

        for (Patient patient :
                new PatientRepository().loadByEmergencyId(emergencyId)) {
            emergency.addPatient(patient);
        }

        for (AssistanceRecord record :
                new AssistanceRecordRepository()
                        .loadByEmergencyId(emergencyId)) {
            emergency.addAssistanceRecord(record);
        }

        for (HospitalTransfer transfer :
                new HospitalTransferRepository()
                        .loadByEmergencyId(emergencyId, emergency.getPatients())) {
            emergency.addHospitalTransfer(transfer);
        }
    }

    private Emergency convertFromCsv(String line) {

        List<String> fields = CsvSupport.parse(line);

        if (fields.size() != 11) {
            throw new IllegalStateException(
                    "Invalid emergency CSV record: " + line
            );
        }

        Severity severity = fields.get(5).isBlank()
                ? null
                : Severity.valueOf(fields.get(5));

        Emergency emergency = new Emergency(
                fields.get(0),
                fields.get(1),
                fields.get(2),
                LocalDateTime.parse(fields.get(3)),
                fields.get(4),
                severity,
                Integer.parseInt(fields.get(6)),
                EmergencyStatus.valueOf(fields.get(7))
        );
        if (!fields.get(9).isBlank()) {
            emergency.setWitnessId(Integer.parseInt(fields.get(9)));
        }
        if (!fields.get(10).isBlank()) {
            emergency.setOperatorId(Integer.parseInt(fields.get(10)));
        }

        if (!fields.get(8).isBlank()) {

            int ambulanceId = Integer.parseInt(fields.get(8));

            Ambulance ambulance =
                    new AmbulanceRepository().findAmbulanceById(ambulanceId);

            if (ambulance == null) {
                throw new IllegalStateException(
                        "Ambulance " + ambulanceId
                        + " referenced by emergency "
                        + emergency.getIncidentId()
                        + " was not found."
                );
            }

            emergency.assignAmbulance(ambulance);
        }

        return emergency;
    }
}