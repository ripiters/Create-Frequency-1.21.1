package dev.ripiters.create_frequency.common;

import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.ripiters.create_frequency.CreateFrequency;
import dev.ripiters.create_frequency.common.link.FrequencyReceiverBlockEntity;
import dev.ripiters.create_frequency.common.link.FrequencyTransmitterBlockEntity;
import dev.ripiters.create_frequency.common.link.controller.LecternFrequencyControllerBlockEntity;
import dev.ripiters.create_frequency.common.link.controller.LecternFrequencyControllerRenderer;

public class CFBlockEntityTypes {
    private static final CreateRegistrate REGISTRATE = CreateFrequency.getRegistrate();

    public static final BlockEntityEntry<FrequencyTransmitterBlockEntity> LINKED_TRANSMITTER = REGISTRATE
            .blockEntity("frequency_transmitter", FrequencyTransmitterBlockEntity::new)
            .validBlocks(CFBlocks.FREQUENCY_TRANSMITTER)
            .renderer(() -> SmartBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<FrequencyReceiverBlockEntity> LINKED_RECEIVER = REGISTRATE
            .blockEntity("frequency_receiver", FrequencyReceiverBlockEntity::new)
            .validBlocks(CFBlocks.FREQUENCY_RECEIVER)
            .renderer(() -> SmartBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<LecternFrequencyControllerBlockEntity> LECTERN_FREQUENCY_CONTROLLER = REGISTRATE
            .blockEntity("lectern_frequency_controller", LecternFrequencyControllerBlockEntity::new)
            .validBlocks(CFBlocks.LECTERN_FREQUENCY_CONTROLLER)
            .renderer(() -> LecternFrequencyControllerRenderer::new)
            .register();

    public static void register() {}
}