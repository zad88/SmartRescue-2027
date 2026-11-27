import java.util.List;

import smartrescue.data.HospitalRepository;
import smartrescue.hospital.Hospital;

public class HospitalRepositoryTest {

    public static void main(String[] args) {

        HospitalRepository repository = new HospitalRepository();

        System.out.println("Test 1: Emergency hospitals with at least 10 beds");

        List<Hospital> hospitals =
                repository.searchHospitals("Emergency", 10);

        System.out.println("Matching hospitals: " + hospitals.size());

        for (Hospital hospital : hospitals) {
            System.out.println(
                "Beds: " + hospital.getAvailableBeds()
                + ", Capacity: " + hospital.getEmergencyCapacity()
                + ", Specialties: " + hospital.getMedicalSpecialties()
            );
        }

        System.out.println("\nTest 2: Cardiology hospitals with at least 15 beds");

        List<Hospital> cardiologyHospitals =
                repository.searchHospitals("Cardiology", 15);

        System.out.println(
            "Matching hospitals: " + cardiologyHospitals.size()
        );
    }
}