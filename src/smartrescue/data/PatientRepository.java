package smartrescue.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import smartrescue.transfer.Patient;

public class PatientRepository {

    private static final Path FILE =
            Path.of("data", "patients.csv");

    private static final String HEADER =
            "patientId,emergencyId,name,age,condition,treatmentStatus";

    public void savePatient(Patient patient, String emergencyId) {
        if (patient == null || emergencyId == null || emergencyId.isBlank()) {
            throw new IllegalArgumentException(
                "Patient and emergency ID are required."
            );
        }

        patient.setEmergencyId(emergencyId);

        String row =
            CsvSupport.escape(String.valueOf(patient.getPatientId())) + "," +
            CsvSupport.escape(emergencyId) + "," +
            CsvSupport.escape(patient.getName()) + "," +
            patient.getAge() + "," +
            CsvSupport.escape(patient.getCondition()) + "," +
            CsvSupport.escape(patient.getTreatmentStatus());

        CsvSupport.upsert(
            FILE, HEADER, 0, String.valueOf(patient.getPatientId()), row
        );
    }

    public List<Patient> loadByEmergencyId(String emergencyId) {
        List<Patient> patients = new ArrayList<>();
        List<String> lines = CsvSupport.readLines(FILE, HEADER);

        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) continue;

            List<String> fields = CsvSupport.parse(lines.get(i));
            if (fields.size() != 6) {
                throw new IllegalStateException(
                    "Invalid patient CSV record: " + lines.get(i)
                );
            }

            if (fields.get(1).equals(emergencyId)) {
                patients.add(new Patient(
                    Integer.parseInt(fields.get(0)),
                    fields.get(2),
                    Integer.parseInt(fields.get(3)),
                    fields.get(4),
                    fields.get(5),
                    fields.get(1)
                ));
            }
        }

        return patients;
    }
}
