package smartrescue.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import smartrescue.hospital.Hospital;

public class HospitalRepository {

    private Path filePath;

    public HospitalRepository() {
        this.filePath = Paths.get("data", "hospitals.csv");
    }

    public List<Hospital> searchHospitals(String specialty, int minBeds) {

        List<Hospital> matchingHospitals = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {

            String line;
            reader.readLine();

            while ((line = reader.readLine()) != null) {

                String[] fields = line.split(",", -1);

                if (fields.length != 6) {
                    continue;
                }

                int hospitalId = Integer.parseInt(fields[0].trim());
                String name = fields[1].trim();
                String location = fields[2].trim();
                int availableBeds = Integer.parseInt(fields[3].trim());
                int emergencyCapacity = Integer.parseInt(fields[4].trim());

                List<String> specialties = new ArrayList<>();
                String[] specialtyValues = fields[5].split(";");

                for (String value : specialtyValues) {
                    specialties.add(value.trim());
                }

                Hospital hospital = new Hospital(
                    hospitalId,
                    name,
                    location,
                    availableBeds,
                    emergencyCapacity,
                    specialties
                );

                boolean specialtyMatches = specialties.stream()
                    .anyMatch(value -> value.equalsIgnoreCase(specialty));

                if (specialtyMatches
                        && availableBeds >= minBeds
                        && hospital.hasCapacityFor(specialty)) {

                    matchingHospitals.add(hospital);
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                "Could not read hospitals.csv", e
            );
        }

        return matchingHospitals;
    }

    public void updateHospitalAvailability(int hospitalId, int beds,
                                            int capacity,
                                            List<String> specialties) {

        try {

            List<String> lines = Files.readAllLines(filePath);
            List<String> updatedLines = new ArrayList<>();

            updatedLines.add(lines.get(0));

            boolean hospitalFound = false;

            for (int i = 1; i < lines.size(); i++) {

                String line = lines.get(i);
                String[] fields = line.split(",", -1);

                if (fields.length != 6) {
                    updatedLines.add(line);
                    continue;
                }

                int currentHospitalId =
                        Integer.parseInt(fields[0].trim());

                if (currentHospitalId == hospitalId) {

                    String updatedLine =
                            fields[0] + "," +
                            fields[1] + "," +
                            fields[2] + "," +
                            beds + "," +
                            capacity + "," +
                            String.join(";", specialties);

                    updatedLines.add(updatedLine);
                    hospitalFound = true;

                } else {
                    updatedLines.add(line);
                }
            }

            if (!hospitalFound) {
                throw new IllegalArgumentException(
                    "Hospital ID " + hospitalId + " was not found."
                );
            }

            Files.write(filePath, updatedLines);

        } catch (IOException e) {
            throw new IllegalStateException(
                "Could not update hospitals.csv", e
            );
        }
    }
}