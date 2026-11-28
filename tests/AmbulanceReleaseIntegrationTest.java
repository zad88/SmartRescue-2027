import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import smartrescue.data.AmbulanceRepository;
import smartrescue.data.EmergencyRepository;
import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceReleaseService;
import smartrescue.dispatch.AmbulanceStatus;
import smartrescue.dispatch.AmbulanceTeam;
import smartrescue.dispatch.Operator;
import smartrescue.emergency.AssistanceRecord;
import smartrescue.emergency.Emergency;
import smartrescue.emergency.EmergencyStatus;

public class AmbulanceReleaseIntegrationTest {

    public static void main(String[] args) {
        if (Files.exists(Path.of("data"))) {
            throw new IllegalStateException("Run this test in an empty working directory.");
        }

        AmbulanceRepository ambulances = new AmbulanceRepository();
        EmergencyRepository emergencies = new EmergencyRepository();
        Ambulance one = ambulance(1, AmbulanceStatus.DISPATCHED);
        Ambulance two = ambulance(2, AmbulanceStatus.DISPATCHED);
        Ambulance three = ambulance(3, AmbulanceStatus.OUT_OF_SERVICE);
        Ambulance four = ambulance(4, AmbulanceStatus.DISPATCHED);
        for (Ambulance ambulance : List.of(one, two, three, four)) {
            ambulances.saveAmbulance(ambulance);
        }

        emergencies.saveEmergency(emergency("closed-1", EmergencyStatus.CLOSED, one));
        emergencies.saveEmergency(emergency("closed-2", EmergencyStatus.CLOSED, two));
        emergencies.saveEmergency(emergency("active-2", EmergencyStatus.ON_SCENE, two));
        emergencies.saveEmergency(emergency("closed-3", EmergencyStatus.CLOSED, three));

        AmbulanceReleaseService service = new AmbulanceReleaseService();
        List<Ambulance> eligible = service.findReleasableAmbulances();
        check(eligible.size() == 1 && eligible.get(0).getAmbulanceId() == 1,
                "Only the dispatched ambulance with no active assignments is releasable");
        check(!service.releaseAmbulance(2), "Active ambulance must not be released");
        check(!service.releaseAmbulance(3), "Out-of-service ambulance must not be released");
        check(!service.releaseAmbulance(4), "Unrelated dispatched ambulance must not be released");
        check(service.releaseAmbulance(1), "Completed ambulance should be released");
        check(ambulances.findAmbulanceById(1).checkAvailability(),
                "Recovered availability must be persisted");

        Ambulance available = ambulances.findAmbulanceById(1);
        Emergency next = emergency("next", EmergencyStatus.VERIFIED, null);
        Operator operator = new Operator(1, "Operator", "Shift", 1);
        operator.assignAmbulance(next, available);
        check(emergencies.findEmergencyById("next").getAmbulance().getAmbulanceId() == 1,
                "New assignment must be persisted");
        check(ambulances.findAmbulanceById(1).getStatus() == AmbulanceStatus.DISPATCHED,
                "Assignment must persist the ambulance's dispatched status");
        check(!service.releaseAmbulance(1), "New active assignment must prevent recovery");

        next.changeStatus(EmergencyStatus.ON_SCENE);
        AmbulanceTeam team = new AmbulanceTeam(1, "Driver", 1, "On Duty", available);
        team.resolveEmergency(next);
        check(ambulances.findAmbulanceById(1).getStatus() == AmbulanceStatus.DISPATCHED,
                "An incident without assistance must not release its ambulance");

        next.addAssistanceRecord(new AssistanceRecord(1, "Assisted", LocalDateTime.now(), 1));
        team.resolveEmergency(next);
        check(emergencies.findEmergencyById("next").getStatus() == EmergencyStatus.RESOLVED,
                "Resolution must be persisted");
        check(ambulances.findAmbulanceById(1).checkAvailability(),
                "Resolving the incident must persist ambulance availability");
        check(available.checkAvailability(), "The team's ambulance must also be released");

        System.out.println("PASS: Ambulance recovery, reassignment, and resolution");
    }

    private static Ambulance ambulance(int id, AmbulanceStatus status) {
        return new Ambulance(id, "A" + id, status, "Base");
    }

    private static Emergency emergency(String id, EmergencyStatus status, Ambulance ambulance) {
        Emergency emergency = new Emergency(id, "location", "description",
                LocalDateTime.now(), null, null, 1, status);
        if (ambulance != null) {
            emergency.assignAmbulance(ambulance);
        }
        return emergency;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
