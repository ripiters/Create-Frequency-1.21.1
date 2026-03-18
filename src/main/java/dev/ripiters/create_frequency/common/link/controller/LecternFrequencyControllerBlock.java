package dev.ripiters.create_frequency.common.link.controller;

import java.util.ArrayList;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;

import dev.ripiters.create_frequency.common.CFBlockEntityTypes;
import dev.ripiters.create_frequency.common.CFItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class LecternFrequencyControllerBlock extends LecternBlock
        implements IBE<LecternFrequencyControllerBlockEntity>, SpecialBlockItemRequirement {

    public LecternFrequencyControllerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HAS_BOOK, true));
    }

    @Override
    public Class<LecternFrequencyControllerBlockEntity> getBlockEntityClass() {
        return LecternFrequencyControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends LecternFrequencyControllerBlockEntity> getBlockEntityType() {
        return CFBlockEntityTypes.LECTERN_FREQUENCY_CONTROLLER.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos p_153573_, BlockState p_153574_) {
        return IBE.super.newBlockEntity(p_153573_, p_153574_);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.isShiftKeyDown() && LecternFrequencyControllerBlockEntity.playerInRange(player, level, pos)) {
            if (!level.isClientSide)
                withBlockEntityDo(level, pos, be -> be.tryStartUsing(player));
            return ItemInteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide)
                replaceWithLectern(state, level, pos);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!world.isClientSide)
                withBlockEntityDo(world, pos, be -> be.dropController(state));

            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return 15;
    }

    public void replaceLectern(BlockState lecternState, Level world, BlockPos pos, ItemStack controller) {
        world.setBlockAndUpdate(pos, defaultBlockState().setValue(FACING, lecternState.getValue(FACING))
                .setValue(POWERED, lecternState.getValue(POWERED)));
        withBlockEntityDo(world, pos, be -> be.setController(controller));
    }

    public void replaceWithLectern(BlockState state, Level world, BlockPos pos) {
        AllSoundEvents.CONTROLLER_TAKE.playOnServer(world, pos);
        world.setBlockAndUpdate(pos, Blocks.LECTERN.defaultBlockState()
                .setValue(FACING, state.getValue(FACING))
                .setValue(POWERED, state.getValue(POWERED)));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return Blocks.LECTERN.getCloneItemStack(state, target, level, pos, player);
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        ArrayList<ItemStack> requiredItems = new ArrayList<>();
        requiredItems.add(new ItemStack(Blocks.LECTERN));
        requiredItems.add(new ItemStack(CFItems.FREQUENCY_CONTROLLER.get()));
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, requiredItems);
    }
}
