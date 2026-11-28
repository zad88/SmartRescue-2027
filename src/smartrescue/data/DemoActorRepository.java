package smartrescue.data;

import java.nio.file.Path;
import java.util.List;

import smartrescue.dispatch.Ambulance;
import smartrescue.dispatch.AmbulanceTeam;
import smartrescue.dispatch.Operator;
import smartrescue.emergency.Witness;
import smartrescue.hospital.Hospital;
import smartrescue.hospital.HospitalStaff;

public class DemoActorRepository {
    public Witness loadWitness() {
        List<String> fields = find("witnesses.csv",
                "witnessId,fullName,phoneNumber,currentLocation", 0, 1, 4);
        return new Witness(Integer.parseInt(fields.get(0)), fields.get(1),
                fields.get(2), fields.get(3));
    }

    public Operator loadOperator() {
        List<String> fields = find("operators.csv",
                "operatorId,fullName,shiftTime,stationId", 0, 1, 4);
        return new Operator(Integer.parseInt(fields.get(0)), fields.get(1),
                fields.get(2), Integer.parseInt(fields.get(3)));
    }

    public AmbulanceTeam loadTeam(Ambulance ambulance) {
        List<String> fields = find("ambulance_teams.csv",
                "teamId,ambulanceId,driverName,medicCount,currentStatus",
                1, ambulance.getAmbulanceId(), 5);
        return new AmbulanceTeam(Integer.parseInt(fields.get(0)), fields.get(2),
                Integer.parseInt(fields.get(3)), fields.get(4), ambulance);
    }

    public HospitalStaff loadStaff(Hospital hospital) {
        List<String> fields = find("hospital_staff.csv",
                "staffId,hospitalId,fullName,role,department",
                1, hospital.getHospitalId(), 5);
        return new HospitalStaff(Integer.parseInt(fields.get(0)), fields.get(2),
                fields.get(3), fields.get(4), hospital);
    }

    private List<String> find(String file, String header, int keyColumn,
                              int key, int fieldCount) {
        Path path = Path.of("data", file);
        List<String> records = CsvSupport.readLines(path, header);
        if (records.isEmpty() || !header.equals(records.get(0))) {
            throw new IllegalStateException("Unexpected actor data header in " + path);
        }
        List<String> match = null;
        for (int i = 1; i < records.size(); i++) {
            if (records.get(i).isBlank()) {
                continue;
            }
            List<String> fields = CsvSupport.parse(records.get(i));
            if (fields.size() != fieldCount) {
                throw new IllegalStateException("Invalid actor record in " + path);
            }
            if (Integer.parseInt(fields.get(keyColumn)) == key) {
                if (match != null) {
                    throw new IllegalStateException("Duplicate actor mapping in " + path);
                }
                match = fields;
            }
        }
        if (match == null) {
            throw new IllegalStateException("No actor record for ID " + key + " in " + path);
        }
        return match;
    }
}
