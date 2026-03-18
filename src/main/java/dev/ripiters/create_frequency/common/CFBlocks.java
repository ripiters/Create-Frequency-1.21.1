package dev.ripiters.create_frequency.common;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.ripiters.create_frequency.CreateFrequency;
import dev.ripiters.create_frequency.common.link.FrequencyLinkGenerator;
import dev.ripiters.create_frequency.common.link.FrequencySignalReceiver;
import dev.ripiters.create_frequency.common.link.FrequencySignalTransmitter;
import dev.ripiters.create_frequency.common.link.controller.LecternFrequencyControllerBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOnly;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

@SuppressWarnings("removal")
public class CFBlocks {
    private static final CreateRegistrate REGISTRATE = CreateFrequency.getRegistrate();

    static {
        REGISTRATE.setCreativeTab(CFCreativeTabs.MAIN);
    }

    public static final BlockEntry<FrequencySignalTransmitter> FREQUENCY_TRANSMITTER =
            REGISTRATE.block("frequency_transmitter", FrequencySignalTransmitter::new)
                    .initialProperties(SharedProperties::wooden)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN)
                            .forceSolidOn())
                    .transform(axeOrPickaxe())
                    .tag(AllTags.AllBlockTags.BRITTLE.tag, AllTags.AllBlockTags.SAFE_NBT.tag)
                    .blockstate(new FrequencyLinkGenerator()::generate)
                    .addLayer(() -> RenderType::cutoutMipped)
                    .item()
                    .transform(customItemModel("_", "transmitter"))
                    .register();

    public static final BlockEntry<FrequencySignalReceiver> FREQUENCY_RECEIVER =
            REGISTRATE.block("frequency_receiver", FrequencySignalReceiver::new)
                    .initialProperties(SharedProperties::wooden)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN)
                            .forceSolidOn())
                    .transform(axeOrPickaxe())
                    .tag(AllTags.AllBlockTags.BRITTLE.tag, AllTags.AllBlockTags.SAFE_NBT.tag)
                    .blockstate(new FrequencyLinkGenerator()::generate)
                    .addLayer(() -> RenderType::cutoutMipped)
                    .item()
                    .transform(customItemModel("_", "receiver"))
                    .register();

    public static final BlockEntry<LecternFrequencyControllerBlock> LECTERN_FREQUENCY_CONTROLLER =
            REGISTRATE.block("lectern_frequency_controller", LecternFrequencyControllerBlock::new)
                    .initialProperties(() -> Blocks.LECTERN)
                    .transform(axeOnly())
                    .blockstate((c, p) -> p.horizontalBlock(c.get(), p.models()
                            .getExistingFile(p.mcLoc("block/lectern"))))
                    .loot((lt, block) -> lt.dropOther(block, Blocks.LECTERN))
                    .register();

    // Load this class

    public static void register() {
    }
}