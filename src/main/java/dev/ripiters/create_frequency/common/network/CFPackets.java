package dev.ripiters.create_frequency.common.network;

import com.simibubi.create.content.redstone.link.controller.LinkedControllerStopLecternPacket;
import dev.ripiters.create_frequency.CreateFrequency;
import dev.ripiters.create_frequency.common.network.controller.FrequencyControllerBindPacket;
import dev.ripiters.create_frequency.common.network.controller.FrequencyControllerInputPacket;
import dev.ripiters.create_frequency.common.network.controller.FrequencyControllerStopLecternPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = CreateFrequency.MODID)
public class CFPackets {

    public static final CustomPacketPayload.Type<ConfigureFrequencyPacket> CONFIGURE_FREQUENCY =
            new CustomPacketPayload.Type<>(CreateFrequency.resourceLocation("configure_frequency"));

    public static final CustomPacketPayload.Type<FrequencyControllerBindPacket> FREQUENCY_CONTROLLER_BIND =
            new CustomPacketPayload.Type<>(CreateFrequency.resourceLocation("frequency_controller_bind"));

    public static final CustomPacketPayload.Type<FrequencyControllerInputPacket> FREQUENCY_CONTROLLER_INPUT =
            new CustomPacketPayload.Type<>(CreateFrequency.resourceLocation("frequency_controller_input"));

    public static final CustomPacketPayload.Type<FrequencyControllerStopLecternPacket> FREQUENCY_CONTROLLER_USE_LECTERN =
            new CustomPacketPayload.Type<>(CreateFrequency.resourceLocation("frequency_controller_use_lectern"));

    @SubscribeEvent
    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CreateFrequency.MODID);

        registrar.playToServer(
                CONFIGURE_FREQUENCY,
                ConfigureFrequencyPacket.STREAM_CODEC,
                ConfigureFrequencyPacket::handle
        );

        registrar.playToServer(
                FREQUENCY_CONTROLLER_BIND,
                FrequencyControllerBindPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> payload.handle((ServerPlayer) context.player()))
        );

        registrar.playToServer(
                FREQUENCY_CONTROLLER_INPUT,
                FrequencyControllerInputPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> payload.handle((ServerPlayer) context.player()))
        );

        registrar.playToServer(
                FREQUENCY_CONTROLLER_USE_LECTERN,
                FrequencyControllerStopLecternPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> payload.handle((ServerPlayer) context.player()))
        );
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void register() {}
}