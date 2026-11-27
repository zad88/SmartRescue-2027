import smartrescue.emergency.Emergency;
import smartrescue.emergency.EmergencyStatus;

public class EmergencyLifecycleTest {

    public static void main(String[] args) {

        Emergency emergency = new Emergency(
            "Cairo", "Car accident", 2
        );

        System.out.println("Initial: " + emergency.getStatus());

        // Invalid transition: REPORTED -> CLOSED
        emergency.changeStatus(EmergencyStatus.CLOSED);
        System.out.println("After invalid CLOSED: " + emergency.getStatus());

        // Valid transitions
        emergency.changeStatus(EmergencyStatus.VERIFIED);
        System.out.println("After VERIFIED: " + emergency.getStatus());

        emergency.changeStatus(EmergencyStatus.RESPONDING);
        System.out.println("After RESPONDING: " + emergency.getStatus());

        emergency.changeStatus(EmergencyStatus.ON_SCENE);
        System.out.println("After ON_SCENE: " + emergency.getStatus());

        emergency.changeStatus(EmergencyStatus.RESOLVED);
        System.out.println("After RESOLVED: " + emergency.getStatus());

        emergency.changeStatus(EmergencyStatus.CLOSED);
        System.out.println("After CLOSED: " + emergency.getStatus());

        // Invalid transition after closure
        emergency.changeStatus(EmergencyStatus.REPORTED);
        System.out.println("After invalid REPORTED: " + emergency.getStatus());
    }
}