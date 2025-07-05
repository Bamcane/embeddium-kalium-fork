package org.embeddedt.embeddium.impl.render.chunk.compile.pipeline;

import lombok.Getter;
import org.embeddedt.embeddium.impl.Embeddium;
import org.embeddedt.embeddium.impl.model.color.ColorProviderRegistry;
import org.embeddedt.embeddium.impl.model.light.DiffuseProvider;
import org.embeddedt.embeddium.impl.model.light.LightPipelineProvider;
import org.embeddedt.embeddium.impl.model.quad.ArrayLightDataCache;
import org.embeddedt.embeddium.impl.world.WorldSlice;
import org.embeddedt.embeddium.impl.world.cloned.ChunkRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockModelShaper;

/**
 * Holds important caches and working data structures for a single chunk meshing thread. All objects within
 * this class do not need to be thread-safe or lightweight to construct, as a separate instance is allocated per thread
 * and reused for the lifetime of that thread.
 */
public class BlockRenderCache {
    private final ArrayLightDataCache lightDataCache;

    private final BlockRenderer blockRenderer;
    private final LightPipelineProvider lightPipelineProvider;
    @Getter
    private final SpecialBlockRenderer specialBlockRenderer;

    private final BlockModelShaper blockModels;
    private final WorldSlice worldSlice;

    public BlockRenderCache(Minecraft client, ClientLevel world) {
        this.worldSlice = new WorldSlice(world);
        this.lightDataCache = new ArrayLightDataCache(this.worldSlice);

        LightPipelineProvider lightPipelineProvider = new LightPipelineProvider(this.lightDataCache, DiffuseProvider.NONE,
                Embeddium.options().quality.useQuadNormalsForShading);

        var colorRegistry = new ColorProviderRegistry(client.getBlockColors());

        this.blockRenderer = new BlockRenderer(colorRegistry, lightPipelineProvider,
                null, worldSlice
        );
        this.lightPipelineProvider = lightPipelineProvider;
        this.specialBlockRenderer = new SpecialBlockRenderer();

        this.blockModels = client.getModelManager().getBlockModelShaper();
    }

    public BlockModelShaper getBlockModels() {
        return this.blockModels;
    }

    public BlockRenderer getBlockRenderer() {
        return this.blockRenderer;
    }

    /**
     * Initialize the render cache for a new chunk.
     */
    public void init(ChunkRenderContext context) {
        this.lightDataCache.reset(context.getOrigin().minBlockX(), context.getOrigin().minBlockY(), context.getOrigin().minBlockZ());
        this.lightPipelineProvider.reset();
        this.worldSlice.copyData(context);
    }

    public WorldSlice getWorldSlice() {
        return this.worldSlice;
    }

    public void cleanup() {
        this.worldSlice.reset();
    }
}
