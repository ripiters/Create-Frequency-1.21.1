package dev.ripiters.create_frequency.common.network.controller;

import dev.ripiters.create_frequency.common.CFDataComponents;
import dev.ripiters.create_frequency.common.link.controller.LecternFrequencyControllerBlockEntity;
import dev.ripiters.create_frequency.common.network.CFPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FrequencyControllerBindPacket extends FrequencyControllerPacketBase {

    public static final StreamCodec<RegistryFriendlyByteBuf, FrequencyControllerBindPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), p -> p.frequencies,
            ByteBufCodecs.VAR_INT, p -> p.singleButtonIndex,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), p -> Optional.ofNullable(p.getLecternPos()),
            FrequencyControllerBindPacket::new
    );

    private final List<Float> frequencies;
    private final int singleButtonIndex;

    public FrequencyControllerBindPacket(List<Float> frequencies, Optional<BlockPos> lecternPos) {
        super(lecternPos.orElse(null));
        this.frequencies = frequencies;
        this.singleButtonIndex = -1;
    }

    public FrequencyControllerBindPacket(int button, float frequency, Optional<BlockPos> lecternPos) {
        super(lecternPos.orElse(null));
        this.frequencies = List.of(frequency);
        this.singleButtonIndex = button;
    }

    private FrequencyControllerBindPacket(List<Float> frequencies, int singleButtonIndex, Optional<BlockPos> lecternPos) {
        super(lecternPos.orElse(null));
        this.frequencies = frequencies;
        this.singleButtonIndex = singleButtonIndex;
    }

    @Override
    protected void handleItem(ServerPlayer player, ItemStack heldItem) {
        if (singleButtonIndex == -1) {
            heldItem.set(CFDataComponents.CONTROLLER_BINDS.get(), frequencies);
        } else {
            List<Float> current = new ArrayList<>(heldItem.getOrDefault(CFDataComponents.CONTROLLER_BINDS.get(),
                    List.of(0f, 0f, 0f, 0f, 0f, 0f)));

            while (current.size() < 6) current.add(0f);

            if (singleButtonIndex >= 0 && singleButtonIndex < 6) {
                float newFreq = frequencies.get(0);
                current.set(singleButtonIndex, newFreq < 0 ? 0f : newFreq);
                heldItem.set(CFDataComponents.CONTROLLER_BINDS.get(), current);
            }
        }
    }

    @Override
    protected void handleLectern(ServerPlayer player, @UnknownNullability LecternFrequencyControllerBlockEntity lectern) {
        ItemStack controller = lectern.getController();
        if (!controller.isEmpty()) {
            handleItem(player, controller);
            lectern.setChanged();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CFPackets.FREQUENCY_CONTROLLER_BIND;
    }
}