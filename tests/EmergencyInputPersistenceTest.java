import java.nio.file.Files;
import java.nio.file.Path;

import smartrescue.data.EmergencyRepository;
import smartrescue.emergency.Emergency;
import smartrescue.emergency.Witness;

public class EmergencyInputPersistenceTest {
    public static void main(String[] args) {
        if (Files.exists(Path.of("data"))) {
            throw new IllegalStateException("Run this test in an empty working directory.");
        }
        Witness witness = new Witness(1, "Witness", "0", "Cairo");
        for (int count : new int[] {0, -1}) {
            try {
                witness.reportEmergency("Cairo", "Description", count);
                throw new AssertionError("Invalid injured count was accepted");
            } catch (IllegalArgumentException expected) {
                // Invalid input must be rejected before persistence.
            }
        }
        String description = "First line\r\nSecond line, \"quoted\"\nThird line";
        Emergency emergency = witness.reportEmergency("Cairo", description, 1);
        Emergency loaded = new EmergencyRepository().findEmergencyById(emergency.getIncidentId());
        if (loaded == null || !description.equals(loaded.getDescription())) {
            throw new AssertionError("Multiline emergency description did not round-trip");
        }
        loaded.setClassification("Traffic", smartrescue.emergency.Severity.HIGH);
        new EmergencyRepository().saveEmergency(loaded);
        if (!description.equals(new EmergencyRepository()
                .findEmergencyById(emergency.getIncidentId()).getDescription())) {
            throw new AssertionError("Multiline description changed after update");
        }
        Emergency second = witness.reportEmergency("Cairo", "Another incident", 1);
        if (emergency.getIncidentId().equals(second.getIncidentId())
                || new EmergencyRepository().loadAllEmergencies().size() != 2) {
            throw new AssertionError("Incident IDs must be unique and records must not be overwritten");
        }
        System.out.println("PASS: Emergency input validation and multiline persistence");
    }
}
