package org.embeddedt.embeddium.impl.util;

import net.minecraft.resources.ResourceLocation;

public class ResourceLocationUtil {
    public static ResourceLocation make(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static ResourceLocation make(String str) {
        if(str.contains(":")) {
            return ResourceLocation.parse(str);
        }
        return ResourceLocation.withDefaultNamespace(str);
    }
}
