package dev.ripiters.create_frequency.common.link;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public class FrequencyReceiverBlockEntity extends FrequencyBlockEntity {
    private int receivedSignal = 0;

    public FrequencyReceiverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        link = FrequencyLinkBehaviour.receiver(this, this::onSignalReceived);
        behaviours.add(link);
    }

    private void onSignalReceived(int power) {
        if (this.receivedSignal == power) return;

        this.receivedSignal = power;
        BlockState state = getBlockState();
        boolean shouldBePowered = power > 0;

        if (state.getValue(FrequencySignalReceiver.POWERED) != shouldBePowered) {
            level.setBlock(worldPosition, state.setValue(FrequencySignalReceiver.POWERED, shouldBePowered), 3);
            level.updateNeighborsAt(worldPosition, state.getBlock());
        }
    }

    private void notifySignalUpdate(BlockState state) {
        level.updateNeighborsAt(worldPosition, state.getBlock());
        Direction attachedFace = state.getValue(FrequencySignalReceiver.FACING).getOpposite();
        level.updateNeighborsAt(worldPosition.relative(attachedFace), state.getBlock());
    }

    public int getReceivedSignal() {
        return receivedSignal;
    }
}