package com.mrcrayfish.furniture.refurbished.platform;

import com.mrcrayfish.furniture.refurbished.platform.services.IEntityHelper;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayer;

/**
 * Author: MrCrayfish
 */
public class NeoForgeEntityHelper implements IEntityHelper
{
    @Override
    public boolean isFakePlayer(Player player)
    {
        return player instanceof FakePlayer;
    }

    @Override
    public void spawnFoodParticles(Player player, ItemStack stack)
    {
        player.spawnItemParticles(stack, 10);
    }

    @Override
    public SimpleParticleType createSimpleParticleType(boolean ignoreLimit)
    {
        return new SimpleParticleType(ignoreLimit);
    }
}
