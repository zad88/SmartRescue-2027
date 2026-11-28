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
    private Integer teamId;
    private Integer staffId;

    public HospitalTransfer(int transferId, int patientCount,
                            String transferStatus, List<Patient> patients,
                            AmbulanceTeam ambulanceTeam, Hospital hospital) {
        this(transferId, null, patientCount, transferStatus, patients,
             ambulanceTeam, hospital, null, null);
        this.teamId = ambulanceTeam == null ? null : ambulanceTeam.getTeamId();
    }

    // Constructor used when restoring a transfer from CSV.
    public HospitalTransfer(int transferId, LocalDateTime transferTime,
                            int patientCount, String transferStatus,
                            List<Patient> patients, Hospital hospital,
                            String emergencyId) {
        this(transferId, transferTime, patientCount, transferStatus,
                patients, hospital, emergencyId, null, null);
    }

    public HospitalTransfer(int transferId, LocalDateTime transferTime,
                            int patientCount, String transferStatus,
                            List<Patient> patients, Hospital hospital,
                            String emergencyId, Integer teamId, Integer staffId) {
        this(transferId, transferTime, patientCount, transferStatus,
             patients, null, hospital, null, emergencyId);
        this.teamId = teamId;
        this.staffId = staffId;
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

        if (emergencyId != null) {
            HospitalTransfer current = null;
            for (HospitalTransfer candidate : new HospitalTransferRepository()
                    .loadByEmergencyId(emergencyId)) {
                if (candidate.getTransferId() == transferId) {
                    current = candidate;
                    break;
                }
            }
            if (current == null || !"PENDING".equals(current.getTransferStatus())) {
                throw new IllegalStateException(
                        "This transfer has changed or was already confirmed. Refresh and try again.");
            }
            if (current.getHospital() == null
                    || current.getHospital().getHospitalId() != hospital.getHospitalId()
                    || current.getPatientCount() != patientCount) {
                throw new IllegalStateException("Transfer details changed. Refresh and try again.");
            }
            patients = current.getPatients();
            hospital.updateAvailability(current.getHospital().getAvailableBeds(),
                    current.getHospital().getEmergencyCapacity(),
                    current.getHospital().getMedicalSpecialties());
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
        this.staffId = staff.getStaffId();
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
    public Integer getTeamId() { return teamId; }
    public Integer getStaffId() { return staffId; }

    public void setEmergencyId(String emergencyId) {
        this.emergencyId = emergencyId;
    }
}
