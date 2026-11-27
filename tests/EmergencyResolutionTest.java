import smartrescue.dispatch.*;
import smartrescue.emergency.*;

public class EmergencyResolutionTest {

    public static void main(String[] args) {

        Ambulance ambulance = new Ambulance(
            1, "ABC-123", AmbulanceStatus.AVAILABLE, "Cairo"
        );

        AmbulanceTeam team = new AmbulanceTeam(
            1, "Ahmed", 2, "Available", ambulance
        );

        Emergency emergency = new Emergency(
            "Cairo", "Test emergency", 2
        );

        team.provideOnSceneAssistance(emergency);
        team.resolveEmergency(emergency);

        System.out.println("Before ON_SCENE: "
                + emergency.getStatus());

        emergency.changeStatus(EmergencyStatus.VERIFIED);
        emergency.changeStatus(EmergencyStatus.RESPONDING);
        emergency.changeStatus(EmergencyStatus.ON_SCENE);

        team.resolveEmergency(emergency);

        System.out.println("After ON_SCENE: "
                + emergency.getStatus());
    }
}