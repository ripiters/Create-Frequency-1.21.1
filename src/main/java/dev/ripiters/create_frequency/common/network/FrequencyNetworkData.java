package dev.ripiters.create_frequency.common.network;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashMap;
import java.util.Map;

public class FrequencyNetworkData extends SavedData {
    public final Map<Float, String> names = new HashMap<>();

    public static FrequencyNetworkData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(FrequencyNetworkData::new, FrequencyNetworkData::load, null),
                "create_frequency_networks"
        );
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag namesTag = new CompoundTag();
        names.forEach((freq, name) -> namesTag.putString(String.valueOf(freq), name));
        tag.put("NetworkNames", namesTag);
        return tag;
    }

    public static FrequencyNetworkData load(CompoundTag tag, HolderLookup.Provider registries) {
        FrequencyNetworkData data = new FrequencyNetworkData();
        CompoundTag namesTag = tag.getCompound("NetworkNames");
        for (String key : namesTag.getAllKeys()) {
            try {
                data.names.put(Float.parseFloat(key), namesTag.getString(key));
            } catch (NumberFormatException ignored) {}
        }
        return data;
    }
}