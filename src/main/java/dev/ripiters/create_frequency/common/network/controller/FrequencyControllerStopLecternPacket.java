package dev.ripiters.create_frequency.common.network.controller;

import java.util.Objects;

import dev.ripiters.create_frequency.common.link.controller.LecternFrequencyControllerBlockEntity;
import dev.ripiters.create_frequency.common.network.CFPackets;
import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class FrequencyControllerStopLecternPacket extends FrequencyControllerPacketBase {
    public static final StreamCodec<ByteBuf, FrequencyControllerStopLecternPacket> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
            FrequencyControllerStopLecternPacket::new, FrequencyControllerPacketBase::getLecternPos
    );

    public FrequencyControllerStopLecternPacket(BlockPos lecternPos) {
        super(Objects.requireNonNull(lecternPos));
    }

    @Override
    protected void handleLectern(ServerPlayer player, LecternFrequencyControllerBlockEntity lectern) {
        lectern.tryStopUsing(player);
    }

    @Override
    protected void handleItem(ServerPlayer player, ItemStack heldItem) { }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CFPackets.FREQUENCY_CONTROLLER_USE_LECTERN;
    }
}
