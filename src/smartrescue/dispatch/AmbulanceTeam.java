package smartrescue.dispatch;

import java.util.ArrayList;
import java.util.List;

import smartrescue.data.HospitalRepository;
import smartrescue.data.IdGenerator;
import smartrescue.emergency.Emergency;
import smartrescue.emergency.AssistanceRecord;
import smartrescue.emergency.EmergencyStatus;
import smartrescue.hospital.Hospital;
import smartrescue.transfer.HospitalTransfer;
import smartrescue.transfer.Patient;

public class AmbulanceTeam {

    private int teamId;
    private String driverName;
    private int medicCount;
    private String currentStatus;

    private Ambulance ambulance;
    private Hospital selectedHospital;

    private List<HospitalTransfer> hospitalTransfers;

    public AmbulanceTeam(int teamId, String driverName, int medicCount,
                         String currentStatus, Ambulance ambulance) {

        this.teamId = teamId;
        this.driverName = driverName;
        this.medicCount = medicCount;
        this.currentStatus = currentStatus;
        this.ambulance = ambulance;
        this.selectedHospital = null;
        this.hospitalTransfers = new ArrayList<>();
    }

    public String viewEmergencyDetails(Emergency emergency) {
        return emergency.getDetails();
    }

    public void updateEmergencyStatus(Emergency emergency,
                                      EmergencyStatus newStatus) {
        emergency.changeStatus(newStatus);
    }

    public boolean registerPatient(Emergency emergency, Patient patient) {

        if (emergency.getStatus() != EmergencyStatus.ON_SCENE) {
            return false;
        }

        if (emergency.getPatients().size() >= emergency.getPatientCount()) {
            return false;
        }

        emergency.addPatient(patient);

        return true;
    }

    public AssistanceRecord provideOnSceneAssistance(Emergency emergency) {

        AssistanceRecord record = new AssistanceRecord(
                IdGenerator.nextAssistanceRecordId(),
                "On-scene assistance",
                emergency.getPatientCount()
        );

        record.recordAssistance();
        emergency.addAssistanceRecord(record);

        return record;
    }

    public List<Hospital> searchHospital(String specialty, int minBeds) {

        HospitalRepository repository = new HospitalRepository();

        return repository.searchHospitals(specialty, minBeds);
    }

    public void selectHospital(Hospital hospital) {
        this.selectedHospital = hospital;
    }

    public HospitalTransfer transferPatients(Emergency emergency,
                                             Hospital hospital) {

        Hospital transferHospital = selectedHospital != null
                ? selectedHospital
                : hospital;

        HospitalTransfer transfer = new HospitalTransfer(
                IdGenerator.nextHospitalTransferId(),
                emergency.getPatientCount(),
                "PENDING",
                emergency.getPatients(),
                this,
                transferHospital
        );

        transfer.recordTransfer();

        hospitalTransfers.add(transfer);
        emergency.addHospitalTransfer(transfer);

        selectedHospital = null;

        return transfer;
    }

    public void resolveEmergency(Emergency emergency) {

        if (emergency.getStatus() != EmergencyStatus.ON_SCENE) {
            return;
        }

        if (emergency.getAssistanceRecords().isEmpty()) {
            return;
        }

        for (HospitalTransfer transfer : emergency.getHospitalTransfers()) {
            if (!"CONFIRMED".equals(transfer.getTransferStatus())) {
                return;
            }
        }

        emergency.changeStatus(EmergencyStatus.RESOLVED);
    }
}