package dev.ripiters.create_frequency.common.link;

import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import dev.ripiters.create_frequency.client.gui.FrequencyConfigScreen;
import dev.ripiters.create_frequency.common.CFBlockEntityTypes;
import dev.ripiters.create_frequency.common.CFShapes;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class FrequencySignalTransmitter extends WrenchableDirectionalBlock implements IBE<FrequencyTransmitterBlockEntity> {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public FrequencySignalTransmitter(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
        boolean isMoving) {
        if (level.isClientSide)
            return;

        Direction blockFacing = state.getValue(FACING);
        if (fromPos.equals(pos.relative(blockFacing.getOpposite()))) {
            if (!canSurvive(state, level, pos)) {
                level.destroyBlock(pos, true);
                return;
            }
        }

        if (!level.getBlockTicks()
                .willTickThisTick(pos, this))
            level.scheduleTick(pos, this, 1);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource r) {
        updateTransmittedSignal(state, level, pos);

        Direction attachedFace = state.getValue(FrequencySignalTransmitter.FACING)
                .getOpposite();
        BlockPos attachedPos = pos.relative(attachedFace);
        level.blockUpdated(pos, level.getBlockState(pos)
                .getBlock());
        level.blockUpdated(attachedPos, level.getBlockState(attachedPos)
                .getBlock());
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (state.getBlock() == oldState.getBlock() || isMoving)
            return;
        updateTransmittedSignal(state, worldIn, pos);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        IBE.onRemove(pState, pLevel, pPos, pNewState);
    }

    public void updateTransmittedSignal(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide)
            return;

        int power = getPower(level, state, pos);

        int powerFromPanels = getBlockEntityOptional(level, pos).map(be -> {
            if (be.panelSupport == null)
                return 0;
            Boolean tri = be.panelSupport.shouldBePoweredTristate();
            if (tri == null)
                return -1;
            return tri ? 15 : 0;
        }).orElse(0);

        if (powerFromPanels == -1)
            return;

        power = Math.max(power, powerFromPanels);

        boolean isPowered = power > 0;
        boolean wasPowered = state.getValue(POWERED);

        if (wasPowered != isPowered) {
            level.setBlock(pos, state.setValue(POWERED, isPowered), Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
        }

        int finalPower = power;
        withBlockEntityDo(level, pos, be -> {
            be.transmit(finalPower);
            if (be.panelSupport != null) {
                be.panelSupport.notifyPanels();
            }
        });
    }

    private static int getPower(Level level, BlockState state, BlockPos pos) {
        int power = 0;
        for (Direction direction : Iterate.directions)
            power = Math.max(level.getSignal(pos.relative(direction), direction), power);
        for (Direction direction : Iterate.directions) {
            if (state.getValue(FACING).getOpposite() != direction)
                power = Math.max(level.getSignal(pos.relative(direction), Direction.UP), power);
        }
        return power;
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        if (side != blockState.getValue(FACING))
            return 0;
        return getSignal(blockState, blockAccess, pos, side);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;

        if (!level.isClientSide) {
            withBlockEntityDo(level, pos, FrequencyBlockEntity::refreshNetworkData);
        }

        if (level.isClientSide) {
            distributeGui(pos);
        }
        return InteractionResult.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    private void distributeGui(BlockPos pos) {
        ClientLevel level = net.minecraft.client.Minecraft.getInstance().level;
        if (level != null && level.getBlockEntity(pos) instanceof FrequencyBlockEntity be) {
            ScreenOpener.open(new FrequencyConfigScreen(pos, be));
        }
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction _targetedFace) {
        return originalState;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        return side != null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockPos neighbourPos = pos.relative(state.getValue(FACING)
                .getOpposite());
        BlockState neighbour = worldIn.getBlockState(neighbourPos);
        return !neighbour.canBeReplaced();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        state = state.setValue(FACING, context.getClickedFace());
        return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return CFShapes.FREQUENCY_BRIDGE.get(state.getValue(FACING));
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public Class<FrequencyTransmitterBlockEntity> getBlockEntityClass() { return FrequencyTransmitterBlockEntity.class; }

    @Override
    public BlockEntityType<? extends FrequencyTransmitterBlockEntity> getBlockEntityType() {
        return CFBlockEntityTypes.LINKED_TRANSMITTER.get();
    }
}