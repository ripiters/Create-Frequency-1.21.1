package dev.ripiters.create_frequency.common.network;

import dev.ripiters.create_frequency.CreateFrequency;
import dev.ripiters.create_frequency.common.link.FrequencyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ConfigureFrequencyPacket(BlockPos pos, float frequency, String networkName) implements CustomPacketPayload {

    public static final Type<ConfigureFrequencyPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateFrequency.MODID, "configure_frequency"));

    public static final StreamCodec<FriendlyByteBuf, ConfigureFrequencyPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ConfigureFrequencyPacket::pos,
            ByteBufCodecs.FLOAT, ConfigureFrequencyPacket::frequency,
            ByteBufCodecs.STRING_UTF8, ConfigureFrequencyPacket::networkName,
            ConfigureFrequencyPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Level level = player.level();

            if (player.blockPosition().distSqr(pos) > 4096) return;

            String finalName = networkName;
            if (finalName.isEmpty()) {
                finalName = FrequencyNetworkHandler.getNetworkName(level, frequency);
            } else {
                FrequencyNetworkHandler.setNetworkName(level, frequency, finalName);
            }

            if (level.isLoaded(pos)) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof FrequencyBlockEntity frequencyBE) {
                    frequencyBE.setFrequency(frequency);
                    frequencyBE.setNetworkName(finalName);
                    frequencyBE.setChanged();
                    level.sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), 3);
                }
            }
        });
    }
}