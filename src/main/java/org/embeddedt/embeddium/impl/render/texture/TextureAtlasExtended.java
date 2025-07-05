package org.embeddedt.embeddium.impl.render.texture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.embeddedt.embeddium.impl.util.collections.quadtree.QuadTree;

public interface TextureAtlasExtended {
    QuadTree<TextureAtlasSprite> kalium$getQuadTree();

    TextureAtlasSprite kalium$findFromUV(float u, float v);
}
