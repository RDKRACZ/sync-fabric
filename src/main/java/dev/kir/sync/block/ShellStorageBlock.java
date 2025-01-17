package dev.kir.sync.block;

import dev.kir.sync.block.entity.ShellStorageBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

@SuppressWarnings("deprecation")
public class ShellStorageBlock extends AbstractShellContainerBlock {
    public static final BooleanProperty ENABLED = Properties.ENABLED;

    public ShellStorageBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(OPEN, false).with(ENABLED, false));
    }

    public static boolean isEnabled(BlockState state) {
        return state.get(ENABLED);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ShellStorageBlockEntity(pos, state);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient) {
            boolean enabled = state.get(ENABLED);
            boolean shouldBeEnabled = shouldBeEnabled(state, world, pos);
            if (enabled != shouldBeEnabled) {
                BlockPos secondPartPos = pos.offset(getDirectionTowardsAnotherPart(state));
                if (enabled) {
                    world.getBlockTickScheduler().schedule(pos, this, 4);
                    world.getBlockTickScheduler().schedule(secondPartPos, this, 4);
                } else {
                    world.setBlockState(pos, state.with(ENABLED, true), 2);
                    BlockState secondPartState = world.getBlockState(secondPartPos);
                    if (secondPartState.isOf(this)) {
                        world.setBlockState(secondPartPos, secondPartState.with(ENABLED, true), 2);
                    }
                }
            }
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(ENABLED) && !shouldBeEnabled(state, world, pos)) {
            world.setBlockState(pos, state.with(ENABLED, false), 2);
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        if (world.isClient && entity instanceof PlayerEntity && isBottom(state)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ShellStorageBlockEntity) {
                ((ShellStorageBlockEntity)blockEntity).onEntityCollisionClient(entity, state);
            }
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ENABLED);
    }

    private static boolean shouldBeEnabled(BlockState state, World world, BlockPos pos) {
        return world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.offset(getDirectionTowardsAnotherPart(state)));
    }
}