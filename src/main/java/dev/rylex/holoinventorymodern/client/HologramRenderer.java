package dev.rylex.holoinventorymodern.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.rylex.holoinventorymodern.HoloInventoryModern;
import dev.rylex.holoinventorymodern.HoloInventoryModernConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

final class HologramRenderer {

    private HologramRenderer() {}

    private static final ContextKey<Frame> FRAME = new ContextKey<>(HoloInventoryModern.id("hologram"));

    private static final float DISTANCE_SCALE = 0.065F;
    private static final float MINIMUM_DISTANCE = 1.75F;
    private static final double FRONT_OFFSET = 0.75;
    private static final float CELL = 1.15F;
    private static final float PADDING = 0.15F;
    private static final float TITLE_SCALE = 0.05F;
    private static final float COUNT_SCALE = 0.045F;
    private static final float TITLE_GAP = 0.3F;
    private static final float LIFT_GAP = 0.3F;
    private static final float PANEL_DEPTH = -0.45F;
    private static final float TITLE_DEPTH = 0.25F;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int SHADOW_COLOR = 0xFF3F3F3F;
    private static final int PANEL_ORDER = 0;
    private static final int PLATE_ORDER = 1;
    private static final int TEXT_ORDER = 2;
    private static final int COUNT_ORDER = 3;

    private record Frame(
            Vec3 position,
            Quaternionf rotation,
            float scale,
            Component name,
            List<ItemStackRenderState> items,
            List<Integer> counts,
            int columns,
            int rows,
            int titleBackground) {}

    static void extract(ExtractLevelRenderStateEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        float partialTick = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        PeekTracker.Hologram hologram = PeekTracker.current(minecraft, partialTick);
        if (hologram == null || hologram.stacks().isEmpty()) {
            return;
        }
        List<ItemStack> stacks = hologram.stacks();
        Camera camera = event.getCamera();
        Vec3 cameraPosition = camera.position();
        HologramAnchor.Placement placement = HologramAnchor.place(
                HoloInventoryModernConfig.anchor(),
                hologram.center(),
                hologram.halfHeight(),
                HoloInventoryModernConfig.customOffset(),
                camera.yRot());
        Vec3 anchor = towardCamera(placement.position(), cameraPosition);
        float scale = (float) Math.max(cameraPosition.distanceTo(anchor), MINIMUM_DISTANCE)
                * DISTANCE_SCALE
                * HoloInventoryModernConfig.size();

        int columns = HologramLayout.columnsFor(stacks.size());
        int rows = HologramLayout.rowsFor(stacks.size(), columns);
        float lift = placement.lifted() ? (rows * CELL / 2.0F + PADDING + LIFT_GAP) * scale : 0.0F;

        List<ItemStackRenderState> items = new ArrayList<>(stacks.size());
        List<Integer> counts = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            ItemStackRenderState state = new ItemStackRenderState();
            minecraft
                    .getItemModelResolver()
                    .updateForTopItem(state, stack, ItemDisplayContext.GUI, minecraft.level, null, 0);
            items.add(state);
            counts.add(stack.getCount());
        }
        int titleBackground = (int) (minecraft.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        Vec3 position = anchor.subtract(cameraPosition).add(0.0, lift, 0.0);
        event.getRenderState()
                .setRenderData(
                        FRAME,
                        new Frame(
                                position,
                                new Quaternionf(camera.rotation()),
                                scale,
                                hologram.name(),
                                items,
                                counts,
                                columns,
                                rows,
                                titleBackground));
    }

