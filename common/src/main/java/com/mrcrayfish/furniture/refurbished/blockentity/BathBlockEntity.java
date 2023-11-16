package com.mrcrayfish.furniture.refurbished.blockentity;

import com.mrcrayfish.furniture.refurbished.Config;
import com.mrcrayfish.furniture.refurbished.block.BathBlock;
import com.mrcrayfish.furniture.refurbished.blockentity.fluid.FluidContainer;
import com.mrcrayfish.furniture.refurbished.blockentity.fluid.IFluidContainerBlock;
import com.mrcrayfish.furniture.refurbished.core.ModBlockEntities;
import com.mrcrayfish.furniture.refurbished.platform.Services;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class BathBlockEntity extends BlockEntity implements IFluidContainerBlock
{
    protected final FluidContainer tank;

    public BathBlockEntity(BlockPos pos, BlockState state)
    {
        this(ModBlockEntities.BATH.get(), pos, state);
    }

    public BathBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.tank = this.createFluidContainer(state);
    }

    public boolean isHead()
    {
        return this.tank != null;
    }

    @Override
    public FluidContainer getFluidContainer()
    {
        return this.tank != null ? this.tank : this.getFluidContainerFromHead();
    }

    @Nullable
    private FluidContainer getFluidContainerFromHead()
    {
        BlockState state = this.getBlockState();
        if(state.hasProperty(BathBlock.DIRECTION))
        {
            Direction direction = state.getValue(BathBlock.DIRECTION);
            Level level = Objects.requireNonNull(this.level);
            if(level.getBlockEntity(this.worldPosition.relative(direction)) instanceof BathBlockEntity bath)
            {
                return bath.getFluidContainer();
            }
        }
        return null;
    }

    @Nullable
    private FluidContainer createFluidContainer(BlockState state)
    {
        if(state.hasProperty(BathBlock.TYPE) && state.getValue(BathBlock.TYPE) == BathBlock.Type.HEAD)
        {
            return FluidContainer.create(Config.SERVER.bath.fluidCapacity.get(), container -> {
                this.setChanged();
                container.sync(this);
            });
        }
        return null;
    }

    public InteractionResult interact(Player player, InteractionHand hand, BlockHitResult result)
    {
        FluidContainer tank = this.getFluidContainer();
        if(tank == null)
        {
            return InteractionResult.PASS;
        }

        if(Config.SERVER.bath.dispenseWater.get() && player.getItemInHand(hand).isEmpty())
        {
            // Fills the sink with water
            if(tank.isEmpty() || tank.getStoredFluid().isSame(Fluids.WATER))
            {
                long filled = tank.push(Fluids.WATER, FluidContainer.BUCKET_CAPACITY, false);
                if(filled > 0)
                {
                    SoundEvent event = Services.FLUID.getBucketEmptySound(Fluids.WATER);
                    if(event != null)
                    {
                        Objects.requireNonNull(this.level).playSound(null, this.worldPosition, event, SoundSource.BLOCKS);
                        return InteractionResult.SUCCESS;
                    }
                }
            }

            // If lava is in the basin, filling it with water will consume the lava and turn it into obsidian
            if(tank.getStoredAmount() >= FluidContainer.BUCKET_CAPACITY && tank.getStoredFluid().isSame(Fluids.LAVA))
            {
                Pair<Fluid, Long> drained = tank.pull(FluidContainer.BUCKET_CAPACITY, true);
                if(drained.right() == FluidContainer.BUCKET_CAPACITY)
                {
                    tank.pull(FluidContainer.BUCKET_CAPACITY, false);
                    Vec3 pos = Vec3.atBottomCenterOf(this.worldPosition).add(0, 1, 0);
                    Level level = Objects.requireNonNull(this.level);
                    ItemEntity entity = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Blocks.OBSIDIAN));
                    entity.setDefaultPickUpDelay();
                    level.addFreshEntity(entity);
                    level.playSound(null, this.worldPosition, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS);
                    level.levelEvent(LevelEvent.LAVA_FIZZ, this.worldPosition, 0);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return Services.FLUID.performInteractionWithBlock(player, hand, this.getLevel(), this.getBlockPos(), result.getDirection());
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        if(this.tank != null)
        {
            this.tank.load(tag.getCompound("FluidTank"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        if(this.tank != null)
        {
            CompoundTag tankTag = new CompoundTag();
            this.tank.save(tankTag);
            tag.put("FluidTank", tankTag);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return this.saveWithoutMetadata();
    }
}
