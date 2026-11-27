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
                if (line.isBlank()) continue;

                String[] fields = parseCsvLine(line);
                if (fields.length != 6) continue;

                Hospital hospital = convertFromCsv(fields);

                boolean specialtyMatches =
                    hospital.getMedicalSpecialties().stream()
                        .anyMatch(value -> value.equalsIgnoreCase(specialty));

                if (specialtyMatches
                        && hospital.getAvailableBeds() >= minBeds
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

    public Hospital findHospitalById(int hospitalId) {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] fields = parseCsvLine(line);

                if (fields.length == 6
                        && Integer.parseInt(fields[0].trim()) == hospitalId) {
                    return convertFromCsv(fields);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                "Could not read hospitals.csv", e
            );
        }

        return null;
    }

    private Hospital convertFromCsv(String[] fields) {
        int hospitalId = Integer.parseInt(fields[0].trim());
        String name = fields[1].trim();
        String location = fields[2].trim();
        int availableBeds = Integer.parseInt(fields[3].trim());
        int emergencyCapacity = Integer.parseInt(fields[4].trim());

        List<String> specialties = new ArrayList<>();
        for (String value : fields[5].split(";")) {
            specialties.add(value.trim());
        }

        return new Hospital(
            hospitalId, name, location,
            availableBeds, emergencyCapacity, specialties
        );
    }

    public void updateHospitalAvailability(int hospitalId, int beds,
                                           int capacity,
                                           List<String> specialties) {
        try {
            List<String> lines = Files.readAllLines(filePath);

            if (lines.isEmpty()) {
                throw new IllegalStateException(
                    "hospitals.csv is empty."
                );
            }

            List<String> updatedLines = new ArrayList<>();
            updatedLines.add(lines.get(0));

            boolean hospitalFound = false;

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);

                if (line.isBlank()) {
                    updatedLines.add(line);
                    continue;
                }

                String[] fields = parseCsvLine(line);

                if (fields.length != 6) {
                    updatedLines.add(line);
                    continue;
                }

                int currentHospitalId =
                    Integer.parseInt(fields[0].trim());

                if (currentHospitalId == hospitalId) {
                    fields[3] = String.valueOf(beds);
                    fields[4] = String.valueOf(capacity);
                    fields[5] = String.join(";", specialties);

                    updatedLines.add(toCsvLine(fields));
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

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);

            if (current == '"') {
                if (insideQuotes
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (current == ',' && !insideQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(current);
            }
        }

        if (insideQuotes) {
            throw new IllegalArgumentException(
                "Invalid CSV row: unmatched quotation mark."
            );
        }

        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }

    private String toCsvLine(String[] fields) {
        List<String> escapedFields = new ArrayList<>();

        for (String field : fields) {
            String value = field == null ? "" : field;

            if (value.contains(",") || value.contains("\"")
                    || value.contains("\n") || value.contains("\r")) {
                value = "\"" + value.replace("\"", "\"\"") + "\"";
            }

            escapedFields.add(value);
        }

        return String.join(",", escapedFields);
    }
}
