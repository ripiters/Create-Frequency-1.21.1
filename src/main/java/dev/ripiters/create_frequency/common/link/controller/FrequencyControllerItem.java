package dev.ripiters.create_frequency.common.link.controller;

import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import dev.ripiters.create_frequency.CreateFrequency;
import dev.ripiters.create_frequency.client.gui.FrequencyControllerMenu;
import dev.ripiters.create_frequency.common.CFBlocks;
import dev.ripiters.create_frequency.common.CFDataComponents;
import dev.ripiters.create_frequency.common.network.FrequencyNetworkHandler;
import dev.ripiters.create_frequency.config.FrequencyConfig;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("removal")
public class FrequencyControllerItem extends Item implements MenuProvider {

    public FrequencyControllerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null)
            return InteractionResult.PASS;
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState hitState = world.getBlockState(pos);

        if (player.mayBuild()) {
            if (player.isShiftKeyDown()) {
                if (CFBlocks.LECTERN_FREQUENCY_CONTROLLER.has(hitState)) {
                    if (!world.isClientSide)
                        CFBlocks.LECTERN_FREQUENCY_CONTROLLER.get().withBlockEntityDo(world, pos, be ->
                                be.swapControllers(stack, player, ctx.getHand(), hitState));
                    return InteractionResult.SUCCESS;
                }
            } else {
                if (CFBlocks.FREQUENCY_RECEIVER.has(hitState)) {
                    if (world.isClientSide)
                        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> this.toggleBindMode(ctx.getClickedPos()));
                    player.getCooldowns()
                            .addCooldown(this, 2);
                    return InteractionResult.SUCCESS;
                }

                if (hitState.is(Blocks.LECTERN) && !hitState.getValue(LecternBlock.HAS_BOOK)) {
                    if (!world.isClientSide) {
                        ItemStack lecternStack = player.isCreative() ? stack.copy() : stack.split(1);
                        CFBlocks.LECTERN_FREQUENCY_CONTROLLER.get().replaceLectern(hitState, world, pos, lecternStack);
                    }
                    return InteractionResult.SUCCESS;
                }

                if (CFBlocks.LECTERN_FREQUENCY_CONTROLLER.has(hitState))
                    return InteractionResult.PASS;
            }
        }

        return use(world, player, ctx.getHand()).getResult();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            if (!world.isClientSide && player instanceof ServerPlayer && player.mayBuild())
                player.openMenu(this, buf -> {
                    ItemStack.STREAM_CODEC.encode(buf, heldItem);
                });
            return InteractionResultHolder.success(heldItem);
        }

        if (!player.isShiftKeyDown()) {
            if (world.isClientSide)
                CatnipServices.PLATFORM.executeOnClientOnly(() -> this::toggleActive);
            player.getCooldowns()
                    .addCooldown(this, 2);
        }

        return InteractionResultHolder.pass(heldItem);
    }

    @OnlyIn(Dist.CLIENT)
    private void toggleBindMode(BlockPos pos) {
        FrequencyControllerClientHandler.toggleBindMode(pos);
    }

    @OnlyIn(Dist.CLIENT)
    private void toggleActive() {
        FrequencyControllerClientHandler.toggle();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof FrequencyControllerItem)) {
            heldItem = player.getOffhandItem();
        }
        return FrequencyControllerMenu.create(id, inv, heldItem);
    }

    @Override
    public Component getDisplayName() {
        return getDescription();
    }

    public static float getBindFrequency(ItemStack stack, int buttonIndex) {
        if (buttonIndex < 0 || buttonIndex >= 6) return 0f;

        List<Float> binds = stack.get(CFDataComponents.CONTROLLER_BINDS.get());

        if (binds == null || buttonIndex >= binds.size()) {
            return 0f;
        }
        return binds.get(buttonIndex);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !oldStack.getItem().equals(newStack.getItem());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        List<Float> binds = stack.getOrDefault(CFDataComponents.CONTROLLER_BINDS.get(), List.of());
        if (FrequencyConfig.CLIENT.enableControllerAdvancedTooltip.get()) {
            for (int i = 0; i < binds.size(); i++) {
                float freq = binds.get(i);
                if (freq <= 0) continue;

                String name = "";
                if (net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT) {
                    name = dev.ripiters.create_frequency.CreateFrequencyClient.getNetworkName(freq);
                }

                String displayName = (name == null || name.isEmpty()) ? "Network Name" : name;

                tooltip.add(Component.literal("Key " + (i + 1) + ": ")
                        .append(Component.literal(displayName).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" (" + freq + "Hz)").withStyle(ChatFormatting.DARK_GRAY))
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new FrequencyControllerItemRenderer()));
    }
}