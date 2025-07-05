package org.embeddedt.embeddium.impl.render.chunk;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public class ChunkRenderMatricesBuilder {


    public static ChunkRenderMatrices from(PoseStack stack) {
        PoseStack.Pose entry = stack.last();
        return new ChunkRenderMatrices(new Matrix4f(RenderSystem.getTextureMatrix()), new Matrix4f(entry.pose()));
    }

    public static ChunkRenderMatrices from(Matrix4f pose) {
        return new ChunkRenderMatrices(new Matrix4f(RenderSystem.getTextureMatrix()), pose);
    }
}
