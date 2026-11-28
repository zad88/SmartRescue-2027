package smartrescue.gui;

import java.time.LocalDateTime;
import java.util.List;

import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceStatus;
import smartrescue.emergency.AssistanceRecord;
import smartrescue.emergency.Emergency;
import smartrescue.emergency.EmergencyStatus;
import smartrescue.hospital.Hospital;
import smartrescue.transfer.HospitalTransfer;
import smartrescue.transfer.Patient;

public class AmbulanceConsoleVisibilityTest {

    public static void main(String[] args) {
        Ambulance current = new Ambulance(1, "A1", AmbulanceStatus.AVAILABLE, "Base");
        Ambulance sameId = new Ambulance(1, "A1", AmbulanceStatus.DISPATCHED, "Base");
        Ambulance other = new Ambulance(2, "A2", AmbulanceStatus.DISPATCHED, "Base");

        Emergency unassigned = emergency(EmergencyStatus.VERIFIED);
        check(AmbulanceConsoleFrame.isVisibleToAmbulance(unassigned, current),
                "Unassigned active incident should be visible");
        check(!AmbulanceConsoleFrame.isAssignedToAmbulance(unassigned, current),
                "Unassigned incident must not enable response actions");

        Emergency assigned = emergency(EmergencyStatus.RESPONDING);
        assigned.assignAmbulance(sameId);
        check(AmbulanceConsoleFrame.isVisibleToAmbulance(assigned, current),
                "Incident assigned to this ambulance should be visible");
        check(AmbulanceConsoleFrame.isAssignedToAmbulance(assigned, current),
                "Matching ambulance ID should allow response actions");

        assigned.assignAmbulance(other);
        check(!AmbulanceConsoleFrame.isVisibleToAmbulance(assigned, current),
                "Incident assigned to another ambulance must not be visible");
        check(!AmbulanceConsoleFrame.isAssignedToAmbulance(assigned, current),
                "Another ambulance must not enable response actions");

        for (EmergencyStatus status : new EmergencyStatus[] {
                EmergencyStatus.RESOLVED, EmergencyStatus.CLOSED }) {
            Emergency finished = emergency(status);
            check(!AmbulanceConsoleFrame.isVisibleToAmbulance(finished, current),
                    status + " unassigned incident must not be visible");
            finished.assignAmbulance(sameId);
            check(!AmbulanceConsoleFrame.isVisibleToAmbulance(finished, current),
                    status + " assigned incident must not be visible");
        }

        Hospital hospital = new Hospital(1, "Hospital", "City", 5, 5,
                List.of("Emergency", "Cardiology"));
        Hospital otherHospital = new Hospital(2, "Other", "City", 3, 3,
                List.of("emergency", "Surgery", " "));
        check(AmbulanceConsoleFrame.specialtiesFor(List.of(hospital, otherHospital))
                .equals(List.of("Cardiology", "Emergency", "Surgery")),
                "Specialty choices must come from hospitals without duplicates");
        hospital.updateAvailability(5, 5, List.of("Neurology"));
        check(AmbulanceConsoleFrame.specialtiesFor(List.of(hospital))
                .equals(List.of("Neurology")),
                "Specialty choices must reflect hospital updates");

        Emergency onScene = emergency(EmergencyStatus.ON_SCENE);
        onScene.assignAmbulance(sameId);
        check(AmbulanceConsoleFrame.transferProblem(onScene, current, hospital)
                .contains("0 of 1"), "Missing patient should be explained");
        onScene.addPatient(new Patient(1, "Patient", 20, "Injury", "Pending", "test"));
        check(AmbulanceConsoleFrame.transferProblem(onScene, current, null)
                .contains("search hospitals"), "Missing hospital should be explained");
        check(AmbulanceConsoleFrame.transferProblem(onScene, current, hospital) == null,
                "A complete incident and selected hospital should allow transfer");
        check(AmbulanceConsoleFrame.transferProblem(onScene, other, hospital)
                .contains("assign"), "Another ambulance must not transfer patients");
        check(AmbulanceConsoleFrame.resolutionProblem(onScene, current)
                .contains("Record On-Scene Assistance"),
                "Resolution must explain when assistance is missing");
        onScene.addAssistanceRecord(new AssistanceRecord(1, "Assisted", 1));
        onScene.addHospitalTransfer(new HospitalTransfer(1, 1, "PENDING",
                onScene.getPatients(), null, hospital));
        check(AmbulanceConsoleFrame.transferProblem(onScene, current, hospital)
                .contains("already"), "An existing transfer should be explained");
        check(AmbulanceConsoleFrame.resolutionProblem(onScene, current)
                .contains("must be confirmed"), "Pending transfer must block resolution");

        Emergency confirmed = emergency(EmergencyStatus.ON_SCENE);
        confirmed.assignAmbulance(sameId);
        confirmed.addHospitalTransfer(new HospitalTransfer(2, 1, "CONFIRMED",
                List.of(), null, hospital));
        check(AmbulanceConsoleFrame.resolutionProblem(confirmed, current)
                .contains("Record On-Scene Assistance"),
                "A confirmed transfer must not replace the assistance record");
        confirmed.addAssistanceRecord(new AssistanceRecord(2, "Assisted", 1));
        check(AmbulanceConsoleFrame.resolutionProblem(confirmed, current) == null,
                "Confirmed transfer and recorded assistance should allow resolution");

        System.out.println("PASS: Ambulance console visibility and transfer guidance");
    }

    private static Emergency emergency(EmergencyStatus status) {
        return new Emergency("test", "location", "description", LocalDateTime.now(),
                null, null, 1, status);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
