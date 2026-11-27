import java.util.Arrays;

import smartrescue.dispatch.*;
import smartrescue.hospital.*;
import smartrescue.emergency.*;
import smartrescue.transfer.*;

public class HospitalTransferTest {

    public static void main(String[] args) {

        Hospital hospital = new Hospital(
            1, "City Hospital", "Cairo",
            10, 10, Arrays.asList("Emergency")
        );

        Ambulance ambulance = new Ambulance(
            1, "ABC-123", AmbulanceStatus.AVAILABLE, "Cairo"
        );

        AmbulanceTeam team = new AmbulanceTeam(
            1, "Ahmed", 2, "Available", ambulance
        );

        Emergency emergency = new Emergency(
            "Cairo", "Car accident", 3
        );

        HospitalTransfer transfer =
                team.transferPatients(emergency, hospital);

        System.out.println("Transfer status: "
                + transfer.getTransferStatus());

        System.out.println("Beds before confirmation: "
                + hospital.getAvailableBeds());

        System.out.println("Capacity before confirmation: "
                + hospital.getEmergencyCapacity());

        HospitalStaff staff = new HospitalStaff(
            1, "Ali", "Doctor", "Emergency", hospital
        );

        staff.confirmPatientTransfer(transfer);

        System.out.println("\nAfter confirmation:");

        System.out.println("Transfer status: "
                + transfer.getTransferStatus());

        System.out.println("Beds: "
                + hospital.getAvailableBeds());

        System.out.println("Capacity: "
                + hospital.getEmergencyCapacity());

        staff.confirmPatientTransfer(transfer);

        System.out.println("\nAfter second confirmation attempt:");

        System.out.println("Beds: "
                + hospital.getAvailableBeds());

        System.out.println("Capacity: "
                + hospital.getEmergencyCapacity());
    }
}