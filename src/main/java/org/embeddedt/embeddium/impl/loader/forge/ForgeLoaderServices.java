package org.embeddedt.embeddium.impl.loader.forge;

import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

import org.embeddedt.embeddium.impl.loader.common.LoaderServices;

public final class ForgeLoaderServices implements LoaderServices {
    @Override
    public int getFluidTintColor(BlockAndTintGetter world, FluidState state, BlockPos pos) {
        return IClientFluidTypeExtensions.of(state).getTintColor(state, world, pos);
    }

    @Override
    public boolean isCullableAABB(AABB box) {
        return !box.equals(AABB.INFINITE);
    }
}