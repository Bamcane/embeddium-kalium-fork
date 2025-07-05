package org.embeddedt.embeddium.impl.mixin.features.render.model.block;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.embeddedt.embeddium.impl.model.quad.BakedQuadView;
import org.embeddedt.embeddium.impl.render.immediate.model.BakedModelEncoder;
import org.embeddedt.embeddium.api.render.texture.SpriteUtil;
import org.embeddedt.embeddium.api.vertex.buffer.VertexBufferWriter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
//$ rng_import
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;

@Mixin(ModelBlockRenderer.class)
public class BlockModelRendererMixin {
    @Unique
    private final RandomSource random = new SingleThreadedRandomSource(42L);

    @Unique
    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static void renderQuads(PoseStack.Pose matrices, VertexBufferWriter writer, int defaultColor, List<BakedQuad> quads, int light, int overlay) {
        for (int i = 0; i < quads.size(); i++) {
            BakedQuad bakedQuad = quads.get(i);

            BakedQuadView quad = (BakedQuadView)(Object)bakedQuad;

            if (quad.getVerticesCount() < 4) {
                continue;
            }

            int color = quad.hasColor() ? defaultColor : 0xFFFFFFFF;

            BakedModelEncoder.writeQuadVertices(writer, matrices, quad, color, light, overlay, true);

            SpriteUtil.markSpriteActive((TextureAtlasSprite)quad.kalium$getSprite());
        }
    }
}
