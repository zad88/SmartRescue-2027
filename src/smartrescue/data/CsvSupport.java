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

            String content = Files.readString(path);
            List<String> records = new ArrayList<>();
            StringBuilder record = new StringBuilder();
            boolean insideQuotes = false;

            for (int i = 0; i < content.length(); i++) {
                char current = content.charAt(i);
                if (current == '"') {
                    record.append(current);
                    if (insideQuotes && i + 1 < content.length()
                            && content.charAt(i + 1) == '"') {
                        record.append(content.charAt(++i));
                    } else {
                        insideQuotes = !insideQuotes;
                    }
                } else if (!insideQuotes && (current == '\n' || current == '\r')) {
                    records.add(record.toString());
                    record.setLength(0);
                    if (current == '\r' && i + 1 < content.length()
                            && content.charAt(i + 1) == '\n') {
                        i++;
                    }
                } else {
                    record.append(current);
                }
            }

            if (insideQuotes) {
                throw new IllegalStateException("Unclosed quoted CSV field in " + path);
            }
            if (record.length() > 0) {
                records.add(record.toString());
            }
            return records;
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

    public static void ensureColumns(Path path, String header, String... oldHeaders) {
        List<String> records = readLines(path, header);
        if (records.isEmpty() || header.equals(records.get(0))) {
            return;
        }

        boolean recognized = false;
        for (String oldHeader : oldHeaders) {
            if (oldHeader.equals(records.get(0))) {
                recognized = true;
                break;
            }
        }
        if (!recognized) {
            throw new IllegalStateException("Unexpected CSV header in " + path + ": "
                    + records.get(0));
        }

        int targetCount = parse(header).size();
        int minimumCount = parse(records.get(0)).size();
        List<String> migrated = new ArrayList<>();
        migrated.add(header);
        for (int i = 1; i < records.size(); i++) {
            String record = records.get(i);
            if (!record.isBlank()) {
                int count = parse(record).size();
                if (count > targetCount || count < minimumCount) {
                    throw new IllegalStateException("Invalid CSV record in " + path);
                }
                record += ",".repeat(targetCount - count);
            }
            migrated.add(record);
        }
        try {
            Files.write(path, migrated);
        } catch (IOException e) {
            throw new IllegalStateException("Could not migrate " + path, e);
        }
    }
}
