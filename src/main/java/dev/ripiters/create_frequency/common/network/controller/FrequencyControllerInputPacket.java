package dev.ripiters.create_frequency.common.network.controller;

import dev.ripiters.create_frequency.common.link.controller.FrequencyControllerItem;
import dev.ripiters.create_frequency.common.link.controller.FrequencyControllerServerHandler;
import dev.ripiters.create_frequency.common.link.controller.LecternFrequencyControllerBlockEntity;
import dev.ripiters.create_frequency.common.network.CFPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.stream.Collectors;

public class FrequencyControllerInputPacket extends FrequencyControllerPacketBase {
    public static final StreamCodec<ByteBuf, FrequencyControllerInputPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()), p -> new ArrayList<>(p.activatedButtons),
            ByteBufCodecs.BOOL, p -> p.press,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), p -> Optional.ofNullable(p.getLecternPos()),
            FrequencyControllerInputPacket::new
    );

    private final Collection<Integer> activatedButtons;
    private final boolean press;

    public FrequencyControllerInputPacket(List<Integer> activatedButtons, boolean press, Optional<BlockPos> lecternPos) {
        super(lecternPos.orElse(null));
        this.activatedButtons = activatedButtons;
        this.press = press;
    }

    public FrequencyControllerInputPacket(Collection<Integer> activatedButtons, boolean press) {
        this(new ArrayList<>(activatedButtons), press, Optional.empty());
    }

    @Override
    protected void handleItem(ServerPlayer player, ItemStack heldItem) {
        if (player.isSpectator() && press) return;

        List<Float> frequencies = activatedButtons.stream()
                .map(i -> FrequencyControllerItem.getBindFrequency(heldItem, i))
                .collect(Collectors.toList());

        FrequencyControllerServerHandler.receivePressed(
                player.level(),
                player.blockPosition(),
                player.getUUID(),
                frequencies,
                press
        );
    }

    @Override
    protected void handleLectern(ServerPlayer player, @UnknownNullability LecternFrequencyControllerBlockEntity lectern) {
        handleItem(player, lectern.getController());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CFPackets.FREQUENCY_CONTROLLER_INPUT;
    }
}