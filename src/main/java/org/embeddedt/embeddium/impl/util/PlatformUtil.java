package org.embeddedt.embeddium.impl.util;

import java.nio.file.Path;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

public class PlatformUtil {
    public static boolean isLoadValid() {
        return !FMLLoader.getCurrent().getLoadingModList().hasErrors();
    }

    public static boolean modPresent(String modid) {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById(modid) != null;
    }

    public static String getModName(String modId) {
        return ModList.get().getModContainerById(modId).map(container -> container.getModInfo().getDisplayName()).orElse(modId);
    }

    public static boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }
}
