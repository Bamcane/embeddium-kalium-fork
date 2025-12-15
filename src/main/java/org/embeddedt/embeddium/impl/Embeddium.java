package org.embeddedt.embeddium.impl;

import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.embeddedt.embeddium.api.EmbeddiumConstants;
import org.embeddedt.embeddium.impl.data.fingerprint.FingerprintMeasure;
import org.embeddedt.embeddium.impl.data.fingerprint.HashedFingerprint;
import org.embeddedt.embeddium.impl.gui.EmbeddiumOptions;
import org.embeddedt.embeddium.impl.gui.EmbeddiumVideoOptionsScreen;
import org.embeddedt.embeddium.impl.render.ShaderModBridge;
import org.embeddedt.embeddium.impl.sodium.FlawlessFrames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLLoader;

import java.io.IOException;

@Mod(value = Embeddium.MODID, dist = Dist.CLIENT)
public class Embeddium {
    public static final String MODID = EmbeddiumConstants.MODID;
    public static final String MODNAME = EmbeddiumConstants.MODNAME;

    private static final Logger LOGGER = LoggerFactory.getLogger(MODNAME);
    private static EmbeddiumOptions CONFIG = loadConfig();

    private static String MOD_VERSION;

    public Embeddium(IEventBus modEventBus) {
        var modContainer = ModList.get().getModContainerById(MODID).orElseThrow();
        MOD_VERSION = modContainer.getModInfo().getVersion().toString();
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (mc, screen) -> new EmbeddiumVideoOptionsScreen(screen, EmbeddiumVideoOptionsScreen.makePages()));

        if (!FMLLoader.getCurrent().getDist().isClient()) {
            return;
        }

        modEventBus.addListener(this::onClientSetup);
    }

    public void onClientSetup(final FMLClientSetupEvent event) {
        FlawlessFrames.onClientInitialization();
    }

    public static EmbeddiumOptions options() {
        if (CONFIG == null) {
            throw new IllegalStateException("Config not yet available");
        }

        return CONFIG;
    }

    public static Logger logger() {
        if (LOGGER == null) {
            throw new IllegalStateException("Logger not yet available");
        }

        return LOGGER;
    }

    private static EmbeddiumOptions loadConfig() {
        try {
            return EmbeddiumOptions.load();
        } catch (Exception e) {
            LOGGER.error("Failed to load configuration file", e);
            LOGGER.error("Using default configuration file in read-only mode");

            var config = new EmbeddiumOptions();
            config.setReadOnly();

            return config;
        }
    }

    public static void restoreDefaultOptions() {
        CONFIG = EmbeddiumOptions.defaults();

        try {
            EmbeddiumOptions.writeToDisk(CONFIG);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config file", e);
        }
    }

    public static String getVersion() {
        if (MOD_VERSION == null) {
            throw new NullPointerException("Mod version hasn't been populated yet");
        }

        return MOD_VERSION;
    }

    public static boolean canUseVanillaVertices() {
        return !Embeddium.options().performance.useCompactVertexFormat && !ShaderModBridge.areShadersEnabled();
    }

    public static boolean canApplyTranslucencySorting() {
        return Embeddium.options().performance.useTranslucentFaceSorting && !ShaderModBridge.isNvidiumEnabled();
    }

    public static boolean areGraphicsFancy() {
        return Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FAST;
    }
}
