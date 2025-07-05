package org.embeddedt.embeddium.impl.render.chunk.compile.pipeline;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.embeddedt.embeddium.api.render.chunk.EmbeddiumBlockAndTintGetter;
import org.embeddedt.embeddium.impl.asm.ProxyClassGenerator;
import org.embeddedt.embeddium.impl.util.WorldUtil;
import org.embeddedt.embeddium.impl.world.WorldSlice;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import org.embeddedt.embeddium.impl.render.matrix_stack.CachingPoseStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Holds the context for the current block being rendered in a chunk section. This container is reused rather than
 * being freshly constructed for each block to avoid allocations.
 */
@Accessors(fluent = true)
public class BlockRenderContext {
    private static final ProxyClassGenerator<WorldSlice, EmbeddiumBlockAndTintGetter> WORLD_SLICE_LOCAL_GENERATOR = new ProxyClassGenerator<>(WorldSlice.class, "WorldSliceLocal", EmbeddiumBlockAndTintGetter.class);

    private final EmbeddiumBlockAndTintGetter localSlice;

    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    private final Vector3f origin = new Vector3f();

    //? if >=1.15
    private PoseStack stack;

    private BlockState state;

    /**
     * The model used for this block.
     */
    @Getter
    @Setter
    private BlockStateModel model;

    private long seed;

    @Setter
    @Accessors(fluent = false)
    private ModelData modelData;

    @Getter
    @Setter
    private ChunkSectionLayer renderLayer;

    private int lightValue = -1;

    @Getter
    //? if >=1.19 {
    private final net.minecraft.util.RandomSource random = new net.minecraft.world.level.levelgen.SingleThreadedRandomSource(42L);
    //?} else
    /*private final java.util.Random random = new org.embeddedt.embeddium.impl.util.rand.XoRoShiRoRandom(42L);*/

    @Getter
    private GeometryCategory category = GeometryCategory.BLOCK;

    public BlockRenderContext(WorldSlice world) {
        this.localSlice = WORLD_SLICE_LOCAL_GENERATOR.generateWrapper(world);
    }

    public void update(GeometryCategory category, BlockPos pos, BlockPos origin, BlockState state,
                       long seed) {
        this.category = category;
        this.pos.set(pos);
        this.origin.set(origin.getX(), origin.getY(), origin.getZ());

        this.state = state;

        this.seed = seed;

        this.random.setSeed(seed);

        this.lightValue = -1;
    }

    /**
     * @return The position (in world space) of the block being rendered
     */
    public BlockPos pos() {
        return this.pos;
    }

    /**
     * @return The world which the block is being rendered from. Guaranteed to be a new object for each subchunk.
     */
    public EmbeddiumBlockAndTintGetter localSlice() {
        return this.localSlice;
    }

    /**
     * @return The state of the block being rendered
     */
    public BlockState state() {
        return this.state;
    }

    //? if >=1.15 {
    /**
     * @return A PoseStack for custom renderers
     */
    public PoseStack stack() {
        if (this.stack == null) {
            this.stack = new PoseStack();
            ((CachingPoseStack)this.stack).embeddium$setCachingEnabled(true);
        }
        return this.stack;
    }
    //?}

    /**
     * @return The origin of the block within the model
     */
    public Vector3fc origin() {
        return this.origin;
    }

    /**
     * @return The PRNG seed for rendering this block
     */
    public long seed() {
        return this.seed;
    }

    //? if forgelike && >=1.19 {
    /**
     * @return The additional data for model instance
     */
    public ModelData modelData() {
        return this.modelData;
    }
    //?}

    //? if forgelike && <1.19 {
    /*/^*
     * @return The additional data for model instance
     ^/
    public IModelData modelData() {
        return this.modelData;
    }
    *///?}

    /**
     * @return The light emission of the current block
     */
    public int lightEmission() {
        if (this.lightValue == -1) {
            this.lightValue = WorldUtil.getLightEmission(this.state, this.localSlice, this.pos);
        }
        return this.lightValue;
    }

    @Override
    public String toString() {
        return "BlockRenderContext{state=" + this.state + ",pos=" + this.pos + "}";
    }
}
