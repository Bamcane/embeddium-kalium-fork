package org.embeddedt.embeddium.impl.mixin.core.model.quad;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.embeddedt.embeddium.impl.model.quad.BakedQuadView;
import org.embeddedt.embeddium.impl.model.quad.properties.ModelQuadFlags;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
//? if >=1.20
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Check if quad's UVs are contained within the sprite's boundaries; if so, mark it as having a trusted sprite
 * (meaning the particle sprite matches the encoded UVs)
 */
@Mixin(FaceBakery.class)
public class BakedQuadFactoryMixin {
    @ModifyReturnValue(method = "bakeQuad", at = @At("RETURN"))
    private BakedQuad setMaterialClassification(BakedQuad quad, @Local(ordinal = 0, argsOnly = true) BlockElementFace face, @Local(ordinal = 0, argsOnly = true) TextureAtlasSprite sprite) {
        handleMaterialClassifications(quad, sprite, face);
        return quad;
    }

    private static void handleMaterialClassifications(BakedQuad quad, TextureAtlasSprite sprite, BlockElementFace face) {
        if (sprite.getClass() == TextureAtlasSprite.class /*? if >=1.20 {*/ && sprite.contents().getClass() == SpriteContents.class /*?}*/) {

            float minUV = Float.MAX_VALUE, maxUV = Float.MIN_VALUE;
            //? if <1.21
            float[] uvs = {
                face.uvs().minU(), face.uvs().minV(),
                face.uvs().maxU(), face.uvs().maxV()
            };

            for (float uv : uvs) {
                minUV = Math.min(minUV, uv);
                maxUV = Math.max(maxUV, uv);
            }

            if (minUV >= 0 && maxUV <= 16) {
                // Quad UVs do not extend outside texture boundary, we can trust the given sprite
                BakedQuadView view = (BakedQuadView)(Object)quad;
                view.addFlags(ModelQuadFlags.IS_TRUSTED_SPRITE);
            }

        }
    }
}
