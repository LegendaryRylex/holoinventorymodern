package dev.rylex.holoinventorymodern.client;

import dev.rylex.holoinventorymodern.HoloInventoryModernConfig.Anchor;
import net.minecraft.world.phys.Vec3;

public final class HologramAnchor {

    private HologramAnchor() {}

    /**
     * @param lifted whether the panel is raised so that its bottom edge, not its middle, rests on the position
     */
    public record Placement(Vec3 position, boolean lifted) {}

    public static Placement place(Anchor anchor, Vec3 center, double halfHeight, Vec3 custom, float yawDegrees) {
        Vec3 top = center.add(0.0, halfHeight, 0.0);
        return switch (anchor) {
            case CENTER -> new Placement(center, false);
            case TOP -> new Placement(top, false);
            case ABOVE -> new Placement(top, true);
            case CUSTOM -> new Placement(top.add(viewerRelative(yawDegrees, custom)), true);
        };
    }

    /**
     * @param offset x to the viewer's right, y up, z away from the viewer
     */
    public static Vec3 viewerRelative(float yawDegrees, Vec3 offset) {
        double yaw = Math.toRadians(yawDegrees);
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        return new Vec3(-cos * offset.x - sin * offset.z, offset.y, -sin * offset.x + cos * offset.z);
    }
}
