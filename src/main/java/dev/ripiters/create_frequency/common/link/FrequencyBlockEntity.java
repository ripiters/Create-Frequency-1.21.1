package dev.ripiters.create_frequency.common.link;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.ripiters.create_frequency.common.network.FrequencyNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class FrequencyBlockEntity extends SmartBlockEntity {
    protected FrequencyLinkBehaviour link;
    protected String networkName = "";

    public FrequencyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && !level.isClientSide && level.getGameTime() % 20 == 0) {
            refreshNetworkData();
        }
    }

    public void refreshNetworkData() {
        if (level == null || level.isClientSide) return;

        String globalName = FrequencyNetworkHandler.getNetworkName(level, getFrequency());

        if (globalName.isEmpty() && !this.networkName.isEmpty()) {
            this.networkName = "";
            sendData();
            setChanged();
        }
        else if (!globalName.isEmpty() && !globalName.equals(this.networkName)) {
            this.networkName = globalName;
            sendData();
            setChanged();
        }
    }

    public void setFrequency(float freq) {
        if (link != null) {
            link.setFrequency(freq);
            refreshNetworkData();
        }
    }

    public void setNetworkName(String name) {
        this.networkName = (name == null || name.isEmpty()) ? "" : name;

        if (level != null && !level.isClientSide) {
            FrequencyNetworkHandler.setNetworkName(level, getFrequency(), this.networkName);
            sendData();
            setChanged();
        }
    }

    public float getFrequency() {
        return link != null ? link.getNetworkKey() : 0.0f;
    }

    public String getNetworkName() {
        return networkName;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putString("NetworkName", networkName);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        networkName = tag.getString("NetworkName");
        super.read(tag, registries, clientPacket);
    }
}