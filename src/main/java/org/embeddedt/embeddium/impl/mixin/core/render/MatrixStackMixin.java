package org.embeddedt.embeddium.impl.mixin.core.render;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.embeddedt.embeddium.impl.render.matrix_stack.CachingPoseStack;
import org.spongepowered.asm.mixin.*;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(value = PoseStack.class, priority = 900)
public abstract class MatrixStackMixin implements CachingPoseStack {
    private int cacheEnabled = 0;

    @Shadow
    @Final
    private Deque<PoseStack.Pose> poseStack;

    @Unique
    private final Deque<PoseStack.Pose> cache = new ArrayDeque<>();

    /**
     * @author JellySquid
     * @reason Re-use entries when possible
     */
    @Overwrite
    public void pushPose() {
        var prev = this.poseStack.getLast();

        PoseStack.Pose entry;

        if (!this.cache.isEmpty()) {
            entry = this.cache.removeLast();
            entry.pose().set(prev.pose());
            entry.normal().set(prev.normal());
        } else {
            entry = new PoseStack.Pose();
            entry.set(prev);
        }

        entry.trustedNormals = prev.trustedNormals;

        this.poseStack.addLast(entry);
    }

    /**
     * @author JellySquid
     * @reason Re-use entries when possible
     */
    @Overwrite
    public void popPose() {
        PoseStack.Pose pose = this.poseStack.removeLast();
        if (this.cacheEnabled > 0 || !((CachingPoseStack.Pose)(Object)pose).kalium$hasEscaped()) {
            this.cache.addLast(pose);
        }
    }

    @ModifyReturnValue(method = "last", at = @At("RETURN"))
    private PoseStack.Pose kalium$markEscaped(PoseStack.Pose original) {
        ((CachingPoseStack.Pose)(Object)original).kalium$setEscaped();
        return original;
    }

    @Override
    public PoseStack.Pose kalium$last() {
        return this.poseStack.getLast();
    }

    @Override
    public void embeddium$setCachingEnabled(boolean flag) {
        this.cacheEnabled += (flag ? 1 : -1);
    }
}
