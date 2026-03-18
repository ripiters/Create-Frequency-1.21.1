package dev.ripiters.create_frequency.common.network;

import com.simibubi.create.infrastructure.config.AllConfigs;
import dev.ripiters.create_frequency.common.link.FrequencyLinkBehaviour;
import dev.ripiters.create_frequency.common.link.IFrequencyLinkable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FrequencyNetworkHandler {
    static final Map<LevelAccessor, Map<Float, Set<IFrequencyLinkable>>> connections = new IdentityHashMap<>();

    public void onLoadWorld(LevelAccessor world) {
        connections.put(world, new ConcurrentHashMap<>());
    }

    public void onUnloadWorld(LevelAccessor world) {
        connections.remove(world);
    }

    public Set<IFrequencyLinkable> getNetworkOf(LevelAccessor world, IFrequencyLinkable actor) {
        return connections.computeIfAbsent(world, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(actor.getNetworkKey(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }

    public void addToNetwork(LevelAccessor world, IFrequencyLinkable actor) {
        getNetworkOf(world, actor).add(actor);
        updateNetworkOf(world, actor);
    }

    /**
     * @param isRemoving true if is deleted from the world.
     */
    public void removeFromNetwork(LevelAccessor world, IFrequencyLinkable actor, boolean isRemoving) {
        Map<Float, Set<IFrequencyLinkable>> networksInWorld = connections.get(world);
        if (networksInWorld == null) return;

        float key = actor.getNetworkKey();
        Set<IFrequencyLinkable> network = networksInWorld.get(key);

        if (network != null) {
            network.remove(actor);

            if (!network.isEmpty()) {
                updateNetworkOf(world, network.iterator().next());
            } else {
                networksInWorld.remove(key);

                if (isRemoving && world instanceof Level level) {
                    setNetworkName(level, key, "");
                }
            }
        }
    }

    public void updateNetworkOf(LevelAccessor world, IFrequencyLinkable actor) {
        Set<IFrequencyLinkable> network = getNetworkOf(world, actor);
        int power = 0;
        for (IFrequencyLinkable other : network) {
            if (other.isAlive() && withinRange(actor, other)) {
                power = Math.max(other.getTransmittedStrength(), power);
            }
        }
        for (IFrequencyLinkable other : network) {
            if (other.isListening() && withinRange(actor, other)) {
                if (other instanceof FrequencyLinkBehaviour flb) flb.newPosition = true;
                other.setReceivedStrength(power);
            }
        }
    }

    public static boolean withinRange(IFrequencyLinkable from, IFrequencyLinkable to) {
        double range = AllConfigs.server().logistics.linkRange.get();
        return from.getLocation().distSqr(to.getLocation()) <= range * range;
    }

    public static void setNetworkName(Level level, float frequency, String name) {
        if (level instanceof ServerLevel serverLevel) {
            FrequencyNetworkData data = FrequencyNetworkData.get(serverLevel);
            if (name == null || name.isEmpty()) {
                data.names.remove(frequency);
            } else {
                data.names.put(frequency, name);
            }
            data.setDirty();
        }
    }

    public static String getNetworkName(Level level, float frequency) {
        if (level instanceof ServerLevel serverLevel) {
            return FrequencyNetworkData.get(serverLevel).names.getOrDefault(frequency, "");
        }
        return "";
    }
}