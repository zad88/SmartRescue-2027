import java.util.List;

import smartrescue.dispatch.*;
import smartrescue.hospital.Hospital;

public class AmbulanceTeamSearchTest {

    public static void main(String[] args) {

        Ambulance ambulance = new Ambulance(
            1, "ABC-123", AmbulanceStatus.AVAILABLE, "Cairo"
        );

        AmbulanceTeam team = new AmbulanceTeam(
            1, "Ahmed", 2, "Available", ambulance
        );

        List<Hospital> results =
            team.searchHospital("Emergency", 10);

        System.out.println("Hospitals found: " + results.size());

        for (Hospital hospital : results) {
            System.out.println("Available beds: "
                + hospital.getAvailableBeds());
        }
    }
}