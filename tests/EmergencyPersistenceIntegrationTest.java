import java.nio.file.Files;
import java.nio.file.Path;

import smartrescue.data.EmergencyRepository;
import smartrescue.emergency.Emergency;
import smartrescue.emergency.EmergencyStatus;
import smartrescue.emergency.Severity;
import smartrescue.emergency.Witness;
import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceStatus;
import smartrescue.dispatch.AmbulanceTeam;
import smartrescue.dispatch.Operator;

public class EmergencyPersistenceIntegrationTest {

    public static void main(String[] args) throws Exception {

        Path emergencyFile = Path.of("data", "emergencies.csv");
        Path counterFile = Path.of("data", "id_counters.csv");

        byte[] originalEmergencyData = Files.readAllBytes(emergencyFile);
        byte[] originalCounterData = Files.readAllBytes(counterFile);

        try {
            EmergencyRepository repository = new EmergencyRepository();

            Witness witness = new Witness(
                    1, "Test Witness", "01000000000", "Cairo"
            );

            Emergency emergency = witness.reportEmergency(
                    "Cairo",
                    "Vehicle accident near the main road",
                    2
            );

            String incidentId = emergency.getIncidentId();

            if (repository.findEmergencyById(incidentId).getStatus()
                    != EmergencyStatus.REPORTED) {
                throw new AssertionError("REPORTED status was not saved.");
            }

            System.out.println("Test 1 passed: Witness report saved.");

            Operator operator = new Operator(
                    1, "Test Operator", "Morning", 1
            );

            operator.classifyEmergency(
                    emergency, "Vehicle Accident", Severity.HIGH
            );

            if (!operator.reviewAndVerifyEmergency(emergency)) {
                throw new AssertionError("Emergency verification failed.");
            }

            if (repository.findEmergencyById(incidentId).getStatus()
                    != EmergencyStatus.VERIFIED) {
                throw new AssertionError("VERIFIED status was not saved.");
            }

            System.out.println("Test 2 passed: Classification and verification saved.");

            Ambulance ambulance = new Ambulance(
                    1, "TEST-001", AmbulanceStatus.AVAILABLE, "Cairo"
            );

            operator.assignAmbulance(emergency, ambulance);

            if (repository.findEmergencyById(incidentId).getStatus()
                    != EmergencyStatus.RESPONDING) {
                throw new AssertionError("RESPONDING status was not saved.");
            }

            System.out.println("Test 3 passed: Ambulance assignment saved.");

            AmbulanceTeam team = new AmbulanceTeam(
                    1, "Test Driver", 2, "Available", ambulance
            );

            team.updateEmergencyStatus(
                    emergency, EmergencyStatus.ON_SCENE
            );

            if (repository.findEmergencyById(incidentId).getStatus()
                    != EmergencyStatus.ON_SCENE) {
                throw new AssertionError("ON_SCENE status was not saved.");
            }

            System.out.println("Test 4 passed: On-scene status saved.");

            team.provideOnSceneAssistance(emergency);
            team.resolveEmergency(emergency);

            if (repository.findEmergencyById(incidentId).getStatus()
                    != EmergencyStatus.RESOLVED) {
                throw new AssertionError("RESOLVED status was not saved.");
            }

            System.out.println("Test 5 passed: Resolution saved.");

            operator.closeEmergency(emergency);

            Emergency loaded = repository.findEmergencyById(incidentId);

            if (loaded == null ||
                    loaded.getStatus() != EmergencyStatus.CLOSED) {
                throw new AssertionError("CLOSED status was not saved.");
            }

            System.out.println("Test 6 passed: Closure saved.");

            System.out.println("\nEMERGENCY PERSISTENCE INTEGRATION TEST PASSED");

        } finally {
            Files.write(emergencyFile, originalEmergencyData);
            Files.write(counterFile, originalCounterData);

            System.out.println("Original CSV files restored.");
        }
    }
}
