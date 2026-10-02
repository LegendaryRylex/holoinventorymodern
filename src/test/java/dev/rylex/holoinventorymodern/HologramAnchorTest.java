package dev.rylex.holoinventorymodern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.rylex.holoinventorymodern.HoloInventoryModernConfig.Anchor;
import dev.rylex.holoinventorymodern.client.HologramAnchor;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class HologramAnchorTest {

    private static final Vec3 CENTER = new Vec3(10.5, 64.5, -3.5);
    private static final Vec3 CUSTOM = new Vec3(1.0, 2.0, 3.0);

    @Test
    void presetsPlaceTheHologramOnTheBlock() {
        HologramAnchor.Placement center = HologramAnchor.place(Anchor.CENTER, CENTER, 0.5, CUSTOM, 0.0F);
        assertClose(CENTER, center.position());
        assertFalse(center.lifted());

        HologramAnchor.Placement top = HologramAnchor.place(Anchor.TOP, CENTER, 0.5, CUSTOM, 0.0F);
        assertClose(new Vec3(10.5, 65.0, -3.5), top.position());
        assertFalse(top.lifted());

        HologramAnchor.Placement above = HologramAnchor.place(Anchor.ABOVE, CENTER, 0.5, CUSTOM, 0.0F);
        assertClose(new Vec3(10.5, 65.0, -3.5), above.position());
        assertTrue(above.lifted());
    }

    @Test
    void customOffsetFollowsTheViewer() {
        HologramAnchor.Placement south = HologramAnchor.place(Anchor.CUSTOM, CENTER, 0.5, CUSTOM, 0.0F);
        assertClose(new Vec3(9.5, 67.0, -0.5), south.position());
        assertTrue(south.lifted());

        assertClose(new Vec3(-3.0, 2.0, -1.0), HologramAnchor.viewerRelative(90.0F, CUSTOM));
        assertClose(new Vec3(1.0, 2.0, -3.0), HologramAnchor.viewerRelative(180.0F, CUSTOM));
    }

    private static void assertClose(Vec3 expected, Vec3 actual) {
        assertEquals(0.0, expected.distanceTo(actual), 1.0E-6, () -> expected + " vs " + actual);
    }
}
