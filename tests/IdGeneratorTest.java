import smartrescue.data.IdGenerator;

public class IdGeneratorTest {

    public static void main(String[] args) {

        System.out.println("Incident ID: "
            + IdGenerator.nextIncidentId());

        System.out.println("Assistance Record ID: "
            + IdGenerator.nextAssistanceRecordId());

        System.out.println("Hospital Transfer ID: "
            + IdGenerator.nextHospitalTransferId());
    }
}