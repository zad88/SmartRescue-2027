package smartrescue.dispatch;

import java.util.ArrayList;
import java.util.List;

import smartrescue.data.AssistanceRecordRepository;
import smartrescue.data.EmergencyRepository;
import smartrescue.data.HospitalRepository;
import smartrescue.data.HospitalTransferRepository;
import smartrescue.data.IdGenerator;
import smartrescue.data.PatientRepository;
import smartrescue.emergency.AssistanceRecord;
import smartrescue.emergency.Emergency;
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
        new EmergencyRepository().saveEmergency(emergency);
    }

    public boolean registerPatient(Emergency emergency, Patient patient) {
        if (emergency == null || patient == null) {
            return false;
        }

        if (emergency.getStatus() != EmergencyStatus.ON_SCENE) {
            return false;
        }

        List<Patient> registeredPatients = emergency.getPatients();

        if (registeredPatients.size() >= emergency.getPatientCount()) {
            return false;
        }

        for (Patient registeredPatient : registeredPatients) {
            if (registeredPatient == null
                    || registeredPatient.getPatientId() == patient.getPatientId()) {
                return false;
            }
        }

        if (patient.getEmergencyId() != null
                && !patient.getEmergencyId().equals(emergency.getIncidentId())) {
            return false;
        }

        new PatientRepository().savePatient(
            patient, emergency.getIncidentId()
        );

        emergency.addPatient(patient);

        return true;
    }

    public AssistanceRecord provideOnSceneAssistance(Emergency emergency) {
        return provideOnSceneAssistance(emergency, "On-scene assistance",
                emergency.getPatientCount());
    }

    public AssistanceRecord provideOnSceneAssistance(Emergency emergency,
                                                       String description,
                                                       int patientsTreated) {
        if (emergency == null || emergency.getStatus() != EmergencyStatus.ON_SCENE) {
            throw new IllegalArgumentException("The emergency must be on scene.");
        }
        if (description == null || description.isBlank()
                || patientsTreated < 0 || patientsTreated > emergency.getPatientCount()) {
            throw new IllegalArgumentException("Enter assistance details and a valid patient count.");
        }
        AssistanceRecord record = new AssistanceRecord(
            IdGenerator.nextAssistanceRecordId(),
            description,
            null,
            patientsTreated,
            teamId
        );

        record.recordAssistance();
        emergency.addAssistanceRecord(record);

        new AssistanceRecordRepository().saveAssistanceRecord(
            record, emergency.getIncidentId()
        );

        return record;
    }

    public int getTeamId() {
        return teamId;
    }

    public List<Hospital> searchHospital(String specialty, int minBeds) {
        return new HospitalRepository().searchHospitals(specialty, minBeds);
    }

    public void selectHospital(Hospital hospital) {
        this.selectedHospital = hospital;
    }

    public HospitalTransfer transferPatients(Emergency emergency,
                                             Hospital hospital) {
        if (emergency == null) {
            throw new IllegalArgumentException(
                "Emergency is required."
            );
        }

        if (emergency.getStatus() != EmergencyStatus.ON_SCENE) {
            throw new IllegalStateException(
                "Patients can only be transferred when the emergency is on scene."
            );
        }

        if (!emergency.getHospitalTransfers().isEmpty()) {
            throw new IllegalStateException(
                "A patient transfer has already been created for this emergency."
            );
        }

        List<Patient> registeredPatients = emergency.getPatients();

        if (registeredPatients.size() != emergency.getPatientCount()) {
            throw new IllegalStateException(
                "Transfer blocked: the emergency reports "
                + emergency.getPatientCount()
                + " patients, but only "
                + registeredPatients.size()
                + " patient records are registered."
            );
        }

        List<Integer> registeredPatientIds = new ArrayList<>();

        for (Patient patient : registeredPatients) {
            if (patient == null
                    || patient.getEmergencyId() == null
                    || !patient.getEmergencyId().equals(emergency.getIncidentId())
                    || registeredPatientIds.contains(patient.getPatientId())) {
                throw new IllegalStateException(
                    "Transfer blocked: patient records are missing, duplicated, "
                    + "or linked to a different emergency."
                );
            }

            registeredPatientIds.add(patient.getPatientId());
        }

        Hospital transferHospital = selectedHospital != null
                ? selectedHospital : hospital;

        HospitalTransfer transfer = new HospitalTransfer(
            IdGenerator.nextHospitalTransferId(),
            emergency.getPatientCount(),
            "PENDING",
            registeredPatients,
            this,
            transferHospital
        );

        transfer.recordTransfer();
        transfer.setEmergencyId(emergency.getIncidentId());

        hospitalTransfers.add(transfer);
        emergency.addHospitalTransfer(transfer);

        new HospitalTransferRepository().saveTransfer(
            transfer, emergency.getIncidentId()
        );

        selectedHospital = null;
        return transfer;
    }

    public void resolveEmergency(Emergency emergency) {
        if (emergency.getStatus() != EmergencyStatus.ON_SCENE) return;
        if (emergency.getAssistanceRecords().isEmpty()) return;

        for (HospitalTransfer transfer : emergency.getHospitalTransfers()) {
            if (!"CONFIRMED".equals(transfer.getTransferStatus())) {
                return;
            }
        }

        emergency.changeStatus(EmergencyStatus.RESOLVED);
        new EmergencyRepository().saveEmergency(emergency);

        if (emergency.getAmbulance() != null
                && new AmbulanceReleaseService().releaseAmbulance(
                        emergency.getAmbulance().getAmbulanceId())
                && ambulance.getAmbulanceId()
                        == emergency.getAmbulance().getAmbulanceId()) {
            ambulance.release();
        }
    }
}
