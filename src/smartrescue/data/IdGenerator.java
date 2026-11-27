package smartrescue.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IdGenerator {

    private static final Path FILE_PATH =
            Paths.get("data", "id_counters.csv");

    private static final String INCIDENT = "incident";
    private static final String ASSISTANCE = "assistance";
    private static final String TRANSFER = "transfer";

    private static int generateNextId(String idType) {

        try {
            Files.createDirectories(FILE_PATH.getParent());

            Map<String, Integer> counters = new LinkedHashMap<>();

            counters.put(INCIDENT, 0);
            counters.put(ASSISTANCE, 0);
            counters.put(TRANSFER, 0);

            if (Files.exists(FILE_PATH)) {

                List<String> lines = Files.readAllLines(FILE_PATH);

                for (int i = 1; i < lines.size(); i++) {

                    String[] fields = lines.get(i).split(",", -1);

                    if (fields.length != 2) {
                        throw new IllegalStateException(
                            "Invalid ID counter file format."
                        );
                    }

                    String type = fields[0].trim();

                    if (counters.containsKey(type)) {
                        counters.put(type,
                            Integer.parseInt(fields[1].trim()));
                    }
                }
            }

            int nextId = counters.get(idType) + 1;
            counters.put(idType, nextId);

            StringBuilder content = new StringBuilder();
            content.append("idType,lastUsed\n");

            for (Map.Entry<String, Integer> entry
                    : counters.entrySet()) {

                content.append(entry.getKey())
                       .append(",")
                       .append(entry.getValue())
                       .append("\n");
            }

            Files.writeString(FILE_PATH, content.toString());

            return nextId;

        } catch (IOException | NumberFormatException e) {

            throw new IllegalStateException(
                "Could not generate a persistent ID.", e
            );
        }
    }

    public static synchronized String nextIncidentId() {

        int number = generateNextId(INCIDENT);

        return String.format("INC-%03d", number);
    }

    public static synchronized int nextAssistanceRecordId() {
        return generateNextId(ASSISTANCE);
    }

    public static synchronized int nextHospitalTransferId() {
        return generateNextId(TRANSFER);
    }
}