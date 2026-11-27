import smartrescue.data.AmbulanceRepository;
import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceStatus;

public class AddDemoAmbulances {

    public static void main(String[] args) {

        AmbulanceRepository repository = new AmbulanceRepository();

        repository.saveAmbulance(new Ambulance(
                1, "DEMO-AMB-001",
                AmbulanceStatus.AVAILABLE, "Cairo"
        ));

        repository.saveAmbulance(new Ambulance(
                2, "DEMO-AMB-002",
                AmbulanceStatus.AVAILABLE, "Giza"
        ));

        repository.saveAmbulance(new Ambulance(
                3, "DEMO-AMB-003",
                AmbulanceStatus.OUT_OF_SERVICE, "Cairo"
        ));

        System.out.println("Demo ambulances saved successfully.");

        for (Ambulance ambulance : repository.loadAllAmbulances()) {
            System.out.println(
                    ambulance.getAmbulanceId() + " | " +
                    ambulance.getLicensePlate() + " | " +
                    ambulance.getStatus() + " | " +
                    ambulance.getCurrentLocation()
            );
        }
    }
}