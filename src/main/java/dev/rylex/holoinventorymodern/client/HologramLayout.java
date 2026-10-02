package dev.rylex.holoinventorymodern.client;

import java.util.Locale;

public final class HologramLayout {

    public static final int MAXIMUM_COLUMNS = 9;

    private HologramLayout() {}

    public static int columnsFor(int stacks) {
        if (stacks <= 1) {
            return 1;
        }
        return Math.min(MAXIMUM_COLUMNS, (int) Math.ceil(Math.sqrt(stacks)));
    }

    public static int rowsFor(int stacks, int columns) {
        return stacks <= 0 ? 0 : (stacks + columns - 1) / columns;
    }

    public static String formatCount(int count) {
        if (count < 1000) {
            return Integer.toString(count);
        }
        if (count < 1_000_000) {
            return shorten(count / 1000.0) + "k";
        }
        return shorten(count / 1_000_000.0) + "M";
    }

    private static String shorten(double value) {
        if (value >= 10.0) {
            return Integer.toString((int) value);
        }
        String rendered = String.format(Locale.ROOT, "%.1f", value);
        return rendered.endsWith(".0") ? rendered.substring(0, rendered.length() - 2) : rendered;
    }
}
