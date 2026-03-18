package dev.ripiters.create_frequency;

import dev.ripiters.create_frequency.common.link.controller.FrequencyControllerServerHandler;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber
public class CreateFrequencyServer {

    @SubscribeEvent
    public static void onServerWorldTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
        Level world = event.getLevel();
        if (world.isClientSide())
            return;
        FrequencyControllerServerHandler.tick(world);
    }

    @SubscribeEvent
    public static void onLoadWorld(LevelEvent.Load event) {
        LevelAccessor world = event.getLevel();
        CreateFrequency.FREQUENCY_NETWORK_HANDLER.onLoadWorld(world);
    }

    @SubscribeEvent
    public static void onUnloadWorld(LevelEvent.Unload event) {
        LevelAccessor world = event.getLevel();
        CreateFrequency.FREQUENCY_NETWORK_HANDLER.onUnloadWorld(world);
    }
}
