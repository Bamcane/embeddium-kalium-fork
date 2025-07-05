package org.embeddedt.embeddium.impl.mixin.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.embeddedt.embeddium.impl.render.matrix_stack.CachingPoseStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PoseStack.Pose.class)
public class PoseMixin implements CachingPoseStack.Pose {
    private boolean kalium$hasEscaped;

    @Override
    public boolean kalium$hasEscaped() {
        return kalium$hasEscaped;
    }

    @Override
    public void kalium$setEscaped() {
        kalium$hasEscaped = true;
    }
}
