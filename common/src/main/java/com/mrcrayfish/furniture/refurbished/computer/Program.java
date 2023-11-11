package com.mrcrayfish.furniture.refurbished.computer;

import com.mrcrayfish.furniture.refurbished.blockentity.IComputer;
import com.mrcrayfish.furniture.refurbished.inventory.ComputerMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public abstract class Program
{
    private final ResourceLocation id;
    private final Component title;
    private final IComputer computer;

    public Program(ResourceLocation id, IComputer computer)
    {
        this.id = id;
        this.title = Component.translatable(String.format("%s.computer_program.%s", id.getNamespace(), id.getPath()));
        this.computer = computer;
    }

    public final ResourceLocation getId()
    {
        return this.id;
    }

    public Component getTitle()
    {
        return this.title;
    }

    public final IComputer getComputer()
    {
        return this.computer;
    }

    public void tick()
    {

    }

    public void saveState(CompoundTag tag) {}

    public void restoreState(CompoundTag tag) {}

    public void saveData(CompoundTag tag) {}

    public void loadData(CompoundTag tag) {}

    public void onClose(boolean remote)
    {

    }
}