import smartrescue.dispatch.*;
import smartrescue.emergency.*;

public class AssistanceRecordTest {

    public static void main(String[] args) {

        Ambulance ambulance = new Ambulance(
            1, "ABC-123", AmbulanceStatus.AVAILABLE, "Cairo"
        );

        AmbulanceTeam team = new AmbulanceTeam(
            1, "Ahmed", 2, "Available", ambulance
        );

        Emergency emergency = new Emergency(
            "Cairo", "Car accident", 2
        );

        System.out.println("Records before assistance: "
                + emergency.getAssistanceRecords().size());

        AssistanceRecord record =
                team.provideOnSceneAssistance(emergency);

        System.out.println("Records after assistance: "
                + emergency.getAssistanceRecords().size());

        System.out.println("Record created: "
                + (record != null));
    }
}