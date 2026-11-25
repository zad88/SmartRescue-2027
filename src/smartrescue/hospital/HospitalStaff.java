package smartrescue.hospital;

import java.util.List;
import smartrescue.transfer.HospitalTransfer;

public class HospitalStaff {

    private int staffId;
    private String fullName;
    private String role;
    private String department;

    private Hospital hospital;

    public HospitalStaff(int staffId, String fullName, String role,
                         String department, Hospital hospital) {

        this.staffId = staffId;
        this.fullName = fullName;
        this.role = role;
        this.department = department;
        this.hospital = hospital;
    }

    public void updateHospitalInfo(Hospital h, int beds, int capacity,
                                   List<String> specialties) {

        h.updateAvailability(beds, capacity, specialties);
    }

    public void confirmPatientTransfer(HospitalTransfer transfer) {
        transfer.recordConfirmation(this);
    }

}