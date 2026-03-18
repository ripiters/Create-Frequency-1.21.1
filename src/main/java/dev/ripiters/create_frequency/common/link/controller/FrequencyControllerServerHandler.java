package dev.ripiters.create_frequency.common.link.controller;

import com.simibubi.create.foundation.advancement.AllAdvancements;
import dev.ripiters.create_frequency.CreateFrequency;
import dev.ripiters.create_frequency.common.link.FrequencyLinkBehaviour;
import dev.ripiters.create_frequency.common.link.IFrequencyLinkable;
import net.createmod.catnip.data.IntAttached;
import net.createmod.catnip.data.WorldAttached;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import java.util.*;

public class FrequencyControllerServerHandler {
    public static WorldAttached<Map<UUID, Collection<FrequencyControllerServerHandler.ManualFrequencyEntry>>> receivedInputs =
            new WorldAttached<>($ -> new HashMap<>());
    static final int TIMEOUT = 30;

    public static void tick(LevelAccessor world) {
        Map<UUID, Collection<FrequencyControllerServerHandler.ManualFrequencyEntry>> map = receivedInputs.get(world);
        for (Iterator<Map.Entry<UUID, Collection<FrequencyControllerServerHandler.ManualFrequencyEntry>>> iterator = map.entrySet()
                .iterator(); iterator.hasNext(); ) {

            Map.Entry<UUID, Collection<FrequencyControllerServerHandler.ManualFrequencyEntry>> entry = iterator.next();
            Collection<FrequencyControllerServerHandler.ManualFrequencyEntry> list = entry.getValue();

            for (Iterator<FrequencyControllerServerHandler.ManualFrequencyEntry> entryIterator = list.iterator(); entryIterator.hasNext(); ) {
                FrequencyControllerServerHandler.ManualFrequencyEntry manualFrequencyEntry = entryIterator.next();
                manualFrequencyEntry.decrement();
                if (!manualFrequencyEntry.isAlive()) {
                    CreateFrequency.FREQUENCY_NETWORK_HANDLER.removeFromNetwork(world, manualFrequencyEntry, true);
                    entryIterator.remove();
                }
            }

            if (list.isEmpty())
                iterator.remove();
        }
    }

    public static void receivePressed(LevelAccessor world, BlockPos pos, UUID uniqueID, List<Float> frequencies, boolean pressed) {
        Map<UUID, Collection<FrequencyControllerServerHandler.ManualFrequencyEntry>> map = receivedInputs.get(world);
        Collection<FrequencyControllerServerHandler.ManualFrequencyEntry> list = map.computeIfAbsent(uniqueID, $ -> new ArrayList<>());

        WithNext:
        for (float freq : frequencies) {
            for (ManualFrequencyEntry entry : list) {
                if (Float.compare(entry.getSecond(), freq) == 0) {
                    if (!pressed) entry.setFirst(0);
                    else entry.updatePosition(pos);
                    continue WithNext;
                }
            }

            if (!pressed)
                continue;

            FrequencyControllerServerHandler.ManualFrequencyEntry entry = new FrequencyControllerServerHandler.ManualFrequencyEntry(pos, freq);
            CreateFrequency.FREQUENCY_NETWORK_HANDLER.addToNetwork(world, entry);
            list.add(entry);

            for (IFrequencyLinkable linkable : CreateFrequency.FREQUENCY_NETWORK_HANDLER.getNetworkOf(world, entry))
                if (linkable instanceof FrequencyLinkBehaviour lb && lb.isListening())
                    AllAdvancements.LINKED_CONTROLLER.awardTo(world.getPlayerByUUID(uniqueID));
        }
    }

    static class ManualFrequencyEntry extends IntAttached<Float> implements IFrequencyLinkable {
        private BlockPos pos;

        public ManualFrequencyEntry(BlockPos pos, float frequency) {
            super(TIMEOUT, frequency);
            this.pos = pos;
        }

        public void updatePosition(BlockPos pos) {
            this.pos = pos;
            setFirst(TIMEOUT);
        }

        @Override public int getTransmittedStrength() { return isAlive() ? 15 : 0; }
        @Override public boolean isAlive() { return getFirst() > 0; }
        @Override public BlockPos getLocation() { return pos; }
        @Override public void setReceivedStrength(int power) {}
        @Override public boolean isListening() { return false; }
        @Override public float getNetworkKey() { return getSecond(); }

    }
}