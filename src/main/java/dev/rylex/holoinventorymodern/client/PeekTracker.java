package dev.rylex.holoinventorymodern.client;

import dev.rylex.holoinventorymodern.HoloInventoryModernConfig;
import dev.rylex.holoinventorymodern.HoloInventoryModernTags;
import dev.rylex.holoinventorymodern.compat.lodestone.LodestoneCompat;
import dev.rylex.holoinventorymodern.net.BlockPeekRequest;
import dev.rylex.holoinventorymodern.net.BlockPeekResponse;
import dev.rylex.holoinventorymodern.net.EntityPeekRequest;
import dev.rylex.holoinventorymodern.net.EntityPeekResponse;
import dev.rylex.holoinventorymodern.peek.ItemHandlers;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class PeekTracker {

    private PeekTracker() {}

    private static final PeekCache<BlockPos> BLOCKS = new PeekCache<>();
    private static final PeekCache<Integer> ENTITIES = new PeekCache<>();

    private static boolean enabled = true;

    record Hologram(Vec3 center, double halfHeight, Component name, List<ItemStack> stacks) {}

    public static void accept(BlockPeekResponse payload) {
        BLOCKS.store(payload.pos(), payload.name(), payload.stacks(), Util.getMillis());
    }

    public static void accept(EntityPeekResponse payload) {
        ENTITIES.store(payload.entityId(), payload.name(), payload.stacks(), Util.getMillis());
    }

    static boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    static void clear() {
        BLOCKS.clear();
        ENTITIES.clear();
    }

    static void tick(Minecraft minecraft) {
        long now = Util.getMillis();
        BLOCKS.expire(now);
        ENTITIES.expire(now);
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || !active(player)) {
            return;
        }
        HitResult hit = minecraft.hitResult;
        if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = block.getBlockPos();
            if (peekable(level, pos) && BLOCKS.shouldRequest(pos, now)) {
                PacketDistributor.sendToServer(new BlockPeekRequest(pos));
            }
        } else if (hit instanceof EntityHitResult entityHit && HoloInventoryModernConfig.showEntities()) {
            Entity entity = entityHit.getEntity();
            if (peekable(entity) && ENTITIES.shouldRequest(entity.getId(), now)) {
                PacketDistributor.sendToServer(new EntityPeekRequest(entity.getId()));
            }
        }
    }

    @Nullable
    static Hologram current(Minecraft minecraft, float partialTick) {
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || !active(player)) {
            return null;
        }
        long now = Util.getMillis();
        HitResult hit = minecraft.hitResult;
        if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK) {
            PeekCache.Entry entry = BLOCKS.get(block.getBlockPos(), now);
            return entry == null
                    ? null
                    : new Hologram(center(minecraft.level, block.getBlockPos()), 0.5, entry.name(), entry.stacks());
        }
        if (hit instanceof EntityHitResult entityHit && HoloInventoryModernConfig.showEntities()) {
            Entity entity = entityHit.getEntity();
            PeekCache.Entry entry = ENTITIES.get(entity.getId(), now);
            if (entry == null) {
                return null;
            }
            double halfHeight = entity.getBbHeight() / 2.0;
            Vec3 center = entity.getPosition(partialTick).add(0.0, halfHeight, 0.0);
            return new Hologram(center, halfHeight, entry.name(), entry.stacks());
        }
        return null;
    }

    private static Vec3 center(ClientLevel level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        Vec3 offset = HoloInventoryModernConfig.anchorOffsets()
                .get(BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()));
        return offset == null ? center : center.add(offset);
    }

    private static boolean active(LocalPlayer player) {
        if (!enabled) {
            return false;
        }
        if (HoloInventoryModernConfig.hideWhileSprinting() && player.isSprinting()) {
            return false;
        }
        return switch (HoloInventoryModernConfig.trigger()) {
            case ALWAYS -> true;
            case SNEAKING -> player.isShiftKeyDown();
            case KEY_HELD -> HoloInventoryModernKeys.PEEK.isDown();
        };
    }

    private static boolean peekable(ClientLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(HoloInventoryModernTags.BLACKLIST)) {
            return false;
        }
        if (state.getBlock() instanceof EnderChestBlock) {
            return true;
        }
        if (!state.hasBlockEntity()) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof Container
                || ItemHandlers.present(
                        side -> level.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, blockEntity, side))
                || ItemHandlers.present(LodestoneCompat.source(blockEntity));
    }

    private static boolean peekable(Entity entity) {
        if (entity instanceof Player) {
            return false;
        }
        return entity instanceof Container
                || entity instanceof AbstractChestedHorse horse && horse.hasChest()
                || ItemHandlers.present(side -> Capabilities.ItemHandler.ENTITY_AUTOMATION.getCapability(entity, side));
    }
}
