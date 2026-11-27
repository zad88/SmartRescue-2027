import java.util.Arrays;

import smartrescue.hospital.Hospital;
import smartrescue.hospital.HospitalStaff;
import smartrescue.transfer.HospitalTransfer;

public class HospitalStaffAuthorizationTest {

    public static void main(String[] args) {

        Hospital hospitalA = new Hospital(
            1, "Hospital A", "Cairo",
            10, 10, Arrays.asList("Emergency")
        );

        Hospital hospitalB = new Hospital(
            2, "Hospital B", "Giza",
            10, 10, Arrays.asList("Emergency")
        );

        HospitalStaff staffA = new HospitalStaff(
            1, "Ahmed", "Doctor", "Emergency", hospitalA
        );

        HospitalStaff staffB = new HospitalStaff(
            2, "Mohamed", "Doctor", "Emergency", hospitalB
        );

        HospitalTransfer transfer = new HospitalTransfer(
            1, 3, "PENDING",
            Arrays.asList(),
            null,
            hospitalB
        );

        transfer.recordTransfer();

        System.out.println("Initial status: "
            + transfer.getTransferStatus());

        staffA.confirmPatientTransfer(transfer);

        System.out.println("After Hospital A staff confirmation: "
            + transfer.getTransferStatus());

        System.out.println("Hospital B beds: "
            + hospitalB.getAvailableBeds());

        staffB.confirmPatientTransfer(transfer);

        System.out.println("After Hospital B staff confirmation: "
            + transfer.getTransferStatus());

        System.out.println("Hospital B beds: "
            + hospitalB.getAvailableBeds());
    }
}