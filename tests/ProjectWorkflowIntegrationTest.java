import java.nio.file.Files;
import java.nio.file.Path;

import smartrescue.data.AmbulanceRepository;
import smartrescue.data.DataChangeTracker;
import smartrescue.data.EmergencyRepository;
import smartrescue.data.HospitalRepository;
import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceStatus;
import smartrescue.dispatch.AmbulanceTeam;
import smartrescue.dispatch.Operator;
import smartrescue.emergency.AssistanceRecord;
import smartrescue.emergency.Emergency;
import smartrescue.emergency.EmergencyStatus;
import smartrescue.emergency.Severity;
import smartrescue.emergency.Witness;
import smartrescue.hospital.Hospital;
import smartrescue.hospital.HospitalStaff;
import smartrescue.service.PortalService;
import smartrescue.transfer.HospitalTransfer;
import smartrescue.transfer.Patient;

public class ProjectWorkflowIntegrationTest {
    public static void main(String[] args) throws Exception {
        Path data = Path.of("data");
        if (Files.exists(data)) {
            throw new IllegalStateException("Run this test in an empty working directory.");
        }
        Files.createDirectory(data);
        write("ambulances.csv", "ambulanceId,licensePlate,status,currentLocation\n"
                + "1,A1,AVAILABLE,Base\n");
        write("hospitals.csv", "hospitalId,name,location,availableBeds,emergencyCapacity,medicalSpecialties\n"
                + "1,Hospital,City,5,5,Emergency\n"
                + "2,City Hospital,Giza,20,15,Cardiology;Emergency;Neurology;Orthopedics;Pediatrics;Surgery\n");
        write("witnesses.csv", "witnessId,fullName,phoneNumber,currentLocation\n"
                + "1,Witness,000,City\n");
        write("operators.csv", "operatorId,fullName,shiftTime,stationId\n"
                + "1,Operator,Day,1\n");
        write("ambulance_teams.csv", "teamId,ambulanceId,driverName,medicCount,currentStatus\n"
                + "21,1,Driver,2,On Duty\n");
        write("hospital_staff.csv", "staffId,hospitalId,fullName,role,department\n"
                + "11,1,Staff,Doctor,Emergency\n");
        write("emergencies.csv", "incidentId,location,description,reportedAt,type,severity,patientCount,status,ambulanceId\n"
                + "legacy,City,Old,2026-01-01T10:00,Emergency,HIGH,0,CLOSED,\n");
        write("assistance_records.csv", "recordId,emergencyId,description,recordedAt,patientsTreated\n"
                + "90,legacy,Old,2026-01-01T10:00,0\n");
        write("hospital_transfers.csv", "transferId,emergencyId,transferTime,patientCount,transferStatus,hospitalId,patientIds\n"
                + "99,legacy,2026-01-01T10:00,0,CONFIRMED,1,\n");

        EmergencyRepository emergencies = new EmergencyRepository();
        Emergency legacy = emergencies.findEmergencyById("legacy");
        check(legacy != null && legacy.getWitnessId() == null && legacy.getOperatorId() == null,
                "Old incidents must migrate without inventing actor links");
        check(legacy.getAssistanceRecords().get(0).getTeamId() == null,
                "Old assistance must not gain a fabricated team");
        check(legacy.getHospitalTransfers().get(0).getTeamId() == null
                && legacy.getHospitalTransfers().get(0).getStaffId() == null,
                "Old transfers must not gain fabricated actor links");

        PortalService service = new PortalService();
        Witness witness = service.loadWitness();
        Operator operator = service.loadOperator();
        Ambulance ambulance = new AmbulanceRepository().findAmbulanceById(1);
        AmbulanceTeam team = service.loadTeam(ambulance);
        Hospital hospital = new HospitalRepository().findHospitalById(1);
        HospitalStaff staff = service.loadStaff(hospital);
        staff.updateHospitalInfo(hospital, 4, 3, java.util.List.of("Emergency", "Surgery"));
        check(new HospitalRepository().findHospitalById(1).getAvailableBeds() == 4,
                "Hospital update must persist");
        check(team.searchHospital("Emergency", 4).stream()
                .anyMatch(result -> result.getHospitalId() == 1),
                "Updated hospital must be searchable by beds and specialty");
        check(team.searchHospital("Neurology", 10).stream()
                .anyMatch(result -> result.getHospitalId() == 2),
                "Giza hospital must be searchable with its expanded capacity and specialties");
        try {
            staff.updateHospitalInfo(hospital, -1, 3, java.util.List.of("Emergency"));
            throw new AssertionError("Invalid hospital availability was accepted");
        } catch (IllegalArgumentException expected) {
            // Invalid updates must be rejected.
        }
        try {
            staff.updateHospitalInfo(new Hospital(2, "Other", "City", 2, 2,
                    java.util.List.of("Emergency")), 2, 2, java.util.List.of("Emergency"));
            throw new AssertionError("Staff updated a different hospital");
        } catch (IllegalArgumentException expected) {
            // Staff may update only their own hospital.
        }

        DataChangeTracker tracker = new DataChangeTracker();
        check(!tracker.hasChanged(), "Initial data check should establish a baseline");
        Emergency incident = witness.reportEmergency("City", "First line\nSecond line", 2);
        check(tracker.hasChanged(), "A saved incident must be detected by portal refresh");
        check(!tracker.hasChanged(), "Unchanged data must not trigger another refresh");
        operator.classifyEmergency(incident, "Emergency", Severity.HIGH);
        check(operator.reviewAndVerifyEmergency(incident), "Verification failed");
        operator.assignAmbulance(incident, ambulance);
        check(new AmbulanceRepository().findAmbulanceById(1).getStatus()
                == AmbulanceStatus.DISPATCHED, "Dispatch status must persist");
        team.updateEmergencyStatus(incident, EmergencyStatus.ON_SCENE);
        AssistanceRecord assistance = team.provideOnSceneAssistance(incident, "Bandaged wound", 2);
        check(team.registerPatient(incident, new Patient(7, "Patient", 20,
                "Injury", "Treated")), "Patient registration failed");
        try {
            team.transferPatients(incident, hospital);
            throw new AssertionError("A transfer with missing patients was accepted");
        } catch (IllegalStateException expected) {
            // All reported patients must be registered before transfer.
        }
        check(team.registerPatient(incident, new Patient(8, "Patient 2", 30,
                "Injury", "Treated")), "Second patient registration failed");
        check(!team.registerPatient(incident, new Patient(9, "Extra", 20,
                "Injury", "Treated")), "The patient count must be enforced");
        team.selectHospital(hospital);
        HospitalTransfer transfer = team.transferPatients(incident,
                new Hospital(2, "Other", "City", 2, 2, java.util.List.of("Emergency")));
        check(transfer.getHospital().getHospitalId() == 1,
                "The selected hospital must be used for transfer");
        team.resolveEmergency(incident);
        check(incident.getStatus() == EmergencyStatus.ON_SCENE,
                "An unconfirmed transfer must prevent resolution");
        HospitalTransfer staleTransfer = emergencies.findEmergencyById(incident.getIncidentId())
                .getHospitalTransfers().get(0);
        HospitalStaff otherStaff = new HospitalStaff(12, "Other", "Doctor", "Emergency",
                new Hospital(2, "Other", "City", 2, 2, java.util.List.of("Emergency")));
        otherStaff.confirmPatientTransfer(transfer);
        check("PENDING".equals(transfer.getTransferStatus()),
                "Staff from another hospital must not confirm the transfer");
        staff.confirmPatientTransfer(transfer);
        check("CONFIRMED".equals(transfer.getTransferStatus()), "Transfer was not confirmed");
        Emergency withoutAssistance = new Emergency("unassisted", "City", "Unassisted",
                java.time.LocalDateTime.now(), "Emergency", Severity.HIGH, 2,
                EmergencyStatus.ON_SCENE);
        withoutAssistance.addHospitalTransfer(transfer);
        team.resolveEmergency(withoutAssistance);
        check(withoutAssistance.getStatus() == EmergencyStatus.ON_SCENE,
                "A confirmed transfer must not allow resolution without assistance");
        check("CONFIRMED".equals(service.findEmergencyById(incident.getIncidentId())
                .getHospitalTransfers().get(0).getTransferStatus()),
                "Refreshing an incident must load the confirmed transfer");
        staff.confirmPatientTransfer(transfer);
        try {
            staff.confirmPatientTransfer(staleTransfer);
            throw new AssertionError("A stale transfer was confirmed twice");
        } catch (IllegalStateException expected) {
            // A stale confirmation must not deduct hospital capacity again.
        }

        Emergency loaded = emergencies.findEmergencyById(incident.getIncidentId());
        check(Integer.valueOf(1).equals(loaded.getWitnessId())
                && Integer.valueOf(1).equals(loaded.getOperatorId()),
                "Incident actor links must persist");
        check("First line\nSecond line".equals(loaded.getDescription()),
                "Multiline incident description must persist");
        check(loaded.getAssistanceRecords().get(0).getRecordId() == assistance.getRecordId()
                && Integer.valueOf(21).equals(loaded.getAssistanceRecords().get(0).getTeamId())
                && "Bandaged wound".equals(loaded.getAssistanceRecords().get(0).getDescription()),
                "Assistance details and team link must persist");
        HospitalTransfer savedTransfer = loaded.getHospitalTransfers().get(0);
        check("CONFIRMED".equals(savedTransfer.getTransferStatus())
                && Integer.valueOf(21).equals(savedTransfer.getTeamId())
                && Integer.valueOf(11).equals(savedTransfer.getStaffId())
                && savedTransfer.getPatients().size() == 2,
                "Transfer status, team and confirming staff must persist");
        check(new HospitalRepository().findHospitalById(1).getAvailableBeds() == 2,
                "Transfer confirmation must deduct beds only once");
        loaded.getPatients().get(0).updateTreatmentStatus("Recovered");
        check("Recovered".equals(emergencies.findEmergencyById(incident.getIncidentId())
                .getPatients().get(0).getTreatmentStatus()), "Patient updates must persist");
        team.resolveEmergency(loaded);
        check(emergencies.findEmergencyById(incident.getIncidentId()).getStatus()
                == EmergencyStatus.RESOLVED, "Resolution must persist");
        check(new AmbulanceRepository().findAmbulanceById(1).checkAvailability(),
                "Ambulance must be released on resolution");
        operator.closeEmergency(loaded);
        check(emergencies.findEmergencyById(incident.getIncidentId()).getStatus()
                == EmergencyStatus.CLOSED, "Closure must persist");
        System.out.println("PASS: Full workflow, actor links, migration, and update detection");
    }

    private static void write(String name, String content) throws Exception {
        Files.writeString(Path.of("data", name), content);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
