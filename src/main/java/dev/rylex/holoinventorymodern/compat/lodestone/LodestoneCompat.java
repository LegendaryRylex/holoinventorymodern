package dev.rylex.holoinventorymodern.compat.lodestone;

import dev.rylex.holoinventorymodern.peek.ItemHandlers;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Lodestone block entities hand out their inventory through {@code IItemHandlerSupplier}, and several of them, Malum's
 * spirit jar among them, never register that handler as a block capability, so it is only reachable by calling the
 * supplier directly.
 */
public final class LodestoneCompat {

    private LodestoneCompat() {}

    private static final String SUPPLIER = "team.lodestar.lodestone.systems.blockentity.IItemHandlerSupplier";

    private static final ItemHandlers.Source ABSENT = side -> null;

    @Nullable
    private static final Class<?> TYPE = loadType();

    @Nullable
    private static final MethodHandle GET_INVENTORY = loadHandle();

    public static ItemHandlers.Source source(@Nullable BlockEntity blockEntity) {
        if (GET_INVENTORY == null || TYPE == null || !TYPE.isInstance(blockEntity)) {
            return ABSENT;
        }
        return side -> invoke(blockEntity, side);
    }

    @Nullable
    private static IItemHandler invoke(BlockEntity blockEntity, @Nullable Direction side) {
        try {
            return (IItemHandler) GET_INVENTORY.invoke(blockEntity, side);
        } catch (Throwable unavailable) {
            return null;
        }
    }

    @Nullable
    private static Class<?> loadType() {
        try {
            return ModList.get().isLoaded("lodestone") ? Class.forName(SUPPLIER) : null;
        } catch (ClassNotFoundException | RuntimeException missing) {
            return null;
        }
    }

    @Nullable
    private static MethodHandle loadHandle() {
        if (TYPE == null) {
            return null;
        }
        try {
            return MethodHandles.publicLookup()
                    .findVirtual(TYPE, "getInventory", MethodType.methodType(IItemHandler.class, Direction.class));
        } catch (ReflectiveOperationException | RuntimeException missing) {
            return null;
        }
    }
}
