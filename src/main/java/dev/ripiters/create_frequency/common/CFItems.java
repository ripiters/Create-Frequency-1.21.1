package dev.ripiters.create_frequency.common;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.ripiters.create_frequency.CreateFrequency;
import dev.ripiters.create_frequency.common.link.controller.FrequencyControllerItem;

public class CFItems {

    private static final CreateRegistrate REGISTRATE = CreateFrequency.getRegistrate();

    static {
        REGISTRATE.setCreativeTab(CFCreativeTabs.MAIN);
    }

    public static final ItemEntry<FrequencyControllerItem> FREQUENCY_CONTROLLER =
            REGISTRATE.item("frequency_controller", FrequencyControllerItem::new)
                    .properties(p -> p.stacksTo(1))
                    .model(AssetLookup.itemModelWithPartials())
                    .register();

    public static void register() {}
}