import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import smartrescue.data.EmergencyRepository;
import smartrescue.emergency.Emergency;
import smartrescue.emergency.EmergencyStatus;
import smartrescue.emergency.Severity;

public class EmergencyRepositoryTest {

    public static void main(String[] args) throws Exception {

        Path emergencyFile = Path.of("data", "emergencies.csv");
        Path counterFile = Path.of("data", "id_counters.csv");

        byte[] originalEmergencyData = Files.readAllBytes(emergencyFile);
        byte[] originalCounterData = Files.readAllBytes(counterFile);

        try {
            EmergencyRepository repository = new EmergencyRepository();

            int originalCount = repository.loadAllEmergencies().size();

            Emergency emergency = new Emergency(
                    "Cairo",
                    "Car accident, near the \"main bridge\"",
                    "Vehicle Accident",
                    Severity.HIGH,
                    2
            );

            String originalId = emergency.getIncidentId();

            // Test 1: Save emergency.
            repository.saveEmergency(emergency);

            System.out.println("Test 1 passed: Emergency saved.");

            // Test 2: Load emergency.
            List<Emergency> emergencies =
                    repository.loadAllEmergencies();

            if (emergencies.size() != originalCount + 1) {
                throw new AssertionError(
                    "Expected exactly one additional emergency."
                );
            }

            Emergency loaded = repository.findEmergencyById(originalId);

            if (loaded == null) {
                throw new AssertionError("Saved emergency was not loaded.");
            }

            if (!loaded.getIncidentId().equals(originalId)) {
                throw new AssertionError("Incident ID was not preserved.");
            }

            if (!loaded.getDescription().equals(
                    "Car accident, near the \"main bridge\"")) {
                throw new AssertionError("Description was not preserved.");
            }

            if (loaded.getStatus() != EmergencyStatus.REPORTED) {
                throw new AssertionError("Initial status was not preserved.");
            }

            System.out.println("Test 2 passed: Emergency loaded correctly.");

            // Test 3: Update status and save again.
            loaded.changeStatus(EmergencyStatus.VERIFIED);
            repository.saveEmergency(loaded);

            emergencies = repository.loadAllEmergencies();

            if (emergencies.size() != originalCount + 1) {
                throw new AssertionError(
                    "Updating created a duplicate record."
                );
            }

            Emergency updated = repository.findEmergencyById(originalId);

            if (updated == null ||
                    updated.getStatus() != EmergencyStatus.VERIFIED) {
                throw new AssertionError("Updated status was not saved.");
            }

            System.out.println("Test 3 passed: Existing emergency updated.");

            // Test 4: Find emergency by ID.
            Emergency found = repository.findEmergencyById(originalId);

            if (found == null) {
                throw new AssertionError("Emergency was not found.");
            }

            if (!found.getIncidentId().equals(originalId)) {
                throw new AssertionError("Wrong emergency was returned.");
            }

            System.out.println("Test 4 passed: Emergency found by ID.");

            System.out.println("\nEMERGENCY REPOSITORY TEST PASSED");

        } finally {
            Files.write(emergencyFile, originalEmergencyData);
            Files.write(counterFile, originalCounterData);

            System.out.println("Original CSV files restored.");
        }
    }
}
