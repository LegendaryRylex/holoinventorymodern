package dev.rylex.holoinventorymodern.peek;

import dev.rylex.holoinventorymodern.HoloInventoryModernConfig;
import dev.rylex.holoinventorymodern.HoloInventoryModernTags;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

public final class ContainerPeek {

    private ContainerPeek() {}

    public static Optional<PeekContents> atBlock(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.level();
        if (!level.isLoaded(pos) || outOfRange(player, Vec3.atCenterOf(pos))) {
            return Optional.empty();
        }
        BlockState state = level.getBlockState(pos);
        if (blacklisted(state)) {
            return Optional.empty();
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BaseContainerBlockEntity container && !container.canOpen(player)) {
            return Optional.empty();
        }
        if (withholdsLoot(player, blockEntity) || withholdsLoot(player, otherChestHalf(level, pos, state))) {
            return Optional.empty();
        }
        Component name = blockName(state, blockEntity);
        if (state.getBlock() instanceof EnderChestBlock) {
            return of(name, player.getEnderChestInventory());
        }
        if (state.getBlock() instanceof ChestBlock chest) {
            Container merged = ChestBlock.getContainer(chest, state, level, pos, true);
            if (merged != null) {
                return of(name, merged);
            }
        }
        if (blockEntity instanceof Container container) {
            return tooSmall(container.getContainerSize(), state) ? Optional.empty() : of(name, container);
        }
        ItemHandlers.Contents contents = ItemHandlers.merged(
                side -> level.getCapability(Capabilities.Item.BLOCK, pos, state, blockEntity, side));
        if (contents == null || tooSmall(contents.slots(), state)) {
            return Optional.empty();
        }
        return Optional.of(new PeekContents(name, cap(contents.stacks())));
    }

    public static Optional<PeekContents> atEntity(ServerPlayer player, Entity entity) {
        if (entity instanceof Player || outOfRange(player, entity.position())) {
            return Optional.empty();
        }
        if (entity instanceof ContainerEntity vehicle && vehicle.getContainerLootTable() != null) {
            if (!HoloInventoryModernConfig.showLootContainers()) {
                return Optional.empty();
            }
            vehicle.unpackChestVehicleLootTable(player);
        }
        Component name = entity.getDisplayName();
        if (entity instanceof Container container) {
            return of(name, container);
        }
        if (entity instanceof AbstractChestedHorse horse && horse.hasChest()) {
            return of(name, horse.getInventory());
        }
        ItemHandlers.Contents contents =
                ItemHandlers.merged(side -> Capabilities.Item.ENTITY_AUTOMATION.getCapability(entity, side));
        return contents == null ? Optional.empty() : Optional.of(new PeekContents(name, cap(contents.stacks())));
    }

    public static boolean tooSmall(int slots, BlockState state) {
        return slots <= 1 && !whitelisted(state);
    }

    public static boolean whitelisted(BlockState state) {
        return state.is(HoloInventoryModernTags.WHITELIST)
                || HoloInventoryModernConfig.whitelist().contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    public static boolean blacklisted(BlockState state) {
        return state.is(HoloInventoryModernTags.BLACKLIST)
                || HoloInventoryModernConfig.blacklist().contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    private static boolean withholdsLoot(ServerPlayer player, @Nullable BlockEntity blockEntity) {
        if (!(blockEntity instanceof RandomizableContainer container) || container.getLootTable() == null) {
            return false;
        }
        if (!HoloInventoryModernConfig.showLootContainers()) {
            return true;
        }
        container.unpackLootTable(player);
        return false;
    }

    @Nullable
    private static BlockEntity otherChestHalf(ServerLevel level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof ChestBlock) || state.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
            return null;
        }
        return level.getBlockEntity(pos.relative(ChestBlock.getConnectedDirection(state)));
    }

    private static boolean outOfRange(ServerPlayer player, Vec3 target) {
        double range = HoloInventoryModernConfig.maximumRange();
        return player.position().distanceToSqr(target) > range * range;
    }

    private static Component blockName(BlockState state, @Nullable BlockEntity blockEntity) {
        return blockEntity instanceof Nameable nameable
                ? nameable.getDisplayName()
                : state.getBlock().getName();
    }

    private static Optional<PeekContents> of(Component name, Container container) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemHandlers.merge(stacks, container.getItem(slot));
        }
        return Optional.of(new PeekContents(name, cap(stacks)));
    }

    private static List<ItemStack> cap(List<ItemStack> stacks) {
        int maximum = HoloInventoryModernConfig.maximumStacks();
        return stacks.size() <= maximum ? stacks : stacks.subList(0, maximum);
    }
}
