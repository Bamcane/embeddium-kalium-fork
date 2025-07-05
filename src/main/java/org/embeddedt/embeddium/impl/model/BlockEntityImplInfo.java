package org.embeddedt.embeddium.impl.model;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class BlockEntityImplInfo {
    private static final ConcurrentHashMap<Class<? extends BlockEntity>, Boolean> OVERRIDES_GET_MODEL_DATA = new ConcurrentHashMap<>();

    public static boolean providesModelData(BlockEntity be) {
        return OVERRIDES_GET_MODEL_DATA.computeIfAbsent(be.getClass(), clz -> {
            try {
                Method method = clz.getMethod("getModelData");
                return method.getDeclaringClass() != net.neoforged.neoforge.common.extensions.IBlockEntityExtension.class;
            } catch (NoSuchMethodException e) {
                return false;
            }
        });
    }
}
