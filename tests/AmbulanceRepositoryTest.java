import java.util.List;

import smartrescue.data.AmbulanceRepository;
import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceStatus;

public class AmbulanceRepositoryTest {

    public static void main(String[] args) {

        AmbulanceRepository repository = new AmbulanceRepository();

        int testId = 999999;

        // Test 1: Save an available ambulance.
        Ambulance ambulance = new Ambulance(
                testId,
                "TEST-999",
                AmbulanceStatus.AVAILABLE,
                "Cairo"
        );

        repository.saveAmbulance(ambulance);

        // Test 2: Load the ambulance by ID.
        Ambulance loaded = repository.findAmbulanceById(testId);

        if (loaded == null) {
            throw new AssertionError("Ambulance was not loaded.");
        }

        if (!loaded.getLicensePlate().equals("TEST-999")) {
            throw new AssertionError("License plate does not match.");
        }

        if (loaded.getStatus() != AmbulanceStatus.AVAILABLE) {
            throw new AssertionError("Initial status does not match.");
        }

        System.out.println("PASS: Ambulance saved and loaded.");

        // Test 3: Update the same ambulance ID.
        Ambulance updatedAmbulance = new Ambulance(
                testId,
                "TEST-999",
                AmbulanceStatus.OUT_OF_SERVICE,
                "Cairo"
        );

        repository.saveAmbulance(updatedAmbulance);

        Ambulance updated = repository.findAmbulanceById(testId);

        if (updated == null ||
            updated.getStatus() != AmbulanceStatus.OUT_OF_SERVICE) {
            throw new AssertionError("Ambulance status was not updated.");
        }

        System.out.println("PASS: Existing ambulance record updated.");

        // Test 4: Check that there is only one record for this ID.
        List<Ambulance> allAmbulances =
                repository.loadAllAmbulances();

        long matchingRecords = allAmbulances.stream()
                .filter(value -> value.getAmbulanceId() == testId)
                .count();

        if (matchingRecords != 1) {
            throw new AssertionError(
                    "Expected one record, found " + matchingRecords
            );
        }

        System.out.println("PASS: No duplicate ambulance records.");
        System.out.println("\nALL AMBULANCE REPOSITORY TESTS PASSED");
    }
}