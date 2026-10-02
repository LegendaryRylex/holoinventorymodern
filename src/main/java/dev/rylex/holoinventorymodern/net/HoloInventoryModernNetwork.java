package dev.rylex.holoinventorymodern.net;

import dev.rylex.holoinventorymodern.peek.ContainerPeek;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class HoloInventoryModernNetwork {

    private HoloInventoryModernNetwork() {}

    private static Consumer<BlockPeekResponse> blockResponses = payload -> {};
    private static Consumer<EntityPeekResponse> entityResponses = payload -> {};

    public static void receiveWith(Consumer<BlockPeekResponse> blocks, Consumer<EntityPeekResponse> entities) {
        blockResponses = blocks;
        entityResponses = entities;
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(
                        BlockPeekRequest.TYPE,
                        BlockPeekRequest.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> handleBlockRequest(payload, context)))
                .playToServer(
                        EntityPeekRequest.TYPE,
                        EntityPeekRequest.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> handleEntityRequest(payload, context)))
                .playToClient(
                        BlockPeekResponse.TYPE,
                        BlockPeekResponse.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> blockResponses.accept(payload)))
                .playToClient(
                        EntityPeekResponse.TYPE,
                        EntityPeekResponse.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> entityResponses.accept(payload)));
    }

    private static void handleBlockRequest(BlockPeekRequest payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        ContainerPeek.atBlock(player, payload.pos())
                .ifPresent(contents -> PacketDistributor.sendToPlayer(
                        player, new BlockPeekResponse(payload.pos(), contents.name(), contents.stacks())));
    }

    private static void handleEntityRequest(EntityPeekRequest payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        Entity entity = player.level().getEntity(payload.entityId());
        if (entity == null) {
            return;
        }
        ContainerPeek.atEntity(player, entity)
                .ifPresent(contents -> PacketDistributor.sendToPlayer(
                        player, new EntityPeekResponse(payload.entityId(), contents.name(), contents.stacks())));
    }
}
