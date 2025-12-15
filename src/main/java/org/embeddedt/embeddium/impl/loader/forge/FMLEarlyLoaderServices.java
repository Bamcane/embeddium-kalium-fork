package org.embeddedt.embeddium.impl.loader.forge;

import net.neoforged.fml.jarcontents.JarResourceVisitor;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.neoforged.neoforge.resource.JarContentsPackResources;
import net.neoforged.neoforge.resource.ResourcePackLoader;

import org.embeddedt.embeddium.impl.loader.common.Distribution;
import org.embeddedt.embeddium.impl.loader.common.EarlyLoaderServices;

import static org.embeddedt.embeddium.impl.Embeddium.logger;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class FMLEarlyLoaderServices implements EarlyLoaderServices {
    private static final String JSON_KEY_SODIUM_OPTIONS = "sodium:options";

    @Override
    public Path findEarlyMixinFolder(String path) {
        ModFileInfo modFileInfo = FMLLoader.getCurrent().getLoadingModList().getModFileById("embeddium");

        if (modFileInfo == null) {
            // Probably a load error
            return null;
        }

        ModFile modFile = modFileInfo.getFile();

        if(modFile.getContents().containsFile(path + "MixinPlugin.class"))
            return Paths.get(path);
        else
            return null;
    }

    @Override
    public Distribution getDistribution() {
        return FMLLoader.getCurrent().getDist().isClient() ? Distribution.CLIENT : Distribution.SERVER;
    }

    @Override
    public boolean isLoadingNormally() {
        return FMLLoader.getCurrent().getLoadingModList().hasErrors();
    }

    public List<String> getLoadedModIds() {
        return FMLLoader.getCurrent().getLoadingModList().getMods().stream().map(ModInfo::getModId).toList();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null;
    }

    @Override
    public void readModMixinConfigOverrides(Consumer<MixinConfigOverride> consumer) {
        // Example of how to put overrides into the mods.toml file:
        // ...
        // [[mods]]
        // modId="examplemod"
        // [mods."sodium:options"]
        // "features.chunk_rendering"=false
        // ...
        for (var meta : FMLLoader.getCurrent().getLoadingModList().getMods()) {
            meta.getConfigElement(JSON_KEY_SODIUM_OPTIONS).ifPresent(overridesObj -> {
                if (overridesObj instanceof Map overrides && overrides.keySet().stream().allMatch(key -> key instanceof String)) {
                    overrides.forEach((key, value) -> {
                        if(value instanceof Boolean flag)
                            consumer.accept(new MixinConfigOverride(meta.getModId(), (String)key, flag));
                    });
                }
            });
        }
    }
}
