package dev.rylex.holoinventorymodern.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.rylex.holoinventorymodern.HoloInventoryModernConfig;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

final class HologramRenderer {

    private HologramRenderer() {}

    private static final float DISTANCE_SCALE = 0.065F;
    private static final float MINIMUM_DISTANCE = 1.75F;
    private static final double FRONT_OFFSET = 0.75;
    private static final float CELL = 1.15F;
    private static final float PADDING = 0.15F;
    private static final float TITLE_SCALE = 0.05F;
    private static final float COUNT_SCALE = 0.045F;
    private static final float TITLE_GAP = 0.3F;
    private static final float LIFT_GAP = 0.3F;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int SHADOW_COLOR = 0xFF3F3F3F;

    static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        PeekTracker.Hologram hologram = PeekTracker.current(minecraft, partialTick);
        if (hologram == null) {
            return;
        }
        List<ItemStack> stacks = hologram.stacks();
        if (stacks.isEmpty()) {
            return;
        }

        Vec3 camera = event.getCamera().getPosition();
        HologramAnchor.Placement placement = HologramAnchor.place(
                HoloInventoryModernConfig.anchor(),
                hologram.center(),
                hologram.halfHeight(),
                HoloInventoryModernConfig.customOffset(),
                event.getCamera().getYRot());
        Vec3 anchor = towardCamera(placement.position(), camera);
        float scale = (float) Math.max(camera.distanceTo(anchor), MINIMUM_DISTANCE)
                * DISTANCE_SCALE
                * HoloInventoryModernConfig.size();

        int columns = HologramLayout.columnsFor(stacks.size());
        int rows = HologramLayout.rowsFor(stacks.size(), columns);
        float height = rows * CELL;
        float lift = placement.lifted() ? (height / 2.0F + PADDING + LIFT_GAP) * scale : 0.0F;

        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();

        pose.pushPose();
        pose.translate(anchor.x - camera.x, anchor.y - camera.y + lift, anchor.z - camera.z);
        pose.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        pose.scale(scale, scale, scale);

        drawPanel(pose, buffers, columns * CELL, height);
        if (HoloInventoryModernConfig.showName()) {
            drawTitlePlate(minecraft, pose, buffers, hologram.name(), height);
        }
        drawStacks(minecraft, pose, buffers, stacks, columns, rows);
        buffers.endBatch();

        if (HoloInventoryModernConfig.showName()) {
            drawTitle(minecraft.font, pose, buffers, hologram.name(), height);
        }
        if (HoloInventoryModernConfig.showCounts()) {
            drawCounts(minecraft.font, pose, buffers, stacks, columns, rows, 1.0F, SHADOW_COLOR);
            buffers.endBatch();
            drawCounts(minecraft.font, pose, buffers, stacks, columns, rows, 0.0F, TEXT_COLOR);
        }
        buffers.endBatch();

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

    private static void drawPanel(PoseStack pose, MultiBufferSource buffers, float width, float height) {
        float opacity = HoloInventoryModernConfig.backgroundOpacity();
        if (opacity <= 0.0F || height <= 0.0F) {
            return;
        }
        float x = width / 2.0F + PADDING;
        float y = height / 2.0F + PADDING;
        drawPlate(pose, buffers, -x, -y, x, y, (int) (opacity * 255.0F) << 24);
    }

