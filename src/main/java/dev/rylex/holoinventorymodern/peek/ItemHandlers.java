package dev.rylex.holoinventorymodern.peek;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public final class ItemHandlers {

    private ItemHandlers() {}

    private static final List<Direction> SIDES = Collections.unmodifiableList(Arrays.<Direction>asList(
            null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST));

    public interface Source {
        @Nullable
        IItemHandler at(@Nullable Direction side);
    }

    public record Contents(int slots, List<ItemStack> stacks) {}

    public static boolean present(Source source) {
        for (Direction side : SIDES) {
            if (source.at(side) != null) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static Contents merged(Source source) {
        int slots = 0;
        boolean found = false;
        List<List<ItemStack>> views = new ArrayList<>();
        for (Direction side : SIDES) {
            IItemHandler handler = source.at(side);
            if (handler == null) {
                continue;
            }
            found = true;
            slots = Math.max(slots, handler.getSlots());
            List<ItemStack> view = contentsOf(handler);
            if (!view.isEmpty() && views.stream().noneMatch(seen -> sameView(seen, view))) {
                views.add(view);
            }
        }
        if (!found) {
            return null;
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (List<ItemStack> view : views) {
            for (ItemStack stack : view) {
                merge(stacks, stack);
            }
        }
        return new Contents(slots, stacks);
    }

    public static void merge(List<ItemStack> stacks, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        for (ItemStack existing : stacks) {
            if (ItemStack.isSameItemSameComponents(existing, stack)) {
                existing.setCount(existing.getCount() + stack.getCount());
                return;
            }
        }
        stacks.add(stack.copy());
    }

    private static List<ItemStack> contentsOf(IItemHandler handler) {
        List<ItemStack> view = new ArrayList<>();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                view.add(stack.copy());
            }
        }
        return view;
    }

    /**
     * Compares contents rather than handler identity, because a machine may hand out a fresh wrapper over the same
     * inventory for every face, which would otherwise be counted six times over.
     */
    private static boolean sameView(List<ItemStack> first, List<ItemStack> second) {
        if (first.size() != second.size()) {
            return false;
        }
        for (int index = 0; index < first.size(); index++) {
            ItemStack left = first.get(index);
            ItemStack right = second.get(index);
            if (left.getCount() != right.getCount() || !ItemStack.isSameItemSameComponents(left, right)) {
                return false;
            }
        }
        return true;
    }
}
