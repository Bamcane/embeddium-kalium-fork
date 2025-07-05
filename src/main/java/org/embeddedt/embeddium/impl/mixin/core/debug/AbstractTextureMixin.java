package org.embeddedt.embeddium.impl.mixin.core.debug;

import net.minecraft.client.renderer.texture.AbstractTexture;
import org.embeddedt.embeddium.impl.gl.debug.GLDebug;
import org.embeddedt.embeddium.impl.render.texture.NameableTexture;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractTexture.class)
public class AbstractTextureMixin implements NameableTexture {
    @Shadow
    protected int id;

    @Unique
    private String kalium$name;

    @Unique
    private boolean kalium$hasSetName;

    @Inject(method = "bind", at = @At("RETURN"))
    private void kalium$applyName(CallbackInfo ci) {
        if (!this.kalium$hasSetName && this.id != -1 && this.kalium$name != null) {
            this.kalium$hasSetName = true;
            GLDebug.nameObject(GL11.GL_TEXTURE, this.id, this.kalium$name);
        }
    }

    @Override
    public void kalium$setName(String name) {
        this.kalium$name = name;
        this.kalium$hasSetName = false;
    }
}
