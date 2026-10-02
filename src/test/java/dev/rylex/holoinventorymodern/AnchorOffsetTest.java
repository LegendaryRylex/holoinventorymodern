package dev.rylex.holoinventorymodern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AnchorOffsetTest {

    @Test
    void parsesBlockAndOffset() {
        assertEquals(
                new AnchorOffset("create:mechanical_crafter", 0.5, 2.0, -1.0),
                AnchorOffset.parse("create:mechanical_crafter = 0.5, 2, -1").orElseThrow());
    }

    @Test
    void rejectsMalformedEntries() {
        assertTrue(AnchorOffset.parse("minecraft:chest").isEmpty());
        assertTrue(AnchorOffset.parse("=0,1,0").isEmpty());
        assertTrue(AnchorOffset.parse("minecraft:chest=1").isEmpty());
        assertTrue(AnchorOffset.parse("minecraft:chest=0,1,0,0").isEmpty());
        assertTrue(AnchorOffset.parse("minecraft:chest=0,up,0").isEmpty());
        assertTrue(AnchorOffset.parse("minecraft:chest=0,NaN,0").isEmpty());
        assertTrue(AnchorOffset.parse("minecraft:chest=0,Infinity,0").isEmpty());
    }
}
