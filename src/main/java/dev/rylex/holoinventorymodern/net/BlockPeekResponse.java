package dev.rylex.holoinventorymodern.net;

import dev.rylex.holoinventorymodern.HoloInventoryModern;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record BlockPeekResponse(BlockPos pos, Component name, List<ItemStack> stacks) implements CustomPacketPayload {

    public static final Type<BlockPeekResponse> TYPE = new Type<>(HoloInventoryModern.id("block_peek_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockPeekResponse> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            BlockPeekResponse::pos,
            ComponentSerialization.STREAM_CODEC,
            BlockPeekResponse::name,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC,
            BlockPeekResponse::stacks,
            BlockPeekResponse::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
