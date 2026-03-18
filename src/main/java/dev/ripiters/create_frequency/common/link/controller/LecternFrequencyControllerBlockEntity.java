package dev.ripiters.create_frequency.common.link.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import dev.ripiters.create_frequency.common.CFDataComponents;
import dev.ripiters.create_frequency.common.CFItems;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class LecternFrequencyControllerBlockEntity extends SmartBlockEntity {

    private List<Float> frequencies = new ArrayList<>();
    private UUID user;
    private UUID prevUser;
    private boolean deactivatedThisTick;

    public LecternFrequencyControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        ListTag list = new ListTag();
        for (float f : frequencies) {
            list.add(FloatTag.valueOf(f));
        }
        compound.put("Frequencies", list);
        if (user != null)
            compound.putUUID("User", user);
    }

    @Override
    public void writeSafe(CompoundTag compound, HolderLookup.Provider registries) {
        super.writeSafe(compound, registries);
        ListTag list = new ListTag();
        for (float f : frequencies) {
            list.add(FloatTag.valueOf(f));
        }
        compound.put("Frequencies", list);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        frequencies.clear();
        if (compound.contains("Frequencies", Tag.TAG_LIST)) {
            ListTag list = compound.getList("Frequencies", Tag.TAG_FLOAT);
            for (int i = 0; i < list.size(); i++) {
                frequencies.add(list.getFloat(i));
            }
        }
        user = compound.hasUUID("User") ? compound.getUUID("User") : null;
    }

    public ItemStack getController() {
        return createFrequencyController();
    }

    public boolean hasUser() {
        return user != null;
    }

    public boolean isUsedBy(Player player) {
        return hasUser() && user.equals(player.getUUID());
    }

    public void tryStartUsing(Player player) {
        if (!deactivatedThisTick && !hasUser() && !playerIsUsingLectern(player) && playerInRange(player, level, worldPosition))
            startUsing(player);
    }

    public void tryStopUsing(Player player) {
        if (isUsedBy(player))
            stopUsing(player);
    }

    private void startUsing(Player player) {
        user = player.getUUID();
        player.getPersistentData().putBoolean("IsUsingLecternFrequencyController", true);
        sendData();
    }

    private void stopUsing(Player player) {
        user = null;
        if (player != null)
            player.getPersistentData().remove("IsUsingLecternFrequencyController");
        deactivatedThisTick = true;
        sendData();
    }

    public static boolean playerIsUsingLectern(Player player) {
        return player.getPersistentData().contains("IsUsingLecternFrequencyController");
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide) {
            CatnipServices.PLATFORM.executeOnClientOnly(() -> this::tryToggleActive);
            prevUser = user;
        }

        if (!level.isClientSide) {
            deactivatedThisTick = false;
            if (user == null) return;

            Entity entity = ((ServerLevel) level).getEntity(user);
            if (!(entity instanceof Player player)) {
                stopUsing(null);
                return;
            }

            if (!playerInRange(player, level, worldPosition) || !playerIsUsingLectern(player))
                stopUsing(player);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tryToggleActive() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        UUID playerUUID = mc.player.getUUID();

        if (user == null && playerUUID.equals(prevUser)) {
            FrequencyControllerClientHandler.deactivateInLectern();
        } else if (prevUser == null && playerUUID.equals(user)) {
            FrequencyControllerClientHandler.activateInLectern(worldPosition);
        }
    }

    public void setController(ItemStack newController) {
        if (newController != null && !newController.isEmpty()) {
            this.frequencies = new ArrayList<>(newController.getOrDefault(CFDataComponents.CONTROLLER_BINDS.get(), List.of()));
            AllSoundEvents.CONTROLLER_PUT.playOnServer(level, worldPosition);
            sendData();
        }
    }

    public void swapControllers(ItemStack stack, Player player, InteractionHand hand, BlockState state) {
        ItemStack newController = stack.copy();
        stack.setCount(0);
        if (player.getItemInHand(hand).isEmpty()) {
            player.setItemInHand(hand, createFrequencyController());
        } else {
            dropController(state);
        }
        setController(newController);
    }

    public void dropController(BlockState state) {
        if (level.isClientSide) return;
        if (user != null) {
            Entity entity = ((ServerLevel) level).getEntity(user);
            if (entity instanceof Player player) stopUsing(player);
        }

        Direction dir = state.getValue(LecternFrequencyControllerBlock.FACING);
        double x = worldPosition.getX() + 0.5 + 0.25 * dir.getStepX();
        double y = worldPosition.getY() + 1.1;
        double z = worldPosition.getZ() + 0.5 + 0.25 * dir.getStepZ();

        ItemEntity itementity = new ItemEntity(level, x, y, z, createFrequencyController());
        itementity.setDefaultPickUpDelay();
        level.addFreshEntity(itementity);

        frequencies.clear();
        sendData();
    }

    public static boolean playerInRange(Player player, Level world, BlockPos pos) {
        double reach = 0.4 * player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        return player.distanceToSqr(Vec3.atCenterOf(pos)) < reach * reach;
    }

    private ItemStack createFrequencyController() {
        ItemStack stack = CFItems.FREQUENCY_CONTROLLER.asStack();
        stack.set(CFDataComponents.CONTROLLER_BINDS.get(), new ArrayList<>(frequencies));
        return stack;
    }
}