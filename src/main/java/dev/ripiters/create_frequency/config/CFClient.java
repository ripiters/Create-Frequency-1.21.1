package dev.ripiters.create_frequency.config;

import net.createmod.catnip.config.ConfigBase;

public class CFClient extends ConfigBase {

    // Sekcja Debug
    public final ConfigGroup debug = group(0, "debug", "Debug Options");

    public final ConfigBool enableExtendedLogging = b(false, "enableExtendedLogging",
            "Enables extended error logging in the console (Extended Debug Logging).",
            "Requires world restart.");

    public final ConfigBool enableControllerAdvancedTooltip = b(false, "enableControllerAdvancedTooltip",
            "Enables additional information in Frequency Controller tooltips.");

    @Override
    public String getName() {
        return "client";
    }
}