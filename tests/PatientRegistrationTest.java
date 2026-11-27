import java.util.Arrays;

import smartrescue.dispatch.*;
import smartrescue.emergency.*;
import smartrescue.hospital.Hospital;
import smartrescue.transfer.Patient;
import smartrescue.transfer.HospitalTransfer;

public class PatientRegistrationTest {

    public static void main(String[] args) {

        Ambulance ambulance = new Ambulance(
            1, "ABC-123", AmbulanceStatus.AVAILABLE, "Cairo"
        );

        AmbulanceTeam team = new AmbulanceTeam(
            1, "Ahmed", 2, "Available", ambulance
        );

        Emergency emergency = new Emergency(
            "Cairo", "Car accident", 2
        );

        Patient patient1 = new Patient(
            1, "Patient One", 25, "Injury", "Under Treatment"
        );

        Patient patient2 = new Patient(
            2, "Patient Two", 30, "Fracture", "Under Treatment"
        );

        Patient patient3 = new Patient(
            3, "Patient Three", 40, "Injury", "Under Treatment"
        );

        System.out.println("Before ON_SCENE: "
            + team.registerPatient(emergency, patient1));

        emergency.changeStatus(EmergencyStatus.VERIFIED);
        emergency.changeStatus(EmergencyStatus.RESPONDING);
        emergency.changeStatus(EmergencyStatus.ON_SCENE);

        System.out.println("Register patient 1: "
            + team.registerPatient(emergency, patient1));

        System.out.println("Register patient 2: "
            + team.registerPatient(emergency, patient2));

        System.out.println("Register patient 3: "
            + team.registerPatient(emergency, patient3));

        System.out.println("Registered patients: "
            + emergency.getPatients().size());

        Hospital hospital = new Hospital(
            1, "City Hospital", "Cairo",
            10, 10, Arrays.asList("Emergency")
        );

        team.selectHospital(hospital);

        HospitalTransfer transfer =
            team.transferPatients(emergency, hospital);

        System.out.println("Transfer status: "
            + transfer.getTransferStatus());

        System.out.println("Patients in transfer: "
            + transfer.getPatients().size());
    }
}