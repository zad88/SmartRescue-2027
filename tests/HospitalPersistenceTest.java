import java.util.List;

import smartrescue.data.HospitalRepository;
import smartrescue.hospital.Hospital;
import smartrescue.hospital.HospitalStaff;

public class HospitalPersistenceTest {

    public static void main(String[] args) {

        if (args.length == 0) {
            throw new IllegalArgumentException(
                "Provide update, verify, or restore."
            );
        }

        HospitalRepository repository = new HospitalRepository();

        switch (args[0]) {

            case "update":

                Hospital hospital = new Hospital(
                    1,
                    "Cairo Hospital",
                    "Cairo",
                    20,
                    15,
                    List.of("Cardiology", "Emergency")
                );

                HospitalStaff staff = new HospitalStaff(
                    1,
                    "Test Staff",
                    "Administrator",
                    "Emergency",
                    hospital
                );

                staff.updateHospitalInfo(
                    hospital,
                    30,
                    25,
                    List.of("Cardiology", "Emergency", "Neurology")
                );

                System.out.println("Hospital updated.");
                System.out.println("Beds in memory: "
                    + hospital.getAvailableBeds());
                break;

            case "verify":

                List<Hospital> hospitals =
                    repository.searchHospitals("Neurology", 30);

                if (hospitals.size() != 1) {
                    throw new IllegalStateException(
                        "Updated hospital was not found."
                    );
                }

                Hospital savedHospital = hospitals.get(0);

                System.out.println("Hospital found after restart.");
                System.out.println("Hospital ID: "
                    + savedHospital.getHospitalId());
                System.out.println("Available beds: "
                    + savedHospital.getAvailableBeds());
                System.out.println("Emergency capacity: "
                    + savedHospital.getEmergencyCapacity());
                System.out.println("Specialties: "
                    + savedHospital.getMedicalSpecialties());
                break;

            case "restore":

                Hospital originalHospital = new Hospital(
                    1,
                    "Cairo Hospital",
                    "Cairo",
                    30,
                    25,
                    List.of("Cardiology", "Emergency", "Neurology")
                );

                HospitalStaff originalStaff = new HospitalStaff(
                    1,
                    "Test Staff",
                    "Administrator",
                    "Emergency",
                    originalHospital
                );

                originalStaff.updateHospitalInfo(
                    originalHospital,
                    20,
                    15,
                    List.of("Cardiology", "Emergency")
                );

                System.out.println("Original hospital data restored.");
                break;

            default:
                throw new IllegalArgumentException(
                    "Unknown test mode: " + args[0]
                );
        }
    }
}