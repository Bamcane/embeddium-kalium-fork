package org.embeddedt.embeddium.impl.mixin.features.render.entity.fast_render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.embeddedt.embeddium.api.util.*;
import org.embeddedt.embeddium.api.vertex.buffer.VertexBufferWriter;
import org.embeddedt.embeddium.impl.model.ModelCuboidAccessor;
import org.embeddedt.embeddium.impl.render.immediate.model.EntityRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

// Inject before everyone else
@Mixin(value = ModelPart.class, priority = 700)
public class ModelPartMixin_Early {
    @Shadow
    @Final
    private List<ModelPart.Cube> cubes;

    /**
     * @author JellySquid, embeddedt
     * @reason Rewrite entity rendering to use faster code path. Original approach of replacing the entire render loop
     * had to be neutered to accommodate mods injecting custom logic here and/or mutating the models at runtime.
     */
    @Overwrite
    public void compile(PoseStack.Pose matrixPose, VertexConsumer vertices, int light, int overlay, int color) {
        VertexBufferWriter writer = VertexBufferWriter.tryOf(vertices);

        EntityRenderer.prepareNormals(matrixPose);

        var cubes = this.cubes;
        int packedColor = ColorARGB.toABGR(color);

        //noinspection ForLoopReplaceableByForEach
        for(int i = 0; i < cubes.size(); i++) {
            var cube = cubes.get(i);
            var simpleCuboid = ((ModelCuboidAccessor)cube).embeddium$getSimpleCuboid();
            if(writer != null && simpleCuboid != null) {
                EntityRenderer.renderCuboidFast(matrixPose, writer, simpleCuboid, light, overlay, packedColor);
            } else {
                // Must use slow path as this cube can't be converted to a simple cuboid
                cube.compile(
                        matrixPose, vertices, light, overlay, color
                );
            }
        }
    }
}
