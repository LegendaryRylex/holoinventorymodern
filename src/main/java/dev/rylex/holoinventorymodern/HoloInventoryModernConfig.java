package dev.rylex.holoinventorymodern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class HoloInventoryModernConfig {

    private HoloInventoryModernConfig() {}

    public enum BlockList {
        BLACKLIST,
        WHITELIST
    }

    public enum Anchor {
        CENTER,
        TOP,
        ABOVE,
        CUSTOM
    }

    public enum Trigger {
        ALWAYS,
        SNEAKING,
        KEY_HELD
    }

    public static final Trigger DEFAULT_TRIGGER = Trigger.ALWAYS;
    public static final boolean DEFAULT_HIDE_WHILE_SPRINTING = true;
    public static final boolean DEFAULT_SHOW_NAME = true;
    public static final ChatFormatting DEFAULT_NAME_COLOR = ChatFormatting.AQUA;
    public static final boolean DEFAULT_SHOW_COUNTS = true;
    public static final boolean DEFAULT_SHOW_ENTITIES = true;
    public static final double DEFAULT_SIZE = 1.0;
    public static final double MINIMUM_SIZE = 0.25;
    public static final double MAXIMUM_SIZE = 3.0;
    public static final double DEFAULT_BACKGROUND_OPACITY = 0.35;
    public static final Anchor DEFAULT_ANCHOR = Anchor.ABOVE;
    public static final double CUSTOM_OFFSET_LIMIT = 16.0;
    public static final int DEFAULT_MAXIMUM_STACKS = 45;
    public static final double DEFAULT_MAXIMUM_RANGE = 12.0;
    public static final boolean DEFAULT_SHOW_LOOT_CONTAINERS = false;
    public static final List<String> DEFAULT_BLACKLIST = List.of("nep:arcane_lectern");

    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec SERVER_SPEC;

    private static final ModConfigSpec.EnumValue<Trigger> TRIGGER;
    private static final ModConfigSpec.BooleanValue HIDE_WHILE_SPRINTING;
    private static final ModConfigSpec.BooleanValue SHOW_NAME;
    private static final ModConfigSpec.EnumValue<ChatFormatting> NAME_COLOR;
    private static final ModConfigSpec.BooleanValue SHOW_COUNTS;
    private static final ModConfigSpec.BooleanValue SHOW_ENTITIES;
    private static final ModConfigSpec.DoubleValue SIZE;
    private static final ModConfigSpec.DoubleValue BACKGROUND_OPACITY;
    private static final ModConfigSpec.EnumValue<Anchor> ANCHOR;
    private static final ModConfigSpec.DoubleValue CUSTOM_OFFSET_X;
    private static final ModConfigSpec.DoubleValue CUSTOM_OFFSET_Y;
    private static final ModConfigSpec.DoubleValue CUSTOM_OFFSET_Z;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ANCHOR_OFFSETS;

    private static List<? extends String> anchorOffsetSource = List.of();
    private static Map<Identifier, Vec3> anchorOffsets = Map.of();

    private static final ModConfigSpec.IntValue MAXIMUM_STACKS;
    private static final ModConfigSpec.DoubleValue MAXIMUM_RANGE;
    private static final ModConfigSpec.BooleanValue SHOW_LOOT_CONTAINERS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> WHITELIST;

    static {
        ModConfigSpec.Builder client = new ModConfigSpec.Builder();
        TRIGGER = client.comment("When the hologram is shown while looking at a container.")
                .translation("holoinventorymodern.configuration.trigger")
                .defineEnum("trigger", DEFAULT_TRIGGER);
        HIDE_WHILE_SPRINTING = client.comment("Hide the hologram while sprinting.")
                .translation("holoinventorymodern.configuration.hideWhileSprinting")
                .define("hideWhileSprinting", DEFAULT_HIDE_WHILE_SPRINTING);
        SHOW_NAME = client.comment("Draw the container name above the contents.")
                .translation("holoinventorymodern.configuration.showName")
                .define("showName", DEFAULT_SHOW_NAME);
        NAME_COLOR = client.comment("Colour of the container name.")
                .translation("holoinventorymodern.configuration.nameColor")
                .defineEnum(
                        "nameColor",
                        DEFAULT_NAME_COLOR,
                        Arrays.stream(ChatFormatting.values())
                                .filter(ChatFormatting::isColor)
                                .toList());
        SHOW_COUNTS = client.comment("Draw the stack size on each item.")
                .translation("holoinventorymodern.configuration.showCounts")
                .define("showCounts", DEFAULT_SHOW_COUNTS);
        SHOW_ENTITIES = client.comment("Peek into entities that carry a container, such as chest minecarts.")
                .translation("holoinventorymodern.configuration.showEntities")
                .define("showEntities", DEFAULT_SHOW_ENTITIES);
        SIZE = client.comment("Size of the hologram, relative to its default.")
                .translation("holoinventorymodern.configuration.size")
                .defineInRange("size", DEFAULT_SIZE, MINIMUM_SIZE, MAXIMUM_SIZE);
        BACKGROUND_OPACITY = client.comment("Opacity of the panel behind the contents. 0 removes it.")
                .translation("holoinventorymodern.configuration.backgroundOpacity")
                .defineInRange("backgroundOpacity", DEFAULT_BACKGROUND_OPACITY, 0.0, 1.0);
        ANCHOR = client.comment("Where the hologram sits: in the middle of the block, at its top, above it, or"
                        + " above it and moved by the custom offset. /holoinventorymodern offset sets it in game.")
                .translation("holoinventorymodern.configuration.anchor")
                .defineEnum("anchor", DEFAULT_ANCHOR);
        CUSTOM_OFFSET_X = client.comment("Blocks to the viewer's right while the hologram position is CUSTOM."
                        + " Negative moves it left.")
                .translation("holoinventorymodern.configuration.customOffsetX")
                .defineInRange("customOffsetX", 0.0, -CUSTOM_OFFSET_LIMIT, CUSTOM_OFFSET_LIMIT);
        CUSTOM_OFFSET_Y = client.comment(
                        "Blocks upwards while the hologram position is CUSTOM. Negative moves it down.")
                .translation("holoinventorymodern.configuration.customOffsetY")
                .defineInRange("customOffsetY", 0.0, -CUSTOM_OFFSET_LIMIT, CUSTOM_OFFSET_LIMIT);
        CUSTOM_OFFSET_Z = client.comment("Blocks away from the viewer while the hologram position is CUSTOM."
                        + " Negative moves it closer.")
                .translation("holoinventorymodern.configuration.customOffsetZ")
                .defineInRange("customOffsetZ", 0.0, -CUSTOM_OFFSET_LIMIT, CUSTOM_OFFSET_LIMIT);
        ANCHOR_OFFSETS = client.comment("Moves the hologram for specific blocks, such as multiblocks or blocks"
                        + " that look larger than one block. Each entry is \"<block id>=<x>,<y>,<z>\" in blocks,"
                        + " added to the default spot above the block, for example \"minecraft:chest=0,1,0\".")
                .translation("holoinventorymodern.configuration.anchorOffsets")
                .defineListAllowEmpty("anchorOffsets", List.of(), () -> "", HoloInventoryModernConfig::isAnchorOffset);
        CLIENT_SPEC = client.build();

        ModConfigSpec.Builder server = new ModConfigSpec.Builder();
        MAXIMUM_STACKS = server.comment("Largest number of distinct stacks reported for one container.")
                .translation("holoinventorymodern.configuration.maximumStacks")
                .defineInRange("maximumStacks", DEFAULT_MAXIMUM_STACKS, 1, 512);
        MAXIMUM_RANGE = server.comment("Largest distance in blocks at which contents are reported.")
                .translation("holoinventorymodern.configuration.maximumRange")
                .defineInRange("maximumRange", DEFAULT_MAXIMUM_RANGE, 1.0, 64.0);
        SHOW_LOOT_CONTAINERS = server.comment("Report containers whose loot has not been generated yet. Looking at one"
                        + " generates its loot for the viewer, as opening it would.")
                .translation("holoinventorymodern.configuration.showLootContainers")
                .define("showLootContainers", DEFAULT_SHOW_LOOT_CONTAINERS);
        BLACKLIST = server.comment(
                        "Block ids whose contents are never reported, for example \"minecraft:barrel\". The block tag"
                                + " #holoinventorymodern:holo_view_blacklist does the same from a datapack.")
                .translation("holoinventorymodern.configuration.blacklist")
                .defineListAllowEmpty(
                        "blacklist", DEFAULT_BLACKLIST, () -> "", HoloInventoryModernConfig::isIdentifier);
        WHITELIST = server.comment("Block ids that are reported even though they hold a single slot, for example"
                        + " \"minecraft:jukebox\". The block tag #holoinventorymodern:holo_view_whitelist does the same"
                        + " from a datapack.")
                .translation("holoinventorymodern.configuration.whitelist")
                .defineListAllowEmpty("whitelist", List.of(), () -> "", HoloInventoryModernConfig::isIdentifier);
        SERVER_SPEC = server.build();
    }

    public static Trigger trigger() {
        return CLIENT_SPEC.isLoaded() ? TRIGGER.get() : DEFAULT_TRIGGER;
    }

    public static boolean hideWhileSprinting() {
        return CLIENT_SPEC.isLoaded() ? HIDE_WHILE_SPRINTING.get() : DEFAULT_HIDE_WHILE_SPRINTING;
    }

    public static boolean showName() {
        return CLIENT_SPEC.isLoaded() ? SHOW_NAME.get() : DEFAULT_SHOW_NAME;
    }

    public static int nameColor() {
        ChatFormatting color = CLIENT_SPEC.isLoaded() ? NAME_COLOR.get() : DEFAULT_NAME_COLOR;
        return 0xFF000000 | color.getColor();
    }

    public static boolean showCounts() {
        return CLIENT_SPEC.isLoaded() ? SHOW_COUNTS.get() : DEFAULT_SHOW_COUNTS;
    }

    public static boolean showEntities() {
        return CLIENT_SPEC.isLoaded() ? SHOW_ENTITIES.get() : DEFAULT_SHOW_ENTITIES;
    }

    public static float size() {
        return (float) (CLIENT_SPEC.isLoaded() ? SIZE.get() : DEFAULT_SIZE);
    }

    public static float backgroundOpacity() {
        return (float) (CLIENT_SPEC.isLoaded() ? BACKGROUND_OPACITY.get() : DEFAULT_BACKGROUND_OPACITY);
    }

    public static Map<Identifier, Vec3> anchorOffsets() {
        if (!CLIENT_SPEC.isLoaded()) {
            return Map.of();
        }
        List<? extends String> raw = ANCHOR_OFFSETS.get();
        if (raw != anchorOffsetSource) {
            anchorOffsets = raw.stream()
                    .flatMap(entry -> AnchorOffset.parse(entry).stream())
                    .filter(offset -> Identifier.tryParse(offset.block()) != null)
                    .collect(Collectors.toUnmodifiableMap(
                            offset -> Identifier.parse(offset.block()),
                            offset -> new Vec3(offset.x(), offset.y(), offset.z()),
                            (first, second) -> second));
            anchorOffsetSource = raw;
        }
        return anchorOffsets;
    }

    public static Anchor anchor() {
        return CLIENT_SPEC.isLoaded() ? ANCHOR.get() : DEFAULT_ANCHOR;
    }

    public static Vec3 customOffset() {
        return CLIENT_SPEC.isLoaded()
                ? new Vec3(CUSTOM_OFFSET_X.get(), CUSTOM_OFFSET_Y.get(), CUSTOM_OFFSET_Z.get())
                : Vec3.ZERO;
    }

    public static void setSize(double size) {
        SIZE.set(size);
        SIZE.save();
    }

    public static void setAnchor(Anchor anchor) {
        ANCHOR.set(anchor);
        ANCHOR.save();
    }

    public static void setCustomOffset(double x, double y, double z) {
        CUSTOM_OFFSET_X.set(x);
        CUSTOM_OFFSET_Y.set(y);
        CUSTOM_OFFSET_Z.set(z);
        ANCHOR.set(Anchor.CUSTOM);
        ANCHOR.save();
    }

    public static int maximumStacks() {
        return SERVER_SPEC.isLoaded() ? MAXIMUM_STACKS.get() : DEFAULT_MAXIMUM_STACKS;
    }

    public static double maximumRange() {
        return SERVER_SPEC.isLoaded() ? MAXIMUM_RANGE.get() : DEFAULT_MAXIMUM_RANGE;
    }

    public static boolean showLootContainers() {
        return SERVER_SPEC.isLoaded() ? SHOW_LOOT_CONTAINERS.get() : DEFAULT_SHOW_LOOT_CONTAINERS;
    }

    public static Set<Identifier> blacklist() {
        return ids(BLACKLIST);
    }

    public static Set<Identifier> whitelist() {
        return ids(WHITELIST);
    }

    public static Set<Identifier> ids(BlockList list) {
        return ids(value(list));
    }

    public static boolean add(BlockList list, Identifier id) {
        ModConfigSpec.ConfigValue<List<? extends String>> value = value(list);
        if (ids(value).contains(id)) {
            return false;
        }
        List<String> updated = new ArrayList<>(value.get());
        updated.add(id.toString());
        value.set(updated);
        value.save();
        return true;
    }

    public static boolean remove(BlockList list, Identifier id) {
        ModConfigSpec.ConfigValue<List<? extends String>> value = value(list);
        List<? extends String> current = value.get();
        List<String> updated = current.stream()
                .filter(entry -> !id.equals(Identifier.tryParse(entry)))
                .collect(Collectors.toList());
        if (updated.size() == current.size()) {
            return false;
        }
        value.set(updated);
        value.save();
        return true;
    }

    private static ModConfigSpec.ConfigValue<List<? extends String>> value(BlockList list) {
        return switch (list) {
            case BLACKLIST -> BLACKLIST;
            case WHITELIST -> WHITELIST;
        };
    }

    private static Set<Identifier> ids(ModConfigSpec.ConfigValue<List<? extends String>> value) {
        List<? extends String> entries = SERVER_SPEC.isLoaded() ? value.get() : value.getDefault();
        return entries.stream()
                .map(Identifier::tryParse)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static boolean isAnchorOffset(Object raw) {
        return raw instanceof String text
                && AnchorOffset.parse(text)
                        .filter(offset -> Identifier.tryParse(offset.block()) != null)
                        .isPresent();
    }

    private static boolean isIdentifier(Object raw) {
        return raw instanceof String text && Identifier.tryParse(text.toLowerCase(Locale.ROOT)) != null;
    }
}
