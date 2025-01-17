package dev.kir.sync.block.entity;

import dev.kir.sync.util.BlockPosUtil;
import dev.kir.sync.api.shell.ShellStateContainer;
import dev.kir.sync.api.event.PlayerSyncEvents;
import dev.kir.sync.block.AbstractShellContainerBlock;
import dev.kir.sync.block.ShellStorageBlock;
import dev.kir.sync.client.gui.ShellSelectorGUI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShellStorageBlockEntity extends AbstractShellContainerBlockEntity {
    private static final int MAX_TICKS_WITHOUT_POWER = 20;

    private EntityState entityState;
    private int ticksWithoutPower;
    private final BooleanAnimator connectorAnimator;

    public ShellStorageBlockEntity(BlockPos pos, BlockState state) {
        super(SyncBlockEntities.SHELL_STORAGE, pos, state);
        this.entityState = EntityState.NONE;
        this.connectorAnimator = new BooleanAnimator(false);
    }

    public DyeColor getIndicatorColor() {
        if (this.world != null && ShellStorageBlock.isEnabled(this.getCachedState())) {
            return this.color == null ? DyeColor.LIME : this.color;
        }

        return DyeColor.RED;
    }

    @Environment(EnvType.CLIENT)
    public float getConnectorProgress(float tickDelta) {
        float progress = this.connectorAnimator.getProgress(tickDelta);
        float secondProgress = this.getSecondPart().map(x -> x instanceof ShellStorageBlockEntity shellStorage ? shellStorage.connectorAnimator.getProgress(tickDelta) : 0).orElse(0F);
        return Math.max(progress, secondProgress);
    }

    @Override
    public void onServerTick(World world, BlockPos pos, BlockState state) {
        super.onServerTick(world, pos, state);

        boolean isEnabled = ShellStorageBlock.isEnabled(state);
        boolean shouldBeOpen = this.shell == null && this.getSecondPart().map(x -> x.shell == null).orElse(true) && isEnabled;
        ShellStorageBlock.setOpen(state, world, pos, shouldBeOpen);

        if (this.shell != null && !isEnabled) {
            ++this.ticksWithoutPower;
            if (this.ticksWithoutPower >= MAX_TICKS_WITHOUT_POWER) {
                this.destroyShell((ServerWorld)world, pos);
            }
        } else {
            this.ticksWithoutPower = 0;
        }
    }

    @Override
    public void onClientTick(World world, BlockPos pos, BlockState state) {
        super.onClientTick(world, pos, state);
        this.connectorAnimator.setValue(this.shell != null);
        this.connectorAnimator.step();
        if (this.entityState == EntityState.LEAVING || this.entityState == EntityState.CHILLING) {
            this.entityState = BlockPosUtil.hasPlayerInside(pos, world) ? this.entityState : EntityState.NONE;
        }
    }

    @Environment(EnvType.CLIENT)
    public void onEntityCollisionClient(Entity entity, BlockState state) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(entity instanceof PlayerEntity player)) {
            return;
        }

        if (this.entityState == EntityState.NONE) {
            boolean isInside = BlockPosUtil.isEntityInside(entity, this.pos);
            PlayerSyncEvents.ShellSelectionFailureReason failureReason = !isInside && client.player == entity ? PlayerSyncEvents.ALLOW_SHELL_SELECTION.invoker().allowShellSelection(player, this) : null;
            this.entityState = isInside || failureReason != null ? EntityState.CHILLING : EntityState.ENTERING;
            if (failureReason != null) {
                player.sendMessage(failureReason.toText(), true);
            }
        } else if (this.entityState != EntityState.CHILLING && client.currentScreen == null) {
            BlockPosUtil.moveEntity(entity, this.pos, state.get(ShellStorageBlock.FACING), this.entityState == EntityState.ENTERING);
        }

        if (this.entityState == EntityState.ENTERING && client.player == entity && client.currentScreen == null && BlockPosUtil.isEntityInside(entity, this.pos)) {
            client.setScreen(new ShellSelectorGUI(() -> this.entityState = EntityState.LEAVING, () -> this.entityState = EntityState.CHILLING));
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();
        if (stack.getCount() < 1 || !(item instanceof DyeItem dye)) {
            return ActionResult.SUCCESS;
        }

        if (!world.isClient) {
            stack.decrement(1);
            this.color = dye.getColor();
        }
        return ActionResult.SUCCESS;
    }

    private enum EntityState {
        NONE,
        ENTERING,
        CHILLING,
        LEAVING
    }

    static {
        ShellStateContainer.LOOKUP.registerForBlockEntity((x, s) -> x.hasWorld() && AbstractShellContainerBlock.isBottom(x.getCachedState()) && (s == null || s.equals(x.getShellState())) ? x : null, SyncBlockEntities.SHELL_STORAGE);
    }
}