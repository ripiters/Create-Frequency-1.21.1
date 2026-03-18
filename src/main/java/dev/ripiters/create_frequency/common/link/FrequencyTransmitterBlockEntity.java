package dev.ripiters.create_frequency.common.link;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSupportBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ripiters.create_frequency.CreateFrequency;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public class FrequencyTransmitterBlockEntity extends FrequencyBlockEntity {

    public FactoryPanelSupportBehaviour panelSupport;

    public FrequencyTransmitterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {super(type, pos, state);}

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        link = FrequencyLinkBehaviour.transmitter(this, () -> {
            BlockState state = getBlockState();
            return (state.hasProperty(FrequencySignalTransmitter.POWERED) &&
                    state.getValue(FrequencySignalTransmitter.POWERED)) ? 15 : 0;
        });
        behaviours.add(link);

        behaviours.add(panelSupport = new FactoryPanelSupportBehaviour(this,
                () -> link != null && link.isListening(),
                () -> (getBlockState().hasProperty(FrequencySignalTransmitter.POWERED) &&
                        getBlockState().getValue(FrequencySignalTransmitter.POWERED)),
                () -> link.getTransmittedStrength()
        ));
    }

    public void transmit(int strength) {
        if (level == null || level.isClientSide) return;
        if (link != null) {
            CreateFrequency.FREQUENCY_NETWORK_HANDLER.updateNetworkOf(level, link);
        }
    }
}