package org.embeddedt.embeddium.impl.render.chunk.sprite;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface SpriteTransparencyLevelHolder {
    SpriteTransparencyLevel embeddium$getTransparencyLevel();

    static SpriteTransparencyLevel getTransparencyLevel(TextureAtlasSprite sprite) {
        return getTransparencyLevel(sprite.contents());
    }

    static SpriteTransparencyLevel getTransparencyLevel(SpriteContents contents) {
        return ((SpriteTransparencyLevelHolder)contents).embeddium$getTransparencyLevel();
    }
}
