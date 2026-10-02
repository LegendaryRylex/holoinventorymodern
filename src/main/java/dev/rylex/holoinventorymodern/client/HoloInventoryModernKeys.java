package dev.rylex.holoinventorymodern.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class HoloInventoryModernKeys {

    private HoloInventoryModernKeys() {}

    public static final String CATEGORY = "key.categories.holoinventorymodern";

    public static final KeyMapping PEEK = new KeyMapping(
            "key.holoinventorymodern.peek",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY);

    public static final KeyMapping TOGGLE = new KeyMapping(
            "key.holoinventorymodern.toggle",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY);

    static void register(RegisterKeyMappingsEvent event) {
        event.register(PEEK);
        event.register(TOGGLE);
    }
}
