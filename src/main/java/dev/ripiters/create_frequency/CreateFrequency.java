package dev.ripiters.create_frequency;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import dev.ripiters.create_frequency.common.*;
import dev.ripiters.create_frequency.common.network.CFPackets;
import dev.ripiters.create_frequency.common.network.FrequencyNetworkHandler;
import dev.ripiters.create_frequency.config.FrequencyConfig;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(CreateFrequency.MODID)
public class CreateFrequency {
    public static final String MODID = "create_frequency";
    public static final String NAME = "Create Frequency";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final FrequencyNetworkHandler FREQUENCY_NETWORK_HANDLER = new FrequencyNetworkHandler();

    private static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );

    public CreateFrequency(IEventBus modEventBus, ModContainer modContainer) {
        REGISTRATE.registerEventListeners(modEventBus);
        ModLoadingContext modLoadingContext = ModLoadingContext.get();

        CFDataComponents.DATA_COMPONENTS.register(modEventBus);

        CFCreativeTabs.register(modEventBus);
        CFBlocks.register();
        CFItems.register();
        CFBlockEntityTypes.register();
        CFMenuTypes.register();

        FrequencyConfig.register(modLoadingContext, modContainer);

        modEventBus.addListener(this::commonSetup);

        CreateFrequency.LOGGER.debug("Create Frequency Registration complete.");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(CFPackets::register);
        CreateFrequency.LOGGER.debug("Create Frequency Common Setup complete.");
    }

    public static CreateRegistrate getRegistrate() {
        return REGISTRATE;
    }

    public static ResourceLocation resourceLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}