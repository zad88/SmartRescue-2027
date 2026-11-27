import smartrescue.Person;
import smartrescue.emergency.Witness;
import smartrescue.dispatch.Operator;
import smartrescue.hospital.Hospital;
import smartrescue.hospital.HospitalStaff;
import java.util.ArrayList;

public class InheritanceTest {

    public static void main(String[] args) {

        Witness witness = new Witness(1, "Ahmed", "01000000000", "Cairo");

        Operator operator = new Operator(1, "Mohamed", "Morning", 101);

        Hospital hospital = new Hospital(
            1, "City Hospital", "Cairo", 50, 20, new ArrayList<>()
        );

        HospitalStaff staff = new HospitalStaff(
            1, "Ali", "Doctor", "Emergency", hospital
        );

        System.out.println("Witness extends Person: "
                + (witness instanceof Person));

        System.out.println("Operator extends Person: "
                + (operator instanceof Person));

        System.out.println("HospitalStaff extends Person: "
                + (staff instanceof Person));
    }
}