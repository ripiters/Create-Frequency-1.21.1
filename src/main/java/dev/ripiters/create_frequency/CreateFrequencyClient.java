package dev.ripiters.create_frequency;

import dev.ripiters.create_frequency.common.link.controller.FrequencyControllerClientHandler;
import dev.ripiters.create_frequency.config.FrequencyConfig;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.minecraft.client.player.Input;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = CreateFrequency.MODID, value = Dist.CLIENT)
public class CreateFrequencyClient {

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        ModContainer container = ModList.get()
                .getModContainerById(CreateFrequency.MODID)
                .orElseThrow(() -> new IllegalStateException("Create Frequency mod container missing on LoadComplete"));

        container.registerExtensionPoint(IConfigScreenFactory.class, (client, parent) -> {
            return new BaseConfigScreen(parent, CreateFrequency.MODID)
                    .withSpecs(null, FrequencyConfig.CLIENT.specification, null);
        });

        CreateFrequency.LOGGER.info("Create Frequency: Config Screen Factory registered successfully.");
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        CreateFrequency.LOGGER.info("Create Frequency Client Setup complete.");
        if (FrequencyConfig.CLIENT.enableExtendedLogging.get()) {
            CreateFrequency.LOGGER.info("Extended logging enabled.");
        }
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (FrequencyControllerClientHandler.MODE != FrequencyControllerClientHandler.Mode.IDLE) {
            Input input = event.getInput();
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
            input.forwardImpulse = 0;
            input.leftImpulse = 0;
            input.jumping = false;
            input.shiftKeyDown = false;
        }
    }

    @SubscribeEvent
    public static void onClientInteract(InputEvent.InteractionKeyMappingTriggered event) {
        if (FrequencyControllerClientHandler.MODE == FrequencyControllerClientHandler.Mode.ACTIVE && FrequencyControllerClientHandler.inLectern()) {
            if (event.isUseItem()) {
                FrequencyControllerClientHandler.deactivateInLectern();
            }
        }
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, CreateFrequency.resourceLocation("frequency_controller"), FrequencyControllerClientHandler.OVERLAY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        FrequencyControllerClientHandler.tick();
    }

    public static String getNetworkName(float freq) {
        return dev.ripiters.create_frequency.common.network.FrequencyNetworkHandler.getNetworkName(
                net.minecraft.client.Minecraft.getInstance().level, freq
        );
    }
}