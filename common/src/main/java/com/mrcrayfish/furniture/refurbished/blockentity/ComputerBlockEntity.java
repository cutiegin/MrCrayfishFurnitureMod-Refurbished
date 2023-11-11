package com.mrcrayfish.furniture.refurbished.blockentity;

import com.mrcrayfish.furniture.refurbished.computer.Computer;
import com.mrcrayfish.furniture.refurbished.computer.Program;
import com.mrcrayfish.furniture.refurbished.core.ModBlockEntities;
import com.mrcrayfish.furniture.refurbished.inventory.BuildableContainerData;
import com.mrcrayfish.furniture.refurbished.inventory.ComputerMenu;
import com.mrcrayfish.furniture.refurbished.network.Network;
import com.mrcrayfish.furniture.refurbished.network.message.MessageComputerState;
import com.mrcrayfish.furniture.refurbished.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;

// TODO create pong game on computer. you can play against players in the server
// TODO create app to control lightswitches connected in the power network
// TODO create amazon like app to buy items and send to mailboxes

/**
 * Author: MrCrayfish
 */
public class ComputerBlockEntity extends ElectricityModuleBlockEntity implements MenuProvider, IComputer
{
    public static final int DATA_SYSTEM = 0;

    protected int systemData;
    protected Program currentProgram;
    protected @Nullable Player currentUser;

    protected final ContainerData data = new BuildableContainerData(builder -> {
        builder.add(DATA_SYSTEM, () -> systemData, value -> {});
    });

    public ComputerBlockEntity(BlockPos pos, BlockState state)
    {
        this(ModBlockEntities.COMPUTER.get(), pos, state);
    }

    public ComputerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void setUser(@Nullable Player player)
    {
        this.currentUser = player;
    }

    @Override
    @Nullable
    public Player getUser()
    {
        if(this.currentUser != null && !this.currentUser.isAlive())
        {
            this.currentUser = null;
        }
        return this.currentUser;
    }

    @Override
    @Nullable
    public Program getProgram()
    {
        return this.currentProgram;
    }

    @Nullable
    @Override
    public ComputerMenu getMenu()
    {
        Player player = this.getUser();
        if(player != null && player.containerMenu instanceof ComputerMenu menu && menu.getComputer() == this)
        {
            return menu;
        }
        return null;
    }

    @Override
    public boolean isPowered()
    {
        BlockState state = this.getBlockState();
        return state.hasProperty(BlockStateProperties.POWERED) && state.getValue(BlockStateProperties.POWERED);
    }

    @Override
    public void setPowered(boolean powered)
    {
        BlockState state = this.getBlockState();
        if(state.hasProperty(BlockStateProperties.POWERED))
        {
            this.level.setBlock(this.worldPosition, state.setValue(BlockStateProperties.POWERED, powered), Block.UPDATE_ALL);
        }
    }

    @Override
    public Component getDisplayName()
    {
        return Utils.translation("container", "computer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player)
    {
        this.launchProgram(Utils.resource("pong_game"));
        return new ComputerMenu(windowId, playerInventory, this.data, this);
    }

    public boolean isBeingUsed()
    {
        return this.currentUser != null && this.currentUser.isAlive() && this.currentUser.containerMenu instanceof ComputerMenu menu && this.currentUser.equals(menu.getComputer().getUser());
    }

    public void launchProgram(@Nullable ResourceLocation id)
    {
        // If the id is null, it means to close the program
        if(id == null)
        {
            if(this.currentProgram != null)
            {
                this.currentProgram.onClose(true);
            }
            this.currentProgram = null;
            this.syncStateToCurrentUser();
            return;
        }

        // Don't open the program again if it's the current program
        if(this.currentProgram != null && this.currentProgram.getId().equals(id))
            return;

        // Create the program and sync the state if on the server
        Computer.get().createProgramInstance(id, this).ifPresent(program -> {
            this.currentProgram = program;
            this.syncStateToCurrentUser();
        });
    }

    public void syncStateToCurrentUser()
    {
        if(this.getUser() instanceof ServerPlayer player)
        {
            this.syncStateToPlayer(player);
        }
    }

    public void syncStateToPlayer(Player player)
    {
        ResourceLocation programId = this.currentProgram != null ? this.currentProgram.getId() : null;
        Network.getPlay().sendToPlayer(() -> (ServerPlayer) player, new MessageComputerState(this.worldPosition, programId));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ComputerBlockEntity computer)
    {
        ElectricityModuleBlockEntity.serverTick(level, pos, state, computer);
    }

    private void setPowerState(boolean powered)
    {
        this.systemData |= (powered ? (byte) 1 : (byte) 0);
    }

    private void setStartupTime(int time)
    {
        this.systemData |= (time << 16);
    }
}