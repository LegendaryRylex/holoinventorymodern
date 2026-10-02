package dev.rylex.holoinventorymodern;

import dev.rylex.holoinventorymodern.client.HoloInventoryModernClient;
import dev.rylex.holoinventorymodern.net.HoloInventoryModernNetwork;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(HoloInventoryModern.MOD_ID)
public final class HoloInventoryModern {
    public static final String MOD_ID = "holoinventorymodern";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public HoloInventoryModern(IEventBus modBus, ModContainer container, Dist dist) {
        container.registerConfig(ModConfig.Type.SERVER, HoloInventoryModernConfig.SERVER_SPEC);
        modBus.addListener(HoloInventoryModernNetwork::registerPayloads);
        if (dist.isClient()) {
            container.registerConfig(ModConfig.Type.CLIENT, HoloInventoryModernConfig.CLIENT_SPEC);
            HoloInventoryModernClient.init(container, modBus);
        }
    }
}
