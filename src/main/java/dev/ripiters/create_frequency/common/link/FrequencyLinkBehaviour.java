package dev.ripiters.create_frequency.common.link;

import dev.ripiters.create_frequency.CreateFrequency;
import dev.ripiters.create_frequency.common.network.FrequencyNetworkHandler;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class FrequencyLinkBehaviour extends BlockEntityBehaviour implements IFrequencyLinkable {
    public static final BehaviourType<FrequencyLinkBehaviour> TYPE = new BehaviourType<>();

    enum Mode { TRANSMIT, RECEIVE }

    private float frequency = 0.0f;
    private Mode mode;
    private IntSupplier transmission;
    private IntConsumer signalCallback;
    public boolean newPosition = true;

    protected FrequencyLinkBehaviour(SmartBlockEntity be) {
        super(be);
    }

    public String getNetworkName() {
        if (blockEntity instanceof FrequencyBlockEntity fbe) {
            return fbe.getNetworkName();
        }
        return "";
    }

    public static FrequencyLinkBehaviour receiver(SmartBlockEntity be, IntConsumer signalCallback) {
        FrequencyLinkBehaviour behaviour = new FrequencyLinkBehaviour(be);
        behaviour.signalCallback = signalCallback;
        behaviour.mode = Mode.RECEIVE;
        return behaviour;
    }

    public static FrequencyLinkBehaviour transmitter(SmartBlockEntity be, IntSupplier transmission) {
        FrequencyLinkBehaviour behaviour = new FrequencyLinkBehaviour(be);
        behaviour.transmission = transmission;
        behaviour.mode = Mode.TRANSMIT;
        return behaviour;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (getWorld() != null && !getWorld().isClientSide) {
            getHandler().addToNetwork(getWorld(), this);
        }
    }

    @Override
    public void unload() {
        super.unload();
        if (getWorld() != null && !getWorld().isClientSide) {
            getHandler().removeFromNetwork(getWorld(), this, false);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (getWorld() != null && !getWorld().isClientSide) {
            getHandler().removeFromNetwork(getWorld(), this, true);
        }
    }

    public void setFrequency(float freq) {
        if (Float.compare(this.frequency, freq) == 0) return;

        Level world = getWorld();
        if (world != null && !world.isClientSide) {
            getHandler().removeFromNetwork(world, this, true);

            if (isListening()) {
                setReceivedStrength(0);
            }

            this.frequency = freq;

            getHandler().addToNetwork(world, this);
            getHandler().updateNetworkOf(world, this);
        } else {
            this.frequency = freq;
        }
        blockEntity.sendData();
    }

    @Override public boolean isListening() { return mode == Mode.RECEIVE; }
    @Override public int getTransmittedStrength() { return mode == Mode.TRANSMIT ? transmission.getAsInt() : 0; }

    @Override
    public void setReceivedStrength(int power) {
        if (signalCallback != null) {
            signalCallback.accept(power);
        }
    }

    @Override public float getNetworkKey() { return frequency; }
    @Override public boolean isAlive() { return !blockEntity.isRemoved(); }
    @Override public BlockPos getLocation() { return getPos(); }
    @Override public BehaviourType<?> getType() { return TYPE; }
    private FrequencyNetworkHandler getHandler() { return CreateFrequency.FREQUENCY_NETWORK_HANDLER; }

    @Override
    public void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        nbt.putFloat("Frequency", frequency);
        super.write(nbt, registries, clientPacket);
    }

    @Override
    public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        this.frequency = nbt.getFloat("Frequency");
        super.read(nbt, registries, clientPacket);
    }
}