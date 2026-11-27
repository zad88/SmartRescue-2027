import smartrescue.data.EmergencyRepository;
import smartrescue.emergency.Emergency;

public class CheckEmergencyMigration {

    public static void main(String[] args) {

        EmergencyRepository repository = new EmergencyRepository();

        for (Emergency emergency : repository.loadAllEmergencies()) {
            System.out.println(
                    emergency.getIncidentId() + " | " +
                    emergency.getStatus()
            );
        }

        System.out.println("Emergency CSV migration check completed.");
    }
}