    static void submit(SubmitCustomGeometryEvent event) {
        Frame frame = event.getLevelRenderState().getRenderData(FRAME);
        if (frame == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        SubmitNodeCollector collector = event.getSubmitNodeCollector();
        PoseStack pose = event.getPoseStack();
        float height = frame.rows() * CELL;

        pose.pushPose();
        pose.translate(frame.position().x, frame.position().y, frame.position().z);
        pose.mulPose(frame.rotation());
        pose.scale(frame.scale(), frame.scale(), frame.scale());

        drawPanel(collector, pose, frame.columns() * CELL, height);
        drawStacks(collector, pose, frame);
        if (HoloInventoryModernConfig.showName()) {
            drawTitlePlate(collector, pose, font, frame, height);
            drawTitle(collector, pose, font, frame.name(), height);
        }
        if (HoloInventoryModernConfig.showCounts()) {
            drawCounts(collector, pose, font, frame, 1.0F, SHADOW_COLOR, TEXT_ORDER);
            drawCounts(collector, pose, font, frame, 0.0F, TEXT_COLOR, COUNT_ORDER);
        }

        pose.popPose();
    }

    /**
     * Pulls the hologram off the block it belongs to so the depth-tested item models are not swallowed by it.
     */
    private static Vec3 towardCamera(Vec3 anchor, Vec3 camera) {
        Vec3 offset = camera.subtract(anchor);
        double length = offset.length();
        return length < 1.0E-4 ? anchor : anchor.add(offset.scale(Math.min(FRONT_OFFSET, length * 0.5) / length));
    }

    private static void drawPanel(SubmitNodeCollector collector, PoseStack pose, float width, float height) {
        float opacity = HoloInventoryModernConfig.backgroundOpacity();
        if (opacity <= 0.0F || height <= 0.0F) {
            return;
        }
        float x = width / 2.0F + PADDING;
        float y = height / 2.0F + PADDING;
        int color = (int) (opacity * 255.0F) << 24;
        collector
                .order(PANEL_ORDER)
                .submitCustomGeometry(
                        pose,
                        RenderTypes.textBackgroundSeeThrough(),
                        (last, buffer) -> plate(last, buffer, -x, -y, x, y, PANEL_DEPTH, color));
    }

    /**
     * The plate is submitted ahead of the glyphs because the font's own background colour shares a distance-sorted
     * buffer with them and lands in front of whichever letters sit further from the camera than its centre.
     */
    private static void drawTitlePlate(
            SubmitNodeCollector collector, PoseStack pose, Font font, Frame frame, float height) {
        float halfWidth = font.width(frame.name()) / 2.0F + 1.0F;
        float top = font.lineHeight + 1.0F;
        pose.pushPose();
        pose.translate(0.0F, height / 2.0F + PADDING + TITLE_GAP, TITLE_DEPTH);
        pose.scale(TITLE_SCALE, TITLE_SCALE, TITLE_SCALE);
        collector
                .order(PLATE_ORDER)
                .submitCustomGeometry(
                        pose,
                        RenderTypes.textBackgroundSeeThrough(),
                        (last, buffer) ->
                                plate(last, buffer, -halfWidth, 0.0F, halfWidth, top, 0.0F, frame.titleBackground()));
        pose.popPose();
    }

    private static void plate(
            PoseStack.Pose last,
            VertexConsumer buffer,
            float left,
            float bottom,
            float right,
            float top,
            float depth,
            int color) {
        Matrix4f matrix = last.pose();
        buffer.addVertex(matrix, left, bottom, depth).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, right, bottom, depth).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, right, top, depth).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, left, top, depth).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);
    }

    private static void drawStacks(SubmitNodeCollector collector, PoseStack pose, Frame frame) {
        for (int index = 0; index < frame.items().size(); index++) {
            pose.pushPose();
            pose.translate(cellX(index, frame.columns()), cellY(index, frame.columns(), frame.rows()), 0.05F);
            frame.items().get(index).submit(pose, collector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }

    /**
     * The shadow is a pass of its own because the font's built-in one shares a distance-sorted buffer with the digits
     * and can be drawn over them.
     */
    private static void drawCounts(
            SubmitNodeCollector collector, PoseStack pose, Font font, Frame frame, float shift, int color, int order) {
        for (int index = 0; index < frame.counts().size(); index++) {
            int count = frame.counts().get(index);
            if (count <= 1) {
                continue;
            }
            FormattedCharSequence text =
                    Component.literal(HologramLayout.formatCount(count)).getVisualOrderText();
            pose.pushPose();
            pose.translate(
                    cellX(index, frame.columns()) + CELL / 2.0F - 0.1F,
                    cellY(index, frame.columns(), frame.rows()) - CELL / 2.0F + 0.1F,
                    TITLE_DEPTH);
            pose.scale(COUNT_SCALE, -COUNT_SCALE, COUNT_SCALE);
            collector
                    .order(order)
                    .submitText(
                            pose,
                            -font.width(text) + shift,
                            -font.lineHeight + shift,
                            text,
                            false,
                            Font.DisplayMode.SEE_THROUGH,
                            LightCoordsUtil.FULL_BRIGHT,
                            color,
                            0,
                            0);
            pose.popPose();
        }
    }

    private static void drawTitle(
            SubmitNodeCollector collector, PoseStack pose, Font font, Component name, float height) {
        pose.pushPose();
        pose.translate(0.0F, height / 2.0F + PADDING + TITLE_GAP, TITLE_DEPTH);
        pose.scale(TITLE_SCALE, -TITLE_SCALE, TITLE_SCALE);
        collector
                .order(TEXT_ORDER)
                .submitText(
                        pose,
                        -font.width(name) / 2.0F,
                        -font.lineHeight,
                        name.getVisualOrderText(),
                        false,
                        Font.DisplayMode.SEE_THROUGH,
                        LightCoordsUtil.FULL_BRIGHT,
                        HoloInventoryModernConfig.nameColor(),
                        0,
                        0);
        pose.popPose();
    }

    private static float cellX(int index, int columns) {
        return -(columns - 1) * CELL / 2.0F + index % columns * CELL;
    }

    private static float cellY(int index, int columns, int rows) {
        return (rows - 1) * CELL / 2.0F - index / columns * CELL;
    }
}
