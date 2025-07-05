package org.embeddedt.embeddium.impl.model;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelDataManager;
import java.util.Map;

public class ModelDataSnapshotter {
    public interface Getter {
        Getter EMPTY = pos -> ModelData.EMPTY;
        ModelData getModelData(BlockPos pos);
    }

    /**
     * Retrieve all needed model data for the given subchunk.
     * @param world the client world to retrieve data for
     * @param origin the origin of the subchunk
     * @return a map of all model data contained within this subchunk
     */
    public static Getter getModelDataForSection(Level world, SectionPos origin) {
        var snapshot = world.getModelDataManager().snapshotSectionRegion(origin.getX(), origin.getY(), origin.getZ(), origin.getX(), origin.getY(), origin.getZ());
        if (snapshot == ModelDataManager.EMPTY_SNAPSHOT) {
            // Avoid an extra level of indirection
            return Getter.EMPTY;
        } else {
            return pos -> snapshot.get(pos.asLong());
        }
    }
}
