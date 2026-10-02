package dev.rylex.holoinventorymodern;

import dev.rylex.holoinventorymodern.peek.ContainerPeek;
import dev.rylex.holoinventorymodern.peek.PeekContents;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

final class ContainerPeekGameTest {

    private static final BlockPos TARGET = new BlockPos(2, 1, 2);
    private static final BlockPos NEIGHBOUR = new BlockPos(3, 1, 2);

    private ContainerPeekGameTest() {}

    static void register(HoloInventoryModernGameTests.Batch batch) {
        batch.add("chest_reports_its_contents", ContainerPeekGameTest::chestReportsItsContents)
                .add("furnace_reports_every_slot", ContainerPeekGameTest::furnaceReportsEverySlot)
                .add("double_chest_reports_both_halves", ContainerPeekGameTest::doubleChestReportsBothHalves)
                .add("single_slot_container_stays_hidden", ContainerPeekGameTest::singleSlotContainerStaysHidden)
                .add("unopened_loot_chest_stays_unrolled", ContainerPeekGameTest::unopenedLootChestStaysUnrolled)
                .add("plain_block_reports_nothing", ContainerPeekGameTest::plainBlockReportsNothing);
    }

    private static void chestReportsItsContents(GameTestHelper helper) {
        helper.setBlock(TARGET, Blocks.CHEST);
        Container chest = container(helper, TARGET);
        chest.setItem(0, new ItemStack(Items.DIAMOND, 12));
        chest.setItem(5, new ItemStack(Items.DIAMOND, 20));
        chest.setItem(9, new ItemStack(Items.IRON_INGOT, 3));

        PeekContents contents = peek(helper, TARGET);
        List<ItemStack> stacks = contents.stacks();
        helper.assertTrue(stacks.size() == 2, "expected two merged stacks, got " + stacks.size());
        helper.assertTrue(stacks.get(0).is(Items.DIAMOND), "first stack should be the diamonds");
        helper.assertTrue(
                stacks.get(0).getCount() == 32,
                "diamonds should merge to 32, got " + stacks.get(0).getCount());
        helper.assertTrue(stacks.get(1).getCount() == 3, "iron should stay at 3");
        helper.assertTrue(
                contents.name().equals(Component.translatable("container.chest")),
                "name should be the chest display name, got " + contents.name());
        helper.succeed();
    }

    private static void furnaceReportsEverySlot(GameTestHelper helper) {
        helper.setBlock(TARGET, Blocks.FURNACE);
        Container furnace = container(helper, TARGET);
        furnace.setItem(0, new ItemStack(Items.RAW_IRON, 8));
        furnace.setItem(1, new ItemStack(Items.COAL, 4));
        furnace.setItem(2, new ItemStack(Items.IRON_INGOT, 2));

        List<ItemStack> stacks = peek(helper, TARGET).stacks();
        helper.assertTrue(stacks.size() == 3, "furnace should report three stacks, got " + stacks.size());
        helper.succeed();
    }

    private static void doubleChestReportsBothHalves(GameTestHelper helper) {
        helper.setBlock(TARGET, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.TYPE, ChestType.LEFT));
        helper.setBlock(NEIGHBOUR, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.TYPE, ChestType.RIGHT));
        container(helper, TARGET).setItem(0, new ItemStack(Items.DIAMOND, 1));
        container(helper, NEIGHBOUR).setItem(0, new ItemStack(Items.EMERALD, 1));

        List<ItemStack> stacks = peek(helper, TARGET).stacks();
        helper.assertTrue(stacks.size() == 2, "double chest should report both halves, got " + stacks.size());
        helper.succeed();
    }

    private static void singleSlotContainerStaysHidden(GameTestHelper helper) {
        helper.setBlock(TARGET, Blocks.JUKEBOX);
        container(helper, TARGET).setItem(0, new ItemStack(Items.MUSIC_DISC_CAT));
        ServerPlayer player = viewer(helper, TARGET);
        helper.assertTrue(
                ContainerPeek.atBlock(player, helper.absolutePos(TARGET)).isEmpty(),
                "a one slot container should stay hidden until it is whitelisted");
        helper.succeed();
    }

    private static void unopenedLootChestStaysUnrolled(GameTestHelper helper) {
        helper.setBlock(TARGET, Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(TARGET, ChestBlockEntity.class);
        chest.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
        ServerPlayer player = viewer(helper, TARGET);
        helper.assertTrue(
                ContainerPeek.atBlock(player, helper.absolutePos(TARGET)).isEmpty(),
                "an unopened loot chest should report no contents");
        helper.assertTrue(chest.getLootTable() != null, "peeking should leave the loot table unrolled");
        helper.succeed();
    }

    private static void plainBlockReportsNothing(GameTestHelper helper) {
        helper.setBlock(TARGET, Blocks.STONE);
        ServerPlayer player = viewer(helper, TARGET);
        helper.assertTrue(
                ContainerPeek.atBlock(player, helper.absolutePos(TARGET)).isEmpty(), "stone should report no contents");
        helper.succeed();
    }

    private static Container container(GameTestHelper helper, BlockPos pos) {
        return (Container) helper.getBlockEntity(pos, BlockEntity.class);
    }

    private static PeekContents peek(GameTestHelper helper, BlockPos pos) {
        ServerPlayer player = viewer(helper, pos);
        return ContainerPeek.atBlock(player, helper.absolutePos(pos))
                .orElseThrow(() -> new AssertionError("no contents reported at " + pos));
    }

    private static ServerPlayer viewer(GameTestHelper helper, BlockPos pos) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos absolute = helper.absolutePos(pos);
        player.setPos(absolute.getX() + 0.5, absolute.getY() + 1.0, absolute.getZ() + 2.5);
        return player;
    }
}
