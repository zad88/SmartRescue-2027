package smartrescue.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class DataChangeTracker {
    private static final List<String> FILES = List.of(
            "emergencies.csv", "ambulances.csv", "hospitals.csv",
            "patients.csv", "assistance_records.csv", "hospital_transfers.csv",
            "witnesses.csv", "operators.csv", "ambulance_teams.csv", "hospital_staff.csv");
    private byte[] previous;

    public boolean hasChanged() {
        byte[] current = fingerprint();
        boolean changed = previous != null && !Arrays.equals(previous, current);
        previous = current;
        return changed;
    }

    private byte[] fingerprint() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String file : FILES) {
                Path path = Path.of("data", file);
                digest.update(file.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                if (Files.exists(path)) {
                    digest.update((byte) 1);
                    digest.update(Files.readAllBytes(path));
                } else {
                    digest.update((byte) 0);
                }
            }
            return digest.digest();
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Could not check for updated portal data.", exception);
        }
    }
}
