package dev.ripiters.create_frequency.client.gui;

import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import dev.ripiters.create_frequency.common.CFMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class FrequencyControllerMenu extends GhostItemMenu<ItemStack> {

    public FrequencyControllerMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public FrequencyControllerMenu(MenuType<?> type, int id, Inventory inv, ItemStack filterItem) {
        super(type, id, inv, filterItem);
    }

    public static FrequencyControllerMenu create(int id, Inventory inv, ItemStack filterItem) {
        return new FrequencyControllerMenu(CFMenuTypes.FREQUENCY_CONTROLLER.get(), id, inv, filterItem);
    }

    @Override
    protected ItemStack createOnClient(RegistryFriendlyByteBuf extraData) {
        return ItemStack.STREAM_CODEC.decode(extraData);
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return new ItemStackHandler(0);
    }

    @Override
    protected void addSlots() {
        addPlayerSlots(36, 130);
    }

    @Override protected void saveData(ItemStack contentHolder) {}
    @Override protected boolean allowRepeats() { return true; }
}