package dev.rylex.holoinventorymodern.client;

import dev.rylex.holoinventorymodern.net.HoloInventoryModernNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

public final class HoloInventoryModernClient {

    private HoloInventoryModernClient() {}

    public static void init(ModContainer container, IEventBus modBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modBus.addListener(HoloInventoryModernKeys::register);
        HoloInventoryModernNetwork.receiveWith(PeekTracker::accept, PeekTracker::accept);
        NeoForge.EVENT_BUS.addListener(HoloInventoryModernClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(HoloInventoryModernClient::onLoggingOut);
        NeoForge.EVENT_BUS.addListener(HoloInventoryModernClient::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(ClientCommand::register);
        NeoForge.EVENT_BUS.addListener(HologramRenderer::render);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (HoloInventoryModernKeys.TOGGLE.consumeClick()) {
            announce(minecraft.player, PeekTracker.toggle());
        }
        PeekTracker.tick(minecraft);
    }

    private static void announce(LocalPlayer player, boolean enabled) {
        if (player != null) {
            player.displayClientMessage(
                    Component.translatable(
                            enabled ? "holoinventorymodern.message.enabled" : "holoinventorymodern.message.disabled"),
                    true);
        }
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        PeekTracker.clear();
    }

    private static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            PeekTracker.clear();
        }
    }
}
