package smartrescue.hospital;

import java.util.List;

import smartrescue.Person;
import smartrescue.transfer.HospitalTransfer;

public class HospitalStaff extends Person {

    private int staffId;
    private String role;
    private String department;

    private Hospital hospital;

    public HospitalStaff(int staffId, String fullName, String role,
                         String department, Hospital hospital) {

        super(fullName);

        this.staffId = staffId;
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

    public Hospital getHospital() {
        return hospital;
    }
}