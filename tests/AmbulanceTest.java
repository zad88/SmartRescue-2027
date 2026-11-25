import smartrescue.emergency.Emergency;
import smartrescue.emergency.Severity;
import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceStatus;

public class AmbulanceTest {

    public static void main(String[] args) {

        // Create an emergency

        Emergency emergency = new Emergency(
            "Cairo",
            "Car accident",
            "Traffic Accident",
            Severity.HIGH,
            2
        );

        // Create an available ambulance

        Ambulance ambulance = new Ambulance(
            1,
            "ABC-123",
            AmbulanceStatus.AVAILABLE,
            "Cairo"
        );

        // Test initial availability

        System.out.println("Initially available: "
                + ambulance.checkAvailability());

        // Assign ambulance to emergency

        ambulance.assignToEmergency(emergency);

        System.out.println("Available after assignment: "
                + ambulance.checkAvailability());

        // Display emergency details

        System.out.println("\nEmergency Details:");
        System.out.println(emergency.getDetails());

        // Release ambulance

        ambulance.release();

        System.out.println("\nAvailable after release: "
                + ambulance.checkAvailability());
    }
}