    /**
     * The plate is drawn here instead of through the font's own background colour, which shares a distance-sorted
     * buffer with the glyphs and so ends up in front of whichever letters sit further from the camera than its centre.
     */
    private static void drawTitlePlate(
            Minecraft minecraft, PoseStack pose, MultiBufferSource buffers, Component name, float height) {
        int color = (int) (minecraft.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        float halfWidth = minecraft.font.width(name) / 2.0F + 1.0F;
        pose.pushPose();
        pose.translate(0.0F, height / 2.0F + PADDING + TITLE_GAP, 0.25F);
        pose.scale(TITLE_SCALE, TITLE_SCALE, TITLE_SCALE);
        drawPlate(pose, buffers, -halfWidth, 0.0F, halfWidth, minecraft.font.lineHeight + 1.0F, color);
        pose.popPose();
    }

    private static void drawPlate(
            PoseStack pose, MultiBufferSource buffers, float left, float bottom, float right, float top, int color) {
        VertexConsumer quads = buffers.getBuffer(RenderType.textBackgroundSeeThrough());
        Matrix4f matrix = pose.last().pose();
        quads.addVertex(matrix, left, bottom, 0.0F).setColor(color).setLight(LightTexture.FULL_BRIGHT);
        quads.addVertex(matrix, right, bottom, 0.0F).setColor(color).setLight(LightTexture.FULL_BRIGHT);
        quads.addVertex(matrix, right, top, 0.0F).setColor(color).setLight(LightTexture.FULL_BRIGHT);
        quads.addVertex(matrix, left, top, 0.0F).setColor(color).setLight(LightTexture.FULL_BRIGHT);
    }

    private static void drawStacks(
            Minecraft minecraft,
            PoseStack pose,
            MultiBufferSource buffers,
            List<ItemStack> stacks,
            int columns,
            int rows) {
        for (int index = 0; index < stacks.size(); index++) {
            pose.pushPose();
            pose.translate(cellX(index, columns), cellY(index, columns, rows), 0.05F);
            minecraft
                    .getItemRenderer()
                    .renderStatic(
                            stacks.get(index),
                            ItemDisplayContext.GUI,
                            LightTexture.FULL_BRIGHT,
                            OverlayTexture.NO_OVERLAY,
                            pose,
                            buffers,
                            minecraft.level,
                            0);
            pose.popPose();
        }
    }

    /**
     * The shadow is a pass of its own because the font's built-in one shares a distance-sorted buffer with the digits
     * and can be drawn over them.
     */
    private static void drawCounts(
            Font font,
            PoseStack pose,
            MultiBufferSource buffers,
            List<ItemStack> stacks,
            int columns,
            int rows,
            float shift,
            int color) {
        for (int index = 0; index < stacks.size(); index++) {
            int count = stacks.get(index).getCount();
            if (count <= 1) {
                continue;
            }
            String text = HologramLayout.formatCount(count);
            pose.pushPose();
            pose.translate(
                    cellX(index, columns) + CELL / 2.0F - 0.1F,
                    cellY(index, columns, rows) - CELL / 2.0F + 0.1F,
                    0.25F);
            pose.scale(COUNT_SCALE, -COUNT_SCALE, COUNT_SCALE);
            font.drawInBatch(
                    text,
                    -font.width(text) + shift,
                    -font.lineHeight + shift,
                    color,
                    false,
                    pose.last().pose(),
                    buffers,
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    LightTexture.FULL_BRIGHT);
            pose.popPose();
        }
    }

    private static void drawTitle(Font font, PoseStack pose, MultiBufferSource buffers, Component name, float height) {
        pose.pushPose();
        pose.translate(0.0F, height / 2.0F + PADDING + TITLE_GAP, 0.25F);
        pose.scale(TITLE_SCALE, -TITLE_SCALE, TITLE_SCALE);
        font.drawInBatch(
                name,
                -font.width(name) / 2.0F,
                -font.lineHeight,
                HoloInventoryModernConfig.nameColor(),
                false,
                pose.last().pose(),
                buffers,
                Font.DisplayMode.SEE_THROUGH,
                0,
                LightTexture.FULL_BRIGHT);
        pose.popPose();
    }

    private static float cellX(int index, int columns) {
        return -(columns - 1) * CELL / 2.0F + index % columns * CELL;
    }

    private static float cellY(int index, int columns, int rows) {
        return (rows - 1) * CELL / 2.0F - index / columns * CELL;
    }
}
