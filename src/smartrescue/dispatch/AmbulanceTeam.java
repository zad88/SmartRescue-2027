package smartrescue.dispatch;

import java.util.ArrayList;
import java.util.List;

import smartrescue.emergency.Emergency;
import smartrescue.emergency.AssistanceRecord;
import smartrescue.emergency.EmergencyStatus;
import smartrescue.hospital.Hospital;
import smartrescue.transfer.HospitalTransfer;

public class AmbulanceTeam {

    private int teamId;
    private String driverName;
    private int medicCount;
    private String currentStatus;

    private Ambulance ambulance;
    private List<HospitalTransfer> hospitalTransfers;

    private static int nextRecordId = 1;
    private static int nextTransferId = 1;

    public AmbulanceTeam(int teamId, String driverName, int medicCount,
                         String currentStatus, Ambulance ambulance) {

        this.teamId = teamId;
        this.driverName = driverName;
        this.medicCount = medicCount;
        this.currentStatus = currentStatus;
        this.ambulance = ambulance;
        this.hospitalTransfers = new ArrayList<>();
    }

    public String viewEmergencyDetails(Emergency emergency) {
        return emergency.getDetails();
    }

    public void updateEmergencyStatus(Emergency emergency,
                                      EmergencyStatus newStatus) {
        emergency.changeStatus(newStatus);
    }

    public AssistanceRecord provideOnSceneAssistance(Emergency emergency) {

        AssistanceRecord record = new AssistanceRecord(
                nextRecordId,
                "On-scene assistance",
                emergency.getPatientCount()
        );

        nextRecordId++;
        record.recordAssistance();

        return record;
    }

    // Data layer connection will be implemented later

    public List<Hospital> searchHospital(String specialty, int minBeds) {
        return new ArrayList<>();
    }

    public void selectHospital(Hospital hospital) {
        // The hospital is passed directly to transferPatients.
    }

    public HospitalTransfer transferPatients(Emergency emergency,
                                             Hospital hospital) {

        HospitalTransfer transfer = new HospitalTransfer(
                nextTransferId,
                emergency.getPatientCount(),
                "",
                emergency.getPatients(),
                this,
                hospital
        );

        nextTransferId++;
        transfer.recordTransfer();
        hospitalTransfers.add(transfer);

        return transfer;
    }

    public void resolveEmergency(Emergency emergency) {
        emergency.changeStatus(EmergencyStatus.RESOLVED);
    }

}