package smartrescue.hospital;

import java.util.List;

import smartrescue.Person;
import smartrescue.data.HospitalRepository;
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

        if (h == null || hospital == null
                || h.getHospitalId() != hospital.getHospitalId()) {
            throw new IllegalArgumentException("Staff may only update their own hospital.");
        }
        if (beds < 0 || capacity < 0 || specialties == null || specialties.isEmpty()) {
            throw new IllegalArgumentException("Enter non-negative availability and at least one specialty.");
        }
        for (String specialty : specialties) {
            if (specialty == null || specialty.isBlank() || specialty.contains(";")
                    || specialty.contains("\n") || specialty.contains("\r")) {
                throw new IllegalArgumentException("Enter valid specialties without semicolons or line breaks.");
            }
        }

        HospitalRepository repository = new HospitalRepository();

        repository.updateHospitalAvailability(
            h.getHospitalId(),
            beds,
            capacity,
            specialties
        );

        h.updateAvailability(beds, capacity, specialties);
    }

    public void confirmPatientTransfer(HospitalTransfer transfer) {
        transfer.recordConfirmation(this);
    }

    public Hospital getHospital() {
        return hospital;
    }

    public int getStaffId() {
        return staffId;
    }
}