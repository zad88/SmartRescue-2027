import java.time.LocalDateTime;

import smartrescue.emergency.Emergency;
import smartrescue.emergency.EmergencyStatus;

public class EmergencyLifecycleTest {

    public static void main(String[] args) {

        Emergency emergency = new Emergency("test", "Cairo", "Accident",
                LocalDateTime.now(), null, null, 2, EmergencyStatus.REPORTED);
        emergency.changeStatus(EmergencyStatus.CLOSED);
        check(emergency.getStatus() == EmergencyStatus.REPORTED,
                "A reported incident cannot be closed directly.");
        for (EmergencyStatus next : new EmergencyStatus[] {
                EmergencyStatus.VERIFIED, EmergencyStatus.RESPONDING,
                EmergencyStatus.ON_SCENE, EmergencyStatus.RESOLVED, EmergencyStatus.CLOSED }) {
            emergency.changeStatus(next);
            check(emergency.getStatus() == next, "Transition to " + next + " failed.");
        }
        emergency.changeStatus(EmergencyStatus.REPORTED);
        check(emergency.getStatus() == EmergencyStatus.CLOSED,
                "A closed incident cannot be reopened.");
        System.out.println("PASS: Emergency lifecycle transitions");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}