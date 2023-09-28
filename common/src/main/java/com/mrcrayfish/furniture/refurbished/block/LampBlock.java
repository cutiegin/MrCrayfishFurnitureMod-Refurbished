package com.mrcrayfish.furniture.refurbished.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.furniture.refurbished.blockentity.LightingBlockEntity;
import com.mrcrayfish.furniture.refurbished.core.ModBlockEntities;
import com.mrcrayfish.furniture.refurbished.data.tag.BlockTagSupplier;
import com.mrcrayfish.furniture.refurbished.electricity.IElectricityNode;
import com.mrcrayfish.furniture.refurbished.util.VoxelShapeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class LampBlock extends FurnitureBlock implements EntityBlock, BlockTagSupplier
{
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private final DyeColor color;

    public LampBlock(DyeColor color, Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(POWERED, false));
        this.color = color;
    }

    public DyeColor getDyeColor()
    {
        return this.color;
    }

    @Override
    protected Map<BlockState, VoxelShape> generateShapes(ImmutableList<BlockState> states)
    {
        VoxelShape baseShape = Block.box(5, 0, 5, 11, 6, 11);
        VoxelShape shadeShape = Block.box(3, 5, 3, 13, 14, 13);
        VoxelShape lampShape = VoxelShapeHelper.combine(List.of(baseShape, shadeShape));
        return ImmutableMap.copyOf(states.stream().collect(Collectors.toMap(state -> state, state -> lampShape)));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if(!state.is(newState.getBlock()))
        {
            if(level.getBlockEntity(pos) instanceof IElectricityNode node)
            {
                node.onDestroyed();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new LightingBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return createTicker(level, type, ModBlockEntities.LIGHTING.get());
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockEntityType<T> type, BlockEntityType<? extends LightingBlockEntity> ceilingLight)
    {
        if(!level.isClientSide())
        {
            return createTickerHelper(type, ceilingLight, LightingBlockEntity::serverTick);
        }
        return null;
    }

    @Override
    public List<TagKey<Block>> getTags()
    {
        return List.of(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    public static int light(BlockState state)
    {
        return state.getValue(POWERED) ? 8 : 0;
    }
}