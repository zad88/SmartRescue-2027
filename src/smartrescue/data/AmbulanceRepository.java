package smartrescue.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceStatus;

public class AmbulanceRepository {

    private static final Path FILE =
            Path.of("data", "ambulances.csv");

    private static final String HEADER =
            "ambulanceId,licensePlate,status,currentLocation";

    public void saveAmbulance(Ambulance ambulance) {

        if (ambulance == null) {
            throw new IllegalArgumentException(
                    "Ambulance cannot be null."
            );
        }

        String row =
                ambulance.getAmbulanceId() + "," +
                CsvSupport.escape(ambulance.getLicensePlate()) + "," +
                CsvSupport.escape(ambulance.getStatus().name()) + "," +
                CsvSupport.escape(ambulance.getCurrentLocation());

        CsvSupport.upsert(
                FILE,
                HEADER,
                0,
                String.valueOf(ambulance.getAmbulanceId()),
                row
        );
    }

    public List<Ambulance> loadAllAmbulances() {

        List<Ambulance> ambulances = new ArrayList<>();

        List<String> lines = CsvSupport.readLines(FILE, HEADER);

        for (int i = 1; i < lines.size(); i++) {

            if (lines.get(i).isBlank()) {
                continue;
            }

            List<String> fields = CsvSupport.parse(lines.get(i));

            if (fields.size() != 4) {
                throw new IllegalStateException(
                        "Invalid ambulance CSV record: " + lines.get(i)
                );
            }

            Ambulance ambulance = new Ambulance(
                    Integer.parseInt(fields.get(0)),
                    fields.get(1),
                    AmbulanceStatus.valueOf(fields.get(2)),
                    fields.get(3)
            );

            ambulances.add(ambulance);
        }

        return ambulances;
    }

    public Ambulance findAmbulanceById(int ambulanceId) {

        for (Ambulance ambulance : loadAllAmbulances()) {

            if (ambulance.getAmbulanceId() == ambulanceId) {
                return ambulance;
            }
        }

        return null;
    }
}