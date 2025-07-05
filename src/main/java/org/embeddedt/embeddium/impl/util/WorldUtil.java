package org.embeddedt.embeddium.impl.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class WorldUtil {
    public static int getMinBuildHeight(LevelReader level) {
        return level.getMinY();
    }

    public static int getMaxBuildHeight(LevelReader level) {
        return level.getMaxY();
    }

    public static int getMinSection(LevelReader level) {
        return level.getMinSectionY();
    }

    public static int getMaxSection(LevelReader level) {
        return level.getMaxSectionY();
    }

    public static int getSectionIndexFromSectionY(LevelReader level, int sectionY) {
        return level.getSectionIndexFromSectionY(sectionY);
    }

    public static boolean isSectionEmpty(LevelChunkSection section) {
        return section == null || section.hasOnlyAir();
    }

    public static boolean hasBlockEntity(BlockState state) {
        return state.hasBlockEntity();
    }

    public static int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return state.getLightEmission(world, pos);
    }

    public static boolean isDebug(Level level) {
        return level.isDebug();
    }

    public static boolean hasSkyLight(Level level) {
        return level.dimensionType().hasSkyLight();
    }

    public static float getShade(BlockAndTintGetter getter, Direction lightFace, boolean shade) {
        return getter.getShade(lightFace, shade);
    }
}
