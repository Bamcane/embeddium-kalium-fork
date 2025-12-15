package org.embeddedt.embeddium.impl.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class MixinClassValidator {
    private static final String MIXIN_DESC = "Lorg/spongepowered/asm/mixin/Mixin;";

    public static boolean isMixinClass(byte[] bytecode) {
        return isMixinClass(fromBytecode(bytecode));
    }

    public static ClassNode fromBytecode(byte[] bytecode) {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytecode);
        reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        return node;
    }

    public static boolean isMixinClass(ClassNode node) {
        if(node.invisibleAnnotations == null) {
            return false;
        }

        return node.invisibleAnnotations.stream().anyMatch(annotation -> annotation.desc.equals(MIXIN_DESC));
    }
}
