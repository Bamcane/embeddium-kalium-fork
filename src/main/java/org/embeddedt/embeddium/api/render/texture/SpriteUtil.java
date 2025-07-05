package org.embeddedt.embeddium.api.render.texture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.embeddedt.embeddium.impl.render.texture.SpriteContentsExtended;
import org.jetbrains.annotations.Nullable;

public class SpriteUtil {
    public static void markSpriteActive(@Nullable TextureAtlasSprite sprite) {
        if (sprite == null) {
            // Can happen in some cases, for example if a mod passes a BakedQuad with a null sprite
            // to a VertexConsumer that does not have a texture element.
            return;
        }

        ((SpriteContentsExtended) sprite.contents()).sodium$setActive(true);
    }

    public static boolean hasAnimation(TextureAtlasSprite sprite) {
        return sprite != null && ((SpriteContentsExtended) sprite.contents()).sodium$hasAnimation();
    }
}
