package dev.rylex.holoinventorymodern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.rylex.holoinventorymodern.client.HologramLayout;
import org.junit.jupiter.api.Test;

class HologramLayoutTest {

    @Test
    void gridHoldsEveryStack() {
        for (int stacks = 1; stacks <= 512; stacks++) {
            int columns = HologramLayout.columnsFor(stacks);
            int rows = HologramLayout.rowsFor(stacks, columns);
            assertTrue(columns >= 1 && columns <= HologramLayout.MAXIMUM_COLUMNS, "columns for " + stacks);
            assertTrue(columns * rows >= stacks, "grid too small for " + stacks);
            assertTrue((rows - 1) * columns < stacks, "trailing empty row for " + stacks);
        }
    }

    @Test
    void emptyGridHasNoRows() {
        assertEquals(0, HologramLayout.rowsFor(0, HologramLayout.columnsFor(0)));
    }

    @Test
    void countsShortenAboveAThousand() {
        assertEquals("1", HologramLayout.formatCount(1));
        assertEquals("999", HologramLayout.formatCount(999));
        assertEquals("1k", HologramLayout.formatCount(1000));
        assertEquals("1.5k", HologramLayout.formatCount(1500));
        assertEquals("12k", HologramLayout.formatCount(12_345));
        assertEquals("999k", HologramLayout.formatCount(999_999));
        assertEquals("1M", HologramLayout.formatCount(1_000_000));
        assertEquals("2.5M", HologramLayout.formatCount(2_500_000));
    }
}
