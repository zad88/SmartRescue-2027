import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import smartrescue.data.EmergencyRepository;
import smartrescue.data.HospitalRepository;
import smartrescue.emergency.Emergency;
import smartrescue.emergency.EmergencyStatus;
import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceStatus;
import smartrescue.dispatch.AmbulanceTeam;
import smartrescue.hospital.Hospital;
import smartrescue.hospital.HospitalStaff;
import smartrescue.transfer.Patient;
import smartrescue.transfer.HospitalTransfer;

public class RelatedRecordsPersistenceTest {

    private static final List<Path> FILES = List.of(
        Path.of("data", "emergencies.csv"),
        Path.of("data", "patients.csv"),
        Path.of("data", "assistance_records.csv"),
        Path.of("data", "hospital_transfers.csv"),
        Path.of("data", "hospitals.csv"),
        Path.of("data", "id_counters.csv")
    );

    public static void main(String[] args) throws Exception {

        Map<Path, byte[]> backups = new LinkedHashMap<>();
        Map<Path, Boolean> existed = new LinkedHashMap<>();

        for (Path file : FILES) {
            existed.put(file, Files.exists(file));

            if (Files.exists(file)) {
                backups.put(file, Files.readAllBytes(file));
            }
        }

        try {
            runPersistenceTest();
            System.out.println("\nALL RELATED RECORDS PERSISTENCE TESTS PASSED");

        } finally {
            for (Path file : FILES) {
                if (Boolean.TRUE.equals(existed.get(file))) {
                    Files.write(file, backups.get(file));
                } else {
                    Files.deleteIfExists(file);
                }
            }

            System.out.println("Original CSV data restored.");
        }
    }

    private static void runPersistenceTest() {

        Emergency emergency = new Emergency(
            "Cairo",
            "Persistence integration test",
            2
        );

        emergency.changeStatus(EmergencyStatus.VERIFIED);
        emergency.changeStatus(EmergencyStatus.RESPONDING);
        emergency.changeStatus(EmergencyStatus.ON_SCENE);

        Ambulance ambulance = new Ambulance(
            900, "TEST-900", AmbulanceStatus.AVAILABLE, "Cairo"
        );

        AmbulanceTeam team = new AmbulanceTeam(
            900, "Test Driver", 2, "Available", ambulance
        );

        Patient patient1 = new Patient(
            9001, "Test Patient One", 25,
            "Injury", "Under Treatment"
        );

        Patient patient2 = new Patient(
            9002, "Test Patient Two", 30,
            "Fracture", "Under Treatment"
        );

        if (!team.registerPatient(emergency, patient1)
                || !team.registerPatient(emergency, patient2)) {
            throw new IllegalStateException("Patient registration failed.");
        }

        System.out.println("PASS: Patients registered and saved.");

        team.provideOnSceneAssistance(emergency);

        if (emergency.getAssistanceRecords().size() != 1) {
            throw new IllegalStateException("Assistance record was not created.");
        }

        System.out.println("PASS: Assistance record created and saved.");

        Hospital hospital =
            new HospitalRepository().findHospitalById(1);

        if (hospital == null) {
            throw new IllegalStateException("Test hospital was not found.");
        }

        int originalBeds = hospital.getAvailableBeds();
        int originalCapacity = hospital.getEmergencyCapacity();

        HospitalTransfer transfer =
            team.transferPatients(emergency, hospital);

        if (!"PENDING".equals(transfer.getTransferStatus())) {
            throw new IllegalStateException("Transfer was not PENDING.");
        }

        System.out.println("PASS: Hospital transfer created and saved.");

        HospitalStaff staff = new HospitalStaff(
            900, "Test Staff", "Doctor", "Emergency", hospital
        );

        staff.confirmPatientTransfer(transfer);

        if (!"CONFIRMED".equals(transfer.getTransferStatus())) {
            throw new IllegalStateException("Transfer confirmation failed.");
        }

        int bedsAfterConfirmation = hospital.getAvailableBeds();
        int capacityAfterConfirmation = hospital.getEmergencyCapacity();

        if (bedsAfterConfirmation != originalBeds - 2
                || capacityAfterConfirmation != originalCapacity - 2) {
            throw new IllegalStateException(
                "Hospital capacity was not updated correctly."
            );
        }

        System.out.println("PASS: Transfer confirmation persisted.");

        EmergencyRepository repository = new EmergencyRepository();
        repository.saveEmergency(emergency);

        Emergency loaded =
            repository.findEmergencyById(emergency.getIncidentId());

        if (loaded == null) {
            throw new IllegalStateException("Emergency was not reloaded.");
        }

        if (loaded.getPatients().size() != 2) {
            throw new IllegalStateException("Patients were not restored.");
        }

        if (loaded.getAssistanceRecords().size() != 1) {
            throw new IllegalStateException(
                "Assistance records were not restored."
            );
        }

        if (loaded.getHospitalTransfers().size() != 1) {
            throw new IllegalStateException(
                "Hospital transfers were not restored."
            );
        }

        System.out.println("PASS: All three record types restored.");

        HospitalTransfer loadedTransfer =
            loaded.getHospitalTransfers().get(0);

        if (!"CONFIRMED".equals(loadedTransfer.getTransferStatus())) {
            throw new IllegalStateException(
                "Restored transfer has an incorrect status."
            );
        }

        if (loadedTransfer.getHospital() == null
                || loadedTransfer.getHospital().getHospitalId() != 1) {
            throw new IllegalStateException(
                "Transfer hospital relationship was not restored."
            );
        }

        if (loadedTransfer.getPatients().size() != 2) {
            throw new IllegalStateException(
                "Transfer patient relationships were not restored."
            );
        }

        System.out.println(
            "PASS: Transfer hospital and patient relationships restored."
        );

        loaded.getPatients().get(0).updateTreatmentStatus("Treated");

        Emergency reloaded =
            repository.findEmergencyById(emergency.getIncidentId());

        if (!"Treated".equals(
                reloaded.getPatients().get(0).getTreatmentStatus())) {
            throw new IllegalStateException(
                "Updated patient treatment status was not persisted."
            );
        }

        System.out.println("PASS: Patient treatment update persisted.");

        Hospital savedHospital =
            new HospitalRepository().findHospitalById(1);

        if (savedHospital.getAvailableBeds() != bedsAfterConfirmation
                || savedHospital.getEmergencyCapacity()
                    != capacityAfterConfirmation) {
            throw new IllegalStateException(
                "Hospital capacity was not saved correctly."
            );
        }

        System.out.println("PASS: Hospital capacity persisted.");
    }
}
