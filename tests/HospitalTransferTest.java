import java.util.Arrays;
import java.util.List;

import smartrescue.dispatch.*;
import smartrescue.data.HospitalRepository;
import smartrescue.hospital.*;
import smartrescue.emergency.*;
import smartrescue.transfer.*;

public class HospitalTransferTest {

    public static void main(String[] args) {

        HospitalRepository repository = new HospitalRepository();

        List<Hospital> availableHospitals =
                repository.searchHospitals("Emergency", 3);

        if (availableHospitals.isEmpty()) {
            throw new IllegalStateException(
                "No suitable hospital was found in hospitals.csv."
            );
        }

        Hospital hospital = availableHospitals.get(0);

        int originalBeds = hospital.getAvailableBeds();
        int originalCapacity = hospital.getEmergencyCapacity();
        List<String> originalSpecialties =
                hospital.getMedicalSpecialties();

        try {
            Ambulance ambulance = new Ambulance(
                1, "ABC-123", AmbulanceStatus.AVAILABLE, "Cairo"
            );

            AmbulanceTeam team = new AmbulanceTeam(
                1, "Ahmed", 2, "Available", ambulance
            );

            Emergency emergency = new Emergency(
                "Cairo", "Car accident", 3
            );

            HospitalTransfer transfer =
                    team.transferPatients(emergency, hospital);

            System.out.println("Transfer status: "
                    + transfer.getTransferStatus());

            System.out.println("Beds before confirmation: "
                    + hospital.getAvailableBeds());

            System.out.println("Capacity before confirmation: "
                    + hospital.getEmergencyCapacity());

            HospitalStaff staff = new HospitalStaff(
                1, "Ali", "Doctor", "Emergency", hospital
            );

            staff.confirmPatientTransfer(transfer);

            System.out.println("\nAfter confirmation:");

            System.out.println("Transfer status: "
                    + transfer.getTransferStatus());

            System.out.println("Beds in memory: "
                    + hospital.getAvailableBeds());

            System.out.println("Capacity in memory: "
                    + hospital.getEmergencyCapacity());

            List<Hospital> savedHospitals =
                    repository.searchHospitals("Emergency", 0);

            Hospital savedHospital = savedHospitals.stream()
                    .filter(h -> h.getHospitalId() == hospital.getHospitalId())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                        "Hospital was not found after saving."
                    ));

            System.out.println("\nValues loaded again from CSV:");

            System.out.println("Beds saved: "
                    + savedHospital.getAvailableBeds());

            System.out.println("Capacity saved: "
                    + savedHospital.getEmergencyCapacity());

            if (savedHospital.getAvailableBeds()
                    != originalBeds - 3
                    || savedHospital.getEmergencyCapacity()
                    != originalCapacity - 3) {
                throw new IllegalStateException(
                    "Hospital capacity was not persisted correctly."
                );
            }

            staff.confirmPatientTransfer(transfer);

            if (hospital.getAvailableBeds() != originalBeds - 3
                    || hospital.getEmergencyCapacity()
                    != originalCapacity - 3) {
                throw new IllegalStateException(
                    "Second confirmation deducted capacity again."
                );
            }

            System.out.println(
                "\nSecond confirmation did not deduct capacity again."
            );

            System.out.println("HOSPITAL TRANSFER TEST PASSED");

        } finally {
            repository.updateHospitalAvailability(
                hospital.getHospitalId(),
                originalBeds,
                originalCapacity,
                originalSpecialties
            );

            hospital.updateAvailability(
                originalBeds,
                originalCapacity,
                originalSpecialties
            );

            System.out.println("Original hospital data restored.");
        }
    }
}
