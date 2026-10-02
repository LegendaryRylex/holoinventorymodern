package dev.rylex.holoinventorymodern.client;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.rylex.holoinventorymodern.HoloInventoryModern;
import dev.rylex.holoinventorymodern.HoloInventoryModernConfig;
import dev.rylex.holoinventorymodern.HoloInventoryModernConfig.Anchor;
import java.util.List;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

final class ClientCommand {

    private ClientCommand() {}

    private static final String X = "x: -left +right";
    private static final String Y = "y: -down +up";
    private static final String Z = "z: -back +forward";
    private static final String SIZE = "size: 1 = default";
    private static final List<String> OFFSETS = List.of("0", "0.5", "1", "-0.5", "-1");
    private static final List<String> SIZES = List.of("1", "1.5", "2", "2.5", "3");

    static void register(RegisterClientCommandsEvent event) {
        double offsetLimit = HoloInventoryModernConfig.CUSTOM_OFFSET_LIMIT;
        event.getDispatcher()
                .register(Commands.literal(HoloInventoryModern.MOD_ID)
                        .then(Commands.literal("offset")
                                .then(preset(Anchor.CENTER))
                                .then(preset(Anchor.TOP))
                                .then(preset(Anchor.ABOVE))
                                .then(Commands.literal("custom")
                                        .then(number(X, -offsetLimit, offsetLimit, OFFSETS, "offset.hint.x")
                                                .then(number(Y, -offsetLimit, offsetLimit, OFFSETS, "offset.hint.y")
                                                        .then(number(
                                                                        Z,
                                                                        -offsetLimit,
                                                                        offsetLimit,
                                                                        OFFSETS,
                                                                        "offset.hint.z")
                                                                .executes(ClientCommand::custom))))))
                        .then(Commands.literal("size")
                                .then(number(
                                                SIZE,
                                                HoloInventoryModernConfig.MINIMUM_SIZE,
                                                HoloInventoryModernConfig.MAXIMUM_SIZE,
                                                SIZES,
                                                "size.hint")
                                        .executes(ClientCommand::size))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> preset(Anchor anchor) {
        String name = anchor.name().toLowerCase(Locale.ROOT);
        return Commands.literal(name).executes(context -> {
            HoloInventoryModernConfig.setAnchor(anchor);
            context.getSource().sendSuccess(() -> message("offset." + name), false);
            return 1;
        });
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Double> number(
            String name, double minimum, double maximum, List<String> suggested, String hint) {
        return Commands.argument(name, DoubleArgumentType.doubleArg(minimum, maximum))
                .suggests((context, builder) -> {
                    Component tooltip = message(hint);
                    for (String value : suggested) {
                        if (value.startsWith(builder.getRemaining())) {
                            builder.suggest(value, tooltip);
                        }
                    }
                    return builder.buildFuture();
                });
    }

    private static int custom(CommandContext<CommandSourceStack> context) {
        double x = DoubleArgumentType.getDouble(context, X);
        double y = DoubleArgumentType.getDouble(context, Y);
        double z = DoubleArgumentType.getDouble(context, Z);
        HoloInventoryModernConfig.setCustomOffset(x, y, z);
        context.getSource().sendSuccess(() -> message("offset.custom", x, y, z), false);
        return 1;
    }

    private static int size(CommandContext<CommandSourceStack> context) {
        double size = DoubleArgumentType.getDouble(context, SIZE);
        HoloInventoryModernConfig.setSize(size);
        context.getSource().sendSuccess(() -> message("size", size), false);
        return 1;
    }

    private static Component message(String key, Object... arguments) {
        return Component.translatable("holoinventorymodern.command." + key, arguments);
    }
}
