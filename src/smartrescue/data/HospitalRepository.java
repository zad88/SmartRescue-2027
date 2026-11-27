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

            // Skip the CSV header
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
}