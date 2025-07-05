package org.embeddedt.embeddium.impl.loader.common;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.embeddedt.embeddium.impl.Embeddium;
import org.embeddedt.embeddium.impl.util.ResourceLocationUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ModLogoUtil {
    private static final Set<String> erroredLogos = new HashSet<>();

    public static ResourceLocation registerLogo(String modId) {
        Optional<String> logoFile = erroredLogos.contains(modId) ? Optional.empty() : ModList.get().getModContainerById(modId).flatMap(c -> c.getModInfo().getLogoFile());
        ResourceLocation texture = null;
        if(logoFile.isPresent()) {
            try {
                var modFile = ModList.get().getModFileById(modId).getFile();
                var logoStream = modFile.getContents().openFile(logoFile.get());
                if(logoStream != null) {
                    texture = handleIoSupplier(modId, logoStream);
                }
            } catch(IOException e) {
                erroredLogos.add(modId);
                Embeddium.logger().error("Exception reading logo for " + modId, e);
            }
        }
        return texture;
    }

    private static ResourceLocation handleIoSupplier(String modId, InputStream logoResource) throws IOException {
        if (logoResource != null) {
            NativeImage logo = NativeImage.read(logoResource);
            if(logo.getWidth() != logo.getHeight()) {
                logo.close();
                throw new IOException("Logo for " + modId + " is not square");
            }
            ResourceLocation texture = ResourceLocationUtil.make(Embeddium.MODID, "logo/" + modId);
            Minecraft.getInstance().getTextureManager().register(texture, new DynamicTexture(() -> logo.toString(), logo));
            return texture;
        } else {
            return null;
        }
    }
}
