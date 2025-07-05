package org.embeddedt.embeddium.impl.mixin.features.render.immediate.buffer_builder.intrinsics;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.embeddedt.embeddium.impl.model.quad.BakedQuadView;
import org.embeddedt.embeddium.impl.render.immediate.model.BakedModelEncoder;
import org.embeddedt.embeddium.api.render.texture.SpriteUtil;
import org.embeddedt.embeddium.api.util.ColorABGR;
import org.embeddedt.embeddium.api.vertex.buffer.VertexBufferWriter;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings({ "SameParameterValue" })
@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements VertexConsumer {
    @Shadow
    private boolean fastFormat;

    @Override
    public void putBulkData(PoseStack.Pose matrices, BakedQuad bakedQuad, float r, float g, float b, float a, int light, int overlay) {
        this.putBulkData(matrices, bakedQuad, r, g, b, a, light, overlay, false);
    }

    public void putBulkData(PoseStack.Pose matrices, BakedQuad bakedQuad, float r, float g, float b, float a, int light, int overlay, boolean colorize) {
        BakedQuadView quad = (BakedQuadView)(Object)bakedQuad;

        if (!this.fastFormat) {
            VertexConsumer.super.putBulkData(matrices, bakedQuad, r, g, b, a, light, overlay, colorize);

            SpriteUtil.markSpriteActive((TextureAtlasSprite)quad.kalium$getSprite());

            return;
        }

        if (quad.getVerticesCount() < 4) {
            return; // we do not accept quads with less than 4 properly sized vertices
        }

        VertexBufferWriter writer = VertexBufferWriter.of(this);


        int color = ColorABGR.pack(r, g, b, a);
        BakedModelEncoder.writeQuadVertices(writer, matrices, quad, color, light, overlay, colorize);

        SpriteUtil.markSpriteActive((TextureAtlasSprite)quad.kalium$getSprite());
    }

    @Override
    public void putBulkData(PoseStack.Pose matrices, BakedQuad bakedQuad, float[] brightnessTable, float r, float g, float b, float a, int[] light, int overlay, boolean colorize) {

        BakedQuadView quad = (BakedQuadView)(Object)bakedQuad;

        if (!this.fastFormat) {
            VertexConsumer.super.putBulkData(matrices, bakedQuad, brightnessTable, r, g, b, a, light, overlay, colorize);

            SpriteUtil.markSpriteActive((TextureAtlasSprite)quad.kalium$getSprite());

            return;
        }

        if (quad.getVerticesCount() < 4) {
            return; // we do not accept quads with less than 4 properly sized vertices
        }

        VertexBufferWriter writer = VertexBufferWriter.of(this);

        BakedModelEncoder.writeQuadVertices(writer, matrices, quad, r, g, b, a, brightnessTable, colorize, light, overlay);

        SpriteUtil.markSpriteActive((TextureAtlasSprite)quad.kalium$getSprite());
    }
}
