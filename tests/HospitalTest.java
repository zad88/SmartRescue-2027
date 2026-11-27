import java.util.Arrays;
import smartrescue.hospital.Hospital;

public class HospitalTest {

    public static void main(String[] args) {

        Hospital hospital = new Hospital(
            1,
            "City Hospital",
            "Cairo",
            10,
            5,
            Arrays.asList("Cardiology", "Emergency")
        );

        System.out.println("Initial beds: " + hospital.getAvailableBeds());
        System.out.println("Initial capacity: " + hospital.getEmergencyCapacity());

        System.out.println("\nReceiving 3 patients...");
        hospital.receivePatients(3);

        System.out.println("Beds after admission: " + hospital.getAvailableBeds());
        System.out.println("Capacity after admission: " + hospital.getEmergencyCapacity());

        System.out.println("\nTrying to receive 6 patients...");
        hospital.receivePatients(6);

        System.out.println("Beds after rejected admission: " + hospital.getAvailableBeds());
        System.out.println("Capacity after rejected admission: " + hospital.getEmergencyCapacity());
    }
}