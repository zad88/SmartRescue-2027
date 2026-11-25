package smartrescue.transfer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import smartrescue.dispatch.AmbulanceTeam;
import smartrescue.hospital.Hospital;
import smartrescue.hospital.HospitalStaff;

public class HospitalTransfer {

    private int transferId;
    private LocalDateTime transferTime;
    private int patientCount;
    private String transferStatus;

    // UML associations

    private List<Patient> patients;
    private AmbulanceTeam ambulanceTeam;
    private Hospital hospital;
    private HospitalStaff hospitalStaff;

    public HospitalTransfer(int transferId, int patientCount,
                            String transferStatus, List<Patient> patients,
                            AmbulanceTeam ambulanceTeam, Hospital hospital) {

        this.transferId = transferId;
        this.patientCount = patientCount;
        this.transferStatus = transferStatus;
        this.transferTime = null;
        this.patients = new ArrayList<>(patients);
        this.ambulanceTeam = ambulanceTeam;
        this.hospital = hospital;
        this.hospitalStaff = null;
    }

    public void recordTransfer() {
        this.transferTime = LocalDateTime.now();
    }

    public void recordConfirmation(HospitalStaff staff) {
        this.hospitalStaff = staff;
    }

}