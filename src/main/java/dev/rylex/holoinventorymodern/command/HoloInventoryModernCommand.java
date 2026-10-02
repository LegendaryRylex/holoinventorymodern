package dev.rylex.holoinventorymodern.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.rylex.holoinventorymodern.HoloInventoryModern;
import dev.rylex.holoinventorymodern.HoloInventoryModernConfig;
import dev.rylex.holoinventorymodern.HoloInventoryModernConfig.BlockList;
import dev.rylex.holoinventorymodern.HoloInventoryModernTags;
import java.util.Locale;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = HoloInventoryModern.MOD_ID)
public final class HoloInventoryModernCommand {

    private static final String BLOCK = "block";

    private HoloInventoryModernCommand() {}

    @SubscribeEvent
    static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal(HoloInventoryModern.MOD_ID)
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(list(BlockList.BLACKLIST))
                .then(list(BlockList.WHITELIST)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> list(BlockList list) {
        return Commands.literal(key(list))
                .then(Commands.literal("add")
                        .then(Commands.argument(BLOCK, ResourceLocationArgument.id())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                        BuiltInRegistries.BLOCK.keySet().stream()
                                                .filter(id -> !HoloInventoryModernConfig.ids(list)
                                                        .contains(id)),
                                        builder))
                                .executes(context -> add(context, list))))
                .then(Commands.literal("remove")
                        .then(Commands.argument(BLOCK, ResourceLocationArgument.id())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                        HoloInventoryModernConfig.ids(list), builder))
                                .executes(context -> remove(context, list))))
                .then(Commands.literal("list").executes(context -> show(context.getSource(), list)));
    }

    private static int add(CommandContext<CommandSourceStack> context, BlockList list) {
        ResourceLocation id = ResourceLocationArgument.getId(context, BLOCK);
        CommandSourceStack source = context.getSource();
        if (!BuiltInRegistries.BLOCK.containsKey(id)) {
            source.sendFailure(Component.translatable("holoinventorymodern.command.unknownBlock", id.toString()));
            return 0;
        }
        if (!HoloInventoryModernConfig.add(list, id)) {
            source.sendFailure(message(list, "already", id));
            return 0;
        }
        source.sendSuccess(() -> message(list, "added", id), true);
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> context, BlockList list) {
        ResourceLocation id = ResourceLocationArgument.getId(context, BLOCK);
        CommandSourceStack source = context.getSource();
        if (!HoloInventoryModernConfig.remove(list, id)) {
            source.sendFailure(message(list, "missing", id));
            return 0;
        }
        source.sendSuccess(() -> message(list, "removed", id), true);
        if (taggedByDatapack(list, id)) {
            source.sendSuccess(() -> message(list, "tagged", id), false);
        }
        return 1;
    }

    private static int show(CommandSourceStack source, BlockList list) {
        var ids = HoloInventoryModernConfig.ids(list);
        if (ids.isEmpty()) {
            source.sendSuccess(
                    () -> Component.translatable("holoinventorymodern.command." + key(list) + ".empty"), false);
            return 0;
        }
        String joined = ids.stream().map(ResourceLocation::toString).sorted().collect(Collectors.joining(", "));
        source.sendSuccess(
                () -> Component.translatable("holoinventorymodern.command." + key(list) + ".list", ids.size(), joined),
                false);
        return ids.size();
    }

    private static boolean taggedByDatapack(BlockList list, ResourceLocation id) {
        TagKey<Block> tag =
                list == BlockList.BLACKLIST ? HoloInventoryModernTags.BLACKLIST : HoloInventoryModernTags.WHITELIST;
        return BuiltInRegistries.BLOCK
                .getOptional(id)
                .map(block -> block.defaultBlockState().is(tag))
                .orElse(false);
    }

    private static Component message(BlockList list, String outcome, ResourceLocation id) {
        return Component.translatable("holoinventorymodern.command." + key(list) + "." + outcome, id.toString());
    }

    private static String key(BlockList list) {
        return list.name().toLowerCase(Locale.ROOT);
    }
}
