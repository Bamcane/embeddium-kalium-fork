package org.embeddedt.embeddium.impl.render.chunk;

import org.embeddedt.embeddium.impl.Embeddium;
import org.embeddedt.embeddium.impl.gl.device.RenderDevice;
import org.embeddedt.embeddium.impl.render.chunk.shader.ChunkShaderInterface;
import org.embeddedt.embeddium.impl.render.chunk.shader.ChunkShaderTextureSlot;

public class ModernChunkRenderer extends DefaultChunkRenderer {
    public ModernChunkRenderer(RenderDevice device, RenderPassConfiguration<?> renderPassConfiguration) {
        super(device, renderPassConfiguration);
    }

    @Override
    protected boolean useBlockFaceCulling() {
        return Embeddium.options().performance.useBlockFaceCulling;
    }

    @Override
    protected void configureShaderInterface(ChunkShaderInterface shader) {
        shader.setTextureSlot(ChunkShaderTextureSlot.BLOCK, 0);
        shader.setTextureSlot(ChunkShaderTextureSlot.LIGHT, 2);
    }
}
