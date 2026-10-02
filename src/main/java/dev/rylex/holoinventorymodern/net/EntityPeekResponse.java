package dev.rylex.holoinventorymodern.net;

import dev.rylex.holoinventorymodern.HoloInventoryModern;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record EntityPeekResponse(int entityId, Component name, List<ItemStack> stacks) implements CustomPacketPayload {

    public static final Type<EntityPeekResponse> TYPE = new Type<>(HoloInventoryModern.id("entity_peek_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityPeekResponse> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            EntityPeekResponse::entityId,
            ComponentSerialization.STREAM_CODEC,
            EntityPeekResponse::name,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC,
            EntityPeekResponse::stacks,
            EntityPeekResponse::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
