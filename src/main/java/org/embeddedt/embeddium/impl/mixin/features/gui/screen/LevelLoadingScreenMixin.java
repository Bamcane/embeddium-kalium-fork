package org.embeddedt.embeddium.impl.mixin.features.gui.screen;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.embeddedt.embeddium.api.util.ColorABGR;
import org.embeddedt.embeddium.api.util.ColorARGB;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.*;

/**
 * Re-implements the loading screen with considerations to reduce draw calls and other sources of overhead. This can
 * improve world load times on slower processors with very few cores.
 */
@Mixin(LevelLoadingScreen.class)
public class LevelLoadingScreenMixin {
    @Mutable
    @Shadow
    @Final
    private static Object2IntMap<ChunkStatus> COLORS;

    @Unique
    private static Reference2IntOpenHashMap<ChunkStatus> STATUS_TO_COLOR_FAST;

    @Unique
    private static final int NULL_STATUS_COLOR = ColorABGR.pack(0, 0, 0, 0xFF);

    @Unique
    private static final int DEFAULT_STATUS_COLOR = ColorARGB.pack(0, 0x11, 0xFF, 0xFF);

    /**
     * This implementation differs from vanilla's in the following key ways.
     * - All tiles are batched together in one draw call, reducing CPU overhead by an order of magnitudes.
     * - Reference hashing is used for faster ChunkStatus -> Color lookup.
     * - Colors are stored in ABGR format so conversion is not necessary every tile draw.
     *
     * @reason Significantly optimized implementation.
     * @author JellySquid
     */
    @Overwrite
    public static void renderChunks(GuiGraphics drawContext, StoringChunkProgressListener tracker, int mapX, int mapY, int mapScale, int mapPadding) {
        if (STATUS_TO_COLOR_FAST == null) {
            STATUS_TO_COLOR_FAST = new Reference2IntOpenHashMap<>(COLORS.size());
            STATUS_TO_COLOR_FAST.put(null, NULL_STATUS_COLOR);
            COLORS.object2IntEntrySet()
                    .forEach(entry -> STATUS_TO_COLOR_FAST.put(entry.getKey(), ColorARGB.toABGR(entry.getIntValue(), 0xFF)));
        }
        int centerSize = tracker.getFullDiameter();
        int size = tracker.getDiameter();

        int tileSize = mapScale + mapPadding;

        if (mapPadding != 0) {
            int mapRenderCenterSize = centerSize * tileSize - mapPadding;
            int radius = mapRenderCenterSize / 2 + 1;

            addRect(drawContext, mapX - radius, mapY - radius, mapX - radius + 1, mapY + radius, DEFAULT_STATUS_COLOR);
            addRect(drawContext, mapX + radius - 1, mapY - radius, mapX + radius, mapY + radius, DEFAULT_STATUS_COLOR);
            addRect(drawContext, mapX - radius, mapY - radius, mapX + radius, mapY - radius + 1, DEFAULT_STATUS_COLOR);
            addRect(drawContext, mapX - radius, mapY + radius - 1, mapX + radius, mapY + radius, DEFAULT_STATUS_COLOR);
        }

        int mapRenderSize = size * tileSize - mapPadding;
        int mapStartX = mapX - mapRenderSize / 2;
        int mapStartY = mapY - mapRenderSize / 2;

        ChunkStatus prevStatus = null;
        int prevColor = NULL_STATUS_COLOR;

        for (int x = 0; x < size; ++x) {
            int tileX = mapStartX + x * tileSize;

            for (int z = 0; z < size; ++z) {
                int tileY = mapStartY + z * tileSize;

                ChunkStatus status = tracker.getStatus(x, z);
                int color;

                if (prevStatus == status) {
                    color = prevColor;
                } else {
                    color = STATUS_TO_COLOR_FAST.getInt(status);

                    prevStatus = status;
                    prevColor = color;
                }

                addRect(drawContext, tileX, tileY, tileX + mapScale, tileY + mapScale, color);
            }
        }
    }

    @Unique
    private static void addRect(GuiGraphics drawContext, int x1, int y1, int x2, int y2, int color) {
        drawContext.fill(x1, y1, x2, y2, color);
    }
}
