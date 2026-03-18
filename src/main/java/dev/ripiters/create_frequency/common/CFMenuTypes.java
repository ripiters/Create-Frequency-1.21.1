package dev.ripiters.create_frequency.common;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.MenuEntry;

import dev.ripiters.create_frequency.CreateFrequency;
import dev.ripiters.create_frequency.client.gui.FrequencyControllerMenu;
import dev.ripiters.create_frequency.client.gui.FrequencyControllerScreen;

public class CFMenuTypes {
    private static final CreateRegistrate REGISTRATE = CreateFrequency.getRegistrate();

    public static final MenuEntry<FrequencyControllerMenu> FREQUENCY_CONTROLLER =
            REGISTRATE.menu("frequency_controller", FrequencyControllerMenu::new, () -> FrequencyControllerScreen::new)
                    .register();

    public static void register() {}

}