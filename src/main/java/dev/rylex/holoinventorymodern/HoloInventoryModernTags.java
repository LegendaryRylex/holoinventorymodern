package dev.rylex.holoinventorymodern;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class HoloInventoryModernTags {

    private HoloInventoryModernTags() {}

    public static final TagKey<Block> BLACKLIST =
            TagKey.create(Registries.BLOCK, HoloInventoryModern.id("holo_view_blacklist"));

    public static final TagKey<Block> WHITELIST =
            TagKey.create(Registries.BLOCK, HoloInventoryModern.id("holo_view_whitelist"));
}
