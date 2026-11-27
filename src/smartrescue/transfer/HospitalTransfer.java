package smartrescue.transfer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import smartrescue.data.HospitalRepository;
import smartrescue.data.HospitalTransferRepository;
import smartrescue.dispatch.AmbulanceTeam;
import smartrescue.hospital.Hospital;
import smartrescue.hospital.HospitalStaff;

public class HospitalTransfer {

    private int transferId;
    private LocalDateTime transferTime;
    private int patientCount;
    private String transferStatus;

    private List<Patient> patients;
    private AmbulanceTeam ambulanceTeam;
    private Hospital hospital;
    private HospitalStaff hospitalStaff;
    private String emergencyId;

    public HospitalTransfer(int transferId, int patientCount,
                            String transferStatus, List<Patient> patients,
                            AmbulanceTeam ambulanceTeam, Hospital hospital) {
        this(transferId, null, patientCount, transferStatus, patients,
             ambulanceTeam, hospital, null, null);
    }

    // Constructor used when restoring a transfer from CSV.
    public HospitalTransfer(int transferId, LocalDateTime transferTime,
                            int patientCount, String transferStatus,
                            List<Patient> patients, Hospital hospital,
                            String emergencyId) {
        this(transferId, transferTime, patientCount, transferStatus,
             patients, null, hospital, null, emergencyId);
    }

    private HospitalTransfer(int transferId, LocalDateTime transferTime,
                             int patientCount, String transferStatus,
                             List<Patient> patients,
                             AmbulanceTeam ambulanceTeam, Hospital hospital,
                             HospitalStaff hospitalStaff, String emergencyId) {
        this.transferId = transferId;
        this.transferTime = transferTime;
        this.patientCount = patientCount;
        this.transferStatus = transferStatus;
        this.patients = new ArrayList<>(patients);
        this.ambulanceTeam = ambulanceTeam;
        this.hospital = hospital;
        this.hospitalStaff = hospitalStaff;
        this.emergencyId = emergencyId;
    }

    public void recordTransfer() {
        this.transferTime = LocalDateTime.now();
    }

    public void recordConfirmation(HospitalStaff staff) {
        if (!"PENDING".equals(transferStatus)) return;
        if (staff == null || staff.getHospital() == null
                || hospital == null
                || staff.getHospital().getHospitalId() != hospital.getHospitalId()) {
            return;
        }

        if (patients.size() != patientCount) return;

        List<Integer> patientIds = new ArrayList<>();

        for (Patient patient : patients) {
            if (patient == null
                    || patientIds.contains(patient.getPatientId())) {
                return;
            }

            patientIds.add(patient.getPatientId());
        }

        if (!hospital.canReceivePatients(patientCount)) return;

        int updatedBeds = hospital.getAvailableBeds() - patientCount;
        int updatedCapacity = hospital.getEmergencyCapacity() - patientCount;

        if (emergencyId != null) {
            new HospitalRepository().updateHospitalAvailability(
                hospital.getHospitalId(),
                updatedBeds,
                updatedCapacity,
                hospital.getMedicalSpecialties()
            );
        }

        hospital.receivePatients(patientCount);
        this.hospitalStaff = staff;
        this.transferStatus = "CONFIRMED";

        if (emergencyId != null) {
            new HospitalTransferRepository().saveTransfer(this, emergencyId);
        }
    }

    public int getTransferId() { return transferId; }
    public LocalDateTime getTransferTime() { return transferTime; }
    public int getPatientCount() { return patientCount; }
    public String getTransferStatus() { return transferStatus; }
    public List<Patient> getPatients() { return new ArrayList<>(patients); }
    public Hospital getHospital() { return hospital; }
    public String getEmergencyId() { return emergencyId; }

    public void setEmergencyId(String emergencyId) {
        this.emergencyId = emergencyId;
    }
}
