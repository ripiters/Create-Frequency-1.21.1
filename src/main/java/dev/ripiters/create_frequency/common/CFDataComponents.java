package dev.ripiters.create_frequency.common;

import dev.ripiters.create_frequency.CreateFrequency;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.component.DataComponentType.Builder;
import com.mojang.serialization.Codec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.UnaryOperator;

public class CFDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CreateFrequency.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Float>>> CONTROLLER_BINDS =
            register("controller_binds", builder -> builder
                    .persistent(Codec.FLOAT.listOf())
                    .networkSynchronized(ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list())));

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<Builder<T>> builderAction) {
        return DATA_COMPONENTS.register(name, () -> builderAction.apply(DataComponentType.builder()).build());
    }
}