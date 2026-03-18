package dev.ripiters.create_frequency.common;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.ripiters.create_frequency.CreateFrequency;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class CFCreativeTabs {
    private static final CreateRegistrate REGISTRATE = CreateFrequency.getRegistrate();

    private static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateFrequency.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = REGISTER.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + CreateFrequency.MODID + ".main"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> CFBlocks.FREQUENCY_TRANSMITTER.asStack())
            .displayItems(new RegistrateDisplayItemsGenerator())
            .build());

    @Internal
    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }

    private static class RegistrateDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {
        private static final Predicate<Item> IS_ITEM_3D_PREDICATE;

        static {
            MutableObject<Predicate<Item>> isItem3d = new MutableObject<>(item -> false);
            if (CatnipServices.PLATFORM.getEnv().isClient())
                isItem3d.setValue(item -> {
                    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                    BakedModel model = itemRenderer.getModel(new ItemStack(item), null, null, 0);
                    return model.isGui3d();
                });
            IS_ITEM_3D_PREDICATE = isItem3d.getValue();
        }

        @Override
        public void accept(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
            List<Item> items = new LinkedList<>();

            // Kolejność: Przedmioty 2D -> Bloki -> Przedmioty 3D
            items.addAll(collectItems(IS_ITEM_3D_PREDICATE.negate()));
            items.addAll(collectBlocks());
            items.addAll(collectItems(IS_ITEM_3D_PREDICATE));

            for (Item item : items) {
                output.accept(item);
            }
        }

        private List<Item> collectBlocks() {
            List<Item> items = new ReferenceArrayList<>();
            // Używamy REGISTRATE Twojego moda zamiast Create.registrate()
            for (RegistryEntry<Block, Block> entry : REGISTRATE.getAll(Registries.BLOCK)) {
                if (!CreateRegistrate.isInCreativeTab(entry, MAIN))
                    continue;
                Item item = entry.get().asItem();
                if (item != Items.AIR)
                    items.add(item);
            }
            return new ReferenceArrayList<>(new ReferenceLinkedOpenHashSet<>(items));
        }

        private List<Item> collectItems(Predicate<Item> filter) {
            List<Item> items = new ReferenceArrayList<>();
            for (RegistryEntry<Item, Item> entry : REGISTRATE.getAll(Registries.ITEM)) {
                if (!CreateRegistrate.isInCreativeTab(entry, MAIN))
                    continue;
                Item item = entry.get();
                if (item instanceof BlockItem)
                    continue;
                if (filter.test(item))
                    items.add(item);
            }
            return items;
        }
    }
}