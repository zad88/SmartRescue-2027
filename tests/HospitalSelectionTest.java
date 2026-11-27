import java.util.List;

import smartrescue.dispatch.*;
import smartrescue.hospital.Hospital;
import smartrescue.transfer.HospitalTransfer;
import smartrescue.emergency.Emergency;

public class HospitalSelectionTest {

    public static void main(String[] args) {

        Ambulance ambulance = new Ambulance(
            1, "ABC-123", AmbulanceStatus.AVAILABLE, "Cairo"
        );

        AmbulanceTeam team = new AmbulanceTeam(
            1, "Ahmed", 2, "Available", ambulance
        );

        List<Hospital> hospitals =
            team.searchHospital("Emergency", 10);

        System.out.println("Hospitals found: " + hospitals.size());

        Hospital selected = hospitals.get(1);

        team.selectHospital(selected);

        System.out.println("Selected hospital beds: "
            + selected.getAvailableBeds());

        Emergency emergency = new Emergency(
            "Cairo", "Accident", 2
        );

        HospitalTransfer transfer =
            team.transferPatients(emergency, hospitals.get(0));

        System.out.println("Transfer status: "
            + transfer.getTransferStatus());

        System.out.println("Selected hospital beds after transfer creation: "
            + selected.getAvailableBeds());
    }
}