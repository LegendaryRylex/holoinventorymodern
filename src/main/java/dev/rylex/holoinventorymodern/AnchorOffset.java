package dev.rylex.holoinventorymodern;

import java.util.Optional;

public record AnchorOffset(String block, double x, double y, double z) {

    public static Optional<AnchorOffset> parse(String entry) {
        int split = entry.indexOf('=');
        if (split <= 0) {
            return Optional.empty();
        }
        String[] parts = entry.substring(split + 1).split(",", -1);
        if (parts.length != 3) {
            return Optional.empty();
        }
        try {
            double x = Double.parseDouble(parts[0].trim());
            double y = Double.parseDouble(parts[1].trim());
            double z = Double.parseDouble(parts[2].trim());
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                return Optional.empty();
            }
            return Optional.of(new AnchorOffset(entry.substring(0, split).trim(), x, y, z));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }
}
