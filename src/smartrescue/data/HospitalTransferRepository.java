package smartrescue.data;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import smartrescue.hospital.Hospital;
import smartrescue.transfer.HospitalTransfer;
import smartrescue.transfer.Patient;

public class HospitalTransferRepository {

    private static final Path FILE =
            Path.of("data", "hospital_transfers.csv");

    private static final String HEADER =
            "transferId,emergencyId,transferTime,patientCount,transferStatus,hospitalId,patientIds";

    public void saveTransfer(HospitalTransfer transfer, String emergencyId) {
        if (transfer == null || emergencyId == null || emergencyId.isBlank()) {
            throw new IllegalArgumentException(
                "Transfer and emergency ID are required."
            );
        }

        transfer.setEmergencyId(emergencyId);

        int hospitalId = transfer.getHospital() == null
                ? 0 : transfer.getHospital().getHospitalId();

        String patientIds = transfer.getPatients().stream()
                .map(patient -> String.valueOf(patient.getPatientId()))
                .collect(Collectors.joining(";"));

        String row =
            transfer.getTransferId() + "," +
            CsvSupport.escape(emergencyId) + "," +
            CsvSupport.escape(transfer.getTransferTime() == null
                ? null : transfer.getTransferTime().toString()) + "," +
            transfer.getPatientCount() + "," +
            CsvSupport.escape(transfer.getTransferStatus()) + "," +
            hospitalId + "," +
            CsvSupport.escape(patientIds);

        CsvSupport.upsert(
            FILE, HEADER, 0, String.valueOf(transfer.getTransferId()), row
        );
    }

    public List<HospitalTransfer> loadByEmergencyId(String emergencyId) {
        List<HospitalTransfer> transfers = new ArrayList<>();
        List<String> lines = CsvSupport.readLines(FILE, HEADER);

        List<Patient> emergencyPatients =
                new PatientRepository().loadByEmergencyId(emergencyId);

        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) continue;

            List<String> fields = CsvSupport.parse(lines.get(i));
            if (fields.size() != 7) {
                throw new IllegalStateException(
                    "Invalid transfer CSV record: " + lines.get(i)
                );
            }

            if (!fields.get(1).equals(emergencyId)) continue;

            LocalDateTime transferTime = fields.get(2).isBlank()
                    ? null : LocalDateTime.parse(fields.get(2));

            int hospitalId = Integer.parseInt(fields.get(5));
            Hospital hospital = hospitalId == 0
                    ? null
                    : new HospitalRepository().findHospitalById(hospitalId);

            List<Patient> transferPatients = new ArrayList<>();

            if (!fields.get(6).isBlank()) {
                String[] ids = fields.get(6).split(";");

                for (String id : ids) {
                    for (Patient patient : emergencyPatients) {
                        if (patient.getPatientId() == Integer.parseInt(id)) {
                            transferPatients.add(patient);
                            break;
                        }
                    }
                }
            }

            transfers.add(new HospitalTransfer(
                Integer.parseInt(fields.get(0)),
                transferTime,
                Integer.parseInt(fields.get(3)),
                fields.get(4),
                transferPatients,
                hospital,
                emergencyId
            ));
        }

        return transfers;
    }
}
