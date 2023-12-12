package com.mrcrayfish.furniture.refurbished.compat.crafttweaker.managers;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.CraftTweakerConstants;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import com.mrcrayfish.furniture.refurbished.core.ModRecipeTypes;
import com.mrcrayfish.furniture.refurbished.crafting.CuttingBoardRecipe;
import com.mrcrayfish.furniture.refurbished.crafting.ToasterHeatingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Author: MrCrayfish
 */
@ZenRegister
@Document("mods/RefurbishedFurniture/CuttingBoard")
@ZenCodeType.Name("mods.refurbished_furniture.CuttingBoard")
public class CuttingBoardRecipeManager implements IRecipeManager<CuttingBoardRecipe>
{
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredient input, IItemStack output)
    {
        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, new CuttingBoardRecipe(CraftTweakerConstants.rl(name), "", input.asVanillaIngredient(), output.getInternal())));
    }

    @Override
    public RecipeType<CuttingBoardRecipe> getRecipeType()
    {
        return ModRecipeTypes.CUTTING_BOARD_SLICING.get();
    }
}



