package org.embeddedt.embeddium.impl.util.sorting;

import com.mojang.blaze3d.vertex.CompactVectorArray;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.joml.Vector3f;

public class VertexSorters {
    public static VertexSorting sortByDistance(Vector3f origin) {
        return new SortByDistance(origin);
    }

    private static class SortByDistance extends AbstractVertexSorter {
        private final Vector3f origin;

        private SortByDistance(Vector3f origin) {
            this.origin = origin;
        }

        @Override
        protected float getKey(Vector3f position) {
            return this.origin.distanceSquared(position);
        }
    }

    private static abstract class AbstractVertexSorter implements VertexSorting {
        @Override
        public final int[] sort(CompactVectorArray positions) {
            return this.mergeSort(positions);
        }

        private int[] mergeSort(CompactVectorArray positions) {
            final var keys = new float[positions.size()];
            final Vector3f temp = new Vector3f(0);
            for (int index = 0; index < positions.size(); index++) {
                positions.get(index, temp);
                keys[index] = this.getKey(temp);
            }

            return MergeSort.mergeSort(keys);
        }

        protected abstract float getKey(Vector3f object);
    }
}
