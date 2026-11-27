package smartrescue.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvSupport {

    public static String escape(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    public static List<String> parse(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);

            if (current == '"') {
                if (insideQuotes && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (current == ',' && !insideQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(current);
            }
        }

        fields.add(field.toString());
        return fields;
    }

    public static List<String> readLines(Path path, String header) {
        try {
            Files.createDirectories(path.getParent());

            if (!Files.exists(path) || Files.size(path) == 0) {
                Files.writeString(path, header + System.lineSeparator());
            }

            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + path, e);
        }
    }

    public static void upsert(Path path, String header, int keyIndex,
                              String key, String record) {
        List<String> lines = readLines(path, header);
        boolean found = false;

        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) continue;

            List<String> fields = parse(lines.get(i));

            if (fields.size() > keyIndex
                    && fields.get(keyIndex).equals(key)) {
                lines.set(i, record);
                found = true;
                break;
            }
        }

        if (!found) lines.add(record);

        try {
            Files.write(path, lines);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write " + path, e);
        }
    }
}
