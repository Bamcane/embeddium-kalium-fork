package org.embeddedt.embeddium.impl.mixin.features.render.gui.outlines;

import org.embeddedt.embeddium.api.vertex.format.common.LineVertex;
import net.minecraft.client.renderer.LevelRenderer;
import org.embeddedt.embeddium.api.vertex.buffer.VertexBufferWriter;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin {
    @Unique
    private static void writeLineVertices(VertexBufferWriter writer, float x, float y, float z, int color, int normal) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long buffer = stack.nmalloc(2 * LineVertex.STRIDE);
            long ptr = buffer;

            for (int i = 0; i < 2; i++) {
                LineVertex.put(ptr, x, y, z, color, normal);
                ptr += LineVertex.STRIDE;
            }

            writer.push(stack, buffer, 2, LineVertex.FORMAT);
        }

    }

}
