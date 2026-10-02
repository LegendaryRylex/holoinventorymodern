package dev.rylex.holoinventorymodern.net;

import dev.rylex.holoinventorymodern.HoloInventoryModern;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record BlockPeekRequest(BlockPos pos) implements CustomPacketPayload {

    public static final Type<BlockPeekRequest> TYPE = new Type<>(HoloInventoryModern.id("block_peek_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockPeekRequest> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, BlockPeekRequest::pos, BlockPeekRequest::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
