package org.embeddedt.embeddium.impl.world.cloned;

import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMaps;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.chunk.*;
import org.embeddedt.embeddium.impl.model.ModelDataSnapshotter;
import org.embeddedt.embeddium.impl.util.PositionUtil;
import org.embeddedt.embeddium.impl.util.WorldUtil;
import org.embeddedt.embeddium.impl.world.ChunkBiomeContainerExtended;
import org.embeddedt.embeddium.impl.world.ReadableContainerExtended;
import org.embeddedt.embeddium.impl.world.WorldSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ClonedChunkSection {
    private static final int DATA_LAYER_COUNT = DataLayer.LAYER_COUNT;
    private static final DataLayer DEFAULT_SKY_LIGHT_ARRAY = new DataLayer(15);
    private static final DataLayer DEFAULT_BLOCK_LIGHT_ARRAY = new DataLayer(0);

    private static final boolean HAS_FABRIC_RENDER_DATA;

    private final SectionPos pos;

    private final @Nullable Int2ReferenceMap<BlockEntity> blockEntityMap;
    private final @Nullable Int2ReferenceMap<Object> blockEntityRenderDataMap;
    @Getter
    private final ModelDataSnapshotter.Getter modelDataGetter;

    private final @Nullable DataLayer[] lightDataArrays;

    private final @Nullable PalettedContainer<BlockState> blockData;

    private final @Nullable PalettedContainer<Holder<Biome>> biomeData;

    private long lastUsedTimestamp = Long.MAX_VALUE;

    static {
        HAS_FABRIC_RENDER_DATA = false;
    }

    public ClonedChunkSection(Level world, LevelChunk chunk, @Nullable LevelChunkSection section, SectionPos pos) {
        this.pos = pos;

        PalettedContainer<BlockState> blockData = null;
        PalettedContainer<Holder<Biome>> biomeData = null;

        Int2ReferenceMap<BlockEntity> blockEntityMap = null;
        Int2ReferenceMap<Object> blockEntityRenderDataMap = null;

        if (section != null) {
            if (!WorldUtil.isSectionEmpty(section)) {
                if (!WorldUtil.isDebug(world)) {
                    blockData = ReadableContainerExtended.clone(section.getStates());
                } else {
                    blockData = constructDebugWorldContainer(world, pos);
                }
                blockEntityMap = copyBlockEntities(chunk, pos);

                if (blockEntityMap != null) {
                    blockEntityRenderDataMap = copyBlockEntityRenderData(blockEntityMap);
                }
            }

            biomeData = ReadableContainerExtended.clone((PalettedContainer<Holder<Biome>>)section.getBiomes());
        }

        this.blockData = blockData;
        this.biomeData = biomeData;

        this.blockEntityMap = blockEntityMap;
        this.blockEntityRenderDataMap = blockEntityRenderDataMap;

        this.lightDataArrays = copyLightData(world, pos);

        this.modelDataGetter = ModelDataSnapshotter.getModelDataForSection(world, pos);
    }

    /**
     * Construct a fake PalettedContainer whose contents match those of the debug world. This is needed to
     * match vanilla's odd approach of short-circuiting getBlockState calls inside its render region class.
     */
    private static PalettedContainer<BlockState> constructDebugWorldContainer(Level level, SectionPos pos) {
        // Fast path for sections which are guaranteed to be empty
        if (pos.getY() != 3 && pos.getY() != 4)
            return null;

        // We use swapUnsafe in the loops to avoid acquiring/releasing the lock on each iteration
        var container = new PalettedContainer<BlockState>(Blocks.AIR.defaultBlockState(), Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY));
        if (pos.getY() == 3) {
            // Set the blocks at relative Y 12 (world Y 60) to barriers
            BlockState barrier = Blocks.BARRIER.defaultBlockState();
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    container.getAndSetUnchecked(x, 12, z, barrier);
                }
            }
        } else if (pos.getY() == 4) {
            // Set the blocks at relative Y 6 (world Y 70) to the appropriate state from the generator
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    container.getAndSetUnchecked(x, 6, z, DebugLevelSource.getBlockStateFor(PositionUtil.sectionToBlockCoord(pos.getX(), x), PositionUtil.sectionToBlockCoord(pos.getZ(), z)));
                }
            }
        }
        return container;
    }

    @NotNull
    private static DataLayer[] copyLightData(Level world, SectionPos pos) {
        var arrays = new DataLayer[2];
        arrays[LightLayer.BLOCK.ordinal()] = copyLightArray(world, LightLayer.BLOCK, pos);

        // Dimensions without sky-light should not have a default-initialized array
        if (WorldUtil.hasSkyLight(world)) {
            arrays[LightLayer.SKY.ordinal()] = copyLightArray(world, LightLayer.SKY, pos);
        }

        return arrays;
    }

    /**
     * Copies the light data array for the given light type for this chunk, or returns a default-initialized value if
     * the light array is not loaded.
     */
    @NotNull
    private static DataLayer copyLightArray(Level world, LightLayer type, SectionPos pos) {
        var array = world.getLightEngine()
                .getLayerListener(type)
                .getDataLayerData(pos);

        if (array == null) {
            array = switch (type) {
                case SKY -> DEFAULT_SKY_LIGHT_ARRAY;
                case BLOCK -> DEFAULT_BLOCK_LIGHT_ARRAY;
            };
        }

        return array;
    }

    private static Iterable<Map.Entry<BlockPos, BlockEntity>> fastIterable(Map<BlockPos, BlockEntity> blockEntityMap) {
        if (blockEntityMap instanceof Object2ObjectMap<BlockPos, BlockEntity> fastutilMap) {
            //noinspection unchecked
            return (Iterable<Map.Entry<BlockPos, BlockEntity>>)(Iterable<?>)Object2ObjectMaps.fastIterable(fastutilMap);
        } else {
            return blockEntityMap.entrySet();
        }
    }

    @Nullable
    private static Int2ReferenceMap<BlockEntity> copyBlockEntities(LevelChunk chunk, SectionPos chunkCoord) {
        var chunkBlockEntityMap = chunk.getBlockEntities();

        if (chunkBlockEntityMap.isEmpty()) {
            return null;
        }

        BoundingBox box = new BoundingBox(chunkCoord.minBlockX(), chunkCoord.minBlockY(), chunkCoord.minBlockZ(),
                chunkCoord.maxBlockX(), chunkCoord.maxBlockY(), chunkCoord.maxBlockZ());

        Int2ReferenceOpenHashMap<BlockEntity> blockEntities = null;

        // Copy the block entities from the chunk into our cloned section
        for (Map.Entry<BlockPos, BlockEntity> entry : fastIterable(chunkBlockEntityMap)) {
            BlockPos pos = entry.getKey();
            BlockEntity entity = entry.getValue();

            if (box.isInside(pos)) {
                if (blockEntities == null) {
                    blockEntities = new Int2ReferenceOpenHashMap<>();
                }

                blockEntities.put(WorldSlice.getLocalBlockIndex(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15), entity);
            }
        }

        if (blockEntities != null) {
            blockEntities.trim();
        }

        return blockEntities;
    }

    @Nullable
    private static Int2ReferenceMap<Object> copyBlockEntityRenderData(Int2ReferenceMap<BlockEntity> blockEntities) {
        return null;
    }

    public SectionPos getPosition() {
        return this.pos;
    }

    public @Nullable PalettedContainer<BlockState> getBlockData() {
        return this.blockData;
    }

    //? if >=1.18.2 {
    public @Nullable PalettedContainer<Holder<Biome>> getBiomeData() {
        return this.biomeData;
    }
    //?}

    public @Nullable Int2ReferenceMap<BlockEntity> getBlockEntityMap() {
        return this.blockEntityMap;
    }

    public @Nullable Int2ReferenceMap<Object> getBlockEntityRenderDataMap() {
        return this.blockEntityRenderDataMap;
    }

    public @Nullable DataLayer getLightArray(LightLayer lightType) {
        return this.lightDataArrays[lightType.ordinal()];
    }

    public long getLastUsedTimestamp() {
        return this.lastUsedTimestamp;
    }

    public void setLastUsedTimestamp(long timestamp) {
        this.lastUsedTimestamp = timestamp;
    }

    public BlockState getBlockState(int x, int y, int z) {
        if (this.blockData != null) {
            return this.blockData.get(x, y, z);
        } else {
            return Blocks.AIR.defaultBlockState();
        }
    }
}
