import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import smartrescue.data.AmbulanceRepository;
import smartrescue.data.EmergencyRepository;
import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceStatus;
import smartrescue.dispatch.Operator;
import smartrescue.emergency.Emergency;
import smartrescue.emergency.EmergencyStatus;

public class AmbulanceAssignmentPersistenceTest {

    public static void main(String[] args) throws Exception {

        AmbulanceRepository ambulanceRepository =
                new AmbulanceRepository();

        EmergencyRepository emergencyRepository =
                new EmergencyRepository();

        Ambulance ambulance =
                ambulanceRepository.findAmbulanceById(1);

        if (ambulance == null) {
            throw new AssertionError("Demo ambulance 1 was not found.");
        }

        String testIncidentId = null;

        try {
            // Create a temporary emergency.
            Emergency emergency = new Emergency(
                    "Test Location",
                    "Temporary assignment persistence test",
                    1
            );

            testIncidentId = emergency.getIncidentId();

            emergency.changeStatus(EmergencyStatus.VERIFIED);

            // Use the actual Operator assignment method.
            Operator operator = new Operator(
                    999,
                    "Test Operator",
                    "Test Shift",
                    1
            );

            operator.assignAmbulance(emergency, ambulance);

            // Save the updated ambulance.
            ambulanceRepository.saveAmbulance(ambulance);

            // Reload the emergency from CSV.
            Emergency loaded =
                    emergencyRepository.findEmergencyById(testIncidentId);

            if (loaded == null) {
                throw new AssertionError("Emergency was not restored.");
            }

            if (loaded.getStatus() != EmergencyStatus.RESPONDING) {
                throw new AssertionError("Emergency status was not restored.");
            }

            if (loaded.getAmbulance() == null) {
                throw new AssertionError("Ambulance relationship was not restored.");
            }

            if (loaded.getAmbulance().getAmbulanceId() != 1) {
                throw new AssertionError("Incorrect ambulance was restored.");
            }

            Ambulance savedAmbulance =
                    ambulanceRepository.findAmbulanceById(1);

            if (savedAmbulance.getStatus() != AmbulanceStatus.DISPATCHED) {
                throw new AssertionError("Ambulance status was not persisted.");
            }

            System.out.println("PASS: Emergency assignment saved.");
            System.out.println("PASS: Emergency status restored as RESPONDING.");
            System.out.println("PASS: Ambulance relationship restored.");
            System.out.println("PASS: Ambulance status restored as DISPATCHED.");

        } finally {

            // Remove only the temporary emergency record.
            if (testIncidentId != null) {

                Path emergencyFile = Path.of("data", "emergencies.csv");
                List<String> lines = Files.readAllLines(emergencyFile);

                List<String> cleanedLines = new java.util.ArrayList<>();
                cleanedLines.add(lines.get(0));

                for (int i = 1; i < lines.size(); i++) {
                    if (lines.get(i).isBlank()) {
                        continue;
                    }

                    if (!smartrescue.data.CsvSupport.parse(lines.get(i))
                            .get(0).equals(testIncidentId)) {
                        cleanedLines.add(lines.get(i));
                    }
                }

                Files.write(emergencyFile, cleanedLines);
            }

            // Restore demo ambulance 1 to AVAILABLE.
            ambulanceRepository.saveAmbulance(new Ambulance(
                    1,
                    "DEMO-AMB-001",
                    AmbulanceStatus.AVAILABLE,
                    "Cairo"
            ));

            System.out.println("Temporary test data cleaned up.");
        }
    }
}