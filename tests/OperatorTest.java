import smartrescue.dispatch.*;
import smartrescue.emergency.*;

public class OperatorTest {

    public static void main(String[] args) {

        Witness witness = new Witness(
            1, "Ahmed", "01000000000", "Cairo"
        );

        Operator operator = new Operator(
            1, "Mohamed", "Morning", 101
        );

        Emergency emergency = witness.reportEmergency(
            "Cairo", "Car accident", 2
        );

        System.out.println("Initial status: " + emergency.getStatus());

        operator.classifyEmergency(
            emergency, "Traffic Accident", Severity.HIGH
        );

        boolean verified =
            operator.reviewAndVerifyEmergency(emergency);

        System.out.println("Verified: " + verified);
        System.out.println("Current status: " + emergency.getStatus());

        Ambulance ambulance = new Ambulance(
            1, "ABC-123", AmbulanceStatus.AVAILABLE, "Cairo"
        );

        operator.assignAmbulance(emergency, ambulance);

        System.out.println("Ambulance available: "
                + ambulance.checkAvailability());

        System.out.println("After assignment: " + emergency.getStatus());

        emergency.changeStatus(EmergencyStatus.RESOLVED);
        operator.closeEmergency(emergency);

        System.out.println("Final status: " + emergency.getStatus());
    }
}