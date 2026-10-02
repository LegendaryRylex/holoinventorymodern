package dev.rylex.holoinventorymodern.net;

import dev.rylex.holoinventorymodern.HoloInventoryModern;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EntityPeekRequest(int entityId) implements CustomPacketPayload {

    public static final Type<EntityPeekRequest> TYPE = new Type<>(HoloInventoryModern.id("entity_peek_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityPeekRequest> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, EntityPeekRequest::entityId, EntityPeekRequest::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
