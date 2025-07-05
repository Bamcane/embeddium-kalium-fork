package org.embeddedt.embeddium.impl.mixin.features.render.particle;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {
    @Accessor("x")
    double kalium$getX();

    @Accessor("y")
    double kalium$getY();

    @Accessor("z")
    double kalium$getZ();
}
