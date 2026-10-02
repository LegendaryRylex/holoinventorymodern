package dev.rylex.holoinventorymodern.peek;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record PeekContents(Component name, List<ItemStack> stacks) {}
