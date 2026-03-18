package dev.ripiters.create_frequency.common.link;

import net.minecraft.core.BlockPos;

public interface IFrequencyLinkable {
    int getTransmittedStrength();
    void setReceivedStrength(int power);
    boolean isListening();
    boolean isAlive();
    float getNetworkKey();
    BlockPos getLocation();
}