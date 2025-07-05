package org.embeddedt.embeddium.impl.render.chunk.config;

import com.google.common.collect.ImmutableListMultimap;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import org.embeddedt.embeddium.impl.Embeddium;
import org.embeddedt.embeddium.impl.render.chunk.RenderPassConfiguration;
import org.embeddedt.embeddium.impl.render.chunk.compile.sorting.QuadPrimitiveType;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.Material;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.lwjgl.opengl.GL32C;

import java.util.HashMap;
import java.util.Map;

public class ModernRenderPassConfigurationBuilder {

    private record ModernSectionLayerPipelineState(ChunkSectionLayer chunkSectionLayer) implements TerrainRenderPass.PipelineState {
        @Override
        public void setup() {
        }

        @Override
        public void clear() {
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ModernSectionLayerPipelineState that = (ModernSectionLayerPipelineState) o;
            return chunkSectionLayer == that.chunkSectionLayer;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(chunkSectionLayer);
        }
    }

    private static TerrainRenderPass.TerrainRenderPassBuilder builderForSectionLayer(ChunkSectionLayer chunkSectionLayer, ChunkVertexType vertexType) {
        var extraDefines = new HashMap<String, String>();

        if (Embeddium.options().quality.chunkFadeInDuration > 0) {
            extraDefines.put("CHUNK_FADE_IN_DURATION_MS", String.valueOf(Embeddium.options().quality.chunkFadeInDuration));
        }

        return TerrainRenderPass.builder().extraDefines(extraDefines).pipelineState(new ModernSectionLayerPipelineState(chunkSectionLayer)).vertexType(vertexType).primitiveType(QuadPrimitiveType.TRIANGULATED);
    }

    public static RenderPassConfiguration<ChunkSectionLayer> build(ChunkVertexType vertexType) {
        // First, build the main passes
        TerrainRenderPass solidPass, cutoutMippedPass, translucentPass, tripwirePass;

        solidPass = builderForSectionLayer(ChunkSectionLayer.SOLID, vertexType)
                .name("solid")
                .fragmentDiscard(false)
                .useReverseOrder(false)
                .build();
        cutoutMippedPass = builderForSectionLayer(ChunkSectionLayer.CUTOUT_MIPPED, vertexType)
                .name("cutout_mipped")
                .fragmentDiscard(true)
                .useReverseOrder(false)
                .build();
        translucentPass = builderForSectionLayer(ChunkSectionLayer.TRANSLUCENT, vertexType)
                .name("translucent")
                .fragmentDiscard(false)
                .useReverseOrder(true)
                .useTranslucencySorting(Embeddium.canApplyTranslucencySorting())
                .build();

        tripwirePass = builderForSectionLayer(ChunkSectionLayer.TRIPWIRE, vertexType)
                .name("tripwire")
                .fragmentDiscard(true)
                .useReverseOrder(true)
                .build();

        ImmutableListMultimap.Builder<ChunkSectionLayer, TerrainRenderPass> vanillaRenderStages = ImmutableListMultimap.builder();

        // Build the materials for the vanilla render passes
        Material solidMaterial, cutoutMaterial, cutoutMippedMaterial, translucentMaterial, tripwireMaterial;
        solidMaterial = new Material(solidPass, AlphaCutoffParameter.ZERO, true);
        translucentMaterial = new Material(translucentPass, AlphaCutoffParameter.ZERO, true);
        cutoutMippedMaterial = new Material(cutoutMippedPass, AlphaCutoffParameter.ONE_TENTH, true);

        vanillaRenderStages.put(ChunkSectionLayer.SOLID, solidPass);
        vanillaRenderStages.put(ChunkSectionLayer.TRANSLUCENT, translucentPass);

        tripwireMaterial = new Material(tripwirePass, AlphaCutoffParameter.ONE_TENTH, false);
        vanillaRenderStages.put(ChunkSectionLayer.TRIPWIRE, tripwirePass);

        if(Embeddium.options().performance.useRenderPassConsolidation) {
            cutoutMaterial = new Material(cutoutMippedPass, AlphaCutoffParameter.ONE_TENTH, false);
            // Render cutout immediately after solid geometry
            vanillaRenderStages.put(ChunkSectionLayer.SOLID, cutoutMippedPass);
        } else {
            TerrainRenderPass cutoutPass;

            cutoutPass = builderForSectionLayer(ChunkSectionLayer.CUTOUT, vertexType)
                    .name("cutout")
                    .fragmentDiscard(true)
                    .useReverseOrder(false)
                    .build();

            cutoutMaterial = new Material(cutoutPass, AlphaCutoffParameter.ONE_TENTH, false);
            vanillaRenderStages.put(ChunkSectionLayer.CUTOUT, cutoutPass);
            vanillaRenderStages.put(ChunkSectionLayer.CUTOUT_MIPPED, cutoutMippedPass);
        }

        // Now build the material map
        Map<ChunkSectionLayer, Material> SectionLayerToMaterialMap = new Reference2ReferenceOpenHashMap<>(5, Reference2ReferenceOpenHashMap.VERY_FAST_LOAD_FACTOR);

        SectionLayerToMaterialMap.put(ChunkSectionLayer.SOLID, solidMaterial);
        SectionLayerToMaterialMap.put(ChunkSectionLayer.CUTOUT, cutoutMaterial);
        SectionLayerToMaterialMap.put(ChunkSectionLayer.CUTOUT_MIPPED, cutoutMippedMaterial);
        SectionLayerToMaterialMap.put(ChunkSectionLayer.TRANSLUCENT, translucentMaterial);
        SectionLayerToMaterialMap.put(ChunkSectionLayer.TRIPWIRE, tripwireMaterial);

        var vanillaRenderStageMap = vanillaRenderStages.build();
        var allPasses = vanillaRenderStageMap.values().stream().distinct().toList();

        return new RenderPassConfiguration<>(SectionLayerToMaterialMap,
                vanillaRenderStageMap.asMap(),
                solidMaterial,
                cutoutMippedMaterial,
                translucentMaterial);
    }
}
